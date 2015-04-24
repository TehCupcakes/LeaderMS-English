/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author David
 */
public class GrenadeHandler extends AbstractMaplePacketHandler {
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		//oid = 102
		//Header: 64 00 (short)
		//18 
		//00 
		//00 
		//00 
		//35 00 00 00
		//86 01 00 00
	}
}
