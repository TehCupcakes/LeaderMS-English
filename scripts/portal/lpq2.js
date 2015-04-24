importPackage(Packages.server.maps);
importPackage(Packages.handling.channel);
importPackage(Packages.tools.packet);

/*
LudiPQ - 1 - 2 Portal
@author Jvlaple
*/

function enter(pi) {
    var nextMap = 922010200;
    var eim = pi.getPlayer().getEventInstance()
    var target = eim.getMapInstance(nextMap);
    var targetPortal = target.getPortal("st00");
    var avail = eim.getProperty("1stageclear");
    if (avail == null) {
        pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Some seal is blocking this door."));
        return false;
    }
    else {
        pi.getPlayer().changeMap(target, targetPortal);
        return true;
    }
}