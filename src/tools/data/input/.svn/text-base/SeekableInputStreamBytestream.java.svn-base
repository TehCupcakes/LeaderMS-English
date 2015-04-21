package net.sf.odinms.tools.data.input;

import java.io.IOException;

public interface SeekableInputStreamBytestream extends ByteInputStream {
    /**
     * Seeks the stream by the specified offset.
     *
     * @param offset
     *            Number of bytes to seek.
     * @throws IOException
     */
    void seek(long offset) throws IOException;

    /**
     * Gets the current position of the stream.
     *
     * @return The stream position as a long integer.
     * @throws IOException
     */
    long getPosition() throws IOException;
}