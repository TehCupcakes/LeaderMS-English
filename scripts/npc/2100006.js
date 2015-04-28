/*
	by ANK - LeaderMS
2100005
*/

/* Natalie
	Ariant VIP Hair/Hair Color Change.
*/
var status = 0;
var beauty = 0;
var hairprice = 1000000;
var haircolorprice = 1000000;
var mhair = Array(30320, 30330, 30150, 30900, 30170, 30180, 30820, 30410, 30460);
var fhair = Array(31400, 31090, 31190, 31620, 31040, 31420, 31330, 31340, 31660);
var hairnew = Array();

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1) 
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendSimple("I'm the head of this hair salon. If you have a #b#t5150027##k or a #b#t5151022##k allow me to take care of your hairdo. Please choose the one you want.\r\n#L0#I want to buy a coupon!#l\r\n#L1#Haircut: #i5150027##t5151022##l\r\n#L2#Dye your hair: #i5150026##t5150026##l");						
		} else if (status == 1) {
			if (selection == 0) {
				beauty = 0;
				cm.sendSimple("Which coupon would you like to buy?\r\n#L0#Haircut for " + hairprice + " mesos: #i5150027##t5150027##l\r\n#L1#Dye your hair for " + haircolorprice + " mesos: #i5150027##t5150027##l");
			} else if (selection == 1) {
				beauty = 1;
				hairnew = Array();
				if (cm.getPlayer().getGender() == 0) {
					for(var i = 0; i < mhair.length; i++) {
						hairnew.push(mhair[i] + parseInt(cm.getPlayer().getHair()
 % 10));
					}
				} 
				if (cm.getPlayer().getGender() == 1) {
					for(var i = 0; i < fhair.length; i++) {
						hairnew.push(fhair[i] + parseInt(cm.getPlayer().getHair()
 % 10));
					}
				}
				cm.sendStyle("I can totally change up your hairstyle and make it look so good. Why don't you change it up a bit? If you have #b#t5150027##k I'll change it for you. Choose the one to your liking~.", hairnew);
			} else if (selection == 2) {
				beauty = 2;
				haircolor = Array();
				var current = parseInt(cm.getPlayer().getHair()
/10)*10;
				for(var i = 0; i < 8; i++) {
					haircolor.push(current + i);
				}
				cm.sendStyle("I can totally change your haircolor and make it look so good. Why don't you change it up a bit? With #b#t5151022##k I'll change it for you. Choose the one to your liking.", haircolor);
			}
		}
		else if (status == 2){  
			cm.dispose();
			if (beauty == 1){
				if (cm.haveItem(5150027) == true){
					cm.gainItem(5150027, -1);
					cm.setHair(hairnew[selection]);
					cm.sendOk("Enjoy your new and improved hairstyle!");
				} else {
					cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't give you a haircut without it. I'm sorry...");
				}
			}
			if (beauty == 2){
				if (cm.haveItem(5151022) == true){
					cm.gainItem(5151022, -1);
					cm.setHair(haircolor[selection]);
					cm.sendOk("Enjoy your new and improved haircolor!");
				} else {
					cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't dye your hair without it. I'm sorry...");
				}
			}
			if (beauty == 0){
				if (selection == 0 && cm.getPlayer().getMeso() >= hairprice) {
					cm.gainMeso(-hairprice);
					cm.gainItem(5150027, 1);
					cm.sendOk("Enjoy!");
				} else if (selection == 1 && cm.getPlayer().getMeso() >= haircolorprice) {
					cm.gainMeso(-haircolorprice);
					cm.gainItem(5151022, 1);
					cm.sendOk("Enjoy!");
				} else {
					cm.sendOk("You don't have enough mesos to buy a coupon!");
				}
			}
		}
	}
}
