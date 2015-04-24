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

package handling.channel;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import client.BuddyList;
import client.BuddylistEntry;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import client.inventory.MaplePet;
import database.DatabaseConnection;
import handling.ByteArrayMaplePacket;
import handling.MaplePacket;
import handling.channel.remote.ChannelWorldInterface;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.guild.MapleGuildSummary;
import handling.world.remote.CheaterData;
import server.ShutdownServer;
import server.TimerManager;
import tools.CollectionUtil;
import tools.packet.*;

/**
 *
 * @author Matze
 */

public class ChannelWorldInterfaceImpl extends UnicastRemoteObject implements ChannelWorldInterface {
	private static final long serialVersionUID = 7815256899088644192L;

	private ChannelServer server;
	
	public ChannelWorldInterfaceImpl() throws RemoteException {
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	}
	
	public ChannelWorldInterfaceImpl(ChannelServer server) throws RemoteException {
		super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
		this.server = server;
	}
	
	public void setChannelId(int id) throws RemoteException {
		server.setChannel(id);
	}

	public int getChannelId() throws RemoteException {
		return server.getChannel();
	}

	public String getIP() throws RemoteException {
		return server.getIP();
	}

	public void broadcastMessage(String sender, byte[] message) throws RemoteException {
		MaplePacket packet = new ByteArrayMaplePacket(message);
		server.broadcastPacket(packet);
	}

    public void broadcastGMMessage(String sender, byte[] message) throws RemoteException {
		MaplePacket packet = new ByteArrayMaplePacket(message);
		server.broadcastGMPacket(packet);
	}

	public void whisper(String sender, String target, int channel, String message) throws RemoteException {
		if (isConnected(target)) {
			server.getPlayerStorage().getCharacterByName(target).getClient().getSession().write(
				MaplePacketCreator.getWhisper(sender, channel, message));
		}
	}

    public void spouse(String sender, String target, int channel, String message) throws RemoteException {
		if (isConnected(target)) {
			server.getPlayerStorage().getCharacterByName(target).getClient().getSession().write(
				MaplePacketCreator.toSpouse(sender, message, 5));
		}
	}

    public boolean isConnected(String charName) throws RemoteException {
        MapleCharacter check = server.getPlayerStorage().getCharacterByName(charName);
        if (check != null) {
            if (check.getClient() != null) {
                if (check.getClient().getSession() != null) {
                    if (check.getClient().getSession().isConnected()) {
                        return true;
                    } else {
                        server.removePlayer(check);
                    }
                } else {
                    server.removePlayer(check);
                }
            } else {
                server.removePlayer(check);
            }
        }
        return false;
    }

	public void shutdown(int time) throws RemoteException {
		server.broadcastPacket(
			MaplePacketCreator.serverNotice(0, "O servidor vai ser desligado em " + (time / 60000) + " minuto(s), por favor, fazer logoff com seguranca."));
		TimerManager.getInstance().schedule(new ShutdownServer(server.getChannel()), time);
	}

	public int getConnected() throws RemoteException {
		return server.getConnectedClients();
	}
	
	@Override
	public void loggedOff(String name, int characterId, int channel, int[] buddies) throws RemoteException {
		updateBuddies(characterId, channel, buddies, true);
	}

	@Override
	public void loggedOn(String name, int characterId, int channel, int buddies[]) throws RemoteException {
		updateBuddies(characterId, channel, buddies, false);
	}
	
	private void updateBuddies(int characterId, int channel, int[] buddies, boolean offline) {
		IPlayerStorage playerStorage = server.getPlayerStorage();
		for (int buddy : buddies) {
			MapleCharacter chr = playerStorage.getCharacterById(buddy);
			if (chr != null) {
				BuddylistEntry ble = chr.getBuddylist().get(characterId);
				if (ble != null && ble.isVisible()) {
					int mcChannel;
					if (offline) {
						ble.setChannel(-1);
						mcChannel = -1;
					} else {
						ble.setChannel(channel);
						mcChannel = channel - 1;
					}
					chr.getBuddylist().put(ble);
					chr.getClient().getSession().write(MaplePacketCreator.updateBuddyChannel(ble.getCharacterId(), mcChannel));
				}
			}
		}
	}

        
        
