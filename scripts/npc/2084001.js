/*
* @autor Java
* LeaderMS MapleStory Private Server
* NPC/Quest CASH
*/


/* Variaveis */
    var status;
    var minLevel = 10;
    var maxLevel = 200;
    var minPlayers = 1;
    var maxPlayers = 6;
    var tempo = new Date();
    var dia = tempo.getDay();
    var ano = tempo.getFullYear();
    var mes = tempo.getMonth();
    var data = tempo.getDate();
    var hora = tempo.getHours();
    var min = tempo.getMinutes();
    var seg = tempo.getSeconds();
    var texto = "                  #e<LeaderMS PQ: Cash Quest>#n\r\n\r\nOla #h #, atraves deste NPC sera possivel obter pontos para a troca de Cash. Lembrando que, para poder participar da busca dos pontos e necessario verificar se voce esta no horario correto de coleta e se voce possui a ocupacao \"Alpha\".\r\n\r\n#eEm caso de duvidas#n - #bwww.leaderms.com/forum#k";
    var texto1 = "                  #e<LeaderMS PQ: Horarios/Informacoes>#n\r\n\r\nMadrugada - 04:00 as 05:00\r\nManha - 08:00 as 09:00\r\nTarde - 14:00 as 15:00\r\nNoite - 19:00 as 20:00\r\n\r\n                               <#eHorario atual#n : " + hora + ":" + min + ">";
    var texto2 = "                  #e<LeaderMS PQ: Horarios/Informacoes>#n\r\n\r\nPara trocar os pontos da CashPQ e necessario falar com o NPC \"Tia\", que se encontra em algum lugar por ai, boa sorte!";

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
		if(cm.getChar().getMapId()== 107000100 || cm.getChar().getMapId()== 100000000){
			if (status == 0) {
				cm.sendNext(texto);
			} else if (status == 1) {
				cm.sendSimple("Voce ja esta preparado e deseja participar?\r\n#b#L3#Sobre Horarios/Informacoes#l#k\r\n#b#L4#Trocar pontos por Cash#l#k\r\n#b#L1#Sim, vamos la!#l#k\r\n#b#L2#Me tire daqui!#l#k");
			} else if (selection == 1) {
                                 if (cm.getPlayerCount(107000200) >= 10) {
                                        cm.sendOk("O mapa ja esta cheio, por favor procure outro canal!");
                                        cm.dispose();
                                        return;
                                 } 
                                 if (hora < 04 || hora >= 05 && hora < 8 || hora >= 9 && hora < 14 || hora >= 15 && hora < 23){ // Verifica horario de funcionamento & Ocupacao
                                        cm.sendOk("                  #e<LeaderMS PQ: Cash Quest>#n\r\n\r\nSinto muito, mais voce chegou atrasado/antecipado ou nao possui a ocupacao Alpha.");
					cm.dispose();
                                }
                                else { 
				 cm.warp(107000200, 0);
                                 cm.dispose();                
	       } 
           } 
     } else if (selection == 2) {
         cm.warp(100000000, 0); 
         cm.playerMessage(textowarp);
         cm.dispose();  
     }   else if (selection == 3) {
         cm.sendOk(texto1);
         cm.dispose();  
      }   else if (selection == 4) {
         cm.sendOk(texto2); 
         cm.dispose();  
     }  
   } if(cm.getChar().getMapId()== 107000200){
       if (status == 0) {
	  cm.sendYesNo("Voce quer mesmo sair?");
          } else if (status == 1) {
          cm.warp(107000100, 0); 
          cm.getPlayer().getMap().cancelCashPQTimer();
          cm.dispose(); 
       }
     }
 }
