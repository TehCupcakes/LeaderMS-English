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

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class DistributeSPHandler extends AbstractMaplePacketHandler {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DistributeSPHandler.class);

	private class SP {

		private int id;
		private MapleClient c;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public MapleClient getClient() {
			return c;
		}

		public void setClient(MapleClient c) {
			this.c = c;
		}
	}

	private void addSP(SP sp) {
		int skillid = sp.getId();
		MapleCharacter player = sp.getClient().getPlayer();
		int remainingSp = player.getRemainingSp();
		boolean isBegginnerSkill = false;

		if (skillid == 8 || skillid == 1003 || skillid == 1004 || skillid == 1005) { // Follow the lead, Legendary Spirit, Monster Rider and Echo of Hero if either unavailable or only thru quest
			log.warn(player.getName() + " is trying to add sp where it doesn't belong..");
			return;
		}

		if (skillid == 1000 || skillid == 1001 || skillid == 1002) { // boo beginner skill
			int snailsLevel = player.getSkillLevel(SkillFactory.getSkill(1000));
			int recoveryLevel = player.getSkillLevel(SkillFactory.getSkill(1001));
			int nimbleFeetLevel = player.getSkillLevel(SkillFactory.getSkill(1002));
			remainingSp = Math.min((player.getLevel() - 1), 6) - snailsLevel - recoveryLevel - nimbleFeetLevel;
			isBegginnerSkill = true;
		}
		ISkill skill = SkillFactory.getSkill(skillid);
		int maxlevel = skill.isFourthJob() ? player.getMasterLevel(skill) : skill.getMaxLevel();
		int curLevel = player.getSkillLevel(skill);
		if ((remainingSp > 0 && curLevel + 1 <= maxlevel) && skill.canBeLearnedBy(player.getJob())) {
			if (!isBegginnerSkill) {
				player.setRemainingSp(player.getRemainingSp() - 1);
			}
			player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
			player.changeSkillLevel(skill, curLevel + 1, player.getMasterLevel(skill));
		} else if (!skill.canBeLearnedBy(player.getJob())) {
			log.warn(sp.getClient() + "Trying to learn a skill for a different job (" + player.getJob().name() + ":" + skillid + ")");
                        sp.getClient().getSession().close();
			//sp.getClient().disconnect();
			return;
		}
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt();
		SP sp = new SP();
		sp.setClient(c);
		sp.setId(slea.readInt());
		addSP(sp);
	}
}
