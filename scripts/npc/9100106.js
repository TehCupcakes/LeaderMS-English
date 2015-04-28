/*
 * Criado por JavaScriptz
 * LeaderMS 2014
 * Gachapon - Mushroom Shine
 * www.leaderms.com.br
 */

/*            Variaveis         */
var comum = Array(1002089, 1002090, 2040800, 2040801, 2040802, 2043101, 204310, 2044101, 2044102, 2048000, 2048001, 2048002, 2048003, 2048004, 2048005, 2030007, 2030008, 2030009, 2022002, 2020013, 2060003, 2061003, 2070013, 2050004, 2022345, 1082228, 1092022, 1302001, 1402014, 1402013, 1322051, 1322003, 4030012, 4010001, 4010002, 4010003, 4010004, 2000002, 2000003, 2000004, 2000005, 2000006);
var normal = Array(1072262, 1072238, 1472054);
var raro = Array(1012106, 1012058, 1012059, 1012060, 1012061, 1082175, 1082176, 1082177, 1082178, 1082179, 1072239, 1092050, 1302098, 1302099);
/*             Fim              */

/*            Funcao            */
function getRandom(min, max) {
	if (min > max) {
		return(-1);
	}

	if (min == max) {
		return(min);
	}

	return(min + parseInt(Math.random() * (max - min + 1)));
}
/*             Fim              */

/*            Variaveis         */
var icomum = comum[getRandom(0, comum.length - 1)];
var inormal = normal[getRandom(0, normal.length - 1)];
var iraro = raro[getRandom(0, raro.length - 1)];

var chance = getRandom(0, 5);
/*             fim              */


function start() {
    if (cm.haveItem(5451000)) {
        cm.dispose();
    } else if (cm.haveItem(5220000))
        cm.sendYesNo("I see you have a Gachapon ticket. Do you wish to use it?");
    else {
        cm.sendSimple("Welcome to " + cm.getPlayer().getMap().getMapName() + " Gachapon. How can I help you?\r\n\r\n#L0#What is Gachapon?#l\r\n#L1#Where can you buy Gachapon Tickets?#l");
    }
}

function action(mode, type, selection){
    if (mode == 1 && cm.haveItem(5220000)) {
        cm.gainItem(5220000, -1);
        if (chance > 0 && chance <= 2) {
	cm.gainItem(icomum, 1);
	} else if (chance >= 3 && chance <= 4) {
	cm.gainItem(inormal, 1);
	} else {
	cm.gainItem(iraro, 1);
	}
        cm.dispose();
    } else {
        if (mode > 0) {
            status++;
            if (selection == 0) {
                cm.sendNext("By playing Gachapon you can win rare scrolls, equipment, chairs, mastery books, and other cool items! All you need is a #bGachapon Ticket#k to be able to get some of these rare items.");
            } else if (selection == 1) {
                cm.sendNext("Gachapon Tickets are available in the #rCash Shop#k and can be purchased with NX or MaplePoints. Click the red SHOP button at the bottom right of the screen to visit the #rCash Shop#k, where you will be able to buy your tickets.");
                cm.dispose();
            } else if (status == 2) {
                cm.sendNext("You will find a variety of items from " + cm.getPlayer().getMap().getMapName() + " Gachapon, but you will likely find several items and scrolls related to the city of " + cm.getPlayer().getMap().getMapName() + ".");
                cm.dispose();
            }
        }
    }
}
