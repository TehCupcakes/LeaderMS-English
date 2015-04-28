/*
* @autor Java
* LeaderMS MapleStory Private Server
* Trocas
*/

var status;
function start() {
    status = -1;//sets status to -1
    action( 1, 0, 0);
}
function action (mode, type , selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        cm.sendSimple("Hello #e#h ##n, my name is Tia, and I am a "+cm.getServerName()+" assistant.\r\nIf you have some cash points you want to exchange, just continue with our conversation.\r\n\r\nIf you already #ehave#n these points, click on exchange. If not, come back later.\r\n\r\nYou have (#e" + cm.getPlayer().getCashPoints() + "#n) CashPoints.\r\n\#L0#Exchange CashPoints#l\r\n\#L1#Exchange Trophies#l\r\n\#L2##r#eHow do I get points?#k#l#n");
    }else if (status == 1) {
        if (selection == 0) {
            cm.sendSimple("                                #e#r<CashPoints - Exchange>#k#n\r\n\r\n#L20#Exchange - 1,000 CashPoints for 2,000 Cash#l\r\n#L21#Exchange - 1,500 CashPoints for 3,000 Cash#l\r\n#L22#Exchange - 2,000 CashPoints for 5,000 Cash#l");
        }else if (selection == 1) {
            cm.sendSimple("                                #e#r<CashPoints - Exchange>#k#n\r\n\r\n#L23#Exchange - 1 Trophy for 5,000 Cash#l\r\n#L24#Exchange - 2 Trophies for 12,000 Cash#l");
        } else if (selection == 2) {
            cm.sendSimple("                                #e#r<CashPoints - Information>#k#n\r\n\r\nCashPoints can be obtained through Cash Quest, which is located somewhere in Kerning City. You should look for NPC \"Cashiro\" and see if it is the correct time to participate in the Cash Quest so you can pick up some points!");
        }
    } else if (status == 2) {
        if (selection == 20) {
            if (cm.getPlayer().getCashPoints() >= 1000) {
                cm.getPlayer().modifyCSPoints(1, 2000);
                cm.getPlayer().gainCashPoints(-1000);
                cm.getPlayer().dropMessage("You gained 2,000 Cash!")
                cm.dispose();
            } else {
                cm.sendOk("You do not have enough CashPoints to exchange!");
                cm.dispose();
            }
        } else if (selection == 21) {
            if (cm.getPlayer().getCashPoints() >= 1500) {
                cm.getPlayer().modifyCSPoints(1, 3000);
                cm.getPlayer().gainCashPoints(-1500);
                cm.getPlayer().dropMessage("You gained 3,000 Cash!")
                cm.dispose();
            } else {
                cm.sendOk("You do not have enough CashPoints to exchange!");
                cm.dispose();
            }
        } else if (selection == 22) {
            if (cm.getPlayer().getCashPoints() >= 2000) {
                cm.getPlayer().modifyCSPoints(1, 5000);
                cm.getPlayer().gainCashPoints(-2000);
                cm.getPlayer().dropMessage("You gained 5,000 Cash!")
                cm.dispose();
            } else {
                cm.sendOk("You do not have enough CashPoints to exchange!");
                cm.dispose();
            }
        } else if (selection  == 23) {
            if (cm.getPlayer().haveItem(4000038)) {
                cm.gainItem(4000038, -1);
                cm.getPlayer().modifyCSPoints(1, 5000);
                cm.getPlayer().dropMessage("You gained 5,000 Cash!")
                cm.dispose();
            } else {
                cm.sendOk("Sorry, you do not have the Trophy needed for the exchange!");
                cm.dispose();
            }
        } else if (selection  == 24) {
            if (cm.haveItem(4000038, 2)) {
                cm.gainItem(4000038, -2);
                cm.getPlayer().modifyCSPoints(1, 12000);
                cm.getPlayer().dropMessage("You gained 12,000 Cash!")
                cm.dispose();
            } else {
                cm.sendOk("Sorry, you do not have the Trophy needed for the exchange!");
                cm.dispose();
            }
        }
    }
}
 
