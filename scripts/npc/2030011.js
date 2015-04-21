var status = 0;

importPackage(Packages.client);

function start() {
	status = -1;
	action(1, 0, 0);
}

var PQItems = new Array(4001015, 4001016, 4001018);

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 0 && mode == 0) {
			cm.sendOk("Ok, keep preservering!");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status==0) {
			cm.sendNext("Tough luck there eh? You can always come back if your'e prepared... but anyway, I will take all the items you obtained from the PQ :)");				
		}else if (status == 1){
			if (cm.getPlayer().isGM() == false) {
				for (var i = 0; i < PQItems.length; i++) {
					cm.removeAll(PQItems[i]);
				}
			}
			cm.warp(211042300);
			cm.dispose();
		}
	}
}	