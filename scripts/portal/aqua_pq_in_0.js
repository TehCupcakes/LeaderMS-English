importPackage(Packages.tools.packet);


function enter(pi) {
	pi.warp(230040300, 5);
	pi.getPlayer().getClient().getSession().write(MaplePacketCreator.musicChange("Bgm12/AquaCave"));
	return true;
}