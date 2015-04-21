package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CancelChairHandler extends AbstractMaplePacketHandler {
	
	public CancelChairHandler() {
	}
	
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int id = slea.readShort();
		
		if (id == -1) { // Cancel Chair
			c.getPlayer().setChair(0);
			c.getSession().write(MaplePacketCreator.cancelChair());
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showChair(c.getPlayer().getId(), 0), false);
		} else { // Use In-Map Chair
			c.getPlayer().setChair(id);
			c.getSession().write(MaplePacketCreator.cancelChair(id));
		}
	}
}
