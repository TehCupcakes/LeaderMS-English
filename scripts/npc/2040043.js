/*
 *Sky Blue Ballon - Stage 7 of LPQ =D
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

var stage8combos = Array(Array(0, 0, 0, 0, 1, 1, 1, 1, 1), //Lol, it took me forever to work these out :D
						Array(0, 0, 0, 1, 0, 1, 1, 1, 1),
						Array(0, 0, 0, 1, 1, 0, 1, 1, 1),
						Array(0, 0, 0, 1, 1, 1, 0, 1, 1),
						Array(0, 0, 0, 1, 1, 1, 1, 0, 1),
						Array(0, 0, 0, 1, 1, 1, 1, 1, 0),
						Array(0, 0, 1, 0, 0, 1, 1, 1, 1),
						Array(0, 0, 1, 0, 1, 0, 1, 1, 1),
						Array(0, 0, 1, 0, 1, 1, 0, 1, 1),
						Array(0, 0, 1, 0, 1, 1, 1, 0, 1),
						Array(0, 0, 1, 0, 1, 1, 1, 1, 0),
						Array(0, 0, 1, 1, 0, 0, 1, 1, 1),
						Array(0, 0, 1, 1, 0, 1, 0, 1, 1),
						Array(0, 0, 1, 1, 0, 1, 1, 0, 1),
						Array(0, 0, 1, 1, 0, 1, 1, 1, 0),
						Array(0, 0, 1, 1, 1, 0, 0, 1, 1),
						Array(0, 0, 1, 1, 1, 0, 1, 0, 1),
						Array(0, 0, 1, 1, 1, 0, 1, 1, 0),
						Array(0, 0, 1, 1, 1, 1, 0, 0, 1),
						Array(0, 0, 1, 1, 1, 1, 0, 1, 0),
						Array(0, 0, 1, 1, 1, 1, 1, 0, 0),
						Array(0, 1, 0, 0, 0, 1, 1, 1, 1),
						Array(0, 1, 0, 0, 1, 0, 1, 1, 1),
						Array(0, 1, 0, 0, 1, 1, 0, 1, 1),
						Array(0, 1, 0, 0, 1, 1, 1, 0, 1),
						Array(0, 1, 0, 0, 1, 1, 1, 1, 0),
						Array(0, 1, 0, 1, 0, 0, 1, 1, 1),
						Array(0, 1, 0, 1, 0, 1, 0, 1, 1),
						Array(0, 1, 0, 1, 0, 1, 1, 0, 1),
						Array(0, 1, 0, 1, 0, 1, 1, 1, 0),
						Array(0, 1, 0, 1, 1, 0, 0, 1, 1),
						Array(0, 1, 0, 1, 1, 0, 1, 0, 1),
						Array(0, 1, 0, 1, 1, 0, 1, 1, 0),
						Array(0, 1, 0, 1, 1, 1, 0, 0, 1),
						Array(0, 1, 0, 1, 1, 1, 0, 1, 0),
						Array(0, 1, 0, 1, 1, 1, 1, 0, 0),
						Array(0, 1, 1, 0, 0, 0, 1, 1, 1),
						Array(0, 1, 1, 0, 0, 1, 0, 1, 1),
						Array(0, 1, 1, 0, 0, 1, 1, 0, 1),
						Array(0, 1, 1, 0, 0, 1, 1, 1, 0),
						Array(0, 1, 1, 0, 1, 0, 0, 1, 1),
						Array(0, 1, 1, 0, 1, 0, 1, 0, 1),
						Array(0, 1, 1, 0, 1, 0, 1, 1, 0),
						Array(0, 1, 1, 0, 1, 1, 0, 0, 1),
						Array(0, 1, 1, 0, 1, 1, 0, 1, 0),
						Array(0, 1, 1, 0, 1, 1, 1, 0, 0),
						Array(0, 1, 1, 1, 0, 0, 0, 1, 1),
						Array(0, 1, 1, 1, 0, 0, 1, 0, 1),
						Array(0, 1, 1, 1, 0, 0, 1, 1, 0),
						Array(0, 1, 1, 1, 0, 1, 0, 0, 1),
						Array(0, 1, 1, 1, 0, 1, 0, 1, 0),
						Array(0, 1, 1, 1, 0, 1, 1, 0, 0),
						Array(0, 1, 1, 1, 1, 0, 0, 0, 1),
						Array(0, 1, 1, 1, 1, 0, 0, 1, 0),
						Array(0, 1, 1, 1, 1, 0, 1, 0, 0),
						Array(0, 1, 1, 1, 1, 1, 0, 0, 0),
						Array(1, 0, 0, 0, 0, 1, 1, 1, 1),
						Array(1, 0, 0, 0, 1, 0, 1, 1, 1),
						Array(1, 0, 0, 0, 1, 1, 0, 1, 1),
						Array(1, 0, 0, 0, 1, 1, 1, 0, 1),
						Array(1, 0, 0, 0, 1, 1, 1, 1, 0),
						Array(1, 0, 0, 1, 0, 0, 1, 1, 1),
						Array(1, 0, 0, 1, 0, 1, 0, 1, 1),
						Array(1, 0, 0, 1, 0, 1, 1, 0, 1),
						Array(1, 0, 0, 1, 0, 1, 1, 1, 0),
						Array(1, 0, 0, 1, 1, 0, 0, 1, 1),
						Array(1, 0, 0, 1, 1, 0, 1, 0, 1),
						Array(1, 0, 0, 1, 1, 0, 1, 1, 0),
						Array(1, 0, 0, 1, 1, 1, 0, 0, 1),
						Array(1, 0, 0, 1, 1, 1, 0, 1, 0),
						Array(1, 0, 0, 1, 1, 1, 1, 0, 0),
						Array(1, 0, 1, 0, 0, 0, 1, 1, 1),
						Array(1, 0, 1, 0, 0, 1, 0, 1, 1),
						Array(1, 0, 1, 0, 0, 1, 1, 0, 1),
						Array(1, 0, 1, 0, 0, 1, 1, 1, 0),
						Array(1, 0, 1, 0, 1, 0, 0, 1, 1),
						Array(1, 0, 1, 0, 1, 0, 1, 0, 1),
						Array(1, 0, 1, 0, 1, 0, 1, 1, 0),
						Array(1, 0, 1, 0, 1, 1, 0, 0, 1),
						Array(1, 0, 1, 0, 1, 1, 0, 1, 0),
						Array(1, 0, 1, 0, 1, 1, 1, 0, 0),
						Array(1, 0, 1, 1, 0, 0, 0, 1, 1),
						Array(1, 0, 1, 1, 0, 0, 1, 0, 1),
						Array(1, 0, 1, 1, 0, 0, 1, 1, 0),
						Array(1, 0, 1, 1, 0, 1, 0, 0, 1),
						Array(1, 0, 1, 1, 0, 1, 0, 1, 0),
						Array(1, 0, 1, 1, 0, 1, 1, 0, 0),
						Array(1, 0, 1, 1, 1, 0, 0, 0, 1),
						Array(1, 0, 1, 1, 1, 0, 0, 1, 0),
						Array(1, 0, 1, 1, 1, 0, 1, 0, 0),
						Array(1, 0, 1, 1, 1, 1, 0, 0, 0),
						Array(1, 1, 0, 0, 0, 0, 1, 1, 1),
						Array(1, 1, 0, 0, 0, 1, 0, 1, 1),
						Array(1, 1, 0, 0, 0, 1, 1, 0, 1),
						Array(1, 1, 0, 0, 0, 1, 1, 1, 0),
						Array(1, 1, 0, 0, 1, 0, 0, 1, 1),
						Array(1, 1, 0, 0, 1, 0, 1, 0, 1),
						Array(1, 1, 0, 0, 1, 0, 1, 1, 0),
						Array(1, 1, 0, 0, 1, 1, 0, 0, 1),
						Array(1, 1, 0, 0, 1, 1, 0, 1, 0),
						Array(1, 1, 0, 0, 1, 1, 1, 0, 0),
						Array(1, 1, 0, 1, 0, 0, 0, 1, 1),
						Array(1, 1, 0, 1, 0, 0, 1, 0, 1),
						Array(1, 1, 0, 1, 0, 0, 1, 1, 0),
						Array(1, 1, 0, 1, 0, 1, 0, 0, 1),
						Array(1, 1, 0, 1, 0, 1, 0, 1, 0),
						Array(1, 1, 0, 1, 0, 1, 1, 0, 0),
						Array(1, 1, 0, 1, 1, 0, 0, 0, 1),
						Array(1, 1, 0, 1, 1, 0, 0, 1, 0),
						Array(1, 1, 0, 1, 1, 0, 1, 0, 0),
						Array(1, 1, 0, 1, 1, 1, 0, 0, 0),
						Array(1, 1, 1, 0, 0, 0, 0, 1, 1),
						Array(1, 1, 1, 0, 0, 0, 1, 0, 1),
						Array(1, 1, 1, 0, 0, 0, 1, 1, 0),
						Array(1, 1, 1, 0, 0, 1, 0, 0, 1),
						Array(1, 1, 1, 0, 0, 1, 0, 1, 0),
						Array(1, 1, 1, 0, 0, 1, 1, 0, 0),
						Array(1, 1, 1, 0, 1, 0, 0, 0, 1),
						Array(1, 1, 1, 0, 1, 0, 0, 1, 0),
						Array(1, 1, 1, 0, 1, 0, 1, 0, 0),
						Array(1, 1, 1, 0, 1, 1, 0, 0, 0),
						Array(1, 1, 1, 1, 0, 0, 0, 0, 1),
						Array(1, 1, 1, 1, 0, 0, 0, 1, 0),
						Array(1, 1, 1, 1, 0, 0, 1, 0, 0),
						Array(1, 1, 1, 1, 0, 1, 0, 0, 0),
						Array(1, 1, 1, 1, 1, 0, 0, 0, 0));

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
			boxStage(cm);
		}
	}
			
function clear(stage, eim, cm) {
    eim.setProperty("8stageclear","true");
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
    cm.mapMessage("["+cm.getServerName()+" Quest] The portal leading to the next stage is open.");
}

function failstage(eim, cm) {
	var packetef = MaplePacketCreator.showEffect("quest/party/wrong_kor");
	var packetsnd = MaplePacketCreator.playSound("Party1/Failed");
	var map = eim.getMapInstance(cm.getChar().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}

function boxStage(cm) {
	var debug = false;
	var eim = cm.getChar().getEventInstance();
	var nthtext = "eighth";
	var nthobj = "boxes";
	var nthverb = "stand";
	var nthpos = "stand too close to the edges";
	var curcombo = stage8combos;
	var currect = cm.getChar().getMap().getAreas();
	var objset = [0,0,0,0,0,0,0,0,0];
		if (playerStatus) { // leader
			if (status == 0) {
					// check for preamble
				 
					party = eim.getPlayers();
					preamble = eim.getProperty("leader" + nthtext + "preamble");
					if (preamble == null) {
							cm.sendNext("Hi. Welcome to the eighth stage. Next to me, there are nine boxes. All you have to do, is have 5 people stand on them and then, the leader must click on me to see if it is correct. Good Luck!");
							eim.setProperty("leader" + nthtext + "preamble","done");
							var sequenceNum = Math.floor(Math.random() * curcombo.length);
							eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
							cm.dispose();
					}
					else {
						// otherwise
						// check for stage completed
						var complete = eim.getProperty("8stageclear");
						if (complete != null) {	
							var mapClear = "8stageclear";
							eim.setProperty(mapClear,"true"); // Just to be sure
							cm.sendNext("Please hurry on to the next stage, the portal opened!");
						}
						// check for people on ropes
						else { 
								// check for people on ropes(objset)
								var totplayers = 0;
								for (i = 0; i < objset.length; i++) {
										for (j = 0; j < party.size(); j++) {
												var present = currect.get(i).contains(party.get(j).getPosition());
													if (present) {
														objset[i] = objset[i] + 1;
														totplayers = totplayers + 1;
												}
										}
								}
								// compare to correct
								// first, are there 3 players on the objset?
								var numSpawn = 5;
								if (totplayers == 5 || debug) {
										var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
										// debug
										// combo = curtestcombo;
										var testcombo = true;
										for (i = 0; i < objset.length; i++) {
											if (combo[i] != objset[i]){
												testcombo = false;
												}
										}
										if (testcombo || debug) {
												// do clear
												clear(1,eim,cm);
												var exp = (3000);
												cm.givePartyExp(exp, party);
												cm.dispose();
										}
										else { // wrong
												// do wrong
												//failstage(eim,cm);
												//cm.sendOk("Wrong!");1706/300
												failstage(eim,cm);
												//cm.sendOk(combo);
												cm.dispose();
												
										}
								}
								else {
										if (debug) {
											var outstring = "Objects contain:"
											for (i = 0; i < objset.length; i++) {
												outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
											}
											cm.sendNext(outstring); 
											var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
											//cm.sendNext(combo);
										}
										else
											cm.sendNext("It looks like you haven't found the 5 " + nthobj + " just yet. Please think of a different combination of " + nthobj + ". Only 5 are allowed to " + nthverb + " on " + nthobj + ", and if you " + nthpos + " it may not count as an answer, so please keep that in mind. Keep going!");
											cm.dispose();
								}
						}
					}
					// just in case.
			}
			else {
				var complete = eim.getProperty("8stageclear");
					if (complete != null) {	
					var target = eim.getMapInstance(103000800 + curMap);
			var targetPortal = target.getPortal("st00");
					cm.getChar().changeMap(target, targetPortal);
				}
				cm.dispose();
			}
	}
	else { // not leader
		if (status == 0) {
				var complete = eim.getProperty("8stageclear");
				if (complete != null) {
					cm.sendNext("Please proceed in the Party Quest, the portal opened!");
					cm.dispose();
				}
				else {
					cm.sendNext("Please have the party leader talk to me.");
					cm.dispose();
				}
		}
	else {
				var complete = eim.getProperty("8stageclear");
			if (complete != null) {	
			cm.sendNext("Please proceed in the Party Quest, the portal opened!");
			cm.dispose();
		}
				cm.dispose();
			}
	}
}