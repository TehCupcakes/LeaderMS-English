/*Kyrin -  Pirate Test room 108000500-108000503
    Made by Cygnus
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
            if ((cm.getPlayer().getMapId() == 108000502) && cm.haveItem(4031856,15)) {
                cm.sendNext("Ohhh... So you managed to gather up #r15 Potent Power Crystals#k! Wasn't it tough? That's amazing... Alright then, now let's talk about The Nautilus.");
            } else if ((cm.getPlayer().getMapId() == 108000502) && !(cm.haveItem(4031856,15))) {
                cm.sendNext("You will have to collect me #v4031856##r15 Potent Power Crystals#k. Good luck.");
                status = 5
            } else if ((cm.getPlayer().getMapId() == 108000500) && cm.haveItem(4031857,15))
               cm.sendNext("Ohhh... So you managed to gather up #b15 Potent Wind Crystal#k! Wasn't it tough? That's amazing... Alright then, now let's talk about The Nautilus.");
                else if ((cm.getPlayer().getMapId() == 108000500) && !(cm.haveItem(4031857,15))){
               cm.sendNext("You will have to collect me #v4031857##b15 Potent Wind Crystal#k. Good luck.");
                  status = 5
            } else {
                cm.sendOk("Something went wrong.")
                cm.dispose();
            }
        } else if (status == 1) {
             cm.sendNextPrev("These crystals can only be used here, so I'll just take them back.");
        } else if (status == 2) {
            cm.warp(120000101, 0);
            cm.dispose();
        } else if (status == 6) {
            cm.dispose
        }
    }
}