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

import static client.BuddyList.BuddyOperation.ADDED;
import static client.BuddyList.BuddyOperation.DELETED;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.BuddyList;
import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MapleClient;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import database.DatabaseConnection;
import handling.AbstractMaplePacketHandler;
import handling.channel.remote.ChannelWorldInterface;
import handling.world.remote.WorldChannelInterface;
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class BuddylistModifyHandler extends AbstractMaplePacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BuddylistModifyHandler.class);
	
	private static class CharacterIdNameBuddyCapacity extends CharacterNameAndId {
		private int buddyCapacity;
		
		public CharacterIdNameBuddyCapacity(int id, String name, int buddyCapacity) {
			super(id, name);
			this.buddyCapacity = buddyCapacity;
		}
		
		public int getBuddyCapacity() {
			return buddyCapacity;
		}
	}
	
	private void nextPendingRequest(MapleClient c) {
		CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
		if (pendingBuddyRequest != null) {
			c.getSession().write(MaplePacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), pendingBuddyRequest.getName()));
		}
	}

	private CharacterIdNameBuddyCapacity getCharacterIdAndNameFromDatabase(String name) throws SQLException {
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = con.prepareStatement("SELECT id, name, buddyCapacity FROM characters WHERE name LIKE ?");
		ps.setString(1, name);
		ResultSet rs = ps.executeQuery();
		CharacterIdNameBuddyCapacity ret = null;
		if (rs.next()) {
			ret = new CharacterIdNameBuddyCapacity(rs.getInt("id"), rs.getString("name"), rs.getInt("buddyCapacity"));
		}
		rs.close();
		ps.close();
		return ret;
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int mode = slea.readByte();
		MapleCharacter player = c.getPlayer();
		WorldChannelInterface worldInterface = c.getChannelServer().getWorldInterface();
		BuddyList buddylist = player.getBuddylist();
		if (mode == 1) { // add
			String addName = slea.readMapleAsciiString();
			BuddylistEntry ble = buddylist.get(addName);
			if (ble != null && !ble.isVisible()) {
				// Already on BL
				c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 13));
			} else if (buddylist.isFull()) {
				// Your BL is Full
				c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 11));
			} else {
				try {
					CharacterIdNameBuddyCapacity charWithId = null;
					int channel;
					MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterByName(addName);
					if (otherChar != null) {
						channel = c.getChannel();
						charWithId = new CharacterIdNameBuddyCapacity(otherChar.getId(), otherChar.getName(), otherChar.getBuddylist().getCapacity());
					} else {
						channel = worldInterface.find(addName);
						charWithId = getCharacterIdAndNameFromDatabase(addName);
					}

					if (charWithId != null) {
						BuddyAddResult buddyAddResult = null;
						if (channel != -1) {
							ChannelWorldInterface channelInterface = worldInterface.getChannelInterface(channel);
							buddyAddResult = channelInterface.requestBuddyAdd(addName, c.getChannel(), player.getId(), player.getName());
						} else {
							Connection con = DatabaseConnection.getConnection();
							PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as buddyCount FROM buddies WHERE characterid = ? AND pending = 0");
							ps.setInt(1, charWithId.getId());
							ResultSet rs = ps.executeQuery();
							if (!rs.next()) {
								throw new RuntimeException("Result set expected");
							} else {
								int count = rs.getInt("buddyCount");
								if (count >= charWithId.getBuddyCapacity()) {
									buddyAddResult = BuddyAddResult.BUDDYLIST_FULL;
								}
							}
							rs.close();
							ps.close();
							ps = con.prepareStatement("SELECT pending FROM buddies WHERE characterid = ? AND buddyid = ?");
							ps.setInt(1, charWithId.getId());
							ps.setInt(2, player.getId());
							rs = ps.executeQuery();
							if (rs.next()) {
								buddyAddResult = BuddyAddResult.ALREADY_ON_LIST;
							}
							rs.close();
							ps.close();
						}
						if (buddyAddResult == BuddyAddResult.BUDDYLIST_FULL) {
							// User's Buddy List is Full
							c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 12));
						} else {
							int displayChannel = -1;
							int otherCid = charWithId.getId();
							if (buddyAddResult == BuddyAddResult.ALREADY_ON_LIST && channel != -1) {
								displayChannel = channel;
								notifyRemoteChannel(c, channel, otherCid, ADDED);
							} else if (buddyAddResult != BuddyAddResult.ALREADY_ON_LIST && channel == -1) {
								Connection con = DatabaseConnection.getConnection();
								PreparedStatement ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, 1)");
								ps.setInt(1, charWithId.getId());
								ps.setInt(2, player.getId());
								ps.executeUpdate();
								ps.close();
							}
							buddylist.put(new BuddylistEntry(charWithId.getName(), otherCid, displayChannel, true));
							c.getSession().write(MaplePacketCreator.updateBuddylist(buddylist.getBuddies()));
						}
					} else {
						// Not Registered
						c.getSession().write(MaplePacketCreator.buddylistMessage((byte) 15));
					}
				} catch (RemoteException e) {
					log.error("REMOTE THROW", e);
				} catch (SQLException e) {
					log.error("SQL THROW", e);
				}
			}
		} else if (mode == 2) { // accept buddy
			int otherCid = slea.readInt();
			if (!buddylist.isFull()) {
				try {
					int channel = worldInterface.find(otherCid);
					String otherName = null;
					MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterById(otherCid);
					if (otherChar == null) {
						Connection con = DatabaseConnection.getConnection();
						PreparedStatement ps = con.prepareStatement("SELECT name FROM characters WHERE id = ?");
						ps.setInt(1, otherCid);
						ResultSet rs = ps.executeQuery();
						if (rs.next()) {
							otherName = rs.getString("name");
						}
						rs.close();
						ps.close();
					} else {
						otherName = otherChar.getName();
					}
					if (otherName != null) {
						buddylist.put(new BuddylistEntry(otherName, otherCid, channel, true));
						c.getSession().write(MaplePacketCreator.updateBuddylist(buddylist.getBuddies()));
						notifyRemoteChannel(c, channel, otherCid, ADDED);
					}
				} catch (RemoteException e) {
					log.error("REMOTE THROW", e);
				} catch (SQLException e) {
					log.error("SQL THROW", e);
				}
			}
			nextPendingRequest(c);
		} else if (mode == 3) { // delete
			int otherCid = slea.readInt();
			if (buddylist.containsVisible(otherCid)) {
				try {
					notifyRemoteChannel(c, worldInterface.find(otherCid), otherCid, DELETED);
				} catch (RemoteException e) {
					log.error("REMOTE THROW", e);
				}
			}
			buddylist.remove(otherCid);
			c.getSession().write(MaplePacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));
			nextPendingRequest(c);
		}
	}

	private void notifyRemoteChannel(MapleClient c, int remoteChannel, int otherCid, BuddyOperation operation)
																												throws RemoteException {
		WorldChannelInterface worldInterface = c.getChannelServer().getWorldInterface();
		MapleCharacter player = c.getPlayer();

		if (remoteChannel != -1) {
			ChannelWorldInterface channelInterface = worldInterface.getChannelInterface(remoteChannel);
			channelInterface.buddyChanged(otherCid, player.getId(), player.getName(), c.getChannel(), operation);
		}
	}
}
