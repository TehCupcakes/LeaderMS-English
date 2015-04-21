/*
 *Sky Blue Ballon - Stage 7 of LPQ =D
  *@author Jvlaple
  */

importPackage(Packages.tools);
importPackage(Packages.server.life);
importPackage(java.awt);

var status;
var partyLdr;
var chatState;
var party;
var preamble;

function start() {
	status = -1;
	playerStatus = cm.isLeader();
	preamble = null;
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
			if (playerStatus) {
				if (status == 0) {
					var eim = cm.getChar().getEventInstance();
					party = eim.getPlayers();
					preamble = eim.getProperty("leader7thpreamble");
					if (preamble == null) {
						cm.sendNext("Ola! Bem-vindo ao 7 estagio.  Aqui voce vai encontrar um monstro ridiculamente poderoso chamado #b#o9300010##k. Derrote o monstro e encontre o #b#t4001022##k necessario para seguir para o proximo estagio. Por favor, colete #b3#t4001022#s#k.\r\nPara acabar com o monstro, derrote-o de longe. A unica maneira de atacar seria de uma longa distancia, mas... ah, sim, tenha cuidado, #o9300010# e muito perigoso. Com certeza voce vai se machucar se nao tomar cuidado. Boa sorte!");
						eim.setProperty("leader7thpreamble","done");
						cm.dispose();
					}
					else { 
                        			var complete = eim.getProperty("7stageclear");
                        			if (complete != null) {
                        				cm.sendNext("Por favor completar todos processos, para abrir o portal.");
                        				cm.dispose();
                        			}
                        			else {
							if (cm.haveItem(4001022, 3) == false) {
								cm.sendNext("Sinto muito, mas voce nao tem todos os 3 passes necessarios para concluir esta fase.");
								cm.dispose();
							}
							else {
								cm.sendNext("Bom trabalho derrotando todos os monstros e coletando #b3 #t4001022#s#k. Muito impressionante!");
								clear(1,eim,cm);
								cm.givePartyExp(3000, party);
								cm.gainItem(4001022, -3);
								cm.dispose();
							}
						}
					}
				}
			}
			else { 
				var eim = cm.getChar().getEventInstance();
				pstring = "member7thpreamble" + cm.getChar().getId().toString();
				preamble = eim.getProperty(pstring);
				if (status == 0 && preamble == null) {
					var qstring = "member7th" + cm.getChar().getId().toString();
					var question = eim.getProperty(qstring);
					if (question == null) {
						qstring = "FUCK";
					}
						cm.sendNext("Ola! Bem-vindo ao 7 estagio.  Aqui voce vai encontrar um monstro ridiculamente poderoso chamado #b#o9300010##k. Derrote o monstro e encontre o #b#t4001022##k necessario para seguir para o proximo estagio. Por favor, colete #b3#t4001022#s#k.\r\nPara acabar com o monstro, derrote-o de longe. A unica maneira de atacar seria de uma longa distancia, mas... ah, sim, tenha cuidado, #o9300010# e muito perigoso. Com certeza voce vai se machucar se nao tomar cuidado. Boa sorte!");
					
				}
				else if (status == 0) {
                        		var complete = eim.getProperty("7stageclear");
                        		if (complete != null) {
                        			cm.sendNext("Por favor completar todos processos, para abrir o portal.");
                        			cm.dispose();
                        		}
                        		else {
							cm.sendOk("Por favor, fale comigo depois de ter concluido o estagio.");
							cm.dispose();
					}
				}
				else if (status == 1) {
					if (preamble == null) {
						cm.sendOk("Ok, boa sorte para voce!");
						cm.dispose();
					}
					else {
						cm.dispose();
					}
						
				}
				else if (status == 2) { 
					eim.setProperty(pstring,"done");
					cm.dispose();
				}
				else { 
					eim.setProperty(pstring,"done"); 
					cm.dispose();
				}
			}
		}
	}
			
function clear(stage, eim, cm) {
eim.setProperty("7stageclear","true");
var packetef = MaplePacketCreator.showEffect("quest/party/clear");
var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
var packetglow = MaplePacketCreator.environmentChange("gate",2);
var map = eim.getMapInstance(cm.getChar().getMapId());
map.broadcastMessage(packetef);
map.broadcastMessage(packetsnd);
map.broadcastMessage(packetglow);
var mf = eim.getMapFactory();
map = mf.getMap(922010100 + stage * 100);
cm.givePartyExp(300, party);
cm.mapMessage("[LeaderMS Quest] O portal que leva para o proximo estagio esta aberto.");
}