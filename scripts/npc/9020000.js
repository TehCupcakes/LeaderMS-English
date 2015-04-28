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
				cm.sendOk("                                    #e<"+cm.getServerName()+" PQ>#n\r\n\r\nHow about you and your party finish a quest together? Here you will find obstacles and problems that can only be solved in teams. If you want to try, ask your #bparty leader#k to talk to me.");
				cm.dispose();
                                return;
			} if (!cm.isLeader()) { 
				cm.sendSimple("You are not the leader of the party.");
				cm.dispose();
                                return;
                         } if (checkLevelsAndMap(minLevel, maxLevel) == 2) {  
	                          cm.sendOk("Not all members of the party are present or are not within the allowed level range.");
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
	                          cm.sendOk("This event is unavailable.");
		                  } else {
		                  var prop = em.getProperty("state");
		                  if (prop.equals("0") || prop == null) {
				  em.startInstance(cm.getParty(),cm.getChar().getMap());
				  cm.dispose();
		                    } else {
		            	      cm.sendOk("Another party is already challenging the quest. Please try again later.");
                                      cm.dispose();
		                 }
		               }
	                 } else {
		                   cm.sendOk("Someone in your party is not between levels 21-30. Please try again.");
                                   cm.dispose();
	                        }
			}
		}
		else {
			cm.sendOk("Something went wrong.");
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

					