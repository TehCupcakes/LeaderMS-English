package net.sf.odinms.tools.data.input;

import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomAccessByteStream implements SeekableInputStreamBytestream {
    private RandomAccessFile raf;
    private long read = 0;
    private static Logger log = LoggerFactory.getLogger(RandomAccessByteStream.class);

    /**
     * Class constructor. Wraps this object around a RandomAccessFile.
     *
     * @param raf
     *            The RandomAccessFile instance to wrap this around.
     * @see java.io.RandomAccessFile
     */
    public RandomAccessByteStream(RandomAccessFile raf) {
        super();
        this.raf = raf;
    }

    /**
     * Reads a byte off of the file.
     *
     * @return The byte read as an integer.
     */
    @Override
    public int readByte() {
        int temp;
        try {
            temp = raf.read();
            if (temp == -1)
                throw new RuntimeException("EOF");
            read++;
            return temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see net.sf.odinms.tools.data.input.SeekableInputStreamBytestream#seek(long)
     */
    @Override
    public void seek(long offset) throws IOException {
        raf.seek(offset);
    }

    /**
     * @see net.sf.odinms.tools.data.input.SeekableInputStreamBytestream#getPosition()
     */
    @Override
    public long getPosition() throws IOException {
        return raf.getFilePointer();
    }

    /**
     * Get the number of bytes read.
     *
     * @return The number of bytes read as a long integer.
     */
    @Override
    public long getBytesRead() {
        return read;
    }

    /**
     * Get the number of bytes available for reading.
     *
     * @return The number of bytes available for reading as a long integer.
     */
    @Override
    public long available() {
        try {
            return raf.length() - raf.getFilePointer();
        } catch (IOException e) {
            log.error("ERROR", e);
            return 0;
        }
    }
}