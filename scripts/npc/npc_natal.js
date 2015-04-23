var status = 0;

function start() {
    cm.sendNext("What a beautiful day. Why not get a gift? (...)");
}

function action(mode, type, selection) {
     if (cm.haveItem(3010045) == true) {
                cm.sendOk("Sorry, you already received your gift!");
                cm.dispose();
            } else {
            cm.gainItem(3010045, 1);
            cm.sendOk("Congratulations , you have received a chair!");
            cm.getPlayer().dropMessage("[Team "+cm.getServerName()+"] Merry Christmas and Happy Holidays! <3");
            cm.dispose();
        }
}