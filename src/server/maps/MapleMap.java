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
package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.inventory.Equip;
import client.IItem;
import client.inventory.Item;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.messages.MessageCallback;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import config.configuration.Configuration;
import database.DatabaseConnection;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import handling.channel.pvp.MaplePvp;
import handling.world.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import scripting.event.EventManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleOxQuiz;
import server.MaplePortal;
import server.MapleSquad;
import server.MapleSquadType;
import server.MapleStatEffect;
import server.PropertiesTable;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.SpawnPoint;
import tools.packet.MaplePacketCreator;
import tools.packet.PetPacket;
import tools.Pair;

public class MapleMap {

	private static final int MAX_OID = 20000;
        private MapleClient c;
	private static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(MapleMapObjectType.ITEM,
		MapleMapObjectType.MONSTER, MapleMapObjectType.DOOR, MapleMapObjectType.SUMMON, MapleMapObjectType.REACTOR);
	/**
	 * Holds a mapping of all oid -> MapleMapObject on this map. mapobjects is NOT a synchronized collection since it
	 * has to be synchronized together with runningOid that's why all access to mapobjects have to be done trough an
	 * explicit synchronized block
	 */
	private Map<Integer, MapleMapObject> mapobjects = new LinkedHashMap<Integer, MapleMapObject>();
	private Collection<SpawnPoint> monsterSpawn = new LinkedList<SpawnPoint>();
	private AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
	private Collection<MapleCharacter> characters = new LinkedHashSet<MapleCharacter>();
	private Map<Integer, MaplePortal> portals = new HashMap<Integer, MaplePortal>();
	private List<Rectangle> areas = new ArrayList<Rectangle>();
	private MapleFootholdTree footholds = null;
        private final Lock mutex = new ReentrantLock();
	private int mapid;
	private int runningOid = 100;
	private int returnMapId;
	private int channel;
	private float monsterRate;
	private boolean dropsDisabled = false;
	private boolean clock;
        private boolean boat;
        private boolean docked;
        private boolean train;
        private boolean trainStopped;
	private String mapName;
	private String streetName;
	private MapleMapEffect mapEffect = null;
	private boolean everlast = false;
	private int forcedReturnMap = 999999999;
	private int timeLimit;
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MapleMap.class);
	private MapleMapTimer mapTimer = null;
	private int dropLife = 180000; //Time in milliseconds drops last before disappearing
	private int decHP = 0;
	private int protectItem = 0;
        public boolean respawnMonsters = true;
	private boolean town;
	private boolean timer = false, checkStates = true;
	private List<MonsterStatus> redTeamBuffs = new LinkedList<MonsterStatus>();
        private final ReentrantReadWriteLock charactersLock = new ReentrantReadWriteLock();
	private List<MonsterStatus> blueTeamBuffs = new LinkedList<MonsterStatus>();
	private List<Point> takenSpawns = new LinkedList<Point>();
	private List<GuardianSpawnPoint> guardianSpawns = new LinkedList<GuardianSpawnPoint>();
	protected boolean swim;
        private int dropRate;
        private int bossDropRate;
        public ScheduledFuture respawnTask;
        private PropertiesTable properties;
	private boolean disablePortal = false;
	private boolean disableInvincibilitySkills = false;
	private boolean disableDamage = false;
	private boolean disableChat = false;
	private ScheduledFuture<?> sfme = null;
	private MapleOxQuiz ox = null;
        private boolean allowHPQSummon = false;
        private Pair<Integer, String> timeMob = null;
        private short mobInterval = 5000;
        public Map<Integer, Integer> reactorLink = new HashMap<>();
        private ScheduledFuture<?> CashPQTimer;
        // Objetos Lock
        private ReentrantReadWriteLock objectlock = new ReentrantReadWriteLock(true);
        private ReentrantReadWriteLock characterlock = new ReentrantReadWriteLock(true);
        
        
        
    public MapleMap(int mapid, int channel, int returnMapId, float monsterRate) {
        this.mapid = mapid;
        this.channel = (short) channel;
        this.returnMapId = returnMapId;
        this.monsterRate = monsterRate;
        this.dropRate = ChannelServer.getInstance(channel).getDropRate();
        this.bossDropRate = ChannelServer.getInstance(channel).getBossDropRate();
        this.properties = new PropertiesTable();
        properties.setProperty("respawn", Boolean.TRUE);
        InitiateRespawnTask();

    }
    
     public void InitiateRespawnTask() {
        respawnTask = TimerManager.getInstance().register(new Runnable() {

            @Override
            public void run() {
                ChannelServer.getInstance(channel).getMapFactory().getMap(mapid).respawn();
            }
        }, 10000);
    }

     public void broadcastNonGmMessage(MapleCharacter source, MaplePacket packet) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isGM()) {
                    chr.getClient().getSession().write(packet);
                }
            }
        }
    }

     public void checkStates(final String chr) {
        if (!checkStates) {
            return;
        }
        final MapleSquad sqd = getSquadByMap();
        final EventManager em = getEMByMap();
        final int size = getCharactersSize();
        if (em != null && em.getProperty("state") != null && (sqd == null || sqd.getStatus() == 2) && size == 0) {
            em.setProperty("state", "0");
            if (em.getProperty("leader") != null) {
                em.setProperty("leader", "true");
            }
        }
    }
     
      public final EventManager getEMByMap() {
        String em = null;
        switch (mapid) {
            case 105100400:
                em = "BossBalrog_EASY";
                break;
            case 105100300:
                em = "BossBalrog_NORMAL";
                break;
            case 280030000:
                em = "ZakumBattle";
                break;
            case 240060200:
                em = "HorntailBattle";
                break;
            case 280030001:
                em = "ChaosZakum";
                break;
            case 240060201:
                em = "ChaosHorntail";
                break;
            case 270050100:
                em = "PinkBeanBattle";
                break;
            case 802000111:
                em = "NamelessMagicMonster";
                break;
            case 802000211:
                em = "Vergamot";
                break;
            case 802000311:
                em = "2095_tokyo";
                break;
            case 802000411:
                em = "Dunas";
                break;
            case 802000611:
                em = "Nibergen";
                break;
            case 802000711:
                em = "Dunas2";
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                em = "CoreBlaze";
                break;
            case 802000821:
            case 802000823:
                em = "Aufhaven";
                break;
            case 211070100:
            case 211070101:
            case 211070110:
                em = "VonLeonBattle";
                break;
            case 551030200:
                em = "ScarTarBattle";
                break;
            case 271040100:
                em = "CygnusBattle";
                break;
            default:
                return null;
        }
        return ChannelServer.getInstance(channel).getEventSM().getEventManager(em);
    }
     
      public final MapleSquad getSquadByMap() {
        MapleSquadType zz = null;
        switch (mapid) {
            case 240060200:
                zz = MapleSquadType.HORNTAIL;
                break;
            case 280030000:
                zz = MapleSquadType.ZAKUM;
                break;
            default:
                return null;
        }
        return ChannelServer.getInstance(channel).getMapleSquad(zz);
    }

     public final List<MapleCharacter> getCharactersThreadsafe() {
        final List<MapleCharacter> chars = new ArrayList<MapleCharacter>();


        charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : characters) {
                chars.add(mc);
            }
        } finally {
            charactersLock.readLock().unlock();
        }
        return chars;
    }

    public List<MapleMapObject> getMapObjectsOfType(MapleMapObjectType type) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (type.equals(l.getType())) {
                    ret.add(l);
                }
            }
        }
        return ret;
    }
    
    public List<MapleCharacter> getPlayersInRect(Rectangle box) {
		List<MapleCharacter> character = new ArrayList<MapleCharacter>();
		synchronized (characters) {
			for (MapleCharacter a : characters) {

					if (box.contains(a.getPosition())) {
						character.add(a);
					}

			}
		}
		return character;
	}

    public void getPlayersInRect2(Rectangle box, ArrayList<MapleCharacter> character) {

		synchronized (characters) {
			for (MapleCharacter a : characters) {

					if (box.contains(a.getPosition())) {
						character.add(a);
					}

			}
		}

	}

     public void spawnItemDrop(final int dropperId, final Point dropperPos, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean expire) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropperId, dropperPos, owner);
        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, ffaDrop ? 0 : owner.getId(), dropperPos, droppos, (byte) 1));
            }
        });
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, ffaDrop ? 0 : owner.getId(), dropperPos, droppos, (byte) 0), drop.getPosition());
        if (expire) {
            TimerManager.getInstance().schedule(new ExpireMapItemJob(drop), dropLife);
        }
        activateItemReactors(drop);
    }

    public void clearDrops() {
        for (MapleMapObject i : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM))) {
            removeMapObject(i);
        }
    }
    


     private class RespawnWorker implements Runnable {
        @Override
		public void run() {
			int playersOnMap = characters.size();

			if (playersOnMap == 0) {
				return;
			}
			int ispawnedMonstersOnMap = spawnedMonstersOnMap.get();
                        int getMaxSpawn = getMaxRegularSpawn() * 2;
			int numShouldSpawn = getMaxSpawn - ispawnedMonstersOnMap;
                        if (numShouldSpawn + ispawnedMonstersOnMap >= getMaxSpawn) {
				numShouldSpawn = getMaxSpawn - ispawnedMonstersOnMap;
			}
			if (numShouldSpawn <= 0) {
				return;
			}
			List<SpawnPoint> randomSpawn = new ArrayList<SpawnPoint>(monsterSpawn);
			Collections.shuffle(randomSpawn);
			int spawned = 0;
			for (SpawnPoint spawnPoint : randomSpawn) {
				if (spawnPoint.shouldSpawn()) {
					spawnPoint.spawnMonster(MapleMap.this);
					spawned++;
				}
				if (spawned >= numShouldSpawn) {
					break;
				}
			}
		}
	}



	public boolean toggleDrops() {
		dropsDisabled = !dropsDisabled;
        return dropsDisabled;
	}

	public int getId() {
		return mapid;
	}
        
        public void setAllowHPQSummon(boolean b) {
        this.allowHPQSummon = b;
        }

	public MapleMap getReturnMap() {
		if (returnMapId == 999999999) return null;
		try {
			return ChannelServer.getInstance(channel).getMapFactory().getMap(returnMapId);
		} catch (Exception ex) {
			return null;
		}
	}

	public int getReturnMapId() {
		return returnMapId;
	}

	public int getForcedReturnId() {
		return forcedReturnMap;
	}

	public MapleMap getForcedReturnMap() {
		if (forcedReturnMap == 999999999) return null;
		return ChannelServer.getInstance(channel).getMapFactory().getMap(forcedReturnMap);
	}

	public void setForcedReturnMap(int map) {
		this.forcedReturnMap = map;
	}

	public int getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
	}

	public int getCurrentPartyId() {
        for (MapleCharacter chr : this.getCharacters()) {
            if (chr.getPartyId() != -1) {
                return chr.getPartyId();
            }
        }
        return -1;
    }

      public void addMapObject(MapleMapObject mapobject) {
        synchronized (this.mapobjects) {
            mapobject.setObjectId(runningOid);
            this.mapobjects.put(Integer.valueOf(runningOid), mapobject);
            incrementRunningOid();
        }
    }

        
   private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery) {
        spawnAndAddRangedMapObject(mapobject, packetbakery, null);
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery, SpawnCondition condition) {
        synchronized (this.mapobjects) {
            mapobject.setObjectId(runningOid);
            synchronized (characters) {
                for (MapleCharacter chr : characters) {
                    if (condition == null || condition.canSpawn(chr)) {
                        if (chr.getPosition().distanceSq(mapobject.getPosition()) <= 722500 && !chr.isFake()) {
                            packetbakery.sendPackets(chr.getClient());
                            chr.addVisibleMapObject(mapobject);
                        }
                    }
                }
            }
            this.mapobjects.put(Integer.valueOf(runningOid), mapobject);
            incrementRunningOid();
        }
    }

    
	private void incrementRunningOid() {
        runningOid++;
        for (int numIncrements = 1; numIncrements < MAX_OID; numIncrements++) {
            if (runningOid > MAX_OID) {
                runningOid = 100;
            }
            if (this.mapobjects.containsKey(Integer.valueOf(runningOid))) {
                runningOid++;
            } else {
                return;
            }
        }
        throw new RuntimeException("Out of OIDs on map " + mapid + " (channel: " + channel + ")");
    }

      public void removeMapObject(int num) {
        synchronized (this.mapobjects) {
            this.mapobjects.remove(Integer.valueOf(num));
        }
    }

    public void removeMapObject(MapleMapObject obj) {
        removeMapObject(obj.getObjectId());
    }

    private Point calcPointBelow(Point initial) {
        MapleFoothold fh = footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            double s5 = Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2)));
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) s5;
            } else {
                dropY = fh.getY1() + (int) s5;
            }
        }
        return new Point(initial.x, dropY);
    } 

	public Point calcDropPos(Point initial, Point fallback) {
		Point ret = calcPointBelow(new Point(initial.x, initial.y - 99));
		if (ret == null) {
			return fallback;
		}
		return ret;
	}

     public void setReactorState() {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setState((byte) 1);
                    broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 1));
                }
            }
        }
    }

    public void setReactorState(int state) {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setState((byte) state);
                    broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, state));
                }
            }
        }
    }

    public void setReactorState(MapleReactor reactor, byte state) {
        synchronized (this.mapobjects) {
            ((MapleReactor) reactor).setState((byte) state);
            broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) reactor, state));
        }
    }

    private int countDrops(List<Integer> theDrop, int dropId) {
        int count = 0;
        for (int i = 0; i < theDrop.size(); i++) {
            if (theDrop.get(i) == dropId) {
                count++;
            }
        }
        return count;
    }
	
	public boolean isWarMap() {
		switch (this.getId()) {
			case 610020011: //Cavern Of Pain
				return true;
		}
		return false;
	}
	
    private int rand(int lbound, int ubound) {
        return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
    }
    
	
    private void dropFromMonster(MapleCharacter dropOwner, MapleMonster monster) {
        if (dropsDisabled || monster.dropsDisabled()) {
            return;
		}
        /*
         * Folha Maple/Neve
         */
       List<Integer> toDrop = new ArrayList<Integer>();
       int chancefolha = (int) (Math.random() * 100);
       int chanceAPQItem = (int) (Math.random() * 100);
       int chanceticket = (int) (Math.random() * 650);
       int chancecards = (int) (Math.random() * 300 * 10);
       if (dropOwner.inPQ()) {
            } else {
            if (chancefolha <= 25) //30% chance of getting a maple leaf
              toDrop.add(4001126);
           }
        if (dropOwner.inPQ()) {
            } else {
            if (chanceticket == 5) //95% chance of getting a ticket
              toDrop.add(5220000);
           }
        if (dropOwner.inPQ()) {
            } else {
            if (chancecards <= 15) //10% chance nx card 250
              toDrop.add(4031531);
           }
       if (dropOwner.inPQ()) {
            } else {
            if (chancecards <= 20) //15% chance nx card 250
              toDrop.add(4031530);
           }
       /* AriantPQ Drops */
       if (dropOwner.getMapId() == 980010101 || dropOwner.getMapId() == 980010201 || dropOwner.getMapId() == 980010301) {
            if (chanceAPQItem <= 10) 
              toDrop.add(2022266); 
           }
       if (dropOwner.getMapId() == 980010101 || dropOwner.getMapId() == 980010201 || dropOwner.getMapId() == 980010301) {
            if (chanceAPQItem <= 10) 
              toDrop.add(2022267);
           }
        if (dropOwner.getMapId() == 980010101 || dropOwner.getMapId() == 980010201 || dropOwner.getMapId() == 980010301) {
            if (chanceAPQItem <= 10) 
              toDrop.add(2022269);
           }
        /* Fim */
       if (dropOwner.getMapId() == 221020800 || dropOwner.getMapId() == 221020700 || dropOwner.getMapId() == 221020900 || dropOwner.getMapId() == 221022000 || dropOwner.getMapId() == 221021900 || dropOwner.getMapId() == 221021800 || dropOwner.getMapId() == 221021700 || dropOwner.getMapId() == 221021300 || dropOwner.getMapId() == 221021200) {
           return;
           }       
       /*
        * Fim
        *///922010600

        String text = "";
        EventInstanceManager eim = dropOwner.getEventInstance();
        if (eim != null) {
            text = eim.getName();
        }
        if (text.startsWith("BossHunterPQ")) {
            bossHunterDrops(monster, dropOwner);
            return;
        }
        if (text.startsWith("AquaPQ")) {
            if (monster.getId() == 8150100 || monster.getId() == 8150101 || monster.getId() == 8140600 || monster.getId() == 8141300 || monster.getId() == 8142100) {
                aquaPQDrops(monster, dropOwner);
                return;
            } else {
                return;
            }
        }

        if (monster.getId() >= 9300127 && monster.getId() <= 9300136) {
            monsterCarnivalDrops(dropOwner, monster);
            return;
        }

        if (!monster.hasDrop()) {
            return;
        }

        /*
         * drop logic: decide based on monster what the max drop count is get drops (not allowed: multiple mesos,
         * multiple items of same type exception: event drops) calculate positions
         */
        int maxDrops;
        int maxMesos;
        int droppedMesos = 0;
        boolean isGoodBoss = false;
        boolean ffa = monster.hasPublicReward();

        switch (monster.getId()) {
            case 6130104:
            case 6230101:
            case 6300003:
            case 6300004:
            case 6400004:
            case 9300038:
            case 9300040:
            case 9300050:
            case 9300062:
            case 9300063:
            case 9300064:
            case 9300081:
            case 9300082:
            case 9300083:
            case 9400520:
            case 9500180:
                return;
        }
		
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final boolean isBoss = monster.isBoss();
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        if (isBoss) {
            maxDrops = 1 * cserv.getBossDropRate() * dropOwner.hasDropCard();
			maxMesos = 1;
        } else {
           maxDrops = 2 * cserv.getDropRate() * dropOwner.hasDropCard();
			maxMesos = 1;
        }
		switch (monster.getId()) {
            case 9300157:
                maxMesos = 0;
                break;
            case 8800002:
            case 9400300:
                maxDrops = 35; //Amount that Zak should Drop o.O
                maxMesos = 7;
                isGoodBoss = true;
                break;
            case 8510000:
            case 8520000:
            case 8500002:
            case 9400121:
                maxDrops = 20;
                maxMesos = 5;
                isGoodBoss = true;
                break;
            case 94000014:
                maxDrops = 14;
                isGoodBoss = true;
                maxMesos = 4;
                break;
            case 8180000:
            case 8180001:
            case 9400549:
                maxDrops = 6;
                isGoodBoss = true;
                maxMesos = 2;
                break;
            case 8810018:
                maxDrops = 70;
                maxMesos = 9;
                isGoodBoss = true;
                break;
            case 9400514:
            case 9300010:
            case 9300012:
            case 9300028:
            case 9300039:
            case 9300058:
                maxDrops = 1;
                break;
            case 9300059:
            case 9300095:
            case 9300094:
                maxDrops = 2;
                break;
            case 9300060:
                maxDrops = 2 + (int) (Math.floor(Math.random() * 3));
                break;
            case 9400202:
                maxDrops = 10;
                break;
        }
        int droppedMezar = 0;
        if (monster.isHT() || monster.getId() == 8800002) {
            for (int i = 0; i < maxDrops; i++) {
                int theDrop = monster.getDrop();
                if (theDrop == -1 && droppedMezar < 7) {
                    toDrop.add(theDrop);
                    droppedMezar++;
                } else if (theDrop == -1 && droppedMezar >= 7) {
                    i--;
                    continue;
                } else if (theDrop == 4001094 && countDrops(toDrop, 4001094) >= 4) { //0xFF, 0xBC T_T
                    i--;
                    continue;
                } else if (theDrop == 5220001 && countDrops(toDrop, 5220001) >= 4) { //0xFA, 0xAD T_T
                    i--;
                    continue;
                } else if (theDrop == 1122000 && toDrop.contains(theDrop)) {
                    i--;
                    continue;
                } else {
                    toDrop.add(theDrop);
                }
            }
        } else if (isGoodBoss) {
            for (int i = 0; i < maxDrops; i++) {
                int toAdd = monster.getDrop();
                if (toAdd == 4000141 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4000175 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031253 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031196 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4000224 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4000235 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4001076 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4000243 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031457 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031903 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4000138 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031901 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031901 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031902 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031903 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031904 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031905 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == 4031906 && toDrop.contains(toAdd)) {
                    i--;
                    continue;
                } else if (toAdd == -1 && droppedMesos < maxMesos) {
                    toDrop.add(toAdd);
                    droppedMezar++;
                    continue;
                } else if (toAdd == -1 && droppedMesos >= maxMesos) {
                    i--;
                    continue;
                } else {
                    toDrop.add(toAdd);
                }
            }
        } else {
            for (int i = 0; i < maxDrops; i++) {
                int toAdd = monster.getDrop();
                if (toAdd == 4000040 && countDrops(toDrop, 4000040) >= 1) {
                    i--;
                    continue;
                }
                if (toAdd == 4000176 && countDrops(toDrop, 4000176) >= 1) {
                    i--;
                    continue;
                }
                toDrop.add(monster.getDrop());
            }
        }
        if (dropOwner.getEventInstance() == null) { //doesn't drop in event instances (eg. PQs)
            Random randomInt = new Random();
            int chance = randomInt.nextInt(1400);
            if (chance <= 1) { //0.1% chance
                toDrop.add(4031530); // 100 NX
            }
        }

        if (dropOwner.getEventInstance() == null) { //doesn't drop in event instances (eg. PQs)
            Random randomInt = new Random();
            int chance = randomInt.nextInt(2000);
            if (chance <= 1) { //0.01% chance
                toDrop.add(4031531); // 250 NX
            }
        }
        if (dropOwner.getEventInstance() == null) { //doesn't drop in event instances (eg. PQs)
            Random randomInt = new Random();
            int chance = randomInt.nextInt(3400);
            if (chance <= 1) { //0.01% chance
                toDrop.add(5220000); // Gachapon
            }
        }

        if (dropOwner.getEventInstance() == null) { //doesn't drop in event instances (eg. PQs)
            Random randomInt = new Random();
            int chance = randomInt.nextInt(5500);
            if (chance <= 1) { //0.01% chance
                toDrop.add(5072000); // Super Megaphone
            }
        }

        if (dropOwner.getEventInstance() == null) { //doesn't drop in event instances (eg. PQs)
            Random randomInt = new Random();
            int chance = randomInt.nextInt(7500); //changethese 3/12/14
            if (chance <= 1) { //0.01% chance
                toDrop.add(5050000); // AP Reset
            }
        }
        Set<Integer> alreadyDropped = new HashSet<Integer>();
               int htpendants = 0;
        int htstones = 0;
        int zhelms = 0;
        for (int i = 0; i < toDrop.size(); i++) { //htpendant
            if (toDrop.get(i) == 1122000) {
                if (htpendants > 3) {
                    toDrop.set(i, -1);
                } else {
                    htpendants++;
                }
            } else if (toDrop.get(i) == 2041200) { //htstone
                if (htstones > 3) {
                    toDrop.set(i, -1);
                } else {
                    htstones++;
                }
            } else if (toDrop.get(i) == 1002357) { //zhelm 1
                if (zhelms > 3) {
                    toDrop.set(i, -1);
                } else {
                    zhelms++;
                }
            } else if (alreadyDropped.contains(toDrop.get(i)) && !isGoodBoss) {
                toDrop.remove(i);
                i--;
            } else {
                alreadyDropped.add(toDrop.get(i));
            }
        }
        if (toDrop.size() > maxDrops) {
            toDrop = toDrop.subList(0, maxDrops);
        }
        //Neckson cards
        final int[] necksonCards = {4031530, 4031531};
        
        if (getCardChance(monster.getLevel(), isBoss)) {
            int necksonCard = necksonCards[(int) (Math.random() * necksonCards.length)];
            toDrop.add(necksonCard);
        }
        if (monster.isHT() || monster.getId() == 8800002) {
            int repeat = this.rand(2, 5);
            for (int a = 0; a < repeat; a++) {
                int necksonCard = necksonCards[(int) (Math.random() * necksonCards.length)];
                toDrop.add(necksonCard);
            }
        }
        //end neckson cards
		 int questDrop = -1;
        for (int k = 0; k < maxDrops; k++) {
            questDrop = monster.getQuestDrop(dropOwner);
            if (questDrop != 1) {
                break;
            }
        }
        if (questDrop != 1) { //Quest Drops
            toDrop.add(questDrop);
        }
        switch (monster.getId()) { //CWK
            case 9400573: // Baby typhon
            case 9400574: // Typhon
            case 9400575: // Bigfoot
            case 9400576: // Windraider
            case 9400577: // Firebrand
            case 9400578: // Firebrand
            case 9400579: // Nightshadow
            case 9400580: // Elderwraith
            case 9400581: // Stormbreaker
            case 9400582: // Crimson Guardian
            case 9400583: // Leprechaun
            case 9400584: // Leprechaun
            case 9400585: // Crimson Tree
            case 9400586: // Crimson Tree
            case 9400587: // Phantom Tree
            case 9400588: // Phantom Tree
                if (isBoss) {
                    toDrop.add(3992039); //mark of hero
                } else if (rand(0, 140 - monster.getLevel()) == 1) {
                    toDrop.add(3992039); //mark of hero
                }
                break;
        }
        Collections.shuffle(toDrop);
        Point[] toPoint = new Point[toDrop.size()];
        int shiftDirection = 0;
        int shiftCount = 0;

        int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
                footholds.getMaxDropX() - toDrop.size() * 25);
        int curY = Math.max(monster.getPosition().y, footholds.getY1());
        //int monsterShift = curX -
        while (shiftDirection < 3 && shiftCount < 1000) {
            // TODO for real center drop the monster width is needed o.o"
            if (shiftDirection == 1) {
                curX += 25;
            } else if (shiftDirection == 2) {
                curX -= 25;
                // now do it
            }
            for (int i = 0; i < toDrop.size(); i++) {
                MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                if (wall != null) {
                    //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                    if (wall.getX1() < curX) {
                        shiftDirection = 1;
                        shiftCount++;
                        break;
                    } else if (wall.getX1() == curX) {
                        if (shiftDirection == 0) {
                            shiftDirection = 1;
                        }
                        shiftCount++;
                        break;
                    } else {
                        shiftDirection = 2;
                        shiftCount++;
                        break;
                    }
                } else if (i == toDrop.size() - 1) {
                    //System.out.println("ok " + curX);
                    shiftDirection = 3;
                }
                final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                toPoint[i] = new Point(curX + i * 25, curY);
                final int drop = toDrop.get(i);

                if (drop == -1) { // meso
                    if (droppedMesos < maxMesos) {
                        if ((monster.getId() == 8810018
                                || monster.getId() == 8800002
                                || monster.getId() == 9400300
                                || monster.getId() == 9400121
                                || monster.getId() == 9400014
                                || monster.getId() == 8500002
                                || monster.getId() == 8510000
                                || monster.getId() == 8520000)
                                || monster.getId() == 9400549) {
                            int mesos = (int) ((Math.random() * (monster.getLevel() * 210 - monster.getLevel() * 70 + 1)) + monster.getLevel() * 70);
                            final int cc = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                            if (monster.isHT()) {
                                mesos *= 180;
                            }
                            final MapleMonster dropMonster = monster;
                            int dropTime;
                            final int dmesos = mesos;
                            if (monster.isHT()) {
                                dropTime = 500;
                            } else {
                                dropTime = monster.getAnimationTime("die1");
                            }
                            final MapleCharacter dropChar = dropOwner;
                            final boolean finalFfa = ffa;
                            TimerManager.getInstance().schedule(new Runnable() {

                                public void run() {
                                    spawnMesoDrop(dmesos * cc, dmesos, dropPos, dropMonster, dropChar, finalFfa);
                                }
                            }, dropTime);
                            droppedMesos++;
                        } else {
                            final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                            Random r = new Random();
                            double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                            if (mesoDecrease > 1.0) {
                                mesoDecrease = 1.0;
                            }
                            int tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp())
                                    * (1.0 + r.nextInt(20)) / 10.0));
                            if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                                tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                            }
                            if (monster.isBoss()) {
                                tempmeso *= 2;
                            }
                            if (monster.getId() == 9400509) {//Sakura Cellion
                                tempmeso *= 3;
                            }
                            //LMPQ monsters don't drop mesos
                            if (monster.getId() >= 9400209 && monster.getId() <= 9400218 || monster.getId() == 9300001 || monster.getId() == 9300094 || monster.getId() == 9300095) {
                                tempmeso = 0;
                            }

                            final int meso = tempmeso;
                            final boolean finalFfa = ffa;
                            if (meso > 0) {
                                final MapleMonster dropMonster = monster;
                                final MapleCharacter dropChar = dropOwner;
                                TimerManager.getInstance().schedule(new Runnable() {

                                    public void run() {
                                        spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, finalFfa);
                                    }
                                }, monster.getAnimationTime("die1"));
                            }
                            droppedMesos++;
                        }
                    }
                } else {
                    IItem idrop;
                    MapleInventoryType type = ii.getInventoryType(drop);
                    if (type.equals(MapleInventoryType.EQUIP)) {
                        Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop));
                        idrop = nEquip;
                    } else {
                        idrop = new Item(drop, (byte) 0, (short) 1);
                        // Randomize quantity for certain items
                        if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop)) {
                            idrop.setQuantity((short) (1 + 100 * Math.random()));
                        } else if (ii.isThrowingStar(drop) || ii.isBullet(drop)) {
                            idrop.setQuantity((short) (1));
                        }
                    }

                    if (monster.getId() == 9400218 && idrop.getItemId() == 4001106) {
                        double klm = Math.random();
                        if (klm < 0.7) // 7/10 chance that a Tauromacis will drop 50 tickets! :D
                        {
                            idrop.setQuantity((short) (50)); // 50 Tickets from a Tauromacis in LMPQ
                        }
                    }

                    StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                    logMsg.append(monster.getObjectId());
                    logMsg.append(" (");
                    logMsg.append(monster.getId());
                    logMsg.append(") at ");
                    logMsg.append(dropPos.toString());
                    logMsg.append(" on map ");
                    logMsg.append(mapid);
                    idrop.log(logMsg.toString(), false);

                    final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                    final MapleMapObject dropMonster = monster;
                    final MapleCharacter dropChar = dropOwner;
                    final TimerManager tMan = TimerManager.getInstance();
                    int dropTime = monster.getAnimationTime("die1");
                    if (monster.isHT()) {
                        dropTime = 500;
                    }
                    mdrop.setFfa(ffa);
                    tMan.schedule(new Runnable() {

                        public void run() {
                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

                                public void sendPackets(MapleClient c) {
                                    c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), isBoss ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                }
                            });

                            tMan.schedule(new ExpireMapItemJob(mdrop), 180000);
                        }
                    }, dropTime);
                    if (!ffa) {
                        tMan.schedule(new Runnable() {
                            public void run() {
                                mdrop.setFfa(true);
                            }
                        }, 15000);
                    }
                }
            }
        }
    }
    
        public int countReactorsOnMap() {
        int count = 0;
        Collection<MapleMapObject> mmos = this.getMapObjects();
        for (MapleMapObject mmo : mmos) 
            if (mmo instanceof MapleReactor) 
                count++;
        return count;
    }

    public int countMobOnMap() {
        int count = 0;
        Collection<MapleMapObject> mmos = this.getMapObjects();
        for (MapleMapObject mmo : mmos) 
            if (mmo instanceof MapleMonster)
                count++;
        return count;
    }

    public int countMobOnMap(int monsterid) {
        int count = 0;
        Collection<MapleMapObject> mmos = this.getMapObjects();
        for (MapleMapObject mmo : mmos) 
            if (mmo instanceof MapleMonster) {
                MapleMonster monster = (MapleMonster) mmo;
                if (monster.getId() == monsterid) 
                    count++;
            }
        return count;
    }
	
	public boolean getCardChance(int mobLvl, boolean boss) {
		/*if (mobLvl < 10) {
			return this.rand(0, 2000/(boss ? 2 : 1)) == 1;
		} else if (mobLvl > 10 && mobLvl < 30) {
			return this.rand(0, 1500/(boss ? 2 : 1)) == 1;
		} else if (mobLvl > 30 && mobLvl < 60) {
			return this.rand(0, 1000/(boss ? 2 : 1)) == 1;
		} else if (mobLvl > 60 && mobLvl < 100) {
			return this.rand(0, 700/(boss ? 2 : 1)) == 1;
		} else if (mobLvl > 100 && mobLvl < 120) {
			return this.rand(0, 500/(boss ? 2 : 1)) == 1;
		} else if (mobLvl > 120 && mobLvl < 150) {
			return this.rand(0, 300/(boss ? 2 : 1)) == 1;
		} else if (mobLvl > 150) {
			return this.rand(0, 300/(boss ? 2 : 1)) == 1;
		}*/
		if (500 - mobLvl/(boss ? 2 : 1) <= 0) return true;
		return this.rand(0, 500 - mobLvl/(boss ? 2 : 1)) == 1;
	}

