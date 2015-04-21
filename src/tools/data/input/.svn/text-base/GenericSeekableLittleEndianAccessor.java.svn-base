package net.sf.odinms.tools.data.input;

import java.io.IOException;

public class GenericSeekableLittleEndianAccessor extends GenericLittleEndianAccessor implements SeekableLittleEndianAccessor {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GenericSeekableLittleEndianAccessor.class);
    private SeekableInputStreamBytestream bs;

    /**
     * Class constructor
     * Provide a seekable input stream to wrap this object around.
     *
     * @param bs The byte stream to wrap this around.
     */
    public GenericSeekableLittleEndianAccessor(SeekableInputStreamBytestream bs) {
        super(bs);
        this.bs = bs;
    }

    /**
     * Seek the pointer to <code>offset</code>
     *
     * @param offset The offset to seek to.
     * @see net.sf.odinms.tools.data.input.SeekableInputStreamBytestream#seek
     */
    @Override
    public void seek(long offset) {
        try {
            bs.seek(offset);
        } catch (IOException e) {
            log.error("Seek failed", e);
        }
    }

    /**
     * Get the current position of the pointer.
     *
     * @return The current position of the pointer as a long integer.
     * @see net.sf.odinms.tools.data.input.SeekableInputStreamBytestream#getPosition
     */
    @Override
    public long getPosition() {
        try {
            return bs.getPosition();
        } catch (IOException e) {
            log.error("getPosition failed", e);
            return -1;
        }
    }

    /**
     * Skip <code>num</code> number of bytes in the stream.
     *
     * @param num The number of bytes to skip.
     */
    @Override
    public void skip(int num) {
        seek(getPosition() + num);
    }
}