/*
 *2408002.js
 *Key Warp for Horn Tail PQ [HTPQ]
 *@author Jvlaple
*/

importPackage(Packages.world);
importPackage(Packages.client);
importPackage(Packages.server.maps);
importPackage(Packages.tools.packet);
importPackage(java.lang);

function act() {
	var eim = rm.getPlayer().getEventInstance();
	var party = rm.getPlayer().getEventInstance().getPlayers();
	var womanfred = eim.getMapFactory().getMap(240050100);
	var vvpMap = rm.getPlayer().getMapId();
	var vvpKey;
	var vvpOrig = 4001087;
	var vvpStage = -1;
	rm.mapMessage(6, "The key will teleport you somewhere...");
	switch (vvpMap) {
		case 240050101 : {
							vvpKey = vvpOrig;
							vvpStage = 2;
							break;
						}
		case 240050102 : {
							vvpKey = vvpOrig + 1;
							vvpStage = 3;
							break;
						}
		case 240050103 : {
							vvpKey = vvpOrig + 2;
							vvpStage = 4;
							break;
						}
		case 240050104 : {
							vvpKey = vvpOrig + 3;
							vvpStage = 5;
							break;
						}
		default : {
					vvpKey = -1;
					break;
		}
	}
	
	var tehWomanfred = new client.Item(vvpKey, 0, 1);
	var theWomanfred = womanfred.getReactorByName("keyDrop1");
	var dropper = eim.getPlayers().get(0);
	womanfred.spawnItemDrop(theWomanfred, dropper, tehWomanfred, theWomanfred.getPosition(), true, true);
	womanfred.broadcastMessage(MaplePacketCreator.serverNotice(5, "There is a bright flash of light, then suddenly a key appears somewhere on the map."));
}
	
	