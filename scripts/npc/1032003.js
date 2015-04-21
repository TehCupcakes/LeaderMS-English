/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
					   Matthias Butz <matze@odinms.de>
					   Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Shane - Ellinia (101000000)
-- By ---------------------------------------------------------------------------------------------
	Unknown
-- Version Info -----------------------------------------------------------------------------------
	1.1 - Statement fix [Information]
	1.0 - First Version by Unknown
---------------------------------------------------------------------------------------------------
**/

importPackage(Packages.client);

var status = 0;
var check = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.sendOk("Alright, see you next time.");
			cm.dispose();
			return;
		}
		if (mode == 1) {
			status++;
		}
		else {
			status--;
		}
		if (status == 0) {
			if (cm.getLevel() < 25) {
				cm.sendOk("You must be a higher level to enter the Forest of Patience.");
				cm.dispose();
				check = 1;
			}
			else {
				cm.sendYesNo("Hi, I'm Shane. I can let you into the Forest of Patience for a small fee. Would you like to enter for #b5000#k mesos?");
			}
		} else if (status == 1) {
			if (check != 1) {
				if (cm.getPlayer().getMeso() < 5000) {
					cm.sendOk("Sorry, but it looks like you don't have enough mesos!")
					cm.dispose();
				}
				else { 
					if (cm.getQuestStatus(2050).equals(MapleQuestStatus.Status.STARTED)) {
						cm.warp(101000100, 0);
					}
					else if (cm.getQuestStatus(2051).equals(MapleQuestStatus.Status.STARTED)) {
						cm.warp(101000102, 0);
					}
					else if (cm.getLevel() >= 25 && cm.getLevel() < 50) {
						cm.warp(101000100, 0);
					} 
					else if (cm.getLevel() >= 50) {
						cm.warp(101000102, 0);
					}
					cm.gainMeso(-5000);
					cm.dispose();
				}
			}
		}
	}
}	


///** 
// * 
// * @author Soulfist 
// */ 
//rewards = [["Trofeu", 14000038, 15]]; 
//
//function start() { 
//    var talk = "Ola #e#h ##n, eu me chamo Shane. Estou aqui para poder te ajudar a trocar seus JQ Points. Cada item custa 15 JQ Points.\r\nOque deseja trocar?"; 
//    for(var i = 0; i < rewards.length; i++) 
//        talk += "\r\n#L"+i+"#"+rewards[i][0]+"#l"; 
//    cm.sendSimple(talk); 
//} 
//
//function action(m,t,s){ 
//    cm.dispose(); 
//    if(m > 0){ 
//        if (cm.getPlayer().getJQPoints() >= rewards[s][2]){ 
//            cm.sendOk("Aqui voce ja pode desfrutar do seu premio !"); 
//            cm.gainItem(rewards[s][1]); 
//            cm.getPlayer().addJQPoints(-rewards[s][2]); 
//        } else { 
//            cm.sendOk("Voce nao tem numero suficiente de pontos em JQ!"); 
//            cm.dispose(); 
//        } 
//    } 
//}  