//	private void dropFromMonsterOld(MapleCharacter dropOwner, MapleMonster monster) {
//		if (dropsDisabled || monster.dropsDisabled()) {
//			return;
//		}
//
//		switch (monster.getId()) {
//			case 6130104:
//			case 6230101:
//			case 6300003:
//			case 6300004:
//			case 6400004:
//			case 9300038:
//			case 9300040:
//			case 9300050:
//			case 9300062:
//			case 9300063:
//			case 9300064:
//			case 9300081:
//			case 9300082:
//			case 9300083:
//			case 9400520:
//			case 9500180:
//				return;
//		}
//
//		/*
//		 * drop logic: decide based on monster what the max drop count is get drops (not allowed: multiple mesos,
//		 * multiple items of same type exception: event drops) calculate positions
//		 */
//        int droppedThis = 0;
//		int droppedMesos = 0;
//		int maxDrops;
//		int maxMesos = 5;
//		int maxMesoDrop = 1;
//		final boolean isBoss = monster.isBoss();
//		final boolean isHornedTail = monster.getId() == 8810018;
//		boolean isGoodBoss = false;
//
//		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
//		ChannelServer cserv = dropOwner.getClient().getChannelServer();
//
//		if (isBoss) {
//			maxDrops = 6 * cserv.getBossDropRate();
//		} else {
//			maxDrops = 4 * cserv.getDropRate();
//		}
//        String text = "";
//        scripting.event.EventInstanceManager eim = dropOwner.getEventInstance();
//        if (eim != null) {
//            text = eim.getName();
//        }
//        if (text.startsWith("BossHunterPQ")) {
//            bossHunterDrops(monster, dropOwner);
//            return;
//        }
//        if (text.startsWith("AquaPQ")) {
//            if (monster.getId() == 8150100 || monster.getId() == 8150101 || monster.getId() == 8140600 || monster.getId() == 8141300 || monster.getId() == 8142100) {
//                aquaPQDrops(monster, dropOwner);
//                return;
//            } else if (monster.getId() == 8510000) {
//                return;
//            } else {
//                return;
//            }
//        }
//		
//		if (monster.getId() >= 9300127 && monster.getId() <= 9300136) {
//            monsterCarnivalDrops(dropOwner, monster);
//            return;
//		}
//		switch (monster.getId()) {
//			case 9300157:
//				maxMesoDrop = 0;
//				break;
//			case 8800002:
//			case 9400300:
//				maxDrops = 30; //Amount that Zak should Drop o.O
//				maxMesoDrop = 7;
//				isGoodBoss = true;
//				break;
//			case 8510000:
//			case 8520000:
//			case 8500002:
//			case 9400121:
//				maxDrops = 20;
//				maxMesoDrop = 5;
//				isGoodBoss = true;
//				break;
//			case 94000014:
//				maxDrops = 14;
//				isGoodBoss = true;
//				maxMesoDrop = 4;
//				break;
//			case 8180000:
//			case 8180001:
//			case 9400549:
//				maxDrops = 6;
//				isGoodBoss = true;
//				maxMesoDrop = 2;
//				break;
//			case 8810018:
//				maxDrops = 70;
//				maxMesoDrop = 9;
//				isGoodBoss = true;
//				break;
//			case 9400514:
//			case 9300010:
//			case 9300012:
//			case 9300028:
//			case 9300039:
//			case 9300058:
//				maxDrops = 1;
//				break;
//			case 9300059:
//			case 9300095:
//			case 9300094:
//				maxDrops = 2;
//				break;
//			case 9300060:
//				maxDrops = 2 + (int) (Math.floor(Math.random() * 3));
//				break;
//			case 9400202:
//				maxDrops = 10;
//				break;
//		}
//
//		List<Integer> toDrop = new ArrayList<Integer>();
//		int droppedMezar = 0;
//		if (isHornedTail || monster.getId() == 8800002) {
//			for (int i = 0; i < maxDrops; i++) {
//				int theDrop = monster.getDrop();
//				if (theDrop == -1 && droppedMezar < 7) {
//					toDrop.add(theDrop);
//					droppedMezar++;
//				} else if (theDrop == -1 && droppedMezar >= 7) {
//					i--;
//					continue;
//				} else if (theDrop == 4001094 && countDrops(toDrop, 4001094) >= 4) { //0xFF, 0xBC T_T
//					i--;
//					continue;
//				} else if (theDrop == 5220001 && countDrops(toDrop, 5220001) >= 4) { //0xFA, 0xAD T_T
//					i--;
//					continue;
//				} else if (theDrop == 1122000 && toDrop.contains(theDrop)) {
//					i--;
//					continue;
//				} else {
//					toDrop.add(theDrop);
//				}
//			}
//		} else if (isGoodBoss) {
//			for (int i = 0; i < maxDrops; i++) {
//				int toAdd = monster.getDrop();
//                        if (toAdd == 4000141 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4000175 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031253 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031196 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4000224 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4000235 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4001076 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4000243 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031457 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031903 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4000138 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031901 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031901 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031902 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031903 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031904 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031905 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == 4031906 && toDrop.contains(toAdd)) {
//                            i--;
//                            continue;
//                        }else if (toAdd == -1 && droppedMezar < maxMesoDrop) {
//                                toDrop.add(toAdd);
//                                droppedMezar++;
//                        }else if (toAdd == 2000005 && countDrops(toDrop, 2000005) >= 4){ //0xFF, 0xBC T_T
//                                i--;
//                                continue;
//                        }else if (toAdd == 2000006 && countDrops(toDrop, 2000006) >= 4){ //0xFA, 0xAD T_T
//                                i--;
//                                continue;
//                        }else if (toAdd == -1 && droppedMezar >= maxMesoDrop){
//                            i--;
//                            continue;
//                        }else {
//                            toDrop.add(toAdd);
//                        }
//				}
//			}
//		} else {
//			for (int i = 0; i < maxDrops; i++) {
//				toDrop.add(monster.getDrop());
//			}
//		}
//
//		Set<Integer> alreadyDropped = new HashSet<Integer>();
//		int zhelms = 0;
//		for (int i = 0; i < toDrop.size(); i++) {
//			if (alreadyDropped.contains(toDrop.get(i)) && isGoodBoss == false) {
//				toDrop.remove(i);
//				i--;
//
//			} else if (toDrop.get(i) == 1002357) {
//				if (zhelms > 1) {
//					toDrop.set(i, -1);
//				} else {
//					zhelms++;
//				}
//			} else {
//
//				alreadyDropped.add(toDrop.get(i));
//			}
//		}
//
//		if (toDrop.size() > maxDrops) {
//			toDrop = toDrop.subList(0, maxDrops);
//		}
//		int thing = 1;
//		for (int k = 0; k < maxDrops; k++) {
//			thing = monster.getQuestDrop(dropOwner);
//			if (thing != 1) {
//				break;
//			}
//		}
//		if (thing != 1) { //Quest Drops
//			toDrop.add(thing);
//		}
//
//		Collections.shuffle(toDrop);
//		Point[] toPoint = new Point[toDrop.size()];
//		int shiftDirection = 0;
//		int shiftCount = 0;
//
//		int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
//			footholds.getMaxDropX() - toDrop.size() * 25);
//		int curY = Math.max(monster.getPosition().y, footholds.getY1());
//		while (shiftDirection < 3 && shiftCount < 1000) {
//			// TODO for real center drop the monster width is needed o.o"
//			if (shiftDirection == 1) {
//				curX += 25;
//			} else if (shiftDirection == 2) {
//				curX -= 25;
//			}
//			// now do it
//			for (int i = 0; i < toDrop.size(); i++) {
//				MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
//				if (wall != null) {
//					//System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
//					if (wall.getX1() < curX) {
//						shiftDirection = 1;
//						shiftCount++;
//						break;
//					} else if (wall.getX1() == curX) {
//						if (shiftDirection == 0) {
//							shiftDirection = 1;
//						}
//						shiftCount++;
//						break;
//					} else {
//						shiftDirection = 2;
//						shiftCount++;
//						break;
//					}
//				} else if (i == toDrop.size() - 1) {
//					//System.out.println("ok " + curX);
//					shiftDirection = 3;
//				}
//				final Point dropPos;
//				if (isHornedTail) {
//					dropPos = calcDropPos(new Point(curX + i * 25, curY - 100), new Point(monster.getPosition()));
//					toPoint[i] = new Point(curX + i * 25, curY - 100);
//				} else {
//					dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
//					toPoint[i] = new Point(curX + i * 25, curY);
//				}
//				final int drop = toDrop.get(i);
//
//				if (drop == -1) { // meso
//					if ((monster.getId() == 8810018 || monster.getId() == 8800002 || monster.getId() == 9400300 || monster.getId() == 9400121 || monster.getId() == 9400014 || monster.getId() == 8500002 || monster.getId() == 8510000 || monster.getId() == 8520000) && droppedThis < 7) {
//						int mesos = (int) ((Math.random() * (monster.getLevel() * 210 - monster.getLevel() * 70 + 1)) + monster.getLevel() * 70);
//						final int cc = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
//						if (isHornedTail) {
//							mesos *= 180;
//						}
//						final MapleMonster dropMonster = monster;
//						int dropTime;
//						final int dmesos = mesos;
//						droppedThis++;
//						if (isHornedTail) {
//							dropTime = 500;
//						} else {
//							dropTime = monster.getAnimationTime("die1");
//						}
//						final MapleCharacter dropChar = dropOwner;
//						TimerManager.getInstance().schedule(new Runnable() {
//
//							public void run() {
//								spawnMesoDrop(dmesos * cc, dmesos, dropPos, dropMonster, dropChar, isBoss);
//							}
//						}, dropTime);
//					} else if (droppedMesos < maxMesos || isGoodBoss) {
//						final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
//						Random r = new Random();
//						double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
//						if (mesoDecrease > 1.0) {
//							mesoDecrease = 1.0;
//						}
//						int tempmeso = 0;
//						tempmeso = Math.min(100000, (int) (mesoDecrease * (monster.getExp()) * (1.0 + r.nextInt(20)) / 10.0));
//						if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
//							tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
//						}
//						if (monster.isBoss()) {
//							tempmeso *= 2;
//						}
//						if (monster.getId() == 9400509) {//Sakura Cellion
//							tempmeso *= 3;
//						}
//						//LMPQ monsters don't drop mesos
//						if (monster.getId() >= 9400209 && monster.getId() <= 9400218 || monster.getId() == 9300001 || monster.getId() == 9300094 || monster.getId() == 9300095) {
//							tempmeso = 0;
//						}
//						final int meso = tempmeso;
//						droppedMesos += 1;
//						int dropTime;
//						if (isHornedTail) {
//							dropTime = 500;
//						} else {
//							dropTime = monster.getAnimationTime("die1");
//						}
//
//						if (meso > 0) {
//							final MapleMonster dropMonster = monster;
//							final MapleCharacter dropChar = dropOwner;
//							TimerManager.getInstance().schedule(new Runnable() {
//
//								public void run() {
//									spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, isBoss);
//								}
//							}, dropTime);
//						}
//					}
//				} else {
//					IItem idrop;
//					MapleInventoryType type = ii.getInventoryType(drop);
//					if (type.equals(MapleInventoryType.EQUIP)) {
//						Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop));
//						idrop = nEquip;
//					} else {
//						idrop = new Item(drop, (byte) 0, (short) 1);
//						// Randomize quantity for certain items
//						if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop)) {
//							idrop.setQuantity((short) (1 + 100 * Math.random()));
//						} else if (ii.isThrowingStar(drop) || ii.isBullet(drop)) {
//							idrop.setQuantity((short) (1));
//						}
//						if (monster.getId() == 9400218 && idrop.getItemId() == 4001106) {
//							double klm = Math.random();
//							if (klm < 0.7) // 7/10 chance that a Tauromacis will drop 50 tickets! :D
//							{
//								idrop.setQuantity((short) (50)); // 50 Tickets from a Tauromacis in LMPQ
//							}
//						}
//					}
//					idrop.log("Created as a drop from monster " + monster.getObjectId() + " (" + monster.getId() + ") at " + dropPos.toString() + " on map " + mapid, false);
//
//					final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
//					final MapleMapObject dropMonster = monster;
//					final MapleCharacter dropChar = dropOwner;
//					final TimerManager tMan = TimerManager.getInstance();
//					int dropTime;
//					if (isHornedTail) {
//						dropTime = 500;
//					} else {
//						dropTime = monster.getAnimationTime("die1");
//					}
//					tMan.schedule(new Runnable() {
//
//						public void run() {
//							spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
//
//								public void sendPackets(MapleClient c) {
//									c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), isBoss ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
//								}
//							}, null);
//
//							tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
//						}
//					}, dropTime);
//					activateItemReactors(mdrop);
//				}
//			}
//		}
//	}
//
//    private void lordPirateDrops(final MapleCharacter dropOwner, final MapleMonster monster) {
//        ChannelServer cserv = dropOwner.getClient().getChannelServer();
//        int randd;
//        int toAdd;
//        int times;
//        int dropArray[] = {2000005, 2000006, 2002022, 2100000, 2070006, 2022179, 2022178, 2002025, -1, 2020013, 2020014, 2020015, -1, -1, -1, -1, -1 };
//        int dropAmountz[] = {100, 200, 100, 1, 1, 5, 50, 100, 1, 50, 100, 150, 1, 1, 1, 1, 1};
//        times = (int) (5 + Math.floor(Math.random() * 20));
//        List<Integer> toDrop = new ArrayList<Integer>();
//        List<Integer> amountDrop = new ArrayList<Integer>();
//		for (int i = 0; i <times; i++) {
//            randd = (int) (Math.floor(Math.random() * (dropArray.length)));
//            toAdd = dropArray[randd];
//			toDrop.add(toAdd);
//            amountDrop.add(dropAmountz[randd]);
//		}
//        final int mesoRate = cserv.getMesoRate();
//
//		if (toDrop.size() > times) {
//			toDrop = toDrop.subList(0, times);
//		}
//		Point[] toPoint = new Point[toDrop.size()];
//		int shiftDirection = 0;
//		int shiftCount = 0;
//        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
//
//		int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25), footholds.getMaxDropX() - toDrop.size() * 25);
//		int curY = Math.max(monster.getPosition().y, footholds.getY1());
//		while (shiftDirection < 3 && shiftCount < 1000) {
//			if (shiftDirection == 1) {
//				curX += 25;
//            } else if (shiftDirection == 2) {
//				curX -= 25;
//            }
//			for (int i = 0; i < times; i++) {
//				MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
//				if (wall != null) {
//					if (wall.getX1() < curX) {
//						shiftDirection = 1;
//						shiftCount++;
//						break;
//					} else if (wall.getX1() == curX) {
//						if (shiftDirection == 0)
//							shiftDirection = 1;
//						shiftCount++;
//						break;
//					} else {
//						shiftDirection = 2;
//						shiftCount++;
//						break;
//					}
//				} else if (i == toDrop.size() - 1) {
//					shiftDirection = 3;
//				}
//				final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
//				toPoint[i] = new Point(curX + i * 25, curY);
//				final int drop = toDrop.get(i);
//                final int dropAmounti = amountDrop.get(i);
//                final short dropAmountReal = (short) (dropAmounti);
//                int tempmeso;
//				if (drop == -1) { // [MESOS]
//					Random r = new Random();
//					double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
//					if (mesoDecrease > 1.0) {
//						mesoDecrease = 1.0;
//					}
//					tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) * (1.0 + r.nextInt(20)) / 10.0));
//                    } else
//                        tempmeso = 0;
//					if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
//						tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
//					}
//					final int meso = tempmeso;
//
//					if (meso > 0) {
//						final MapleMonster dropMonster = monster;
//						final MapleCharacter dropChar = dropOwner;
//						TimerManager.getInstance().schedule(new Runnable() {
//							public void run() {
//								spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
//							}
//						}, monster.getAnimationTime("die1"));
//					} else {
//                        IItem idrop;
//                        MapleInventoryType type = ii.getInventoryType(drop);
//                        if (type.equals(MapleInventoryType.EQUIP)) {
//                            Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop));
//                            idrop = nEquip;
//                        } else {
//                            idrop = new Item(drop, (byte) 0, (short) 1);
//                            if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop))
//                                idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
//                                idrop.setQuantity(dropAmountReal);
//                            }
//
//                            StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
//                            logMsg.append(monster.getObjectId());
//                            logMsg.append(" (");
//                            logMsg.append(monster.getId());
//                            logMsg.append(") at ");
//                            logMsg.append(dropPos.toString());
//                            logMsg.append(" on map ");
//                            logMsg.append(mapid);
//                            idrop.log(logMsg.toString(),false);
//
//                            final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
//                            final MapleMapObject dropMonster = monster;
//                            final MapleCharacter dropChar = dropOwner;
//                            final TimerManager tMan = TimerManager.getInstance();
//                            final MapleClient c;
//
//                            tMan.schedule(new Runnable() {
//                                public void run() {
//                                    spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
//                                        public void sendPackets(MapleClient c) {
//                                            c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
//                                        }
//                                    });
//                                    tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
//                                }
//                            }, monster.getAnimationTime("die1"));
//                }
//            }
//        }
//    }

    private void fakeBossDrops(final MapleCharacter dropOwner, final MapleMonster monster) {
                //double rand;
                ChannelServer cserv = dropOwner.getClient().getChannelServer();
                int randd;
                int toAdd;
                int times;
                int dropArray[] = {2020016, 2020017, 2020018, 2020019, 2020020, 2000002, 2000006 }; //These are the drops, -1 means meso :D
                times = (int) (2 + Math.floor(Math.random() * 2));
                List<Integer> toDrop = new ArrayList<Integer>();
                toDrop.add(-1); //First add a bag of mezars
		for (int i = 0; i <times; i++) {
                        randd = (int) (Math.floor(Math.random() * (dropArray.length)));
                        toAdd = dropArray[randd];
                        if (toDrop.contains(toAdd) == false)
			toDrop.add(toAdd);
                        else
                        i --;
		}
                final int mesoRate = cserv.getMesoRate();

		if (toDrop.size() > times) {
			toDrop = toDrop.subList(0, times);
		}
		Point[] toPoint = new Point[toDrop.size()];
		int shiftDirection = 0;
		int shiftCount = 0;
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

		int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
			footholds.getMaxDropX() - toDrop.size() * 25);
		int curY = Math.max(monster.getPosition().y, footholds.getY1());
		//int monsterShift = curX -
		while (shiftDirection < 3 && shiftCount < 1000) {
			// TODO for real center drop the monster width is needed o.o"
			if (shiftDirection == 1)
				curX += 25;
			else if (shiftDirection == 2)
				curX -= 25;
			// now do it
			for (int i = 0; i < times; i++) {
				MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
				if (wall != null) {
					//System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
					if (wall.getX1() < curX) {
						shiftDirection = 1;
						shiftCount++;
						break;
					} else if (wall.getX1() == curX) {
						if (shiftDirection == 0)
							shiftDirection = 1;
						shiftCount++;
						break;
					} else {
						shiftDirection = 2;
						shiftCount++;
						break;
					}
				} else if (i == toDrop.size() - 1) {
					//System.out.println("ok " + curX);
					shiftDirection = 3;
				}
				final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
				toPoint[i] = new Point(curX + i * 25, curY);
				final int drop = toDrop.get(i);
                                //final int dropAmounti = amountDrop.get(i);
                                //final short dropAmountReal = (short) (dropAmounti);
                                int tempmeso;
				if (drop == -1) { // meso
					//final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
					Random r = new Random();
					double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
					if (mesoDecrease > 1.0) {
						mesoDecrease = 1.0;
					}
					tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
						(1.0 + r.nextInt(20)) / 10.0));
                                        } else tempmeso = 0;
					if(dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
						tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
					}
					final int meso = tempmeso * 2;

					if (meso > 0) {
						final MapleMonster dropMonster = monster;
						final MapleCharacter dropChar = dropOwner;
						TimerManager.getInstance().schedule(new Runnable() {
							public void run() {
								spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
							}
						}, monster.getAnimationTime("die1"));
					}
				 else {
                                            IItem idrop;
                                            MapleInventoryType type = ii.getInventoryType(drop);
                                            if (type.equals(MapleInventoryType.EQUIP)) {
                                                    Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), monster.getLevel() / 10);
                                                    idrop = nEquip;
                                            } else {
                                                    idrop = new Item(drop, (byte) 0, (short) 1);
                                                    // Randomize quantity for certain items
                                                    if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop))
                                                            idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                                            }

					StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
					logMsg.append(monster.getObjectId());
					logMsg.append(" (");
					logMsg.append(monster.getId());
					logMsg.append(") at ");
					logMsg.append(dropPos.toString());
					logMsg.append(" on map ");
					logMsg.append(mapid);
					idrop.log(logMsg.toString(),false);

					final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
					final MapleMapObject dropMonster = monster;
					final MapleCharacter dropChar = dropOwner;
					final TimerManager tMan = TimerManager.getInstance();
                                        final MapleClient c;

					tMan.schedule(new Runnable() {
						public void run() {
							spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
								public void sendPackets(MapleClient c) {
									c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster
										.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
								}
							});

							tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
						}
					}, monster.getAnimationTime("die1"));

				}
			}
		}
        }

        private void bossHunterDrops(final MapleMonster monster, final MapleCharacter dropOwner) {
                //double rand;
                ChannelServer cserv = dropOwner.getClient().getChannelServer();
                int randd;
                int toAdd;
                int times = 1;
                int dropArray[] = {1002739, 1002740, 1002749, 1002750, 1052148, 1052149, 1072342, 1072343, 1072345, 1072346};
                List<Integer> toDrop = new ArrayList<Integer>();
                if (Math.random() * 3000 > monster.getLevel()) {
                    return;
                }
                randd = (int) (Math.floor(Math.random() * (dropArray.length)));
                toAdd = dropArray[randd];
                toDrop.add(toAdd);
                final int mesoRate = cserv.getMesoRate();
		//Set<Integer> alreadyDropped = new HashSet<Integer>();

		Point[] toPoint = new Point[toDrop.size()];
		int shiftDirection = 0;
		int shiftCount = 0;
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

		int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
			footholds.getMaxDropX() - toDrop.size() * 25);
		int curY = Math.max(monster.getPosition().y, footholds.getY1());
		//int monsterShift = curX -
                try {
                    while (shiftDirection < 3 && shiftCount < 1000) {
                            // TODO for real center drop the monster width is needed o.o"
                            if (shiftDirection == 1)
                                    curX += 25;
                            else if (shiftDirection == 2)
                                    curX -= 25;
                            // now do it
                            for (int i = 0; i < times; i++) {
                                    MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                                    if (wall != null) {
                                            //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                                            if (wall.getX1() < curX) {
                                                    shiftDirection = 1;
                                                    shiftCount++;
                                                    break;
                                            } else if (wall.getX1() == curX) {
                                                    if (shiftDirection == 0)
                                                            shiftDirection = 1;
                                                    shiftCount++;
                                                    break;
                                            } else {
                                                    shiftDirection = 2;
                                                    shiftCount++;
                                                    break;
                                            }
                                    } else if (i == toDrop.size() - 1) {
                                            //System.out.println("ok " + curX);
                                            shiftDirection = 3;
                                    }
                                    final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                                    toPoint[i] = new Point(curX + i * 25, curY);
                                    final int drop = toDrop.get(i);
                                    int tempmeso;
                                    if (drop == -1) { // meso
                                            //final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                                            Random r = new Random();
                                            double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                                            if (mesoDecrease > 1.0) {
                                                    mesoDecrease = 1.0;
                                            }
                                            tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                                                    (1.0 + r.nextInt(20)) / 10.0));
                                            } else tempmeso = 0;
                                            if(dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                                                    tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                                            }
                                            final int meso = tempmeso;

                                            if (meso > 0) {
                                                    final MapleMonster dropMonster = monster;
                                                    final MapleCharacter dropChar = dropOwner;
                                                    TimerManager.getInstance().schedule(new Runnable() {
                                                            public void run() {
                                                                    spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
                                                            }
                                                    }, monster.getAnimationTime("die1"));
                                            }
                                     else {
                                            IItem idrop;
                                            MapleInventoryType type = ii.getInventoryType(drop);
                                            if (type.equals(MapleInventoryType.EQUIP)) {
                                                    Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), monster.getLevel() / 10);
                                                    idrop = nEquip;
                                            } else {
                                                    idrop = new Item(drop, (byte) 0, (short) 1);
                                                    // Randomize quantity for certain items
                                                    if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop))
                                                            idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                                            }

                                            StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                                            logMsg.append(monster.getObjectId());
                                            logMsg.append(" (");
                                            logMsg.append(monster.getId());
                                            logMsg.append(") at ");
                                            logMsg.append(dropPos.toString());
                                            logMsg.append(" on map ");
                                            logMsg.append(mapid);
                                            idrop.log(logMsg.toString(),false);

                                            final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                                            final MapleMapObject dropMonster = monster;
                                            final MapleCharacter dropChar = dropOwner;
                                            final TimerManager tMan = TimerManager.getInstance();
                                            final MapleClient c;

                                            tMan.schedule(new Runnable() {
                                                    public void run() {
                                                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
                                                                    public void sendPackets(MapleClient c) {
                                                                            c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster
                                                                                    .getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                                                    }
                                                            });

                                                            tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                                                    }
                                            }, monster.getAnimationTime("die1"));
                                            //activateItemReactors(mdrop); -- No we dont need to activate reactors... =.="

                                    }
                            }
                    }
                } catch (IndexOutOfBoundsException e) {
                        return;
                }
        }
        	
        public void setMonsterRate(float monsterRate) {
		this.monsterRate = monsterRate;
	}
        
        


        private void aquaPQDrops(final MapleMonster monster, final MapleCharacter dropOwner) {
                //double rand;
                ChannelServer cserv = dropOwner.getClient().getChannelServer();
                //int randd;
                int toAdd;
                int times = 1;
                //int dropArray[] = {1002739, 1002740, 1002749, 1002750, 1052148, 1052149, 1072342, 1072343, 1072345, 1072346};
                // dropArray[] = {4001022};
                List<Integer> toDrop = new ArrayList<Integer>();
//                if (Math.random() * 3000 > monster.getLevel()) {
//                    return;
//                }
                //randd = (int) (Math.floor(Math.random() * (dropArray.length)));
                toAdd = 4001022;
                toDrop.add(toAdd);
                final int mesoRate = cserv.getMesoRate();
		//Set<Integer> alreadyDropped = new HashSet<Integer>();

		Point[] toPoint = new Point[toDrop.size()];
		int shiftDirection = 0;
		int shiftCount = 0;
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

		int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25),
			footholds.getMaxDropX() - toDrop.size() * 25);
		int curY = Math.max(monster.getPosition().y, footholds.getY1());
		//int monsterShift = curX -
                try {
                    while (shiftDirection < 3 && shiftCount < 1000) {
                            // TODO for real center drop the monster width is needed o.o"
                            if (shiftDirection == 1)
                                    curX += 25;
                            else if (shiftDirection == 2)
                                    curX -= 25;
                            // now do it
                            for (int i = 0; i < times; i++) {
                                    MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
                                    if (wall != null) {
                                            //System.out.println("found a wall. wallX " + wall.getX1() + " curX " + curX);
                                            if (wall.getX1() < curX) {
                                                    shiftDirection = 1;
                                                    shiftCount++;
                                                    break;
                                            } else if (wall.getX1() == curX) {
                                                    if (shiftDirection == 0)
                                                            shiftDirection = 1;
                                                    shiftCount++;
                                                    break;
                                            } else {
                                                    shiftDirection = 2;
                                                    shiftCount++;
                                                    break;
                                            }
                                    } else if (i == toDrop.size() - 1) {
                                            //System.out.println("ok " + curX);
                                            shiftDirection = 3;
                                    }
                                    final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
                                    toPoint[i] = new Point(curX + i * 25, curY);
                                    final int drop = toDrop.get(i);
                                    int tempmeso;
                                    if (drop == -1) { // meso
                                            //final int mesoRate = ChannelServer.getInstance(dropOwner.getClient().getChannel()).getMesoRate();
                                            Random r = new Random();
                                            double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
                                            if (mesoDecrease > 1.0) {
                                                    mesoDecrease = 1.0;
                                            }
                                            tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
                                                    (1.0 + r.nextInt(20)) / 10.0));
                                            } else tempmeso = 0;
                                            if(dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
                                                    tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
                                            }
                                            final int meso = tempmeso;

                                            if (meso > 0) {
                                                    final MapleMonster dropMonster = monster;
                                                    final MapleCharacter dropChar = dropOwner;
                                                    TimerManager.getInstance().schedule(new Runnable() {
                                                            public void run() {
                                                                    spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
                                                            }
                                                    }, monster.getAnimationTime("die1"));
                                            }
                                     else {
                                            IItem idrop;
                                            MapleInventoryType type = ii.getInventoryType(drop);
                                            if (type.equals(MapleInventoryType.EQUIP)) {
                                                    Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), monster.getLevel() / 10);
                                                    idrop = nEquip;
                                            } else {
                                                    idrop = new Item(drop, (byte) 0, (short) 1);
                                                    // Randomize quantity for certain items
                                                    if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop))
                                                            idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                                            }

                                            StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                                            logMsg.append(monster.getObjectId());
                                            logMsg.append(" (");
                                            logMsg.append(monster.getId());
                                            logMsg.append(") at ");
                                            logMsg.append(dropPos.toString());
                                            logMsg.append(" on map ");
                                            logMsg.append(mapid);
                                            idrop.log(logMsg.toString(),false);

                                            final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                                            final MapleMapObject dropMonster = monster;
                                            final MapleCharacter dropChar = dropOwner;
                                            final TimerManager tMan = TimerManager.getInstance();
                                            final MapleClient c;

                                            tMan.schedule(new Runnable() {
                                                    public void run() {
                                                            spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
                                                                    public void sendPackets(MapleClient c) {
                                                                            c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster
                                                                                    .getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                                                    }
                                                            });

                                                            tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                                                    }
                                            }, monster.getAnimationTime("die1"));
                                            //activateItemReactors(mdrop); -- No we dont need to activate reactors... =.="

                                    }
                            }
                    }
                } catch (IndexOutOfBoundsException e) {
                        return;
                }
        }
		
	public Collection<MapleCharacter> getNearestPvpChar(Point attacker, double maxRange, double maxHeight, Collection<MapleCharacter> chr) {
			Collection<MapleCharacter> character = new LinkedList<MapleCharacter>();
			for (MapleCharacter a : characters) {
					if (chr.contains(a.getClient().getPlayer())) {
							Point attackedPlayer = a.getPosition();
							//MaplePortal Port = a.getMap().findClosestSpawnpoint(a.getPosition());
							//Point nearestPort = Port.getPosition();
							//double safeDis = attackedPlayer.distance(nearestPort);
							double distanceX = attacker.distance(attackedPlayer.getX(), attackedPlayer.getY());
							if(MaplePvp.isLeft) {
									if (attacker.x > attackedPlayer.x && distanceX < maxRange && distanceX > 2 && attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight) {
											character.add(a);
									}
							}
							if(MaplePvp.isRight) {
									if (attacker.x < attackedPlayer.x && distanceX < maxRange && distanceX > 2 && attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight) {
											character.add(a);
									}
							}
					}
			}
			return character;
	}
        
    public void killAllMonsters() {
        for (MapleMapObject monstermo : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
        }
    }
    
   
   
   
       public void warpMap(MapleMap map) {
        synchronized (characters) {
            for (MapleCharacter chr : this.characters) {
                if (chr.isAlive()) {
                    chr.changeMap(map, map.getPortal(0));
                } else {
                    chr.changeMap(chr.getMap().getReturnMap(), map.getPortal(0));
                }
            }
        }
    }
       
    public void spawnMonsterOnGroudBelow(MapleMonster mob, Point pos) {
        spawnMonsterOnGroundBelow(mob, pos);
    }

    public void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = getGroundBelow(pos);
        mob.setPosition(spos);
        spawnMonster(mob);
    }
    
    public void spawnMonsterOnGroundBelow(int mobid, int x, int y) {
        MapleMonster mob = MapleLifeFactory.getMonster(mobid);
        if (mob != null) {
            Point point = new Point(x, y);
            spawnMonsterOnGroundBelow(mob, point);
        }
    }
    
   public Point getGroundBelow(Point pos) {
        Point spos = new Point(pos.x, pos.y - 1);
        spos = calcPointBelow(spos);
        spos.y -= 1;
        return spos;
    }
       
    /* Original da source - erros ;s
   public int spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
		Point spos = new Point(pos.x, pos.y - 1);
		spos = calcPointBelow(spos);
		spos.y -= 1;
		mob.setPosition(spos);
		spawnMonster(mob);
		return mob.getObjectId();
	}
   */
    private void superPianusDrops(final MapleCharacter dropOwner, final MapleMonster monster) {
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int randd;
        int toAdd;
        int times;
        int dropArray[] = {1082223, 1072344, 1102041, 1102042, 1082230, 1122000, 1002357, 1012070, 1012071, 1012072, 1050018, 1051017};
        times = (int) (5 + Math.floor(Math.random() * 40));
        List<Integer> toDrop = new ArrayList<Integer>();
		for (int i = 0; i <times; i++) {
            randd = (int) (Math.floor(Math.random() * (dropArray.length)));
            toAdd = dropArray[randd];
			toDrop.add(toAdd);
		}
       final int mesoRate = cserv.getMesoRate();

		if (toDrop.size() > times) {
			toDrop = toDrop.subList(0, times);
		}
		Point[] toPoint = new Point[toDrop.size()];
		int shiftDirection = 0;
		int shiftCount = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

		int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25), footholds.getMaxDropX() - toDrop.size() * 25);
		int curY = Math.max(monster.getPosition().y, footholds.getY1());
		while (shiftDirection < 3 && shiftCount < 1000) {
			if (shiftDirection == 1)
				curX += 25;
			else if (shiftDirection == 2)
				curX -= 25;
			for (int i = 0; i < times; i++) {
				MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
				if (wall != null) {
					if (wall.getX1() < curX) {
						shiftDirection = 1;
						shiftCount++;
						break;
					} else if (wall.getX1() == curX) {
						if (shiftDirection == 0)
							shiftDirection = 1;
						shiftCount++;
						break;
					} else {
						shiftDirection = 2;
						shiftCount++;
						break;
					}
				} else if (i == toDrop.size() - 1) {
					//System.out.println("ok " + curX);
					shiftDirection = 3;
				}
				final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
				toPoint[i] = new Point(curX + i * 25, curY);
				final int drop = toDrop.get(i);
                int tempmeso;
				if (drop == -1) { // meso
					Random r = new Random();
					double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
					if (mesoDecrease > 1.0) {
						mesoDecrease = 1.0;
					}
					tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) *
						(1.0 + r.nextInt(20)) / 10.0));
                                        } else tempmeso = 0;
					if(dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
						tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
					}
					final int meso = tempmeso;

					if (meso > 0) {
						final MapleMonster dropMonster = monster;
						final MapleCharacter dropChar = dropOwner;
						TimerManager.getInstance().schedule(new Runnable() {
							public void run() {
								spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
							}
						}, monster.getAnimationTime("die1"));
					}
				 else {
					IItem idrop;
					MapleInventoryType type = ii.getInventoryType(drop);
					if (type.equals(MapleInventoryType.EQUIP)) {
						Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop), 10);
						idrop = nEquip;
					} else {
						idrop = new Item(drop, (byte) 0, (short) 1);
						// Randomize quantity for certain items
						if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop))
							idrop.setQuantity((short) (1 + ii.getSlotMax(drop) * Math.random()));
                                                //idrop.setQuantity(dropAmountReal); //Set the quantity! w00t!
					}

					StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
					logMsg.append(monster.getObjectId());
					logMsg.append(" (");
					logMsg.append(monster.getId());
					logMsg.append(") at ");
					logMsg.append(dropPos.toString());
					logMsg.append(" on map ");
					logMsg.append(mapid);
					idrop.log(logMsg.toString(),false);

					final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
					final MapleMapObject dropMonster = monster;
					final MapleCharacter dropChar = dropOwner;
					final TimerManager tMan = TimerManager.getInstance();
                                        final MapleClient c;

					tMan.schedule(new Runnable() {
						public void run() {
							spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
								public void sendPackets(MapleClient c) {
									c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster
										.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
								}
							});

							tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
						}
					}, monster.getAnimationTime("die1"));
				}
			}
		}
    }


	public boolean damageMonster(MapleCharacter chr, MapleMonster monster, int damage) {
		if (monster.getId() == 8800000) {
			Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
			for (MapleMapObject object : objects) {
				MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
				if (mons != null && mons.getId() >= 8800003 && mons.getId() <= 8800010) {
					return true;
				}
			}
		}

		// double checking to potentially avoid synchronisation overhead
		if (monster.isAlive()) {
			boolean killMonster = false;

			synchronized (monster) {
				if (!monster.isAlive()) {
					return false;
				}
				if (damage > 0) {
					int monsterhp = monster.getHp();
					monster.damage(chr, damage, true);
					if (!monster.isAlive()) { // monster just died
						killMonster(monster, chr, true);
						if (monster != null && monster.getId() >= 8810002 && monster.getId() <= 8810009) {
							Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
							for (MapleMapObject object : objects) {
								MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
								if (mons != null) {
									if (mons.getId() == 8810018) {
										damageMonster(chr, mons, monsterhp);
									}
								}
							}
						}
					} else {
						if (monster != null && monster.getId() >= 8810002 && monster.getId() <= 8810009) {
							Collection<MapleMapObject> objects = chr.getMap().getMapObjects();
							for (MapleMapObject object : objects) {
								MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
								if (mons != null) {
									if (mons.getId() == 8810018) {
										damageMonster(chr, mons, damage);
									}
								}
							}
						}
					}
				}
			}
			// the monster is dead, as damageMonster returns immediately for dead monsters this makes
			// this block implicitly synchronized for ONE monster
			if (killMonster) {
				killMonster(monster, chr, true);
			}
			return true;
		}
		return false;
	}

	public boolean damageBunny(MapleMonster attacker, MapleMonster monster, int damage) {
		if (monster.isAlive()) {
			boolean killMonster = false;

			synchronized (monster) {
				if (!monster.isAlive())
					return false;
				if (damage > 0) {
					if (!monster.isAlive()) { // monster just died
						killAllMonsters(false);
                    }
                    monster.setHp(monster.getHp() - attacker.getLevel() * 40);
                    if (monster.getHp() < 0) {
                        monster.setHp(0);
                    }
                    this.broadcastMessage(MaplePacketCreator.serverNotice(5, "[Notice] The Moon Bunny was Damaged. Moon Bunny now has " + monster.getHp() + "/" + monster.getMaxHp() + " HP."));
                }
            }
			if (monster.getHp() < 1) {
				killAllMonsters(false);
                this.broadcastMessage(MaplePacketCreator.serverNotice(5, "[Notice] You have failed the quest."));
                MapleMap outMap;
                MapleMapFactory mapFactory;
                for (MapleCharacter aaa : characters) {
                    mapFactory = ChannelServer.getInstance(aaa.getClient().getChannel()).getMapFactory();
                    outMap = mapFactory.getMap(910010300);
                    aaa.getClient().getPlayer().changeMap(outMap, outMap.getPortal(0));
                    aaa.getClient().getPlayer().getEventInstance().unregisterPlayer(aaa.getClient().getPlayer()); //Unregister them all
                }
			}
			return true;
		}
		return false;
	}

    public void quitHPQ() {
        MapleMap outMap;
        MapleMapFactory mapFactory;
        for (MapleCharacter aaa : characters) {
            mapFactory = ChannelServer.getInstance(aaa.getClient().getChannel()).getMapFactory();
            outMap = mapFactory.getMap(910010300);
            aaa.getClient().getPlayer().changeMap(outMap, outMap.getPortal(0));
            aaa.getClient().getPlayer().getEventInstance().unregisterPlayer(aaa.getClient().getPlayer()); //Unregister them all
        }
    }
   

    public void quitEPQ() {
        MapleMap outMap;
        MapleMapFactory mapFactory;
        for (MapleCharacter aaa : characters) {
            mapFactory = ChannelServer.getInstance(aaa.getClient().getChannel()).getMapFactory();
            outMap = mapFactory.getMap(211000001);
            aaa.getClient().getPlayer().changeMap(outMap, outMap.getPortal(0));
            aaa.getClient().getPlayer().getEventInstance().unregisterPlayer(aaa.getClient().getPlayer()); //Unregister them all
        }
    }

    public int countMonster(final MapleCharacter chr) {
        MapleMap map = chr.getClient().getPlayer().getMap();
        double range = Double.POSITIVE_INFINITY;
        List<MapleMapObject> monsters = map.getMapObjectsInRange(chr.getClient().getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
        return monsters.size();
    }
    

    public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops) {
        killMonster(monster, chr, withDrops, false, 1);
    }
        

	public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime) {
		killMonster(monster, chr, withDrops, secondTime, 1);
	}
        
        
	public void killAllBoogies() {
		List<MapleMapObject> monsters = getMapObjectsInRange(new Point(0,0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
		for (MapleMapObject monstermo : monsters) {
			MapleMonster monster = (MapleMonster) monstermo;
			if (monster.getId() == 3230300 || monster.getId() == 3230301 || monster.getName().toLowerCase().contains("boogie")) {
				spawnedMonstersOnMap.decrementAndGet();
				monster.setHp(0);
				broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
				removeMapObject(monster);
			}
		}
		this.broadcastMessage(MaplePacketCreator.serverNotice(6, "As the rock crumbled, Jr. Boogie fell in great pain and disappeared."));
	}
        
	
	public void buffMap(int buffId) {
		MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
		MapleStatEffect statEffect = mii.getItemEffect(buffId);
		synchronized (this.characters) {
			for (MapleCharacter character : this.characters) {
				if (character.isAlive()) {
					statEffect.applyTo(character);
				}
			}
		}		
	}
	
      public boolean isCPQMap() {
		switch (this.getId()) {
			case 980000101:
			case 980000201:
			case 980000301:
			case 980000401:
			case 980000501:
			case 980000601:
				return true;
		}
		return false;
	}
	
	public boolean isBlueCPQMap() {
		switch (this.getId()) {
			case 980000501:
			case 980000601:
				return true;
		}
		return false;
	}
	
	public boolean isPurpleCPQMap() {
		switch (this.getId()) {
			case 980000301:
			case 980000401:
				return true;
		}
		return false;
	}
	
	public void addClock(int seconds) {
		broadcastMessage(MaplePacketCreator.getClock(seconds));
	}

	@SuppressWarnings("static-access")
	public void killMonster(final MapleMonster monster, final MapleCharacter chr, final boolean withDrops, final boolean secondTime, int animation) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        if (monster.getId() == 8810018 && !secondTime) {
            TimerManager.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    killMonster(monster, chr, withDrops, true, 1);
                    killAllMonsters(false);
                }
            }, 3000);
            return;
        }
        if (monster.getBuffToGive() > -1)
            for (MapleMapObject mmo : this.getAllPlayer()) {
                MapleCharacter character = (MapleCharacter) mmo;
                if (character.isAlive()) {
                    MapleStatEffect statEffect = mii.getItemEffect(monster.getBuffToGive());
                    statEffect.applyTo(character);
                }
            }
		if (monster.getCP() > 0) {
			chr.gainCP(monster.getCP());
		}
		if (monster.getId() == 8810018) { //Horntail
				if (chr.getMapId() == 240060200) {
						MaplePacket packet = MaplePacketCreator.serverNotice(0, "To the crew that have finally conquered Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!");
						//chr.getClient().getChannelServer().getMapFactory().getMap(211042300).resetReactors();
						//chr.getClient().getChannelServer().getMapFactory().getMap(21104300).getPortal("ps00").setPortalState(MaplePortal.OPEN);
						chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(1, "You have killed Horntail. You will be warped out in 2 minutes."));
						chr.getClient().getChannelServer().getMapFactory().getMap(240060200).resetReactors(); //Reset Reactors for this Map
						MapleMap toGoto = chr.getClient().getChannelServer().getMapFactory().getMap(240040700);
						MapleMap frm = chr.getMap();
						chr.getEventInstance().dispose(); //End the instance o.O
						//Disband Squad
						for (MapleCharacter aaa : characters) {
							aaa.getClient().getSession().write(MaplePacketCreator.getClock(120));
							if (aaa.getEventInstance() != null) {
								aaa.getEventInstance().unregisterPlayer(aaa);
							}
						}
						MapleSquad squad = chr.getClient().getChannelServer().getMapleSquad(MapleSquadType.HORNTAIL);
						chr.getClient().getChannelServer().removeMapleSquad(squad, MapleSquadType.HORNTAIL);
						TimerManager tMan = TimerManager.getInstance();
						tMan.schedule(new warpAll(toGoto, frm), 120000);
						try {
								chr.getClient().getChannelServer().getWorldInterface().broadcastMessage(chr.getName(), packet.getBytes());
						} catch (RemoteException e) {
								chr.getClient().getChannelServer().reconnectWorld();
						}
				}
		}
		  if (monster.getId() == 8510000) { //Super Pianus
            if (chr.getEventInstance() != null) {
                if (chr.getEventInstance().getName().startsWith("AquaPQ")) {
                    MaplePacket packet = MaplePacketCreator.serverNotice(0, "To the crew that have finally slain Super Pianus after numerous attempts, I salute thee! You are the true heroes of Aquarium!!");
                    chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(1, "You have killed Super Pianus. You will be warped out in 2 minutes."));
                    MapleMap toGoto = chr.getClient().getChannelServer().getMapFactory().getMap(230000000);
                    MapleMap frm = chr.getMap();
                    chr.getEventInstance().dispose(); //End the instance o.O
                    for (MapleCharacter aaa : characters) {
                        aaa.getClient().getSession().write(MaplePacketCreator.getClock(120));
                        if (aaa.getEventInstance() != null) {
                            aaa.getEventInstance().unregisterPlayer(aaa);
                        }
                    }
                    TimerManager tMan = TimerManager.getInstance();
                    tMan.schedule(new warpAll(toGoto, frm), 120000);
                    try {
                        chr.getClient().getChannelServer().getWorldInterface().broadcastMessage(chr.getName(), packet.getBytes());
                    } catch (RemoteException e) {
                        chr.getClient().getChannelServer().reconnectWorld();
                    }
                    superPianusDrops(chr, monster);
                    EventInstanceManager eim = chr.getEventInstance();
                    eim.setProperty("state", "0");
                    monster.disableDrops();
                }
            }
        }
		if (monster.getId() == 9300049){ //Dark Nependath is Killed o.O
				if (chr.getMapId() == 920010800 && chr.getEventInstance() != null) {
					//This spawns Papa Pixie
					EventInstanceManager eim = chr.getEventInstance();
					if (eim.getProperty("papaSpawned").equals("no")) {
						MaplePacket papapacket = MaplePacketCreator.serverNotice(5, "Here comes Papa Pixie.");
						MapleMonster papa = MapleLifeFactory.getMonster(9300039);
						chr.getClient().getPlayer().getMap().spawnMonsterWithCoords(papa, -98, 563);
						chr.getClient().getPlayer().getMap().broadcastMessage(papapacket);
						eim.setProperty("papaSpawned", "yes");
					}
				}
		}
		if (monster.getId() == 8500002) { //Pap :: Reset the Reactors and open the Door
				if (monster.getMap().getId() == 220080001) { //Make sure it IS this map
					chr.getClient().getChannelServer().getMapFactory().getMap(220080000).resetReactors();
					//chr.getClient().getChannelServer().getMapFactory().getMap(220080000).getPortal("in00").setPortalState(MaplePortal.OPEN);
				}
		}
		if (monster.getId() == 8800002) { //Zakum3
				if (monster.getMap().getId() == 280030000) { //Make sure it IS this map
					chr.getClient().getChannelServer().getMapFactory().getMap(211042300).resetReactors();
					//chr.getClient().getChannelServer().getMapFactory().getMap(211042300).getPortal("ps00").setPortalState(MaplePortal.OPEN);
					chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "You have killed Zakum. You will be warped out in 2 minutes."));
					chr.getClient().getChannelServer().getMapFactory().getMap(280030000).resetReactors(); //Reset Reactors for this Map
					MapleMap toGoto = chr.getClient().getChannelServer().getMapFactory().getMap(211042300);
					MapleMap frm = chr.getMap();
					//Disband Squad
					for (MapleCharacter aaa : characters) {
						aaa.getClient().getSession().write(MaplePacketCreator.getClock(120));
					}
					MapleSquad squad = chr.getClient().getChannelServer().getMapleSquad(MapleSquadType.ZAKUM);
					chr.getClient().getChannelServer().removeMapleSquad(squad, MapleSquadType.ZAKUM);
					TimerManager tMan = TimerManager.getInstance();
					tMan.schedule(new warpAll(toGoto, frm), 120000);
				}
		}
		if (monster.getId() == 8810000) {
				if (monster.getMap().getId() == 240060000 && chr.getEventInstance() != null) {
					chr.getEventInstance().setProperty("head1", "yes");
					chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, "You have killed Horntail\'s Left Head, and now you may proceed."));
				}
		}

		if (monster.getId() == 8810001) {
				if (monster.getMap().getId() == 240060100 && chr.getEventInstance() != null) {
					chr.getEventInstance().setProperty("head2", "yes");
					chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, "You have killed Horntail\'s Right Head, and now you may proceed."));
				}
		}
		if (monster.getId() == 9300040) { //Cellion for OPQ
			if (monster.getMap().getId() == 920010300 && chr.getEventInstance() != null) {
					EventInstanceManager eim = chr.getEventInstance();
					try {
						int alreadySpawned = Integer.parseInt(eim.getProperty("killedCellions")) + 1;
						Point spawnPoints[] = new Point[15];
						spawnPoints[0] = new Point(-99, -99);
						spawnPoints[1] = new Point(160, -717);
						spawnPoints[2] = new Point(-201, -915);
						spawnPoints[3] = new Point(192, -1122);
						spawnPoints[4] = new Point(-234, -1310);
						spawnPoints[5] = new Point(129, -1526);
						spawnPoints[6] = new Point(-276, -1736);
						spawnPoints[7] = new Point(134, -1912);
						spawnPoints[8] = new Point(-242, -2114);
						spawnPoints[9] = new Point(184, -2321);
						spawnPoints[10] = new Point(-228, -2510);
						spawnPoints[11] = new Point(136, -2716);
						spawnPoints[12] = new Point(-284, -2910);
						spawnPoints[13] = new Point(125, -3116);
						spawnPoints[14] = new Point(-215, -3357); //Last Cellion
						int index = (int) (Math.round(Math.random() * (spawnPoints.length - 1)));
						Point pt = spawnPoints[index];
						if (alreadySpawned < 13) {
							//MapleMonster mob = MapleLifeFactory.getMonster(9300040);
							final MapleMap map = this;
							final Point fpt = pt;
							//this.spawnMonsterOnGroundBelow(mob, pt);
							TimerManager tMan = TimerManager.getInstance();
							tMan.schedule(new Runnable() {
								@Override
								public void run() {
									MapleMonster mob = MapleLifeFactory.getMonster(9300040);
									map.spawnMonsterOnGroundBelow(mob, fpt);
									map.broadcastMessage(MaplePacketCreator.serverNotice(5, "Cellion has been summoned somewhere in the map."));
								}
							}, 3000 + (long) (Math.round(Math.random() * 2000)));
						} else {
							//MapleMonster mob = MapleLifeFactory.getMonster(9300057);
							//this.spawnMonsterOnGroundBelow(mob, pt);   
							final MapleMap map = this;
							final Point fpt = pt;
							//this.spawnMonsterOnGroundBelow(mob, pt);
							TimerManager tMan = TimerManager.getInstance();
							tMan.schedule(new Runnable() {
								@Override
								public void run() {
									MapleMonster mob = MapleLifeFactory.getMonster(9300057);
									map.spawnMonsterOnGroundBelow(mob, fpt);
									map.broadcastMessage(MaplePacketCreator.serverNotice(5, "Cellion has been summoned somewhere in the map."));
								}
							}, 3000 + (long) (Math.round(Math.random() * 2000)));
						}
						String newVal = Integer.toString(alreadySpawned + 1);
						eim.setProperty("killedCellions", newVal); //Increment the value
					} catch (NumberFormatException nfe) {
						//do nothing...
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						log.error("Out of bounds error while trying to spawn cellion: " + aioobe);
					} catch (Exception exc) {
						log.error("Error while trying to spawn cellion: " + exc);
					}
			 }
		}
		spawnedMonstersOnMap.decrementAndGet();
                monster.setHp(0);
                broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
                removeMapObject(monster);
                /* GuildPQ Rev - "+Configuration.Server_Name+" */
                if (monster.getId() == 9300025 && this.mapid == 990000630) {
                this.spawnMonsterOnGroundBelow(9300024, -83, 155);
                this.spawnMonsterOnGroundBelow(9300024, -83, 155);
                this.spawnMonsterOnGroundBelow(9300024, -83, 155);
                this.spawnMonsterOnGroundBelow(9300024, -83, 155);
                this.spawnMonsterOnGroundBelow(9300024, -83, 155);
                this.spawnMonsterOnGroundBelow(9300024, -83, 155);
                this.spawnMonsterOnGroundBelow(9300024, -83, 155);
                this.spawnMonsterOnGroundBelow(9300024, -83, 155);
                }
                /*Ludibrium PQ Rev - "+Configuration.Server_Name+" */
                if (monster.getId() == 9300170 && this.mapid == 922010700) {
                this.broadcastMessage(MaplePacketCreator.serverNotice(5, "Rombot foi convocado em algum lugar do mapa."));
                this.spawnMonsterOnGroundBelow(9300010, 1, -211);
                this.destroyReactor(this.getReactorByLinkId(0).getObjectId());
                }
                if (monster.getId() == 9300169 && this.mapid == 922010700) {
                this.broadcastMessage(MaplePacketCreator.serverNotice(5, "Rombot foi convocado em algum lugar do mapa."));
                this.spawnMonsterOnGroundBelow(9300010, 1, -211);
                this.destroyReactor(this.getReactorByLinkId(1).getObjectId());
                }
                if (monster.getId() == 9300171 && this.mapid == 922010700) {
                this.broadcastMessage(MaplePacketCreator.serverNotice(5, "Rombot foi convocado em algum lugar do mapa."));
                this.spawnMonsterOnGroundBelow(9300010, 1, -211);
                this.destroyReactor(this.getReactorByLinkId(2).getObjectId());

                }
                if (monster.getId() == 9300006 && this.mapid == 922010900) {
                this.broadcastMessage(MaplePacketCreator.serverNotice(5, "Alishar foi invocado!"));
                this.spawnMonsterOnGroundBelow(9300012, 941, 184);
                this.destroyReactor(this.getReactorByLinkId(0).getObjectId());
                }
		if (monster.getId() >= 8800003 && monster.getId() <= 8800010) {
			boolean makeZakReal = true;
			Collection<MapleMapObject> objects = getMapObjects();
			for (MapleMapObject object : objects) {
				MapleMonster mons = getMonsterByOid(object.getObjectId());
				if (mons != null) {
					if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
						makeZakReal = false;
					}
				}
			}
			if (makeZakReal) {
				for (MapleMapObject object : objects) {
					MapleMonster mons = chr.getMap().getMonsterByOid(object.getObjectId());
					if (mons != null) {
						if (mons.getId() == 8800000) {
							makeMonsterReal(mons);
							updateMonsterController(mons);
						}
					}
				}
			}
		}
		MapleCharacter dropOwner = monster.killBy(chr);
		if (withDrops && !monster.dropsDisabled()) {
			if (dropOwner == null) {
				dropOwner = chr;
			}
			dropFromMonster(dropOwner, monster);
		}
	}
        
         public MapleReactor getReactorByLinkId(int Id) {
        synchronized (mapobjects) {
            int oid = this.reactorLink.get(Id);
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getObjectId() == oid) {
                        return (MapleReactor) obj;
                    }
                }
            }
            return null;
        }
    }
        
	private void monsterCarnivalDrops(final MapleCharacter dropOwner, final MapleMonster monster) {
        ChannelServer cserv = dropOwner.getClient().getChannelServer();
        int randd;
        int toAdd;
        int times = 3 + (monster.getId() == 9300136 ? 2 : 1);
        int dropArray[] = {2022157, 2022158, 2022159, 2022160, 2022161, 2022162, 2022163, 2022164, 2022165, 2022166, 2022167, 2022168, 2022169, 2022170, 2022171, 2022172, 2022173, 2022174, 2022175, 2022176, 2022177, 2022178, 4001129 }; //These are the drops, -1 means meso :D
        int dropAmountz[] = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}; //Drop amount - Amount of what item to drop
        List<Integer> toDrop = new ArrayList<Integer>();
        List<Integer> amountDrop = new ArrayList<Integer>();
		for (int i = 0; i <times; i++) {
            randd = (int) (Math.floor(Math.random() * (dropArray.length)));
            toAdd = dropArray[randd];
			toDrop.add(toAdd);
            amountDrop.add(dropAmountz[randd]);
		}
        final int mesoRate = cserv.getMesoRate();
		if (toDrop.size() > times) {
			toDrop = toDrop.subList(0, times);
		}
		for (int i = 0; i < toDrop.size(); i++) {
			int drop = toDrop.get(i);
			if (drop == 4001129) {
				if (Math.random() > 0.2) { //Maple Coin
					toDrop.set(i, -2);
				}
			}
		}
		Point[] toPoint = new Point[toDrop.size()];
		int shiftDirection = 0;
		int shiftCount = 0;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		int curX = Math.min(Math.max(monster.getPosition().x - 25 * (toDrop.size() / 2), footholds.getMinDropX() + 25), footholds.getMaxDropX() - toDrop.size() * 25);
		int curY = Math.max(monster.getPosition().y, footholds.getY1());
		while (shiftDirection < 3 && shiftCount < 1000) {
			if (shiftDirection == 1)
				curX += 25;
			else if (shiftDirection == 2)
				curX -= 25;
			for (int i = 0; i < times; i++) {
				MapleFoothold wall = footholds.findWall(new Point(curX, curY), new Point(curX + toDrop.size() * 25, curY));
				if (wall != null) {
					if (wall.getX1() < curX) {
						shiftDirection = 1;
						shiftCount++;
						break;
					} else if (wall.getX1() == curX) {
						if (shiftDirection == 0)
							shiftDirection = 1;
						shiftCount++;
						break;
					} else {
						shiftDirection = 2;
						shiftCount++;
						break;
					}
				} else if (i == toDrop.size() - 1) {
					shiftDirection = 3;
				}
				final Point dropPos = calcDropPos(new Point(curX + i * 25, curY), new Point(monster.getPosition()));
				toPoint[i] = new Point(curX + i * 25, curY);
				final int drop = toDrop.get(i);
                final int dropAmounti = amountDrop.get(i);
                final short dropAmountReal = (short) (dropAmounti);
                int tempmeso;
				if (drop == -2) continue;
				if (drop == -1) {
					Random r = new Random();
					double mesoDecrease = Math.pow(0.93, monster.getExp() / 300.0);
					if (mesoDecrease > 1.0) {
						mesoDecrease = 1.0;
					}
					tempmeso = Math.min(30000, (int) (mesoDecrease * (monster.getExp()) * (1.0 + r.nextInt(20)) / 10.0));
                } else
                    tempmeso = 0;
					if (dropOwner.getBuffedValue(MapleBuffStat.MESOUP) != null) {
						tempmeso = (int) (tempmeso * dropOwner.getBuffedValue(MapleBuffStat.MESOUP).doubleValue() / 100.0);
					}
					final int meso = tempmeso;
					if (meso > 0) {
						final MapleMonster dropMonster = monster;
						final MapleCharacter dropChar = dropOwner;
						TimerManager.getInstance().schedule(new Runnable() {
							public void run() {
								spawnMesoDrop(meso * mesoRate, meso, dropPos, dropMonster, dropChar, true);
							}
						}, monster.getAnimationTime("die1"));
					} else {
                        IItem idrop;
                        MapleInventoryType type = ii.getInventoryType(drop);
                        if (type.equals(MapleInventoryType.EQUIP)) {
                            Equip nEquip = ii.randomizeStats((Equip) ii.getEquipById(drop));
                            idrop = nEquip;
                        } else {
                            idrop = new Item(drop, (byte) 0, (short) 1);
                            if (ii.isArrowForBow(drop) || ii.isArrowForCrossBow(drop) || ii.isThrowingStar(drop))
                                idrop.setQuantity((short) (1 + ii.getSlotMax(dropOwner.getClient(), drop) * Math.random()));
                            idrop.setQuantity(dropAmountReal); //Set the quantity! w00t!
                        }
                        StringBuilder logMsg = new StringBuilder("Created as a drop from monster ");
                        logMsg.append(monster.getObjectId());
                        logMsg.append(" (");
                        logMsg.append(monster.getId());
                        logMsg.append(") at ");
                        logMsg.append(dropPos.toString());
                        logMsg.append(" on map ");
                        logMsg.append(mapid);
                        idrop.log(logMsg.toString(),false);

                        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, monster, dropOwner);
                        final MapleMapObject dropMonster = monster;
                        final MapleCharacter dropChar = dropOwner;
                        final TimerManager tMan = TimerManager.getInstance();
                        final MapleClient c;

                        tMan.schedule(new Runnable() {
                            public void run() {
                                spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
                                    public void sendPackets(MapleClient c) {
                                        c.getSession().write(MaplePacketCreator.dropItemFromMapObject(drop, mdrop.getObjectId(), dropMonster.getObjectId(), monster.isBoss() ? 0 : dropChar.getId(), dropMonster.getPosition(), dropPos, (byte) 1));
                                    }
                                });
                                tMan.schedule(new ExpireMapItemJob(mdrop), 60000);
                            }
                        }, monster.getAnimationTime("die1"));
                    }
            }
        }
    }

    public void scheduleWarp(MapleMap toGoto, MapleMap frm, long time) {
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(new warpAll(toGoto, frm), time);
    }
    
	 public void killAllMonsters(boolean drop) {
        List<MapleMapObject> players = null;
        if (drop)
            players = getAllPlayer();
        List<MapleMapObject> monsters = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for (MapleMapObject monstermo : monsters) {
            MapleMonster monster = (MapleMonster) monstermo;
            spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0);
            broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), true), monster.getPosition());
            removeMapObject(monster);
            if (drop)
                dropFromMonster((MapleCharacter) players.get((int) Math.random() * (players.size())), monster);
        }
    }

	public void killMonster(int monsId) {
		for (MapleMapObject mmo : getMapObjects()) {
			if (mmo instanceof MapleMonster) {
				if (((MapleMonster) mmo).getId() == monsId) {
					this.killMonster((MapleMonster) mmo, (MapleCharacter) getAllPlayer().get(0), false);
				}
			}
		}
	}

	public List<MapleMapObject> getAllPlayer() {
		return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
	}

	public final void destroyReactor(final int oid) {
	final MapleReactor reactor = getReactorByOid(oid);
	broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
	reactor.setAlive(false);
	removeMapObject(reactor);
	reactor.setTimerActive(false);

	if (reactor.getDelay() > 0) {
	    MapTimer.getInstance().schedule(new Runnable() {

		@Override
		public final void run() {
		    respawnReactor(reactor);
		}
	    }, reactor.getDelay());
	}
    }

	/*
	 * command to reset all item-reactors in a map to state 0 for GM/NPC use - not tested (broken reactors get removed
	 * from mapobjects when destroyed) Should create instances for multiple copies of non-respawning reactors...
	 */
    public void resetReactors() {
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setState((byte) 0);
                    ((MapleReactor) o).setTimerActive(false);
                    broadcastMessage(MaplePacketCreator.triggerReactor((MapleReactor) o, 0));
                }
            }
        }
    }

       /*
	*
        * command to shuffle the positions of all reactors in a map for PQ purposes (such as ZPQ/LMPQ)
        */
    public void shuffleReactors() {
        List<Point> points = new ArrayList<Point>();
        synchronized (this.mapobjects) {
            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    points.add(((MapleReactor) o).getPosition());
                }
            }

            Collections.shuffle(points);

            for (MapleMapObject o : mapobjects.values()) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    ((MapleReactor) o).setPosition(points.remove(points.size() - 1));
                }
            }
        }
    }

	/**
     * Automagically finds a new controller for the given monster from the chars on the map...
     *
     * @param monster
     */
     public void updateMonsterController(MapleMonster monster) {
        synchronized (monster) {
            if (!monster.isAlive()) {
                return;
            }
            if (monster.getController() != null) {
                if (monster.getController().getMap() != this) {
                    monster.getController().stopControllingMonster(monster);
                } else {
                    return;
                }
            }
            int mincontrolled = -1;
            MapleCharacter newController = null;
            synchronized (characters) {
                for (MapleCharacter chr : characters) {
                    if (!chr.isHidden() && (chr.getControlledMonsters().size() < mincontrolled || mincontrolled == -1)) {
                        mincontrolled = chr.getControlledMonsters().size();
                        newController = chr;
                    }
                }
            }
            if (newController != null) {// was a new controller found? (if not no one is on the map)
                if (monster.isFirstAttack()) {
                    newController.controlMonster(monster, true);
                    monster.setControllerHasAggro(true);
                    monster.setControllerKnowsAboutAggro(true);
                } else {
                    newController.controlMonster(monster, false);
                }
            }
        }
    }

	public Collection<MapleMapObject> getMapObjects() {
		return Collections.unmodifiableCollection(mapobjects.values());
	}

    public boolean containsNPC(int npcid) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.NPC) {
                    if (((MapleNPC) obj).getId() == npcid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

	public MapleMapObject getMapObject(int oid) {
		return mapobjects.get(oid);
	}

	/**
	 * returns a monster with the given oid, if no such monster exists returns null
	 * 
	 * @param oid
	 * @return
	 */
	public MapleMonster getMonsterByOid(int oid) {
		MapleMapObject mmo = getMapObject(oid);
		if (mmo == null) {
			return null;
		}
		if (mmo.getType() == MapleMapObjectType.MONSTER) {
			return (MapleMonster) mmo;
		}
		return null;
	}

	public MapleReactor getReactorByOid(int oid) {
		MapleMapObject mmo = getMapObject(oid);
		if (mmo == null) {
			return null;
		}
		if (mmo.getType() == MapleMapObjectType.REACTOR) {
			return (MapleReactor) mmo;
		}
		return null;
	}


	 public MapleReactor getReactorByName(String name) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getName().equals(name)) {
                        return (MapleReactor) obj;
                    }
                }
            }
        }
        return null;
    }

        public List<MapleCharacter> getPlayersInRange(Rectangle box, List<MapleCharacter> chr) { //what was whoever who wrote this THINKING!? needs synch
        List<MapleCharacter> character = new LinkedList<MapleCharacter>();
        characterlock.readLock().lock();
        try
        {
        for (MapleCharacter a : characters) {
            if (chr.contains(a.getClient().getPlayer())) {
                if (box.contains(a.getPosition())) {
                    character.add(a);
                }
            }
        }
        return character;
        } finally {
            characterlock.readLock().unlock();
        }
    }

	public void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
		Point spos = new Point(pos.x, pos.y - 1);
		spos = calcPointBelow(spos);
		spos.y -= 1;
		mob.setPosition(spos);
		spawnFakeMonster(mob);
	}

	public void spawnRevives(final MapleMonster monster) {
		monster.setMap(this);
		synchronized (this.mapobjects) {
			spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

				public void sendPackets(MapleClient c) {
					c.getSession().write(MaplePacketCreator.spawnMonster(monster, false));
				}
			}, null);
			updateMonsterController(monster);
		}
		spawnedMonstersOnMap.incrementAndGet();
	}
	
	/**
	 * returns 1 on success, 0 on already spawned, -1 on no spot
	 * @param status
	 * @param team
	 * @return
	 */
	public int spawnGuardian(MonsterStatus status, int team) {
		List<MapleMapObject> reactors = getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.REACTOR));
		for (GuardianSpawnPoint gs : this.guardianSpawns) {
			for (MapleMapObject o : reactors) {
				MapleReactor reactor = (MapleReactor) o;
				if (reactor.getCancelStatus().equals(status) && (reactor.getId() - 9980000) == team) {
					return 0;
				}
			}
		}
		GuardianSpawnPoint pt = this.getRandomGuardianSpawn(team);
		if (pt == null) {
			return -1;
		}
		int reactorID = 9980000 + team;
		MapleReactor reactor = new MapleReactor(MapleReactorFactory.getReactor(reactorID), reactorID);
		pt.setTaken(true);
		reactor.setPosition(pt.getPosition());
		this.spawnReactor(reactor);
		reactor.setCancelStatus(status);
		reactor.setGuardian(pt);
		this.buffMonsters(team, status);
		getReactorByOid(reactor.getObjectId()).hitReactor(((MapleCharacter)this.getAllPlayer().get(0)).getClient());
		return 1;
	}

     public void spawnMonster(final MapleMonster monster) {
        monster.setMap(this);
        synchronized (this.mapobjects) {
            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
                public void sendPackets(MapleClient c) {
                    c.getSession().write(MaplePacketCreator.spawnMonster(monster, true));
                    if (monster.getId() == 9300166 || monster.getId() == 8810026)
                        TimerManager.getInstance().schedule(new Runnable() {
                            @Override
                            public void run() {
                                killMonster(monster, (MapleCharacter) getAllPlayer().get(0), false, false, 3);
                            }
                        }, new Random().nextInt(5000));
                }
            }, null);
            updateMonsterController(monster);
        } if (monster.getId() == 9300166) {
            //Bomb
            final MapleMap map = this;
            TimerManager.getInstance().schedule(new Runnable() {
                public void run() {
                    killMonster(monster, (MapleCharacter) getAllPlayer().get(0), false, false, 3);
                    for (MapleMapObject ob : map.getMapObjectsInRange(monster.getPosition(), 40000, Arrays.asList(MapleMapObjectType.PLAYER))) {
                        MapleCharacter chr = (MapleCharacter) ob;
                        if (chr != null) {
                            if (chr.hasShield()) {
                                chr.cancelShield();
                                continue;
                            }
                            int hasJewels = chr.countItem(4031868);
                            if (hasJewels <= 0) {
                                chr.giveDebuff(MapleDisease.STUN, MobSkillFactory.getMobSkill(123, 11));
                                continue;
                            }
                            int drop = (int) (Math.random() * hasJewels);
                            if (drop > 5) {
                                drop = (int) (Math.random() * 5);
                            }
                            if (drop < 1) {
                                drop = 1;
                            }
                            MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.ETC, 4031868, (short) drop, false, false);
                            for (int i = 0; i < drop; i++) {
                                Point pos = chr.getPosition();
                                int x = pos.x;
                                int y = pos.y;
                                if (Math.random() < 0.5) {
                                    x -= (int) (Math.random() * 100);
                                } else {
                                    x += (int) (Math.random() * 100);
                                }
                                map.spawnItemDrop(ob, chr, new Item(4031868, (byte) -1, (short) 1), new Point(x, y), true, true);
                            }
                            broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chr.getName(), chr.getAriantScore(), false));
                        }
                    }
                }
            }, 3000 + (int) (Math.random() * 2000));
        }
        spawnedMonstersOnMap.incrementAndGet();
    }
	
        
        
	public boolean isAriantPQMap() {
		switch (this.getId()) {
			case 980010101:
			case 980010201:
			case 980010301:
				return true;
		}
		return false;
	}
	
	public void spawnCPQMonster(final MapleMonster monster, final int team) {
		monster.setMap(this);
		monster.setTeam(team);
		synchronized (this.mapobjects) {
			spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

				public void sendPackets(MapleClient c) {
					if (c.getPlayer().getParty() != null) {
						if (monster.getTeam() == c.getPlayer().getTeam()) {
							c.getSession().write(MaplePacketCreator.spawnFakeMonster(monster, 0));
						} else {
							c.getSession().write(MaplePacketCreator.spawnMonster(monster, true));
						}
					} else {
						c.getSession().write(MaplePacketCreator.spawnMonster(monster, true));
					}
/*					Random rand = new Random();
					if (monster.getId() == 9300166) {
						removeAfter = rand.nextInt((4500) + 500);
					}*/
					if (monster.getRemoveAfter() > 0) { // 9300166
						TimerManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							killMonster(monster, (MapleCharacter) getAllPlayer().get(0), false, false, 3);
						}
					}, monster.getRemoveAfter());
				}
			}
		}, null);
		updateMonsterController(monster);
		List<MonsterStatus> teamS = null;
		if (team == 0) teamS = redTeamBuffs;
		else if (team == 1) teamS = blueTeamBuffs;
		if (teamS != null) {
			for (MonsterStatus status : teamS) {
				int skillID = getSkillId(status);
				MobSkill skill = getMobSkill(skillID, this.getSkillLevel(status));
					monster.applyMonsterBuff(status, skill.getX(), skill.getSkillId(), 
					60 * 1000 * 10, skill);
			}
		}
	}
	spawnedMonstersOnMap.incrementAndGet();
	}
	
	public int spawnMonsterWithCoords(MapleMonster mob, int x, int y) {
    	Point spos = new Point(x, y - 1);
    	spos = calcPointBelow(spos);
    	spos.y -= 1;
    	mob.setPosition(spos);    
    	spawnMonster(mob);
		return mob.getObjectId();
   	}

	public void spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
		try {
			monster.setMap(this);
			Point spos = new Point(pos.x, pos.y - 1);
			spos = calcPointBelow(spos);
			spos.y -= 1;
			monster.setPosition(spos);
			monster.disableDrops();
			synchronized (this.mapobjects) {
				spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

					public void sendPackets(MapleClient c) {
						c.getSession().write(MaplePacketCreator.spawnMonster(monster, true, effect));
					}
				}, null);
				/*if (monster.hasBossHPBar()) {
				broadcastMessage(monster.makeBossHPBarPacket(), monster.getPosition());
				}*/
				updateMonsterController(monster);
			}
			spawnedMonstersOnMap.incrementAndGet();
		} catch (Exception e) {
		}
	}

	public boolean hasTimer() {
		return timer;
	}

	public void setTimer(boolean timer) {
		this.timer = timer;
	}

	public void spawnFakeMonster(final MapleMonster monster) {
		monster.setMap(this);
		monster.setFake(true);
		synchronized (this.mapobjects) {
			spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {

				public void sendPackets(MapleClient c) {
					c.getSession().write(MaplePacketCreator.spawnFakeMonster(monster, 0));
				}
			}, null);
		}
		spawnedMonstersOnMap.incrementAndGet();
	}

	public void makeMonsterReal(final MapleMonster monster) {
		monster.setFake(false);
		broadcastMessage(MaplePacketCreator.makeMonsterReal(monster));
		/*if (monster.hasBossHPBar()) {
		broadcastMessage(monster.makeBossHPBarPacket(), monster.getPosition());
		}*/
		updateMonsterController(monster);
	}

	public final void spawnReactor(final MapleReactor reactor) {
	reactor.setMap(this);

	spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {

	    @Override
	    public final void sendPackets(MapleClient c) {
		c.getSession().write(MaplePacketCreator.spawnReactor(reactor));
	    }
	}, null);
    }

	private void respawnReactor(final MapleReactor reactor) {
		reactor.setState((byte) 0);
		reactor.setAlive(true);
		spawnReactor(reactor);
	}

        
        public void respawn() {
        if (characters.size() == 0 || this.properties.getProperty("respawn").equals(Boolean.FALSE)) {
            return;
        }
        int ispawnedMonstersOnMap = spawnedMonstersOnMap.get();
                        int getMaxSpawn = getMaxRegularSpawn() * 2;
			int numShouldSpawn = getMaxSpawn - ispawnedMonstersOnMap;
                        if (numShouldSpawn + ispawnedMonstersOnMap >= getMaxSpawn) {
				numShouldSpawn = getMaxSpawn - ispawnedMonstersOnMap;
			}
			if (numShouldSpawn <= 0) {
				return;
			}
			List<SpawnPoint> randomSpawn = new ArrayList<SpawnPoint>(monsterSpawn);
			Collections.shuffle(randomSpawn);
			int spawned = 0;
			for (SpawnPoint spawnPoint : randomSpawn) {
				if (spawnPoint.shouldSpawn()) {
					spawnPoint.spawnMonster(MapleMap.this);
					spawned++;
				}
				if (spawned >= numShouldSpawn) {
					break;
				}
			}
    
        }
        
        
	public void spawnDoor(final MapleDoor door) {
		synchronized (this.mapobjects) {
			spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {

				public void sendPackets(MapleClient c) {
					c.getSession().write(MaplePacketCreator.spawnDoor(door.getOwner().getId(), door.getTargetPosition(), false));
					if (door.getOwner().getParty() != null && (door.getOwner() == c.getPlayer() || door.getOwner().getParty().containsMembers(new MaplePartyCharacter(c.getPlayer())))) {
						c.getSession().write(MaplePacketCreator.partyPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
					}
					c.getSession().write(MaplePacketCreator.spawnPortal(door.getTown().getId(), door.getTarget().getId(), door.getTargetPosition()));
					c.getSession().write(MaplePacketCreator.enableActions());
				}
			}, new SpawnCondition() {

				public boolean canSpawn(MapleCharacter chr) {
					return chr.getMapId() == door.getTarget().getId() ||
						chr == door.getOwner() && chr.getParty() == null;
				}
			});
		}
	}
        
       public MapleMonster getMonsterById(int id) {
        objectlock.readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.MONSTER) {
                    if (((MapleMonster) obj).getId() == id) {
                        return (MapleMonster) obj;
                    }
                }
            }
        }
        finally
        {
            objectlock.readLock().unlock();
        }
        return null;
    }
          


