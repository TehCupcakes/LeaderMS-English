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
import java.util.logging.Level;
import java.util.logging.Logger;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import client.SkillFactory;
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class SkillEffectHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
                if (c.getPlayer().isHidden()) {
                    return;
                }
                
		int skillId = slea.readInt();
		int level = slea.readByte();
		byte flags = slea.readByte();
		int speed = slea.readByte();
		int currentLevel = c.getPlayer().getSkillLevel(SkillFactory.getSkill(skillId)); 

		if(level == 0) { // using a skill when it's level 0
			c.getSession().close(); // Disconnect
			return;
		}

		 // Hurricane / Storm of Arrow, Pirate, Monster Magnet, Big bang
		 // To prevent possible map dc hack or map crash.
		if(currentLevel == level) { // Checking the client skill level
			if(skillId == 3121004 || skillId == 5221004 || skillId == 1121001 || skillId == 1221001 || skillId == 1321001 || skillId == 2121001 || skillId == 2221001 || skillId == 2321001
					|| skillId == 5101004|| skillId == 5201002 || skillId == 2111002 || skillId == 4211001 || skillId == 3221001) {
				c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillEffect(c.getPlayer(), skillId, level, flags, speed), false);
			} else {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "[h4x] " + 
				c.getPlayer().getName() + " using unusable skill! ID: " + skillId);
				try {
					c.getPlayer().getClient().getChannelServer().getWorldInterface().broadcastGMMessage("", MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " is using an unusable skill, skillID: " + skillId).getBytes());
				} catch (RemoteException ex) {
					c.getPlayer().getClient().getChannelServer().reconnectWorld();
				}
				c.getSession().close(); // Disconnect
				return;
			}
		}
	}
}
