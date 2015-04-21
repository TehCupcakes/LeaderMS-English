var status = -1;

function action(mode, type, selection) {
    if (mode != 1) {
	cm.sendOk("Good luck on finishing the Guild Quest!");
	cm.dispose();
	return;
    }
status++;
    if (status == 0) {
	if (cm.isPlayerInstance()) {
		cm.sendSimple("What would you like to do? \r\n #L0#Exit the Guild Quest#l");
	} else {
		cm.sendOk("Sorry, but I cannot do anything for you!");
		cm.dispose();
	}
    }
    else if (status == 1) {
		cm.sendYesNo("Are you sure you want to leave? You will be able to return!");
    }
    else if (status == 2) {
	if (cm.isPlayerInstance()) { 
		cm.getPlayer().getEventInstance().removePlayer(cm.getPlayer());
	}
	cm.dispose();
	return;
    }
}
