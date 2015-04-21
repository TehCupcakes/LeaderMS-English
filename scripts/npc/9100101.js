/*
 * Criado por JavaScriptz
 * LeaderMS 2014
 * Gachapon - Ellinia
 * www.leaderms.com.br
 */

/*            Variaveis         */
var comum = Array(1312007, 1332009, 1422001, 1442000, 2000005, 2022113, 2043019, 2040824, 1002093, 1002143, 2000013, 2000014, 2000015, 2000016, 2000017, 2000018, 2022003, 2022000, 4030012, 4020000, 4020003, 4010004, 4003000);
var normal = Array(1422005, 1312012, 1312010, 2043201, 2041038, 2041040, 2040931);
var raro = Array(1312015, 2043801, 2043701, 2043702, 2043001, 2043002, 2041036, 2041037, 2041035, 2041039, 2041041, 2040804, 2040805, 2040704, 2040705);
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

