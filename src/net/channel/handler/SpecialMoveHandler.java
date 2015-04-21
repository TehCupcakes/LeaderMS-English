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

import java.awt.Point;

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.messages.ServernoticeMapleClientMessageCallback;
import net.AbstractMaplePacketHandler;
import server.MapleStatEffect;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class SpecialMoveHandler extends AbstractMaplePacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpecialMoveHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter player = c.getPlayer();
		// [53 00] [12 62] [AA 01] [6B 6A 23 00] [1E] [BA 00] [97 00] 00
		slea.readShort();
		slea.readShort();
		int skillId = slea.readInt();
		// seems to be skilllevel for movement skills and -32748 for buffs
		int _skillLevel = slea.readByte();
		ISkill skill = SkillFactory.getSkill(skillId);
		int skillLevel = player.getSkillLevel(skill);
		MapleStatEffect effect = skill.getEffect(skillLevel);
		
		//System.out.println(player.getName() + " has used a skill: " + skillId);

		//cooldowns
		if (skillId != 0 && effect.getCooldown() > 0
				&& skillId != 5221006) {
			if (player.skillisCooling(skillId)) return;
			player.checkCoolDown(skill);
		}

		//monster magnet
		switch (skillId) {
			case 1121001:
			case 1221001:
			case 1321001:
				int num = slea.readInt();
				int mobId;
				byte success;
				for (int i = 0; i < num; i++) {
					mobId = slea.readInt();
					success = slea.readByte();
					player.getMap().broadcastMessage(player, MaplePacketCreator.showMagnet(mobId, success), false);
					MapleMonster monster = player.getMap().getMonsterByOid(mobId);
					if (monster != null) {
						monster.switchController(player, monster.isControllerHasAggro());
					}
				}
				byte direction = slea.readByte();
				player.getMap().broadcastMessage(player, MaplePacketCreator.showBuffeffect(player.getId(), skillId, 1, direction), false);
				c.getSession().write(MaplePacketCreator.enableActions());
				break;
		}

		if (skillLevel == 0 || skillLevel != _skillLevel) {
			log.warn(player.getName() + " is using a move skill he doesn't have.. ID: " + skill.getId());
			return;
		} else {
			if (player.isAlive()) {
				if (skill.getId() != 2311002 || player.canDoor()) {
					effect.applyTo(player, (slea.available() == 5) ? new Point(slea.readShort(), slea.readShort()) : null);
				} else {
					new ServernoticeMapleClientMessageCallback(5, c).dropMessage("Please wait 5 seconds before casting Mystic Door again");
					c.getSession().write(MaplePacketCreator.enableActions());
				}
			} else c.getSession().write(MaplePacketCreator.enableActions());
		}
	}
}