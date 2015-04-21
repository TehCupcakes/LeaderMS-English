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
						cm.sendNext("Hello, welcome to the first stage of the Aquarium PQ. See these squids here? Kill them and give me 200 passes and to complete the first stage.");
					}
					else if (status == 0) {// otherwise
	                        		// check for stage completed
	                        		var complete = eim.getProperty("1stageclear");
	                        		if (complete != null) {
                                                    cm.sendNext("Continue the mission. The portal has already opened!");
                                                    cm.dispose();
	                        		}
	                        		else {
                                                    cm.sendOk("Please talk to me after completing the stage.");
                                                    cm.dispose();
						}
					}
					else if (status == 1) {
						if (preamble == null) {
							cm.sendOk("Ok, good luck to you!");
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
							cm.sendNext("Welcome to the second stage of the Aquarium PQ. See these squids here? Kill them and give me 50 passes to complete the second stage.");
							eim.setProperty("leader2ndpreamble","done");
							cm.dispose();
						}
						else { // check how many they have compared to number of party members
								// check for stage completed
								var complete = eim.getProperty("2stageclear");
								if (complete != null) {
									cm.sendNext("Continue the mission. The portal has already opened!");
									cm.dispose();
								}
								else {
								if (cm.haveItem(4001022, 50) == false) {
									cm.sendNext("I'm sorry, but you do not have all 50 passes required to complete this stage.");
									cm.dispose();
								}
								else {
									cm.sendNext("Congratulations on completing the second stage! The portal will now open.");
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
						cm.sendNext("Welcome to the second stage of the Aquarium PQ. See these squids here? Kill them and give me 50 passes to complete the second stage.");
						
					}
					else if (status == 0) {// otherwise
	                        		// check for stage completed
	                        		var complete = eim.getProperty("2stageclear");
	                        		if (complete != null) {
	                        			cm.sendNext("Continue the mission. The portal has already opened!");
	                        			cm.dispose();
	                        		}
	                        		else {
							cm.sendOk("Please talk to me after completing the stage.");
							cm.dispose();
						}
					}
					else if (status == 1) {
						if (preamble == null) {
							cm.sendOk("Ok, good luck to you!");
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
							cm.sendNext("Welcome to the third stage of the Aquarium PQ. See these squids here? Kill them and give me 150 passes to complete the third stage.");
							eim.setProperty("leader3rdpreamble","done");
							cm.dispose();
						}
						else { // check how many they have compared to number of party members
	                        			// check for stage completed
	                        			var complete = eim.getProperty("3stageclear");
	                        			if (complete != null) {
	                        				cm.sendNext("Continue the mission. The portal has already opened!");
	                        				cm.dispose();
	                        			}
	                        			else {
								if (cm.haveItem(4001022, 150) == false) {
									cm.sendNext("I'm sorry, but you do not have all 150 passes required to complete this stage.");
									cm.dispose();
								}
								else {
									cm.sendNext("Congratulations on completing the third stage! The portal will now open.");
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
						cm.sendNext("Welcome to the third stage of the Aquarium PQ. See these squids here? Kill them and give me 150 passes to complete the third stage.");
						
					}
					else if (status == 0) {// otherwise
	                        		// check for stage completed
	                        		var complete = eim.getProperty("3stageclear");
	                        		if (complete != null) {
	                        			cm.sendNext("Continue the mission. The portal has already opened!");
	                        			cm.dispose();
	                        		}
	                        		else {
							cm.sendOk("Please talk to me after completing the stage.");
							cm.dispose();
						}
					}
					else if (status == 1) {
						if (preamble == null) {
							cm.sendOk("Ok, good luck to you!");
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
							cm.sendNext("Welcome to the final stage of the Aquarium PQ. See these squids here? Kill them and give me 300 passes to complete the final stage.");
							eim.setProperty("leader4thpreamble","done");
							cm.dispose();
						}
						else { // check how many they have compared to number of party members
	                        			// check for stage completed
	                        			var complete = eim.getProperty("4stageclear");
	                        			if (complete != null) {
	                        				cm.sendNext("Continue the mission. The portal has already opened!");
	                        				cm.dispose();
	                        			}
	                        			else {
								if (cm.haveItem(4001022, 300) == false) {
									cm.sendNext("I'm sorry, but you do not have all 300 passes required to complete this stage.");
									cm.dispose();
								}
								else {
									cm.sendNext("Congratulations on completing the final stage! I will now open the portal. Go forth and defeat the evil force of #bSuper Pianus#k.");
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
						cm.sendNext("Welcome to the final stage of the Aquarium PQ. See these squids here? Kill them and give me 300 passes to complete the final stage.");
						
					}
					else if (status == 0) {// otherwise
	                        		// check for stage completed
	                        		var complete = eim.getProperty("4stageclear");
	                        		if (complete != null) {
	                        			cm.sendNext("Continue the mission. The portal has already opened!");
	                        			cm.dispose();
	                        		}
	                        		else {
							cm.sendOk("Please talk to me after completing the stage.");
                                                        cm.dispose();
						}
					}
					else if (status == 1) {
						if (preamble == null) {
							cm.sendOk("Ok, good luck to you!");
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
					cm.sendNext("Good luck in the fight against #bSuper Pianus#k!");
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