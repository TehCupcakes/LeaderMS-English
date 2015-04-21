package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Mats
 */

public class EnableActionHandler extends AbstractMaplePacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnableActionHandler.class);    

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		try {
			c.getPlayer().saveToDB (true, true);
		} catch (Exception ex) {
			log.error("Error updating player", ex);
		}
	}
}
