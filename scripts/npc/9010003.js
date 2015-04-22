/*
 * LeaderMS Private Server
 * Servidor Brasileiro 2011-2014
 * JavaScriptz <javascriptz@leaderms.com.br>
 * Troca de LeaderPoints
 */

importPackage(Packages.config.configuration);

var status;
function start() {
	status = -1;
	action( 1, 0, 0);
}
function action (mode, type , selection) {
	if (mode == 1) {
		status++; 
	} else {
		status--;
	}
	if(cm.getChar().getMapId() == 910000000) {
		if (status == 0) {
			cm.sendSimple("Hello #e#h ##n, I am Ria, a "+Configuration.Server_Name+" assistant.\r\nIf you have some cash points you want to exchange, just continue with our conversation.\r\n\r\nIf you already have points, click exchange. If not, come back later.\r\n\r\nYou have (#e" + cm.getPlayer().getCSPoints(4) + "#n) Cash / LeaderPoints (#e" + cm.getPlayer().getCSPoints(2) + "#n).\r\n\#L0#Exchange Cash#l\r\n\#L1#Exchange LeaderPoints#l\r\n\#L3#Exchange Mesos#l\r\n\#L2##r#eHow to get points?#k#l#n");
		} else if (status == 1) {
			if (selection == 0) {
				cm.sendSimple("                                #e#r<Cash - Exchange>#k#n\r\n\r\n#L20#Exchange - 1,500 (Cash) for (1) Megaphone#l");
			} else if (selection == 1) {
				cm.sendSimple("                                #e#r<LeaderPoints - Exchange>#k#n\r\n\r\n#L23#Exchange - 4 (LeaderPoints) for (1) Incubator#l");
			} else if (selection == 3) {
				cm.sendSimple("                                #e#r<Mesos - Exchange>#k#n\r\n\r\n#L24#Exchange - 100,000 (Mesos) for (1) Pygmy egg#l");
			} else if (selection == 2) {
				cm.sendSimple("#e#r<Cash - Info>#k#n\r\nCash can be obtained through the monsters in game, which have a random chance to drop an NX card.#e#r\r\n\r\n<LeaderPoints - Info>#k#n\r\nThese are donation points, which can be obtained on our site.");
			}
		} else if (status == 2) {
			if (selection == 20) {
				if (cm.getPlayer().getCSPoints(4) >= 1500) {
					cm.getPlayer().modifyCSPoints(4, -1500);
					cm.gainItem(5076000, 1);
					cm.getPlayer().dropMessage("[Cash] You lost (1,500) Cash!")
					cm.getPlayer().dropMessage("You gained a Megaphone!")
					cm.dispose();
				} else {
					cm.sendOk("You do not have enough cash to exchange!");
					cm.dispose();
				}
			} else if (selection == 23) {
				if (cm.getPlayer().getCSPoints(2) >= 4) {
					cm.getPlayer().modifyCSPoints(2, -4);
					cm.gainItem(5060002, 1)
					cm.getPlayer().dropMessage("[LeaderPoints] You lost (-4) LeaderPoints!")
					cm.sendOk("Thank you. Enjoy your new item!");
					cm.dispose();
				} else {
					cm.sendOk("You do not have enough LeaderPoints to exchange!");
					cm.dispose();
				}
			} else if (selection == 24) {
				if (cm.getPlayer().getMeso() > 100000) {
					cm.gainMeso(-100000);
					cm.gainItem(4170000, 1)
					cm.sendOk("Thank you. Enjoy your new item!");
					cm.dispose();
				} else {
					cm.sendOk("You do not have enough Mesos to exchange!");
					cm.dispose();
				}
			}
		}
	}
}
 