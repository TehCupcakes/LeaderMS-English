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

import java.util.Map;

import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.ISkill;
import client.SkillFactory;
	
public class SkillBookHandler extends AbstractMaplePacketHandler {	

	public SkillBookHandler() {
	}

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (!c.getPlayer().isAlive()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		slea.readInt();
		byte slot = (byte) slea.readShort();
		int itemId = slea.readInt();
		MapleCharacter player = c.getPlayer();
		IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
			
		if (toUse != null && toUse.getQuantity() == 1) {
			if (toUse.getItemId() != itemId) {
				return;
			}
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			Map<String, Integer> skilldata = ii.getSkillStats(toUse.getItemId(), c.getPlayer().getJob().getId());
			
			boolean canuse = false;
			boolean success = false;
			int skill = 0;
			int maxlevel = 0;
			if (skilldata == null) { // Hacking or used an unknown item
				return;				
			}
			if (skilldata.get("skillid") == 0) { // Wrong Job
				canuse = false;
			} else if (player.getMasterLevel(SkillFactory.getSkill(skilldata.get("skillid"))) >= skilldata.get("reqSkillLevel") || skilldata.get("reqSkillLevel") == 0) {
				canuse = true;
				int random = (int) Math.floor(Math.random() * 100) + 1;				
				if (random <= skilldata.get("success") && skilldata.get("success") != 0) {
					success = true;
					ISkill skill2 = SkillFactory.getSkill(skilldata.get("skillid"));
					int curlevel = player.getSkillLevel(skill2);
					player.changeSkillLevel(skill2, curlevel, skilldata.get("masterLevel"));
					MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
				} else {
					success = false;
					MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
				}				
			} else { // Failed to meet skill requirements
				canuse = false;					
			}
			player.getClient().getSession().write(MaplePacketCreator.skillBookSuccess(player, skill, maxlevel, canuse, success));
		} else {
			return;
		}
	}
}