//    public void spawnSummon(final MapleSummon summon) {
//        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {
//            public void sendPackets(MapleClient c) {
//                int skillLevel = summon.getOwner().getSkillLevel(SkillFactory.getSkill(summon.getSkill()));
//                c.getSession().write(MaplePacketCreator.spawnSpecialMapObject(summon, skillLevel, true));
//            }
//        });
//    }
    
     public final void spawnSummon(final MapleSummon summon) {
	spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {

	    @Override
	    public void sendPackets(MapleClient c) {
		c.getSession().write(MaplePacketCreator.spawnSummon(summon, summon.getSkillLevel(), true));
	    }
	}, null);
    }

	public void spawnMist(final MapleMist mist, final int duration, boolean poison, boolean fake) {
		addMapObject(mist);
		broadcastMessage(fake ? mist.makeFakeSpawnData(30) : mist.makeSpawnData());
		TimerManager tMan = TimerManager.getInstance();
		final ScheduledFuture<?> poisonSchedule;
		if (poison) {
			Runnable poisonTask = new Runnable() {

				@Override
				public void run() {
					List<MapleMapObject> affectedMonsters = getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
					for (MapleMapObject mo : affectedMonsters) {
						if (mist.makeChanceResult()) {
							MonsterStatusEffect poisonEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), mist.getSourceSkill(), false);
							((MapleMonster) mo).applyStatus(mist.getOwner(), poisonEffect, true, duration);
						}
					}
				}
			};
			poisonSchedule = tMan.register(poisonTask, 2000, 2500);
		} else {
			poisonSchedule = null;
		}
		tMan.schedule(new Runnable() {

			@Override
			public void run() {
				removeMapObject(mist);
				if (poisonSchedule != null) {
					poisonSchedule.cancel(false);
				}
				broadcastMessage(mist.makeDestroyData());
			}
		}, duration);
	}

	public void disappearingItemDrop(final MapleMapObject dropper,
		final MapleCharacter owner, final IItem item, Point pos) {
		final Point droppos = calcDropPos(pos, pos);
		final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner);
		broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, 0, dropper.getPosition(), droppos, (byte) 3), drop.getPosition());
	}

      public void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, Point pos, final boolean ffaDrop, final boolean expire) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner);
        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
                c.getSession().write(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, ffaDrop ? 0 : owner.getId(),
                dropper.getPosition(), droppos, (byte) 1));
            }
        });
        broadcastMessage(MaplePacketCreator.dropItemFromMapObject(item.getItemId(), drop.getObjectId(), 0, ffaDrop ? 0 : owner.getId(), dropper.getPosition(), droppos, (byte) 0), drop.getPosition());
        if (expire)
            TimerManager.getInstance().schedule(new ExpireMapItemJob(drop), dropLife);
        activateItemReactors(drop);
    }
          public final int getCharactersSize() {
	    return characters.size();
    }
     
    public void timeMob(int id, String msg) {
        timeMob = new Pair<>(id, msg);
    }

    public Pair<Integer, String> getTimeMob() {
        return timeMob;
    }

    public void toggleHiddenNPC(int id) {
        for (MapleMapObject obj : mapobjects.values()) {
            if (obj.getType() == MapleMapObjectType.NPC) {
                MapleNPC npc = (MapleNPC) obj;
                if (npc.getId() == id) {
                    npc.setHide(!npc.isHidden());
                    if (!npc.isHidden()) //Should only be hidden upon changing maps
                    {
                        broadcastMessage(MaplePacketCreator.spawnNPC(npc));
                    }
                }
            }
        }
    }

   public boolean isMiniDungeonMap() {
        switch (mapid) {
            case 100020000:
            case 105040304:
            case 105050100:
            case 221023400:
                return true;
            default:
                return false;
        }
    }
   
  public void broadcastNONGMMessage(MaplePacket packet) {
        broadcastNONGMMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

   public void broadcastGMMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastGMMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    private void broadcastGMMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && chr.isGM()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

    
     private void broadcastNONGMMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source && !chr.isFake() && !chr.isGM()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

       /**
	 * not threadsafe, please synchronize yourself
	 * 
	 * @param monster
	 */
	public void addMonsterSpawn(MapleMonster monster, int mobTime) {
		Point newpos = calcPointBelow(monster.getPosition());
		newpos.y -= 1;
		SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime);

		monsterSpawn.add(sp);
		if (sp.shouldSpawn() || mobTime == -1) { // -1 does not respawn and should not either but force ONE spawn
			sp.spawnMonster(this);
		}
	}
	
	public void addMonsterSpawn(MapleMonster monster, int mobTime, int team) {
		Point newpos = calcPointBelow(monster.getPosition());
		newpos.y -= 1;
		SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime, team);

		monsterSpawn.add(sp);
		if (sp.shouldSpawn() || mobTime == -1 || (team == 0 || team == 1)) { // -1 does not respawn and should not either but force ONE spawn
			sp.spawnMonster(this);
		}
	}


    
	private class TimerDestroyWorker implements Runnable {

		@Override
		public void run() {
			if (mapTimer != null) {
				int warpMap = mapTimer.warpToMap();
				int minWarp = mapTimer.minLevelToWarp();
				int maxWarp = mapTimer.maxLevelToWarp();
				mapTimer = null;
				if (warpMap != -1) {
					MapleMap map2wa2 = ChannelServer.getInstance(channel).getMapFactory().getMap(warpMap);
					String warpmsg = "You will now be warped to " + map2wa2.getStreetName() + " : " + map2wa2.getMapName();
					broadcastMessage(MaplePacketCreator.serverNotice(6, warpmsg));
					for (MapleCharacter chr : getCharacters()) {
						try {
							if (chr.getLevel() >= minWarp && chr.getLevel() <= maxWarp) {
								chr.changeMap(map2wa2, map2wa2.getPortal(0));
							} else {
								chr.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You are not at least level " + minWarp + " or you are higher than level " + maxWarp + "."));
							}
						} catch (Exception ex) {
							String errormsg = "There was a problem warping you. Please contact a GM";
							chr.getClient().getSession().write(MaplePacketCreator.serverNotice(5, errormsg));
						}
					}
				}
			}
		}
	}

	public void addMapTimer(int duration) {
		ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
		mapTimer = new MapleMapTimer(sf0f, duration, -1, -1, -1);
		// TimerManager.getInstance().

		broadcastMessage(mapTimer.makeSpawnData());
	}

	public void addMapTimer(int duration, int mapToWarpTo) {
		ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
		mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, 0, 256);
		// TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
		broadcastMessage(mapTimer.makeSpawnData());
	}

	public void addMapTimer(int duration, int mapToWarpTo, int minLevelToWarp) {
		ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
		mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, minLevelToWarp, 256);
		// TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
		broadcastMessage(mapTimer.makeSpawnData());
	}

	public void addMapTimer(int duration, int mapToWarpTo, int minLevelToWarp, int maxLevelToWarp) {
		ScheduledFuture<?> sf0f = TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
		mapTimer = new MapleMapTimer(sf0f, duration, mapToWarpTo, minLevelToWarp, maxLevelToWarp);
		// TimerManager.getInstance().schedule(new TimerDestroyWorker(), duration * 1000);
		broadcastMessage(mapTimer.makeSpawnData());
	}

	public void clearMapTimer() {
		if (mapTimer != null) {
			mapTimer.getSF0F().cancel(true);
		}
		mapTimer = null;
	}

        private void activateItemReactors(MapleMapItem drop) {
		IItem item = drop.getItem();
		final TimerManager tMan = TimerManager.getInstance();
		//check for reactors on map that might use this item
		for (MapleMapObject o : mapobjects.values()) {
			if (o.getType() == MapleMapObjectType.REACTOR){
				if (((MapleReactor)o).getReactorType() == 100 && ((MapleReactor) o).getId() != 2008006 && ((MapleReactor) o).getId() != 2408002 && ((MapleReactor) o).getId() != 2006001) {
					if (((MapleReactor)o).getReactItem().getLeft() == item.getItemId() && ((MapleReactor)o).getReactItem().getRight() <= item.getQuantity()){
						Rectangle area = ((MapleReactor)o).getArea();

						if (area.contains(drop.getPosition())){
							MapleClient ownerClient = null;
							if (drop.getOwner() != null) {
								ownerClient = drop.getOwner().getClient();
							}
							tMan.schedule(new ActivateItemReactor(drop, ((MapleReactor)o), ownerClient), 5000);
						}
					}
				}
			}
		}
                for (MapleMapObject o : mapobjects.values()) {
			if (o.getType() == MapleMapObjectType.REACTOR){
				if (((MapleReactor)o).getReactorType() == 100 && ((MapleReactor) o).getId() == 2008006) {
                                    //4001056 - 4001062
					//if (((MapleReactor)o).getReactItem().getLeft() == item.getItemId() && ((MapleReactor)o).getReactItem().getRight() <= item.getQuantity()){
                                        if (item.getItemId() == 4001056 ||
                                            item.getItemId() == 4001057 ||
                                            item.getItemId() == 4001058 ||
                                            item.getItemId() == 4001059 ||
                                            item.getItemId() == 4001060 ||
                                            item.getItemId() == 4001061 ||
                                            item.getItemId() == 4001062){
						Rectangle area = ((MapleReactor)o).getArea();
                                                int itemid = item.getItemId();
                                                int state;
                                                switch (itemid) {
                                                    case 4001056:
                                                        state = 1;
                                                        break;
                                                    case 4001057:
                                                        state = 2;
                                                        break;
                                                    case 4001058:
                                                        state = 3;
                                                        break;
                                                    case 4001059:
                                                        state = 4;
                                                        break;
                                                    case 4001060:
                                                        state = 5;
                                                        break;
                                                    case 4001061:
                                                        state = 6;
                                                        break;
                                                    case 4001062:
                                                        state = 7;
                                                        break;
                                                    default :
                                                        throw new RuntimeException("Reactor state for OPQ Lobby reactor out of range.");
                                                }

						if (area.contains(drop.getPosition())){
							MapleClient ownerClient = null;
							if (drop.getOwner() != null) {
								ownerClient = drop.getOwner().getClient();
							}
							tMan.schedule(new ActivateItemReactorPlus(drop, ((MapleReactor)o), ownerClient, state), 5000);
						}
					}
				}
			}
		}
                for (MapleMapObject o : mapobjects.values()) {
			if (o.getType() == MapleMapObjectType.REACTOR){
				if (((MapleReactor)o).getReactorType() == 100 && ((MapleReactor) o).getId() == 2408002) {
                                    int mid = ((MapleReactor) o).getMap().getId();
                                    int iid;
                                        switch (mid) {
                                            case 240050101: iid = 4001087;
                                                            break;
                                            case 240050102: iid = 4001088;
                                                            break;
                                            case 240050103: iid = 4001089;
                                                            break;
                                            case 240050104: iid = 4001090;
                                                            break;
                                            default : iid = -1;
                                                      break;
                                                            
                                        }
					if (iid == item.getItemId() && ((MapleReactor)o).getReactItem().getRight() <= 6){
						Rectangle area = ((MapleReactor)o).getArea();

						if (area.contains(drop.getPosition())){
							MapleClient ownerClient = null;
							if (drop.getOwner() != null) {
								ownerClient = drop.getOwner().getClient();
							}
							tMan.schedule(new ActivateItemReactor(drop, ((MapleReactor)o), ownerClient), 5000);
						}
					}
				}
			}
		}
                for (MapleMapObject o : mapobjects.values()) { //Goddess reactor
			if (o.getType() == MapleMapObjectType.REACTOR){
				if (((MapleReactor) o).getId() == 2006001) {
					if (item.getItemId() == 4001055){
						if (this.getReactorByName("scar1").getState() == 1 &&
							this.getReactorByName("scar2").getState() == 1 &&
							this.getReactorByName("scar3").getState() == 1 &&
							this.getReactorByName("scar4").getState() == 1 &&
							this.getReactorByName("scar5").getState() == 1 &&
							this.getReactorByName("scar6").getState() == 1) {
								//Rectangle area = ((MapleReactor)o).getArea();

								//if (area.contains(drop.getPosition())){
										MapleClient ownerClient = null;
										if (drop.getOwner() != null) {
												ownerClient = drop.getOwner().getClient();
										}
										tMan.schedule(new ActivateItemReactor(drop, ((MapleReactor)o), ownerClient), 5000);
								//}
						}
					}
				}
			}
		}
                //2006001
	}
		
	public void removePortals() {
		for (MaplePortal pt : getPortals()) {
			pt.setScriptName("blank");
		}
	}
	
	public final void spawnNpc(final int id, final Point pos) {
	final MapleNPC npc = MapleLifeFactory.getNPC(id);
	npc.setPosition(pos);
	npc.setCy(pos.y);
	npc.setRx0(pos.x + 50);
	npc.setRx1(pos.x - 50);
	npc.setFh(getFootholds().findBelow(pos).getId());
	npc.setCustom(true);
	addMapObject(npc);
	broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
    }

	public void AriantPQStart() {
		int i = 1;
		for (MapleCharacter chars2 : this.getCharacters()) {
			broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, false));
			broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, false).toString()));
			if (this.getCharacters().size() > i) {
				broadcastMessage(MaplePacketCreator.updateAriantPQRanking(null, 0, true));
				broadcastMessage(MaplePacketCreator.serverNotice(0, MaplePacketCreator.updateAriantPQRanking(chars2.getName(), 0, true).toString()));
			}
			i++;
		}
	}

	public void spawnMesoDrop(final int meso, final int displayMeso, Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean ffaLoot) {
		TimerManager tMan = TimerManager.getInstance();
		final Point droppos = calcDropPos(position, position);
		final MapleMapItem mdrop = new MapleMapItem(meso, displayMeso, droppos, dropper, owner);
		spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {

			public void sendPackets(MapleClient c) {
				c.getSession().write(MaplePacketCreator.dropMesoFromMapObject(displayMeso, mdrop.getObjectId(), dropper.getObjectId(),
					ffaLoot ? 0 : owner.getId(), dropper.getPosition(), droppos, (byte) 1));
			}
		}, null);
		tMan.schedule(new ExpireMapItemJob(mdrop), dropLife);
	}

	public void startMapEffect(String msg, int itemId) {
		if (mapEffect != null) {
			return;
		}
		mapEffect = new MapleMapEffect(msg, itemId);
		broadcastMessage(mapEffect.makeStartData());
		TimerManager tMan = TimerManager.getInstance();
		/*tMan.schedule(new Runnable() {
		@Override
		public void run() {
		mapEffect.setActive(false);
		broadcastMessage(mapEffect.makeStartData());
		}
		}, 20000);*/
		sfme = tMan.schedule(new Runnable() {

			@Override
			public void run() {
				broadcastMessage(mapEffect.makeDestroyData());
				mapEffect = null;
			}
		}, 30000);
	}
    public void stopMapEffect()
    {
        if(sfme != null)
        sfme.cancel(false);
        if(mapEffect != null)
        broadcastMessage(mapEffect.makeDestroyData());
        mapEffect = null;
    }
    
    
