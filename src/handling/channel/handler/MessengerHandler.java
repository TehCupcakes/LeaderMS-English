/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package handling.channel.handler;

import java.rmi.RemoteException;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.channel.ChannelServer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.remote.WorldChannelInterface;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class MessengerHandler extends AbstractMaplePacketHandler {
	
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		String input;
		byte mode = slea.readByte();
		MapleCharacter player = c.getPlayer();
		WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
		MapleMessenger messenger = player.getMessenger();
 
		switch (mode) {
			case 0x00: // open
				if (messenger == null) {
					int messengerid = slea.readInt();
					if (messengerid == 0) { // create
						try {
							MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player);
							messenger = wci.createMessenger(messengerplayer);
							player.setMessenger(messenger);
							player.setMessengerPosition(0);
						} catch (RemoteException e) {
							c.getChannelServer().reconnectWorld();
						}	
					} else { // join
						try {
							messenger = wci.getMessenger(messengerid);
							int position = messenger.getLowestPosition();
							MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player, position); 
							if (messenger != null) {
								if (messenger.getMembers().size() < 3) {
									player.setMessenger(messenger);
									player.setMessengerPosition(position);
									wci.joinMessenger(messenger.getId(), messengerplayer, player.getName(), messengerplayer.getChannel());
								} 
							} 
						} catch (RemoteException e) {
							c.getChannelServer().reconnectWorld();
						}
					}
				}
				break;
			case 0x02: // exit
				if (messenger != null) {
					MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player);
					try {
						wci.leaveMessenger(messenger.getId(), messengerplayer);
					} 
					catch (RemoteException e) {
						c.getChannelServer().reconnectWorld();
					}
					player.setMessenger(null);
					player.setMessengerPosition(4);
				}
				break;
			case 0x03: // invite
				if (messenger.getMembers().size() < 3) {
					input = slea.readMapleAsciiString();	
					MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);
					if (target != null) {
						if (target.getMessenger() == null) {
							target.getClient().getSession().write(MaplePacketCreator.messengerInvite(c.getPlayer().getName(), messenger.getId()));
							c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 1));
						} else {
							c.getSession().write(MaplePacketCreator.messengerChat(player.getName() + " : " + input + " is already using Maple Messenger"));
						}
					} else {
						try {
							if (ChannelServer.getInstance(c.getChannel()).getWorldInterface().isConnected(input)) {
								ChannelServer.getInstance(c.getChannel()).getWorldInterface().messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel());
							} else {
								c.getSession().write(MaplePacketCreator.messengerNote(input, 4, 0));
							}
						} catch (RemoteException e) {
							c.getChannelServer().reconnectWorld();
						}
					}
				} else {
					c.getSession().write(MaplePacketCreator.messengerChat(player.getName() + " : You cannot have more than 3 people in the Maple Messenger"));
				}
				break;	
			case 0x05: // decline
				String targeted = slea.readMapleAsciiString();
				MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
				if (target != null) {
					if (target.getMessenger() != null) {
						target.getClient().getSession().write(MaplePacketCreator.messengerNote(player.getName(), 5, 0));
					}
				} else {
					try {
						wci.declineChat(targeted, player.getName());
					} catch (RemoteException e) {
						c.getChannelServer().reconnectWorld();
					}
				}
				break;
			case 0x06: // message
				if (messenger != null) {
					MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(player);
					input = slea.readMapleAsciiString();
					try {
						wci.messengerChat(messenger.getId(), input, messengerplayer.getName());
					} catch (RemoteException e) {
						c.getChannelServer().reconnectWorld();
					}
				}
				break;
		}
	}
}