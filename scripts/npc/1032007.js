/* Author: Xterminator
	NPC Name: 		Joel
	Map(s): 		Victoria Road : Ellinia Station (101000300)
	Description: 		Ellinia Ticketing Usher
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
		cm.sendYesNo("Hello, I'm responsible for the selling tickets for the boat ride to Orbis on Ossyria Island. The journey to Orbis will cost you #B5000 mesos#k. Are you sure you want to buy a #bTicket to Orbis (Normal)#K?");
	} else if (status == 1) {
		if (cm.getPlayer().getMeso() < 5000) {
			cm.sendNext("Are you sure you have #B5000 mesos#k? If so, please check your inventory and make sure your etc. tab is not full.");
			cm.dispose();
		} else {
			cm.gainMeso(-5000);
			cm.gainItem(4031045, 1);
			cm.dispose();
			}		
		}
	}
}