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

 * @author TheRamon

    This is the item sort handler, which handles inventory sorting (red button next to X button)
 */

package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import client.inventory.MapleInventoryType;
import client.inventory.MapleInventory;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ItemSortHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
	/*
		40 00 4D 2B A1 00 05 cash
		40 00 F7 17 A1 00 04 etc
		40 00 6F 36 A1 00 03 setup
		40 00 1F 4A A1 00 02 use
		40 00 EB 50 A1 00 02 use
		40 00 8D 54 A1 00 02 use
		40 00 AD 61 A1 00 01 eqp

		MapleStory just sorts items at the top
	*/

	slea.readInt(); //Timestamp or something?
	byte mode = slea.readByte();

	boolean sorted = false;
	MapleInventoryType pInvType = MapleInventoryType.getByType(mode);
	MapleInventory pInv = c.getPlayer().getInventory(pInvType); //Mode should correspond with MapleInventoryType
	
	while(!sorted) {
		byte freeSlot = pInv.getNextFreeSlot();
		if (freeSlot != -1) {
			byte itemSlot = -1;
			for (byte i = (byte)(freeSlot+1); i <= 100; i++) {
				if (pInv.getItem(i) != null) {
					itemSlot = i;
					break;
				}
			}
			if (itemSlot <= 100 && itemSlot > 0) {
//				pInv.move(i, freeSlot, (short)9999);
				MapleInventoryManipulator.move(c, pInvType, itemSlot, freeSlot);
//				c.getSession().write(MaplePacketCreator.enableActions());
			} else {
				sorted = true;
				}
			}
		}
		c.getSession().write(MaplePacketCreator.enableActions());
	}
}