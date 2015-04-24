// @Author Groat
// Boss Quest 

importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.life);
importPackage(Packages.tools.packet);

var exitMap;
var instanceId;
var monster;
monster = new Array(
    3220000, // Stumpy,
    9300003, // Slime King
    4130103, // Rombot
    9300012, // Alishar
    8220001, // Yeti on Skis
    8220000, // Elliza
    9300119, // Lord Pirate
    9300152, // Angry Franken Lloyd
    9300039, // Papa Pixie
    9300000, // Knight Statue B
    9300028, // Ergoth
    9400549, // Headless Horseman
    8180001, // Griffey
    8180000, // Manon
    8500001, // Papulatus
    9400575, // Big Foot
    9400014, // Black Crow
    8800002, // Zakum Body 3
    9400121, // Female Boss
    9400300 // The Boss
); 


function init() {
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup(partyid) {
    exitMap = em.getChannelServer().getMapFactory().getMap(200000000);
    var instanceName = "BossQuest" + partyid;

    var eim = em.newInstance(instanceName);
    var mf = eim.getMapFactory();
    var map = mf.getMap(240060200, false, true, false);
    map.toggleDrops();

    eim.setProperty("points", 0);
    eim.setProperty("monster_number", 0);

    eim.schedule("beginQuest", 5000);
    return eim;
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(240060200);
    player.changeMap(map, map.getPortal(0));
}

function playerDead(eim, player) {
}

function playerRevive(eim, player) { 
    player.setHp(player.getMaxHp());
    playerExit(eim, player);
    return false;
}

function playerDisconnected(eim, player) {
    removePlayer(eim, player);
}

function leftParty(eim, player) {            
    playerExit(eim, player);
}

function disbandParty(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
}

function playerExit(eim, player) {
    var party = eim.getPlayers();
    var dispose = false;
    if (party.size() == 1) {
        dispose = true;
    }
    eim.saveBossQuestPoints(parseInt(eim.getProperty("points")), player);
    player.getClient().getSession().write(MaplePacketCreator.serverNotice(6, "[The Boss Quest] Your current points have been awarded, spend them as you wish. Better luck next time!"));
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
    if (dispose) {
        eim.dispose();
    }
}

function removePlayer(eim, player) {
    var party = eim.getPlayers();
    var dispose = false;
    if (party.size() == 1) {
        dispose = true;
    }
    eim.saveBossQuestPoints(parseInt(eim.getProperty("points")), player);
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
    if (dispose) {
        eim.dispose();
    }
}

function clearPQ(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
}

function allMonstersDead(eim) {
    var monster_number = parseInt(eim.getProperty("monster_number"));
    var points = parseInt(eim.getProperty("points"));
    
    var monster_end = java.lang.System.currentTimeMillis();
    var monster_time = Math.round((monster_end - parseInt(eim.getProperty("monster_start"))) / 1000);
    
    if (3600 - monster_time <= 0) points += monster_number * 10000;
    else points += (monster_number * 10000) + ((3600 - monster_time) * (monster_number + 1));
    
    monster_number++;
    
    eim.setProperty("points", points);
    eim.setProperty("monster_number", monster_number);
    
    var map = eim.getMapInstance(240060200);

    if (monster_number > 19) {
        map.broadcastMessage(MaplePacketCreator.serverNotice(6, "[The Boss Quest] Congratulations! Your team has defeated all the bosses with " + points + " points!"));
        map.broadcastMessage(MaplePacketCreator.serverNotice(6, "[The Boss Quest] The points have been awarded, spend them as you wish."));
        disbandParty();
    }
    else {
        map.broadcastMessage(MaplePacketCreator.serverNotice(6, "[The Boss Quest] Your team now has " + points + " points! The next boss will spawn in 10 seconds."));
        map.broadcastMessage(MaplePacketCreator.getClock(10));
        eim.schedule("monsterSpawn", 10000);
    }
}

function monsterSpawn(eim) {
    var mob = MapleLifeFactory.getMonster(monster[parseInt(eim.getProperty("monster_number"))]);
    var overrideStats = new MapleMonsterStats();

    if (parseInt(eim.getProperty("monster_number")) > 16) overrideStats.setHp(mob.getHp());
    else overrideStats.setHp(mob.getHp() * 2);

    overrideStats.setExp(mob.getExp());
    overrideStats.setMp(mob.getMaxMp());
    mob.setOverrideStats(overrideStats);

    if (parseInt(eim.getProperty("monster_number")) > 16) mob.setHp(mob.getHp());
    else mob.setHp(mob.getHp() * 2);

    eim.registerMonster(mob);

    var map = eim.getMapInstance(240060200);
    map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(100, 100));
    eim.setProperty("monster_start", java.lang.System.currentTimeMillis());
}

function beginQuest(eim) {
    var map = eim.getMapInstance(240060200);
    map.broadcastMessage(MaplePacketCreator.serverNotice(6, "[The Boss Quest] The creatures of the darkness are coming in 30 seconds. Prepare for the worst!"));
    eim.schedule("monsterSpawn", 30000);
    map.broadcastMessage(MaplePacketCreator.getClock(30));
}

function cancelSchedule() {
}