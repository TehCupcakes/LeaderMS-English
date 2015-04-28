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

/* Dolphin in Aquaroad - Monster Riding Teacher and Aquarium PQ
*/

var status = 0;
var minLevel = 180;
var maxLevel = 200;
var minPlayers = 5;
var maxPlayers = 6;

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
			cm.sendSimple ("Hello, I am the Dolphin taxi!\r\nWhat do you want to do today?\r\n#L0#Learn to ride a pig#l\r\n#L1#Go to Herb Town#l\r\n#L2#Begin Aquarium Party Quest#l\r\n#L3#Tell me about the other dimension#l");
		} else if (status == 1) {
			if (selection == 0) {
				if(cm.getLevel() >= 70) {
					cm.teachSkill(1004, 1, 0);
					cm.sendOk("You are ready to ride a pig");
				} else {
					cm.sendOk("You are very weak. Please come back when you have grown more.");
				}
				cm.dispose();
			} else if (selection == 1) {
                            cm.sendNext ("All right, see you next time.");
			} else if (selection == 2) {
					if (cm.getParty() == null) {
						cm.sendOk("Please speak to me again after forming a party.");
						cm.dispose();
						return;
					}
					if (!cm.isLeader()) { 
						cm.sendOk("Please ask the party leader to talk to me.");
						cm.dispose();
				       } if (checkLevelsAndMap(minLevel, maxLevel) == 2) {  
	                                        cm.sendOk("Not all of the party members are present or in the appropriate level range.");
                                                cm.dispose();
                                                return;
                                             }  else {
						var party = cm.getParty().getMembers();
						var mapId = cm.getChar().getMapId();
						var next = true;
						var levelValid = 0;
						var inMap = 0;
						if (party.size() < minPlayers || party.size() > maxPlayers) 
							next = false;
						else {
							for (var i = 0; i < party.size() && next; i++) {
								if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
									levelValid += 1;
								if (party.get(i).getMapid() == mapId)
									inMap += 1;
							}
							if (levelValid < minPlayers || inMap < minPlayers)
								next = false;
						}
						if (next) {
							var em = cm.getEventManager("AquariumPQ");
							if (em == null) {
								cm.sendOk("unavailable");
								cm.dispose();
							} else {
								var eim = em.startInstance(cm.getParty(),cm.getChar().getMap());
								cm.dispose();
							}
							cm.dispose();
						}else {
					cm.sendOk("Your party cannot participate because it does not have 6 members. Please gather 6 people in your party.");
					cm.dispose();
				}
			 }
			 cm.dispose();
			} else if (selection == 3) {
				cm.sendNext("Recently, a portal to another dimension has appeared in the Aquarium. The greatest threat posed to us is #bSuper Pianus#k, whose world is slowly melting into our world. We need brave people to fight #bSuper Pianus#k. The items dropped by Super Pianus are also from another dimension, which means they are incredibly strong.");
				cm.dispose();
			}
		} else if (status == 2) {
			cm.warp(251000100, 0);
			cm.dispose();
		}
	}
}	

function checkLevelsAndMap(lowestlevel, highestlevel) {
    var party = cm.getParty().getMembers();
    var mapId = cm.getMapId();
    var valid = 0;
    var inMap = 0;

    var it = party.iterator();
    while (it.hasNext()) {
        var cPlayer = it.next();
        if (!(cPlayer.getLevel() >= lowestlevel && cPlayer.getLevel() <= highestlevel) && cPlayer.getJobId() != 900) {
            valid = 1;
        }
        if (cPlayer.getMapid() != mapId) {
            valid = 2;
        }
    }
    return valid;
}
					
