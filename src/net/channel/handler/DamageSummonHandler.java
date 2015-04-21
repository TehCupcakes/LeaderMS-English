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

import java.util.Iterator;

import client.MapleBuffStat;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.MaplePacketHandler;
import server.maps.MapleSummon;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/*
 * GMS v.62 puppet fixed by LaiLaiNoob.
 * ValhallaDev.com
 */
public class DamageSummonHandler extends AbstractMaplePacketHandler implements MaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt(); //Bugged? might not be skillid.
        int unkByte = slea.readByte();
        int damage = slea.readInt();
        int monsterIdFrom = slea.readInt();
        slea.readByte(); // stance
        Iterator<MapleSummon> iter = c.getPlayer().getSummons().values().iterator();
        while (iter.hasNext()) {
            MapleSummon summon = iter.next();
            if (summon.isPuppet() && summon.getOwner() == c.getPlayer()) { //We can only have one puppet(AFAIK O.O) so this check is safe.
                summon.addHP(-damage);
                if (summon.getHP() <= 0) {
                    c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
                }
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.damageSummon(c.getPlayer().getId(), summon.getSkill(), damage, unkByte, monsterIdFrom), summon.getPosition());
                break;
            }
        }
    }
}