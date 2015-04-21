/*
 * Criado por JavaScriptz
 * LeaderMS 2014
 * Gachapon - Kerning
 * www.leaderms.com.br
 */

/*            Variaveis         */
var comum = Array(1051037, 1472026, 4130014, 1332040, 2000004, 1041060, 1472003, 1060086, 1060079, 1322008, 1002005, 1002023, 1060087, 1002085, 1472009, 1302021, 2000005, 1322022, 1060051, 1041080, 1040109, 1302013, 2040201, 2040402, 1092018, 1332017, 1332034, 1051031, 1040081, 4030012, 2043301, 1322009, 1322010, 1322011, 1322012, 2000000, 2000001, 2000002, 2000003, 2040316, 2040318, 2040319, 2040320, 2040322, 2040324, 2040326, 2040328, 1002212, 1102000, 1102001, 1102002, 1102043, 4010000, 4010001, 4010002, 4010003, 4010004, 4010005, 4010006, 1332022, 1472019);
var normal = Array(2043305, 2040317, 2040321);
var raro = Array(2040323, 1432015, 1432016, 1432017);
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

