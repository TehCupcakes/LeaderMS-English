/*
 *Aquarium PQ NPC [Vending Machine]
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
			if (cm.getPlayer().getMapId() == 230040200) {
                            var party1 = cm.getPlayer().getEventInstance().getPlayers();
				if (playerStatus) { 
					if (status == 0) {
						var eim = cm.getChar().getEventInstance();
						party = eim.getPlayers();
						preamble = eim.getProperty("leader1stpreamble");
						if (preamble == null) {
							cm.sendNext("Hello, welcome to the first stage of Aquarium PQ. See these squids here? Kill them and get me 200 passes in order to complete the first stage.");
							eim.setProperty("leader1stpreamble","done");
							cm.dispose();
						}
						else { 
	                        			var complete = eim.getProperty("1stageclear");
	                        			if (complete != null) {
	                        				cm.sendNext("Continue the mission; the portal has already been opened!");
	                        				cm.dispose();
	                        			}
	                        			else {
								if (cm.haveItem(4001022, 200) == false) {
									cm.sendNext("I'm sorry but you do not have all the 200 passes required to complete this phase.");
									cm.dispose();
								}
								else {
									cm.sendNext("Congratulations on completing the first stage. The portal will now open.");
									clear(1,eim,cm);
                                                                        //cm.givePartyQPoints(20, party);
									cm.getPlayer().getMap().getPortal(4).setScriptName("aqua_pq_in_0");
									cm.givePartyExp(300000, party);
									cm.gainItem(4001022, -200);
									cm.dispose();
								}
							}
						}
					}
				}
				else { // non leader
					var eim = cm.getChar().getEventInstance();
					pstring = "member1stpreamble" + cm.getChar().getId().toString();
					preamble = eim.getProperty(pstring);
					if (status == 0 && preamble == null) {
						cm.sendNext("Ola, bem-vindo a primeira etapa de AquariumPQ. Veja estas lulas aqui? Mate-os e me de 200 passes e eu irei concluir a primeira etapa.");
					}
					else if (status == 0) {// otherwise
	                        		// check for stage completed
	                        		var complete = eim.getProperty("1stageclear");
	                        		if (complete != null) {
	                        			cm.sendNext("Prossiga com a missao pois o portal ja foi liberado!");
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
						else { // shouldn't happen, if it does then just dispose
							cm.dispose();
						}
							
					}
					else if (status == 2) { // preamble completed
						eim.setProperty(pstring,"done");
						cm.dispose();
					}
					else { // shouldn't happen, but still...
						eim.setProperty(pstring,"done"); // just to be sure
						cm.dispose();
					}
				}
			} else if (cm.getPlayer().getMapId() == 230040300) {
				if (playerStatus) { // party leader
					if (status == 0) {
						var eim = cm.getChar().getEventInstance();
						party = eim.getPlayers();
						preamble = eim.getProperty("leader2ndpreamble");
						if (preamble == null) {
							cm.sendNext("Bem-vindo a segunda etapa de AquariumPQ. Veja estas lulas aqui? Mate-os e me de 50 passes e eu irei concluir a segunda etapa.");
							eim.setProperty("leader2ndpreamble","done");
							cm.dispose();
						}
						else { // check how many they have compared to number of party members
								// check for stage completed
								var complete = eim.getProperty("2stageclear");
								if (complete != null) {
									cm.sendNext("Prossiga com a missao pois o portal ja foi liberado!");
									cm.dispose();
								}
								else {
								if (cm.haveItem(4001022, 50) == false) {
									cm.sendNext("Me desculpe, mas voce nao tem todos os 50 passes necessarios para concluir esta fase.");
									cm.dispose();
								}
								else {
									cm.sendNext("Parabens pela conclusao da segunda fase, vou abrir o portal agora.");
									clear(2,eim,cm);
                                                                        //cm.givePartyQPoints(20, party);
									eim.setProperty("2stageclear","done");
									cm.getPlayer().getMap().getPortal(6).setScriptName("aqua_pq_in_1");
									cm.givePartyExp(100000, party);
									cm.gainItem(4001022, -50);
									cm.dispose();
								}
							}
						}
					}
				}
				else { // non leader
					var eim = cm.getChar().getEventInstance();
					pstring = "member2ndpreamble" + cm.getChar().getId().toString();
					preamble = eim.getProperty(pstring);
					if (status == 0 && preamble == null) {
						cm.sendNext("Bem-vindo a segunda etapa de AquariumPQ. Veja estas lulas aqui? Mate-os e me 50 passes e eu irei concluir a segunda etapa.");
						
					}
					else if (status == 0) {// otherwise
	                        		// check for stage completed
	                        		var complete = eim.getProperty("2stageclear");
	                        		if (complete != null) {
	                        			cm.sendNext("Prossiga com a missao pois o portal ja foi liberado!");
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
						else { // shouldn't happen, if it does then just dispose
							cm.dispose();
						}
							
					}
					else if (status == 2) { // preamble completed
						eim.setProperty(pstring,"done");
						cm.dispose();
					}
					else { // shouldn't happen, but still...
						eim.setProperty(pstring,"done"); // just to be sure
						cm.dispose();
					}
				}
			} else if (cm.getPlayer().getMapId() == 230040000) {
				if (playerStatus) { // party leader
					if (status == 0) {
						var eim = cm.getChar().getEventInstance();
						party = eim.getPlayers();
						preamble = eim.getProperty("leader3rdpreamble");
						if (preamble == null) {
							cm.sendNext("Bem-vindo a terceira etapa de AquariumPQ. Veja estas lulas aqui? Mate-os e me de 150 passes e eu irei concluir a terceira etapa.");
							eim.setProperty("leader3rdpreamble","done");
							cm.dispose();
						}
						else { // check how many they have compared to number of party members
	                        			// check for stage completed
	                        			var complete = eim.getProperty("3stageclear");
	                        			if (complete != null) {
	                        				cm.sendNext("Prossiga com a missao pois o portal ja foi liberado!");
	                        				cm.dispose();
	                        			}
	                        			else {
								if (cm.haveItem(4001022, 150) == false) {
									cm.sendNext("Me desculpe, mas voce nao tem todos os 150 passes necessarios para concluir esta fase.");
									cm.dispose();
								}
								else {
									cm.sendNext("Parabens pela conclusao da terceira fase, vou abrir o portal agora.");
									clear(3,eim,cm);
                                                                        //cm.givePartyQPoints(20, party);
									cm.getPlayer().getMap().getPortal(4).setScriptName("aqua_pq_in_2");
									cm.givePartyExp(450000, party);
									cm.gainItem(4001022, -150);
									cm.dispose();
								}
							}
						}
					}
				}
				else { // non leader
					var eim = cm.getChar().getEventInstance();
					pstring = "member3rdpreamble" + cm.getChar().getId().toString();
					preamble = eim.getProperty(pstring);
					if (status == 0 && preamble == null) {
						cm.sendNext("Bem-vindo a terceira etapa de AquariumPQ. Veja estas lulas aqui? Mate-os e me de 150 passes e eu irei concluir a terceira etapa.");
						
					}
					else if (status == 0) {// otherwise
	                        		// check for stage completed
	                        		var complete = eim.getProperty("3stageclear");
	                        		if (complete != null) {
	                        			cm.sendNext("Prossiga com a missao pois o portal ja foi liberado!");
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
						else { // shouldn't happen, if it does then just dispose
							cm.dispose();
						}
							
					}
					else if (status == 2) { // preamble completed
						eim.setProperty(pstring,"done");
						cm.dispose();
					}
					else { // shouldn't happen, but still...
						eim.setProperty(pstring,"done"); // just to be sure
						cm.dispose();
					}
				}
			} else if (cm.getPlayer().getMapId() == 230040400) {
				if (playerStatus) { // party leader
					if (status == 0) {
						var eim = cm.getChar().getEventInstance();
						party = eim.getPlayers();
						preamble = eim.getProperty("leader4thpreamble");
						if (preamble == null) {
							cm.sendNext("Bem-vindo a etapa final de AquariumPQ. Veja estas lulas aqui? Mate-os e me de 300 passes e eu irei concluir a etapa final.");
							eim.setProperty("leader4thpreamble","done");
							cm.dispose();
						}
						else { // check how many they have compared to number of party members
	                        			// check for stage completed
	                        			var complete = eim.getProperty("4stageclear");
	                        			if (complete != null) {
	                        				cm.sendNext("Prossiga com a missao pois o portal ja foi liberado!");
	                        				cm.dispose();
	                        			}
	                        			else {
								if (cm.haveItem(4001022, 300) == false) {
									cm.sendNext("Me desculpe, mas voce nao tem todos os 300 passes necessarios para concluir esta fase.");
									cm.dispose();
								}
								else {
									cm.sendNext("Parabens por concluir a fase final! Vou abrir o portal agora, va terminar a forca do mal que e o #bSuper Pianus#k.");
									clear(4,eim,cm);
                                                                        //cm.givePartyQPoints(20, party);
									cm.getPlayer().getMap().getPortal(4).setScriptName("aqua_pq_boss_0");
									cm.givePartyExp(450000, party);
									cm.gainItem(4001022, -300);
									cm.dispose();
								}
							}
						}
					}
				}
				else { // non leader
					var eim = cm.getChar().getEventInstance();
					pstring = "member4thpreamble" + cm.getChar().getId().toString();
					preamble = eim.getProperty(pstring);
					if (status == 0 && preamble == null) {
						cm.sendNext("Bem-vindo a etapa final de AquariumPQ. Veja estas lulas aqui? Mate-os e me de 300 passes e eu irei concluir a etapa final.");
						
					}
					else if (status == 0) {// otherwise
	                        		// check for stage completed
	                        		var complete = eim.getProperty("4stageclear");
	                        		if (complete != null) {
	                        			cm.sendNext("Prossiga com a missao pois o portal ja foi liberado!");
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
						else { // shouldn't happen, if it does then just dispose
							cm.dispose();
						}
							
					}
					else if (status == 2) { // preamble completed
						eim.setProperty(pstring,"done");
						cm.dispose();
					}
					else { // shouldn't happen, but still...
						eim.setProperty(pstring,"done"); // just to be sure
						cm.dispose();
					}
				}
			} else if (cm.getPlayer().getMapId() == 230040420) {
				if (status == 0) {
					cm.sendNext("Boa sorte no combate contra o #bSuper Pianus#k!");
					cm.dispose();
				}
			}
		}
	}
			
function clear(stage, eim, cm) {
	eim.setProperty(stage + "stageclear","true");
	var packetef = MaplePacketCreator.showEffect("quest/party/clear");
	var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
	var map = eim.getMapInstance(cm.getChar().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}