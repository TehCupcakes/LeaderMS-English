/* Author: Xterminator
	NPC Name: 		Peter
	Map(s): 		Maple Road: Entrance - Mushroom Town Training Camp (3)
	Description: 	Takes you out of Entrace of Mushroom Town Training Camp
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
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("Great job, you finished all your training! You seem to be ready to start the journey. Well, I'll let you go to the next place.");
		} else if (status == 1) {
			cm.sendNextPrev("But remember, once you leave here, you can not come back. I will send you away to a world full of monsters, so be careful!");
		} else if (status == 2) {
			cm.warp(40000, 0);
			//cm.gainExp(3 * cm.getC().getChannelServer().getExpRate());
			//cm.gainExp(3);
			cm.dispose();
		} 
	}
}