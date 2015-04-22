/*
* @autor Java
* LeaderMS MapleStory Private Server
* HenesysPQ
*/

importPackage(Packages.config.configuration);

/* Variaveis */
var texto = "                          #e<"+Configuration.Server_Name+" HenesysPQ>#n\r\n\r\nThis is the #rPrimrose Hill#k. When there is a full moon the moon bunny comes to make rice cakes. Growlie wants rice cakes so you better go help him or he\'ll eat you.\r\n\r\n";
var map = 390009999;
var status = 0;
var minLevel = 10;
var maxLevel = 200;
var minPlayers = 4;
var maxPlayers = 6;

var PQItems = new Array(4001095, 4001096, 4001097, 4001098, 4001099, 40011000);
/* Fim */


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
				cm.sendNext(texto);
			} else if (status == 1) {
				cm.sendSimple("Would you like to go help Growlie?#b\r\n#L0#Yes, I will go.#l#k");
			} else if (status == 2) {
				if (cm.getParty() == null) { 
					cm.sendOk("You are not in a party.");
					cm.dispose();
					return;
				}
				if (!cm.isLeader()) {
					cm.sendOk("You are not the party leader.");
					cm.dispose();
				} if (checkLevelsAndMap(minLevel, maxLevel) == 2) { 
	                          cm.sendOk("Someone in your party is not on the map or does not meet the level requirements!");
                                  cm.dispose();
                                  return;
                                }  else {
					var party = cm.getParty().getMembers();
					var mapId = cm.getChar().getMapId();
					var next = true;
					var levelValid = 0;
					var inMap = 0;
					if (party.size() < minPlayers || party.size() > maxPlayers) 
						next = false;
					else {
						for (var i = 0; i < party.size() && next; i++) {
							if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
								levelValid += 1;
							if (party.get(i).getMapid() == mapId)
								inMap += 1;
						}
						if (levelValid < minPlayers || inMap < minPlayers)
							next = false;
					}  if (next) {
		                  var em = cm.getEventManager("HenesysPQ");
	                          if (em == null) {
	                          cm.sendOk("This event is unavailable..");
		                  } else {
		                  var prop = em.getProperty("state");
		                  if (prop.equals("0") || prop == null) {
		                    em.startInstance(cm.getParty(),cm.getChar().getMap());
                                    party = cm.getChar().getEventInstance().getPlayers();
			            cm.dispose();
		                    } else {
		            	      cm.sendOk("There is another party in the PQ.");
                                      cm.dispose();
		                 }
		               }
	                 } else {
		    cm.sendOk("Your party is not a party of three to six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
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
                cm.playerMessage("You have been warped to Henesys Park.");
                cm.dispose();
            }
        } else if (cm.getPlayer().getMapId() == 910010100) {
            if (status==0) {
                cm.sendYesNo("Would you like go to #rHenesys Park#k?");
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
		
                