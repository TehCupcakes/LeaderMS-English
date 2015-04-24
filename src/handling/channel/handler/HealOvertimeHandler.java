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

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class HealOvertimeHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        slea.readShort();
        slea.readByte();
        int healHP = slea.readShort();
        if (healHP != 0) {
            //  if (healHP > 140) {
            //    c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.REGEN_HIGH_HP, String.valueOf(healHP));
            //   }
            //  c.getPlayer().getCheatTracker().checkHPRegen();
            //   if (c.getPlayer().getCurrentMaxHp() == c.getPlayer().getHp()) {
            //    c.getPlayer().getCheatTracker().resetHPRegen();
            //}
            c.getPlayer().addHP(healHP);
        }
        int healMP = slea.readShort();
        if (healMP != 0) {
            //    if (healMP > Math.floor(((float) ((float) c.getPlayer().getSkillLevel(SkillFactory.getSkill(2000000)) / 10) * c.getPlayer().getLevel()) + 3)) {
            //       c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.REGEN_HIGH_MP, String.valueOf(healMP));
            //    }
            // c.getPlayer().getCheatTracker().checkMPRegen();
            c.getPlayer().addMP(healMP);
        // if (c.getPlayer().getCurrentMaxMp() == c.getPlayer().getMp()) {
        //       c.getPlayer().getCheatTracker().resetMPRegen();
        //   }
        }
    }
}
