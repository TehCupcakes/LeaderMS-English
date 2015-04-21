package net.sf.odinms.tools.data.input;

import java.awt.Point;
import net.sf.odinms.tools.data.output.LittleEndianWriter;

public class StreamUtil {
    /**
     * Read a 2-D coordinate of short integers (x, y).
     *
     * @param lea The accessor to read the point from.
     * @return A <code>point</code> object read from the accessor.
     */
    public static Point readShortPoint(LittleEndianAccessor lea) {
        int x = lea.readShort();
        int y = lea.readShort();
        return new Point(x, y);
    }

    /**
     * Writes a 2-D coordinate of short integers (x, y).
     *
     * @param lew The stream-writer to write the point to.
     * @param p The point to write to the stream-writer.
     */
    public static void writeShortPoint(LittleEndianWriter lew, Point p) {
        lew.writeShort(p.x);
        lew.writeShort(p.y);
    }
}