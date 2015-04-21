package net.sf.odinms.tools.data.output;

import java.io.ByteArrayOutputStream;
import net.sf.odinms.net.ByteArrayMaplePacket;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.tools.HexTool;

public class MaplePacketLittleEndianWriter extends GenericLittleEndianWriter {
    private ByteArrayOutputStream baos;

    /**
     * Constructor - initializes this stream with a default size.
     */
    public MaplePacketLittleEndianWriter() {
        this(32);
    }

    /**
     * Constructor - initializes this stream with size <code>size</code>.
     *
     * @param size The size of the underlying stream.
     */
    public MaplePacketLittleEndianWriter(int size) {
        this.baos = new ByteArrayOutputStream(size);
        setByteOutputStream(new BAOSByteOutputStream(baos));
    }

    /**
     * Gets a <code>MaplePacket</code> instance representing this
     * sequence of bytes.
     *
     * @return A <code>MaplePacket</code> with the bytes in this stream.
     */
    public MaplePacket getPacket() {
        //MaplePacket packet = new ByteArrayMaplePacket(baos.toByteArray());
        //System.out.println("Packet to be sent:\n" +packet.toString() + "\n\n");
        return new ByteArrayMaplePacket(baos.toByteArray());
    }

    /**
     * Changes this packet into a human-readable hexadecimal stream of bytes.
     *
     * @return This packet as hex digits.
     */
    @Override
    public String toString() {
        return HexTool.toString(baos.toByteArray());
    }
}