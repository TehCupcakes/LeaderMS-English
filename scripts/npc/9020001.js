importPackage(Packages.tools.packet);
importPackage(Packages.server.life);
importPackage(java.awt);

var status;
var curMap;
var playerStatus;
var chatState;
var questions = Array("This is your task: Collect the same number of coupons as the minimum level necessary to make the first job advancement to Warrior.",
			"This is your task: Collect the same number of coupons as the minimum STR ​​necessary to make the first job advancement to Warrior.",
			"This is your task: Collect the same number of coupons as the minimum INT ​​necessary to make the first job advancement to Magician.",
			"This is your task: Collect the same number of coupons as the minimum DEX ​​necessary to make the first job advancement to Bowman.",
			"This is your task: Collect the same number of coupons as the minimum DEX ​​necessary to make the first job advancement to Thief.",
			"This is your task: Collect the same number of coupons as the minimum level necessary to advance to the second job in a class.");
var qanswers = Array(10, 35, 20, 25, 25, 30);
var party;
var preamble;
var stage2rects = Array(Rectangle(-770,-132,28,178),Rectangle(-733,-337,26,105),Rectangle(-601,-328,29,105),Rectangle(-495,-125,24,165));
var stage2combos = Array(Array(0,1,1,1),Array(1,0,1,1),Array(1,1,0,1),Array(1,1,1,0));
var stage3rects = Array(Rectangle(608,-180,140,50),Rectangle(791,-117,140,45),Rectangle(958,-180,140,50),Rectangle(876,-238,140,45),Rectangle(702,-238,140,45));
var stage3combos = Array(Array(0,0,1,1,1),Array(0,1,0,1,1),Array(0,1,1,0,1),Array(0,1,1,1,0),Array(1,0,0,1,1),Array(1,0,1,0,1),Array(1,0,1,1,0),Array(1,1,0,0,1),Array(1,1,0,1,0),Array(1,1,1,0,0));
var stage4rects = Array(Rectangle(910,-236,35,5),Rectangle(877,-184,35,5),Rectangle(946,-184,35,5),Rectangle(845,-132,35,5),Rectangle(910,-132,35,5),Rectangle(981,-132,35,5));
var stage4combos = Array(Array(0,0,0,1,1,1),Array(0,0,1,0,1,1),Array(0,0,1,1,0,1),Array(0,0,1,1,1,0),Array(0,1,0,0,1,1),Array(0,1,0,1,0,1),Array(0,1,0,1,1,0),Array(0,1,1,0,0,1),Array(0,1,1,0,1,0),Array(0,1,1,1,0,0),Array(1,0,0,0,1,1),Array(1,0,0,1,0,1),Array(1,0,0,1,1,0),Array(1,0,1,0,0,1),Array(1,0,1,0,1,0),Array(1,0,1,1,0,0),Array(1,1,0,0,0,1),Array(1,1,0,0,1,0),Array(1,1,0,1,0,0),Array(1,1,1,0,0,0));
var eye = 9300002;
var necki = 9300000;
var slime = 9300003;
var monsterIds = Array(eye, eye, eye, necki, necki, necki, necki, necki, necki, slime);
var prizeIdScroll = Array(2040502, 2040505,					// Overall DEX and DEF
			2040802,										// Gloves for DEX 
			2040002, 2040402, 2040602);						// Helmet, Topwear and Bottomwear for DEF
var prizeIdUse = Array(2000001, 2000002, 2000003, 2000006,	// Orange, White and Blue Potions and Mana Elixir
			2000004, 2022000, 2022003);						// Elixir, Pure Water and Unagi
var prizeQtyUse = Array(80, 80, 80, 50,
			5, 15, 15);
var prizeIdEquip = Array(1032004, 1032005, 1032009,			// Level 20-25 Earrings
			1032006, 1032007, 1032010,						// Level 30 Earrings
			1032002,										// Level 35 Earring
			1002026, 1002089, 1002090);						// Bamboo Hats
var prizeIdEtc = Array(4010000, 4010001, 4010002, 4010003,	// Mineral Ores
			4010004, 4010005, 4010006,						// Mineral Ores
			4020000, 4020001, 4020002, 4020003,				// Jewel Ores
			4020004, 4020005, 4020006,						// Jewel Ores
			4020007, 4020008, 4003000);						// Diamond and Black Crystal Ores and Screws	