/**
     * Adds a player to this map and sends nescessary data
     *
     * @param chr
     */
    public void addPlayer(MapleCharacter chr) {
        //log.warn("[dc] [level2] Player {} enters map {}", new Object[] { chr.getName(), mapid });
        synchronized (characters) {
            this.characters.add(chr);
        }
        synchronized (this.mapobjects) {
            if (!chr.isHidden()) {
                broadcastMessage(chr, (MaplePacketCreator.spawnPlayerMapobject(chr)), false);
                MaplePet[] pets = chr.getPets();
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        broadcastMessage(chr, PetPacket.showPet(chr, pets[i], false, false), false);
                    }
                }
                if (chr.getChalkboard() != null) {
                    broadcastMessage(MaplePacketCreator.useChalkboard(chr, false));
                }
            } else {
                broadcastGMMessage(chr, (MaplePacketCreator.spawnPlayerMapobject(chr)), false);
                MaplePet[] pets = chr.getPets();
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        broadcastGMMessage(chr, PetPacket.showPet(chr, pets[i], false, false), false);
                    }
                }
                if (chr.getChalkboard() != null) {
                    broadcastGMMessage(MaplePacketCreator.useChalkboard(chr, false));
                }
            }
            if (isAriantPQMap()) {
                broadcastMessage(MaplePacketCreator.updateAriantPQRanking(chr.getName(), chr.getAriantScore(), false));
            }
            sendObjectPlacement(chr.getClient());
            chr.getClient().getSession().write(MaplePacketCreator.spawnPlayerMapobject(chr));
            if (this.getId() == 1 || this.getId() == 2 || this.getId() == 809000101 || this.getId() == 809000201) {
                chr.getClient().getSession().write(MaplePacketCreator.showEquipEffect());
            }
            MaplePet[] pets = chr.getPets();
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    chr.getClient().getSession().write(PetPacket.showPet(chr, pets[i], false, false));
                }
            }
            this.mapobjects.put(Integer.valueOf(chr.getObjectId()), chr);
        }

        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT id FROM playernpcs WHERE map = ?");
            ps.setInt(1, chr.getMapId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chr.getClient().getSession().write(MaplePacketCreator.getPlayerNPC(rs.getInt("id")));
            }
            rs.close();
            ps.close();
        } catch (SQLException se) {
            //Dont swallow please
        }
        final MapleStatEffect stat = chr.getStatForBuff(MapleBuffStat.SUMMON);
	if (stat != null) {
	    final MapleSummon summon = chr.getSummons().get(stat.getSourceId());
	    summon.setPosition(chr.getPosition());
	    chr.addVisibleMapObject(summon);
	    this.spawnSummon(summon);
	}
        if (mapEffect != null) {
            mapEffect.sendStartData(chr.getClient());
        }
        if (mapTimer != null) {
            mapTimer.sendSpawnData(chr.getClient());
        }
        if (getTimeLimit() > 0 && getForcedReturnMap() != null) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock(getTimeLimit()));
            chr.startMapTimeLimitTask(this, this.getForcedReturnMap());
        }
        if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted()) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000)));
        }

        if (chr.getMonsterCarnival() != null) {
            chr.getClient().getSession().write(MaplePacketCreator.getClock(chr.getMonsterCarnival().getTimeLeftSeconds()));
        }

        if (hasClock()) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);
            chr.getClient().getSession().write((MaplePacketCreator.getClockTime(hour, min, second)));
        }

        if (hasBoat() == 2) {
            chr.getClient().getSession().write((MaplePacketCreator.boatPacketFree((byte) 12, (byte) 6)));
        } else if (hasBoat() == 1 && (chr.getMapId() != 200090000 || chr.getMapId() != 200090010)) {
            chr.getClient().getSession().write(MaplePacketCreator.boatPacketFree((byte) 8, (byte) 2));
        }
        if (this.isQuestMapa()){
         /*          LudiPQ Itens            */
           chr.removeAll(4001022, true); 
           chr.removeAll(4001023, true);
           /*         KerningPQ Itens          */
           chr.removeAll(4001007, true); 
           chr.removeAll(4001008, true);
           /*            HenesysPQ             */
           chr.removeAll(4001101, true);
           chr.removeAll(4001095, true);  
           chr.removeAll(4001096, true);        
           chr.removeAll(4001097, true);        
           chr.removeAll(4001098, true);        
           chr.removeAll(4001099, true);        
           chr.removeAll(4001100, true); 
           /*             AriantPQ             */
           chr.removeAll(2022266, true);
           chr.removeAll(2022267, true);
           chr.removeAll(2022269, true);
           chr.removeAll(2270002, true);
           chr.removeAll(2100067, true);
           chr.removeAll(4031868, true);
           /*              OrbisPQ             */
           chr.removeAll(4001045, true);
           chr.removeAll(4001046, true);
           chr.removeAll(4001047, true);
           chr.removeAll(4001048, true);
           chr.removeAll(4001049, true);
           chr.removeAll(4001050, true);
           chr.removeAll(4001051, true);
           chr.removeAll(4001052, true);
           chr.removeAll(4001053, true);
           chr.removeAll(4001054, true);
           chr.removeAll(4001055, true);
           chr.removeAll(4001056, true);
           chr.removeAll(4001057, true);
           chr.removeAll(4001058, true);
           chr.removeAll(4001059, true);
           chr.removeAll(4001060, true);
           chr.removeAll(4001061, true);
           chr.removeAll(4001062, true);
           chr.removeAll(4001063, true);
           /*              LudiMaze            */
            chr.removeAll(4001106, true);
            /*             PiratePQ            */
            chr.removeAll(4001120, true);
            chr.removeAll(4001121, true);
            chr.removeAll(4001122, true);
           /*             PiratePQ            */
            chr.removeAll(1032033, true);
            chr.removeAll(4001024, true);
            chr.removeAll(4001025, true);
            chr.removeAll(4001026, true);
            chr.removeAll(4001027, true);
            chr.removeAll(4001028, true);
            chr.removeAll(4001031, true);
            chr.removeAll(4001032, true);
            chr.removeAll(4001033, true);
            chr.removeAll(4001034, true);
            chr.removeAll(4001035, true);
            chr.removeAll(4001037, true);
            }
            chr.receivePartyMemberHP();
    }
       	       
        public boolean isQuestMapa() {
		switch (this.getId()) {
			case 922010100: // LudiPQ
			case 103000800: // KerningPQ
                        case 920010000: // OrbisPQ    
                        case 910010000: // HenesysPQ
                        case 980010101: // AriantPQ (1)
                        case 980010201: // AriantPQ (2) 
                        case 980010301: // AriantPQ (3) 
                        case 809050000: // LudiMaze
                        case 925100000: // PiratePQ   
                        case 990000000: // GPQ    
			return true;
		}
		return false;
	}
	

        
         public void startMapEffect(String msg, int itemId, long time) {
        if (mapEffect != null) {
            return;
        }
        mapEffect = new MapleMapEffect(msg, itemId);
        broadcastMessage(mapEffect.makeStartData());
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, time);
    }
         
   public MapleReactor getReactorById(int Id) {
        synchronized (mapobjects) {
            for (MapleMapObject obj : mapobjects.values()) {
                if (obj.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) obj).getId() == Id) {
                        return (MapleReactor) obj;
                    }
                }
            }
            return null;
        }
    }

         

       public void removePlayer(MapleCharacter chr) {
        //log.warn("[dc] [level2] Player {} leaves map {}", new Object[] { chr.getName(), mapid });
        synchronized (characters) {
            characters.remove(chr);
        }
        removeMapObject(Integer.valueOf(chr.getObjectId()));
        if (!chr.isHidden()) {
            broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        } else {
            broadcastGMMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        }
        for (MapleMapObject o : this.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster monster = (MapleMonster) o;
            if (monster.getTarget() != null) {
                if (monster.isRandom()) {
                    if (monster.getTarget().getId() == chr.getId()) {
                        monster.destroyRandomEventMob();
                        continue;
                    }
                }
            }
        }

        for (MapleMonster monster : chr.getControlledMonsters()) {
            monster.setController(null);
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
            updateMonsterController(monster);
        }
        chr.leaveMap();
        chr.cancelMapTimeLimitTask();

        for (MapleSummon summon : chr.getSummons().values()) {
            if (summon.isPuppet()) {
                chr.cancelBuffStats(MapleBuffStat.PUPPET);
            } else {
                removeMapObject(summon);
            }
        }
        List<MapleSummon> removes = new LinkedList<MapleSummon>();
        for (MapleSummon summon : chr.getPirateSummons()) {
            removeMapObject(summon);
            if (summon.isOctopus()) {
                removes.add(summon);
            }
        }
        for (MapleSummon summon : removes) {
            chr.removePirateSummon(summon);
        }
    }


                
    public void broadcastGMMessage(MaplePacket packet) {
        broadcastGMMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

	/**
	 * Broadcasts the given packet to everyone on the map but the source. source = null Broadcasts to everyone
	 * 
	 * @param source
	 * @param packet
	 */
     public void broadcastMessage(MapleCharacter source, MaplePacket packet) {
     synchronized (characters) {
     for (MapleCharacter chr : characters) {
     if (chr != source) {
     chr.getClient().getSession().write(packet);
     }
     }
     }
     }
	/**
	 * Broadcast a message to everyone in the map
	 * 
	 * @param packet
	 */
    public void broadcastMessage(MaplePacket packet) {
        broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }

    /**
     * Nonranged. Repeat to source according to parameter.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Ranged and repeat according to parameters.
     *
     * @param source
     * @param packet
     * @param repeatToSource
     * @param ranged
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, boolean repeatToSource, boolean ranged) {
        broadcastMessage(repeatToSource ? null : source, packet, ranged ? 722500 : Double.POSITIVE_INFINITY, source.getPosition());
    }

    /**
     * Always ranged from Point.
     *
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MaplePacket packet, Point rangedFrom) {
        broadcastMessage(null, packet, 722500, rangedFrom);
    }

    /**
     * Always ranged from point. Does not repeat to source.
     *
     * @param source
     * @param packet
     * @param rangedFrom
     */
    public void broadcastMessage(MapleCharacter source, MaplePacket packet, Point rangedFrom) {
        broadcastMessage(source, packet, 722500, rangedFrom);
    }

        
        
  private void broadcastMessage(MapleCharacter source, MaplePacket packet, double rangeSq, Point rangedFrom) {
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                if (chr != source) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        }
    }

   public int countMonster(int id) {
        int count = 0;
        for (MapleMapObject m : getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER))) {
            MapleMonster mob = (MapleMonster) m;
            if (mob.getId() == id) {
                count++;
            }
        }
        return count;
    }
	private boolean isNonRangedType(MapleMapObjectType type) {
		switch (type) {
			case NPC:
			case PLAYER:
			case MIST:
			case PLAYER_NPC_MERCHANT:
			case HIRED_MERCHANT:
				//case REACTOR:
				return true;
		}
		return false;
	}
        
        
     public final List<MapleMapObject> getAllMonster() {
	return getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
    }
        
      private void sendObjectPlacement(MapleClient mapleClient) {
        for (MapleMapObject o : mapobjects.values()) {
            if (isNonRangedType(o.getType())) {
                o.sendSpawnData(mapleClient);
            } else if (o.getType() == MapleMapObjectType.MONSTER) {
                updateMonsterController((MapleMonster) o);
            }
        }
        MapleCharacter chr = mapleClient.getPlayer();
        if (chr != null) {
            for (MapleMapObject o : getMapObjectsInRange(chr.getPosition(), 722500, rangedMapobjectTypes)) {
                if (o.getType() == MapleMapObjectType.REACTOR) {
                    if (((MapleReactor) o).isAlive()) {
                        o.sendSpawnData(chr.getClient());
                        chr.addVisibleMapObject(o);
                    }
                } else {
                    o.sendSpawnData(chr.getClient());
                    chr.addVisibleMapObject(o);
                }
            }
        }
    }

    public List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (from.distanceSq(l.getPosition()) <= rangeSq) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

   public List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> types) {
        List<MapleMapObject> ret = new LinkedList<MapleMapObject>();
        synchronized (mapobjects) {
            for (MapleMapObject l : mapobjects.values()) {
                if (types.contains(l.getType())) {
                    if (box.contains(l.getPosition())) {
                        ret.add(l);
                    }
                }
            }
        }
        return ret;
    }

    public List<MapleCharacter> getPlayersInRect(Rectangle box, List<MapleCharacter> chr) {
        List<MapleCharacter> character = new LinkedList<MapleCharacter>();
        synchronized (characters) {
            for (MapleCharacter a : characters) {
                if (chr.contains(a.getClient().getPlayer())) {
                    if (box.contains(a.getPosition())) {
                        character.add(a);
                    }
                }
            }
        }
        return character;
    }

	public void addPortal(MaplePortal myPortal) {
		portals.put(myPortal.getId(), myPortal);
	}

	public MaplePortal getPortal(String portalname) {
		for (MaplePortal port : portals.values()) {
			if (port.getName().equals(portalname)) {
				return port;
			}
		}
		return null;
	}

	public MaplePortal getPortal(int portalid) {
		return portals.get(portalid);
	}

	public void addMapleArea(Rectangle rec) {
		areas.add(rec);
	}

	public List<Rectangle> getAreas() {
		return new ArrayList<Rectangle>(areas);
	}

	public Rectangle getArea(int index) {
		return areas.get(index);
	}

	public void setFootholds(MapleFootholdTree footholds) {
		this.footholds = footholds;
	}

	public MapleFootholdTree getFootholds() {
		return footholds;
	}

	/**
	 * not threadsafe, please synchronize yourself
	 * 
	 * @param monster
	 */


	public Point getRandomSP(int team) {
		if (takenSpawns.size() > 0) {
			for (SpawnPoint sp : monsterSpawn) {
				for (Point pt : takenSpawns) {
					if ((sp.getPosition().x == pt.x && sp.getPosition().y == pt.y) || (sp.getTeam() != team && !this.isBlueCPQMap())) {
						continue;
					} else {
						takenSpawns.add(pt);
						return sp.getPosition();
					}
				}
			}
		} else {
			for (SpawnPoint sp : monsterSpawn) {
				if (sp.getTeam() == team || this.isBlueCPQMap()) {
					takenSpawns.add(sp.getPosition());
					return sp.getPosition();
				}
			}
		}
		return null;
	}
	
	public GuardianSpawnPoint getRandomGuardianSpawn(int team) {
		boolean alltaken = false;
		for (GuardianSpawnPoint a : this.guardianSpawns) {
			if (!a.isTaken()) {
				alltaken = false;
				break;
			}
		}
		if (alltaken) return null;
		if (this.guardianSpawns.size() > 0) {
			while (true) {
				for (GuardianSpawnPoint gsp : this.guardianSpawns) {
					if (!gsp.isTaken() && Math.random() < 0.3
							&& (gsp.getTeam() == -1 || gsp.getTeam() == team)) {
						return gsp;
					}
				}
			}
		}
		return null;
	}
	
	public void addGuardianSpawnPoint(GuardianSpawnPoint a) {
		this.guardianSpawns.add(a);
	}

	public float getMonsterRate() {
		return monsterRate;
	}

	public Collection<MapleCharacter> getCharacters() {
        return Collections.unmodifiableCollection(this.characters);
    }

    public MapleCharacter getCharacterById(int id) {
        for (MapleCharacter c : this.characters) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }


	private final void updateMapObjectVisibility(final MapleCharacter chr, final MapleMapObject mo) {
	if (!chr.isMapObjectVisible(mo)) { // monster entered view range
	    if (mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq(chr.getPosition()) <= Configuration.maxViewRangeSq()) {
		chr.addVisibleMapObject(mo);
		mo.sendSpawnData(chr.getClient());
	    }
	} else { // monster left view range
	    if (mo.getType() != MapleMapObjectType.SUMMON && mo.getPosition().distanceSq(chr.getPosition()) > Configuration.maxViewRangeSq()) {
		chr.removeVisibleMapObject(mo);
		mo.sendDestroyData(chr.getClient());
	    }
	}
    }

	public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);
        synchronized (characters) {
            for (MapleCharacter chr : characters) {
                updateMapObjectVisibility(chr, monster);
            }
        }
    }

  public void movePlayer(MapleCharacter player, Point newPosition) {
        player.setPosition(newPosition);
         if(player.isFake())
            return;
        Collection<MapleMapObject> visibleObjects = player.getVisibleMapObjects();
        MapleMapObject[] visibleObjectsNow = visibleObjects.toArray(new MapleMapObject[visibleObjects.size()]);
        try {
            for (MapleMapObject mo : visibleObjectsNow) {
                if (mo != null) {
                    if (mapobjects.get(mo.getObjectId()) == mo) {
                        updateMapObjectVisibility(player, mo);
                    } else {
                        player.removeVisibleMapObject(mo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (MapleMapObject mo : getMapObjectsInRange(player.getPosition(), 722500, rangedMapobjectTypes)) {
            if (!player.isMapObjectVisible(mo)) {
                mo.sendSpawnData(player.getClient());
                player.addVisibleMapObject(mo);
            }
        }
    }
         
	public MaplePortal findClosestSpawnpoint(Point from) {
		MaplePortal closest = null;
		double shortestDistance = Double.POSITIVE_INFINITY;
		for (MaplePortal portal : portals.values()) {
			double distance = portal.getPosition().distanceSq(from);
			if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
				closest = portal;
				shortestDistance = distance;
			}
		}
		return closest;
	}

	public void spawnDebug(MessageCallback mc) {
		mc.dropMessage("Spawndebug...");
		synchronized (mapobjects) {
			mc.dropMessage("Mapobjects in map: " + mapobjects.size() + " \"spawnedMonstersOnMap\": " +
				spawnedMonstersOnMap + " spawnpoints: " + monsterSpawn.size() +
				" maxRegularSpawn: " + getMaxRegularSpawn());
			int numMonsters = 0;
			for (MapleMapObject mo : mapobjects.values()) {
				if (mo instanceof MapleMonster) {
					numMonsters++;
				}
			}
			mc.dropMessage("actual monsters: " + numMonsters);
		}
	}

	private int getMaxRegularSpawn() {
		return (int) (monsterSpawn.size() / monsterRate);
	}

	public Collection<MaplePortal> getPortals() {
		return Collections.unmodifiableCollection(portals.values());
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setClock(boolean hasClock) {
		this.clock = hasClock;
	}

	public boolean hasClock() {
		return clock;
	}

	public void setTown(boolean isTown) {
		this.town = isTown;
	}

	public boolean isTown() {
		return town;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public void setEverlast(boolean everlast) {
		this.everlast = everlast;
	}

	public boolean getEverlast() {
		return everlast;
	}

	public int getSpawnedMonstersOnMap() {
		return spawnedMonstersOnMap.get();
	}

	private class ExpireMapItemJob implements Runnable {

		private MapleMapItem mapitem;

		public ExpireMapItemJob(MapleMapItem mapitem) {
			this.mapitem = mapitem;
		}

		@Override
		public void run() {
			if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
				synchronized (mapitem) {
					if (mapitem.isPickedUp()) {
						return;
					}
					MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0),
						mapitem.getPosition());
					MapleMap.this.removeMapObject(mapitem);
					mapitem.setPickedUp(true);
				}
			}
                }
        }

	private class ActivateItemReactor implements Runnable {

	private MapleMapItem mapitem;
	private MapleReactor reactor;
	private MapleClient c;

	public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
	    this.mapitem = mapitem;
	    this.reactor = reactor;
	    this.c = c;
	}

	@Override
	public void run() {
	    if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
		if (mapitem.isPickedUp()) {
		    reactor.setTimerActive(false);
		    return;
		}
		mapitem.setPickedUp(true);

		broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0));
		removeMapObject(mapitem);

		reactor.hitReactor(c);
		reactor.setTimerActive(false);

		if (reactor.getDelay() > 0) {
		    MapTimer.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
			    reactor.setState((byte) 0);
			    broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
			}
		    }, reactor.getDelay());
		}
	    }
	}
    }


	private static interface DelayedPacketCreation {

		void sendPackets(MapleClient c);
	}

	private static interface SpawnCondition {

		boolean canSpawn(MapleCharacter chr);
	}

	public int getHPDec() {
		return decHP;
	}

	public void setHPDec(int delta) {
		decHP = delta;
	}

	public int getHPDecProtect() {
		return this.protectItem;
	}

	public void setHPDecProtect(int delta) {
		this.protectItem = delta;
	}

        
     public int hasBoat() {
        if (boat && docked) {
            return 2;
        } else if (boat) {
            return 1;
        } else {
            return 0;
        }
    }

    public void setBoat(boolean hasBoat) {
        this.boat = hasBoat;
    }

    public void setDocked(boolean isDocked) {
        this.docked = isDocked;
        broadcastMessage(MaplePacketCreator.boatPacketFree((byte) 12, (byte) 6));
    }

    public void notDocked(boolean isDocked) {
        this.docked = isDocked;
        broadcastMessage(MaplePacketCreator.boatPacketFree((byte) 8, (byte) 2));
    }

    public int hasTrain() {
        if (train && trainStopped) {
            return 2;
        } else if (train) {
            return 1;
        }
        return 0;
    }

