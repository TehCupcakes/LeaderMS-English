/* Author: Xterminator
	NPC Name: 		Peter
	Map(s): 		Maple Road: Entrance - Mushroom Town Training Camp (3)
	Description: 	Takes you out of Entrace of Mushroom Town Training Camp
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
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("Voce terminou todos os seus treinamentos, otimo trabalho. Voce parece estar pronto para comecar a jornada de imediato! Bom, eu vou deixar voce passar para o proximo lugar.");
		} else if (status == 1) {
			cm.sendNextPrev("Mas lembre-se, uma vez que voce sair daqui, nao podera mais voltar. La fora a um mundo cheio de monstros, entao cuidado!");
		} else if (status == 2) {
			cm.warp(40000, 0);
			//cm.gainExp(3 * cm.getC().getChannelServer().getExpRate());
			//cm.gainExp(3);
			cm.dispose();
		} 
	}
}