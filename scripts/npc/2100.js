/* Author: Xterminator
	NPC Name: 		Sera
	Map(s): 		0, 1, 3
	Description: 	First NPC
*/
importPackage(Packages.client);

var status = 0;
var yes = 0;

function start() {
    if (!cm.getJob().equals(MapleJob.BEGINNER) && !cm.getChar().gmLevel() > 0)
        cm.getC().disconnect();
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (cm.getChar().getMapId() == 0 || cm.getChar().getMapId() == 3) {
        if (mode == -1)
            cm.dispose();
        else {
            if (status == -1 && mode == 0) {
                cm.sendNext("Please talk to me again when you finally made your decision.");
                cm.dispose();
                return;
            } else if (status >= 0 && mode == 0) {
                yes = 1;
                cm.sendYesNo("Do you really want to start your journey right away?");
            }
            if (mode == 1)
                status++;
            else
                status--;
            if (status == 0) {
                if (yes == 1) {
                    status = 2;
                    cm.sendNext("It seems like you want to start your journey without taking the training program. Then, I will let you move on to the training ground. Be careful.");
                } else
                    cm.sendYesNo("Welcome to the world of MapleStory. The purpose of this training camp is to help beginners. Would you like to enter this training camp? Some people start their journey without taking the training program. But I strongly recommend you take the training program first.");
            } else if (status == 1)
                cm.sendNext("Ok then, I will let you enter the training camp. Please follow your instructor's lead.");
            else if (status == 2) {
                cm.warp(1, 0);
                cm.dispose();
            } else if (status == 3) {
                cm.warp(40000);
                cm.dispose();
            }
        }
    } else {
        if (mode < 1)
            cm.dispose();
        else {
            status++;
            if (status == 0) 
                cm.sendNext("This is the image room where your first training program begins. In this room, you will have an advance look into the job of your choice.");
            else if (status == 1)
                cm.sendPrev("Once you train hard enough, you will be entitled to occupy a job. You can become a Bowman in Henesys, a Magician in Ellinia, a Warrior in Perion, a Thief in Kerning City, and a Pirate in Nautilus Port.");
            else
                cm.dispose();
        }
    }
}