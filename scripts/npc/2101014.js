/*2101014.js - Lobby and Entrance
 * @author Jvlaple
 * For Jvlaple's AriantPQ
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
		if (cm.getPlayer().getMapId() == 980010000) {
			if (status == 0) {
				var toSnd = "Would you like to join the #eAriant Coliseum Challenge#n?\r\n\r\n#e#r       (Choose an arena)#n#k\r\n#b";
				if (cm.getSquadState(MapleSquadType.ARIANT1) != 2 && cm.getSquadState(MapleSquadType.ARIANT1) != 1) {
					toSnd += "#L0#Start Ariant Coliseum (1)#l\r\n";
				} else if (cm.getSquadState(MapleSquadType.ARIANT1) == 1) {
					toSnd += "#L0#Join Ariant Coliseum (1)  Owner: " + cm.getSquadMember(MapleSquadType.ARIANT1, 0).getName() + " ; Current Members: " + cm.numSquadMembers(MapleSquadType.ARIANT1) + "\r\n";
				}
				if (cm.getSquadState(MapleSquadType.ARIANT2) != 2 && cm.getSquadState(MapleSquadType.ARIANT2) != 1) {
					toSnd += "#L1#Start Ariant Coliseum (2)#l\r\n";
				} else if (cm.getSquadState(MapleSquadType.ARIANT2) == 1) {
					toSnd += "#L1#Join Ariant Coliseum (2)  Owner: " + cm.getSquadMember(MapleSquadType.ARIANT2, 0).getName() + " ; Current Members: " + cm.numSquadMembers(MapleSquadType.ARIANT2) + "\r\n";
				}
				if (cm.getSquadState(MapleSquadType.ARIANT3) != 2 && cm.getSquadState(MapleSquadType.ARIANT3) != 1) {
					toSnd += "#L2#Start Ariant Coliseum (3)#l\r\n";
				} else if (cm.getSquadState(MapleSquadType.ARIANT3) == 1) {
					toSnd += "#L2#Join Ariant Coliseum (3)  Owner: " + cm.getSquadMember(MapleSquadType.ARIANT3, 0).getName()  + " ; Current Members: " + cm.numSquadMembers(MapleSquadType.ARIANT3) + "\r\n";
				}
				if (toSnd.equals("Would you like to join the #eAriant Coliseum Challenge#n?\r\n\r\n#e#r       (Choose an arena)#n#k\r\n#b")) {
                                        cm.sendOk("All arenas are busy now. I suggest you come back later or change channels.");
					cm.dispose();
				} else {
					cm.sendSimple(toSnd);
				}
			} else if (status == 1) {
				switch (selection) {
					case 0 : choice = MapleSquadType.ARIANT1;
							 map = 980010100;
							 break;
					case 1 : choice = MapleSquadType.ARIANT2;
							 map = 980010200;
							 break;
					case 2 : choice = MapleSquadType.ARIANT3;
							 map = 980010300;
							 break;
					default : choice = MapleSquadType.UNDEFINED;
							  map = 0;
							  return;
							  break;
					}
				if (cm.getSquadState(choice) == 0) {
					if (cm.createMapleSquad(choice) != null) {
						cm.getPlayer().dropMessage("Your arena was created. Please wait for people to enter!");
						cm.warp(map, 0);
						cm.dispose();
					} else {
						cm.getPlayer().dropMessage("There was an error. Please report this to a GameMaster as soon as possible.");
						cm.dispose();
					}
				} else if (cm.getSquadState(choice) == 1) {
					if (cm.numSquadMembers(choice) > 5) {
						cm.sendOk("Sorry, the lobby is full now.");
						cm.dispose();
					} else {
						if (cm.canAddSquadMember(choice)) {
							cm.addSquadMember(choice);
							cm.sendOk("You have signed up!");
							cm.warp(map, 0);
							cm.dispose();
						} else {
							cm.sendOk("Sorry, the leader did not allow you to enter .");
							cm.dispose();
						}
					}
				} else {
					cm.sendOk("Something bad happened.");
					cm.dispose();
				}
			}  
		} 
	}
}