/* Author: Xterminator
	NPC Name: 		Tommie
	Map(s): 		Leafre: Cabin<To Orbis> (240000110)
	Description: 		Leafre Ticketing Usher
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
		cm.sendYesNo("It seems like there is still plenty of space on this ride. Please have your ticket ready so I can let you get on. The journey will be long, but you will get to your destination safely. What do you think? Do you want to go on this ride?");
	} else if (status == 1) {
		if (cm.haveItem(4031045)) {
			cm.gainItem(4031045, -1);
			cm.warp(200000100, 0);
			cm.dispose();
		} else {
			cm.sendNext("Oh, no... It looks like you do not have a ticket with you. I cannot let you on without it. Please buy the ticket at the ticket sales guide.");
			cm.dispose();
			}		
		}
	}
}