var prizeQtyEtc = Array(15, 15, 15, 15,
			8, 8, 8,
			8, 8, 8, 8,
			8, 8, 8,
			3, 3, 30);
			
function start() {
	status = -1;
	mapId = cm.getPlayer().getMapId();
	if (mapId == 103000800)
		curMap = 1;
	else if (mapId == 103000801)
		curMap = 2;
	else if (mapId == 103000802)
		curMap = 3;
	else if (mapId == 103000803)
		curMap = 4;
	else if (mapId == 103000804)
		curMap = 5;
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
	if (curMap == 1) { 
		if (playerStatus) { 
			if (status == 0) {
				var eim = cm.getPlayer().getEventInstance();
				party = eim.getPlayers();
				preamble = eim.getProperty("leader1stpreamble");
				if (preamble == null) {
					cm.sendNext("Hello! Welcome to the 1st stage. Walk around the map and you will see alligators all around. When you defeat them, they will give you a #bcoupon#k. Each member of the group besides the leader should talk to me when they have the same number of #bcoupons#k as the answer to the question I give them.\r\nIf you have the correct number of #bcoupons#k, I'll give you a #bpass#k. Once all the members of the group have finished, they must give the leader the #bpasses#k, and the leader should give the #bpasses#k to me, thus concluding this stage. The quicker you complete the stages, the more stages you will be able to challenge. Be careful and good luck!");
					eim.setProperty("leader1stpreamble","done");
					cm.dispose();
				} else {
					var complete = eim.getProperty(curMap.toString() + "stageclear");
					if (complete != null) {
						cm.sendNext("Please hurry on to the next stage. The portal is open!");
						cm.dispose();
					} else {
						var numpasses = party.size()-1;
						var passes = cm.haveItem(4001008,numpasses);
						var strpasses = "#b" + numpasses.toString() + " passes#k";
						if (!passes) {
							cm.sendNext("You need to collect the number of coupons that correctly answers the question. Neither more, nor less. Make sure you have the coupons in your inventory.");
							cm.dispose();
						} else {
							cm.sendNext("You collected " + strpasses + "! Congratulations on completing this stage. I will open the portal and send you to the next stage. There is a time limit, so please hurry. Good luck to all of you!");
							clear(1,eim,cm);
							cm.givePartyExp(100, party);
							cm.gainItem(4001008, -numpasses);
							cm.dispose();
						}
					}
				}
			}
		} else { 
			var eim = cm.getPlayer().getEventInstance();
			pstring = "member1stpreamble" + cm.getPlayer().getId().toString();
			preamble = eim.getProperty(pstring);
			if (status == 0 && preamble == null) {
				var qstring = "member1st" + cm.getPlayer().getId().toString();
				var question = eim.getProperty(qstring);
				if (question == null) {
					var questionNum = Math.floor(Math.random() * questions.length);
					eim.setProperty(qstring, questionNum.toString());
				}
					cm.sendNext("Hello! Welcome to the 1st stage. Walk around the map and you will see alligators all around. When you defeat them, they will give you a #bcoupon#k. Each member of the group besides the leader should talk to me when they have the same number of #bcoupons#k as the answer to the question I give them.\r\nIf you have the correct number of #bcoupons#k, I'll give you a #bpass#k. Once all the members of the group have finished, they must give the leader the #bpasses#k, and the leader should give the #bpasses#k to me, thus concluding this stage. The quicker you complete the stages, the more stages you will be able to challenge. Be careful and good luck!");
			} else if (status == 0) { 
				var complete = eim.getProperty(curMap.toString() + "stageclear");
				if (complete != null) {
					cm.sendNext("Please hurry on to the next stage. The portal is open!");
					cm.dispose();
				} else {
					var qstring = "member1st" + cm.getPlayer().getId().toString();
					var numcoupons = qanswers[parseInt(eim.getProperty(qstring))];
                                        var enough = cm.haveItem(4001007,numcoupons);
					var toomany = cm.haveItem(4001007,(numcoupons+1));
					if (enough && !toomany) {
                                            cm.sendNext("Correct! You just won a #bpass#k. Please give it to your party leader.");
                                            cm.gainItem(4001007, -numcoupons);
                                            cm.gainItem(4001008, 1);
					} else {
                                            cm.sendNext("Incorrect answer. I can give you the pass if you collect the number of #bcoupons#k that correctly answers the question.");
					}
					cm.dispose();
				}
			} else if (status == 1) {
				if (preamble == null) {
					var qstring = "member1st" + cm.getPlayer().getId().toString();
					var question = parseInt(eim.getProperty(qstring));
					cm.sendNextPrev(questions[question]);
				} else { 
					cm.dispose();
				}
			} else if (status == 2) { 
				eim.setProperty(pstring,"done");
				cm.dispose();
			} else { 
				eim.setProperty(pstring,"done");
				cm.dispose();
			}
		} 
	} else if (2 <= curMap && 4 >= curMap) {
		rectanglestages(cm);
	} else if (curMap == 5) {
		var eim = cm.getPlayer().getEventInstance();
		var stage5done = eim.getProperty("5stageclear");
		if (stage5done == null) {
			if (playerStatus) { 
				var map = eim.getMapInstance(cm.getPlayer().getMapId());
				var passes = cm.haveItem(4001008,10);
				if (passes) {
					cm.sendNext("Here is the gate that leads to the ultimate bonus stage. This stage allows you to defeat common monsters a bit more easily. You will have a time limit to defeat as many as possible, but will be able to leave the stage when you want by talking to the NPC. Again, congratulations on completing the stages.");
					party = eim.getPlayers();
					cm.gainItem(4001008, -10);
					clear(5,eim,cm);
					cm.givePartyExp(1500, party);
                                      //  cm.givePartyQPoints(20, party);
					cm.dispose();
				} else { 
					cm.sendNext("Hello! Welcome to the 5th and final stage. Walk around the map and you will see some Monster Heads. Defeat all of them and give 10 #bpasses#k to me. Give your passes to your party leader, and the leader will deliver all 10 passes to me. The monsters may look familiar, but they are much stronger than you think. Be careful. Good luck!");
				}
				cm.dispose();
			} else { 
					cm.sendNext("Hello! Welcome to the 5th and final stage. Walk around the map and you will see some Monster Heads. Defeat all of them and give 10 #bpasses#k to me. Give your passes to your party leader, and the leader will deliver all 10 passes to me. The monsters may look familiar, but they are much stronger than you think. Be careful. Good luck!");
				cm.dispose();
			}
		} else { 
			if (status == 0) {
				cm.sendNext("Incredible! You have completed all the stages in order to get here! Here is a small reward for a job well done. But before accepting, make sure you have available space in your use and etc. inventory tabs.");
			}
			if (status == 1) {
				getPrize(eim,cm);
				cm.dispose();
			}
		}
	} else { 
		cm.sendNext("Invalid map. You cannot complete this stage.");
		cm.dispose();
		}
	}
}

