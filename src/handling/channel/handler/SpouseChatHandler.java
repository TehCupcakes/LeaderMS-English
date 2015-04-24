package handling.channel.handler;

import java.rmi.RemoteException;
import client.MapleCharacter;
import client.MapleClient;
import client.messages.CommandProcessor;
import handling.AbstractMaplePacketHandler;
import handling.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;


public class SpouseChatHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
            String recipient = slea.readMapleAsciiString();
            String text = slea.readMapleAsciiString();
			
			if (!CommandProcessor.getInstance().processCommand(c, text)) {
				MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
				if (player != null) {
					player.getClient().getSession().write(MaplePacketCreator.toSpouse(c.getPlayer().getName(), text, 5));
					c.getSession().write(MaplePacketCreator.toSpouse(c.getPlayer().getName(), text, 4));
				} else { // not found
					try {
						if (ChannelServer.getInstance(c.getChannel()).getWorldInterface().isConnected(recipient)) {
							ChannelServer.getInstance(c.getChannel()).getWorldInterface().spouse(c.getPlayer().getName(), recipient, c.getChannel(), text);
							c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
						} else {
							c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
						}
					} catch (RemoteException e) {
						c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
						c.getChannelServer().reconnectWorld();
					}
				}
			}
                }
}