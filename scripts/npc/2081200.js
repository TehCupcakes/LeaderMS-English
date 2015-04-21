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

/* Floating mage lady whose name I forget
	Magician 4th job advancement
	Somewhere in Leafre (hell if I know the mapID)
*/

importPackage(Packages.client);

var status = 0;
var job;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 1) {
			cm.sendOk("Make up your mind and visit me again.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if (!(cm.getJob().equals(MapleJob.FP_MAGE) ||
				cm.getJob().equals(MapleJob.IL_MAGE) ||
				cm.getJob().equals(MapleJob.PRIEST))) {
				cm.sendOk("I only train the brightest magicians.");
				cm.dispose();
				return;
			}
			if ((cm.getJob().equals(MapleJob.FP_MAGE) ||
				cm.getJob().equals(MapleJob.IL_MAGE) ||
				cm.getJob().equals(MapleJob.PRIEST)) &&
				cm.getLevel() >= 120 && 
				cm.getPlayer().getRemainingSp() <= (cm.getLevel() - 120) * 3) {
				cm.sendNext("You are a strong one.");
			} else {
				cm.sendOk("Your time has yet to come...");
				cm.dispose();
			}
		} else if (status == 1) {
				if (cm.getJob().equals(MapleJob.FP_MAGE)) {
					cm.changeJob(MapleJob.FP_ARCHMAGE);
					cm.getPlayer().gainAp(5);
					cm.gainItem(2280003,1);
					cm.teachSkill(2121001,0,10);
					cm.teachSkill(2121006,0,10);
					cm.teachSkill(2121002,0,10);
					cm.sendOk("You are now a #bFire/Poison Arch Mage#k.");
					cm.dispose();
				} else if (cm.getJob().equals(MapleJob.IL_MAGE)) {
					cm.changeJob(MapleJob.IL_ARCHMAGE);
					cm.getPlayer().gainAp(5);
					cm.gainItem(2280003,1);
					cm.teachSkill(2221001,0,10);
					cm.teachSkill(2221006,0,10);
					cm.teachSkill(2221002,0,10);
					cm.sendOk("You are now an #bIce/Lightning Arch Mage#k.");
					cm.dispose();
				} else if (cm.getJob().equals(MapleJob.PRIEST)) {
					cm.changeJob(MapleJob.BISHOP);
					cm.getPlayer().gainAp(5);
					cm.gainItem(2280003,1);
					cm.teachSkill(2321001,0,10);
					cm.teachSkill(2321005,0,10);
					cm.teachSkill(2321002,0,10);
					cm.sendOk("You are now a #bBishop#k.");
					cm.dispose();
			} else {
				cm.sendAcceptDecline("But I can make you even stronger. Although you will have to prove not only your strength but your knowledge. Are you ready for the challenge?");
			}
		} 
	}
}	