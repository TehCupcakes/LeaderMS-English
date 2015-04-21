var status;

function start() {
    status = -1;
    action(1,0,0);
}

function action(mode, type, selection){
    if (mode == 1)
        status++;
    else {
        cm.dispose();
        return;
    }
    var mapId = cm.getPlayer().getMapId();
    if (mapId == 103000890) {
        if (status == 0) {
            cm.sendNext("I see. Team-work is very important here. Please work together with the members of your group.");
        } else {
            cm.warp(103000000);
            cm.removeAll(4001007);
            cm.removeAll(4001008);
            cm.dispose();
        }
    } else {
        if (status == 0) {
            var outText = "If you leave the map, you will need to redo the entire mission if you want to try again. Do you still want to leave this map?";
            if (mapId == 103000805) {
                outText = "Are you ready to leave this map?";
            }
            cm.sendYesNo(outText);
        } else if (mode == 1) {
            var eim = cm.getPlayer().getEventInstance(); 
            if (eim == null)
                cm.warp(103000890, "st00");
            else if (cm.isLeader()) {
                eim.disbandParty();
            } else
            eim.leftParty(cm.getPlayer());
            cm.dispose();
        }
    }
}