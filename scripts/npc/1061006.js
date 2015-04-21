///*
//	This file is part of the OdinMS Maple Story Server
//    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
//					   Matthias Butz <matze@odinms.de>
//					   Jan Christian Meyer <vimes@odinms.de>
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Affero General Public License as
//    published by the Free Software Foundation version 3 as published by
//    the Free Software Foundation. You may not use, modify or distribute
//    this program under any other version of the GNU Affero General Public
//    License.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Affero General Public License for more details.
//
//    You should have received a copy of the GNU Affero General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
//*/
//
///**
//-- Odin JavaScript --------------------------------------------------------------------------------
//	Mysterious Statue - Sleepywood (105040300)
//-- By ---------------------------------------------------------------------------------------------
//	Unknown
//-- Version Info -----------------------------------------------------------------------------------
//	1.1 - Statement fix [Information]
//	1.0 - First Version by Unknown
//---------------------------------------------------------------------------------------------------
//**/
//
//importPackage(Packages.client);
//
//var status = 0;
//var check = 0;
//
//function start() {
//	status = -1;
//	action(1, 0, 0);
//}
//
//function action(mode, type, selection) {
//	if (mode == -1) {
//		cm.dispose();
//	} else {
//		if (mode == 0) {
//			cm.sendOk("Alright, see you next time.");
//			cm.dispose();
//			return;
//		}
//		if (mode == 1) {
//			status++;
//		}
//		else {
//			status--;
//		}
//		if (status == 0) {
//			if (cm.getLevel() < 15) {
//				cm.sendOk("You must be a higher level to enter the mysterious place.");
//				cm.dispose();
//				check = 1;
//			}
//			else {
//				cm.sendYesNo("Once I lay my hand on the statue, a strange light covers me and it feels like I am being sucked into somewhere else. Is it okay to be moved to somewhere else randomly just like that?");
//			}
//		} else if (status == 1) {
//			if (check != 1) {
//				if (cm.getQuestStatus(2052).equals(MapleQuestStatus.Status.STARTED)) {
//					cm.warp(105040310, 0);
//				}
//				else if (cm.getQuestStatus(2053).equals(MapleQuestStatus.Status.STARTED)) {
//					cm.warp(105040312, 0);
//				}
//				else if (cm.getQuestStatus(2054).equals(MapleQuestStatus.Status.STARTED)) {
//					cm.warp(105040314, 0);
//				}
//				else if (cm.getLevel() >= 15 && cm.getLevel() < 30) {
//					cm.warp(105040310, 0);
//				}
//				else if (cm.getLevel() >= 30 && cm.getLevel() < 60) {
//					cm.warp(105040312, 0);
//				}
//				else if (cm.getLevel() >= 60) {
//					cm.warp(105040314, 0);
//				}
//				cm.dispose();
//			}
//		}
//	}
//}	
//
//

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    cm.sendOk("Muaaaaaaaaaaaaaaaaaaw!");
    cm.dispose();
}
