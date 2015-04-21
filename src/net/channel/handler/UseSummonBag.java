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

import java.util.List;
import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleItemInformationProvider.SummonEntry;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author AngelSL
 */

public class UseSummonBag extends AbstractMaplePacketHandler {

    public UseSummonBag() {
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0) {
            if (toUse.getItemId() != itemId) {
      //          c.getPlayer().ban("Trying to use a summonbag not in item inventory.");
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            List<SummonEntry> toSpawn = ii.getSummonMobs(itemId);
           for (int z = 0; z < toSpawn.size(); z++) {
                    SummonEntry se = toSpawn.get(z);
                if ((int) Math.ceil(Math.random() * 100) <= se.getChance()) {
                    MapleMonster mob = MapleLifeFactory.getMonster(se.getMobId());
                    c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
                }
            }
        } else {
            ///c.getPlayer().ban("Trying to use a summonbag not in item inventory.");
            return;
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}