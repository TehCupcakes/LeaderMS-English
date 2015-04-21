/* Author: Xterminator
	NPC Name: 		Asesson
	Map(s): 		Ariant: Ariant Station Platform (260000100)
	Description: 	Ariant Ticketing Usher
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
		cm.sendNext("Voce deve ter algum negocio para cuidar aqui, certo?");
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
		cm.sendYesNo("Parece que ha espaco para este passeio. Por favor, tenham o seu bilhete pronto para que eu possa deixá-lo entrar. A viagem sera longa, mas voce vai chegar ao seu destino bem. O que voce acha? Voce quer ir ao passeio?");
	} else if (status == 1) {
		if (cm.haveItem(4031045)) {
			cm.gainItem(4031045, -1)
			cm.warp(200000100, 0);
			cm.dispose();
		} else {
			cm.sendNext("Ah, nao ... Eu nao acho que voce tem o bilhete com voce. Eu nao posso deixa-lo ir sem ele. Por favor, comprar o bilhete no guiche de bilheteria.");
			cm.dispose();
			}
		}
	}
}