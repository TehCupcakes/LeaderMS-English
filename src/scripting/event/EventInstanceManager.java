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
package scripting.event;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.script.ScriptException;

import client.MapleCharacter;
import config.configuration.Configuration;
import database.DatabaseConnection;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import provider.MapleDataProviderFactory;
import server.MapleSquad;
import server.MapleSquadType;
import server.TimerManager;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import tools.FilePrinter;

/**
 *
 * @author Matze
 */
public class EventInstanceManager {

    private List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
    private List<MapleMonster> mobs = new LinkedList<MapleMonster>();
    private Map<MapleCharacter, Integer> killCount = new HashMap<MapleCharacter, Integer>();
    private Map<Integer, Integer> killCounter = new HashMap<Integer, Integer>(); //teste
    private List<Boolean> isInstanced = new LinkedList<Boolean>();
    private EventManager em;
    private List<Integer> mapIds = new LinkedList<Integer>();
    private List<Integer> dced = new LinkedList<Integer>();
    private MapleMapFactory mapFactory;
    private String name;
    private int channel;
    private Properties props = new Properties();
    private long timeStarted = 0;
    private long eventTime = 0;
    private boolean disposed = false;
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock rL = mutex.readLock(), wL = mutex.writeLock();
    private Map<Integer, MapleMap> instanceMaps = new HashMap<Integer, MapleMap>();

    public EventInstanceManager(EventManager em, String name) {
        this.em = em;
        this.name = name;
        mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")));
        mapFactory.setChannel(em.getChannelServer().getChannel());
    }
    
    public EventInstanceManager(EventManager em, String name, int channel) {
        this.em = em;
        this.name = name;
        this.channel = channel;
    }

   public void registerPlayer(MapleCharacter chr) {
        if (disposed || chr == null) {
            return;
        }
        try {
            wL.lock();
            try {
                chars.add(chr);
            } finally {
                wL.unlock();
            }
            chr.setEventInstance(this);
            em.getIv().invokeFunction("playerEntry", this, chr);
        } catch (NullPointerException ex) {
            FilePrinter.printError(FilePrinter.ScriptEx_Log, ex);
            ex.printStackTrace();
        } catch (Exception ex) {
             FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerEntry:\n" + ex);
            System.out.println("Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerEntry:\n" + ex);
        }
    }

       public void saveAllBossQuestPoints(int bossPoints) { 
        for (MapleCharacter character : chars) { 
            int points = character.getBossPoints(); 
            character.setBossPoints(points + bossPoints);
        }
    } 
    public void saveBossQuestPoints(int bossPoints, MapleCharacter character) { 
        int points = character.getBossPoints(); 
        character.setBossPoints(points + bossPoints);              
    }

    public void startEventTimer(long time) {
        timeStarted = System.currentTimeMillis();
        eventTime = time;
    }

    public boolean isTimerStarted() {
        return eventTime > 0 && timeStarted > 0;
    }

    public long getTimeLeft() {
        return eventTime - (System.currentTimeMillis() - timeStarted);
    }
    
   
	public void registerParty(MapleParty party, MapleMap map) {
		for (MaplePartyCharacter pc : party.getMembers()) {
			MapleCharacter c = map.getCharacterById(pc.getId());
			registerPlayer(c);
		}
	}

    public void unregisterPlayer(MapleCharacter chr) {
        chars.remove(chr);
        chr.setEventInstance(null);
    }

    public int getPlayerCount() {
        return chars.size();
    }
     
        public int getMonsterCount() {
		return mobs.size();
	}
        
        public void addMapInstance(int mapId, MapleMap map) {
        instanceMaps.put(mapId, map);
        }
        
      public List<MapleCharacter> getPlayers() {
        if (disposed) {
            return Collections.emptyList();
        }
        rL.lock();
        try {
            return new LinkedList<MapleCharacter>(chars);
        } finally {
            rL.unlock();
        }
    }
     
    public void registerMonster(MapleMonster mob) {
        mobs.add(mob);
        mob.setEventInstance(this);
    }

    public void unregisterMonster(MapleMonster mob) {
        mob.setEventInstance(null);
        if (disposed) {
            return;
        }
        if (mobs.contains(mob)) {
            mobs.remove(mob);
        }
        if (mobs.size() == 0) {
            try {
                em.getIv().invokeFunction("allMonstersDead", this);
            } catch (Exception ex) {
                FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : allMonstersDead:\n" + ex);
                System.out.println("Event name" + em.getName() + ", Instance name : " + name + ", method Name : allMonstersDead:\n" + ex);
            }
        }
    }

	 public void playerKilled(MapleCharacter chr) {
        if (disposed) {
            return;
        }
        try {
            em.getIv().invokeFunction("playerDead", this, chr);
        } catch (Exception ex) {
            FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerDead:\n" + ex);
            System.out.println("Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerDead:\n" + ex);
        }
    }

        public boolean revivePlayer(MapleCharacter chr) {
        if (disposed) {
            return false;
        }
        try {
            Object b = em.getIv().invokeFunction("playerRevive", this, chr);
            if (b instanceof Boolean) {
                return (Boolean) b;
            }
        } catch (Exception ex) {
            FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerRevive:\n" + ex);
            System.out.println("Event name" + em.getName() + ", Instance name : " + name + ", method Name : playerRevive:\n" + ex);
        }
        return true;
    }

