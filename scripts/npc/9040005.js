var status = -1;

function action(mode, type, selection) {
    if (mode != 1) {
	cm.sendOk("Boa sorte em terminar a Guild Quest!");
	cm.dispose();
	return;
    }
status++;
    if (status == 0) {
	if (cm.isPlayerInstance()) {
		cm.sendSimple("O que voce gostaria de fazer? \r\n #L0#Sair da Guild Quest#l");
	} else {
		cm.sendOk("Desculpe, mas eu nao posso fazer nada por voce!");
		cm.dispose();
	}
    }
    else if (status == 1) {
	cm.sendYesNo("Tem certeza de que quer fazer isso? Voce nao sera capaz de voltar!");
    }
    else if (status == 2) {
	if (cm.isPlayerInstance()) { 
		cm.getPlayer().getEventInstance().removePlayer(cm.getPlayer());
	}
	cm.dispose();
	return;
    }
}
