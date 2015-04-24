importPackage(Packages.server.maps);
importPackage(Packages.handling.channel);
importPackage(Packages.tools.packet);

function enter(pi) {
    var mapid = 541010100;
    var map = ChannelServer.getInstance(pi.getPlayer().getClient().getChannel()).getMapFactory().getMap(mapid);
    var mapchars = map.getCharacters();
    if (mapchars.isEmpty()) {
        var mapobjects = map.getMapObjects();
        var iter = mapobjects.iterator();
        
        while (iter.hasNext()) {
            o = iter.next();
            if (o.getType() == MapleMapObjectType.MONSTER){
                map.removeMapObject(o);
            }
        }
        map.resetReactors();
    } else {
        var mapobjects = map.getMapObjects();
        var boss = null;
        var iter = mapobjects.iterator();
        while (iter.hasNext()) {
            o = iter.next();
            if (o.getType() == MapleMapObjectType.MONSTER){
                boss = o;
            }
        }
        if (boss != null) {
            pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "The battle against the boss has already begun, so you may not enter this place."));
            return false;
        }
    }
    pi.warp(541010100, "sp");
    return true;
}