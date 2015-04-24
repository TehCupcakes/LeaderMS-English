package handling.channel.handler;

import java.net.InetAddress;
import java.rmi.RemoteException;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import java.io.IOException;
import handling.AbstractMaplePacketHandler;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import handling.world.MapleMessengerCharacter;
import handling.world.remote.WorldChannelInterface;
import server.MapleTrade;
import server.PlayerInteraction.HiredMerchant;
import server.PlayerInteraction.IPlayerInteractionManager;
import server.PlayerInteraction.MaplePlayerShop;
import server.PublicChatHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeChannelHandler extends AbstractMaplePacketHandler {

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int channel = slea.readByte() + 1;
        changeChannel(channel, c);
    }

    public static void changeChannel(int channel, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (!player.isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (PublicChatHandler.getPublicChatHolder().containsKey(player.getId())) {
            PublicChatHandler.getPublicChatHolder().remove(player.getId());
            PublicChatHandler.getPublicChatHolder().put(player.getId(), channel);
        }
        String ip = ChannelServer.getInstance(c.getChannel()).getIP(channel);
        String[] socket = ip.split(":");
		if (c.getPlayer().getTrade() != null) {
			MapleTrade.cancelTrade(c.getPlayer());
		}
		c.getPlayer().cancelMagicDoor();
		if (c.getPlayer().getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }
        if (c.getPlayer().getBuffedValue(MapleBuffStat.PUPPET) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
        if (c.getPlayer().getBuffedValue(MapleBuffStat.MORPH) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        }
        if (c.getPlayer().getBuffedValue(MapleBuffStat.COMBO) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.COMBO);
        }
        if (c.getPlayer().getBuffedValue(MapleBuffStat.SUMMON) != null) {
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
        }
        if (!c.getPlayer().getDiseases().isEmpty()) {
            c.getPlayer().dispelDebuffs();
        }
		try {
			WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
			wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
			wci.addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
		} catch (RemoteException e) {
			//log.info("RemoteException: {}", e);
			c.getChannelServer().reconnectWorld();
		}
		c.getPlayer().saveToDB(true, true);
		if (c.getPlayer().getCheatTracker() != null)
			c.getPlayer().getCheatTracker().dispose();
		
		if (c.getPlayer().getMessenger() != null) {
			MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
			try {
				WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
				wci.silentLeaveMessenger(c.getPlayer().getMessenger().getId(), messengerplayer);
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
		}
		
		c.getPlayer().getMap().removePlayer(c.getPlayer());
		ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
		c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
		try {
			MaplePacket packet = MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
			c.getSession().write(packet);
			// c.getSession().close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
