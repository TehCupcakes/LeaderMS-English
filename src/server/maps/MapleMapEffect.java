package server.maps;

import client.MapleClient;
import handling.MaplePacket;
import tools.MaplePacketCreator;

public class MapleMapEffect {
	private String msg;
	private int itemId;
	private boolean active = true;
		
	public MapleMapEffect(String msg, int itemId) {
		this.msg = msg;
		this.itemId = itemId;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public MaplePacket makeDestroyData() {
		return MaplePacketCreator.removeMapEffect();
	}
	
	public MaplePacket makeStartData() {
		return MaplePacketCreator.startMapEffect(msg, itemId, active);
	}
	
	public void sendStartData(MapleClient client) {
		client.getSession().write(makeStartData());
	}
}
