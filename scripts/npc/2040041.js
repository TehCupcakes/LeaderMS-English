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
		cm.sendNext("Hello and welcome to the sixth stage of Ludibrium Party Quest. Look here, and you'll see a number of boxes. All you have to do, is find the right combination, and press up on it to teleport up. But, if you get it wrong, you will be teleported back down to the bottom. Good Luck!");
		cm.dispose();
		}
	}
}