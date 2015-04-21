/*Tory - [Does the Main function of HPQ]
 *@author Jvlaple
 *This file is part of Henesys Party Quest created by Jvlaple of RaGEZONE. [www.forum.ragezone.com] removing this notice means you may not use this script, or any other software released by Jvlaple.
 */
var status = 0;
var minLevel = 10;
var maxLevel = 200;
var minPlayers = 3;
var maxPlayers = 6;

var PQItems = new Array(4001095, 4001096, 4001097, 4001098, 4001099, 40011000);

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
        if(cm.getChar().getMapId()==100000200){
            if (status == 0) {
                cm.sendNext("Este e o #rPrimrose Hill#k. Quando ha uma lua cheia o coelho vem fazer bolos de arroz. Growlie quer bolos de arroz, entao e melhor ir ajuda-lo ou ele vai te comer.");
            } else if (status == 1) {
                cm.sendSimple("Voce gostaria de ajudar Growlie?#b\r\n#L0#Sim, eu vou.#l#k");
            } else if (status == 2) {
                if (cm.getParty() == null) {
                    cm.sendOk("Voce precisa estar em algum grupo!");
                    cm.dispose();
                    return;
                }
                if (!cm.isLeader()) {
                    cm.sendOk("Voce nao e o lider do grupo.");
                    cm.dispose();
                }  else {
                    var party = cm.getParty().getMembers();
                    var mapId = cm.getChar().getMapId();
                    var next = true;
                    var levelValid = 0;
                    var inMap = 0;
                    if (next) {
	            var em = cm.getEventManager("HenesysPQ");
	            if (em == null) {
	             cm.sendOk("Este evento esta indisponivel.");
		      } else {
		     if (em.getProperty("entryPossibleHPQ") != "false") {
		    // Begin the PQ.
		     em.startInstance(cm.getParty(),cm.getChar().getMap());
		   // Remove Passes and Coupons
		       sparty = cm.getChar().getEventInstance().getPlayers();
                        var eim = cm.getPlayer().getEventInstance();
                        var party2 = eim.getPlayers();
                        cm.removeFromParty(4001022, party2);//Item to Remove
			em.setProperty("entryPossibleHPQ", "false"); 
			} else {
			cm.sendNext("Um grupo ja entrou na #rQuest#k neste canal. Por favor, tente outro canal, ou esperar que o grupo atual para terminar.");
			}
		}
		cm.dispose();
		} else {
		cm.sendNext("Seu grupo nao tem o minino de 3 jogadores. Certifique-se de todos os seus membros estejam presentes e qualificados para participar desta missao.  Eu vejo #b" + levelValid.toString() + " #kmembros estao no nivel certo, e #b" + inMap.toString() + "#k estao em meu mapa. Se isso estiver errado, #bsaia para fora,#k ou refaca seu grupo.");
				cm.dispose();
				}
			}
		}
	} else if(cm.getChar().getMapId() == 910010400){
            if (status == 0){
                for (var i = 0; i < PQItems.length; i++) {
				cm.removeAll(PQItems[i]);
                            }
                cm.warp(100000200);
                cm.playerMessage("Voce foi levado para o Parque de Henesys.");
                cm.dispose();
            }
        } else if (cm.getPlayer().getMapId() == 910010100) {
            if (status==0) {
                cm.sendYesNo("Voce deseja voltar para o #rParque de Henesys#k?");
            }else if (status == 1){
                for (var i = 0; i < PQItems.length; i++) {
				cm.removeAll(PQItems[i]);
                            }
                cm.warp(100000200, 0);
                cm.dispose();
            }
        }
    }
}
					
					