//    public void setBoat(boolean hasBoat) {
//        this.boat = hasBoat;
//    }

    public void setTrain(boolean hasTrain) {
        this.train = hasTrain;
    }

//    public void setDocked(boolean isDocked) {
//        this.docked = isDocked;
//        broadcastMessage(MaplePacketCreator.boatPacket(isDocked));
//    }

    public void setTrainStopped(boolean isStopped) {
        this.trainStopped = isStopped;
        broadcastMessage(MaplePacketCreator.trainPacket(isStopped));
    }
	
	public void buffMonsters(int team, MonsterStatus status) {
		if (team == 0) {
			redTeamBuffs.add(status);
		} else if (team == 1) {
			blueTeamBuffs.add(status);
		}
		for (MapleMapObject mmo : this.mapobjects.values()) {
			if (mmo.getType() == MapleMapObjectType.MONSTER) {
				MapleMonster mob = (MapleMonster) mmo;
				if (mob.getTeam() == team) {
					int skillID = getSkillId(status);
					if (skillID != -1) {
						MobSkill skill = getMobSkill(skillID, this.getSkillLevel(status));
						mob.applyMonsterBuff(status, skill.getX(), skill.getSkillId(), 
								1000 * 60 * 10, skill);
					}
				}
			}
		}
	}
	
	public void removeStatus(MonsterStatus status, int team) {
		List<MonsterStatus> a = null;
		if (team == 0) a = redTeamBuffs;
		else if (team == 1) a = blueTeamBuffs;
		List<MonsterStatus> r = new LinkedList<MonsterStatus>();
		for (MonsterStatus ms : a) {
			if (ms.equals(status)) {
				r.add(ms);
			}
		}
		for (MonsterStatus al : r) {
			if (a.contains(al)) {
				a.remove(al);
			}
		}
	}
	
	
	public boolean isCPQWinnerMap() {
		switch (this.getId()) {
			case 980000103:
			case 980000203:
			case 980000303:
			case 980000403:
			case 980000503:
			case 980000603:
				return true;
		}
		return false;
	}
	
    public boolean isCPQLoserMap() {
		switch (this.getId()) {
			case 980000104:
			case 980000204:
			case 980000304:
			case 980000404:
			case 980000504:
			case 980000604:
				return true;
		}
		return false;
	}
	
	public void debuffMonsters(int team, MonsterStatus status) {
		if (team == 0) {
			removeStatus(status, team);
		} else if (team == 1) {
			removeStatus(status, team);
		}
		for (MapleMapObject mmo : this.mapobjects.values()) {
			if (mmo.getType() == MapleMapObjectType.MONSTER) {
				MapleMonster mob = (MapleMonster) mmo;
				if (mob.getTeam() == team) {
					int skillID = getSkillId(status);
					if (skillID != -1) {
						if (mob.getMonsterBuffs().contains(status)) {
							mob.cancelMonsterBuff(status);
						}
					}
				}
			}
		}
	}
	
	public void mapMessage(int type, String message) {
		broadcastMessage(MaplePacketCreator.serverNotice(type, message));
	}
	
	public void mapMessage() {
		
	}
	
	public void removeCPQSpawns() {
		List<SpawnPoint> remove = new LinkedList<SpawnPoint>();
		for (SpawnPoint sp : this.monsterSpawn) {
			if (sp.isTemporary()) {
				remove.add(sp);
			}
		}
		for (SpawnPoint sp : remove) {
			this.monsterSpawn.remove(sp);
		}
		this.takenSpawns.clear();
		List<MapleMapObject> removeObjects = new LinkedList<MapleMapObject>();
		for (MapleMapObject o : this.mapobjects.values()) {
			if (o.getType() == MapleMapObjectType.REACTOR) {
				removeObjects.add(o);
			}
		}
		for (MapleMapObject o : removeObjects) {
			removeMapObject(o);
		}
	}
	
	public void removeItems() {
		MapleMap map = this;
		double               range = Double.POSITIVE_INFINITY;
		List<MapleMapObject> items = map.getMapObjectsInRange(new Point(0, 0), range,
										 Arrays.asList(MapleMapObjectType.ITEM));

		for (MapleMapObject itemmo : items) {
			map.removeMapObject(itemmo);
		}
	}
	
	public MapleMonster findClosestMonster(Point from, double range) {
		MapleMonster closest = null;
		double shortestDistance = range;
                List<MapleMapObject> monstersi = this.getMapObjectsInRange(from, shortestDistance, Arrays
				.asList(MapleMapObjectType.MONSTER));
			for (MapleMapObject monstermo : monstersi) {
				MapleMonster mob = (MapleMonster) monstermo;
				double distance = mob.getPosition().distanceSq(from);
				if (distance < shortestDistance && mob.getId() != 9300061) {
					closest = mob;
					shortestDistance = distance;
				}
			}
		return closest;
	}
	
	public MobSkill getMobSkill(int skillId, int level) {
		return MobSkillFactory.getMobSkill(skillId, level);
	}
	
	public int getSkillId(MonsterStatus status) {
		if (status == MonsterStatus.WEAPON_ATTACK_UP) {
			return 100;
		} else if (status.equals(MonsterStatus.MAGIC_ATTACK_UP)) {
			return 101;
		} else if (status.equals(MonsterStatus.WEAPON_DEFENSE_UP)) {
			return 112;
		} else if (status.equals(MonsterStatus.MAGIC_DEFENSE_UP)) {
			return 113;
		} else if (status.equals(MonsterStatus.WEAPON_IMMUNITY)) {
			return 140;
		} else if (status.equals(MonsterStatus.MAGIC_IMMUNITY)) {
			return 141;
		}
		return -1;
	}
	
	public int getSkillLevel(MonsterStatus status) {
		if (status == MonsterStatus.WEAPON_ATTACK_UP) {
			return 1;
		} else if (status.equals(MonsterStatus.MAGIC_ATTACK_UP)) {
			return 1;
		} else if (status.equals(MonsterStatus.WEAPON_DEFENSE_UP)) {
			return 1;
		} else if (status.equals(MonsterStatus.MAGIC_DEFENSE_UP)) {
			return 1;
		} else if (status.equals(MonsterStatus.WEAPON_IMMUNITY)) {
			return 10;
		} else if (status.equals(MonsterStatus.MAGIC_IMMUNITY)) {
			return 9;
		}
		return -1;
	}
	
	public void clearBuffList() {
		redTeamBuffs.clear();
		blueTeamBuffs.clear();
	}
        
	public boolean hasMapleTV() {
		int tvIds[] = {9250042, 9250043, 9250045, 9250044, 9270001, 9270002, 9250023, 9250024, 9270003, 9270004, 9250026, 9270006, 9270007, 9250046, 9270000, 9201066};
		for (int id : tvIds) {
			if (this.containsNPC(id)) {
			return true;
			}
		}
		return false;
	}
		
        
	private final class ActivateItemReactorPlus implements Runnable {
		private MapleMapItem mapitem;
		private MapleReactor reactor;
		private MapleClient c;
                private int stated;

		public ActivateItemReactorPlus(MapleMapItem mapitem, MapleReactor reactor, MapleClient c, int stat) {
			this.mapitem = mapitem;
			this.reactor = reactor;
			this.c = c;
                        this.stated = stat;
		}

		@Override
		public void run() {
			if (mapitem != null && mapitem == getMapObject(mapitem.getObjectId())) {
				synchronized (mapitem) {
					TimerManager tMan = TimerManager.getInstance();
					if (mapitem.isPickedUp())
						return;
					MapleMap.this.broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 0, 0),
						mapitem.getPosition());
					MapleMap.this.removeMapObject(mapitem);
					reactor.hitReactor(c);
					if (reactor.getDelay() > 0){
						tMan.schedule(new Runnable() {
							@Override
							public void run() {
								reactor.setState((byte)0);
								broadcastMessage(MaplePacketCreator.triggerReactor(reactor, 0));
                                                                reactor.setMode(stated);
							}
						}, reactor.getDelay());
					}
					reactor.setMode(stated);
				}
			}
		}
	}

    private final class warpAll implements Runnable {
		private MapleMap toGo;
                private MapleMap from;

		public warpAll(MapleMap toGoto, MapleMap from) {
			this.toGo = toGoto;
			this.from = from;
		}

		@Override
		public void run() {
            synchronized (toGo) {
                for (MapleCharacter ppp : characters) {
                    if (ppp.getMap().equals(from)){
                        ppp.changeMap(toGo, toGo.getPortal(0));
                        if (ppp.getEventInstance() != null) {
                            ppp.getEventInstance().unregisterPlayer(ppp);
                        }
                    }
                }
            }
		}
	}
    public void warpAllToNearestTown(String reason)
    {
        this.broadcastMessage(MaplePacketCreator.serverNotice(5, reason));
        int rid = this.forcedReturnMap == 999999999 ? this.returnMapId : this.forcedReturnMap;
        new warpAll(ChannelServer.getInstance(this.channel).getMapFactory().getMap(rid), this).run();
    }
    public void dcAllPlayers()
    {

        int rid = this.forcedReturnMap == 999999999 ? this.returnMapId : this.forcedReturnMap;
        new warpAll(ChannelServer.getInstance(this.channel).getMapFactory().getMap(rid), this).run();
    }

    public void warpAllToCashShop(String reason)
    {
        MaplePacket x = MaplePacketCreator.serverNotice(1, reason);
        for(MapleCharacter mc : getCharacters())
        {
           
            mc.warpToCashShop();
             mc.getClient().getSession().write(x);
        }
        
    }
//InvincibilitySkills
    public boolean setPortalDisable(boolean v)
    {
        this.disablePortal = v;
        return disablePortal;
    }

    public boolean getPortalDisable()
    {
        return this.disablePortal;
    }
    public boolean setDisableInvincibilitySkills(boolean v)
    {
        this.disableInvincibilitySkills = v;
        return disableInvincibilitySkills;
    }

    public boolean getDisableInvincibilitySkills()
    {
        return this.disableInvincibilitySkills;
    }

    public boolean setDisableDamage(boolean v)
    {
        this.disableDamage = v;
        return disableDamage;
    }

    public boolean getDisableDamage()
    {
        return this.disableDamage;
    }

    public boolean setDisableChat(boolean v)
    {
        this.disableChat = v;
        return disableChat;
    }

    public boolean getDisableChat()
    {
        return this.disableChat;
    }

    public boolean isSwim() {
        return swim;
    }

    public void setSwim(boolean swim) {
        this.swim = swim;
    }

	public MapleOxQuiz getOx() {
		return ox;
	}

	public void setOx(MapleOxQuiz ox) {
		this.ox = ox;
	}
}
