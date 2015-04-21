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

/* Floating thief guy whose name I forget
	Thief 4th job advancement
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
			if (cm.getJob().equals(MapleJob.NIGHTLORD)) {
				var p = cm.c.getPlayer();
				var skillFact = new SkillFactory(); 
				var skillDummy = skillFact.getSkill(4120002);
				var skillLevel = p.getSkillLevel(skillDummy);
				var skillMaster = p.getMasterLevel(skillDummy);
				if (skillMaster != 10) {
					cm.teachSkill(4120002,skillLevel,10);
					cm.sendOk("You can now add points to Shadow Shifter.");
					cm.dispose();
					return;
				} else {
					cm.sendOk("I have nothing more to say to you.");
					cm.dispose();
					return;
				}
			} else if (cm.getJob().equals(MapleJob.SHADOWER)) {
				var p = cm.c.getPlayer();
				var skillFact = new SkillFactory(); 
				var skillDummy = skillFact.getSkill(4220002);
				var skillLevel = p.getSkillLevel(skillDummy);
				var skillMaster = p.getMasterLevel(skillDummy);
				if (skillMaster != 10) {
					cm.teachSkill(4220002,skillLevel,10);
					cm.sendOk("You can now add points to Shadow Shifter.");
					cm.dispose();
					return;
				} else {
					cm.sendOk("I have nothing more to say to you.");
					cm.dispose();
					return;
				}
			} else if (!(cm.getJob().equals(MapleJob.HERMIT) ||
				cm.getJob().equals(MapleJob.CHIEFBANDIT))) {
				cm.sendOk("I only train the most dangerous thieves.");
				cm.dispose();
				return;
			} else if (cm.getLevel() >= 120 && 
				cm.getPlayer().getRemainingSp() <= (cm.getLevel() - 120) * 0) {
				cm.sendNext("You are a strong one.");
			} else {
				cm.sendOk("Your time has yet to come...");
				cm.dispose();
				return;
			}
		} else if (status == 1) {
				if (cm.getJob().equals(MapleJob.HERMIT)) {
					cm.changeJob(MapleJob.NIGHTLORD);
					cm.getPlayer().gainAp(5);
					cm.gainItem(2280003,1);
					cm.teachSkill(4120002,0,10); //shadow shift
					cm.teachSkill(4121006,0,10); //shadow star
					cm.teachSkill(4120005,0,10); //venom star
					cm.sendOk("You are now a #bNight Lord#k.");
					cm.dispose();
				} else if (cm.getJob().equals(MapleJob.CHIEFBANDIT)) {
					cm.changeJob(MapleJob.SHADOWER);
					cm.getPlayer().gainAp(5);
					cm.gainItem(2280003,1);
					cm.teachSkill(4220002,0,10); //shadow shift
					cm.teachSkill(4221007,0,10); //b-step
					cm.teachSkill(4220005,0,10); //venom stab
					cm.sendOk("You are now a #bShadower#k.");
					cm.dispose();
				}  else {
				cm.sendAcceptDecline("But I can make you even stronger. Although you will have to prove not only your strength but your knowledge. Are you ready for the challenge?");
			}
		} 
	}
}	