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

package client.messages.commands;

import java.rmi.RemoteException;
import java.util.List;

import static client.messages.CommandProcessor.getNamedIntArg;
import static client.messages.CommandProcessor.joinAfterString;

import java.text.DateFormat;
import java.util.Calendar;
import java.net.InetAddress;

import java.util.Arrays;
import java.util.TimeZone;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.messages.ServernoticeMapleClientMessageCallback;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.MapleCharacter;
import client.messages.CommandProcessor;
import config.configuration.Configuration;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.remote.CheaterData;
import net.world.remote.WorldChannelInterface;
import net.world.remote.WorldLocation;
import server.MaplePortal;
import server.MapleTrade;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.StringUtil;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import client.Equip;
import client.IItem;
import static client.messages.CommandProcessor.getOptionalIntArg;

public class GMCommand implements Command {
	private static int getNoticeType(String typestring) {
		if (typestring.equals("n")) {
			return 0;
		} else if (typestring.equals("p")) {
			return 1;
		} else if (typestring.equals("l")) {
			return 2;
		} else if (typestring.equals("nv")) {
			return 5;
		} else if (typestring.equals("v")) {
			return 5;
		} else if (typestring.equals("b")) {
			return 6;
		}
		return -1;
	}

	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {
         MapleCharacter player = c.getPlayer();
         ChannelServer cserv = c.getChannelServer();
         
	    if(splitted[0].equals("!notice")) {
		int joinmod = 1;
		int range = -1;
		if (splitted[1].equals("m")) {
			range = 0;
		} else if (splitted[1].equals("c")) {
			range = 1;
		} else if (splitted[1].equals("w")) {
			range = 2;
		}

		int tfrom = 2;
		if (range == -1) {
			range = 2;
			tfrom = 1;
		}
		int type = getNoticeType(splitted[tfrom]);
		if (type == -1) {
			type = 0;
			joinmod = 0;
		}
		String prefix = "";
		if (splitted[tfrom].equals("nv")) {
			prefix = "[Notice] ";
		}
		joinmod += tfrom;
                String outputMessage = StringUtil.joinStringFrom(splitted, joinmod);
                if (outputMessage.equalsIgnoreCase("!array"))
                  outputMessage = c.getChannelServer().getArrayString();

		MaplePacket packet = MaplePacketCreator.serverNotice(type, prefix + outputMessage);
		if (range == 0) {
			c.getPlayer().getMap().broadcastMessage(packet);
		} else if (range == 1) {
			ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
		} else if (range == 2) {
			try {
				ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(
					c.getPlayer().getName(), packet.getBytes());
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
			}
		}
	    } else if (splitted[0].equals("!me")) {
	    String prefix = "[" + c.getPlayer().getName() + "] ";
	    String message = prefix + StringUtil.joinStringFrom(splitted, 1);
	    c.getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(6, message));
        } else if (splitted[0].equals("!ban")) {
			if (splitted.length < 3) {
				throw new IllegalCommandSyntaxException(3);
			}
			String originalReason = StringUtil.joinStringFrom(splitted, 2);
			String reason = c.getPlayer().getName() + " banned " + splitted[1] + ": " + originalReason;
			MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			if (target != null) {
				String readableTargetName = MapleCharacterUtil.makeMapleReadable(target.getName());
				String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
				reason += " (IP: " + ip + ")";
				target.ban(reason);
				cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason));

