/*
 * Criado por JavaScriptz
 * LeaderMS 2014
 * Gachapon - Sleep
 * www.leaderms.com.br
 */

/*            Variaveis         */
var comum = Array(2000005, 1032032, 1442018, 2044701, 2044702, 2044602, 2040101, 2043002, 2040517, 2040902, 2040705, 2040707, 2040708, 2044001, 2044002, 1442039, 2101000, 2101001, 2041006, 2041007, 2041008, 2041009, 2041010, 2041011, 2040803, 2040804, 2040805, 2040532, 2040534, 2040024, 2040026, 2040027, 2040028, 2040030, 2040031, 2040000, 2040001, 2040002, 2000001, 2000002, 2000003, 2000004, 2044302, 2044404, 2044405, 2044401, 2044402, 4010001, 4010002, 4010003, 4010004, 4010005, 4030012);
var normal = Array(2000005, 1032032, 1442018, 2044701);
var raro = Array(2040513, 2100000, 2040025, 2040029);
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
