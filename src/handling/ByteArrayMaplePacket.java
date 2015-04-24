package handling;

import tools.HexTool;

public class ByteArrayMaplePacket implements MaplePacket {
    private byte[] data;
    private Runnable onSend;

    public ByteArrayMaplePacket(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getBytes() {
        return data;
    }

    @Override
    public String toString() {
        return HexTool.toString(data);
    }

    public Runnable getOnSend() {
        return onSend;
    }

    public void setOnSend(Runnable onSend) {
        this.onSend = onSend;
    }
}
