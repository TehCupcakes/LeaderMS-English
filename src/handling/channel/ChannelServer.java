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

package handling.channel;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.messages.CommandProcessor;
import config.configuration.Configuration;
import database.DatabaseConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import handling.MaplePacket;
import handling.MapleServerHandler;
import handling.PacketProcessor;
import handling.channel.remote.ChannelWorldInterface;
import handling.mina.MapleCodecFactory;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import handling.world.guild.MapleGuildSummary;
import handling.world.remote.WorldChannelInterface;
import handling.world.remote.WorldRegistry;
import provider.MapleDataProviderFactory;
import scripting.event.EventScriptManager;
import server.MapleSquad;
import server.MapleSquadType;
import server.PlayerInteraction.HiredMerchant;
import server.ShutdownServer;
import server.TimerManager;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.market.MarketEngine;
import tools.packet.MaplePacketCreator;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import server.AutobanManager;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.maps.MapTimer;


public class ChannelServer implements Runnable, ChannelServerMBean {
        MapleClient c;
	private static int uniqueID = 1;
	private int port = 7575;
	private static Properties initialProp;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChannelServer.class);
	private static WorldRegistry worldRegistry;
	private PlayerStorage players = new PlayerStorage();
	private String serverMessage;
	private int expRate;
	private int mesoRate;
	private int dropRate;
	private int bossdropRate;
        private boolean MT;
	private int petExpRate;
        public boolean eventOn = false;
        public boolean doublecash = false;
        public int eventMap = 0;
        private int mountExpRate;
        private int QuestExpRate;
	private boolean gmWhiteText;
	private boolean cashshop;
	private boolean mts;
	private boolean dropUndroppables;
	private boolean moreThanOne;
	private int channel;
        private int instanceId = 0;
        private boolean GMItems;
	private String key;
        private boolean AB;
	private Properties props = new Properties();
	private ChannelWorldInterface cwi;
	private WorldChannelInterface wci = null;
	private IoAcceptor acceptor;
	private String ip;
	private boolean shutdown = false;
	private boolean finishedShutdown = false;
	private String arrayString = "";

        
	private MapleMapFactory mapFactory;
	private MapleMapFactory gmMapFactory;
	private EventScriptManager eventSM;
	private static Map<Integer, ChannelServer> instances = new HashMap<Integer, ChannelServer>();
	private static Map<String, ChannelServer> pendingInstances = new HashMap<String, ChannelServer>();
	private Map<Integer, MapleGuildSummary> gsStore = new HashMap<Integer, MapleGuildSummary>();
	
	private Boolean worldReady = true;
        private final Map<Integer, HiredMerchant> merchants = new HashMap<Integer, HiredMerchant>();
        private final Lock merchant_mutex = new ReentrantLock();
	
	private Map<MapleSquadType, MapleSquad> mapleSquads = new HashMap<MapleSquadType, MapleSquad>();
	private MarketEngine me = new MarketEngine();
	private long lordLastUpdate = 0;
	private int lordId = 0;

        private ChannelServer(String key) {
		mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")));
		gmMapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")));    
                this.key = key;
	}
	
	public static WorldRegistry getWorldRegistry() {
		return worldRegistry;
	}
	
	public void reconnectWorld() {
        try {
            wci.isAvailable();
        } catch (RemoteException ex) {
            synchronized (worldReady) {
                worldReady = false;
            }
            synchronized (cwi) {
                synchronized (worldReady) {
                    if (worldReady)
                        return;
                }
                System.out.println("Reconnecting to world server");
                synchronized (wci) {
                    try {
						initialProp = new Properties();
						FileReader fr = new FileReader(System.getProperty("channel.config"));
						initialProp.load(fr);
						fr.close();
						Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
						worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
						cwi = new ChannelWorldInterfaceImpl(this);
						wci = worldRegistry.registerChannelServer(key, cwi);
						props = wci.getGameProperties();
                                                expRate = Integer.parseInt(props.getProperty("world.exp"));
                                                QuestExpRate = Integer.parseInt(props.getProperty("world.questExp"));
                                                mesoRate = Integer.parseInt(props.getProperty("world.meso"));
                                                dropRate = Integer.parseInt(props.getProperty("world.drop"));
						bossdropRate = Integer.parseInt(props.getProperty("world.bossdrop"));
						petExpRate = Integer.parseInt(props.getProperty("world.petExp"));
                                                mountExpRate = Integer.parseInt(props.getProperty("world.mountExp"));
						serverMessage = props.getProperty("world.serverMessage");
						dropUndroppables = Boolean.parseBoolean(props.getProperty("world.alldrop", "false"));
						moreThanOne = Boolean.parseBoolean(props.getProperty("world.morethanone", "false"));
						gmWhiteText = Boolean.parseBoolean(props.getProperty("world.gmWhiteText", "true"));
						cashshop = Boolean.parseBoolean(props.getProperty("world.cashshop", "false"));
						mts = Boolean.parseBoolean(props.getProperty("world.mts", "false"));
						Properties dbProp = new Properties();
						fr = new FileReader("Game/Database/db.properties");
						dbProp.load(fr);
                                                fr.close();
                        DatabaseConnection.setProps(dbProp);
                        DatabaseConnection.getConnection();
                        wci.serverReady();
                    } catch (Exception e) {
                        System.out.println("Reconnecting failed " + e);
                    }
                    worldReady = true;
                }
            }
            synchronized (worldReady) {
                worldReady.notifyAll();
            }
        }
    }

                        @Override
                        public void run() {
                        try {
                        cwi = new ChannelWorldInterfaceImpl(this);
                        wci = worldRegistry.registerChannelServer(key, cwi);
                        props = wci.getGameProperties();
	 		expRate = Integer.parseInt(props.getProperty("world.exp"));
                        QuestExpRate = Integer.parseInt(props.getProperty("world.questExp"));
			mesoRate = Integer.parseInt(props.getProperty("world.meso"));
			dropRate = Integer.parseInt(props.getProperty("world.drop"));
			bossdropRate = Integer.parseInt(props.getProperty("world.bossdrop"));
			petExpRate = Integer.parseInt(props.getProperty("world.petExp"));
                        mountExpRate = Integer.parseInt(props.getProperty("world.mountExp"));
			serverMessage = props.getProperty("world.serverMessage");
			dropUndroppables = Boolean.parseBoolean(props.getProperty("world.alldrop", "false"));
			moreThanOne = Boolean.parseBoolean(props.getProperty("world.morethanone", "false"));
			eventSM = new EventScriptManager(this, props.getProperty("channel.events").split(","));
			gmWhiteText = Boolean.parseBoolean(props.getProperty("world.gmWhiteText", "false"));
			cashshop = Boolean.parseBoolean(props.getProperty("world.cashshop", "false"));
			mts = Boolean.parseBoolean(props.getProperty("world.mts", "false"));
	                Properties dbProp = new Properties();
                        FileReader fileReader = new FileReader("Game/Database/db.properties");
                        dbProp.load(fileReader);
                        fileReader.close();
                        DatabaseConnection.setProps(dbProp);
                        DatabaseConnection.getConnection();
                        Connection c = DatabaseConnection.getConnection();
            try {
                PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0");
                ps.executeUpdate();
                ps = c.prepareStatement("UPDATE characters SET HasMerchant = 0");
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                System.out.println("Could not reset databases " + ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        port = Integer.parseInt(props.getProperty("channel.net.port"));
        ip = props.getProperty("channel.net.interface") + ":" + port;
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        acceptor = new SocketAcceptor();
        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
        
        final TimerManager tMan = TimerManager.getInstance();
        long timeToTake = System.currentTimeMillis();  
        timeToTake = System.currentTimeMillis();
        tMan.start();
        tMan.register(AutobanManager.getInstance(), 60000);
        MapTimer.getInstance().start();
        SkillFactory.cacheSkills();
        MapleItemInformationProvider.getInstance().getAllItems();
        System.out.println("[INFO] Loaded items in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds.");
        
        try {
            final MapleServerHandler serverHandler = new MapleServerHandler(PacketProcessor.getProcessor(PacketProcessor.Mode.CHANNELSERVER), channel);
            acceptor.bind(new InetSocketAddress(port), serverHandler, cfg);
            System.out.println("[INFO] Channel (" + getChannel() + ") listening on port (" + port + ").");
            wci.serverReady();
            eventSM.init();
            final ChannelServer serv = this;
			tMan.schedule(new Runnable() {
				public void run() {
					serv.broadcastPacket(MaplePacketCreator.serverNotice(6, "[System Message] " + Configuration.botMessages[(int) (Math.random() * Configuration.botMessages.length)]));
					tMan.schedule(new Runnable() {
						public void run() {
							serv.broadcastPacket(MaplePacketCreator.serverNotice(6, "[System Message] " + Configuration.botMessages[(int) (Math.random() * Configuration.botMessages.length)]));
						}
					}, 20 * 60000 + (int) (Math.random() * 10000));
				}
			}, 3 * 60000);
        } catch (IOException e) {
            System.out.println("Connection at port " + port + " failed (ch: " + getChannel() + ")" + e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutDown()));
    }

      public final int getId() {
        return channel;
    }
              
                        
       public List<MapleCharacter> getPartyMembers(MapleParty party) {
        List<MapleCharacter> partym = new ArrayList<>(8);
        for (MaplePartyCharacter partychar : party.getMembers()) {
            if (partychar.getChannel() == getId()) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    partym.add(chr);
                }
            }
        }
        return partym;


    }
	
       private final class ShutDown implements Runnable {

	@Override
	public void run() {
        shutdown = true;
        List<CloseFuture> futures = new LinkedList<CloseFuture>();
        Collection<MapleCharacter> allchars = players.getAllCharacters();
        MapleCharacter chrs[] = allchars.toArray(new MapleCharacter[allchars.size()]);
        for (MapleCharacter chr : chrs) {
            if (chr.getTrade() != null) {
                MapleTrade.cancelTrade(chr);
            }
            if (chr.getEventInstance() != null) {
                chr.getEventInstance().playerDisconnected(chr);
            }
            if (!chr.getClient().isGuest()) {
                chr.saveToDB(true, true);
            }
            if (chr.getCheatTracker() != null) {
                chr.getCheatTracker().dispose();
            }
            removePlayer(chr);
        }
        for (MapleCharacter chr : chrs) {
            futures.add(chr.getClient().getSession().close());
        }
        for (CloseFuture future : futures) {
            future.join(500);
        }
        finishedShutdown = true;
        wci = null;
        cwi = null;
	}
    }
       
        public void shutdown() {
        shutdown = true;
        List<CloseFuture> futures = new LinkedList<CloseFuture>();
        Collection<MapleCharacter> allchars = players.getAllCharacters();
        MapleCharacter chrs[] = allchars.toArray(new MapleCharacter[allchars.size()]);
        for (MapleCharacter chr : chrs) {
            if (chr.getTrade() != null) {
                MapleTrade.cancelTrade(chr);
            }
            if (chr.getEventInstance() != null) {
                chr.getEventInstance().playerDisconnected(chr);
            }
            if (!chr.getClient().isGuest()) {
                chr.saveToDB(true, true);
            }
            if (chr.getCheatTracker() != null) {
                chr.getCheatTracker().dispose();
            }
            removePlayer(chr);
        }
        for (MapleCharacter chr : chrs) {
            futures.add(chr.getClient().getSession().close());
        }
        for (CloseFuture future : futures) {
            future.join(500);
        }
        finishedShutdown = true;
        wci = null;
        cwi = null;
    }
        

        
     public final void closeAllMerchant() {
	merchant_mutex.lock();

	final Iterator<HiredMerchant> merchants_ = merchants.values().iterator();
	try {
	    while (merchants_.hasNext()) {
		merchants_.next().closeShop(true);
		merchants_.remove();
	    }
	} finally {
	    merchant_mutex.unlock();
	}
    }
    
    public void broadcastGMPacket(MaplePacket data) {
            for (MapleCharacter chr : players.getAllCharacters()) {
                if(chr.isGM())
                    chr.getClient().getSession().write(data);
            }
	}
	
	public void unbind() {
		acceptor.unbindAll();
	}
	
	public boolean hasFinishedShutdown() {
		return finishedShutdown;
	}

	public MapleMapFactory getMapFactory() {
		return mapFactory;
	}
	
    private static ChannelServer newInstance(String key) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException {
        ChannelServer instance = new ChannelServer(key);
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.registerMBean(instance, new ObjectName("net.channel:type=ChannelServer,name=ChannelServer" + uniqueID++));
        pendingInstances.put(key, instance);
        return instance;
    }
    
    
    public static ChannelServer getInstance(int channel) {
        return instances.get(channel);
    }


        public final void addPlayer(final MapleCharacter chr) {
	players.registerPlayer(chr);
	chr.getClient().getSession().write(MaplePacketCreator.serverMessage(serverMessage));
    }

    public final PlayerStorage getPlayerStorage() {
	return players;
    }

     public final void removePlayer(final MapleCharacter chr) {
	players.deregisterPlayer(chr);
    }

	public int getConnectedClients() {
		return players.getAllCharacters().size();
	}
	
	@Override
	public String getServerMessage() {
		return serverMessage;
	}

	@Override
	public void setServerMessage(String newMessage) {
		serverMessage = newMessage;
		broadcastPacket(MaplePacketCreator.serverMessage(serverMessage));
	}

	public void broadcastPacket(MaplePacket data) {
		for (MapleCharacter chr : players.getAllCharacters()) {
			chr.getClient().getSession().write(data);
		}
	}
        

	 @Override
    public int getExpRate() {
        return expRate;
    }

    @Override
    public void setExpRate(int expRate) {
        this.expRate = expRate;
    }
	
       public String getArrayString() {
        return arrayString;
        }
        
       public void setArrayString(String newStr) {
        arrayString = newStr;
       }

	public int getChannel() {
		return channel;
	}
        
        public boolean MTtoFM() {
        return MT;
    }

	 public void setChannel(int channel) {
        if (pendingInstances.containsKey(key))
            pendingInstances.remove(key);
        if (instances.containsKey(channel))
            instances.remove(channel);
        instances.put(channel, this);
        this.channel = channel;
        this.mapFactory.setChannel(channel);
    }
        
	public static Collection<ChannelServer> getAllInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }
	
	public String getIP() {
		return ip;
	}
	
    public String getIP(int channel) {
        try {
            return getWorldInterface().getIP(channel);
        } catch (RemoteException e) {
            System.out.println("Lost connection to world server " + e);
            throw new RuntimeException("Lost connection to world server");
        }
    }

    public WorldChannelInterface getWorldInterface() {
        synchronized (worldReady) {
            while (!worldReady)
                try {
                    worldReady.wait();
                } catch (InterruptedException e) {
                }
        }
        return wci;
    }
        
        public void autoRespawn() {
            for (Entry<Integer, MapleMap> map : mapFactory.getMaps().entrySet()) {
                map.getValue().respawn();
              //  log.info("Auto-Respawn successfully executed!"); 
          }
       }
        
       public void saveAll() {
        for (MapleCharacter chr : players.getAllCharacters()) {
            chr.saveToDB(true, true);
            //log.info("Auto-Save successfully executed!");
           }
       }
	
	public String getProperty(String name) {
		return props.getProperty(name);
	}

	public boolean isShutdown() {
		return shutdown;
	}


	
	@Override
	public void shutdown(int time) {
		broadcastPacket(MaplePacketCreator.serverNotice(0, "The server will shut down in under " + (time / 60000) + " minutes. Please log off safely."));
		TimerManager.getInstance().schedule(new ShutdownServer(getChannel()), time);
	}
	
	@Override
	public void shutdownWorld(int time) {
		try {
			getWorldInterface().shutdown(time);
		} catch (RemoteException e) {
			reconnectWorld();
		}
	}
	
	public int getLoadedMaps() {
		return mapFactory.getLoadedMapSize();
	}
	
	public MapleMapFactory getGmMapFactory() {
		return this.gmMapFactory;
	}
	
	public EventScriptManager getEventSM() {
		return eventSM;
	}
	
	public void reloadEvents() {
		eventSM.cancel();
		eventSM = new EventScriptManager(this, props.getProperty("channel.events").split(","));
		eventSM.init();
	}
	
	 @Override
    public int getMesoRate() {
        return mesoRate;
    }

    @Override
    public void setMesoRate(int mesoRate) {
        this.mesoRate = mesoRate;
    }

    @Override
    public int getDropRate() {
        return dropRate;
    }

    @Override
    public void setDropRate(int dropRate) {
        this.dropRate = dropRate;
    }

    @Override
    public int getBossDropRate() {
        return bossdropRate;
    }

    @Override
    public void setBossDropRate(int bossdropRate) {
        this.bossdropRate = bossdropRate;
    }

    @Override
    public int getPetExpRate() {
        return petExpRate;
    }

    @Override
    public void setPetExpRate(int petExpRate) {
        this.petExpRate = petExpRate;
    }

    @Override
    public int getMountRate() {
        return mountExpRate;
    }

    @Override
    public void setMountRate(int mountExpRate) {
        this.mountExpRate = mountExpRate;
    }
	
    public boolean allowUndroppablesDrop() {
        return dropUndroppables;
    }
	
	public boolean allowMoreThanOne() {
		return moreThanOne;
	}
	public boolean allowGmWhiteText() {
		return gmWhiteText;
	}
	
	public boolean allowCashshop() {
		return cashshop;
	}
	
	public boolean characterNameExists(String name) {
		int size = 0;
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?");
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				size++;
			}
			ps.close();
			rs.close();
		} catch (SQLException e) {
			log.error("Error in charname check: \r\n" + e.toString());
		}
		return size >= 1;
	}
	
	public MapleGuild getGuild(MapleGuildCharacter mgc) {
		int gid = mgc.getGuildId();
		MapleGuild g = null;
		try {
			g = this.getWorldInterface().getGuild(gid, mgc);
		}
		catch (RemoteException re) {
			log.error("RemoteException while fetching MapleGuild.", re);
			return null;
		}
		
		if (gsStore.get(gid) == null)
			gsStore.put(gid, new MapleGuildSummary(g));
		
		return g;
	}
	
	public MapleGuildSummary getGuildSummary(int gid) {
		if (gsStore.containsKey(gid))
			return gsStore.get(gid);
		else {		//this shouldn't happen much, if ever, but if we're caught
			//without the summary, we'll have to do a worldop
			try {
				MapleGuild g = this.getWorldInterface().getGuild(gid, null);
				if (g != null)
					gsStore.put(gid, new MapleGuildSummary(g));
				return gsStore.get(gid);	//if g is null, we will end up returning null
			}
			catch (RemoteException re) {
				log.error("RemoteException while fetching GuildSummary.", re);
				return null;
			}
		}
	}
	
	public void updateGuildSummary(int gid, MapleGuildSummary mgs) {
		gsStore.put(gid, mgs);
	}
	
	public void reloadGuildSummary() {
		try {
			MapleGuild g;
			for (int i : gsStore.keySet())
			{
				g = this.getWorldInterface().getGuild(i, null);
				if (g != null)
					gsStore.put(i, new MapleGuildSummary(g));
				else
					gsStore.remove(i);
			}
		}
		catch (RemoteException re) {
			log.error("RemoteException while reloading GuildSummary.", re);
		}
	}

 public static void main(String args[]) throws FileNotFoundException, IOException, NotBoundException,
            InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, MalformedObjectNameException {
        initialProp = new Properties();
        initialProp.load(new FileReader(System.getProperty("channel.config")));
        Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
        worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
        for (int i = 0; i < Integer.parseInt(initialProp.getProperty("channel.count", "0")); i++) {
            newInstance(initialProp.getProperty("channel." + i + ".key")).run();
        }
        DatabaseConnection.getConnection();
        CommandProcessor.registerMBean();
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                for (ChannelServer channel : getAllInstances()) {
                    for (int i = 910000001; i <= 910000022; i++) {
                        if (channel.getMapFactory().isMapLoaded(i)) {
                            MapleMap m = channel.getMapFactory().getMap(i);
                            for (MapleMapObject obj : m.getMapObjectsOfType(MapleMapObjectType.HIRED_MERCHANT)) {
                                HiredMerchant hm = (HiredMerchant) obj;
                                hm.closeShop(true);
                            }
                        }
                    }

                    for (MapleCharacter mc : channel.getPlayerStorage().getAllCharacters()) {
                        mc.saveToDB(true, true);
                    }
                }
            }
        });
    }


    public void yellowWorldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.announce(MaplePacketCreator.sendYellowTip(msg));
        }
    }

    public void worldMessage(String msg) {
        for (MapleCharacter mc : getPlayerStorage().getAllCharacters()) {
            mc.dropMessage(msg);
        }
    }
	
	public MapleSquad getMapleSquad(MapleSquadType type) {
		return mapleSquads.get(type);
	}
	
	public boolean addMapleSquad(MapleSquad squad, MapleSquadType type) {
		if (mapleSquads.get(type) == null) {
			mapleSquads.remove(type);
			mapleSquads.put(type, squad);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean removeMapleSquad(MapleSquad squad, MapleSquadType type) {
		if (mapleSquads.containsKey(type)) {
			if (mapleSquads.get(type) == squad) {
				mapleSquads.remove(type);
				return true;
			}
		}
		return false;
	}
        
        
   public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int k) {
        instanceId = k;
    }

    public void addInstanceId() {
        instanceId++;
    }
	
	public MarketEngine getMarket() {
		return me;
	}

	public long getLordLastUpdate() {
		return lordLastUpdate;
	}

	public void setLordLastUpdate(long lordLastUpdate) {
		this.lordLastUpdate = lordLastUpdate;
	}
	
	public void saveLordLastUpdate() {
		File file = new File("lordlastupdate.txt");
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(file);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ChannelServer.class.getName()).log(Level.SEVERE, null, ex);
		}
		String write = String.valueOf(lordLastUpdate);
		for (int i = 0; i < write.length(); i++) {
			try {
				o.write((int) (write.charAt(i)));
			} catch (IOException ex) {
				Logger.getLogger(ChannelServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (o != null) {
			try {
				o.close();
			} catch (IOException ex) {
				Logger.getLogger(ChannelServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public int getLordId() {
		return lordId;
	}

	public void setLordId(int lordId) {
		this.lordId = lordId;
	}
	
	public void saveLordId() {
		File file = new File("lordid.txt");
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(file);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ChannelServer.class.getName()).log(Level.SEVERE, null, ex);
		}
		String write = String.valueOf(lordId);
		for (int i = 0; i < write.length(); i++) {
			try {
				o.write((int) (write.charAt(i)));
			} catch (IOException ex) {
				Logger.getLogger(ChannelServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (o != null) {
			try {
				o.close();
			} catch (IOException ex) {
				Logger.getLogger(ChannelServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
 	public boolean allowMTS() {
        return mts;
    }

    public boolean CanGMItem() {
        return GMItems;
    }
    
     public void broadcastSMega(MaplePacket data) {
        for (MapleCharacter chr : players.getAllCharacters()) {
            if (chr.getSmegaEnabled()) {
                chr.getClient().getSession().write(data);
            }
        }
    }

    public boolean AutoBan() {
        return AB;
    }
    
    public int getQuestRate() {
        return QuestExpRate;
    }

        public void setQuestRate(int QuestExpRate) {
        this.QuestExpRate = QuestExpRate;
    }
}