	public void playerDisconnected(MapleCharacter chr) {
        try {
            em.getIv().invokeFunction("playerDisconnected", this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }
        
        
        
        public MapleMap getMapInstance(int mapId, boolean empty) {
        MapleMap map = null;
        if (empty) {
            map = mapFactory.getMap(mapId, false, false);
        } else {
            map = mapFactory.getMap(mapId);
        }
        if (!mapFactory.isMapLoaded(mapId)) {
            if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true")) {
                map.shuffleReactors();
            }
        }
        return map;
    }
        
	/**
	 * 
	 * @param chr
	 * @param mob
	 */
	    public void monsterKilled(final MapleCharacter chr, final MapleMonster mob) {
        if (disposed) {
            return;
        }
        try {
            int inc = ((Double) em.getIv().invokeFunction("monsterValue", this, mob.getId())).intValue();
            if (disposed || chr == null) {
                return;
            }
            Integer kc = killCounter.get(chr.getId());
            if (kc == null) {
                kc = inc;
            } else {
                kc += inc;
            }
            killCounter.put(chr.getId(), kc);
        } catch (ScriptException ex) {
            System.out.println("Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
             FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
        } catch (NoSuchMethodException ex) {
            System.out.println("Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
             FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name" + (em == null ? "null" : em.getName()) + ", Instance name : " + name + ", method Name : monsterValue:\n" + ex);
        } catch (Exception ex) {
            ex.printStackTrace();
             FilePrinter.printError(FilePrinter.ScriptEx_Log, ex);
        }
    }

	 public int getKillCount(MapleCharacter chr) {
        if (disposed) {
            return 0;
        }
        Integer kc = killCounter.get(chr.getId());
        if (kc == null) {
            return 0;
        } else {
            return kc;
        }
    }


	public void dispose() {
        chars.clear();
        mobs.clear();
        killCounter.clear();
        mapFactory = null;
        em.disposeInstance(name);
        em = null;
    }

    public MapleMapFactory getMapFactory() {
        return mapFactory;
    }

	 public void schedule(final String methodName, long delay) {
           TimerManager.getInstance().schedule(new Runnable() {
            public void run() {
                try {
                    em.getIv().invokeFunction(methodName, EventInstanceManager.this);
                } catch (NullPointerException npe) {
                } catch (ScriptException ex) {
                    System.out.println("Error: " + em.getName());
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    System.out.println("Error: " + em.getName());
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, delay);
    }

	  public String getName() {
        return name;
    }

	public void saveWinner(MapleCharacter chr) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO eventstats (event, instance, characterid, channel) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, em.getName());
                ps.setString(2, getName());
                ps.setInt(3, chr.getId());
                ps.setInt(4, chr.getClient().getChannel());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

   public MapleMap getMapInstance(int mapId) {
        boolean wasLoaded = mapFactory.isMapLoaded(mapId);
        MapleMap map = mapFactory.getMap(mapId);
        if (!wasLoaded)
            if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true"))
                map.shuffleReactors();
        return map;
    }
   
   public void dispose_NoLock() {
        if (disposed || em == null) {
            return;
        }
        final String emN = em.getName();
        try {

            disposed = true;
            for (MapleCharacter chr : chars) {
                chr.setEventInstance(null);
            }
            chars.clear();
            chars = null;
            if (mobs.size() >= 1) {
                for (MapleMonster mob : mobs) {
                    if (mob != null) {
                        mob.setEventInstance(null);
                    }
                }
            }
            mobs.clear();
            mobs = null;
            killCount.clear();
            killCount = null;
            dced.clear();
            dced = null;
            timeStarted = 0;
            eventTime = 0;
            props.clear();
            props = null;
//            for (int i = 0; i < mapIds.size(); i++) {
//                if (isInstanced.get(i)) {
//                    this.getMapFactory().removeInstanceMap(mapIds.get(i));
//                }
//            }
            mapIds.clear();
            mapIds = null;
            isInstanced.clear();
            isInstanced = null;
            em.disposeInstance(name);
        } catch (Exception e) {
            System.out.println("Caused by : " + emN + " instance name: " + name + " method: dispose:");
            e.printStackTrace();
             FilePrinter.printError(FilePrinter.ScriptEx_Log, e);
        }
    }


  public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public Object setProperty(String key, String value, boolean prev) {
        return props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }


	 public void leftParty(MapleCharacter chr) {
        try {
            em.getIv().invokeFunction("leftParty", this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void disbandParty() {
        try {
            em.getIv().invokeFunction("disbandParty", this);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }
    
    public void liberaEntrada() {
        try {
            em.getIv().invokeFunction("liberaEntrada", this);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void finishPQ() {
        try {
            em.getIv().invokeFunction("clearPQ", this);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void removePlayer(MapleCharacter chr) {
        try {
            em.getIv().invokeFunction("playerExit", this, chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    
    public boolean isLeader(MapleCharacter chr) {
        return (chr.getParty().getLeader().getId() == chr.getId());
    }
	
    public boolean isSquadLeader(MapleCharacter tt, MapleSquadType ttt) {
        return (tt.getClient().getChannelServer().getMapleSquad(ttt).getLeader().equals(tt));
    }

    public void registerSquad(MapleSquad squad, MapleMap map) {
        for (MapleCharacter pc : squad.getMembers()) {
            MapleCharacter c = map.getCharacterById(pc.getId());
            registerPlayer(c);
        }
    }
    
    public String getServerName() {
        return Configuration.Server_Name;
    }
    
    public String getMapleVersion() {
        return Configuration.MS_Version;
    }
    
    public String getSourceVersion() {
        return Configuration.Source_Version;
    }
}
