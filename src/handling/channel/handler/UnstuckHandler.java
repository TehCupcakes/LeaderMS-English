/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.MaplePacketCreator;

/**
 *
 * @author David
 */
public class UnstuckHandler extends AbstractMaplePacketHandler {
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		c.getSession().write(MaplePacketCreator.enableActions());
	}
}
