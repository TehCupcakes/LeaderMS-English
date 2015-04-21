package server.maps;

import java.rmi.RemoteException;
import java.util.List;
import client.MapleCharacter;
import net.MaplePacket;
import server.TimerManager;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;

public class MapleTVEffect {

    public static MaplePacket packet;
    public static boolean active;
    private ChannelServer cserv;

    public MapleTVEffect(MapleCharacter user, MapleCharacter partner, List<String> msg, int type) {
        cserv = user.getClient().getChannelServer();
        packet = MaplePacketCreator.sendTV(user, msg, type <= 2 ? type : type - 3, partner);
        broadCastTV(true);
        scheduleCancel(type);
    }

    private void broadCastTV(boolean active) {
        MapleTVEffect.active = active;
        try {
            if (active) {
                cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.enableTV().getBytes());
                cserv.getWorldInterface().broadcastMessage(null, packet.getBytes());
            } else {
                cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.removeTV().getBytes());
                packet = null;
            }
        } catch (RemoteException noob) {
            cserv.reconnectWorld();
        }
    }

    public static int getMapleTVDuration(int type) {
        switch (type) {
            case 1:
            case 4:
                return 30000;
            case 2:
            case 5:
                return 60000;
            default:
                return 15000;
        }
    }

    private void scheduleCancel(int type) {
        TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                broadCastTV(false);
            }
        }, getMapleTVDuration(type));
    }
}