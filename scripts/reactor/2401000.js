/*
 *@author Jvlaple
 */


function act() {
    rm.mapMessage(6, "Horntail apareceu das profundezas de sua caverna..");
    rm.changeMusic("Bgm14/HonTale");
    rm.spawnMonster(8810026, 76, 260);
    rm.getReactor().getMap().addMapTimer(12 * 60 * 60, 240000000);
}  