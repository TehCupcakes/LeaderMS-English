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

/* Floating warrior dude whose name I forget
	Warrior 4th job advancement
	Somewhere in Leafre (240010601)
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
			if (!(cm.getJob().equals(MapleJob.CRUSADER) ||
				cm.getJob().equals(MapleJob.WHITEKNIGHT) ||
				cm.getJob().equals(MapleJob.DRAGONKNIGHT))) {
				cm.sendOk("I only train the strongest warriors.");
				cm.dispose();
				return;
			}
			if ((cm.getJob().equals(MapleJob.CRUSADER) ||
				cm.getJob().equals(MapleJob.WHITEKNIGHT) ||
				cm.getJob().equals(MapleJob.DRAGONKNIGHT)) &&
				cm.getLevel() >= 120 && 
				cm.getPlayer().getRemainingSp() <= (cm.getLevel() - 120) * 3) {
				cm.sendNext("You are a strong one.");
			} else {
				cm.sendOk("Your time has yet to come...");
				cm.dispose();
			}
		} else if (status == 1) {
				if (cm.getJob().equals(MapleJob.CRUSADER)) {
					cm.changeJob(MapleJob.HERO);
					cm.getPlayer().gainAp(5);
					cm.gainItem(2280003,1);
					cm.teachSkill(1121001, 0, 10);
					cm.teachSkill(1120004, 0, 10);
					cm.teachSkill(1121008, 0, 10);
					cm.sendOk("You are now a #bHero#k.");
					cm.dispose();
				} else if (cm.getJob().equals(MapleJob.DRAGONKNIGHT)) {
					cm.changeJob(MapleJob.DARKKNIGHT);
					cm.getPlayer().gainAp(5);
					cm.gainItem(2280003,1);
					cm.teachSkill(1320005, 0, 10);
					cm.teachSkill(1321001, 0, 10);
					cm.teachSkill(1321007, 0, 10);
					cm.sendOk("You are now a #bDark Knight#k.");
					cm.dispose();
				} else if (cm.getJob().equals(MapleJob.WHITEKNIGHT)) {
					cm.changeJob(MapleJob.PALADIN);
					cm.getPlayer().gainAp(5);
					cm.gainItem(2280003,1);
					cm.teachSkill(1220005, 0, 10);
					cm.teachSkill(1221001, 0, 10);
					cm.teachSkill(1221009, 0, 10);
					cm.sendOk("You are now a #bPaladin#k.");
					cm.dispose();
			} else {
				cm.sendAcceptDecline("But I can make you even stronger. Although you will have to prove not only your strength but your knowledge. Are you ready for the challenge?");
			}
		} 
	}
}	