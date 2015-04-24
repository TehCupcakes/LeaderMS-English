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
 * UseItemHandler.java
 *
 * Created on 27. November 2007, 16:51
 */

package handling.channel.handler;

import client.IItem;
import client.MapleClient;
import client.MapleDisease;
import client.inventory.MapleInventoryType;
import config.Game.CashPQ;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.maps.MapleMap;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */

public class UseItemHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (!c.getPlayer().isAlive()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		slea.readInt(); // i have no idea :) (o.o)
		byte slot = (byte)slea.readShort();
		int itemId = slea.readInt(); //as if we cared... ;)
		IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot); 
		if (itemId == 2022178) {
			c.getPlayer().dispelDebuffs();
		}else if (itemId == 2050000) {
			c.getPlayer().dispelDebuff(MapleDisease.POISON);
		} else if (itemId == 2050001) {
			c.getPlayer().dispelDebuff(MapleDisease.DARKNESS);
		} else if (itemId == 2050002) {
			c.getPlayer().dispelDebuff(MapleDisease.WEAKEN);
		} else if (itemId == 2050003) {
			c.getPlayer().dispelDebuff(MapleDisease.SEAL);
			c.getPlayer().dispelDebuff(MapleDisease.CURSE);
		} else if (itemId == 2050004) {
			c.getPlayer().dispelDebuffs();
		}
		if (toUse != null && toUse.getQuantity() > 0) {
			if (toUse.getItemId() != itemId) {
				return;
			}
		    switch (toUse.getItemId()) {
					case 2030019:
						   MapleMap target = c.getChannelServer().getMapFactory().getMap(120000000);
						   c.getPlayer().changeMap(target, c.getChannelServer().getMapFactory().getMap(120000000).getPortal(0));
						   MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
						   c.getSession().write(MaplePacketCreator.enableActions());
						   return;
					case 2030001:
						   MapleMap target1 = c.getChannelServer().getMapFactory().getMap(104000000);
						   c.getPlayer().changeMap(target1, c.getChannelServer().getMapFactory().getMap(104000000).getPortal(0));
						   MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
						   c.getSession().write(MaplePacketCreator.enableActions());
						   return;
					case 2030002:
						   MapleMap target2 = c.getChannelServer().getMapFactory().getMap(101000000);
						   c.getPlayer().changeMap(target2, c.getChannelServer().getMapFactory().getMap(101000000).getPortal(0));
						   MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
						   c.getSession().write(MaplePacketCreator.enableActions());
						   return;
					case 2030003:
						   MapleMap target3 = c.getChannelServer().getMapFactory().getMap(102000000);
						   c.getPlayer().changeMap(target3, c.getChannelServer().getMapFactory().getMap(102000000).getPortal(0));
						   MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
						   c.getSession().write(MaplePacketCreator.enableActions());
						   return;
					case 2030004:
						   MapleMap target4 = c.getChannelServer().getMapFactory().getMap(100000000);
						   c.getPlayer().changeMap(target4, c.getChannelServer().getMapFactory().getMap(100000000).getPortal(0));
						   MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
						   c.getSession().write(MaplePacketCreator.enableActions());
						   return;
					case 2030005:
						   MapleMap target5 = c.getChannelServer().getMapFactory().getMap(103000000);
						   c.getPlayer().changeMap(target5, c.getChannelServer().getMapFactory().getMap(103000000).getPortal(0));
						   MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
						   c.getSession().write(MaplePacketCreator.enableActions());
						   return;
					case 2030006:
						   MapleMap target6 = c.getChannelServer().getMapFactory().getMap(105040300);
						   c.getPlayer().changeMap(target6, c.getChannelServer().getMapFactory().getMap(105040300).getPortal(0));
						   MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
						   c.getSession().write(MaplePacketCreator.enableActions());
						   return;
					case 2030007:
						   MapleMap target7 = c.getChannelServer().getMapFactory().getMap(211041500);
						   c.getPlayer().changeMap(target7, c.getChannelServer().getMapFactory().getMap(211041500).getPortal(0));
						   MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
						   c.getSession().write(MaplePacketCreator.enableActions());
						   return;
		    }
            if (ii.isTownScroll(itemId)) {
                if (ii.getItemEffect(toUse.getItemId()).applyTo(c.getPlayer())) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                }
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
			ii.getItemEffect(toUse.getItemId()).applyTo(c.getPlayer());
			c.getPlayer().checkBerserk();
		} else {
			return;
		}
	}
        
          private static void remove(MapleClient c, int itemId) {
        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
    }
}
