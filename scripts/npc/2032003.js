/* Lira - Zakum Jump Quest NPC 
 * @author Manfred
 * For use with Zakum PQ.
 */
 
//4031062 - Breath of lava

var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.sendOk("Ok. Seems like you don't want the #rBreath of Lava#k...");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
            cm.sendNext("Congratulations on getting this far!  Well, I suppose I'd better give you your #bBreath of Fire#k.  You've certainly earned it!");
		} else if (status == 1) {
			cm.gainItem(4031062, 1);
			cm.addZakumLevel();
			cm.getPlayer().saveToDB(true, true);
			cm.warp(211042300, 0);
			cm.dispose();
		}
	}
}