				mc.dropMessage(readableTargetName + "'s IP: " + ip + "!");
			} else {
				if (MapleCharacter.ban(splitted[1], reason, false)) {
					@SuppressWarnings("unused")
					String readableTargetName = MapleCharacterUtil.makeMapleReadable(target.getName());
					//String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
					//reason += " (IP: " + ip + ")";
					cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason));

				} else {
					mc.dropMessage("Failed to ban " + splitted[1]);
				}
			}
		} else if (splitted[0].equals("!tempban")) {
			Calendar tempB = Calendar.getInstance();
			String originalReason = joinAfterString(splitted, ":");

			if (splitted.length < 4 || originalReason == null) {
				 mc.dropMessage("Syntax helper: !tempban <name> [i / m / w / d / h] <amount> [r [reason id] : Text Reason <Make sure you put :>");
                                 return;
				//throw new IllegalCommandSyntaxException(4);
			}

			int yChange = getNamedIntArg(splitted, 1, "y", 0);
			int mChange = getNamedIntArg(splitted, 1, "m", 0);
			int wChange = getNamedIntArg(splitted, 1, "w", 0);
			int dChange = getNamedIntArg(splitted, 1, "d", 0);
			int hChange = getNamedIntArg(splitted, 1, "h", 0);
			int iChange = getNamedIntArg(splitted, 1, "i", 0);
			int gReason = getNamedIntArg(splitted, 1, "r", 7);

			String reason = c.getPlayer().getName() + " tempbanned " + splitted[1] + ": " + originalReason;

			if (gReason > 14) {
				mc.dropMessage("You have entered an incorrect ban reason ID, please try again.");
				return;
			}

			DateFormat df = DateFormat.getInstance();
			tempB.set(tempB.get(Calendar.YEAR) + yChange, tempB.get(Calendar.MONTH) + mChange, tempB.get(Calendar.DATE) +
				(wChange * 7) + dChange, tempB.get(Calendar.HOUR_OF_DAY) + hChange, tempB.get(Calendar.MINUTE) +
				iChange);

			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);

			if (victim == null) {
				int accId = MapleClient.findAccIdForCharacterName(splitted[1]);
				if (accId >= 0 && MapleCharacter.tempban(reason, tempB, gReason, accId)) {
					String readableTargetName = MapleCharacterUtil.makeMapleReadable(victim.getName());
					cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason));

				} else {
					mc.dropMessage("There was a problem offline banning character " + splitted[1] + ".");
				}
			} else {
				victim.tempban(reason, tempB, gReason);
				mc.dropMessage("The character " + splitted[1] + " has been successfully tempbanned till " +
					df.format(tempB.getTime()));
			}
		} else if (splitted[0].equals("!dc")) {
			int level = 0;
			MapleCharacter victim;
			if (splitted[1].charAt(0) == '-') {
				level = StringUtil.countCharacters(splitted[1], 'f');
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
			} else {
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			}

			if (level < 2) {
				victim.getClient().getSession().close();
				if (level >= 1) {
					victim.getClient().disconnect();
				}
			} else {
				mc.dropMessage("Please use dc -f instead.");
			//This, apparently, crashes the server. (Credits to Alysha, rofl =P)

			/*victim.saveToDB(true, victim.getMap().getForcedReturnId());
			cserv.removePlayer(victim);*/
            }
			} else if (splitted[0].equals("!whosthere")) {
			MessageCallback callback = new ServernoticeMapleClientMessageCallback(c);
			StringBuilder builder = new StringBuilder("Players on Map: ");
			for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) {
				if (builder.length() > 150) { // wild guess :o
					builder.setLength(builder.length() - 2);
					callback.dropMessage(builder.toString());
					builder = new StringBuilder();
				}
				builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
				builder.append(", ");
			}
			builder.setLength(builder.length() - 2);
			c.getSession().write(MaplePacketCreator.serverNotice(6, builder.toString()));
		} else if (splitted[0].equals("!cheaters")) {
			try {
				List<CheaterData> cheaters = c.getChannelServer().getWorldInterface().getCheaters();
				for (int x = cheaters.size() - 1; x >= 0; x--) {
					CheaterData cheater = cheaters.get(x);
					mc.dropMessage(cheater.getInfo());
				}
			} catch (RemoteException e) {
				c.getChannelServer().reconnectWorld();
            }
			} else if (splitted[0].equals("!warp")) {
			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			if (victim != null) {
				if (splitted.length == 2) {
					MapleMap target = victim.getMap();
					c.getPlayer().changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));

				} else {
					int mapid = Integer.parseInt(splitted[2]);
					MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
					victim.changeMap(target, target.getPortal(0));
				}
			} else {
				try {
					victim = c.getPlayer();
					WorldLocation loc = c.getChannelServer().getWorldInterface().getLocation(splitted[1]);
					if (loc != null) {
						mc.dropMessage("You will be cross-channel warped. This may take a few seconds.");
						// WorldLocation loc = new WorldLocation(40000, 2);
						MapleMap target = c.getChannelServer().getMapFactory().getMap(loc.map);
						String ip = c.getChannelServer().getIP(loc.channel);
						c.getPlayer().getMap().removePlayer(c.getPlayer());
						victim.setMap(target);
						String[] socket = ip.split(":");
						if (c.getPlayer().getTrade() != null) {
							MapleTrade.cancelTrade(c.getPlayer());
						}
						try {
							WorldChannelInterface wci = ChannelServer.getInstance(c.getChannel()).getWorldInterface();
							wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
							wci.addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
						} catch (RemoteException e) {
							c.getChannelServer().reconnectWorld();
						}
						c.getPlayer().saveToDB(true, true);
						if (c.getPlayer().getCheatTracker() != null)
							c.getPlayer().getCheatTracker().dispose();
						ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
						c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
						try {
							MaplePacket packet = MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
							c.getSession().write(packet);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					} else {
						int map = Integer.parseInt(splitted[1]);
						MapleMap target = cserv.getMapFactory().getMap(map);
						c.getPlayer().changeMap(target, target.getPortal(0));
					}

				} catch (/* Remote */Exception e) {
					mc.dropMessage("Something went wrong " + e.getMessage());
				}
			}
		} else if (splitted[0].equals("!warphere")) {
			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));

		} else if (splitted[0].equals("!jail")) {
			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			int mapid = 200090300; // mulung ride
			if (splitted.length > 2 && splitted[1].equals("2")) {
				mapid = 980000404; // exit for CPQ; not used
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
			}
			if (victim != null) {
				MapleMap target = cserv.getMapFactory().getMap(mapid);
				MaplePortal targetPortal = target.getPortal(0);
				victim.changeMap(target, targetPortal);
				mc.dropMessage(victim.getName() + " was jailed!");
			} else {
				mc.dropMessage(splitted[1] + " not found!");
			}
		} else if (splitted[0].equals("!map")) {
			int mapid = Integer.parseInt(splitted[1]);
			MapleMap target = cserv.getMapFactory().getMap(mapid);
			MaplePortal targetPortal = null;
			if (splitted.length > 2) {
				try {
					targetPortal = target.getPortal(Integer.parseInt(splitted[2]));
				} catch (IndexOutOfBoundsException ioobe) {
					// noop, assume the gm didn't know how many portals there are
				} catch (NumberFormatException nfe) {
					// noop, assume that the gm is drunk
				}
			}
			if (targetPortal == null) {
				targetPortal = target.getPortal(0);
			}
			c.getPlayer().changeMap(target, targetPortal);
		} else if (splitted[0].equals("!say")) {
			if (splitted.length > 1) {
				MaplePacket packet = MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(splitted, 1));
				try {
					ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(c.getPlayer().getName(), packet.getBytes());
				} catch (RemoteException e) {
					c.getChannelServer().reconnectWorld();
				}
			} else {
				mc.dropMessage("Syntax: !say <message>");
			}
		}  else if (splitted[0].equals("!droprate")) {
			if (splitted.length > 1) {
				int drop = Integer.parseInt(splitted[1]);
				if (drop > 10 && !c.getPlayer().isInvincible()) {
					c.getPlayer().ban("Attempting to set drop rate to: " + drop);
					return;
				}
				cserv.setDropRate(drop);
				MaplePacket packet = MaplePacketCreator.serverNotice(6, "Drop rate was changed to " + drop + "x.");
				ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
			} else {
				mc.dropMessage("Syntax: !droprate <number>");
			}
		} else if (splitted[0].equals("!bossdroprate")) {
			if (splitted.length > 1) {
				int bossdrop = Integer.parseInt(splitted[1]);
				if (bossdrop > 10 && !c.getPlayer().isInvincible()) {
					c.getPlayer().ban("Attempting to set Boss drop rate to: " + bossdrop);
					return;
				}
				cserv.setBossDropRate(bossdrop);
				MaplePacket packet = MaplePacketCreator.serverNotice(6, "Boss drop rate was changed to " + bossdrop + "x.");
				ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
			} else {
				mc.dropMessage("Syntax: !bossdroprate <number>");
			}
		} else if (splitted[0].equals("!exprate")) {
			if (splitted.length > 1) {
				int exp = Integer.parseInt(splitted[1]);
				if (exp > 3 && !c.getPlayer().isInvincible()) {
					c.getPlayer().ban("Attempting to set EXP rate to: " + exp);
					return;
				}
				cserv.setExpRate(exp);
				MaplePacket packet = MaplePacketCreator.serverNotice(6, "Expeerience rate was changed to " + exp + "x.");
				ChannelServer.getInstance(c.getChannel()).broadcastPacket(packet);
			} else {
				mc.dropMessage("Syntax: !bossdroprate <number>");
			}
		} else if (splitted[0].equals("!cleardrops")) {
            MapleMap             map   = c.getPlayer().getMap();
            double               range = Double.POSITIVE_INFINITY;
            List<MapleMapObject> items = map.getMapObjectsInRange(c.getPlayer().getPosition(), range,
                                             Arrays.asList(MapleMapObjectType.ITEM));

            for (MapleMapObject itemmo : items) {
                map.removeMapObject(itemmo);
                map.broadcastMessage(MaplePacketCreator.removeItemFromMap(itemmo.getObjectId(), 4,
                        c.getPlayer().getId()));
            }

            mc.dropMessage("You have destroyed " + items.size() + " items on the ground.");
        } else if (splitted[0].equalsIgnoreCase("!fh")) {
			mc.dropMessage("" + c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
		} else if (splitted[0].equalsIgnoreCase("!commands")) {
			mc.dropMessage("=="+Configuration.Server_Name+" Commands");
		int page = CommandProcessor.getOptionalIntArg(splitted, 1, 1);
		CommandProcessor.getInstance().dropHelp(c.getPlayer(), mc, page);
		}
		else {
			mc.dropMessage("GM Command " + splitted[0] + " does not exist");
        }
    }

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("notice", "[mcw] [n/p/l/nv/v/b] message", "", 2),
			new CommandDefinition("me","message","send a message with your name as the prefix", 2),
                        new CommandDefinition("ban", "charname reason", "Permanently ip, mac and accountbans the given character", 2),
			new CommandDefinition("tempban", "<name> [i / m / w / d / h] <amount> [r  [reason id] : Text Reason", "Tempbans the given account", 2),
			new CommandDefinition("dc", "[-f] name", "Disconnects player matching name provided. Use -f only if player is persistant!", 2),
                        new CommandDefinition("whosthere", "", "", 2),
			new CommandDefinition("cheaters", "", "", 2),
                        new CommandDefinition("warp", "playername [targetid]", "Warps yourself to the player with the given name. When targetid is specified warps the player to the given mapid", 2),
			new CommandDefinition("warphere", "playername", "Warps the player with the given name to yourself", 2),
			new CommandDefinition("jail", "[2] playername", "Warps the player to a map that he can't leave", 2),
			new CommandDefinition("map", "mapid", "Warps you to the given mapid (use /m instead)", 2),
                        new CommandDefinition("say", "message", "Talks to the whole world in the format: [Name] message", 2),
                        new CommandDefinition("droprate", "rate", "Changes the drop rate", 2),
                        new CommandDefinition("bossdroprate", "rate", "Changes the boss drop rate", 2),
			new CommandDefinition("cleardrops", "", "", 2),
			new CommandDefinition("exprate", "", "", 2),
			new CommandDefinition("commands", "[page defaults to 1]", "", 2),
                        new CommandDefinition("expireitem", "", "", 2),
                        new CommandDefinition("event", "", "", 2),
		};
	}
}