         @Override
    public void updateParty(MapleParty party, PartyOperation operation, MaplePartyCharacter target) throws RemoteException {
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar.getChannel() == server.getChannel()) {
                MapleCharacter chr = server.getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    if (operation == PartyOperation.DISBAND) {
                        chr.setParty(null);
                    } else {
                        chr.setParty(party);
                    }
                    chr.getClient().getSession().write(MaplePacketCreator.updateParty(chr.getClient().getChannel(), party, operation, target));
                }
            }
        }
        switch (operation) {
            case LEAVE:
            case EXPEL:
                if (target.getChannel() == server.getChannel()) {
                    MapleCharacter chr = server.getPlayerStorage().getCharacterByName(target.getName());
                    if (chr != null) {
                        chr.getClient().getSession().write(MaplePacketCreator.updateParty(chr.getClient().getChannel(), party, operation, target));
                        chr.setParty(null);
                    }
                }
                break;
            case CHANGE_LEADER:
                if (target.getChannel() == server.getChannel()) {
                    MapleCharacter chr = server.getPlayerStorage().getCharacterByName(target.getName());
                    if (chr != null) {
                        chr.getParty().setLeader(target);
                    }
                }
        }
    }

	@Override
	public void partyChat(MapleParty party, String chattext, String namefrom) throws RemoteException {
		for (MaplePartyCharacter partychar : party.getMembers()) {
			if (partychar.getChannel() == server.getChannel() && !(partychar.getName().equals(namefrom))) {
				MapleCharacter chr = server.getPlayerStorage().getCharacterByName(partychar.getName());
				if (chr != null) {
					chr.getClient().getSession().write(MaplePacketCreator.multiChat(namefrom, chattext, 1));
				}
			}
		}
	}

	public boolean isAvailable() throws RemoteException {
		return true;
	}

	public int getLocation(String name) throws RemoteException {
		MapleCharacter chr = server.getPlayerStorage().getCharacterByName(name);
		if (chr != null)
			return server.getPlayerStorage().getCharacterByName(name).getMapId();
		return -1;
	}

	public List<CheaterData> getCheaters() throws RemoteException {
		List<CheaterData> cheaters = new ArrayList<CheaterData>();
		List<MapleCharacter> allplayers = new ArrayList<MapleCharacter>(server.getPlayerStorage().getAllCharacters());
		/*Collections.sort(allplayers, new Comparator<MapleCharacter>() {
			@Override
			public int compare(MapleCharacter o1, MapleCharacter o2) {
				int thisVal = o1.getCheatTracker().getPoints();
				int anotherVal = o2.getCheatTracker().getPoints();
				return (thisVal<anotherVal ? 1 : (thisVal==anotherVal ? 0 : -1));
			}
		});*/
		for (int x = allplayers.size() - 1; x >= 0; x--) {
			MapleCharacter cheater = allplayers.get(x);
			if (cheater.getCheatTracker().getPoints() > 0) {
				cheaters.add(new CheaterData(cheater.getCheatTracker().getPoints(), MapleCharacterUtil.makeMapleReadable(cheater.getName()) + " (" + cheater.getCheatTracker().getPoints() + ") " + cheater.getCheatTracker().getSummary()));
			}
		}
		Collections.sort(cheaters);
		return CollectionUtil.copyFirst(cheaters, 10);
	}

	@Override
	public BuddyAddResult requestBuddyAdd(String addName, int channelFrom, int cidFrom, String nameFrom) {
		MapleCharacter addChar = server.getPlayerStorage().getCharacterByName(addName);
		if (addChar != null) {
			BuddyList buddylist = addChar.getBuddylist();
			if (buddylist.isFull()) {
				return BuddyAddResult.BUDDYLIST_FULL;
			}
			if (!buddylist.contains(cidFrom)) {
				buddylist.addBuddyRequest(addChar.getClient(), cidFrom, nameFrom, channelFrom);
			} else {
				if (buddylist.containsVisible(cidFrom)) {
					return BuddyAddResult.ALREADY_ON_LIST;
				}
			}
		}
		return BuddyAddResult.OK;
	}

	@Override
	public boolean isConnected(int characterId) throws RemoteException {
		MapleCharacter check = server.getPlayerStorage().getCharacterById(characterId);
		if (check != null) {
			if (check.getClient() != null) {
				if (check.getClient().getSession() != null) {
					if (check.getClient().getSession().isConnected()) {
						return true;
					} else {
						if (check.getMap() != null) {
							if (check.getMap().getCharacterById(check.getId()) != null) {
								check.getMap().removePlayer(check);
							}
						}
						server.removePlayer(check);
					}
				} else {
					if (check.getMap() != null) {
						if (check.getMap().getCharacterById(check.getId()) != null) {
							check.getMap().removePlayer(check);
						}
					}
					server.removePlayer(check);
				}
			} else {
				if (check.getMap() != null) {
					if (check.getMap().getCharacterById(check.getId()) != null) {
						check.getMap().removePlayer(check);
					}
				}
				server.removePlayer(check);
			}
		}
		return false;
	}

	@Override
	public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation operation) {
		MapleCharacter addChar = server.getPlayerStorage().getCharacterById(cid);
		if (addChar != null) {
			BuddyList buddylist = addChar.getBuddylist();
			switch (operation) {
				case ADDED:
					if (buddylist.contains(cidFrom)) {
						buddylist.put(new BuddylistEntry(name, cidFrom, channel, true));
						addChar.getClient().getSession().write(MaplePacketCreator.updateBuddyChannel(cidFrom, channel - 1));
					}
					break;
				case DELETED:
					if (buddylist.contains(cidFrom)) {
						buddylist.put(new BuddylistEntry(name, cidFrom, -1, buddylist.get(cidFrom).isVisible()));
						addChar.getClient().getSession().write(MaplePacketCreator.updateBuddyChannel(cidFrom, -1));
					}
					break;
			}
		}
	}

	@Override
	public void buddyChat(int[] recipientCharacterIds, int cidFrom, String nameFrom, String chattext) throws RemoteException {
		IPlayerStorage playerStorage = server.getPlayerStorage();
		for (int characterId : recipientCharacterIds) {
			MapleCharacter chr = playerStorage.getCharacterById(characterId);
			if (chr != null) {
				if (chr.getBuddylist().containsVisible(cidFrom)) {
					chr.getClient().getSession().write(MaplePacketCreator.multiChat(nameFrom, chattext, 0));
				}
			}
		}
	}

	@Override
	public int[] multiBuddyFind(int charIdFrom, int[] characterIds) throws RemoteException {
		List<Integer> ret = new ArrayList<Integer>(characterIds.length);
		IPlayerStorage playerStorage = server.getPlayerStorage();
		for (int characterId : characterIds) {
			MapleCharacter chr = playerStorage.getCharacterById(characterId);
			if (chr != null) {
				if (chr.getBuddylist().containsVisible(charIdFrom)) {
					ret.add(characterId);
				}
			}
		}
		int [] retArr = new int[ret.size()];
		int pos = 0;
		for (Integer i : ret) {
			retArr[pos++] = i.intValue();
		}
		return retArr;
	}

	@Override
	public void sendPacket(List<Integer> targetIds, MaplePacket packet,
			int exception) 
		throws RemoteException
	{
		MapleCharacter c;
		for (int i : targetIds)
		{
			if (i == exception) continue;
			c = server.getPlayerStorage().getCharacterById(i);
			if (c != null)
				c.getClient().getSession().write(packet);
		}
	}
	
	@Override
	public void setGuildAndRank(List<Integer> cids, int guildid, int rank,
			int exception) throws RemoteException
	{
		for (int cid : cids)
			if (cid != exception)
				setGuildAndRank(cid, guildid, rank);
	}
	
	@Override
	public void setGuildAndRank(int cid, int guildid, int rank) throws RemoteException
	{
		MapleCharacter mc =	server.getPlayerStorage().getCharacterById(cid);
		if (mc == null)
		{
			// System.out.println("ERROR: cannot find player in given channel");
			return;
		}
		
		boolean bDifferentGuild;
		if (guildid == -1 && rank == -1)	//just need a respawn
			bDifferentGuild = true;
		else
		{
			bDifferentGuild = guildid != mc.getGuildId();
			mc.setGuildId(guildid);
			mc.setGuildRank(rank);
			mc.saveGuildStatus();
		}
		
		
		if (bDifferentGuild)
		{
			mc.getMap().broadcastMessage(mc,
					MaplePacketCreator.removePlayerFromMap(cid), false);
			mc.getMap().broadcastMessage(mc,
					MaplePacketCreator.spawnPlayerMapobject(mc), false);
			MaplePet[] pets = mc.getPets();
			for (int i = 0; i < 3; i++) {
				if (pets[i] != null) {
					mc.getMap().broadcastMessage(mc, PetPacket.showPet(mc, pets[i], false, false), false);
				}
			}
		}
	}

	@Override
	public void setOfflineGuildStatus(int guildid, byte guildrank, int cid) throws RemoteException
	{
		org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
		try
		{
			java.sql.Connection con = DatabaseConnection.getConnection();
			java.sql.PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ? WHERE id = ?");
			ps.setInt(1, guildid);
			ps.setInt(2, guildrank);
			ps.setInt(3, cid);
			ps.execute();
			ps.close();
		}
		catch (SQLException se)
		{
			log.error("SQLException: " + se.getLocalizedMessage(), se);
		}
	}
	
	@Override
	public void reloadGuildCharacters() throws RemoteException
	{
		for (MapleCharacter mc : server.getPlayerStorage().getAllCharacters())
		{
			if (mc.getGuildId() > 0)
			{
				//multiple world ops, but this method is ONLY used 
				//in !clearguilds gm command, so it shouldn't be a problem
				server.getWorldInterface().setGuildMemberOnline(
						mc.getMGC(), true, server.getChannel());
				server.getWorldInterface().memberLevelJobUpdate(mc.getMGC());
			}
		}
		
		ChannelServer.getInstance(this.getChannelId()).reloadGuildSummary();
	}
	
	@Override
	public void changeEmblem(int gid, 
			List<Integer> affectedPlayers, MapleGuildSummary mgs)
	throws RemoteException
	{
		ChannelServer.getInstance(this.getChannelId()).updateGuildSummary(gid, mgs);
		this.sendPacket(affectedPlayers, 
				MaplePacketCreator.guildEmblemChange(gid, 
						mgs.getLogoBG(), mgs.getLogoBGColor(),
						mgs.getLogo(), mgs.getLogoColor()),
						-1
		);
		this.setGuildAndRank(affectedPlayers, -1, -1, -1);	//respawn player
	}

	public void messengerInvite(String sender, int messengerid, String target, int fromchannel) throws RemoteException {
		if (isConnected(target)) {
			MapleMessenger messenger = server.getPlayerStorage().getCharacterByName(target).getMessenger();
			if (messenger == null) {
				server.getPlayerStorage().getCharacterByName(target).getClient().getSession().write(MaplePacketCreator.messengerInvite(sender, messengerid));
				MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(sender);
				from.getClient().getSession().write(MaplePacketCreator.messengerNote(target, 4, 1));
			} else {
				MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(sender);
				from.getClient().getSession().write(MaplePacketCreator.messengerChat(sender + " : " + target + " is already using Maple Messenger"));
			}
		}
	}

	public void addMessengerPlayer(MapleMessenger messenger, String namefrom, int fromchannel, int position) throws RemoteException {
		for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
			if (messengerchar.getChannel() == server.getChannel() && !(messengerchar.getName().equals(namefrom))) {
				MapleCharacter chr = server.getPlayerStorage().getCharacterByName(messengerchar.getName());
				if (chr != null) {
					MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(namefrom);
					chr.getClient().getSession().write(MaplePacketCreator.addMessengerPlayer(namefrom, from, position, fromchannel - 1));
					from.getClient().getSession().write(MaplePacketCreator.addMessengerPlayer(chr.getName(), chr, messengerchar.getPosition(), messengerchar.getChannel() - 1));
				}
			} else if (messengerchar.getChannel() == server.getChannel() && (messengerchar.getName().equals(namefrom))) {
				MapleCharacter chr = server.getPlayerStorage().getCharacterByName(messengerchar.getName());
				if (chr != null) {
					chr.getClient().getSession().write(MaplePacketCreator.joinMessenger(messengerchar.getPosition()));
				}
			}
		}
	}
	
	public void removeMessengerPlayer(MapleMessenger messenger, int position) throws RemoteException {
		for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
			if (messengerchar.getChannel() == server.getChannel()) {
				MapleCharacter chr = server.getPlayerStorage().getCharacterByName(messengerchar.getName());
				if (chr != null) {
					chr.getClient().getSession().write(MaplePacketCreator.removeMessengerPlayer(position));
				}
			}
		}
	}
	
	public void messengerChat(MapleMessenger messenger, String chattext, String namefrom) throws RemoteException {
		for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
			if (messengerchar.getChannel() == server.getChannel() && !(messengerchar.getName().equals(namefrom))) {
				MapleCharacter chr = server.getPlayerStorage().getCharacterByName(messengerchar.getName());
				if (chr != null) {
					chr.getClient().getSession().write(MaplePacketCreator.messengerChat(chattext));
				}
			}
		}
	}
	
	public void declineChat(String target, String namefrom) throws RemoteException {
		if (isConnected(target)) {
			MapleMessenger messenger = server.getPlayerStorage().getCharacterByName(target).getMessenger();
			if (messenger != null) {
				server.getPlayerStorage().getCharacterByName(target).getClient().getSession().write(
				MaplePacketCreator.messengerNote(namefrom, 5, 0));
			}
		}
	}
        
        
    public void broadcastSMega(String sender, byte[] message) throws RemoteException {
        MaplePacket packet = new ByteArrayMaplePacket(message);
        server.broadcastSMega(packet);
    }
	
	public void updateMessenger(MapleMessenger messenger, String namefrom, int position, int fromchannel) throws RemoteException {
		for (MapleMessengerCharacter messengerchar : messenger.getMembers()) {
			if (messengerchar.getChannel() == server.getChannel() && !(messengerchar.getName().equals(namefrom))) {
				MapleCharacter chr = server.getPlayerStorage().getCharacterByName(messengerchar.getName());
				if (chr != null) {
					MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(namefrom);
					chr.getClient().getSession().write(MaplePacketCreator.updateMessengerPlayer(namefrom, from, position, fromchannel - 1));
				}
			}
		}
	}
}
