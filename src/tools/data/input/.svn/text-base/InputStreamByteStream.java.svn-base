package net.sf.odinms.tools.data.input;

import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputStreamByteStream implements ByteInputStream {
    private InputStream is;
    private long read = 0;
    private static Logger log = LoggerFactory.getLogger(InputStreamByteStream.class);

    /**
     * Class constructor.
     * Provide an input stream to wrap this around.
     *
     * @param is The input stream to wrap this object around.
     */
    public InputStreamByteStream(InputStream is) {
        this.is = is;
    }

    /**
     * Reads the next byte from the stream.
     *
     * @return Then next byte in the stream.
     */
    @Override
    public int readByte() {
        int temp;
        try {
            temp = is.read();
            if (temp == -1)
                throw new RuntimeException("EOF");
            read++;
            return temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the number of bytes read from the stream.
     *
     * @return The number of bytes read as a long integer.
     */
    @Override
    public long getBytesRead() {
        return read;
    }

    /**
     * Returns the number of bytes left in the stream.
     *
     * @return The number of bytes available for reading as a long integer.
     */
    @Override
    public long available() {
        try {
            return is.available();
        } catch (IOException e) {
            log.error("ERROR", e);
            return 0;
        }
    }
}