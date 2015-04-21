//Created by zambookii on 6/29/2014

var map = 980000000; //put your map id here.
var minLvl = 30;
var maxLvl = 51;
var minAmt = 1;
var maxAmt = 6;

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
    if (cm.getParty() == null) {
        cm.sendOk("#eCreate a party!#k");
        cm.dispose();
    } else if (!cm.isLeader()) {
        cm.sendOk("If you want to try Carnival PQ, please tell the #bleader of your party#k to talk to me.");
        cm.dispose();
    }else{
        var party = cm.getParty().getMembers();
        var inMap = cm.partyMembersInMap();
        var lvlOk = 0;
		var isInMap = 0;
        for (var i = 0; i < party.size(); i++) {
			if (party.get(i).getLevel() >= minLvl && party.get(i).getLevel() <= maxLvl) {
				lvlOk++;
			}
			if (party.get(i).getMapid() != 980000000) {
				//isInMap = false;
				isInMap++
			}
        }
	
        if (party >= 1) {
            cm.sendOk("You don't have enough people in your party. You need a party of #b"+minAmt+"#k - #r"+maxAmt+"#k members and they must be in the map with you. There are #b"+inMap+"#k members here.");
            cm.dispose();
        } else if (lvlOk != inMap) {
            cm.sendOk("Make sure everyone in your party is here and is in the right level range of 30-50!");
            cm.dispose();
		} else if (isInMap > 0) {
			cm.sendOk("All members are not in the map!");
			cm.dispose();
        }else{
            cm.sendCPQMapLists();	
        }
    }
} else if (status == 1) {
		if (cm.fieldTaken(selection)) {
				if (cm.fieldLobbied(selection)) {
					cm.challengeParty(selection);
					cm.dispose();
				} else {
					cm.sendOk("The room is taken.");
					cm.dispose();
					}
				} else {
				cm.cpqLobby(selection);
				cm.dispose();
				}
	}
}
}
//
////var status = 0;
//
//function start()
//{
//    status = -1;
//    action(1, 0, 0);
//}
//
//function action(mode, type, selection) {
//    if (mode == -1) {
//        cm.dispose();
//    } else {
//        if ((mode == 0 && status == 1) || (mode == 0 && status == 4)) {
//            cm.sendOk("Come back once you have thought about it some more.");
//            cm.dispose();
//            return;
//        }
//    }
//    if (mode == -1)
//    {
//        cm.dispose();
//    }
//    else
//    {
//        if (mode == 1)
//        {
//            status++;
//        }
//        else
//        {
//            status--;
//        }
//        if (status == 0) {
//            if (cm.getParty() == null) { // no party
//		  cm.sendOk("Voce precisa estar em algum grupo.");
//		 cm.dispose();
//	         return;
//	} if (!cm.isLeader()) { // not party leader
//		cm.sendOk("Voce nao e o lider do grupo.");
//		cm.dispose();
//                return;
//        } if (checkLevelsAndMap(30, 255) == 2) { // not party leader  
//	        cm.sendOk("Esta faltando alguem do grupo no mapa!");
//                cm.dispose();
//                return;
//                } else {
//                cm.sendCPQMapLists();
//            }
//
//        } else if (status == 1) {
//            if (cm.fieldTaken(selection)) {
//                if (cm.fieldLobbied(selection)) {
//                    cm.challengeParty(selection);
//                    cm.dispose();
//                } else {
//                    cm.sendOk("A sala ja esta ocupada.");
//                    cm.dispose();
//                }
//            } else {
//                cm.cpqLobby(selection);
//                cm.dispose();
//            }
//        }
//    }
//}
//
//function checkLevelsAndMap(lowestlevel, highestlevel) {
//    var party = cm.getParty().getMembers();
//    var mapId = cm.getMapId();
//    var valid = 0;
//    var inMap = 0;
//
//    var it = party.iterator();
//    while (it.hasNext()) {
//        var cPlayer = it.next();
//        if (!(cPlayer.getLevel() >= lowestlevel && cPlayer.getLevel() <= highestlevel) && cPlayer.getJobId() != 900) {
//            valid = 1;
//        }
//        if (cPlayer.getMapid() != mapId) {
//            valid = 2;
//        }
//    }
//    return valid;
//}
//status = 0; 
//lowestLevel = 0; // Lowest level allowed into BossPq 
//highestLevel = 200; // Highest level allowed into BossPq 
//potionCost = 10000; // cost of the number of potions 
//amountofPotions = 10; // Number of potions obtained for potion cost 
//minPartySize = 1; // Mininum number of players needed in party to join PQ 
//maxPartySize = 6; // Maximum number of players allowed in party to join the PQ 
//timeBefore = 15; // Time before first mob spawns 
//mapOfBossPQ = 980000000; // Map at which the bosses are spawned and killed. 
//
//
//function start() { 
//    if (cm.getPlayer().getMapId() == mapOfBossPQ) { 
//        if (cm.isLeader()) { 
//            cm.sendSimple("Ola, o que voce gostaria de fazer? \r\n#L0# Eu quero sair #l\r\n#L1# Eu quero sair junto com eu grupo"); 
//            status = 19; 
//        } else { 
//            cm.sendYesNo("Gostaria de deixar BossPQ?"); 
//            status = 29; 
//        } 
//    } else { 
//        if (cm.getPlayer().getLevel() >= lowestLevel) { 
//            if (cm.getPlayer().getLevel() <= highestLevel) { 
//                cm.sendSimple("Ola, o que voce gostaria de fazer? \r\n#L3# Trocar BossPoints \r\n#L0# Saber mais sobre BossPQ\r\n#L2# Entrar na BossPQ"); 
//            } else { 
//                cm.sendOk("Desculpe, voce nao pode estar acima do nível " + highestLevel + "."); 
//                cm.dispose(); 
//            } 
//        } else { 
//            cm.sendOk("Desculpe, mas voce precisa ter pelo menos nivel " + lowestLevel + "."); 
//            cm.dispose(); 
//        } 
//    } 
//} 
//
//function action(m,t,s) { 
//    if (m < 1) { 
//        cm.dispose(); 
//        return; 
//    } else { 
//        status++; 
//    } 
//    if (status == 1) { 
//        if (s == 0) { 
//            cm.sendNext("BossPQ e uma Party Quest, aonde voce e seu grupo tem de enfrentar varios chefes, um apos o outro. Estes chefes comecao bem fracos, e ao mata-los eles crescem muito mais fortes. Para cada chefe que voce matar, voce ganha BossPoints."); 
//            status = 9; 
//        } else if (s == 1) { 
//            if (cm.getMeso() >= potionCost) { 
//                cm.gainMeso(-potionCost); 
//                cm.gainItem(2000005, amountofPotions); 
//            } else { 
//                cm.sendOk(" Sorry, but you don't have " + potionCost + " Mesos. "); 
//            } 
//            cm.dispose(); 
//        } else if (s == 2) { 
//            if (cm.getParty() != null) { 
//                if (cm.getParty().getMembers().size() >= minPartySize && cm.getParty().getMembers().size() <= maxPartySize) { 
//                    if (cm.isLeader()) { 
//                        for (var i = 0; i < cm.getParty().getMembers().size(); i++) 
//                        if (cm.getParty().getMembers().get(i).getMapId() == cm.getPlayer().getMapId()) { 
//                            //if (cm.getPlayersInMap(mapOfBossPQ) == 0) { 
//                                cm.getPlayer().warpToBossPQ(mapOfBossPQ, timeBefore); 
//                            //} else { 
//                                //cm.sendOk(" Sorry, but there is already a party doing the Boss Party Quest. "); 
//                            //} 
//                        } else {
//                            cm.sendOk("Por favor, diga aos seus membros do grupo para vir para ao seu mapa."); 
//                        } 
//                    } else { 
//                        cm.sendOk("Por favor, pedir para o lider falar comigo."); 
//                    } 
//                } else { 
//                    cm.sendOk("Voce precisa ter pelo menos " + minPartySize + " pessoas em seu grupo, e nao mais do que " + maxPartySize + " pessoas. "); 
//                } 
//            } else { 
//                cm.sendOk("Voce precisa estar em um grupo."); 
//            } 
//            cm.dispose(); 
//        } else if (s == 3) { 
//         
//        } 
//    } else if (status == 10) { 
//        cm.sendNext("Voce precisa ter pelo menos " + minPartySize + " pessoas em seu grupo. Voce tambem precisa ser maior do que o nivel " + lowestLevel + ".  "); 
//        cm.dispose(); 
//    } else if (status == 20) { 
//        if (s == 1) { 
//            cm.warpOutOfBossPQ(0, false); 
//        } else { 
//            cm.warpOutOfBossPQ(0, true); 
//        } 
//        cm.dispose(); 
//    } else if (status == 30) { 
//        cm.warpOutOfBossPQ(0, false); 
//        cm.dispose(); 
//    } 
//}  