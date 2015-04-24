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

package handling.channel.handler;

import java.rmi.RemoteException;
import client.MapleClient;
import client.inventory.MaplePet;
import client.anticheat.CheatingOffense;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.inventory.MapleInventoryType;
import tools.Pair;

/**
 *
 * @author Raz
 * 
 */
public class PetLootHandler extends AbstractMaplePacketHandler {
	final Pair<?, ?>[] necksonCards = {
		new Pair<Integer, Integer>(4031530, 100),
		new Pair<Integer, Integer>(4031531, 250),
	};

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

		if (c.getPlayer().getNoPets() == 0) {
			return;
		}
		MaplePet pet = c.getPlayer().getPet(c.getPlayer().getPetIndex(slea.readInt()));
		slea.skip(13);
		int oid = slea.readInt();
		MapleMapObject ob = c.getPlayer().getMap().getMapObject(oid);
		if (ob == null || pet == null) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			return;
		}
		if (ob instanceof MapleMapItem) {
			MapleMapItem mapitem = (MapleMapItem) ob;
			synchronized (mapitem) {
				if (mapitem.isPickedUp()) {
					c.getSession().write(MaplePacketCreator.getInventoryFull());
					return;
				}
				double distance = pet.getPos().distanceSq(mapitem.getPosition());
				c.getPlayer().getCheatTracker().checkPickupAgain();
				if (distance > 90000.0) { // 300^2, 550 is approximatly the range of ultis
                                c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.ITEMVAC);
                                } else if (distance > 22500.0) {
                                c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.SHORT_ITEMVAC);
                                }
                for (Pair<?, ?> pair : necksonCards) {
                    if (mapitem.getMeso() <= 0) {
                        if (mapitem.getItem().getItemId() == (Integer) pair.getLeft()) {
                            c.getPlayer().getMap().broadcastMessage(
                                    MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()),
                                    mapitem.getPosition());
                            c.getPlayer().getCheatTracker().pickupComplete();
                            c.getPlayer().getMap().removeMapObject(ob);
                            int necksonGain = (Integer) pair.getRight();
                            c.getSession().write(MaplePacketCreator.serverNotice(5, "[Cash System] You gained NX (+" + necksonGain + ")."));
                            c.getPlayer().modifyCSPoints(4, necksonGain);
                            c.getSession().write(MaplePacketCreator.getShowItemGain(mapitem.getItem().getItemId(), (short) 1));
                            c.getSession().write(MaplePacketCreator.enableActions());
                            return;
                        }
                    }
                }
				if (mapitem.getMeso() > 0 && mapitem.getDropper() != c.getPlayer()) {
					if(c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812000) != null) { //Evil hax until I find the right packet - Ramon
						c.getPlayer().gainMeso(mapitem.getMeso(), true, true);
						c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, c.getPlayer().getId(), true, c.getPlayer().getPetIndex(pet)), mapitem.getPosition());
						c.getPlayer().getCheatTracker().pickupComplete();
						c.getPlayer().getMap().removeMapObject(ob);
					} else {
						c.getPlayer().getCheatTracker().pickupComplete();
						mapitem.setPickedUp(false);
						c.getSession().write(MaplePacketCreator.enableActions());
						return;
					}
				} else if (mapitem.getDropper() != c.getPlayer()) {
					if (ii.isPet(mapitem.getItem().getItemId())) {
						int petId = MaplePet.createPet(mapitem.getItem().getItemId());
						if (petId == -1) {
							return;
						}
                                                MapleInventoryManipulator.addById(c, mapitem.getItem().getItemId(), mapitem.getItem().getQuantity(), null);
						c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, c.getPlayer().getId()), mapitem.getPosition());
						c.getPlayer().getCheatTracker().pickupComplete();
						c.getPlayer().getMap().removeMapObject(ob);
					} else {
						if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), "Picked up by " + c.getPlayer().getName(), true)) {
							c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, c.getPlayer().getId(), true, c.getPlayer().getPetIndex(pet)), mapitem.getPosition());
							c.getPlayer().getCheatTracker().pickupComplete();
							c.getPlayer().getMap().removeMapObject(ob);
						} else {
							c.getPlayer().getCheatTracker().pickupComplete();
							return;
						}
					}
				}
				if (mapitem.getDropper() != c.getPlayer())
					mapitem.setPickedUp(true);
			}
		}
		c.getSession().write(MaplePacketCreator.enableActions());
	}
}