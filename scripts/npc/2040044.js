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
			if (playerStatus) { // party leader
				if (status == 0) {
					var eim = cm.getChar().getEventInstance();
					party = eim.getPlayers();
					preamble = eim.getProperty("leader9thpreamble");
					if (preamble == null) {
						cm.sendNext("Hello, and welcome to the Boss stage of Ludibrium PQ. Here, is the boss, Alishar. Kill the #eBlack#n Rat over there, and wait a few seconds for Alishar to spawn. I suggest you first put your party's supportive skills on, as #rAlishar#k can be quite deadly. Collect the #bKey#k that drops from #rAlishar#k, and give it to me.");
						eim.setProperty("leader9thpreamble","done");
						cm.dispose();
					}
					else { // check how many they have compared to number of party members
                        			// check for stage completed
                        			var complete = eim.getProperty("9stageclear");
                        			if (complete != null) {
	                        			cm.warp(922011000, 0);
										cm.dispose();
                        			}
                        			else {
							if (cm.haveItem(4001023, 1) == false) {
								cm.sendNext("I'm sorry, but you do not have the Key of Dimension needed to clear this stage.");
								cm.dispose();
							}
							else {
								cm.sendNext("Congratulations on clearing the Boss stage! You have saved Ludibrium! Good Job and you will now be warped to Bonus!");
								clear(1,eim,cm);
								cm.givePartyExp(6000, party);
								cm.gainItem(4001023, -1);
								var eim = cm.getPlayer().getEventInstance();
								eim.finishPQ();
								cm.dispose();
							}
						}
					}
				}
			}
			else { // non leader
				var eim = cm.getChar().getEventInstance();
				pstring = "member9thpreamble" + cm.getChar().getId().toString();
				preamble = eim.getProperty(pstring);
				if (status == 0 && preamble == null) {
					var qstring = "member9th" + cm.getChar().getId().toString();
					var question = eim.getProperty(qstring);
					if (question == null) {
						qstring = "Something went wrong.";
					}
					cm.sendNext("Hello, and welcome to the Boss stage of Ludibrium PQ. Here, is the boss, Alishar. Kill the #eBlack#n Rat over there, and wait a few seconds for Alishar to spawn. I suggest you first put your party's supportive skills on, as #rAlishar#k can be quite deadly. Collect the #bKey#k that drops from #rAlishar#k, and give it to me.");
					
				}
				else if (status == 0) {// otherwise
                        		// check for stage completed
                        		var complete = eim.getProperty("9stageclear");
                        		if (complete != null) {
                        			cm.warp(922011000, 0);
									cm.dispose();
                        		}
                        		else {
							cm.sendOk("Please talk to me after you've completed the stage.");
							cm.dispose();
					}
				}
				else if (status == 1) {
					if (preamble == null) {
						cm.sendOk("Ok, best of luck to you!");
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
    cm.mapMessage("["+cm.getServerName()+" Quest] Congratulations on successfully completing the quest!");
}