importPackage(Packages.server.maps);
importPackage(Packages.handling.channel);
importPackage(Packages.tools.packet);

function enter(pi) {
    var papuMap = ChannelServer.getInstance(pi.getPlayer().getClient().getChannel()).getMapFactory().getMap(220080001);
    if (papuMap.getCharacters().isEmpty()) {
        sendMessage(pi,"The room is empty.  A perfect opportunity to challenge the boss.");
        var mapobjects = papuMap.getMapObjects();
        var iter = mapobjects.iterator();
        while (iter.hasNext()) {
            o = iter.next();
            if (o.getType() == MapleMapObjectType.MONSTER){
                papuMap.removeMapObject(o);
            }
        }
        papuMap.resetReactors();
    }
    else { // someone is inside
        var mapobjects = papuMap.getMapObjects();
        var boss = null;
        var iter = mapobjects.iterator();
        while (iter.hasNext()) {
            o = iter.next();
            if (o.getType() == MapleMapObjectType.MONSTER){
                boss = o;
            }
        }
        if (boss != null) {
            sendMessage(pi,"Someone is fighting " + boss.getName() + ".");
            return false;
        }
    }
    pi.warp(220080001, "st00");
    return true;
}

function sendMessage(pi,message) {
    pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, message));
}