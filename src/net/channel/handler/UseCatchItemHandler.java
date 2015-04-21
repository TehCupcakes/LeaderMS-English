package net.channel.handler;

import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
*
* @author Pat
*/

/**
 *
 * @author Pat
 */
public class UseCatchItemHandler extends AbstractMaplePacketHandler {

    public UseCatchItemHandler() {
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (System.currentTimeMillis() - c.getPlayer().getLastCatch() < 2000) {
            c.getSession().write(MaplePacketCreator.serverNotice(5, "You cannot use the rock right now."));
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        slea.readInt();
        slea.readShort();
        int itemid = slea.readInt();
        int oid = slea.readInt();
        MapleMonster mob = c.getPlayer().getMap().getMonsterByOid(oid);
        switch (itemid) {
            case 2270002: { // Characteristic Stone
                final MapleMap map = c.getPlayer().getMap();
                if (map.getId() != 980010101 && map.getId() != 980010201 && map.getId() != 980010301) {
                    c.getPlayer().dropMessage(1, "This item is not available for use outside the AriantPQ!");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if (mob.getHp() <= mob.getMaxHp() / 2) {
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(oid, itemid, (byte) 1));
                    mob.getMap().killMonster(mob, c.getPlayer(), false, false, 0);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                    MapleInventoryManipulator.addById(c, 4031868, (short)1, null, "", -1);
                    c.getSession().write(MaplePacketCreator.serverNotice(5, "You won a jewel!"));
                    c.getPlayer().setLastCatch(System.currentTimeMillis());
                    c.getPlayer().updateAriantScore();
                } else {
                  ///  map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 0));
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.catchMonster(oid, itemid, (byte) 0));
                    c.getPlayer().dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
                }
                break;
            }
            case 2270000: { // Pheromone Perfume
                if (mob.getId() != 9300101) {
                    break;
                }
                final MapleMap map = c.getPlayer().getMap();

                map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
                map.killMonster(mob, c.getPlayer(), true, false, (byte) 0);
                MapleInventoryManipulator.addById(c, 1902000, (short) 1, null);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                break;
            }
            case 2270003: { // Cliff's Magic Cane
                if (mob.getId() != 9500320) {
                    break;
                }
                final MapleMap map = c.getPlayer().getMap();

                if (mob.getHp() <= mob.getMaxHp() / 2) {
                    map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
                    map.killMonster(mob, c.getPlayer(), true, false, (byte) 0);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                } else {
                    map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 0));
                    c.getPlayer().dropMessage(5, "The monster has too much physical strength, so you cannot catch it.");
                }
                break;
            }
            case 2270001: {
                if (mob.getId() != 9500197) {
                    break;
                }
                final MapleMap map = c.getPlayer().getMap();
                map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
                map.killMonster(mob, c.getPlayer(), true, false, (byte) 0);
                MapleInventoryManipulator.addById(c, 1902000, (short) 1, null);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                break;
            }

            case 2270005: {

                if (mob.getId() != 9300187) {
                    break;
                }
                final MapleMap map = c.getPlayer().getMap();
                if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {

                    map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
                    map.killMonster(mob, c.getPlayer(), true, false, (byte) 0);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                    MapleInventoryManipulator.addById(c, 2109001, (short) 1, "");
                }
            }
            c.getSession().write(MaplePacketCreator.enableActions());
            break;

            case 2270006: {
                if (mob.getId() == 9300189) {
                    final MapleMap map = c.getPlayer().getMap();
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
                        map.killMonster(mob, c.getPlayer(), true, false, (byte) 0);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                        MapleInventoryManipulator.addById(c, 2109002, (short) 1, "");
                    }

                }
                c.getSession().write(MaplePacketCreator.enableActions());
                break;
            }
            case 2270007: {
                if (mob.getId() == 9300191) {
                    final MapleMap map = c.getPlayer().getMap();
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 3)) {
                        map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
                        map.killMonster(mob, c.getPlayer(), true, false, (byte) 0);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                        MapleInventoryManipulator.addById(c, 2109003, (short) 1, "");
                    }
                }
                c.getSession().write(MaplePacketCreator.enableActions());
                break;
            }
            case 2270004: {
                if (mob.getId() == 9300175) {
                    final MapleMap map = c.getPlayer().getMap();
                    if (mob.getHp() < ((mob.getMaxHp() / 10) * 4)) {
                        map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
                        map.killMonster(mob, c.getPlayer(), true, false, (byte) 0);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                        MapleInventoryManipulator.addById(c, 4001169, (short) 1, "");
                    }
                }
                c.getSession().write(MaplePacketCreator.enableActions());
                break;
            }
            case 2270008: {
                if (mob.getId() == 9500336) {
                    final MapleMap map = c.getPlayer().getMap();
                    map.broadcastMessage(MaplePacketCreator.catchMonster(mob.getId(), itemid, (byte) 1));
                    map.killMonster(mob, c.getPlayer(), true, false, (byte) 0);
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, false, false);
                    MapleInventoryManipulator.addById(c, 2022323, (short) 1, "");
                } else {
                    c.getPlayer().dropMessage(5, "You cannot use the Fishing Net yet.");
                }
                c.getSession().write(MaplePacketCreator.enableActions());
            }
            break;
        }

    }
}
