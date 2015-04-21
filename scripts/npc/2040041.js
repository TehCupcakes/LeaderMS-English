/*
 *Aqua Ballon - Stage 6 of LPQ =D
  *@author Jvlaple
  */

importPackage(Packages.client);

var status;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
	if (mode == 0 && status == 0) {
		cm.dispose();
		return;
	}
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
		cm.sendNext("Ola! Bem-vindo(a) ao 6 estagio. Aqui, voce vera caixas com numeros escritos e, se ficar em cima da caixa correta apertando a SETA PARA CIMA, voce se transportara para a proxima caixa. Darei ao lider do grupo uma pista sobre como passar deste estagio #bapenas duas vezes#k e e dever do lider lembrar-se da pista e dar o passo certo, um por vez.\r\nAssim que chegar ao alto, voce ira encontrar o portal para o proximo estagio. Quando todos do seu grupo tiverem passado pelo portal, o estagio estara completo. Tudo vai depender de se lembrar das caixas corretas. Eu ja dei a pista #bduas vezes#k e nao posso mais ajudar voce daqui em diante. Boa sorte!");
		cm.dispose();
		}
	}
}