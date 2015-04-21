/* Author: Xterminator
	NPC Name: 		Tommie
	Map(s): 		Leafre: Cabin<To Orbis> (240000110)
	Description: 		Leafre Ticketing Usher
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
	if (status >= 0 && mode == 0) {
		cm.sendNext("Voce deve ter alguns negocios para cuidar aqui, certo?");
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
		cm.sendYesNo("Parece que nao ha muito espaco para esse passeio. Por favor, tenham o seu bilhete pronto para que eu possa deixa-lo ir. A viagem vai ser longa, mas voce vai chegar ao seu destino bem. O que voce acha? Voce quer ir com esse passeio?");
	} else if (status == 1) {
		if (cm.haveItem(4031045)) {
			cm.gainItem(4031045, -1);
			cm.warp(200000100, 0);
			cm.dispose();
		} else {
			cm.sendNext("Ah, nao ... Eu acho que voce nao tem o bilhete com voce. Eu nao posso deixa-lo ir sem ele. Por favor, compre o bilhete no guiche de venda de bilhetes.");
			cm.dispose();
			}		
		}
	}
}