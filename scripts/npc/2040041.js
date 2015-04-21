/*
 *Aqua Ballon - Stage 6 of LPQ =D
  *@author Jvlaple
  */

importPackage(Packages.client);

var status;

function start() {
	status = -1;
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
	if (status == 0) {
		cm.sendNext("Hello! Welcome to the 6th stage. Here you will see boxes with numbers written on them. Stand on top of a box and press the UP ARROW, then carry the box to the correct location. I will give the leader of the party a clue about how to pass this stage. I will do this on #btwo times#k, and the leader must keep track of each step.\r\nOnce you reach the top, you will find the portal to the next stage. When all of your party has passed through the portal, the stage will be complete. Everything depends on remembering the correct boxes. Once I have given you advice #btwo times#k I can no longer help you. Good luck!");
		cm.dispose();
		}
	}
}