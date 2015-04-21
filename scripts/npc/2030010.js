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

/* 
	Amon
*/

importPackage(java.lang);
importPackage(Packages.server);


var status = 0;
var map = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.sendOk("Talk to me when you're ready.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (cm.getPlayer().getMapId() == 280030000) {
			if (status == 0) {
				cm.sendSimple("Welcome to #rZakum's altar#k. I can not help with the fight, but can I do anything else for you?\r\n#L2#Take me to #bEl Nath#k!#l\r\n\#L3#Take me to #bThe Door To Zakum#k!#l");
			} else if (status == 1) {
				if (selection == 1) {
					if(cm.getMeso() >= 10000000) {
						cm.gainMeso(-10000000);
						cm.gainItem(4001017, 1);
						cm.sendOk("Enjoy!");
					} else {
						cm.sendOk("Sorry, you do not have enough mesos.");
					}
					cm.dispose();
				} else if (selection == 2) {
					if (cm.checkSquadLeader(MapleSquadType.ZAKUM)) {
						cm.warpSquadMembers(MapleSquadType.ZAKUM, 211042300);
						cm.dispose();
					} else {
						cm.removeSquadMember(MapleSquadType.ZAKUM, cm.getPlayer(), false);
						cm.warp(211000000, 0);
						cm.dispose();
					}
				} else if (selection == 3) {
					if (cm.checkSquadLeader(MapleSquadType.ZAKUM)) {
						cm.warpSquadMembers(MapleSquadType.ZAKUM, 211042300);
						cm.dispose();
					} else {
						cm.removeSquadMember(MapleSquadType.ZAKUM, cm.getPlayer(), false);
						cm.warp(211042300, 0);
						cm.dispose();
					}
				}
			}
		} else if (cm.getPlayer().getMapId() == 280020000 || cm.getPlayer().getMapId() == 280020001) {
			if (status == 0) {
				cm.sendYesNo("Would you like to quit this quest? You will have to start from scratch next time...");
			} else if (status == 1) {
				cm.warp(211042300, 0);
				cm.dispose();
			}
		} else {
			if (status == 0) {
				cm.dispose();
			}
		}
	}
}
