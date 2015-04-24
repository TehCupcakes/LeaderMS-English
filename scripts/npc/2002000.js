/*
 * LeaderMS Revision
 * @autor Gabriel A. <javascriptz@leaderms.com.br>
 * Rupi - 2002000
*/

function start() {
    cm.sendYesNo("Do you want to leave #bHappyville#k?");
}

function action(mode, type, selection) {
    if (mode != 1) {
		cm.dispose();
	} else {
		var location = cm.getPlayerVariable("HV_map");
        cm.warp(location,0);
		cm.dispose();
	}
}