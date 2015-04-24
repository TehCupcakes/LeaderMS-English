/* Pedro -  El Nath: Chief's Residence (211000001)
   Made By Cygnus
*/

importPackage(Packages.client);

status = -1;
var job;
var sel;
actionx = {"Mental" : false, "Physical" : false};

function start() {
    if (!(cm.getJob().equals(MapleJob.BRAWLER) ||
                cm.getJob().equals(MapleJob.GUNSLINGER))){
                cm.sendOk("May #rSimplicity#k be with you!");
                cm.dispose();
                return;
    }else if (cm.getLevel() <=69){
        cm.sendOk("You're not ready yet...");
        cm.dispose();
        return;
    }
    if (cm.haveItem(4031058))
        actionx["Mental"] = true;
    else if (cm.haveItem(4031057))
        actionx["Physical"] = true;
    cm.sendSimple("Can I help you#b " + cm.getPlayer().getName() + "#k?\r\n#L0#I want to make the 3rd job advancement.");
    //\r\n#L1#Please allow me to do the Zakum Dungeon Quest
}


function action(mode, type, selection){
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    } else if ((mode != 1) || (status > 2 && !actionx["Mental"]) || (status > 3)) {
        if (mode == 0 && type == 1)
            cm.sendNext("Make up your mind.");
            cm.dispose();
            return;
    }
    if (actionx["Mental"]) {
        if (status == 0)
           cm.sendNext("Great job completing the mental part of the test. You have wisely answered all the questions correctly. I must say, I am quite impressed with the level of wisdom you have displayed there. Please hand me the necklace first, before we take on the next step.");
        else if (status == 1)
       cm.sendYesNo("Great! You may now become the kind of Pirate you've always dreamed of! With newfound power and stellar new skills, your power has endless possibilities! Before we proceed, however, please check and see that you have used up all your Skill Points. You must use up all the SP's you've earned up to Level 70 in order for you to make the 3rd job advancement. Since you've already chosen which of the two paths you wanted to take for Pirates for the 2nd job advancement, this will not require much thought. Do you wish to make the job advancement right now?");
        else if (status == 2) {
            if (cm.getPlayer().getRemainingSp() > 0)
                if (cm.getPlayer().getRemainingSp() > (cm.getLevel() - 70) * 3) {
                    cm.sendNext("Please, use all your SP before continuing.");
                    cm.dispose();
                    return;
                }
            cm.completeQuest(100102);
            if (cm.getJob().equals(MapleJob.BRAWLER)) {
                cm.changeJob(MapleJob.MARAUDER);
                cm.getChar().gainAp(5);
                cm.getChar().gainSp(1);
                cm.gainItem(4031058, -1);
                cm.sendNext("Great! You are now a #bMarauder#k. As a Marauder, you will learn some of the most sophisticated skills related to melee-based attacks. #bEnergy Charge#k is a skill that allows you to store your power and the damage you received into a special form of energy. Once this ball of energy is charged, you may use #bEnergy Blast#k to apply maximum damage against your enemies, and also use #bEnergy Drain#k to steal your enemy's HP to recover your own. #bTransformation#k will allow you to transform into a superhuman being with devastating melee attacks, and while transformed, you can use #bShockwave#k to cause a mini-earthquake and inflict massive damage to your enemies.");
            }
            if (cm.getJob().equals(MapleJob.GUNSLINGER)) {
                cm.changeJob(MapleJob.OUTLAW);
                cm.getChar().gainAp(5);
                cm.getChar().gainSp(1);
                cm.gainItem(4031058, -1);
            cm.sendNext("Great! You are now an #bOutlaw#k. As an Outlaw, you will become a true pistolero, a master of every known Gun attack, as well as a few other skills to help you vanquish evil. #bBurst Fire#k is a more powerful version of Double Shot, shooting more bullets and causing more damage at the same time. You also now have the ability to summon a loyal #bOctopus#k and the swooping #bGaviota#k as your trusty allies, while attacking your enemies using #bBullseye#k. You can also use element-based attacks by using #rFlamethrower#k and #bIce Splitter#k.")
            }
        } else if (status == 3) {
        cm.sendNext("I have also given you Skill Points and Ability Points, so please apply them when you get a chance. Now that you have made the job advancement, I believe you have now become a formidable Pirate. Remember, though, that this will open up a whole new set of difficult journeys for you to take. Come see me when you feel like you have reached the pinnacle of your current Pirate skills, and have nothing else to train for. Then, and only then, will I help you obtain ultimate power.")
        }
    } else if (actionx["Physical"]){
        if (status == 0)
            cm.sendNext("Great job completing the mental part of the test. You have wisely answered all the questions correctly. I must say, I am quite impressed with the level of wisdom you have displayed there. Please hand me the necklace first, before we take on the next step.");
        else if (status == 1) {
            if (cm.haveItem(4031057)) {
                cm.gainItem(4031057, -1);
                cm.completeQuest(100101);
                cm.startQuest(100102)
            }
            cm.sendNext("Here's the 2nd half of the test. This test will determine whether you are smart enough to take the next step towards greatness. There is a dark, snow-covered area called the Holy Ground at the snowfield in Ossyria, where even the monsters can't reach. On the center of the area lies a huge stone called the Holy Stone. You'll need to offer a special item as the sacrifice, then the Holy Stone will test your wisdom right there on the spot.");
        } else if (status == 2)
            cm.sendOk("You'll need to answer each and every question given to you with honesty and conviction. If you correctly answer all the questions, then the Holy Stone will formally accept you and hand you #b#t4031058##k. Bring back the necklace, and I will help you to the next step forward. Good luck.");
        } else if (cm.getQuestStatus(100102).equals(MapleQuestStatus.Status.STARTED)){
        cm.sendOk("Go, talk with the #b#p2030006##k and bring me #i4031058# \r\n#b#t4031058##k.");
        cm.dispose();
            } else if (cm.getQuestStatus(100101).equals(MapleQuestStatus.Status.COMPLETED)) {
            cm.sendOk("Now, go see #b#p2030006##k and bring me #b#i4031058#\r\n#t4031058##k.You'll need to answer the #b#p2030006#s#k each and every question with honesty and conviction. Bring back the necklace, and I will help you to the next step forward. Good luck.");
        cm.dispose();
    } else if (cm.getQuestStatus(100100).equals(MapleQuestStatus.Status.STARTED)) {
                    cm.sendOk("The mental half of the test can only start after you pass the physical part of the test. #b#t4031057##k will be the proof that you have indeed passed the test. I'll let \r\n#b#p1090000##k know in advance that you're making your way there, so get ready. It won't be easy, but I have the utmost faith in you. Good luck.");    
        cm.dispose();
    } else {
        if (sel == undefined)
            sel = selection;
        if (sel == 0){
            if (cm.getPlayer().getLevel() >= 70 && cm.getJob().equals(MapleJob.BRAWLER) || cm.getJob().equals(MapleJob.GUNSLINGER)) {
                if (status == 0)
            cm.sendYesNo("Hmm. So you want to be a stronger pirate by making the 3rd job advancement? First i can say congratulations. Few have this level of dedication. I can certainly make you stronger with my powers, but I'll need to test your strength to see if your training has been adequate. Many come professing their strength, few are actually able to prove it. Are you ready for me to test your strength?");
                else if (status == 1) {
            cm.sendNext("Great! Now you will have to prove your strength and intelligence. Let me first explain the strength test. Do you remember #bKyrin#k from The Nautilus who helped you make the 1st and 2nd job advancements? Go to see her and she will give you a task to fulfill. Complete the task and you will receive #bThe Necklace of Strength#k from Kyrin.");
                       cm.startQuest(100100); 
               } 
            }
        } else {
            cm.sendNext("Not done yet.");
            cm.dispose();
        }
    }
}