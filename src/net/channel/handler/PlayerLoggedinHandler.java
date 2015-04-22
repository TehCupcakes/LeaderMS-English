/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.channel.handler;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import client.MaplePet;
import client.MapleQuestStatus;
import client.MapleStat;
import client.PetDataFactory;
import client.SkillFactory;
import config.configuration.Configuration;
import database.DatabaseConnection;
import java.awt.Point;
import java.util.ArrayList;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.channel.PetStorage;
import net.world.CharacterIdChannelPair;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerBuffValueHolder;
import net.world.PlayerCoolDownValueHolder;
import net.world.guild.MapleAlliance;
import net.world.remote.WorldChannelInterface;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class PlayerLoggedinHandler extends AbstractMaplePacketHandler {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlayerLoggedinHandler.class);

	@Override
	public boolean validateState(MapleClient c) {
		return !c.isLoggedIn();
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int cid = slea.readInt();
		MapleCharacter player = null;
		try {
			player = MapleCharacter.loadCharFromDB(cid, c, true);
			c.setPlayer(player);
		} catch (SQLException e) {
			log.error("Loading the char failed", e);
		}
		c.setAccID(player.getAccountID());
		int state = c.getLoginState();
		boolean allowLogin = true;
		ChannelServer channelServer = c.getChannelServer();
		synchronized (this) {
			try {
				WorldChannelInterface worldInterface = channelServer.getWorldInterface();
				if (state == MapleClient.LOGIN_SERVER_TRANSITION) {
					for (String charName : c.loadCharacterNames(c.getWorld())) {
						if (worldInterface.isConnected(charName)) {
							log.warn(MapleClient.getLogMessage(player, "Attempting to double login with " + charName));
							allowLogin = false;
							break;
						}
					}
				}
			} catch (RemoteException e) {
				channelServer.reconnectWorld();
				allowLogin = false;
			}
			if (state != MapleClient.LOGIN_SERVER_TRANSITION || !allowLogin) {
				c.setPlayer(null); 
				c.getSession().close();
				return;
			}
			c.updateLoginState(MapleClient.LOGIN_LOGGEDIN);
		}
		ChannelServer cserv = ChannelServer.getInstance(c.getChannelByWorld());
		cserv.addPlayer(player);
		try {
			WorldChannelInterface wci = ChannelServer.getInstance(c.getChannelByWorld()).getWorldInterface();
			List<PlayerBuffValueHolder> buffs = wci.getBuffsFromStorage(cid);
			if (buffs != null) {
				c.getPlayer().silentGiveBuffs(buffs);
			}
			List<PlayerCoolDownValueHolder> cooldowns = wci.getCooldownsFromStorage(cid);
			if (cooldowns != null) {
				c.getPlayer().giveCoolDowns(cooldowns);
			}
		} catch (RemoteException e) {
			c.getChannelServer().reconnectWorld();
		}
		
       Connection con = DatabaseConnection.getConnection();
       c.getSession().write(MaplePacketCreator.getCharInfo(player));
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages WHERE RecieverId = ? and checked = 1");
            ps.setInt(1, c.getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                DueyHandler.reciveMsg(c, c.getPlayer().getId()); //Only once, since it will set all checks = 0 anyhow.
            }
            ps.close();
            rs.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
            if (player.isGM()) {
            //GMs hide when logged in. Uncomment to enable.
            //player.Hide(true, true);
            //SkillFactory.getSkill(9101004).getEffect(1).applyTo(player, true, true);
            player.setChatMode(1);
               }
		player.getMap().addPlayer(player);
		try {
			Collection<BuddylistEntry> buddies = player.getBuddylist().getBuddies();
			int buddyIds[] = player.getBuddylist().getBuddyIds();

			cserv.getWorldInterface().loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
			if (player.getParty() != null) {
				cserv.getWorldInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
			}

			CharacterIdChannelPair[] onlineBuddies = cserv.getWorldInterface().multiBuddyFind(player.getId(), buddyIds);
			for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
				BuddylistEntry ble = player.getBuddylist().get(onlineBuddy.getCharacterId());
				ble.setChannel(onlineBuddy.getChannel());
				player.getBuddylist().put(ble);
			}
			c.getSession().write(MaplePacketCreator.updateBuddylist(buddies));

			c.getPlayer().sendMacros();
                        c.getPlayer().showNote();

	       if (player.getGuildId() > 0) {
                c.getChannelServer().getWorldInterface().setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.getSession().write(MaplePacketCreator.showGuildInfo(player));
                int allianceId = player.getGuild().getAllianceId();
                if (allianceId > 0) {
                    MapleAlliance newAlliance = cserv.getWorldInterface().getAlliance(allianceId);
                    if (newAlliance == null) {
                        newAlliance = MapleAlliance.loadAlliance(allianceId);
                        cserv.getWorldInterface().addAlliance(allianceId, newAlliance);
                    }
                    c.getSession().write(MaplePacketCreator.getAllianceInfo(newAlliance));
                    c.getSession().write(MaplePacketCreator.getGuildAlliances(newAlliance, c));
                    c.getChannelServer().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.allianceMemberOnline(player, true), player.getId(), -1);
                }
            }
         } catch (RemoteException e) {
                cserv.reconnectWorld();
            }
        try {
            c.getPlayer().showNote();
            if (player.getParty() != null) {
                cserv.getWorldInterface().updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));
            }
            player.updatePartyMemberHP();
        } catch (RemoteException e) {
            cserv.reconnectWorld();
        }
		player.updatePartyMemberHP();

		player.sendKeymap();
		for (MapleQuestStatus status : player.getStartedQuests()) {
                if (status.hasMobKills()) {
                c.getSession().write(MaplePacketCreator.updateQuestMobKills(status));
                          }
                 }
		CharacterNameAndId pendingBuddyRequest = player.getBuddylist().pollPendingRequest();
		if (pendingBuddyRequest != null) {
			player.getBuddylist().put(new BuddylistEntry(pendingBuddyRequest.getName(), pendingBuddyRequest.getId(), -1, false));
			c.getSession().write(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), pendingBuddyRequest.getName()));
		}

                player.checkMessenger();
		player.checkBerserk();
                player.expirationTask();
                player.dropOverheadMessage(Configuration.Jogador_Logado);
                if (player.getLevel() <= 8) {
                    c.announce(MaplePacketCreator.getWhisper("[" + Configuration.Server_Name + " Information]", 1, Configuration.Jogador_Iniciante));
                }
                if (c.getPlayer().getMapId() == 0 && c.getPlayer().getLevel() == 1) {
                    c.getChannelServer().yellowWorldMessage("[" + c.getPlayer().getName() + "] " + Configuration.Novo_Jogador);
                }
                /* Buffs for new players... Removed because not GMS-like
	        if (player.getLevel() <= 8) {
			player.giveItemBuff(2022118);
                        player.giveItemBuff(4101004);
                        player.giveItemBuff(2301004);
			c.getSession().write(MaplePacketCreator.serverNotice(5, Configuration.Jogador_Buffado));
		}
                */
             
       MaplePet[] petz = PetStorage.getPetz(player.getId());
        if (petz != null) {
            for (int i = 0; i < 3; i++) {
                if (petz[i] != null) {
                    MaplePet pet = petz[i];
                    // Handle dragons
                    // Handle robo
                    if (c.getPlayer().getPetIndex(pet) != -1) {
                        c.getPlayer().unequipPet(pet, true);
                    } else {
                        if (c.getPlayer().getSkillLevel(SkillFactory.getSkill(8)) == 0 && c.getPlayer().getPet(0) != null) {
                            c.getPlayer().unequipPet(c.getPlayer().getPet(0), false);
                        }

                        Point pos = c.getPlayer().getPosition();
                        pos.y -= 12;
                        pet.setPos(pos);
                        pet.setFh(c.getPlayer().getMap().getFootholds().findBelow(pet.getPos()).getId());
                        pet.setStance(0);

                        c.getPlayer().addPet(pet);

                        // Broadcast packet to the map...
                        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showPet(c.getPlayer(), pet, false), true);

                        // Find the pet's unique ID
                        int uniqueid = pet.getUniqueId();

                        // Make a new List for the stat update
                        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
                        stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(uniqueid)));
                        // Write the stat update to the player...
                        c.getSession().write(MaplePacketCreator.petStatUpdate(c.getPlayer()));
                        c.getSession().write(MaplePacketCreator.enableActions());

                        // Get the data
                        int hunger = PetDataFactory.getHunger(pet.getItemId());

                        // Start the fullness schedule
                        c.getPlayer().startFullnessSchedule(hunger, pet, c.getPlayer().getPetIndex(pet));

                    }
                }
            }
        }       
     }
}


