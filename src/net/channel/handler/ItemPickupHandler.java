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

/*
 * ItemPickupHandler.java
 *
 * Created on 29. November 2007, 13:39
 */
package net.channel.handler;

import java.rmi.RemoteException;
import java.util.concurrent.ScheduledFuture;
import client.MapleCharacter;
import net.channel.ChannelServer;
import net.world.MaplePartyCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MaplePet;
import client.anticheat.CheatingOffense;
import config.Game.CashPQ;
import config.Game.Items;
import net.AbstractMaplePacketHandler;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.life.MobSkillFactory;
import server.life.MobSkill;
import net.world.MapleParty;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class ItemPickupHandler extends AbstractMaplePacketHandler {
	final Pair<?, ?>[] necksonCards = {
		new Pair<Integer, Integer>(4031530, 100),
		new Pair<Integer, Integer>(4031531, 250),
	};
	/** Creates a new instance of ItemPickupHandler */
	public ItemPickupHandler() {
	}

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		@SuppressWarnings("unused")
		byte mode = slea.readByte();	// or something like that... but better ignore it if you want
		// mapchange to work! o.o!
		slea.readInt(); //?
		slea.readInt(); // position, but we dont need it o.o
		int oid = slea.readInt();
		MapleMapObject ob = c.getPlayer().getMap().getMapObject(oid);
		if (ob == null) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			c.getSession().write(MaplePacketCreator.getShowInventoryFull());
			return;
		}
                 if(c.getPlayer().getGMLevel() == 3) {
                    c.getSession().write(MaplePacketCreator.serverNotice(5, "JR GameMasters cannot pick up items."));
                    return;
                 }
		if (ob instanceof MapleMapItem) {
			MapleMapItem mapitem = (MapleMapItem) ob;
			synchronized (mapitem) {
				if (mapitem.isPickedUp()) {
					c.getSession().write(MaplePacketCreator.getInventoryFull());
					c.getSession().write(MaplePacketCreator.getShowInventoryFull());
					return;
				}
				if (!mapitem.isFfa()) {
					if (mapitem.getOwner() != null) {
						if (c.getPlayer().getParty() != null) {
							if (c.getPlayer().getParty().getId() ==
									mapitem.getOwner().getId()
									|| c.getPlayer().getId() == mapitem.getOwner().getId()) {
								//continue..
							} else {
								return;
							}
						} else if (c.getPlayer().getId() != mapitem.getOwner().getId()) {
							return;
						} else {
							//continue
						}
					} else {
						//continue
					}
				}
					
				double distance = c.getPlayer().getPosition().distanceSq(mapitem.getPosition());
				c.getPlayer().getCheatTracker().checkPickupAgain();
				if (distance > 90000.0) { // 300^2, 550 is approximatly the range of ultis
					AutobanManager.getInstance().addPoints(c, 100, 300000, "Itemvac");
					c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.ITEMVAC);
                                        if (c.getPlayer().getCheatTracker().getPoints() > 50)
                                            c.getPlayer().getClient().getSession().close();
				// Double.valueOf(Math.sqrt(distance))
				} else if (distance > 30000.0) {
					// log.warn("[h4x] Player {} is picking up an item that's fairly far away: {}", c.getPlayer().getId(), Double.valueOf(Math.sqrt(distance)));
					c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.SHORT_ITEMVAC);
                                        if (c.getPlayer().getCheatTracker().getPoints() > 50)
                                            c.getPlayer().getClient().getSession().close();
				}
                                if (!c.getPlayer().isAlive()) {
                                        c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
                                        if (c.getPlayer().getCheatTracker().getPoints() > 10) {
                                            try {
                                                c.getPlayer().getClient().getChannelServer().getWorldInterface().broadcastGMMessage("", MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " is looting while dead.").getBytes());
                                            } catch (RemoteException ex) {
                                                c.getPlayer().getClient().getChannelServer().reconnectWorld();
                                            }
                                            c.getPlayer().getClient().getSession().close();
                                        }
                                        return;
                                }
				for (Pair<?, ?> pair : necksonCards) {
					if (mapitem.getMeso() <= 0) {
						if (mapitem.getItem().getItemId() == (Integer)pair.getLeft()) {
							c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
							c.getPlayer().getCheatTracker().pickupComplete();
							c.getPlayer().getMap().removeMapObject(ob);
							int necksonGain = (Integer) pair.getRight();
							c.getSession().write(MaplePacketCreator.serverNotice(5, "[Cash System] You gained LeaderNX (+" + necksonGain + ")."));
							c.getPlayer().modifyCSPoints(4, necksonGain);
							c.getSession().write(MaplePacketCreator.getShowItemGain(mapitem.getItem().getItemId(), (short)1));
							c.getSession().write(MaplePacketCreator.enableActions());
							return;
						}
					}
				}
				if (mapitem.getMeso() > 0) {
					if (c.getPlayer().getParty() != null) {
						ChannelServer cserv = c.getChannelServer();
						int mesosamm = mapitem.getMeso();
						int partynum = 0;
						for (MaplePartyCharacter partymem : c.getPlayer().getParty().getMembers()) {
							if (partymem.isOnline() && partymem.getMapid() == c.getPlayer().getMap().getId() && partymem.getChannel() == c.getChannel()) {
								partynum++;
							}
						}
						int mesosgain = mesosamm / partynum;
						for (MaplePartyCharacter partymem : c.getPlayer().getParty().getMembers()) {
							if (partymem.isOnline() && partymem.getMapid() == c.getPlayer().getMap().getId()) {
								MapleCharacter somecharacter = cserv.getPlayerStorage().getCharacterById(partymem.getId());
								if (somecharacter != null) {
									somecharacter.gainMeso(mesosgain, true, true);
								}
							}
						}
					} else {
						c.getPlayer().gainMeso(mapitem.getMeso(), true, true);
					}
					c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
					c.getPlayer().getCheatTracker().pickupComplete();
					c.getPlayer().getMap().removeMapObject(ob);
				}else if (mapitem.getItem().getItemId() >= 2022157 && mapitem.getItem().getItemId() <= 2022178) {
					MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
					c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()),
					mapitem .getPosition());
					c.getPlayer().getCheatTracker().pickupComplete();
					c.getPlayer().getMap().removeMapObject(ob);
					short q = 1;
					c.getSession().write(MaplePacketCreator.getShowItemGain(mapitem.getItem().getItemId(), q));
					if (mapitem.getItem().getItemId() == 2022157 || mapitem.getItem().getItemId() == 2022158 || mapitem.getItem().getItemId() == 2022159) {
						//This gives your party CP
						try {
							c.getPlayer().gainCP(mapitem.getItem().getItemId()- 2022156);
						} catch (NullPointerException manfred) {
							//Do nothing
						}
					}
					if (mapitem.getItem().getItemId() == 2022160 || mapitem.getItem().getItemId() == 2022161 || mapitem.getItem().getItemId() == 2022162 || mapitem.getItem().getItemId() == 2022163) {
						ii.getItemEffect(mapitem.getItem().getItemId()).applyTo(c.getPlayer());
						if (c.getPlayer().getParty() != null) {
							for (int i = 0; i<c.getPlayer().getParty().getMembers().size(); i++) {
								MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(c.getPlayer().getParty().getMemberByPos(i).getId());
								if (chr != null)
									ii.getItemEffect(mapitem.getItem().getItemId()).applyTo(chr);
							}
						}                                                            
						//All Cure o.O
						if (c.getPlayer().getParty() != null && mapitem.getItem().getItemId() == 2022163) {
							for (int i = 0; i<c.getPlayer().getParty().getMembers().size(); i++) {
								c.getChannelServer().getPlayerStorage().getCharacterById(c.getPlayer().getParty().getMemberByPos(i).getId()).cancelAllDebuffs();
							}
						}
					} else if (mapitem.getItem().getItemId() == 2022164 || mapitem.getItem().getItemId() == 2022165 || mapitem.getItem().getItemId() == 2022166) {// || mapitem.getItem().getItemId() == 2022174 || mapitem.getItem().getItemId() == 2022175 || mapitem.getItem().getItemId() == 2022176 || mapitem.getItem().getItemId() == 2022177 || mapitem.getItem().getItemId() == 2022178) {
						//Debuff Givers
						MapleDisease canDo[] = {
							MapleDisease.DARKNESS, 
							MapleDisease.SEAL, 
							MapleDisease.STUN, /*MapleDisease.POISON,*/ 
							MapleDisease.SLOW, 
							MapleDisease.WEAKEN 
						};
						int skills[] = {
							121,
							120,
							123, /*125,*/ 
							126, 
							122
						};
						//darkness, seal, stun, slow, weaken
						int levels[] = {6, 10, 11, 6, 7};
						int randd = (int) Math.floor(Math.random() * canDo.length);
						MapleDisease toCast = canDo[randd];
						int skillID = skills[randd];
						MobSkill skill = MobSkillFactory.getMobSkill(skillID, levels[randd]);
						if (mapitem.getItem().getItemId() == 2022164) {
							//This only affects one random member
							MapleParty enemy = c.getPlayer().getParty().getEnemy();
							if (enemy != null) {
								int amount = enemy.getMembers().size();
								randd = (int) Math.floor(Math.random() * amount);
								MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(c.getPlayer().getParty().getEnemy().getMemberByPos(randd).getId());
								if (chr != null)
									chr.giveDebuff(toCast, skill, true);
							}
							c.getSession().write(MaplePacketCreator.enableActions());
						} else if (mapitem.getItem().getItemId() == 2022165) { //Cube of Darkness
							if (c.getPlayer().getParty().getEnemy() != null) {
								for (int i = 0; i< c.getPlayer().getParty().getEnemy().getMembers().size(); i++) {
									MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(c.getPlayer().getParty().getEnemy().getMemberByPos(i).getId());
									if (chr != null)
										chr.giveDebuff(toCast, skill, true);
								}
							}
							c.getSession().write(MaplePacketCreator.enableActions());
						} else if (mapitem.getItem().getItemId() == 2022166) { //Stunner
							MapleParty enemy = c.getPlayer().getParty().getEnemy();
							if (enemy != null) {
								skill = MobSkillFactory.getMobSkill(123, 11);
								int amount = enemy.getMembers().size();
								randd = (int) Math.floor(Math.random() * amount);
								MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(c.getPlayer().getParty().getEnemy().getMemberByPos(randd).getId());
								if (chr != null)
									chr.giveDebuff(MapleDisease.STUN, skill, true);
							}
							c.getSession().write(MaplePacketCreator.enableActions());
						}
					} else if (mapitem.getItem().getItemId() == 2022174 || mapitem.getItem().getItemId() == 2022175 || mapitem.getItem().getItemId() == 2022176 || mapitem.getItem().getItemId() == 2022177 || mapitem.getItem().getItemId() == 2022178) {
						//Use Pots only on me
						ii.getItemEffect(mapitem.getItem().getItemId()).applyTo(c.getPlayer());
					}
					c.getSession().write(MaplePacketCreator.enableActions());
			}else if (mapitem.getItem().getItemId() >= 2022266 && mapitem.getItem().getItemId() <= 2022269) {
					if (mapitem.getItem().getItemId() == 2022266 || mapitem.getItem().getItemId() == 2022267) {
						c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()),
						mapitem .getPosition());
						c.getPlayer().getCheatTracker().pickupComplete();
						c.getPlayer().getMap().removeMapObject(ob);
						short q = 1;
						c.getSession().write(MaplePacketCreator.getShowItemGain(mapitem.getItem().getItemId(), q));
						MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
						ii.getItemEffect(mapitem.getItem().getItemId()).applyTo(c.getPlayer());
						c.getSession().write(MaplePacketCreator.enableActions());
					} else if (mapitem.getItem().getItemId() == 2022268) {
						c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()),
						mapitem .getPosition());
						c.getPlayer().getCheatTracker().pickupComplete();
						c.getPlayer().getMap().removeMapObject(ob);
						short q = 1;
						c.getSession().write(MaplePacketCreator.getShowItemGain(mapitem.getItem().getItemId(), q));
						MobSkill skill = MobSkillFactory.getMobSkill(129, 3);
						for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
							if (chr.getId() != c.getPlayer().getId()) {
								chr.giveDebuff(MapleDisease.SWITCH_CONTROLS, skill);
							}
						}
						c.getSession().write(MaplePacketCreator.enableActions());
					} else if (mapitem.getItem().getItemId() == 2022269) {
						c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()),
						mapitem .getPosition());
						c.getPlayer().getCheatTracker().pickupComplete();
						c.getPlayer().getMap().removeMapObject(ob);
						short q = 1;
						c.getSession().write(MaplePacketCreator.getShowItemGain(mapitem.getItem().getItemId(), q));
						//Aura thingy... protects against bombs
						final MapleCharacter chr = c.getPlayer();
						ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(new Runnable() {
							public void run() {
								chr.cancelShield();
							}
						}, 60 * 1000);
						chr.shield(schedule);
						
						c.getSession().write(MaplePacketCreator.enableActions());
					}
					c.getPlayer().getMap().removeMapObject(ob);
				  } else {
                        if (mapitem.getItem().getItemId() >= 5000000 && mapitem.getItem().getItemId() <= 5000100) {
						int petId = MaplePet.createPet(mapitem.getItem().getItemId());
						if (petId == -1) {
							return;
						}
                                                MapleInventoryManipulator.addById(c, mapitem.getItem().getItemId(), mapitem.getItem().getQuantity(), "", null, petId);
						c.getPlayer().getMap().broadcastMessage(
							MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()),
							mapitem.getPosition());
						c.getPlayer().getCheatTracker().pickupComplete();
						c.getPlayer().getMap().removeMapObject(ob);
					} else {
						if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), "Picked up by " + c.getPlayer().getId(), true)) {
							c.getPlayer().getMap().broadcastMessage(
								MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()),
								mapitem.getPosition());
							c.getPlayer().getCheatTracker().pickupComplete();
							c.getPlayer().getMap().removeMapObject(ob);
							if (mapitem.getItem().getItemId() == 4031868) {
								c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(c.getPlayer().getName(), c.getPlayer().getAriantScore(), false));
							}
						} else {
							c.getPlayer().getCheatTracker().pickupComplete();
							return;
						}
					}
				} 
				mapitem.setPickedUp(true);
			}
		}
		c.getSession().write(MaplePacketCreator.enableActions());
	}
        
        

    public boolean useItem(MapleClient c, int id) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (Items.isUse(id)) { // TO prevent caching of everything, waste of mem
            if (ii.isConsumeOnPickup(id)) {
                ii.getItemEffect(id).applyTo(c.getPlayer());
                return true;
            }

        }
        return false;
    }

    private void removeItem(MapleCharacter chr, MapleMapItem mapitem, MapleMapObject ob) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
        chr.getCheatTracker().pickupComplete();
        chr.getMap().removeMapObject(ob);
    }
}
