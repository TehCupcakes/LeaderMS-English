/*
 * This file is part of the OdinMS Maple Story Server
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

package scripting.reactor;

import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import client.inventory.Equip;
import client.IItem;
import client.inventory.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.MapleQuestStatus;
import java.util.Random;
import handling.channel.ChannelServer;
import scripting.AbstractPlayerInteraction;
import scripting.event.EventManager;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MapleMonsterInformationProvider.DropEntry;
import server.life.MapleMonsterQuestInformationProvider.QuestDropEntry;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;

/**
 * @author Lerk
 */

public class ReactorActionManager extends AbstractPlayerInteraction {
    private MapleReactor reactor;
    private MapleClient c;

    public ReactorActionManager(MapleClient c, MapleReactor reactor) {
        super(c);
        this.reactor = reactor;
        this.c = c;
    }

    // only used for meso = false, really. No minItems because meso is used to fill the gap
    public void dropItems() {
            dropItems(false, 0, 0, 0, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso) {
            dropItems(meso, mesoChance, minMeso, maxMeso, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems) {
        List<ReactorDropEntry> chances = ReactorScriptManager.getInstance().getDrops(this.reactor.getReactorId());
        List<ReactorDropEntry> items = new LinkedList<>();
        int numItems = 0;

        if (meso && Math.random() < (1.0D / mesoChance)) {
                items.add(new ReactorDropEntry(0, mesoChance, -1));
        }

        // narrow list down by chances
        Iterator<ReactorDropEntry> iter = chances.iterator();
        // for (DropEntry d : chances){
        while (iter.hasNext()) {
                ReactorDropEntry d = (ReactorDropEntry) iter.next();
                if (Math.random() < (1.0D / d.chance) && ((d.questid <= 0) || (getPlayer().getQuest(MapleQuest.getInstance(d.questid)).getStatus() == MapleQuestStatus.Status.STARTED))) {
                        numItems++;
                        items.add(d);
                }
        }

        // if a minimum number of drops is required, add meso
        while (items.size() < minItems) {
                items.add(new ReactorDropEntry(0, mesoChance, -1));
                numItems++;
        }

        // randomize drop order
        java.util.Collections.shuffle(items);

        final Point dropPos = reactor.getPosition();

        dropPos.x -= (12 * numItems);

        for (ReactorDropEntry d : items) {
            if (d.itemId == 0) {
                    int range = maxMeso - minMeso;
                    int displayDrop = (int) (Math.random() * range) + minMeso;
                    int mesoDrop = (int) (displayDrop * ChannelServer.getInstance(getClient().getChannel()).getMesoRate());
                    reactor.getMap().spawnMesoDrop(mesoDrop, displayDrop, dropPos, reactor, getPlayer(), meso);
            } else {
                    IItem drop;
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    if (ii.getInventoryType(d.itemId) != MapleInventoryType.EQUIP) {
                            drop = new Item(d.itemId, (byte) 0, (short) 1);
                    }
                    else {
                            drop = ii.randomizeStats((Equip) ii.getEquipById(d.itemId));
                    }
                    reactor.getMap().spawnItemDrop(reactor, getPlayer(), drop, dropPos, false, true);
            }
            dropPos.x += 25;
        }
    }
    
    public EventManager getEventManager(String event) {
		return getClient().getChannelServer().getEventSM().getEventManager(event);
    }
    
    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPosition());
    }

    public void spawnMonster(int id, int x, int y) {
        spawnMonster(id, 1, new Point(x, y));
    }
    
    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, getPosition());
    }

    public void spawnMonster(int id, int qty, int x, int y) {
        spawnMonster(id, qty, new Point(x, y));
    }

    private void spawnMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            MapleMonster mob = MapleLifeFactory.getMonster(id);
            reactor.getMap().spawnMonsterOnGroudBelow(mob, pos);
        }
    }

    public Point getPosition() {
        Point pos = reactor.getPosition();
        pos.y -= 10;
        return pos;
    }

    /**
     * Spawns an NPC at the reactor's location
     * @param [Int] npcId
     */
    public void spawnNpc(int npcId) {
            spawnNpc(npcId, getPosition());
    }

    /**
     * Spawns an NPC at a custom position
     * @param [Int] npcId
     * @param [Int] X
     * @param [Int] Y
     */
    public void spawnNpc(int npcId, int x, int y) {
            spawnNpc(npcId, new Point(x,y));
    }

    /**
     * Spawns an NPC at a custom position
     * @param [Int] npcId
     * @param [Point] pos
     */
    public void spawnNpc(int npcId, Point pos) {
        MapleNPC npc = MapleLifeFactory.getNPC(npcId);
        if (npc != null && !npc.getName().equals("MISSINGNO")) {
            npc.setPosition(pos);
            npc.setCy(pos.y);
            npc.setRx0(pos.x + 50);
            npc.setRx1(pos.x - 50);
            npc.setFh(reactor.getMap().getFootholds().findBelow(pos).getId());
            npc.setCustom(true);
            reactor.getMap().addMapObject(npc);  
            reactor.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
        }
    }

    public MapleReactor getReactor() {
        return reactor;
    }

    public void spawnFakeMonster(int id) {
        spawnFakeMonster(id, 1, getPosition());
    }

    // summon one monster, remote location
    public void spawnFakeMonster(int id, int x, int y) {
        spawnFakeMonster(id, 1, new Point(x, y));
    }

    // multiple monsters, reactor location
    public void spawnFakeMonster(int id, int qty) {
        spawnFakeMonster(id, qty, getPosition());
    }

    // multiple monsters, remote location
    public void spawnFakeMonster(int id, int qty, int x, int y) {
        spawnFakeMonster(id, qty, new Point(x, y));
    }

    // handler for all spawnFakeMonster
    private void spawnFakeMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            MapleMonster mob = MapleLifeFactory.getMonster(id);
            reactor.getMap().spawnFakeMonsterOnGroundBelow(mob, pos);
        }
    }

    public void killAll() {
        reactor.getMap().killAllMonsters(false);
    }

    public void killMonster(int monsId) {
        reactor.getMap().killMonster(monsId);
    }

    protected MapleClient getClient() {
        return c;
    }
	
   public void closeDoor(int mapid) {
       getClient().getChannelServer().getMapFactory().getMap(mapid).setReactorState();
   }

   public void openDoor(int mapid) {
       getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
   }
}
