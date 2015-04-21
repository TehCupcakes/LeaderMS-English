/*
 * @Author Jvlaple
 * @Re-coder JavaScriptz 
 *
 * Wedding p/ LeaderMS
 */
importPackage(java.lang);

importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(Packages.tools);

var exitMap;
var altarMap;
var cakeMap;
var instanceId;
var minPlayers = 1;

function init() {
	exitMap = em.getChannelServer().getMapFactory().getMap(680000500); 
	altarMap = em.getChannelServer().getMapFactory().getMap(680000210); 
	cakeMap = em.getChannelServer().getMapFactory().getMap(680000300);
	instanceId = 1;
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup(eim) {
             var instanceName = "CathedralWedding" + instanceId;
             var eim = em.newInstance(instanceName);
             instanceId++;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	var map = mf.getMap(680000200);
	em.schedule("playerAltar", 3 * 60000);
	eim.setProperty("hclicked", 0);
	eim.setProperty("wclicked", 0);
	eim.setProperty("entryTimestamp",System.currentTimeMillis() + (3 * 60000));
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(680000200);
	player.changeMap(map, map.getPortal(0));
	player.getClient().getSession().write(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) {
}

function playerDisconnected(eim, player) {
	playerExit(eim, player);
}

function leftParty(eim, player) {		
}

function disbandParty(eim) {
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
}

function playerWarpAltar(eim, player) {
	if ((player.getName() != eim.getProperty("husband")) && (player.getName() != eim.getProperty("wife"))){
	player.changeMap(altarMap, altarMap.getPortal(0));
	player.getClient().getSession().write(MaplePacketCreator.getClock(300));
	}else{
	player.changeMap(altarMap, altarMap.getPortal(2));
	player.getClient().getSession().write(MaplePacketCreator.getClock(300));
	player.getClient().getSession().write(MaplePacketCreator.serverNotice(6, "Por favor, fale com o Padre agora!"));
	}
}

function playerWarpCake(eim, player) {
	player.changeMap(cakeMap, cakeMap.getPortal(0));
	player.getClient().getSession().write(MaplePacketCreator.getClock(300));
}

function playerAltar(eim, player) {
		var iter = em.getInstances().iterator();
		while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerWarpAltar(eim, pIter.next());
			}
		}
		em.schedule("playerCake", 5 * 60000);
	}
}

function playerCake(eim, player) {
		var iter = em.getInstances().iterator();
		while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerWarpCake(eim, pIter.next());
			}
		}
		em.schedule("timeOut", 5 * 60000);
	}
}

function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function clearPQ(eim) {
	var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
		playerExit(eim, party.get(i));
	}
	eim.dispose();
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


function dispose() {

}