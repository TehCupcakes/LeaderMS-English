/*
 * Criado por JavaScriptz
 * LeaderMS 2014
 * Gachapon - Mushroom Shine
 * www.leaderms.com.br
 */

/*            Variaveis         */
var comum = Array(1002130, 1060033, 1082147, 01072321, 1472017, 1051030, 2040604, 2040605, 2040608, 2040606, 2041038, 2041039, 2041030, 2041041, 2044704, 2044604, 2043304, 2044903, 2040011, 2043105, 2043006, 2040510, 2040511, 2040531, 2040906, 2040907, 2040932, 2040714, 2040716, 2044304, 2040407, 2044013, 2044105, 2044204, 1040084, 1332030, 1072263, 1402013, 1332020, 1412004, 1002395, 1082148, 1052122, 2040200, 2040201, 2043019, 1060046, 1402007);
var normal = Array(1082145, 1332053, 2040411, 2044405, 2043005, 2044804, 2044904, 2044505, 2040611, 1332032);
var raro = Array(1082149, 2040305, 2040811, 2040814, 2040518, 2040519, 2040533, 2040922, 2040916, 2044305, 1102041, 1102042, 1002586, 1102040, 1102086, 1102084);
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
