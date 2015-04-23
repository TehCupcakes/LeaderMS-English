importPackage(Packages.server);

var status;
var choice;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 1)
		status++;
	else {
		cm.dispose();
		return;
	}
	if (status == 0)
		cm.sendNext("#e<"+cm.getServerName()+" Merchant>#n\r\n\r\nHello #e#h ##n,\r\nMy name is  Fredrick, and I take care of storing items/mesos in #e"+cm.getServerName()+"#n.\r\n");
	else if (status == 1)
		if (cm.hasTemp()) {
			if (cm.getHiredMerchantItems(true)) {
				cm.sendOk("These items were saved from the last server shutdown!");
				cm.dispose();
			} else {
				cm.sendOk("Please make a little space to receive all of your items.");
				cm.dispose();
			}
		} else {
			cm.sendSimple("What did you wish to withdraw?\r\n\r\n#b#L0#Mesos#l\r\n#L1#Items#l");
		}
	else if (status == 2) {
		cm.sendNext("Let me get your files...");
		choice = selection;
	} else {
		if (choice == 0) {
			if (status == 3) {
				var mesoEarnt = cm.getHiredMerchantMesos();
				if (mesoEarnt > 0)
					cm.sendYesNo("You have "+mesoEarnt+" mesos in this shop at the moment. Would you like to remove them?");
				else {
					cm.sendOk("You did not earn any mesos.");
					cm.dispose();
				}
			} else if (status == 4) {
				cm.sendNext("Thank you for using my services.");
				cm.gainMeso(cm.getHiredMerchantMesos());
				cm.setHiredMerchantMesos(0);
				cm.dispose();
			}
		} else {
			if (cm.getHiredMerchantItems(false)) {
				cm.sendOk("Thank you for using my services.");
				cm.dispose();
			} else {
				cm.sendOk("Please make a little space to receive all of your items.");
				cm.dispose();
			}
		}
	}
}