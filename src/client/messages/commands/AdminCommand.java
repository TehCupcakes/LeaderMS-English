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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.rmi.RemoteException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Pattern;
import client.inventory.Equip;
import client.IItem;
import client.inventory.Item;
import server.MapleShopFactory;
import server.ShutdownServer;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.SkillFactory;
import client.messages.CommandProcessor;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import database.DatabaseConnection;
import handling.ExternalCodeTableGetter;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import handling.PacketProcessor;
import handling.RecvPacketOpcode;
import handling.SendPacketOpcode;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.performance.CPUSampler;
import scripting.portal.PortalScriptManager;
import scripting.reactor.ReactorScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.maps.MapleReactorStats;

public class AdminCommand implements Command {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminCommand.class);

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {
        ChannelServer cserv = c.getChannelServer();

        if (splitted[0].equals("!shutdown")) {
            int time = 60000;
            if (splitted.length > 1) {
                time = Integer.parseInt(splitted[1]) * 60000;
            }
            CommandProcessor.forcePersisting();
            c.getChannelServer().shutdown(time);
        } else if (splitted[0].equals("!shutdownworld")) {
            int time = 60000;
            if (splitted.length > 1) {
                time = Integer.parseInt(splitted[1]) * 60000;
            }
            CommandProcessor.forcePersisting();
            c.getChannelServer().shutdownWorld(time);
        // shutdown
        } else if (splitted[0].equals("!shutdownnow")) {
            CommandProcessor.forcePersisting();
            new ShutdownServer(c.getChannel()).run();
        } else if (splitted[0].equals("!removenpcs")) {
            MapleCharacter player = c.getPlayer();
            List<MapleMapObject> npcs = player.getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
            for (MapleMapObject npcmo : npcs) {
                MapleNPC npc = (MapleNPC) npcmo;
                if (npc.isCustom()) {
                    player.getMap().removeMapObject(npc.getObjectId());
                }
            }
        } else if (splitted[0].equals("!removemapnpcs")) {
            int mapId = Integer.parseInt(splitted[1]);
            MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
            List<MapleMapObject> npcs = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
            for (MapleMapObject npcmo : npcs) {
                MapleNPC npc = (MapleNPC) npcmo;
                if (npc.isCustom()) {
                    map.removeMapObject(npc.getObjectId());
                }
            }
        } else if (splitted[0].equals("!mynpcpos")) {
            Point pos = c.getPlayer().getPosition();
            mc.dropMessage("CY: " + pos.y + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getMap().getFootholds().findBelow(pos).getId());
        } else if (splitted[0].equals("!saveall")) {
            Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
            mc.dropMessage("Saving all characters in channel " + cserv.getChannel() + "...");
            Collection<MapleCharacter> chrs = cserv.getPlayerStorage().getAllCharacters();
            for (MapleCharacter chr : chrs) {
                chr.saveToDB(true, true);
            }
            mc.dropMessage("All characters saved.");
        } else if (splitted[0].equals("!npc")) {
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(c.getPlayer().getPosition().y);
                npc.setRx0(c.getPlayer().getPosition().x + 50);
                npc.setRx1(c.getPlayer().getPosition().x - 50);
                npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                npc.setCustom(true);
                c.getPlayer().getMap().addMapObject(npc);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            // c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
            } else {
                mc.dropMessage("You have entered an invalid Npc-Id");
            }
        } else if (splitted[0].equals("!packet")) {
            if (splitted.length > 1) {
                c.getSession().write(MaplePacketCreator.getPacketFromHexString(StringUtil.joinStringFrom(splitted, 0)));
            } else {
                mc.dropMessage("Please enter packet data!");
            }
        } else if (splitted[0].equals("!clearguilds")) {
            try {
                mc.dropMessage("Attempting to reload all guilds... this may take a while...");
                cserv.getWorldInterface().clearGuilds();
                mc.dropMessage("Completed.");
            } catch (RemoteException re) {
                mc.dropMessage("RemoteException occurred while attempting to reload guilds.");
                log.error("RemoteException occurred while attempting to reload guilds.", re);
            }
        } else if (splitted[0].equals("!reloadops")) {
            try {
                ExternalCodeTableGetter.populateValues(SendPacketOpcode.getDefaultProperties(), SendPacketOpcode.values());
                ExternalCodeTableGetter.populateValues(RecvPacketOpcode.getDefaultProperties(), RecvPacketOpcode.values());
            } catch (Exception e) {
                log.error("Failed to reload props", e);
            }
            PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
            PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER).reset(PacketProcessor.Mode.CHANNELSERVER);
        } else if (splitted[0].equals("!clearPortalScripts")) {
            PortalScriptManager.getInstance().clearScripts();
        } else if (splitted[0].equals("!clearReactorDrops")) {
            ReactorScriptManager.getInstance().clearDrops();
        } else if (splitted[0].equals("!clearshops")) {
            MapleShopFactory.getInstance().clear();
        } else if (splitted[0].equals("!clearevents")) {
            for (ChannelServer instance : ChannelServer.getAllInstances()) {
                instance.reloadEvents();
            }
        } else if (splitted[0].equals("!reloadcommands")) {
            CommandProcessor.getInstance().reloadCommands();
        } else if (splitted[0].equals("!servermessage")) {
            Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
            String outputMessage = StringUtil.joinStringFrom(splitted, 1);
            if (outputMessage.equalsIgnoreCase("!array")) {
                outputMessage = c.getChannelServer().getArrayString();
            }
            cserv.setServerMessage(outputMessage);
        } else if (splitted[0].equals("!startProfiling")) {
            CPUSampler sampler = CPUSampler.getInstance();
            sampler.addIncluded("net.sf.odinms");
            sampler.start();
        } else if (splitted[0].equals("!stopProfiling")) {
            CPUSampler sampler = CPUSampler.getInstance();
            try {
                String filename = "odinprofile.txt";
                if (splitted.length > 1) {
                    filename = splitted[1];
                }
                File file = new File(filename);
                if (file.exists()) {
                    mc.dropMessage("The entered filename already exists, choose a different one");
                    return;
                }
                sampler.stop();
                FileWriter fw = new FileWriter(file);
                sampler.save(fw, 1, 10);
                fw.close();
            } catch (IOException e) {
                log.error("THROW", e);
            }
            sampler.reset();
        } else if (splitted[0].equalsIgnoreCase("!warpallhere")) {
            List<MapleCharacter> people = new LinkedList<MapleCharacter>();
            //list the chars...
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                for (MapleCharacter mch : cs.getPlayerStorage().getAllCharacters()) {
                    if (mch.getMapId() != c.getPlayer().getMapId() || mch.getClient().getChannel() != c.getChannel()) {
                        people.add(mch);
                    }
                }
            }
            //do warp
            String ip = c.getChannelServer().getIP();
            String[] socket = ip.split(":");
            for (MapleCharacter chr : people) {
                if (chr.getClient().getChannel() != c.getChannel()) {
                    chr.getMap().removePlayer(chr);
                    ChannelServer.getInstance(chr.getClient().getChannel()).removePlayer(chr);
                    chr.getClient().updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                    chr.getClient().getSession().write(MaplePacketCreator.getChannelChange(
                            InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                }
                chr.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().getPortal("sp"));
            }
        } else if (splitted[0].equals("!letter")) {
            if (splitted.length < 3) {
                mc.dropMessage("syntax: !letter <color (green/red)> <word>");

                return;
            }

            int start, nstart;

            if (splitted[1].equalsIgnoreCase("green")) {
                start = 3991026;
                nstart = 3990019;
            } else if (splitted[1].equalsIgnoreCase("red")) {
                start = 3991000;
                nstart = 3990009;
            } else {
                mc.dropMessage("Unknown color!");

                return;
            }

            String splitString = StringUtil.joinStringFrom(splitted, 2);
            List<Integer> chars = new LinkedList<Integer>();

            splitString = splitString.toUpperCase();
            System.out.println(splitString);

            for (int i = 0; i < splitString.length(); i++) {
                char chr = splitString.charAt(i);

                if (chr == ' ') {
                    chars.add(-1);
                } else if ((int) (chr) >= (int) 'A' && (int) (chr) <= (int) 'Z') {
                    chars.add((int) (chr));
                } else if ((int) (chr) >= (int) '0' && (int) (chr) <= (int) ('9')) {
                    chars.add((int) (chr) + 200);
                }
            }

            final int w = 32;
            int dStart = c.getPlayer().getPosition().x - (splitString.length() / 2 * w);

            for (Integer i : chars) {
                if (i == -1) {
                    dStart += w;
                } else if (i < 200) {
                    int val = start + i - (int) ('A');
                    Item item = new Item(val, (byte) 0, (short) 1);

                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item,
                            new Point(dStart, c.getPlayer().getPosition().y), false, false);
                    dStart += w;
                } else if ((i >= 200) && (i <= 300)) {
                    int val = nstart + i - (int) ('0') - 200;
                    Item item = new Item(val, (byte) 0, (short) 1);

                    c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), item,
                            new Point(dStart, c.getPlayer().getPosition().y), false, false);
                    dStart += w;
                }
            }
        } else if (splitted[0].equals("!strip")) {
            ChannelServer cs = c.getChannelServer();

            for (MapleCharacter mchr : cs.getPlayerStorage().getAllCharacters()) {
                MapleInventory equipped = mchr.getInventory(MapleInventoryType.EQUIPPED);
                MapleInventory equip = mchr.getInventory(MapleInventoryType.EQUIP);
                List<Byte> ids = new LinkedList<Byte>();

                for (IItem item : equipped.list()) {
                    ids.add(item.getPosition());
                }

                for (byte id : ids) {
                    MapleInventoryManipulator.unequip(mchr.getClient(), id, equip.getNextFreeSlot());
                }
            }
        } else if (splitted[0].equalsIgnoreCase("!superequip")) {
            if (splitted.length != 3) {
                mc.dropMessage("Syntax: !superequip <id> <stat>");

                return;
            }

            int id;
            short stat;

            id = stat = 0;

            try {
                id = Integer.parseInt(splitted[1]);
                stat = (short) Integer.parseInt(splitted[2]);
            } catch (NumberFormatException nfe) {
                mc.dropMessage("Error occured while parsing values. Please recheck and try again.");

                return;
            }

            Equip eq = new Equip(id, (byte) -1);

            eq.setAcc(stat);
            eq.setAvoid(stat);
            eq.setInt(stat);
            eq.setDex(stat);
            eq.setLuk(stat);
            eq.setMatk(stat);
            eq.setMdef(stat);
            eq.setStr(stat);
            eq.setWatk(stat);
            eq.setWdef(stat);
            MapleInventoryManipulator.addFromDrop(c, eq, new String(), false);    // If this doesnt work

        // change to this:
        // MapleInventoryManipulator.addFromDrop(c, eq, new String());
        } else if (splitted[0].equalsIgnoreCase("!memory")) {
            mc.dropMessage("Free memory: " + Runtime.getRuntime().freeMemory() + "/" +
                    Runtime.getRuntime().maxMemory());
            mc.dropMessage("Total memory: " + Runtime.getRuntime().totalMemory());
        } else if (splitted[0].equalsIgnoreCase("!gmmap")) {
            server.maps.MapleMapFactory gmf = c.getChannelServer().getGmMapFactory();
            server.maps.MapleMap map = gmf.getMap(Integer.parseInt(splitted[1]));
            c.getPlayer().changeMap(map, map.getPortal("sp"));
        } else if (splitted[0].equals("!pnpc")) {
            int npcId = Integer.parseInt(splitted[1]);
            MapleNPC npc = MapleLifeFactory.getNPC(npcId);
            if (npc != null && !npc.getName().equals("MISSINGNO")) {
                npc.setPosition(c.getPlayer().getPosition());
                npc.setCy(c.getPlayer().getPosition().y);
                npc.setRx0(c.getPlayer().getPosition().x + 50);
                npc.setRx1(c.getPlayer().getPosition().x - 50);
                npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                npc.setCustom(false);

                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                ps.setInt(1, npcId);
                ps.setInt(2, 0);
                ps.setInt(3, c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                ps.setInt(4, c.getPlayer().getPosition().y);
                ps.setInt(5, c.getPlayer().getPosition().x + 50);
                ps.setInt(6, c.getPlayer().getPosition().x - 50);
                ps.setString(7, "n");
                ps.setInt(8, c.getPlayer().getPosition().x);
                ps.setInt(9, c.getPlayer().getPosition().y);
                ps.setInt(10, c.getPlayer().getMapId());
                ps.executeUpdate();

                c.getPlayer().getMap().addMapObject(npc);
                c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            // c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
            } else {
                mc.dropMessage("You have entered an invalid Npc-Id");
            }
        } else if (splitted[0].equals("!pmob")) {
            int npcId = Integer.parseInt(splitted[1]);
            int mobTime = Integer.parseInt(splitted[2]);
            MapleMonster mob = MapleLifeFactory.getMonster(npcId);
            if (mob != null && !mob.getName().equals("MISSINGNO")) {
                mob.setPosition(c.getPlayer().getPosition());
                mob.setCy(c.getPlayer().getPosition().y);
                mob.setRx0(c.getPlayer().getPosition().x + 50);
                mob.setRx1(c.getPlayer().getPosition().x - 50);
                mob.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());

                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("INSERT INTO spawns ( idd, f, fh, cy, rx0, rx1, type, x, y, mid, mobtime ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )");
                ps.setInt(1, npcId);
                ps.setInt(2, 0);
                ps.setInt(3, c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                ps.setInt(4, c.getPlayer().getPosition().y);
                ps.setInt(5, c.getPlayer().getPosition().x + 50);
                ps.setInt(6, c.getPlayer().getPosition().x - 50);
                ps.setString(7, "m");
                ps.setInt(8, c.getPlayer().getPosition().x);
                ps.setInt(9, c.getPlayer().getPosition().y);
                ps.setInt(10, c.getPlayer().getMapId());
                ps.setInt(11, mobTime);
                ps.executeUpdate();
                c.getPlayer().getMap().addMonsterSpawn(mob, mobTime);
            } else {
                mc.dropMessage("You have entered an invalid Npc-Id");
            }
        } else if (splitted[0].equalsIgnoreCase("!curse")) {
            if (splitted.length != 3) {
                mc.dropMessage("Syntax: !curse <name> <what>");
                return;
            }
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            String curse = splitted[2];
            if (victim != null) {
                if (curse.equalsIgnoreCase("seal")) {
                    victim.giveDebuff(MapleDisease.getType(120), MobSkillFactory.getMobSkill(120, 1), true);
                } else if (curse.equalsIgnoreCase("stun")) {
                    victim.giveDebuff(MapleDisease.getType(123), MobSkillFactory.getMobSkill(123, 1), true);
                } else if (curse.equalsIgnoreCase("seduce")) {
                    victim.giveDebuff(MapleDisease.getType(128), MobSkillFactory.getMobSkill(128, 1), true);
                } else if (curse.equalsIgnoreCase("poison")) {
                    victim.giveDebuff(MapleDisease.getType(125), MobSkillFactory.getMobSkill(125, 1), true);
                } else if (curse.equalsIgnoreCase("darkness")) {
                    victim.giveDebuff(MapleDisease.getType(121), MobSkillFactory.getMobSkill(121, 1), true);
                } else if (curse.equalsIgnoreCase("weaken")) {
                    victim.giveDebuff(MapleDisease.getType(122), MobSkillFactory.getMobSkill(122, 1), true);
                } else if (curse.equalsIgnoreCase("dispel")) {
                    victim.cancelAllBuffs();
                } else if (curse.equalsIgnoreCase("cancel")) {
                    victim.cancelAllDebuffs();
                }
            }
        } else if (splitted[0].equalsIgnoreCase("!gmob")) {
            int id = Integer.parseInt(splitted[1]);
            MapleMonster mob = MapleLifeFactory.getMonster(id);
            if (c.getPlayer().getParty() == null) {
                c.getPlayer().getMap().spawnCPQMonster(mob, -1);
            } else {
                c.getPlayer().getMap().spawnCPQMonster(mob, c.getPlayer().getTeam());
            }
        } else if (splitted[0].equalsIgnoreCase("!ns")) {
            String name = splitted[1];
            int type = Integer.parseInt(splitted[2]);
            int ns = Integer.parseInt(splitted[3]);
            if (ns > 10000 && !c.getPlayer().isInvincible()) {
                mc.dropMessage("You suck");
                return;
            }
            c.getChannelServer().getPlayerStorage().getCharacterByName(name).
                    modifyCSPoints(type, ns);
        } else if (splitted[0].equalsIgnoreCase("!kill")) {
            String name = splitted[1];
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
            if (chr.isInvincible()) {
                c.disconnect();
                return;
            }
            chr.assassinate();
        } else if (splitted[0].equalsIgnoreCase("!level")) {
            int level = Integer.parseInt(splitted[1]);
            if (level < 1 || level > 200) {
                mc.dropMessage("No.");
                return;
            }
            c.getPlayer().setLevel(level);
            c.getPlayer().levelUp();
        } else if (splitted[0].equalsIgnoreCase("!disposemap")) {
            int mapId = Integer.parseInt(splitted[1]);
            MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
            boolean force = splitted.length >= 3 && splitted[2].equals("force");// && c.getPlayer().isInvincible();
            if (force) {
                if(map.getForcedReturnId() == map.getId() || map.getReturnMapId() == map.getId())
                {
                    map.warpAllToCashShop(map.getStreetName() + " : " + map.getMapName() + "was disposed by " + c.getPlayer().getName() + ". You will be warped to the cash shop.");
                } else
                    map.warpAllToNearestTown(map.getStreetName() + " : " + map.getMapName() + "was disposed by " + c.getPlayer().getName() + ". You will be warped to the nearest town.");




            }
            if (map.getAllPlayer().size() == 0) {
                c.getChannelServer().getMapFactory().disposeMap(mapId);
                mc.dropMessage("Disposed map: " + mapId);
            } else {
                mc.dropMessage("Cannot dispose a map while players are in it.");
            }
        } else if (splitted[0].equalsIgnoreCase("!yn")) {
            String text = StringUtil.joinStringFrom(splitted, 1);
            c.getChannelServer().getWorldInterface().broadcastMessage("", MaplePacketCreator.yellowChat(text).getBytes());
        } else if (splitted[0].equalsIgnoreCase("!clock")) {
            if (splitted.length < 2) {
                return;
            }
            MapleMap map = c.getPlayer().getMap();
            int duration = 0, mapid = 100000000, minlev = 1, maxlev = 200, x = 0;
            duration = Integer.parseInt(splitted[1]);
            x++;
            try {
                mapid = Integer.parseInt(splitted[2]);
                x++;
                minlev = Integer.parseInt(splitted[3]);
                x++;
                maxlev = Integer.parseInt(splitted[4]);
                x++;

            } catch (Exception e) {
            }
            switch (x) {
                case 1:
                    map.addMapTimer(duration);
                    break;
                case 2:
                    map.addMapTimer(duration, mapid);
                    break;
                case 3:
                    map.addMapTimer(duration, mapid, minlev);
                    break;
                case 4:
                    map.addMapTimer(duration, mapid, minlev, maxlev);
                    break;
            }


        } else if (splitted[0].equalsIgnoreCase("!mspfc")) {
            int duration = 0, mapid = 100000000;
            duration = Integer.parseInt(splitted[1]);
            mapid = CommandProcessor.getOptionalIntArg(splitted, 2, 104000000);
            for (int x = 109040000; x <= 109040004; x++) {

                c.getChannelServer().getMapFactory().getMap(x).addMapTimer(duration, mapid);
            }
            MapleMap end = c.getChannelServer().getMapFactory().getMap(109040004);
            end.setPortalDisable(true);
            c.getChannelServer().getMapFactory().getMap(109040000).setPortalDisable(false);

            c.getPlayer().changeMap(end, end.getPortal(12));
            try {
                ChannelServer.getInstance(c.getChannel()).getWorldInterface().broadcastMessage(
                        c.getPlayer().getName(), MaplePacketCreator.serverNotice(0, "MapleStory Physical Fitness Test (or rather, tedious jump quest event) has started.").getBytes());
            } catch (RemoteException e) {
                c.getChannelServer().reconnectWorld();
            }


        } else if (splitted[0].equalsIgnoreCase("!area")) {
            MapleMap map = c.getPlayer().getMap();

            Rectangle area = splitted[1].equals("pq") ? null : map.getArea(Integer.parseInt(splitted[2]));
            List<MapleCharacter> players = splitted[1].equals("pq") ? null : map.getPlayersInRect(area);
            if (splitted[1].equals("give")) {
                int amount = Integer.parseInt(splitted[4]);
                if (splitted[3].equals("exp")) {

                    for (MapleCharacter x : players) {
                        x.gainExp(amount, true, true);
                    }
                } else if (splitted[3].equals("meso")) {
                    for (MapleCharacter x : players) {
                        x.gainMeso(amount, true, true, true);
                    }
                } else if (splitted[3].equals("item")) {
                    short quantity = (short) CommandProcessor.getOptionalIntArg(splitted, 5, 1);
                    boolean pet = false;

                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    if (ii.getSlotMax(amount) <= 0) {
                        return;
                    }
                    if (amount >= 5000000 && amount <= 5000100) {
                        if (quantity > 1) {
                            quantity = 1;
                        }

                        pet = true;


                    } else if (ii.isRechargable(amount)) {
                        quantity = (short) ii.getSlotMax(c, amount);


                    }
                    for (MapleCharacter x : players) {
                        MapleInventoryManipulator.addById(x.getClient(), amount, quantity, "Area item.", x.getName(), pet ? MaplePet.createPet(amount) : -1);
                    }
                }
            } else if (splitted[1].equals("mark")) {
                Point tl = area.getLocation();
                Point bl = new Point(tl.x, tl.y + (int) area.getHeight());
                Point tr = new Point(tl.x + (int) area.getWidth(), tl.y);
                Point br = new Point(tl.x + (int) area.getWidth(), tl.y + (int) area.getHeight());

                map.spawnReactor(createMarker(tl));
                map.spawnReactor(createMarker(bl));
                map.spawnReactor(createMarker(tr));
                map.spawnReactor(createMarker(br));
            } else if (splitted[1].equals("pq")) {
                MaplePacket clear1 = MaplePacketCreator.showEffect("quest/party/clear");
                MaplePacket clear2 = MaplePacketCreator.playSound("Party1/Clear");
                MaplePacket wrong1 = MaplePacketCreator.showEffect("quest/party/wrong_kor");
                MaplePacket wrong2 = MaplePacketCreator.playSound("Party1/Failed");
                String[] strings = splitted[2].split(Pattern.quote("|"));
                String[] correctS = strings[0].split(Pattern.quote(","));
                String[] wrongS = strings[1].split(Pattern.quote(","));
                ArrayList<MapleCharacter> correct = new ArrayList<MapleCharacter>();
                ArrayList<MapleCharacter> wrong = new ArrayList<MapleCharacter>();
                for (String f : correctS) {
                    map.getPlayersInRect2(map.getArea(Integer.parseInt(f)), correct);
                }
                for (String f : wrongS) {
                    map.getPlayersInRect2(map.getArea(Integer.parseInt(f)), wrong);
                }
                for (MapleCharacter mcg : correct) {
                    mcg.getClient().getSession().write(clear1);
                    mcg.getClient().getSession().write(clear2);
                }
                for (MapleCharacter mcg : wrong) {
                    mcg.getClient().getSession().write(wrong1);
                    mcg.getClient().getSession().write(wrong2);
                }

            }
        } else if (splitted[0].equals("!toggleinvincibilityskills")) {
            mc.dropMessage(c.getPlayer().getMap().setDisableInvincibilitySkills(!c.getPlayer().getMap().getDisableInvincibilitySkills()) + ": invincibility skills disabled");
        } else if (splitted[0].equalsIgnoreCase("!cursemap")) {
            if (splitted.length != 2) {
                mc.dropMessage("Syntax: !cursemap <what>");
                return;
            }

            String curse = splitted[1];
            Collection<MapleCharacter> victims = c.getPlayer().getMap().getCharacters();
            for (MapleCharacter victim : victims) {
                if (victim != null) {
                    if (curse.equalsIgnoreCase("seal")) {
                        victim.giveDebuff(MapleDisease.getType(120), MobSkillFactory.getMobSkill(120, 1), true);
                    } else if (curse.equalsIgnoreCase("stun")) {
                        victim.giveDebuff(MapleDisease.getType(123), MobSkillFactory.getMobSkill(123, 1), true);
                    } else if (curse.equalsIgnoreCase("seduce")) {
                        victim.giveDebuff(MapleDisease.getType(128), MobSkillFactory.getMobSkill(128, 1), true);
                    } else if (curse.equalsIgnoreCase("poison")) {
                        victim.giveDebuff(MapleDisease.getType(125), MobSkillFactory.getMobSkill(125, 1), true);
                    } else if (curse.equalsIgnoreCase("darkness")) {
                        victim.giveDebuff(MapleDisease.getType(121), MobSkillFactory.getMobSkill(121, 1), true);
                    } else if (curse.equalsIgnoreCase("weaken")) {
                        victim.giveDebuff(MapleDisease.getType(122), MobSkillFactory.getMobSkill(122, 1), true);
                    } else if (curse.equalsIgnoreCase("dispel")) {
                        victim.cancelAllBuffs();
                    } else if (curse.equalsIgnoreCase("cancel")) {
                        victim.cancelAllDebuffs();
                    }
                }
            }
        } else if (splitted[0].equals("!mapmsg")) {
            c.getPlayer().getMap().stopMapEffect();
            c.getPlayer().getMap().startMapEffect(StringUtil.joinStringFrom(splitted, 1), 5120002);
        } else if (splitted[0].equals("!toggledamage")) {
            mc.dropMessage(c.getPlayer().getMap().setDisableDamage(!c.getPlayer().getMap().getDisableDamage()) + ": Damage disabled");
        } else if (splitted[0].equals("!mute")) {
            mc.dropMessage(c.getPlayer().getMap().setDisableChat(!c.getPlayer().getMap().getDisableChat()) + ": Chat disabled");
        } else if (splitted[0].equals("!energycharge")) {
            MapleCharacter player = c.getPlayer();
            if (splitted.length < 2 || player.getSkillLevel(SkillFactory.getSkill(5110001)) <= 0) {
                return;
            }
            if (splitted[1].equals("max")) {
                player.setEnergy(10000);
                player.energyFull(60000 * 60);
                return;
            }
            player.setEnergy(Integer.parseInt(splitted[1]));
            if (player.getEnergy() >= 10000) {
                player.energyFull(60000 * 60);
            }
        } else if (splitted[0].equals("!immune")) {
            c.getPlayer().setImmune(!c.getPlayer().getImmune());
            mc.dropMessage(c.getPlayer().getImmune() + ": you cannot be debuffed");
        } else if (splitted[0].equals("!donatorpoints")) {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            int d = Integer.parseInt(splitted[2]);
            if (d > 100 && !c.getPlayer().isInvincible()) {
                mc.dropMessage("Wow, you got owned");
                return;
            }
            chr.gainDonatorPoints(d);
            mc.dropMessage(chr.getName() + " now has " + chr.getDonatorPoints() + " donator points.");
        } else if (splitted[0].equalsIgnoreCase("!chatmode")) {
			int mode = Integer.parseInt(splitted[1]);
			c.getPlayer().setChatMode(mode);
		} else if (splitted[0].equalsIgnoreCase("!chatrange")) {
			int range = Integer.parseInt(splitted[1]);
			c.getPlayer().setChatRange(range);
		}
    }

    private MapleReactor createMarker(Point position) {
        MapleReactorStats reactorSt = MapleReactorFactory.getReactor(2006000);
        MapleReactor reactor = new MapleReactor(reactorSt, 2006000);
        reactor.setDelay(-1);
        reactor.setPosition(position);
        return reactor;
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
                    new CommandDefinition("shutdown", "[when in Minutes]", "Shuts down the current channel - don't use atm", 4),
                    new CommandDefinition("shutdownnow", "", "Shuts down the current channel now", 4),
                    new CommandDefinition("shutdownworld", "[when in Minutes]", "Cleanly shuts down all channels and the loginserver of this world", 4),
                    new CommandDefinition("removenpcs", "", "Removes all custom spawned npcs from the map - requires reentering the map", 4),
                    new CommandDefinition("saveall", "?", "save data", 4),
                    new CommandDefinition("mynpcpos", "", "Gets the info for making an npc", 4),
                    new CommandDefinition("npc", "npcid", "Spawns the npc with the given id at the player position", 4),
                    new CommandDefinition("packet", "hex data", "Shows a clock to everyone in the map", 4),
                    new CommandDefinition("clearguilds", "", "", 4),
                    new CommandDefinition("reloadops", "", "", 4),
                    new CommandDefinition("clearPortalScripts", "", "", 4),
                    new CommandDefinition("reloaddrops", "", "", 4),
                    new CommandDefinition("clearReactorDrops", "", "", 4),
                    new CommandDefinition("clearshops", "", "", 4),
                    new CommandDefinition("clearevents", "", "", 4),
                    new CommandDefinition("reloadcommands", "", "", 4),
                    new CommandDefinition("startProfiling", "", "Starts the CPU Sampling based profiler", 4),
                    new CommandDefinition("stopProfiling", "[fileName]", "Stops the Profiler and saves the results to the given fileName", 4),
                    new CommandDefinition("warpallhere", "", "", 4),
                    new CommandDefinition("letter", "", "", 4),
                    new CommandDefinition("strip", "", "", 4),
                    new CommandDefinition("superequip", "", "", 4),
                    new CommandDefinition("memory", "", "", 4),
                    new CommandDefinition("gmmap", "", "", 4),
                    new CommandDefinition("pmob", "", "", 4),
                    new CommandDefinition("pnpc", "", "", 4),
                    new CommandDefinition("curse", "", "", 4),
                    new CommandDefinition("gmob", "", "", 4),
                    new CommandDefinition("ns", "", "", 4),
                    new CommandDefinition("kill", "", "", 4),
                    new CommandDefinition("randomevent", "", "", 4),
                    new CommandDefinition("randomexp", "", "", 4),
                    new CommandDefinition("level", "", "", 4),
                    new CommandDefinition("removemapnpcs", "", "", 4),
                    new CommandDefinition("disposemap", "", "", 4),
                    new CommandDefinition("clock", "<duration> [mapid to warp to] [min level to warp] [max level to warp]", "", 4),
                    new CommandDefinition("yn", "", "", 4),
                    new CommandDefinition("mspfc", "", "ASK ANGELSL FOR TRAINING ON COMMAND!", 4),
                    new CommandDefinition("area", "", "ASK ANGELSL FOR TRAINING ON COMMAND!", 4),
                    new CommandDefinition("toggleinvincibilityskills", "", "", 4),
                    new CommandDefinition("cursemap", "", "", 4),
                    new CommandDefinition("mapmsg", "", "", 4),
                    new CommandDefinition("toggledamage", "", "", 4),
                    new CommandDefinition("mute", "", "", 4),
                    new CommandDefinition("energycharge", "", "", 4),
                    new CommandDefinition("immune", "", "", 4),
                    new CommandDefinition("donatorpoints", "", "", 4),
					new CommandDefinition("unhide", "", "", 4),
                    new CommandDefinition("chatmode", "", "", 4),
					new CommandDefinition("chatrange", "", "", 4),
		};

    }
}