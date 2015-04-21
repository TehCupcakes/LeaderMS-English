/**
 *2406000.js = Nine Spirit Egg
 *@author Jvlaple
 */
importPackage(Packages.client); 
 
function act() {
	if (rm.getQuestStatus(3706) == MapleQuestStatus.Status.STARTED) {
		rm.dropItems();
		rm.getPlayer().giveItemBuff(2022109);
		rm.getPlayer().gainExp(90000, true, true, true);
		rm.completeQuest(3706);
	} else {
		rm.getPlayer().getMap().resetReactors();
		rm.getPlayer().getMap().setReactorState();
	}
}
