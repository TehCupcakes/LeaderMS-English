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
			cm.sendSimple ("Ola, eu sou o taxista Dolphin,\r\nO que voce quer fazer hoje?\r\n#L0#Aprenda a montar um porco#l\r\n#L1#Ir para Herb Town#l\r\n#L2#Comecar Aquarium Party Quest#l\r\n#L3#Conte-me sobre a outra dimensao.#l");
		} else if (status == 1) {
			if (selection == 0) {
				if(cm.getLevel() >= 70) {
					cm.teachSkill(1004, 1, 0);
					cm.sendOk("Voce esta pronto para entrar na Pig.");
				} else {
					cm.sendOk("Voce e muito fraco. Por favor, volte quando voce crescer mais.");
				}
				cm.dispose();
			} else if (selection == 1) {
				cm.sendNext ("Tudo bem, vejo voce na próxima vez.");
			} else if (selection == 2) {
					if (cm.getParty() == null) {
						cm.sendOk("Por favor, fale comigo de novo depois de ter grupo formado.");
						cm.dispose();
						return;
					}
					if (!cm.isLeader()) { 
						cm.sendOk("Por favor, peca o lider do grupo para falar comigo!");
						cm.dispose();
				       } if (checkLevelsAndMap(minLevel, maxLevel) == 2) {  
	                                        cm.sendOk("Acho que nem todos os membros do seu grupo estao presentes.");
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
					cm.sendOk("Seu grupo nao pode participar da missao porque nao possui 6 membros. Por favor, reuna 6 pessoas no seu grupo.");
					cm.dispose();
				}
			 }
			 cm.dispose();
			} else if (selection == 3) {
				cm.sendNext("Ultimamente no aquario, uma outra dimensao tem aparecido do nada, e que a maior ameaca que representa para nos e que #bSuper Pianus#k, o chefe de seu mundo esta lentamente se fundindo em nosso mundo. Precisamos de pessoas corajosas para combater #bSuper Pianus .#k Os item dropados do Super Pianus tambem sao de outra dimensao, o que significa que eles sao incrivelmente fortes.");
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
					
