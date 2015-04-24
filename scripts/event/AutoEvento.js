/**
@author JavaScriptz <javascriptz@leaderms.com.br>
* LeaderMS 2014
* Auto Evento
* Monstros no Mercado Livre
**/

importPackage(Packages.client);
importPackage(Packages.server.life);
importPackage(Packages.tools.packet);

var setupTask;
var Mapas = 910000000;
var Monstros = 9400202;  


function init() {
	scheduleNew();
}

function scheduleNew() {
	setupTask = em.schedule("start", 50 * 60000); 
}

function cancelSchedule() {
	setupTask.cancel(true);
}

function start() {
        var Evento = em.getChannelServer().getMapFactory().getMap(Mapas, true, true);
	if (Evento.countMobOnMap(9400202) == 0) {
		Evento.broadcastMessage(MaplePacketCreator.serverNotice(6, "[Auto Event] A monster invaded the Free Market, run to help us!"));
                Evento.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(Monstros), new java.awt.Point(-138, -200));                
	}
	scheduleNew();
}