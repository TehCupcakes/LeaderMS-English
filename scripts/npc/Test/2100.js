/* Author: Xterminator
	NPC Name: 		Sera
	Map(s): 		0, 1, 3
	Description: 	First NPC
*/
importPackage(Packages.client);

var status = 0;
var yes = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1)
            cm.dispose();
        else {
            if (status == -1 && mode == 0) {
                cm.sendNext("Yo yo yo, wassup?!");
                cm.dispose();
                return;
            } else if (status >= 0 && mode == 0) {
                yes = 1;
                cm.sendYesNo("This is a test!");
            }
            if (mode == 1)
                status++;
            else
                status--;
            if (status == 0) {
                if (yes == 1) {
                    status = 2;
                    cm.sendNext("Well, dat be coo.");
                } else
                    cm.sendYesNo("Welcome and all that jazz.");
            } else if (status == 1)
                cm.sendNext("Let'za go!");
            else if (status == 2) {
                cm.warp(1, 0);
                cm.dispose();
            } else if (status == 3) {
                cm.warp(40000);
                cm.dispose();
            }
        }
}