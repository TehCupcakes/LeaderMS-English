/*Amos the Strong - Entrance
**9201043
**@author Jvlaple
*/

var status = 0;
var MySelection = -1;

importPackage(Packages.client);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action() {
 cm.sendOk("This quest is currently unavailable. Report this ID: 9201043"); 
 cm.dispose();
}


/*
function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 0 && mode == 0) {
			cm.sendOk("Ok come back when your'e ready.");
			cm.dispose();
			return;
		}
		if (mode == 1) {
			status++;
		}
		else {
			status--;
		}
		if (status == 0) {
			cm.sendSimple("My name is Amos the Strong. What would you like to do? r\n#b#L0#Enter the Amorian Challenge!!#l\r\n#L1#Trade 10 Keys for a Ticket!#l\r\n#k");
		} else if (status == 1 && selection == 0) {
			if (cm.haveItem(4031592, 1) && cm.getPlayer().isMarried()== 0) {
				cm.sendYesNo("So you would like to enter the #bEntrance#k?");
				MySelection = selection;
			} else {
				cm.sendOk("You must have an Entrance Ticket to enter, and you have to be married.");
				cm.dispose();
			}
		} else if (status == 1 && selection == 1) {
			if (cm.haveItem(4031593, 10)) {
				cm.sendYesNo("So you would like a Ticket?");
				MySelection = selection;
			} else {
				cm.sendOk("Please get me 10 Keys first!");
				cm.dispose();
			}
		} else if (status == 2 && MySelection == 0) {
			cm.warp(670010100, 0);
			cm.gainItem(4031592, -1)
			cm.dispose();
		} else if (status == 2 && MySelection == 1) {
			cm.gainItem(4031593, -10);
			cm.gainItem(4031592, 1);
			cm.dispose();
		}
	}
}*/