importPackage(Packages.tools.packet);
importPackage(Packages.server);
importPackage(Packages.server.life);
importPackage(Packages.server.maps);


function act() {
	rm.mapMessage(6, "[Leader Quest] Uma das sementes foi colocada.");
	var em = rm.getEventManager("HenesysPQ");
	if (em != null) {
		var react = rm.getMap().getReactorByName("fullmoon");
		em.setProperty("semente", parseInt(em.getProperty("semente")) + 1);
		react.forceHitReactor(react.getState() + 1);
		if (em.getProperty("semente").equals("6")) {
                      var eim = rm.getPlayer().getEventInstance();
                      var tehMap = eim.getMapInstance(910010000);
                      var bunny = MapleLifeFactory.getMonster(9300061);
                      tehMap.spawnMonsterOnGroundBelow(bunny, new java.awt.Point(-187, -186));
                      eim.registerMonster(bunny);
                      eim.setProperty("shouldDrop", "true");
                      rm.getPlayer().getMap().setMonsterRate(1);
                      rm.getPlayer().getMap().startMapEffect("[Leader Quest] Proteja o Coelhinho da Lua!", 5120002, 7000);
		}
	}
}