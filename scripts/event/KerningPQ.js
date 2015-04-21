importPackage(Packages.world);

var exitMap;
var instanceId;
var minPlayers = 1;

function init() {
	instanceId = 1;
        em.setProperty("state", "0");
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup() {
        em.setProperty("state", "1");
 	exitMap = em.getChannelServer().getMapFactory().getMap(103000890); // <exit>
	var instanceName = "KerningPQ" + instanceId;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	instanceId++;
	var map = mf.getMap(103000800);
	map.shuffleReactors();
	var firstPortal = eim.getMapInstance(103000800).getPortal("next00");
	firstPortal.setScriptName("kpq1");
	var eventTime = 30 * (1000 * 60);
	em.schedule("timeOut", eventTime);
	eim.startEventTimer(eventTime);
	
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(103000800);
	player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
	if (eim.isLeader(player)) { 
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim, party.get(i));
		}
		eim.dispose();
	}
	else {
		var party = eim.getPlayers();
		if (party.size() <= minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose();
		}
		else
			playerExit(eim, player);
	}
}

function playerDisconnected(eim, player) {
	if (eim.isLeader(player)) {
		var party = eim.getPlayers();
		for (var i = 0; i < party.size(); i++) {
			if (party.get(i).equals(player)) {
				removePlayer(eim, player);
			} else {
				playerExit(eim, party.get(i));
			}
		}
		eim.dispose();
	} else { 
		var party = eim.getPlayers();
		if (party.size() < minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose();
		} else
			playerExit(eim, player);
	}
}


function leftParty(eim, player) {			
	var party = eim.getPlayers();
	if (party.size() <= minPlayers) {
		for (var i = 0; i < party.size(); i++) {
			playerExit(eim,party.get(i));
		}
		eim.dispose();
	}
	else
		playerExit(eim, player);
}

function disbandParty(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose();
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
         if (eim.getPlayerCount() == 0) {
	 em.setProperty("state", "0");
     }
}

function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
        if (eim.getPlayerCount() == 0) {
	 em.setProperty("state", "0");
     }
}

function clearPQ(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	 eim.dispose();
         if (eim.getPlayerCount() == 0) {
	 em.setProperty("state", "0");
     }
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function timeOut() {
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerExit(eim, pIter.next());
			}
		}
		eim.dispose();
	}
}