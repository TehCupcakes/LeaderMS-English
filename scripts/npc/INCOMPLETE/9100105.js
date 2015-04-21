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
        cm.sendYesNo("Percebo que voce possui um bilhete do Gachapon, deseja usalo?");
    else {
        cm.sendSimple("Bem-vindo ao " + cm.getPlayer().getMap().getMapName() + " Gachapon. Como posso ajuda-lo?\r\n\r\n#L0#O que e Gachapon?#l\r\n#L1#Onde voce pode comprar bilhetes Gachapon?#l");
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
                cm.sendNext("Jogando no Gachapon voce pode ganhar scrolls raros, equipamentos, cadeiras, livros de maestria, e outros artigos legais! Tudo que voce precisa e de um #bGachapon Ticket#k para poder obter algum desses items raros.");
            } else if (selection == 1) {
                cm.sendNext("Bilhete Gachapon estao disponiveis no #rCash Shop#k e podem ser adquiridos atraves do NX ou MaplePoints. Clique no SHOP vermelho no canto inferior direito da tela para visitar o #rCash Shop #konde voce podera comprar bilhetes.");
                cm.dispose();
            } else if (status == 2) {
                cm.sendNext("Voce vai encontrar uma variedade de itens da " + cm.getPlayer().getMap().getMapName() + " Gachapon, mas voce provavelmente vai encontrar varios itens e pergaminhos relacionados a cidade de " + cm.getPlayer().getMap().getMapName() + ".");
                cm.dispose();
            }
        }
    }
}