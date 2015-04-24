package handling;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;

public interface MaplePacketHandler {
	void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c);
	boolean validateState(MapleClient c);
}
