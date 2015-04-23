/*
 *Yellow Ballon - Stage 3 of LPQ =D
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
					preamble = eim.getProperty("leader3rdpreamble");
					if (preamble == null) {
						cm.sendNext("Ola! Bem-vindo(a) ao 3 estagio. Aqui voce vera um monte de monstros e caixas. Se voce derrotar os monstros, eles vao entregar #b#t4001022##k, igual aos monstros da outra dimensao. Se voce quebrar a caixa, um monstro aparecera e ele tambem dara #b#t4001022##k.\r\nO numero de #b#t4001022#s#k que voce precisa coletar sao 32.");
						eim.setProperty("leader3rdpreamble","done");
						cm.dispose();
					}
					else { 
                        			var complete = eim.getProperty("3stageclear");
                        			if (complete != null) {
                        				cm.sendNext("Por favor completar todos processos, para abrir o portal.");
                        				cm.dispose();
                        			}
                        			else {
							if (cm.haveItem(4001022, 32) == false) {
								cm.sendNext("Sinto muito, mas voce nao tem todos os 32 passes necessarios para concluir esta fase.");
								cm.dispose();
							}
							else {
								cm.sendNext("Bom trabalho derrotando todos os monstros e coletando #b32 #t4001022#s#k. Muito impressionante!");
								clear(1,eim,cm);
								cm.givePartyExp(3000, party);
								cm.gainItem(4001022, -32);
								cm.dispose();
							}
						}
					}
				}
			}
			else { 
				var eim = cm.getChar().getEventInstance();
				pstring = "member3rdpreamble" + cm.getChar().getId().toString();
				preamble = eim.getProperty(pstring);
				if (status == 0 && preamble == null) {
					var qstring = "member3rd" + cm.getChar().getId().toString();
					var question = eim.getProperty(qstring);
					if (question == null) {
						qstring = "CRAP";
					}
						cm.sendNext("Ola! Bem-vindo(a) ao 3 estagio. Aqui voca vera um monte de monstros e caixas. Se voce derrotar os monstros, eles vao entregar #b#t4001022##k, igual aos monstros da outra dimensao. Se voce quebrar a caixa, um monstro aparecera e ele tambem dara #b#t4001022##k.\r\nO numero de #b#t4001022#s#k que voce precisa coletar sao 32.");
					
				}
				else if (status == 0) {
                        		var complete = eim.getProperty("3stageclear");
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
eim.setProperty("3stageclear","true");
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
cm.mapMessage("["+cm.getServerName()+" Quest] O portal que leva para o proximo estagio esta aberto.");
}