function clear(stage, eim, cm) {
	eim.setProperty(stage.toString() + "stageclear","true");
	var packetef = MaplePacketCreator.showEffect("quest/party/clear");
	var packetsnd = MaplePacketCreator.playSound("Party1/Clear");
	var packetglow = MaplePacketCreator.environmentChange("gate",2);
	var map = eim.getMapInstance(cm.getPlayer().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
	map.broadcastMessage(packetglow);
	var mf = eim.getMapFactory();
	map = mf.getMap(103000800 + stage);
	var nextStage = eim.getMapInstance(103000800 + stage);
	var portal = nextStage.getPortal("next00");
	if (portal != null) {
		portal.setScriptName("kpq" + (stage+1).toString());
	}
}

function failstage(eim, cm) {
	var packetef = MaplePacketCreator.showEffect("quest/party/wrong_kor");
	var packetsnd = MaplePacketCreator.playSound("Party1/Failed");
	var map = eim.getMapInstance(cm.getPlayer().getMapId());
	map.broadcastMessage(packetef);
	map.broadcastMessage(packetsnd);
}

function rectanglestages (cm) {
	var debug = false;
	var eim = cm.getPlayer().getEventInstance();
	if (curMap == 2) {
		var nthtext = "2";
		var nthobj = "ropes";
		var nthverb = "hang";
		var nthpos = "hang very low on the ropes";
		var curcombo = stage2combos;
		var currect = stage2rects;
		var objset = [0,0,0,0];
	} else if (curMap == 3) {
		var nthtext = "3";
		var nthobj = "platforms";
		var nthverb = "stand";
		var nthpos = "stand too close to the edge";
		var curcombo = stage3combos;
		var currect = stage3rects;
		var objset = [0,0,0,0,0];
	} else if (curMap == 4) {
		var nthtext = "4";
		var nthobj = "barrels";
		var nthverb = "stand";
		var nthpos = "stand too close to the edge";
		var curcombo = stage4combos;
		var currect = stage4rects;
		var objset = [0,0,0,0,0,0];
	}
	if (playerStatus) { 
		if (status == 0) {
			party = eim.getPlayers();
			preamble = eim.getProperty("leader" + nthtext + "preamble");
			if (preamble == null) {
				cm.sendNext("Hello! Welcome to stage " + nthtext + ". You'll see some barrels nearby. 3 barrels are connected to the portal leading to the next stage. #b3 members of the party need to find the correct barrels and stand on top of them to complete the stage. BUT, to determine the answer, the player must stand in the center of the barrel, not at the edge, and only 3 members of your group can stand on top of the barrels. When the members are on top, the leader of the group should #bdouble click me to see if the answer is correct or not#k. Now, find the correct barrels!");
				eim.setProperty("leader" + nthtext + "preamble","done");
				var sequenceNum = Math.floor(Math.random() * curcombo.length);
				eim.setProperty("stage" + nthtext + "combo",sequenceNum.toString());
				cm.dispose();
			} else {
				var complete = eim.getProperty(curMap.toString() + "stageclear");
				if (complete != null) {	
					var mapClear = curMap.toString() + "stageclear";
					eim.setProperty(mapClear,"true"); 
					cm.sendNext("Please hurry on to the next stage. The portal is open!");
				} else { 
					var totplayers = 0;
					for (i = 0; i < objset.length; i++) {
						for (j = 0; j < party.size(); j++) {
							var present = currect[i].contains(party.get(j).getPosition());
							if (present) {
								objset[i] = objset[i] + 1;
								totplayers = totplayers + 1;
							}
						}
					}
			if (totplayers == 3 || debug) {
				var combo = curcombo[parseInt(eim.getProperty("stage" + nthtext + "combo"))];
				var testcombo = true;
				for (i = 0; i < objset.length; i++) {
					if (combo[i] != objset[i])
						testcombo = false;
				}
			if (testcombo || debug) {
				clear(curMap,eim,cm);
				var exp = (Math.pow(2,curMap) * 50);
				cm.givePartyExp(exp, party);
				cm.dispose();
			} else { 
				failstage(eim,cm);
				cm.dispose();
				}
			} else {
				if (debug) {
					var outstring = "Objects contain:"
					for (i = 0; i < objset.length; i++) {
						outstring += "\r\n" + (i+1).toString() + ". " + objset[i].toString();
					}
					cm.sendNext(outstring); 
				} else
					cm.sendNext("It seems that you still have not found the three barrels. Think of a different combination of the barrels. Don't forget that only 3 members can stand on top of the barrels, and they must be in the center for the response to count as correct. Keep trying!");
					cm.dispose();
					}
				}
			}
		} else {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {
				var target = eim.getMapInstance(103000800 + curMap);
				var targetPortal = target.getPortal("st00");
				cm.getPlayer().changeMap(target, targetPortal);
			}
			cm.dispose();
		}
	} else { 
		if (status == 0) {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {
				cm.sendNext("Please hurry on to the next stage. The portal is open!");
			} else {
				cm.sendNext("Please tell your party leader to speak to me.");
				cm.dispose();
			}
		} else {
			var complete = eim.getProperty(curMap.toString() + "stageclear");
			if (complete != null) {	
				var target = eim.getMapInstance(103000800 + curMap);
				var targetPortal = target.getPortal("st00");
				cm.getPlayer().changeMap(target, targetPortal);
			}
			cm.dispose();
		}
	}
}

function getPrize(eim,cm) {
	var itemSetSel = Math.random();
	var itemSet;
	var itemSetQty;
	var hasQty = false;
	if (itemSetSel < 0.3)
		itemSet = prizeIdScroll;
	else if (itemSetSel < 0.6)
		itemSet = prizeIdEquip;
	else if (itemSetSel < 0.9) {
		itemSet = prizeIdUse;
		itemSetQty = prizeQtyUse;
		hasQty = true;
	} else { 
		itemSet = prizeIdEtc;
		itemSetQty = prizeQtyEtc;
		hasQty = true;
	}
	var sel = Math.floor(Math.random()*itemSet.length);
	var qty = 1;
	if (hasQty)
	qty = itemSetQty[sel];
	cm.gainItem(itemSet[sel], qty);
	var map = eim.getMapInstance(103000805);
	var portal = map.getPortal("sp");
	cm.getPlayer().changeMap(map,portal);
}