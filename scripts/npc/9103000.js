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

/* Pietri
 * 
 * Ludi Maze PQ Room 16
 * 
 * @author Jvlaple
*/

var status = 0;
var copns;

importPackage(Packages.client);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if (!cm.isLeader()) { // not party leader
					cm.sendNext("Please tell the #bParty Leader#k to talk to me.");
					cm.dispose();
			} else  { //party leader
				copns = cm.getPlayer().countItem(4001106);
				if (copns < 30) { cm.sendNext("You must have 30 or more coupons to complete the quest."); cm.dispose(); }
				else {
					cm.sendNext("Wow, you picked up " + copns + " coupons! Congratulations! You will be taken to another map to get your #bEXP#k and Rolly will give you your rewards!");
				}
			}
		} else if (status == 1) {
			var party = cm.getChar().getEventInstance().getPlayers();
			var myParty = cm.getParty().getMembers();
			eim = cm.getChar().getEventInstance();
			cm.givePartyExp(5 * copns, party);
			cm.removeAll(4001106);
			for (var outt = 0; outt<party.size(); outt++)
				{//Kick everyone out =D
					var exitMapz = eim.getMapInstance(809050016);
					party.get(outt).changeMap(exitMapz, exitMapz.getPortal(0));
					eim.unregisterPlayer(party.get(outt));
				}
			//cm.warp(809050016, 0);
			cm.dispose();
		}
	}
}