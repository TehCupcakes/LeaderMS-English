/* Author: Xterminator
	NPC Name: 		Ramini
	Map(s): 		Orbis: Cabin<To Leafre> (200000131)
	Description: 		Orbis Ticketing Usher
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
		cm.sendYesNo("It seems that there is still plenty of room for this ride. Please have your ticket ready so I can let you on. The journey will be long, but you will get to your destination safely. What do you think? Do you want take this ride?");
	} else if (status == 1) {
		if (cm.haveItem(4031331)) {
			cm.gainItem(4031331, -1);
			cm.warp(240000100, 0);
			cm.dispose();
		} else {
			cm.sendNext("Oh, no... It looks like you don't have a ticket with you. I can't let you on without it. Please buy the ticket at the ticket from the ticket guide.");
			cm.dispose();
			}		
		}
	}
}