package net.login.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ServerlistRequestHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(MaplePacketCreator.getServerList(0, LoginServer.getInstance().getServerName(), LoginServer.getInstance().getLoad()));
        if (LoginServer.getInstance().twoWorldsActive()) c.getSession().write(MaplePacketCreator.getServerList(1, LoginServer.getInstance().getServerName(), LoginServer.getInstance().getLoad()));
        c.getSession().write(MaplePacketCreator.getEndOfServerList());
    }
}