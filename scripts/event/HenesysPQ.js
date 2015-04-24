/*
 * @Author Jvlaple
 * @Recraido JavaScriptz
 * LeaderMS 2014
 * Henesys Party Quest
 */


importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.life);
importPackage(Packages.server.maps);
importPackage(Packages.tools.packet);
importPackage(java.lang);

var exitMap;
var PQMap3;
var instanceId;
var minPlayers = 1;

function init() {
    em.setProperty("state", "0");
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup(eim) {
        em.setProperty("semente", "0");
        em.setProperty("state", "1");
	exitMap = em.getChannelServer().getMapFactory().getMap(910010300); 
	instanceId = em.getChannelServer().getInstanceId();
	var instanceName = "HenesysPQ" + instanceId;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	em.getChannelServer().addInstanceId();
	var map = mf.getMap(910010000);
	em.setProperty("cakeNum", "1");
	em.setProperty("shouldDrop", "false");
        var eventTime = 10 * 60000 + 10000;
        em.schedule("timeOut", eventTime);
	eim.startEventTimer(eventTime);
        map.killAllMonsters(false);
        map.setMonsterRate(9999);
    
	return eim;
	
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(910010000);
	player.changeMap(map, map.getPortal(0));
}
  
function playerDead(eim, player) {
}

function playerRevive(eim, player) { 
    if (eim.isLeader(player) || party.size() <= minPlayers) { 
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            playerExit(eim, party.get(i));
        eim.dispose();
    } else
        playerExit(eim, player);
}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
    if (eim.isLeader(player) || party.size() < minPlayers) {
        var party = eim.getPlayers();
        for (var i = 0; i < party.size(); i++)
            if (party.get(i).equals(player))
                removePlayer(eim, player);
            else
                playerExit(eim, party.get(i));
        eim.dispose();
    } else
        removePlayer(eim, player);
}

function leftParty(eim, player) {
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    } else
        playerExit(eim, player);
}

function disbandParty(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
    em.setProperty("state", "0");
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
	var iter = eim.getPlayers().iterator();
        var bonusMap = eim.getMapInstance(910010200);
        while (iter.hasNext()) {
                var player = iter.next();
		player.changeMap(bonusMap, bonusMap.getPortal(0));
		eim.setProperty("entryTimestamp",System.currentTimeMillis() + (5 * 60000));
                player.getClient().getSession().write(MaplePacketCreator.getClock(300));
		}
        eim.schedule("finish", 5 * 60000)
        em.setProperty("state", "0");
}

function liberaEntrada(eim) {
	 em.setProperty("state", "0");
}

function finish(eim) {
		var dMap = eim.getMapInstance(910010400);
        var iter = eim.getPlayers().iterator();
        while (iter.hasNext()) {
		var player = iter.next();
		eim.unregisterPlayer(player);
        player.changeMap(dMap, dMap.getPortal(0));
	}
	eim.dispose();
	em.setProperty("state", "0");
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
                em.setProperty("state", "0");
	}
}
