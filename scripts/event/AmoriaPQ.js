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
 * Amoria Party Quest
 */
importPackage(java.lang);

importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(Packages.tools);

var exitMap;
var instanceId;
var minPlayers = 1;

function init() {
	instanceId = 1;
}

function monsterValue(eim, mobId) {
	return 1;
}

function setup(eim) {
	instanceId = em.getChannelServer().getInstanceId();
	exitMap = em.getChannelServer().getMapFactory().getMap(670011000); //Teh exit map :) <---------t
	var instanceName = "AmoriaPQ" + instanceId;
	var eim = em.newInstance(instanceName);
	var mf = eim.getMapFactory();
	em.getChannelServer().addInstanceId();
	var map = mf.getMap(670010200);//wutt
	em.schedule("broadcastClock", 1500);
	eim.setProperty("entryTimestamp",System.currentTimeMillis() + (60 * 60000));
	em.schedule("timeOut", 60 * 60000);
	var fMap = mf.getMap(670010200);
	var sMap = mf.getMap(670010300);
	var hardMap = mf.getMap(670010400);
	var easyMap = mf.getMap(670010500);
	fMap.getPortal(15).setScriptName("male00");
	fMap.getPortal(16).setScriptName("female00");
	var trapMap = mf.getMap(670010600);
	eim.setProperty("openedDoors", "0");
	sMap.getPortal(8).setScriptName("apq_door_3");
	hardMap.getPortal(2).setScriptName("apq_door_4");
	easyMap.getPortal(2).setScriptName("apq_door_5");
	
	return eim;
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(670010200);
	player.changeMap(map, map.getPortal(0));
	player.getClient().getSession().write(MaplePacketCreator.getClock(3600));
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
			}			
			else {
				playerExit(eim, party.get(i));
			}
		}
		eim.dispose();
	}
	else {
		var party = eim.getPlayers();
		if (party.size() < minPlayers) {
			for (var i = 0; i < party.size(); i++) {
				playerExit(eim,party.get(i));
			}
			eim.dispose();
		}
		else
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
	var exitMap1 = em.getChannelServer().getMapFactory().getMap(670011000);
	player.changeMap(exitMap1, exitMap1.getPortal(0));
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

function clearPQ(eim) {
	var iter = eim.getPlayers().iterator();
        var bonusMap = eim.getMapInstance(670010800);
        while (iter.hasNext()) {
			var player = iter.next();
			player.changeMap(bonusMap, bonusMap.getPortal(0));
			eim.setProperty("entryTimestamp",System.currentTimeMillis() + (2 * 60000));
	        player.getClient().getSession().write(MaplePacketCreator.getClock(120));
		}
        eim.schedule("finish", 1200000)
}

function finish(eim) {
		var dMap = eim.getMapInstance(670011000);
        var iter = eim.getPlayers().iterator();
        while (iter.hasNext()) {
			var player = iter.next();
			eim.unregisterPlayer(player);
	        player.changeMap(dMap, dMap.getPortal(0));
		}
	eim.dispose();
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