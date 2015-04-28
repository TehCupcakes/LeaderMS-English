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
				cm.sendNext("#e<"+cm.getServerName()+" PQ: Cash Quest>#n\r\n\r\nHello #h #, through this NPC it is possible to exchange points for Cash. Remember, in order to participate in the search for points it is necessary to check the schedule and make sure you have the right occupation \"Alpha\".\r\n\r\n#eIn case of doubt, visit our site#n");
			} else if (status == 1) {
				cm.sendSimple("Are you prepared and want to participate?\r\n#b#L3#About Hours/Information#l#k\r\n#b#L4#Trade points for Cash#l#k\r\n#b#L1#Yes, come on!#l#k\r\n#b#L2#Let me out of here!#l#k");
			} else if (selection == 1) {
                                 if (cm.getPlayerCount(107000200) >= 10) {
                                        cm.sendOk("O mapa ja esta cheio, por favor procure outro canal!");
                                        cm.dispose();
                                        return;
                                 } 
                                 if (hora < 04 || hora >= 05 && hora < 8 || hora >= 9 && hora < 14 || hora >= 15 && hora < 23){ // Verifica horario de funcionamento & Ocupacao
                                        cm.sendOk("                  #e<"+cm.getServerName()+" PQ: Cash Quest>#n\r\n\r\nSorry, you arrived late/early or do not have the occupation Alpha.");
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
         cm.sendOk("#e<"+cm.getServerName()+" PQ: Information>#n\r\n\r\nDawn - 04:00 to 05:00 AM\r\nMorning - 08:00 to 09:00 AM\r\nAfternoon - 2:00 as 3:00 PM\r\nEvening - 7:00 as 8:00 PM\r\n\r\n                               <#eActual Time#n: " + hora + ":" + min + ">");
         cm.dispose();  
      }   else if (selection == 4) {
         cm.sendOk("#e<"+cm.getServerName()+" PQ: Information>#n\r\n\r\nTo exchange CashPQ points you need to talk to the NPC \"Tia\", who is somewhere out there. Good luck!"); 
         cm.dispose();  
     }  
   } if(cm.getChar().getMapId()== 107000200){
       if (status == 0) {
	  cm.sendYesNo("Do you want to quit?");
          } else if (status == 1) {
          cm.warp(107000100, 0); 
          cm.getPlayer().getMap().cancelCashPQTimer();
          cm.dispose(); 
       }
     }
 }
