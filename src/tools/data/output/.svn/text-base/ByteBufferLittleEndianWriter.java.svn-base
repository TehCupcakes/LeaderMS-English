package net.sf.odinms.tools.data.output;

import org.apache.mina.common.ByteBuffer;

public class ByteBufferLittleEndianWriter extends GenericLittleEndianWriter {
    private ByteBuffer bb;

    /**
     * Constructor - Constructs this object as fixed at the default size.
     */
    public ByteBufferLittleEndianWriter() {
        this(50, true);
    }

    /**
     * Constructor - Constructs this object as fixed at size <code>size</code>.
     *
     * @param size The size of the fixed bytebuffer.
     */
    public ByteBufferLittleEndianWriter(int size) {
        this(size, false);
    }

    /**
     * Constructor - Constructs this object as optionally fixed at size <code>size</code>.
     *
     * @param initialSize The size of the fixed bytebuffer.
     * @param autoExpand Expand if needed.
     */
    public ByteBufferLittleEndianWriter(int initialSize, boolean autoExpand) {
        bb = ByteBuffer.allocate(initialSize);
        bb.setAutoExpand(autoExpand);
        setByteOutputStream(new ByteBufferOutputstream(bb));
    }

    /**
     * Returns a flipped version of the underlying bytebuffer.
     *
     * @return A flipped version of the underlying bytebuffer.
     */
    public ByteBuffer getFlippedBB() {
        return bb.flip();
    }

    /**
     * Returns the underlying bytebuffer.
     *
     * @return The underlying bytebuffer.
     */
    public ByteBuffer getByteBuffer() {
        return bb;
    }
}