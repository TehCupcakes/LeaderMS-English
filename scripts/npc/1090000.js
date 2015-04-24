/* Kyrin - The Nautilus - Navigation Room(120000101)
    Made by Cygnus
*/

importPackage(Packages.client);

var status = 0;
var job;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 2) {
            cm.sendOk("Make up your mind and visit me again.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            if (cm.getJob().equals(MapleJob.BEGINNER)) {
                if (cm.getLevel() >= 10 && cm.getChar().getDex() >= 20)
                    cm.sendNext("So you decided to become a #rPirate#k?");
                else {
                    cm.sendOk("Train a bit more and I can show you the way of the #rPirate#k.")
                    cm.dispose();
                }
            } else {
                if (cm.getLevel() >= 30 
                    && cm.getJob().equals(MapleJob.PIRATE)) {
                        if (cm.getQuestStatus(2192).equals(MapleQuestStatus.Status.COMPLETED)) {
                            status = 20;
                            cm.sendNext("I knew you'd pass the test as expected. You had some impressive moves in there. Not bad at all! Now as promised, you will become a #bGunslinger#k.");
                        } 
                     else if (cm.getQuestStatus(2191).equals(MapleQuestStatus.Status.COMPLETED)) {
                            status = 30;
                            cm.sendNext("I knew you'd pass the test as expected. You had some impressive moves in there. Not bad at all! Now as promised, you will become a #bBrawler#k.");
                        }                        
                     else if (cm.getQuestStatus(2191).equals(MapleQuestStatus.Status.STARTED)) {
                        status = 15;
						cm.sendYesNo("Would you like to take the test for the second job advancement?");
                    } else if (cm.getQuestStatus(2192).equals(MapleQuestStatus.Status.STARTED)) {
                        status = 10;
						cm.sendYesNo("Would you like to take the test for the second job advancement?");
                    } else {
                        cm.sendOk("You need to start my quest before I can allow you to advance any further...")
                        cm.dispose();
                    }
                } else if ((cm.getJob().equals(MapleJob.BRAWLER) ||    cm.getJob().equals(MapleJob.GUNSLINGER)) && cm.getQuestStatus(100100).equals(MapleQuestStatus.Status.STARTED)) {
                    if (cm.getQuestStatus(100101).equals(MapleQuestStatus.Status.COMPLETED) && (cm.haveItem(4031057))) {
                        cm.sendOk("Alright, now bring #b#i4031057##t4031057##k to #bPedro#k.");
                    } else if (cm.haveItem(4031059,1)) {
                        cm.sendNext("Wow... You beat my the other self and brought #b#t4031059##kto me. Good! this surely proves your strength. In terms of strength, you are ready to advance to 3rd job. As I promised, I will give #b#t4031057##k to you. Give this necklace to #b#p2020013##k in Ossyria and you will be able to take second test of 3rd job advancement. Good Luck!");
                        cm.completeQuest(100101);
                        cm.gainItem(4031059, -1);
                        cm.gainItem(4031057)
                        cm.dispose();
                    }  else if (cm.getQuestStatus(100101).equals(MapleQuestStatus.Status.COMPLETED) && !(cm.haveItem(4031057))) {
                    cm.sendOk ("Why would you dispose of such an important necklace? \r\nOh well, here, have another one.")
                    cm.gainItem(4031057)
                    } else if (cm.getQuestStatus(100101).equals(MapleQuestStatus.Status.STARTED)) {
                        cm.sendOk("There is a secret passage near the ant tunnel. If you go into the passage, you will meet my other self. Beat him and bring the#b #t4031059##k to me.");
                    } else {
                        cm.sendOk("I was expecting you. A few days ago, I heard about you from #b#p2020013##k in Ossyria. Well... I'd like to test your strength. There is a secret passage near the ant tunnel. Nobody but you can go into that passage. If you go into the passage, you will meet my other self. Beat him and bring the #b#t4031059##k to me.");
                        cm.startQuest(100101);
                    }
                    cm.dispose();
                } else {
                    cm.sendOk("You have chosen wisely.");
                    cm.dispose();
                }
            }
        } if (status == 1) {
            cm.sendNextPrev("It is an important and final choice. You will not be able to turn back.");
        } else if (status == 2) {
            cm.sendYesNo("Do you want to become a #rPirate#k?");
        } else if (status == 3) {
            if (cm.getJob().equals(MapleJob.BEGINNER))
                cm.changeJob(MapleJob.PIRATE);
            cm.getChar().gainSp(1);
            cm.gainItem(2330000,1000);
            cm.gainItem(1492000,1);
            cm.gainItem(1482000,1);
            cm.sendOk("So be it! Now go, and go with pride.");
            cm.dispose();
        } else if (status == 11) {
            cm.sendNext("Okay, now I'll take you to the test room. Here are the instructions: defeat the Octopirates and gather up #b15 Potent Wind Crystals#k. The Octopirates you'll see here are highly trained and very storng, so I suggest you really buckle down and get ready for this.");
        } else if (status == 12) {
            cm.sendNextPrev("Oh, and for the sake of training Gunslingers, those Octos will not be affected unless hit with #bDouble Shot#k. And one more thing, when you enter the test room, I'll remove all the Potent Power Crystal you have. Yes, you'll be starting off from scratch.");
        } else if (status == 13) {
            cm.removeAll(4031857);
            cm.removeAll(4031856);
            cm.warp(108000500);
        } else if (status == 16) {
            cm.sendNext("Okay, now I'll take you to the test room. Here are the instructions: defeat the Octopirates and gather up #b15 Potent Power Crystals#k. The Octopirates you'll see here are highly trained and very storng, so I suggest you really buckle down and get ready for this.")
        } else if (status == 17) {
            cm.sendNextPrev("Oh, and for the sake of training Brawlers, those Octos will not be affected unless hit with #bFlash Fist#k. And one more thing, when you enter the test room, I'll remove all the Potent Power Crystal you have. Yes, you'll be starting off from scratch.");
        } else if (status == 18) {
            cm.removeAll(4031857);
            cm.removeAll(4031856);
            cm.warp(108000502);
        } else if (status == 21) {
            cm.sendNext("From here on out, you are a #bGunslinger#k. Gunslingers are notable for their long-range attacks with sniper-like accuracy and of course, using Guns as their primary weapon. You should continue training to truly master your skills. If you are having trouble training, I'll be there to help.");
            cm.changeJob(MapleJob.GUNSLINGER);
            cm.getChar().gainAp(5);
            cm.getChar().gainSp(1);
            cm.removeAll(4031857);
        } else if (status == 22) {
            cm.sendNext("I have just given you a skill book that entails the skills for Gunslingers, one that you'll find very helpful. I also boosted your MaxHP and MaxMP. Check it out for yourself.");
        } else if (status == 23) {
            cm.sendNextPrev("I have given you a little bit of #bSP#k, so I suggest you open the #bskill menu#k right now. You'll be able to enhance your newly-acquired 2nd job skills. Beware that not all skills can be enhanced from the get go. There are some skills that you can only acquire after mastering basic skills.");                    
        } else if (status == 24) {
            cm.sendNextPrev("Gunslingers are deadly at range combat, but that doesn't mean they have the right to bully the weak. Gunslingers will need to use their immense power in positive ways, and that is actually harder than just training to gain strength. I hope you follow this creed as you leave your mark in this world as a Gunslinger. I will see you when you have accomplished everything you can as a Gunslinger. I'll be waiting for you here.");    
        }    else if (status == 31) {
			cm.sendNext("From here on out, you are a #bBrawler#k. Brawlers rule the world with the power of their bare firsts... Which means they need to train the body more than others. If you have any trouble training, I'll be more than happy to help.");
            cm.changeJob(MapleJob.BRAWLER);
            cm.getChar().gainAp(5);
            cm.getChar().gainSp(1);
            cm.removeAll(4031856);
        } else if (status == 32) {
            cm.sendNext("I have just given you a skill book that entails Brawler skills, you'll find it very helpful. I also boosted your MaxHP and MaxMP. Check it out for yourself.");
        } else if (status == 33) {
            cm.sendNextPrev("I have given you a little bit of #bSP#k, so I suggest you open the #bskill menu#k right now. You'll be able to enhance your newly-acquired 2nd job skills. Beware that not all skills can be enhanced from the get go. There are some skills that you can only acquire after mastering basic skills.");
        } else if (status == 34) {
            cm.sendNextPrev("Brawlers need to be a powerful force, but that doesn't mean they have the right to bully the weak. True Brawlers use their immense power in positive ways, which is much harder than just training to gain strength. I hope you follow this creed as you leave your mark in this world as a Brawler. I will see you when you have accomplished everything you can as a Brawler. I'll be waiting for you here.");        
        }
    }
}