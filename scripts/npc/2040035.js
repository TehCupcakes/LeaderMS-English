/*
@author Jvlaple
*Aturo gives you a random item :D
*/
var status = 0;
var itemArray = Array(2000004, 2000005, 2000006, 2002020, 2002021, 2002022, 2002023, 2002024, 2002025, 2002026, 2001000, 2001002, 2002015, 2050005, 2022179, 2020014, 2020015 
                      /*End of Pots*/, 2100000, 2100001, 2100002, 2100003, 2100004, 2100005 
                      /*End of Sacks*/, 2061003, 2060003, 2060004, 2061004
                      /*End of Arrows*/, 2210000, 2210001, 2210002
                      /*End of Morph items*/ );
					
var itemQuan = Array(50, 20, 200, 200, 200, 200, 200, 200, 200, 200, 200, 200, 5, 30, 2, 100, 50, 1, 1, 1, 1, 1, 1, 2000, 2000, 2000, 2000, 1, 1, 1, 1, 5, 5, 5);

var PQItems = new Array(4001022, 4001023);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
		if (mode == -1) {
		cm.dispose();
		} else { 
		  if (status >= 2 && mode == 0) { 
		   cm.dispose(); 
		   return; 
		} 
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
			cm.sendNext("Incrivel! Voce completou todos os estagios e agora esta aqui aproveitando sua vitoria. Uau! Meus sinceros parabens para cada um de voces pelo trabalho bem feito. Aqui esta um presentinho para voces. Antes de aceitar, verifique se seu inventario de uso e equip. tem um slot disponivel.");
        } else if (status == 1) {
			for (var i = 0; i < PQItems.length; i++) {
				cm.removeAll(PQItems[i]); 
			}
			var randmm = Math.floor(Math.random() * itemArray.length);
			cm.gainItem(itemArray[randmm], itemQuan[randmm]);
			cm.warp(221024500, 0);
			cm.dispose();
		}
    }
}