var mob;

function act() {
	if(rm.getPlayer().getMapId() == 240060000){
		rm.mapMessage(6, "The Enormous Creature is Approaching from the Deep Cave...");
		rm.changeMusic("Bgm14/HonTale");
		mob=rm.spawnMonster(8810024, 882, 230);
		rm.killMonster(mob,false);
	} 
	if(rm.getPlayer().getMapId() == 240060100){
		rm.mapMessage(6, "Watch out for Horntail... Be prepared for a long fight.");
		rm.changeMusic("Bgm14/HonTale");
		mob=rm.spawnMonster(8810025, -345, 230);
		rm.killMonster(mob,false);
	}
}	