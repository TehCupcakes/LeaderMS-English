package handling.channel.handler;

import java.rmi.RemoteException;
import client.MapleCharacter;
import client.MapleClient;
import client.messages.CommandProcessor;
import config.configuration.Configuration;
import handling.AbstractMaplePacketHandler;
import server.AutobanManager;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PartychatHandler extends AbstractMaplePacketHandler {
    // private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PartychatHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int type = slea.readByte(); // 0 for buddys, 1 for partys
        int numRecipients = slea.readByte();
        int recipients[] = new int[numRecipients];
        for (int i = 0; i < numRecipients; i++) {
            recipients[i] = slea.readInt();
        }
        String chattext = slea.readMapleAsciiString();
        if (!(c.getPlayer().isGM())&& chattext.length() > 100) { // 70 = max text for client. remove this if u have edit the client
            AutobanManager.getInstance().autoban(c.getPlayer().getClient(), Configuration.Server_Name + " | " + c.getPlayer().getName() + " had infinite text with a text length of " + chattext.length() + ".");
        } else {
            if (!CommandProcessor.getInstance().processCommand(c, chattext)) {
                MapleCharacter player = c.getPlayer();
                try {
                    if (type == 0) {
                        c.getChannelServer().getWorldInterface().buddyChat(recipients, player.getId(), player.getName(), chattext);
                    } else if (type == 1 && player.getParty() != null) {
                        c.getChannelServer().getWorldInterface().partyChat(player.getParty().getId(), chattext, player.getName());
                    } else if (type == 2 && player.getGuildId() > 0) {
                        c.getChannelServer().getWorldInterface().guildChat(c.getPlayer().getGuildId(), c.getPlayer().getName(), c.getPlayer().getId(), chattext);
                    } else if (type == 3 && player.getGuild() != null) {
                    int allianceId = player.getGuild().getAllianceId();
                    if (allianceId > 0) {
                        c.getChannelServer().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.multiChat(player.getName(), chattext, 3), player.getId(), -1);
                    }
                }
            } catch (RemoteException e) {
                    c.getChannelServer().reconnectWorld();
                }
            }
        }
    }
}