package handling.mina;

import client.MapleClient;
import java.util.concurrent.locks.Lock;
import handling.MaplePacket;
import tools.MapleCustomEncryption;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import tools.MapleAESOFB;

public class MaplePacketEncoder implements ProtocolEncoder {

    @Override
    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
	final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);

	if (client != null) {
	    final Lock mutex = client.getLock();

	    mutex.lock();
	    try {
		final MapleAESOFB send_crypto = client.getSendCrypto();

		final byte[] inputInitialPacket = ((MaplePacket) message).getBytes();
		final byte[] unencrypted = new byte[inputInitialPacket.length];
		System.arraycopy(inputInitialPacket, 0, unencrypted, 0, inputInitialPacket.length); // Copy the input > "unencrypted"

		final byte[] ret = new byte[unencrypted.length + 4]; // Create new bytes with length = "unencrypted" + 4
		final byte[] header = send_crypto.getPacketHeader(unencrypted.length);

		MapleCustomEncryption.encryptData(unencrypted); // Encrypting Data
		send_crypto.crypt(unencrypted); // Crypt it with IV

		System.arraycopy(header, 0, ret, 0, 4); // Copy the header > "Ret", first 4 bytes
		System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length); // Copy the unencrypted > "ret"
		out.write(ByteBuffer.wrap(ret));
	    } finally {
		mutex.unlock();
	    }
	} else { // no client object created yet, send unencrypted (hello)
	    out.write(ByteBuffer.wrap(((MaplePacket) message).getBytes()));
	}
    }

    @Override
    public void dispose(IoSession session) throws Exception {
	// nothing to do
    }
}

