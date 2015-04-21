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

import java.util.Arrays;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import client.anticheat.CheatingOffense;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.status.MonsterStatus;
import net.MaplePacket;
import server.MaplePortal;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import server.maps.MapleReactorStats;
import server.maps.PlayerNPCMerchant;
import server.quest.MapleQuest;
import tools.HexTool;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;

public class DebugCommands implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
            IllegalCommandSyntaxException {
        MapleCharacter player = c.getPlayer();
        if (splitted[0].equals("!resetquest")) {
            MapleQuest.getInstance(Integer.parseInt(splitted[1])).forfeit(c.getPlayer());
        } else if (splitted[0].equals("!nearestPortal")) {
            final MaplePortal portal = player.getMap().findClosestSpawnpoint(player.getPosition());
            mc.dropMessage(portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());
        } else if (splitted[0].equals("!spawndebug")) {
            c.getPlayer().getMap().spawnDebug(mc);
        } else if (splitted[0].equals("!door")) {
            Point doorPos = new Point(player.getPosition());
            doorPos.y -= 270;
            MapleDoor door = new MapleDoor(c.getPlayer(), doorPos);
            door.getTarget().addMapObject(door);
            // c.getSession().write(MaplePacketCreator.spawnDoor(/*c.getPlayer().getId()*/ 0x1E47, door.getPosition(),
            // false));
			/* c.getSession().write(MaplePacketCreator.saveSpawnPosition(door.getPosition())); */
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.write(HexTool.getByteArrayFromHexString("B9 00 00 47 1E 00 00 0A 04 76 FF"));
            c.getSession().write(mplew.getPacket());
            mplew = new MaplePacketLittleEndianWriter();
            mplew.write(HexTool.getByteArrayFromHexString("36 00 00 EF 1C 0D 4C 3E 1D 0D 0A 04 76 FF"));
            c.getSession().write(mplew.getPacket());
            c.getSession().write(MaplePacketCreator.enableActions());
            door = new MapleDoor(door);
            door.getTown().addMapObject(door);
        } else if (splitted[0].equals("!threads")) {
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            String filter = "";
            if (splitted.length > 1) {
                filter = splitted[1];
            }
            for (int i = 0; i < threads.length; i++) {
                String tstring = threads[i].toString();
                if (tstring.toLowerCase().indexOf(filter.toLowerCase()) > -1) {
                    mc.dropMessage(i + ": " + tstring);
                }
            }
        } else if (splitted[0].equals("!showtrace")) {
            if (splitted.length < 2) {
                throw new IllegalCommandSyntaxException(2);
            }
            Thread[] threads = new Thread[Thread.activeCount()];
            Thread.enumerate(threads);
            Thread t = threads[Integer.parseInt(splitted[1])];
            mc.dropMessage(t.toString() + ":");
            for (StackTraceElement elem : t.getStackTrace()) {
                mc.dropMessage(elem.toString());
            }
        } else if (splitted[0].equals("!fakerelog")) {
            c.getSession().write(MaplePacketCreator.getCharInfo(player));
            player.getMap().removePlayer(player);
            player.getMap().addPlayer(player);
        } else if (splitted[0].equals("!toggleoffense")) {
            try {
                CheatingOffense co = CheatingOffense.valueOf(splitted[1]);
                co.setEnabled(!co.isEnabled());
            } catch (IllegalArgumentException iae) {
                mc.dropMessage("Offense " + splitted[1] + " not found");
            }
        } else if (splitted[0].equals("!tdrops")) {

            mc.dropMessage(player.getMap().toggleDrops() + ": drops are disabled");
        } else if (splitted[0].equals("!givebuff")) {
            long mask = 0;
            mask |= Long.decode(splitted[1]);
            c.getSession().write(MaplePacketCreator.giveBuffTest(1000, 60, mask));
        } else if (splitted[0].equals("!givemonsbuff")) {
            int mask = 0;
            mask |= Integer.decode(splitted[1]);
            MobSkill skill = MobSkillFactory.getMobSkill(128, 1);
            c.getSession().write(MaplePacketCreator.applyMonsterStatusTest(Integer.valueOf(splitted[2]), mask, 0, skill, Integer.valueOf(splitted[3])));
        } else if (splitted[0].equals("!givemonstatus")) {
            int mask = 0;
            mask |= Integer.decode(splitted[1]);
            c.getSession().write(MaplePacketCreator.applyMonsterStatusTest2(Integer.valueOf(splitted[2]), mask, 1000, Integer.valueOf(splitted[3])));
        } else if (splitted[0].equals("!sreactor")) {
            MapleReactorStats reactorSt = MapleReactorFactory.getReactor(Integer.parseInt(splitted[1]));
            MapleReactor reactor = new MapleReactor(reactorSt, Integer.parseInt(splitted[1]));
            reactor.setDelay(-1);
            reactor.setPosition(c.getPlayer().getPosition());
            c.getPlayer().getMap().spawnReactor(reactor);

        } else if (splitted[0].equals("!hreactor")) {
            c.getPlayer().getMap().getReactorByOid(Integer.parseInt(splitted[1])).hitReactor(c);
        } else if (splitted[0].equals("!lreactor")) {
            MapleMap map = c.getPlayer().getMap();
            List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
            for (MapleMapObject reactorL : reactors) {
                MapleReactor reactor2l = (MapleReactor) reactorL;
                //mc.dropMessage("Reactor: " + reactor2l.toString());
                mc.dropMessage("Reactor: oID: " + reactor2l.getObjectId() + " reactorID: " + reactor2l.getId() + " Position: " + reactor2l.getPosition().toString() + " State: " + reactor2l.getState());
            }
        } else if (splitted[0].equals("!dreactor")) {
            MapleMap map = c.getPlayer().getMap();
            List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
            if (splitted[1].equals("all")) {
                for (MapleMapObject reactorL : reactors) {
                    MapleReactor reactor2l = (MapleReactor) reactorL;
                    // reactor2l.
                    //mc.dropMessage("Reactor: " + reactor2l.toString());
                    c.getPlayer().getMap().destroyReactor(reactor2l.getObjectId());
                }
            } else {
                c.getPlayer().getMap().destroyReactor(Integer.parseInt(splitted[1]));
            }
        } else if (splitted[0].equals("!rreactor")) {
            c.getPlayer().getMap().resetReactors();
        } else if (splitted[0].equals("!gc")) {
            System.gc();
            mc.dropMessage("Free Memory = " + Runtime.getRuntime().freeMemory() + " .");
        } else if (splitted[0].equalsIgnoreCase("!cpqhud")) {
            c.getSession().write(MaplePacketCreator.startMonsterCarnival(c.getPlayer().getTeam()));
        } else if (splitted[0].equalsIgnoreCase("!maxskills")) {
            teachSkill(c, 9001000, 1, 1); //Start of max-level "1" skills
            teachSkill(c, 9001001, 1, 1);
            teachSkill(c, 9001002, 1, 1);
            teachSkill(c, 9101000, 1, 1);
            teachSkill(c, 9101001, 1, 1);
            teachSkill(c, 9101002, 1, 1);
            teachSkill(c, 9101003, 1, 1);
            teachSkill(c, 9101004, 1, 1);
            teachSkill(c, 9101005, 1, 1);
            teachSkill(c, 9101006, 1, 1);
            teachSkill(c, 9101007, 1, 1);
            teachSkill(c, 9101008, 1, 1);
            teachSkill(c, 5000000, 20, 20);//no here
            teachSkill(c, 5001001, 20, 20); //Start of Pirate Job Skills
            teachSkill(c, 5001002, 20, 20);
            teachSkill(c, 5001003, 20, 20);
            teachSkill(c, 5001005, 10, 10);
            teachSkill(c, 5100000, 10, 10);
            teachSkill(c, 5100001, 20, 20);
            teachSkill(c, 5101002, 20, 20);
            teachSkill(c, 5101003, 20, 20);
            teachSkill(c, 5101004, 20, 20);
            teachSkill(c, 5101005, 10, 10);
            teachSkill(c, 5101006, 20, 20);
            teachSkill(c, 5101007, 10, 10);
            teachSkill(c, 5200000, 20, 20);
            teachSkill(c, 5201001, 20, 20);
            teachSkill(c, 5201002, 20, 20);
            teachSkill(c, 5201003, 20, 20);
            teachSkill(c, 5201004, 20, 20);
            teachSkill(c, 5201005, 10, 10);
            teachSkill(c, 5201006, 20, 20);
            teachSkill(c, 5110000, 20, 20);
            teachSkill(c, 5110001, 40, 40);
            teachSkill(c, 5111002, 30, 30);
            teachSkill(c, 5111004, 20, 20);
            teachSkill(c, 5111005, 20, 20);
            teachSkill(c, 5111006, 30, 30);
            teachSkill(c, 5210000, 20, 20);
            teachSkill(c, 5211001, 30, 30);
            teachSkill(c, 5211002, 30, 30);
            teachSkill(c, 5211004, 30, 30);
            teachSkill(c, 5211005, 30, 30);
            teachSkill(c, 5211006, 30, 30);
            teachSkill(c, 5220001, 30, 30);
            teachSkill(c, 5220002, 20, 20);
            teachSkill(c, 5221000, 20, 20);
            teachSkill(c, 5221003, 30, 30);
            teachSkill(c, 5221004, 30, 30);
            teachSkill(c, 5221006, 10, 10);
            teachSkill(c, 5221007, 30, 30);
            teachSkill(c, 5221008, 30, 30);
            teachSkill(c, 5221009, 20, 20);
            teachSkill(c, 5221010, 1, 1);
            teachSkill(c, 5220011, 20, 20);
            teachSkill(c, 5121000, 20, 20);
            teachSkill(c, 5121001, 30, 30);
            teachSkill(c, 5121002, 30, 30);
            teachSkill(c, 5121003, 20, 20);
            teachSkill(c, 5121004, 30, 30);
            teachSkill(c, 5121005, 30, 30);
            teachSkill(c, 5121007, 30, 30);
            teachSkill(c, 5121008, 1, 1);
            teachSkill(c, 5121009, 20, 20); //End of Pirate Job Skills
            teachSkill(c, 5121010, 30, 30); //End of Pirate Job Skills
            teachSkill(c, 1003, 1, 1);
            teachSkill(c, 1004, 1, 1);
            teachSkill(c, 1121011, 1, 1);
            teachSkill(c, 1221012, 1, 1);
            teachSkill(c, 1321010, 1, 1);
            teachSkill(c, 2121008, 1, 1);
            teachSkill(c, 2221008, 1, 1);
            teachSkill(c, 2321009, 1, 1);
            teachSkill(c, 3121009, 1, 1);
            teachSkill(c, 3221008, 1, 1);
            teachSkill(c, 4121009, 1, 1);
            teachSkill(c, 4221008, 1, 1); //End of max-level "1" skills
            teachSkill(c, 1000002, 8, 8); //Start of max-level "8" skills
            teachSkill(c, 3000002, 8, 8);
            teachSkill(c, 4000001, 8, 8); //End of max-level "8" skills
            teachSkill(c, 1000001, 10, 10); //Start of max-level "10" skills
            teachSkill(c, 2000001, 10, 10); //End of max-level "10" skills
            teachSkill(c, 1000000, 16, 16); //Start of max-level "16" skills
            teachSkill(c, 2000000, 16, 16);
            teachSkill(c, 3000000, 16, 16); //End of max-level "16" skills
            teachSkill(c, 1001003, 20, 20); //Start of max-level "20" skills
            teachSkill(c, 1001004, 20, 20);
            teachSkill(c, 1001005, 20, 20);
            teachSkill(c, 2001002, 20, 20);
            teachSkill(c, 2001003, 20, 20);
            teachSkill(c, 2001004, 20, 20);
            teachSkill(c, 2001005, 20, 20);
            teachSkill(c, 3000001, 20, 20);
            teachSkill(c, 3001003, 20, 20);
            teachSkill(c, 3001004, 20, 20);
            teachSkill(c, 3001005, 20, 20);
            teachSkill(c, 4000000, 20, 20);
            teachSkill(c, 4001344, 20, 20);
            teachSkill(c, 4001334, 20, 20);
            teachSkill(c, 4001002, 20, 20);
            teachSkill(c, 4001003, 20, 20);
            teachSkill(c, 1101005, 20, 20);
            teachSkill(c, 1100001, 20, 20); //Start of mastery's
            teachSkill(c, 1100000, 20, 20);
            teachSkill(c, 1200001, 20, 20);
            teachSkill(c, 1200000, 20, 20);
            teachSkill(c, 1300000, 20, 20);
            teachSkill(c, 1300001, 20, 20);
            teachSkill(c, 3100000, 20, 20);
            teachSkill(c, 3200000, 20, 20);
            teachSkill(c, 4100000, 20, 20);
            teachSkill(c, 4200000, 20, 20); //End of mastery's
            teachSkill(c, 4201002, 20, 20);
            teachSkill(c, 4101003, 20, 20);
            teachSkill(c, 3201002, 20, 20);
            teachSkill(c, 3101002, 20, 20);
            teachSkill(c, 1301004, 20, 20);
            teachSkill(c, 1301005, 20, 20);
            teachSkill(c, 1201004, 20, 20);
            teachSkill(c, 1201005, 20, 20);
            teachSkill(c, 1101004, 20, 20); //End of boosters
            teachSkill(c, 1101006, 20, 20);
            teachSkill(c, 1201006, 20, 20);
            teachSkill(c, 1301006, 20, 20);
            teachSkill(c, 2101001, 20, 20);
            teachSkill(c, 2100000, 20, 20);
            teachSkill(c, 2101003, 20, 20);
            teachSkill(c, 2101002, 20, 20);
            teachSkill(c, 2201001, 20, 20);
            teachSkill(c, 2200000, 20, 20);
            teachSkill(c, 2201003, 20, 20);
            teachSkill(c, 2201002, 20, 20);
            teachSkill(c, 2301004, 20, 20);
            teachSkill(c, 2301003, 20, 20);
            teachSkill(c, 2300000, 20, 20);
            teachSkill(c, 2301001, 20, 20);
            teachSkill(c, 3101003, 20, 20);
            teachSkill(c, 3101004, 20, 20);
            teachSkill(c, 3201003, 20, 20);
            teachSkill(c, 3201004, 20, 20);
            teachSkill(c, 4100002, 20, 20);
            teachSkill(c, 4101004, 20, 20);
            teachSkill(c, 4200001, 20, 20);
            teachSkill(c, 4201003, 20, 20); //End of second-job skills and first-job
            teachSkill(c, 4211005, 20, 20);
            teachSkill(c, 4211003, 20, 20);
            teachSkill(c, 4210000, 20, 20);
            teachSkill(c, 4110000, 20, 20);
            teachSkill(c, 4111001, 20, 20);
            teachSkill(c, 4111003, 20, 20);
            teachSkill(c, 3210000, 20, 20);
            teachSkill(c, 3110000, 20, 20);
            teachSkill(c, 3210001, 20, 20);
            teachSkill(c, 3110001, 20, 20);
            teachSkill(c, 3211002, 20, 20);
            teachSkill(c, 3111002, 20, 20);
            teachSkill(c, 2210000, 20, 20);
            teachSkill(c, 2211004, 20, 20);
            teachSkill(c, 2211005, 20, 20);
            teachSkill(c, 2111005, 20, 20);
            teachSkill(c, 2111004, 20, 20);
            teachSkill(c, 2110000, 20, 20);
            teachSkill(c, 2311001, 20, 20);
            teachSkill(c, 2311005, 20, 20);
            teachSkill(c, 2310000, 20, 20);
            teachSkill(c, 1311007, 20, 20);
            teachSkill(c, 1310000, 20, 20);
            teachSkill(c, 1311008, 20, 20);
            teachSkill(c, 1210001, 20, 20);
            teachSkill(c, 1211009, 20, 20);
            teachSkill(c, 1210000, 20, 20);
            teachSkill(c, 1110001, 20, 20);
            teachSkill(c, 1111007, 20, 20);
            teachSkill(c, 1110000, 20, 20); //End of 3rd job skills
            teachSkill(c, 1121000, 20, 20);
            teachSkill(c, 1221000, 20, 20);
            teachSkill(c, 1321000, 20, 20);
            teachSkill(c, 2121000, 20, 20);
            teachSkill(c, 2221000, 20, 20);
            teachSkill(c, 2321000, 20, 20);
            teachSkill(c, 3121000, 20, 20);
            teachSkill(c, 3221000, 20, 20);
            teachSkill(c, 4121000, 20, 20);
            teachSkill(c, 4221000, 20, 20); //End of Maple Warrior // Also end of max-level "20" skills
            teachSkill(c, 1321007, 10, 10);
            teachSkill(c, 1320009, 25, 25);
            teachSkill(c, 1320008, 25, 25);
            teachSkill(c, 2321006, 10, 10);
            teachSkill(c, 1220010, 10, 10);
            teachSkill(c, 1221004, 19, 19);
            teachSkill(c, 1221003, 19, 19);
            teachSkill(c, 1100003, 30, 30);
            teachSkill(c, 1100002, 30, 30);
            teachSkill(c, 1101007, 30, 30);
            teachSkill(c, 1200003, 30, 30);
            teachSkill(c, 1200002, 30, 30);
            teachSkill(c, 1201007, 30, 30);
            teachSkill(c, 1300003, 30, 30);
            teachSkill(c, 1300002, 30, 30);
            teachSkill(c, 1301007, 30, 30);
            teachSkill(c, 2101004, 30, 30);
            teachSkill(c, 2101005, 30, 30);
            teachSkill(c, 2201004, 30, 30);
            teachSkill(c, 2201005, 30, 30);
            teachSkill(c, 2301002, 30, 30);
            teachSkill(c, 2301005, 30, 30);
            teachSkill(c, 3101005, 30, 30);
            teachSkill(c, 3201005, 30, 30);
            teachSkill(c, 4100001, 30, 30);
            teachSkill(c, 4101005, 30, 30);
            teachSkill(c, 4201005, 30, 30);
            teachSkill(c, 4201004, 30, 30);
            teachSkill(c, 1111006, 30, 30);
            teachSkill(c, 1111005, 30, 30);
            teachSkill(c, 1111002, 30, 30);
            teachSkill(c, 1111004, 30, 30);
            teachSkill(c, 1111003, 30, 30);
            teachSkill(c, 1111008, 30, 30);
            teachSkill(c, 1211006, 30, 30);
            teachSkill(c, 1211002, 30, 30);
            teachSkill(c, 1211004, 30, 30);
            teachSkill(c, 1211003, 30, 30);
            teachSkill(c, 1211005, 30, 30);
            teachSkill(c, 1211008, 30, 30);
            teachSkill(c, 1211007, 30, 30);
            teachSkill(c, 1311004, 30, 30);
            teachSkill(c, 1311003, 30, 30);
            teachSkill(c, 1311006, 30, 30);
            teachSkill(c, 1311002, 30, 30);
            teachSkill(c, 1311005, 30, 30);
            teachSkill(c, 1311001, 30, 30);
            teachSkill(c, 2110001, 30, 30);
            teachSkill(c, 2111006, 30, 30);
            teachSkill(c, 2111002, 30, 30);
            teachSkill(c, 2111003, 30, 30);
            teachSkill(c, 2210001, 30, 30);
            teachSkill(c, 2211006, 30, 30);
            teachSkill(c, 2211002, 30, 30);
            teachSkill(c, 2211003, 30, 30);
            teachSkill(c, 2311003, 30, 30);
            teachSkill(c, 2311002, 30, 30);
            teachSkill(c, 2311004, 30, 30);
            teachSkill(c, 2311006, 30, 30);
            teachSkill(c, 3111004, 30, 30);
            teachSkill(c, 3111003, 30, 30);
            teachSkill(c, 3111005, 30, 30);
            teachSkill(c, 3111006, 30, 30);
            teachSkill(c, 3211004, 30, 30);
            teachSkill(c, 3211003, 30, 30);
            teachSkill(c, 3211005, 30, 30);
            teachSkill(c, 3211006, 30, 30);
            teachSkill(c, 4111005, 30, 30);
            teachSkill(c, 4111006, 20, 20);
            teachSkill(c, 4111004, 30, 30);
            teachSkill(c, 4111002, 30, 30);
            teachSkill(c, 4211002, 30, 30);
            teachSkill(c, 4211004, 30, 30);
            teachSkill(c, 4211001, 30, 30);
            teachSkill(c, 4211006, 30, 30);
            teachSkill(c, 1120004, 30, 30);
            teachSkill(c, 1120003, 30, 30);
            teachSkill(c, 1121008, 30, 30);
            teachSkill(c, 1121010, 30, 30);
            teachSkill(c, 1121006, 30, 30);
            teachSkill(c, 1121002, 30, 30);
            teachSkill(c, 1220005, 30, 30);
            teachSkill(c, 1221009, 30, 30);
            teachSkill(c, 1221007, 30, 30);
            teachSkill(c, 1221011, 30, 30);
            teachSkill(c, 1221002, 30, 30);
            teachSkill(c, 1320005, 30, 30);
            teachSkill(c, 1320006, 30, 30);
            teachSkill(c, 1321003, 30, 30);
            teachSkill(c, 1321002, 30, 30);
            teachSkill(c, 2121005, 30, 30);
            teachSkill(c, 2121003, 30, 30);
            teachSkill(c, 2121004, 30, 30);
            teachSkill(c, 2121002, 30, 30);
            teachSkill(c, 2121007, 30, 30);
            teachSkill(c, 2121006, 30, 30);
            teachSkill(c, 2221007, 30, 30);
            teachSkill(c, 2221006, 30, 30);
            teachSkill(c, 2221003, 30, 30);
            teachSkill(c, 2221005, 30, 30);
            teachSkill(c, 2221004, 30, 30);
            teachSkill(c, 2221002, 30, 30);
            teachSkill(c, 2321007, 30, 30);
            teachSkill(c, 2321003, 30, 30);
            teachSkill(c, 2321008, 30, 30);
            teachSkill(c, 2321005, 30, 30);
            teachSkill(c, 2321004, 30, 30);
            teachSkill(c, 2321002, 30, 30);
            teachSkill(c, 3120005, 30, 30);
            teachSkill(c, 3121008, 30, 30);
            teachSkill(c, 3121003, 30, 30);
            teachSkill(c, 3121007, 30, 30);
            teachSkill(c, 3121006, 30, 30);
            teachSkill(c, 3121002, 30, 30);
            teachSkill(c, 3121004, 30, 30);
            teachSkill(c, 3221006, 30, 30);
            teachSkill(c, 3220004, 30, 30);
            teachSkill(c, 3221003, 30, 30);
            teachSkill(c, 3221005, 30, 30);
            teachSkill(c, 3221001, 30, 30);
            teachSkill(c, 3221002, 30, 30);
            teachSkill(c, 4121004, 30, 30);
            teachSkill(c, 4121008, 30, 30);
            teachSkill(c, 4121003, 30, 30);
            teachSkill(c, 4121006, 30, 30);
            teachSkill(c, 4121007, 30, 30);
            teachSkill(c, 4120005, 30, 30);
            teachSkill(c, 4221001, 30, 30);
            teachSkill(c, 4221007, 30, 30);
            teachSkill(c, 4221004, 30, 30);
            teachSkill(c, 4221003, 30, 30);
            teachSkill(c, 4221006, 30, 30);
            teachSkill(c, 4220005, 30, 30);
            teachSkill(c, 1221001, 30, 30);
            teachSkill(c, 1121001, 30, 30);
            teachSkill(c, 1321001, 30, 30);
            teachSkill(c, 2121001, 30, 30);
            teachSkill(c, 2221001, 30, 30);
            teachSkill(c, 2321001, 30, 30);
        } else if (splitted[0].equalsIgnoreCase("!partymessage")) {
            int a = Integer.parseInt(splitted[1]);
            c.getSession().write(MaplePacketCreator.partyStatusMessage(a));
        } else if (splitted[0].equalsIgnoreCase("!cpdebug")) {
            mc.dropMessage("CP: " + c.getPlayer().getCP() + "/" + c.getPlayer().getTotalCP());
            mc.dropMessage("Party CP: " + c.getPlayer().getMonsterCarnival().getCP(c.getPlayer().getTeam()) + "/" + c.getPlayer().getMonsterCarnival().getTotalCP(c.getPlayer().getTeam()));
            mc.dropMessage("Team: " + c.getPlayer().getTeam());
            mc.dropMessage("Has Instance: " + (c.getPlayer().getMonsterCarnival() != null));
            mc.dropMessage("Has enemy: " + (c.getPlayer().getParty().getEnemy() != null));
            mc.dropMessage("Enemy ID: " + c.getPlayer().getParty().getEnemy().getId());
            mc.dropMessage("Red team: " + (c.getPlayer().getMonsterCarnival().getRed() != null));
            mc.dropMessage("Red team ID: " + (c.getPlayer().getMonsterCarnival().getRed().getId()));
            mc.dropMessage("Blue team: " + (c.getPlayer().getMonsterCarnival().getBlue() != null));
            mc.dropMessage("Blue team ID: " + (c.getPlayer().getMonsterCarnival().getBlue().getId()));
            mc.dropMessage("Time left: " + (c.getPlayer().getMonsterCarnival().getTimeLeft()));
            mc.dropMessage("Time left seconds: " + c.getPlayer().getMonsterCarnival().getTimeLeftSeconds());
        } else if (splitted[0].equalsIgnoreCase("!zakclock")) {
            int a = Integer.parseInt(splitted[1]);
            c.getSession().write(MaplePacketCreator.showZakumShrineTimeLeft(a));
        } else if (splitted[0].equalsIgnoreCase("!npcdebug")) {
            List<MapleMapObject> npcs =
                    c.getPlayer().getMap().
                    getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
            for (MapleMapObject mmo : npcs) {
                mc.dropMessage(mmo.toString());
            }
        } else if (splitted[0].equalsIgnoreCase("!mapevent")) {
            if (c.getPlayer().getEventInstance() == null) {
                mc.dropMessage("You are not in an event instance!");
                return;
            }
            MapleMap map = c.getPlayer().getEventInstance().getMapFactory().getMap(Integer.parseInt(splitted[1]));
            c.getPlayer().changeMap(map, map.getPortal(0));
            mc.dropMessage("[Event Instance] warped to map " + Integer.parseInt(splitted[1]));
        } else if (splitted[0].equalsIgnoreCase("!cpqbuffmap")) {
            c.getPlayer().getMap().buffMonsters(c.getPlayer().getTeam(), MonsterStatus.WEAPON_IMMUNITY);
        } else if (splitted[0].equalsIgnoreCase("!buffmap")) {
            int buffID = Integer.parseInt(splitted[1]);
            c.getPlayer().getMap().buffMap(buffID);
        } else if (splitted[0].equalsIgnoreCase("!cpqunbuffmap")) {
            c.getPlayer().getMap().debuffMonsters(c.getPlayer().getTeam(), MonsterStatus.WEAPON_IMMUNITY);
        } else if (splitted[0].equalsIgnoreCase("!packet")) {
            MaplePacketLittleEndianWriter a = new MaplePacketLittleEndianWriter();
            a.write(HexTool.getByteArrayFromHexString(splitted[1]));
            MaplePacket packet = a.getPacket();
            c.getSession().write(packet);
        } else if (splitted[0].equalsIgnoreCase("!playernpc")) {
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(MapleLifeFactory.getNPC(9901000)));
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getPlayerNPC(c.getPlayer()));
        } else if (splitted[0].equalsIgnoreCase("!pmerchant")) {
            PlayerNPCMerchant merchant = new PlayerNPCMerchant(c.getPlayer(), 9901000);
            mc.dropMessage("Hows that eh..");

        } else if (splitted[0].equalsIgnoreCase("!save")) {
            c.getPlayer().saveToDB(true, false);
        } else if (splitted[0].equalsIgnoreCase("!disableportal")) {
            mc.dropMessage(c.getPlayer().getMap().setPortalDisable(!c.getPlayer().getMap().getPortalDisable()) + ": portal disabled");
        } else if (splitted[0].equalsIgnoreCase("!testchat")) {
            c.getSession().write(MaplePacketCreator.testChat("Hai", Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2])));
        }
    }

    public void teachSkill(MapleClient c, int skill, int a, int b) {
        c.getPlayer().changeSkillLevel(client.SkillFactory.getSkill(skill), a, b);
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
			new CommandDefinition("gc", "", "", 4),
			new CommandDefinition("resetquest", "", "", 4),
			new CommandDefinition("nearestPortal", "", "", 3),
			new CommandDefinition("spawndebug", "", "", 4),
			new CommandDefinition("timerdebug", "", "", 4),
			new CommandDefinition("threads", "", "", 4),
			new CommandDefinition("showtrace", "", "", 4),
			new CommandDefinition("toggleoffense", "", "", 4),
			new CommandDefinition("fakerelog", "", "", 4),
			new CommandDefinition("tdrops", "", "", 4),
			new CommandDefinition("givebuff", "", "", 4),
			new CommandDefinition("givemonsbuff", "", "", 4),
			new CommandDefinition("givemonstatus", "", "", 4),
			new CommandDefinition("sreactor", "[id]", "Spawn a Reactor", 4),
			new CommandDefinition("hreactor", "[object ID]", "Hit reactor", 4),
			new CommandDefinition("rreactor", "", "Resets all reactors", 4),
			new CommandDefinition("lreactor", "", "List reactors", 4),
			new CommandDefinition("dreactor", "", "Remove a Reactor", 4),
			new CommandDefinition("cpqhud", "", "", 4),
			new CommandDefinition("maxskills", "", "", 4),
			new CommandDefinition("partymessage", "", "", 4),
			new CommandDefinition("cpdebug", "", "", 4),
			new CommandDefinition("zakclock", "", "", 4),
			new CommandDefinition("npcdebug", "", "", 4),
			new CommandDefinition("mapevent", "", "", 4),
			new CommandDefinition("cpqbuffmap", "", "", 4),
			new CommandDefinition("buffmap", "", "", 4),
			new CommandDefinition("cpqunbuffmap", "", "", 4),
			new CommandDefinition("packet", "", "", 4),
			new CommandDefinition("hmerchantlist", "", "", 4),
			new CommandDefinition("playernpc", "", "", 4),
			new CommandDefinition("save", "", "", 4),
			new CommandDefinition("disableportal", "", "", 4),
			new CommandDefinition("pmerchant", "", "", 4),
			new CommandDefinition("pmerchant", "", "", 4),
			new CommandDefinition("testchat", "", "", 4),
		};
    }
}
