/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
	
	THIS  FILE WAS MADE BY JVLAPLE. REMOVING THIS NOTICE MEANS YOU CAN'T USE THIS SCRIPT OR ANY OTHER SCRIPT PROVIDED BY JVLAPLE.
 */

/*
 * @Author Jvlaple
 * 
 * Aquarium Party Quest
 */
importPackage(java.lang);

importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(Packages.server);
importPackage(Packages.server.life);
importPackage(Packages.tools);

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
	instanceId = em.getChannelServer().getInstanceId();
	exitMap = em.getChannelServer().getMapFactory().getMap(230000000); //Teh exit map :) <---------t
	var instanceName = "AquaPQ" + instanceId;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	em.getChannelServer().addInstanceId();	
	var map = mf.getMap(230040400);//wutt
	map.removePortals();
	map.spawnNpc(1052014, new java.awt.Point(-367, -63));
	var dangerMap1 = mf.getMap(230040200);
	dangerMap1.removePortals();
	dangerMap1.spawnNpc(1052014, new java.awt.Point(78, 308));
	var dangerMap2 = mf.getMap(230040300);
	dangerMap2.removePortals();
	dangerMap2.spawnNpc(1052014, new java.awt.Point(8, -428));
	var deepMap1 = mf.getMap(230040000);
	deepMap1.removePortals();
	deepMap1.spawnNpc(1052014, new java.awt.Point(-733, -50));
	var bossmap = mf.getMap(230040420);
	bossmap.removePortals();
	bossmap.spawnNpc(1052014, new java.awt.Point(-39, 168));
	bossmap.killAllMonsters(false);
	var mob = MapleLifeFactory.getMonster(8510000);
	var overrideStats = new MapleMonsterStats();
	overrideStats.setHp(2147000000);
	overrideStats.setExp(2147000000);
	overrideStats.setMp(mob.getMaxMp());
	mob.setOverrideStats(overrideStats);
	mob.setHp(overrideStats.getHp());
        bossmap.spawnMonsterOnGroudBelow(mob, new java.awt.Point(568, 137));
	//Fuck this timer
	em.schedule("timeOut", 60000 * 10 * 60);
	em.schedule("broadcastClock", 1500);
	eim.setProperty("entryTimestamp",System.currentTimeMillis() + (10 * 60000 * 60));
	
	return eim;
}

function playerEntry(eim, player) {
	//Bgm11/ShiningSea
	var map = eim.getMapInstance(230040200);
	player.changeMap(map, map.getPortal(5));
	player.getClient().getSession().write(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
	player.getClient().getSession().write(MaplePacketCreator.musicChange("Bgm12/AquaCave"));
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

function liberaEntrada(eim) {
	 em.setProperty("state", "0");
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

function playerClocks(eim, player) {
  if (player.getMap().hasTimer() == false){
	player.getClient().getSession().write(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
	}
}

function playerTimer(eim, player) {
	if (player.getMap().hasTimer() == false) {
		player.getMap().setTimer(true);
	}
}

function broadcastClock(eim, player) {
	var iter = em.getInstances().iterator();
	while (iter.hasNext()) {
		var eim = iter.next();
		if (eim.getPlayerCount() > 0) {
			var pIter = eim.getPlayers().iterator();
			while (pIter.hasNext()) {
				playerClocks(eim, pIter.next());
			}
		}
	}
	var iterr = em.getInstances().iterator();
	while (iterr.hasNext()) {
		var eim = iterr.next();
		if (eim.getPlayerCount() > 0) {
			var pIterr = eim.getPlayers().iterator();
			while (pIterr.hasNext()) {
				playerTimer(eim, pIterr.next());
			}
		}
	}
	em.schedule("broadcastClock", 1600);
}
