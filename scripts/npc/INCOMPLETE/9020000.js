var status = 0;
var minLevel = 21; //arrumar
var maxLevel = 30; //arrumar
var minPlayers = 4;
var maxPlayers = 6;

var PQItems = new Array(4001007, 4001008);

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
			if (cm.getParty() == null) { 
				cm.sendOk("                                    #e<LeaderMS-PQ>#n\r\n\r\nQue tal voce e seu grupo terminarem uma missao juntos? Aqui voce vai encontrar obstaculos e problemas que so poderao ser resolvidos em equipe. Se quiser tentar, peca ao #blider do seu grupo#k para falar comigo.");
				cm.dispose();
                                return;
			} if (!cm.isLeader()) { 
				cm.sendSimple("Voce nao e o lider do grupo.");
				cm.dispose();
                                return;
                         } if (checkLevelsAndMap(minLevel, maxLevel) == 2) {  
	                          cm.sendOk("Acho que nem todos os membros do seu grupo estao presentes.");
                                  cm.dispose();
                                  return;
                         } else {
				var party = cm.getParty().getMembers();
				var mapId = cm.getChar().getMapId();
				var next = true;
				var levelValid = 0;
				var inMap = 0;
				var it = party.iterator();
				while (it.hasNext()) {
					var cPlayer = it.next();
					if ((cPlayer.getLevel() >= minLevel) && (cPlayer.getLevel() <= maxLevel)) {
						levelValid += 1;
					} else {
						next = false;
					}
					if (cPlayer.getMapid() == mapId) {
						inMap += 1;
					}
				}
				if (party.size() < minPlayers || party.size() > maxPlayers || inMap < minPlayers) 
					next = false;
				if (next) {
				  var em = cm.getEventManager("KerningPQ");
	                          if (em == null) {
	                          cm.sendOk("Este evento esta indisponivel.");
		                  } else {
		                  var prop = em.getProperty("state");
		                  if (prop.equals("0") || prop == null) {
				  em.startInstance(cm.getParty(),cm.getChar().getMap());
				  cm.dispose();
		                    } else {
		            	      cm.sendOk("Um outro grupo ja entrou para completar a missao. Por favor, tente mais tarde.");
                                      cm.dispose();
		                 }
		               }
	                 } else {
		                   cm.sendOk("Alguem no seu grupo nao esta entre os niveis 21~30. Por favor, verifique novamente.");
                                   cm.dispose();
	                        }
			}
		}
		else {
			cm.sendOk("Dialogo perdido.");
			cm.dispose();
		}
	}
}

function checkLevelsAndMap(lowestlevel, highestlevel) {
    var party = cm.getParty().getMembers();
    var mapId = cm.getMapId();
    var valid = 0;
    var inMap = 0;

    var it = party.iterator();
    while (it.hasNext()) {
        var cPlayer = it.next();
        if (!(cPlayer.getLevel() >= lowestlevel && cPlayer.getLevel() <= highestlevel) && cPlayer.getJobId() != 900) {
            valid = 1;
        }
        if (cPlayer.getMapid() != mapId) {
            valid = 2;
        }
    }
    return valid;
}

					