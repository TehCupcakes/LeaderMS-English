importPackage(Packages.server.maps);
importPackage(Packages.net.channel);
importPackage(Packages.tools);

/*
    Zakum Entrance
*/

function enter(pi) {
	var nextMap = 211042400;
	if (!pi.haveItem(4001017)) {
		// do nothing; does not have Eye of Fire
		pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6,"You do not have the Eye of Fire.  You may not face the boss."));
		return false;
	}
	else {
		pi.warp(nextMap,"west00");
		return true;
	}
}