/*
 *Violent Ballon - Stage 9 =D
  *@author Jvlaple
  */

importPackage(Packages.tools.packet);
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
					preamble = eim.getProperty("leader9thpreamble");
					if (preamble == null) {
						cm.sendNext("Voce conseguiu chegar ate aqui. Agora e a sua chance de finalmente colocar as maos no verdadeiro culpado. Va para a direita e voce vera um monstro. Derrote-o para encontrar um monstruoso #b#o9300012##k aparecendo do nada. Ele vai estar muito agitado pela presenca do seu grupo, tenha cuidado. \r\nSua tarefa e derrota-lo, coletar o #b#t4001023##k que ele possui e trazer para mim, Se voce conseguir tirar a chave do monstro, nao ha como a porta dimensional ser aberta novamente. Tenho fe em voces. Boa sorte!");
						eim.setProperty("leader9thpreamble","done");
						cm.dispose();
					}
					else {
                        			var complete = eim.getProperty("9stageclear");
                        			if (complete != null) {
	                        			cm.warp(922011000, 0);
							cm.dispose();
                        			}
                        			else {
							if (cm.haveItem(4001023, 1) == false) {
								cm.sendNext("Sinto muito, mas voce nao tem a Chave da Dimensao, necessaria para concluir este estagio.");
								cm.dispose();
							}
							else {
								cm.sendNext("Bom trabalho derrotando todos os monstros e coletando #b#t4001022##k. Muito impressionante!");
								clear(1,eim,cm);
								cm.givePartyExp(6000, party);
                                                             //   cm.givePartyQPoints(20, party);
								cm.gainItem(4001023, -1);
								var eim = cm.getPlayer().getEventInstance();
								eim.finishPQ();
								cm.dispose();
							}
						}
					}
				}
			}
			else { 
				var eim = cm.getChar().getEventInstance();
				pstring = "member9thpreamble" + cm.getChar().getId().toString();
				preamble = eim.getProperty(pstring);
				if (status == 0 && preamble == null) {
					var qstring = "member9th" + cm.getChar().getId().toString();
					var question = eim.getProperty(qstring);
					if (question == null) {
						qstring = "CRAP";
					}
						cm.sendNext("Voce conseguiu chegar ate aqui. Agora e a sua chance de finalmente colocar as maos no verdadeiro culpado. Va para a direita e voce vera um monstro. Derrote-o para encontrar um monstruoso #b#o9300012##k aparecendo do nada. Ele vai estar muito agitado pela presenca do seu grupo, tenha cuidado. \r\nSua tarefa e derrota-lo, coletar o #b#t4001023##k que ele possui e trazer para mim, Se voce conseguir tirar a chave do monstro, nao ha como a porta dimensional ser aberta novamente. Tenho fe em voces. Boa sorte!");
					
				}
				else if (status == 0) {
                        		var complete = eim.getProperty("9stageclear");
                        		if (complete != null) {
                        			cm.warp(922011000, 0);
						cm.dispose();
                        		}
                        		else {
					        cm.sendOk("Por favor completar todos processos, para abrir o portal.");
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
eim.setProperty("9stageclear","true");
var packetef = MaplePacketCreator.showEffect("quest/party/clear");
var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
var packetglow = MaplePacketCreator.environmentChange("gate",2);
var map = eim.getMapInstance(cm.getChar().getMapId());
map.broadcastMessage(packetef);
map.broadcastMessage(packetsnd);
var mf = eim.getMapFactory();
map = mf.getMap(922010100 + stage * 100);
cm.givePartyExp(300, party);
cm.mapMessage("["+cm.getServerName()+" Quest] Parabens por concluir esta missao com sucesso!");
}