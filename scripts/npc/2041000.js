 /* Author: Xterminator
	NPC Name: 		Tian
	Map(s): 		Ludibrium: Station<Orbis> (220000110)
	Description: 		Ludibrium Ticketing Usher
*/
var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
	if (status >= 0 && mode == 0) {
		cm.sendNext("You must have some business to take care of here, right?");
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
		cm.sendYesNo("We are preparing to depart for Orbis. If you need to take care of some things, I suggest you do that first before getting on board. Do you still wish to get on?");
	} else if (status == 1) {
		if (cm.haveItem(4031045)) {
			cm.gainItem(4031045, -1);
			cm.warp(200000100, 0);
			cm.dispose();
		} else {
			cm.sendNext("Oh, no... You do not have a ticket with you. I can't let you on without one. Please buy a ticket at the ticket sales guide...");
			cm.dispose();
			}		
		}
	}
}