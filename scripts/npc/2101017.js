/*2101017.js
 *Cesar
 *@author Jvlaple
 */
 
importPackage(java.lang);
importPackage(Packages.server);
 
 
var status = 0;
var toBan = -1;
var choice;
var arena;
var arenaName;
var type;
var map;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1) {
			status++;
		} else {
			status--;
		} 
	
	if (cm.getPlayer().getMapId() == 980010100 || cm.getPlayer().getMapId() == 980010200 || cm.getPlayer().getMapId() == 980010300) {
			if (status == 0) {
				switch (cm.getPlayer().getMapId()) {
					case 980010100:
						arena = MapleSquadType.ARIANT1;
						break;
					case 980010200:
						arena = MapleSquadType.ARIANT2;
						break;
					case 980010300:
						arena = MapleSquadType.ARIANT3;
						break;
					default :
						return;
				}
				if (cm.checkSquadLeader(arena)) {
					cm.sendSimple("What would you like to do?#b\r\n\r\n#L1#View current arena record!#l\r\n#L2#Start the fight!#l\r\n#L3#Leave this arena!#l");
                    status = 19;
				} else if (cm.isSquadMember(arena)) {
					var noOfChars = cm.numSquadMembers(arena);
                    var toSend = "You currently have these people in your arena:\r\n#b";
					for (var i = 1; i <= noOfChars; i++) {
						toSend += "\r\n#L" + i + "#" + cm.getSquadMember(arena, i - 1).getName() + "#l";
					}
					cm.sendSimple(toSend);
					cm.dispose();
				} else {
					cm.sendOk("What happened?");
					cm.dispose();
				}
			} else if (status == 20) {
				switch (cm.getPlayer().getMapId()) {
						case 980010100:
							arena = MapleSquadType.ARIANT1;
							arenaName = "AriantPQ1";
							break;
						case 980010200:
							arena = MapleSquadType.ARIANT2;
							arenaName = "AriantPQ2";
							break;
						case 980010300:
							arena = MapleSquadType.ARIANT3;
							arenaName = "AriantPQ3";
							break;
						default :
							return;
					}
				if (selection == 1) {
					var noOfChars = cm.numSquadMembers(arena);
                    var toSend = "You currently have these people in your arena:\r\n#b";
					for (var i = 1; i <= noOfChars; i++) {
						toSend += "\r\n#L" + i + "#" + cm.getSquadMember(arena, i - 1).getName() + "#l";
					}
					cm.sendSimple(toSend);
					cm.dispose();
				} else if (selection == 2) {
					if (cm.numSquadMembers(arena) < 2 && !cm.getChar().isGM()) {
						cm.sendOk("I can only let you fight when you have two or more people.");
						cm.dispose();
					} else {
						var em = cm.getEventManager(arenaName);
						if (em == null) {
							cm.sendOk("...");
							cm.dispose();
						}
						else {
							cm.setSquadState(arena, 2);
							em.startInstance(cm.getSquad(arena), cm.getChar().getMap());
						}
						cm.dispose();
					}
				} else if (selection == 3) {
					cm.mapMessage("The leader of the arena left.");
					cm.warpSquadMembers(arena, 980010000)
					var squad = cm.getPlayer().getClient().getChannelServer().getMapleSquad(arena);
					cm.getPlayer().getClient().getChannelServer().removeMapleSquad(squad, arena);
					cm.dispose();
				}
			} 
                    } else if (cm.getPlayer().getMapId() == 980010101 || cm.getPlayer().getMapId() == 980010201 || cm.getPlayer().getMapId() == 980010301) {
			var eim = cm.getChar().getEventInstance();
			if (status == 0) {
				var gotTheBombs = eim.getProperty("gotBomb" + cm.getChar().getId());
				if (gotTheBombs != null) {
                                    cm.sendOk("I already gave you the bombs. Please kill #eScorpions#n to get more in the next round!");
//					cm.sendOk("Eu ja lhe dei as bombas, por favor, mate os #eEscorpioes#n para conseguir mais na proxima!\r\n\r\n#b#L3#Eu quero sair daqui!#l#k");
					cm.dispose();
				} else {
					cm.sendOk("I gave you (5) #b#eBombs#k#n and (50) #b#eElemental Rocks#k#n.\r\nUse the elemental rocks to capture the scorpions for #r#eSpirit Jewels#k#n!");
					eim.setProperty("gotBomb" + cm.getChar().getId(), "got");
					cm.gainItem(2270002, 50);
					cm.gainItem(2100067, 5);
					cm.dispose();
				}
			}
		} 
	}
}
