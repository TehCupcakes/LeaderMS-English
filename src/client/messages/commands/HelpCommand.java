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

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Collection;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.CommandProcessor;
import static client.messages.CommandProcessor.getOptionalIntArg;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.remote.WorldChannelInterface;
import net.world.remote.WorldLocation;
import server.MaplePortal;
import server.MapleTrade;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class HelpCommand implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splittedLineLineLine) throws Exception,
		IllegalCommandSyntaxException {
            	MapleCharacter player = c.getPlayer();	
                ChannelServer cserv = c.getChannelServer();
                if (splittedLineLineLine[0].equals("!startev")) {
                int mapid = getOptionalIntArg(splittedLineLineLine, 1, c.getPlayer().getMapId());
                if (player.getClient().getChannelServer().eventOn == false) {
                player.getClient().getChannelServer().eventOn = true;
                player.getClient().getChannelServer().eventMap = mapid;
                try {
                    ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Evento] O evento foi iniciado no canal (" + c.getChannel() + "). Use @evento para participar.").getBytes()); 
                } catch (RemoteException e) {
                    ChannelServer.getInstance(c.getChannel()).reconnectWorld();
                }
            } else {
                player.getClient().getChannelServer().eventOn = false;
                try {
                     ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Evento] O evento terminou, obrigado aqueles que participaram.").getBytes());  
                } catch (RemoteException e) {
                    ChannelServer.getInstance(c.getChannel()).reconnectWorld();
                }
            }
        } else if (splittedLineLineLine[0].equals("setall")) {
            final int x = Short.parseShort(splittedLineLineLine[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, x);
            player.updateSingleStat(MapleStat.DEX, x);
            player.updateSingleStat(MapleStat.INT, x);
            player.updateSingleStat(MapleStat.LUK, x);
        } else if (splittedLineLineLine[0].equals("!online")) {
			mc.dropMessage("Characters connected to channel " + c.getChannel() + ":");
			Collection<MapleCharacter> chrs = c.getChannelServer().getInstance(c.getChannel()).getPlayerStorage().getAllCharacters();
			for (MapleCharacter chr : chrs) {
				mc.dropMessage(chr.getName() + " at map ID: " + chr.getMapId());
			}
			mc.dropMessage("Total characters on channel " + c.getChannel() + ": " + chrs.size());
	} else if (splittedLineLineLine[0].equals("!job")) {
			int jobId = Integer.parseInt(splittedLineLineLine[1]);
			if (MapleJob.getById(jobId) != null) {
			player.changeJob(MapleJob.getById(jobId));
                        }
		} else if (splittedLineLineLine[0].equals("!ban")) {
			if (splittedLineLineLine.length < 3) {
				throw new IllegalCommandSyntaxException(3);
			}
			String originalReason = StringUtil.joinStringFrom(splittedLineLineLine, 2);
			String reason = c.getPlayer().getName() + " banned " + splittedLineLineLine[1] + ": " + originalReason;
			MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(splittedLineLineLine[1]);
			if (target != null) {
				String readableTargetName = MapleCharacterUtil.makeMapleReadable(target.getName());
				String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
				reason += " (IP: " + ip + ")";
				target.ban(reason);
				cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason));

				mc.dropMessage(readableTargetName + "'s IP: " + ip + "!");
                        }
                } else if (splittedLineLineLine[0].equals("!warp")) {
			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splittedLineLineLine[1]);
			if (victim != null) {
				if (splittedLineLineLine.length == 2) {
					MapleMap target = victim.getMap();
					c.getPlayer().changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));

				} else {
					int mapid = Integer.parseInt(splittedLineLineLine[2]);
					MapleMap target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
					victim.changeMap(target, target.getPortal(0));
				}
			} else {
				try {
					victim = c.getPlayer();
					WorldLocation loc = c.getChannelServer().getWorldInterface().getLocation(splittedLineLineLine[1]);
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
						int map = Integer.parseInt(splittedLineLineLine[1]);
						MapleMap target = cserv.getMapFactory().getMap(map);
						c.getPlayer().changeMap(target, target.getPortal(0));
					}

				} catch (/* Remote */Exception e) {
					mc.dropMessage("Something went wrong " + e.getMessage());
				}
			}
		}  else if (splittedLineLineLine[0].equals("!warphere")) {
			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splittedLineLineLine[1]);
			victim.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().findClosestSpawnpoint(c.getPlayer().getPosition()));

		} else if (splittedLineLineLine[0].equals("!map")) {
			int mapid = Integer.parseInt(splittedLineLineLine[1]);
			MapleMap target = cserv.getMapFactory().getMap(mapid);
			MaplePortal targetPortal = null;
			if (splittedLineLineLine.length > 2) {
				try {
					targetPortal = target.getPortal(Integer.parseInt(splittedLineLineLine[2]));
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
		} else if (splittedLineLineLine[0].equals("!say")) {
			if (splittedLineLineLine.length > 1) {
				MaplePacket packet = MaplePacketCreator.serverNotice(6, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(splittedLineLineLine, 1));
				try {
					ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(c.getPlayer().getName(), packet.getBytes());
				} catch (RemoteException e) {
					c.getChannelServer().reconnectWorld();
				}
			} else {
				mc.dropMessage("Syntax: !say <mensagem>");
			} 
		} else if (splittedLineLineLine[0].equalsIgnoreCase("!commands")) {
			mc.dropMessage("== LeaderMS Comandos ==");
		int page = CommandProcessor.getOptionalIntArg(splittedLineLineLine, 1, 1);
		CommandProcessor.getInstance().dropHelp(c.getPlayer(), mc, page);
		}
		else {
			mc.dropMessage("(GM-3) Comando " + splittedLineLineLine[0] + " nao existe!");
        }
    }

        
        
        
        
	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("startev", "Inicia evento", "", 1),
                        new CommandDefinition("setall", "", "", 1),
                        new CommandDefinition("online", "", "", 1),
                        new CommandDefinition("job", "", "", 1),
                        new CommandDefinition("ban", "", "", 1),
                        new CommandDefinition("warp", "", "", 1),
                        new CommandDefinition("warphere", "", "", 1),
                        new CommandDefinition("map", "", "", 1),
                        new CommandDefinition("say", "", "", 1),
		};
	}
}
