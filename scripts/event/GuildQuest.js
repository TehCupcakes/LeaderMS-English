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
 */

/*
 * @Author Lerk
 * 
 * Guild Quest 
 */

var exitMap;
 
importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(Packages.tools.packet);
importPackage(java.lang);

function init() {
        em.setProperty("started", "false");
        em.setProperty("shuffleReactors","false");
}

function monsterValue(eim, mobId) { 
        if (mobId == 9300028) { 
                var rubian = new Item(4001024, 0, 1);
                var map = eim.getMapInstance(990000900);
                var reactor = map.getReactorByName("boss");
                map.spawnItemDrop(reactor, eim.getPlayers().get(0), rubian, reactor.getPosition(), true, false);
        }
        return -1;
}

function setup(eim) {
	exitMap = em.getChannelServer().getMapFactory().getMap(990001100); 
        eim.getMapInstance(990000501).shuffleReactors();
        eim.getMapInstance(990000502).shuffleReactors();
        eim.getMapInstance(990000611).getReactorByName("").setDelay(-1);
        eim.getMapInstance(990000620).getReactorByName("").setDelay(-1);
        eim.getMapInstance(990000631).getReactorByName("").setDelay(-1);
        eim.getMapInstance(990000641).getReactorByName("").setDelay(-1);
        eim.setProperty("entryTimestamp",System.currentTimeMillis() + (3 * 60000));
        eim.setProperty("canEnter", "true");
	eim.schedule("begin", 60000);
}

function begin(eim) {
        eim.setProperty("canEnter", "false");
        eim.schedule("earringcheck", 15000);
        var party = eim.getPlayers();
		var iter = party.iterator();
                while (iter.hasNext()) {
                        iter.next().getClient().getSession().write(MaplePacketCreator.serverNotice(6,"["+eim.getServerName()+" Quest] The quest has begun!"));
		}
}

function playerEntry(eim, player) {
	var map = eim.getMapInstance(990000000);
	player.changeMap(map, map.getPortal(0));
        player.getClient().getSession().write(MaplePacketCreator.getClock((Long.parseLong(eim.getProperty("entryTimestamp")) - System.currentTimeMillis()) / 1000));
}

function playerRevive(eim, player) {
        var returnMap = 990000200;
        if (eim.getProperty("canEnter").equals("true")) {
                returnMap = 990000000;
        }
        player.setHp(50);
        player.setStance(0);
        player.changeMap(eim.getMapInstance(returnMap), eim.getMapInstance(returnMap).getPortal(0));
        return false;
}

function playerDead(eim, player) {
}

function playerDisconnected(eim, player) {
        var party = eim.getPlayers();
	if (player.getName().equals(eim.getProperty("leader"))) { 
		var iter = party.iterator();
                while (iter.hasNext()) {
			var pl = iter.next();
                        pl.getClient().getSession().write(MaplePacketCreator.serverNotice(6,"["+eim.getServerName()+" Quest] The leader of the group was disconnected, and the remaining players will be warped out."));
			if (pl.equals(player)) {
				removePlayer(eim, pl);
			}			
			else {
				eim.unregisterPlayer(pl);
				pl.changeMap(exitMap, exitMap.getPortal(0));
			}
		}
		eim.dispose();
	}
	else { 
		removePlayer(eim, player);
                if (party.size() < 6) { 
                        end(eim,"["+eim.getServerName()+" Quest] There are not enough players to continue. The remaining players will be warped out.");
                }
	}
}

function disposePlayerBelow(eim, size, mapid, msg) {
	var z = players(eim);
	var map = eim.getMapFactory().getMap(mapid);
	if (z.size() <= size) {
		var iter = z.iterator();
		while (iter.hasNext()) {
			var cha = iter.next();
			eim.unregisterPlayer(cha);
			if (mapid > 0) {
				cha.changeMap(map, map.getPortal(0));
			}
			if (msg.length > 0) {
				cha.dropMessage(6, msg);
			}
		}
		em.setProperty("started", "false");
		eim.dispose();
		return true;
	}
	return false;
}

function leftParty(eim, player) { 
}

function disbandParty(eim) { 
}

function playerExit(eim, player) {
	eim.unregisterPlayer(player);
	player.changeMap(exitMap, exitMap.getPortal(0));
        var party = eim.getPlayers();
        if (party.size() < 6) { 
                end(eim,"["+eim.getServerName()+" Quest] There are not enough players to continue. The remaining players will be warped out.");
        }
}

function end(eim, msg) {
        var iter = eim.getPlayers().iterator();
        while (iter.hasNext()) {
                var player = iter.next();
                player.getClient().getSession().write(MaplePacketCreator.serverNotice(6,msg));
		eim.unregisterPlayer(player);
                player.changeMap(exitMap, exitMap.getPortal(0));
	}
	eim.dispose();
}

function removePlayer(eim, player) {
	eim.unregisterPlayer(player);
	player.getMap().removePlayer(player);
	player.setMap(exitMap);
}

function clearPQ(eim) {
	var iter = eim.getPlayers().iterator();
        var bonusMap = eim.getMapInstance(990001000);
        while (iter.hasNext()) {
                var player = iter.next();
		player.changeMap(bonusMap, bonusMap.getPortal(0));
                player.getClient().getSession().write(MaplePacketCreator.getClock(40));
	}
        eim.schedule("finish", 40000)
}

function finish(eim) {
        var iter = eim.getPlayers().iterator();
        while (iter.hasNext()) {
		var player = iter.next();
		eim.unregisterPlayer(player);
                player.changeMap(exitMap, exitMap.getPortal(0));
	}
	eim.dispose();
}

function allMonstersDead(eim) {
}

function cancelSchedule() {
}

function timeOut() {
	
}

function earringcheck(eim, player) {
	var iter = eim.getPlayers().iterator();
        while (iter.hasNext()) {
		var pl = iter.next();
                if (pl.getHp() > 0 && pl.getMapId() > 990000200 && pl.getInventory(MapleInventoryType.EQUIPPED).countById(1032033) == 0) {
			pl.addHP(-30000);
			pl.getMap().broadcastMessage(MaplePacketCreator.serverNotice(6,"["+eim.getServerName()+" Quest] " + pl.getName() + " died for not using the earrings!"));
                }
        }
        eim.schedule("earringcheck", 15000);
}
