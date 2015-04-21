importPackage(Packages.server);

var status;
var choice;

function start() {
	status = -1;
	action(1, 0, 0);
} 

var texto = "                          #e<LeaderMS Comerciantes>#n\r\n\r\nOla #e#h ##n,\r\nEu sou o Fredrick, cuido do banco de items/mesos do     #eLeaderMS#n.\r\n";


function action(mode, type, selection) {
	if (mode == 1)
		status++;
	else {
		cm.dispose();
		return;
	}
	if (status == 0)
		cm.sendNext(texto);
	else if (status == 1)
		if (cm.hasTemp()) {
			if (cm.getHiredMerchantItems(true)) {
				cm.sendOk("Esses itens foram salvos do desligamento do servidor passado!");
				cm.dispose();
			} else {
				cm.sendOk("Por favor, faca um pouco de espaco para receber todos os seus itens.");
				cm.dispose();
			}
		} else {
			cm.sendSimple("Oque gostaria de retirar?\r\n\r\n#b#L0#Mesos#l\r\n#L1#Items#l");
		}
	else if (status == 2) {
		cm.sendNext("Deixa-me tirar de seus arquivos ...");
		choice = selection;
	} else {
		if (choice == 0) {
			if (status == 3) {
				var mesoEarnt = cm.getHiredMerchantMesos();
				if (mesoEarnt > 0)
					cm.sendYesNo("Voce fez "+mesoEarnt+" mesos em sua loja ate o momento. Gostaria de retirar-los?");
				else {
					cm.sendOk("Voce nao fez qualquer mesos.");
					cm.dispose();
				}
			} else if (status == 4) {
				cm.sendNext("Obrigado por usar os meus servicos.");
				cm.gainMeso(cm.getHiredMerchantMesos());
				cm.setHiredMerchantMesos(0);
				cm.dispose();
			}
		} else {
			if (cm.getHiredMerchantItems(false)) {
				cm.sendOk("Obrigado por usar os meus servicos.");
				cm.dispose();
			} else {
				cm.sendOk("Por favor, faca um pouco de espaco para receber todos os seus itens.");
				cm.dispose();
			}
		}
	}
}