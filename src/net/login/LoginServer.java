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

package net.login;

import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import client.SkillFactory;

import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.MapleServerHandler;
import net.PacketProcessor;
import net.login.remote.LoginWorldInterface;
import net.mina.MapleCodecFactory;
import net.world.remote.WorldLoginInterface;
import net.world.remote.WorldRegistry;
import server.MapleItemInformationProvider;
import server.TimerManager;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class LoginServer implements Runnable, LoginServerMBean {
	public static final int PORT = 8484;
	private IoAcceptor acceptor;
	static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginServer.class);
	private static WorldRegistry worldRegistry = null;
	private Map<Integer, String> channelServer = new HashMap<Integer, String>();
	private LoginWorldInterface lwi;
	private WorldLoginInterface wli;
	private Properties prop = new Properties();
	private Properties initialProp = new Properties();
	private Boolean worldReady = Boolean.TRUE;
	private Properties subnetInfo = new Properties();
	private Map<Integer, Integer> load = new HashMap<Integer, Integer>();
	private String serverName;
	private String eventMessage;
	int flag;
	int maxCharacters;
        private boolean pin; 
        private boolean AutoReg;
        private byte AutoRegLimit;
        private boolean twoWorlds;
        private boolean resetStats;

	int userLimit;
	int loginInterval;
	private long rankingInterval;
        private boolean serverCheck;
	private static LoginServer instance = new LoginServer();


    static {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mBeanServer.registerMBean(instance, new ObjectName("net.login:type=LoginServer,name=LoginServer"));
        } catch (Exception e) {
            System.out.println("MBEAN ERROR " + e);
        }
    }

	private LoginServer() {
    }

    public static LoginServer getInstance() {
        return instance;
    }

    public Set<Integer> getChannels() {
        return channelServer.keySet();
    }

   public void addChannel(int channel, String ip) {
        channelServer.put(channel, ip);
        load.put(channel, 0);
    }

    public void removeChannel(int channel) {
        channelServer.remove(channel);
        load.remove(channel);
    }

    public String getIP(int channel) {
        return channelServer.get(channel);
    }
    
        
    public void reconnectWorld() {
        try {
            wli.isAvailable();
        } catch (RemoteException ex) {
            synchronized (worldReady) {
                worldReady = Boolean.FALSE;
            }
            synchronized (lwi) {
                synchronized (worldReady) {
                    if (worldReady)
                        return;
                }
                System.out.println("Reconnecting to world server");
                synchronized (wli) {
                    try {
                        FileReader fileReader = new FileReader(System.getProperty("login.config"));
                        initialProp.load(fileReader);
                        fileReader.close();
                        Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
                        worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
                        lwi = new LoginWorldInterfaceImpl();
                        wli = worldRegistry.registerLoginServer(initialProp.getProperty("login.key"), lwi);
                        Properties dbProp = new Properties();
                        fileReader = new FileReader("Game/Database/db.properties");
                        dbProp.load(fileReader);
                        fileReader.close();
                        DatabaseConnection.setProps(dbProp);
                        DatabaseConnection.getConnection();
                        prop = wli.getWorldProperties();
			userLimit = Integer.parseInt(prop.getProperty("login.userlimit"));
			serverName = prop.getProperty("login.serverName");
			eventMessage = prop.getProperty("login.eventMessage");
			flag = Integer.parseInt(prop.getProperty("login.flag"));
			maxCharacters = Integer.parseInt(prop.getProperty("login.maxCharacters"));
                        AutoReg = Boolean.parseBoolean(prop.getProperty("login.AutoRegister"));
                        AutoRegLimit = Byte.parseByte(prop.getProperty("login.AutoRegisterLimit"));
                        twoWorlds = Boolean.parseBoolean(prop.getProperty("world.twoWorlds", "false"));
			} catch (Exception e) {
                        System.out.println("Reconnecting failed" + e);
                    }
                    worldReady = Boolean.TRUE;
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
            FileReader fileReader = new FileReader(System.getProperty("login.config"));
            initialProp.load(fileReader);
            fileReader.close();
            Registry registry = LocateRegistry.getRegistry(initialProp.getProperty("world.host"), Registry.REGISTRY_PORT, new SslRMIClientSocketFactory());
            worldRegistry = (WorldRegistry) registry.lookup("WorldRegistry");
            lwi = new LoginWorldInterfaceImpl();
            wli = worldRegistry.registerLoginServer(initialProp.getProperty("login.key"), lwi);
            Properties dbProp = new Properties();
            fileReader = new FileReader("Game/Database/db.properties");
            dbProp.load(fileReader);
            fileReader.close();
            DatabaseConnection.setProps(dbProp);
            DatabaseConnection.getConnection();
            prop = wli.getWorldProperties();
            userLimit = Integer.parseInt(prop.getProperty("login.userlimit"));
	    serverName = prop.getProperty("login.serverName");
	    eventMessage = prop.getProperty("login.eventMessage");
	    flag = Integer.parseInt(prop.getProperty("login.flag"));
	    maxCharacters = Integer.parseInt(prop.getProperty("login.maxCharacters"));
            AutoReg = Boolean.parseBoolean(prop.getProperty("login.AutoRegister", "false"));
            AutoRegLimit = Byte.parseByte(prop.getProperty("login.AutoRegisterLimit", "5"));
            twoWorlds = Boolean.parseBoolean(prop.getProperty("world.twoWorlds", "false"));
           } catch (Exception e) {
            throw new RuntimeException("Could not connect to world server.", e);
        }
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        acceptor = new SocketAcceptor();
        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory()));
        TimerManager tMan = TimerManager.getInstance();
        tMan.start();
        loginInterval = Integer.parseInt(prop.getProperty("login.interval"));
        tMan.register(LoginWorker.getInstance(), loginInterval);
        rankingInterval = Long.parseLong(prop.getProperty("login.ranking.interval"));
        tMan.register(new RankingWorker(), rankingInterval);
        try {
            acceptor.bind(new InetSocketAddress(PORT), new MapleServerHandler(PacketProcessor.getProcessor(PacketProcessor.Mode.LOGINSERVER)), cfg);
        System.out.println("Server binding to port (" + PORT + ")");
        
        } catch (IOException e) {
            System.out.println("Binding to port " + PORT + " failed: " + e);
        }
    }
	
 public void shutdown() {
        System.out.println("Shutting down...");
        try {
            worldRegistry.deregisterLoginServer(lwi);
        } catch (RemoteException e) {
        }
        TimerManager.getInstance().stop();
        System.exit(0);
    }

    public WorldLoginInterface getWorldInterface() {
        synchronized (worldReady) {
            while (!worldReady)
                try {
                    worldReady.wait();
                } catch (InterruptedException e) {
                }
        }
        return wli;
    }

    public static void main(String args[]) {
        try {
            LoginServer.getInstance().run();
        } catch (Exception ex) {
            System.out.println("Error initializing loginserver " + ex);
        }
    }

	public int getLoginInterval() {
		return loginInterval;
	}

	public Properties getSubnetInfo() {
		return subnetInfo;
	}

	public int getUserLimit() {
		return userLimit;
	}

	public String getServerName() {
		return serverName;
	}

	@Override
	public String getEventMessage() {
		return eventMessage;
	}

	@Override
	public int getFlag() {
		return flag;
	}

	public int getMaxCharacters() {
		return maxCharacters;
	}

	public Map<Integer, Integer> getLoad() {
		return load;
	}

	public void setLoad(Map<Integer, Integer> load) {
		this.load = load;
	}

	@Override
	public void setEventMessage(String newMessage) {
		this.eventMessage = newMessage;
	}

	@Override
	public void setFlag(int newflag) {
		flag = newflag;
	}

	@Override
	public int getNumberOfSessions() {
		return acceptor.getManagedSessions(new InetSocketAddress(PORT)).size();
	}

	@Override
	public void setUserLimit(int newLimit) {
		userLimit = newLimit;
	}
        
        public boolean isAllowPin() { 
        return pin; 
    } 

    public void setServerCheck(boolean set) {
        serverCheck = set;
    }

    public boolean isServerCheck() {
        return serverCheck;
    }
    
     public boolean AutoRegister() {
        return AutoReg;
    }

    public byte AutoRegLimit() {
        return AutoRegLimit;
    }

   public boolean twoWorldsActive() {
        return twoWorlds;
    }

    public boolean getResetStats() {
        return resetStats;
    }

   public int getPossibleLogins() {
		int ret = 0;
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement limitCheck = con.prepareStatement("SELECT COUNT(*) FROM accounts WHERE loggedin > 1 AND gm=0");
			ResultSet rs = limitCheck.executeQuery();
			if (rs.next()) {
				int usersOn = rs.getInt(1);
				// log.info("userson: " + usersOn + ", limit: " + userLimit);
				if (usersOn < userLimit) {
					ret = userLimit - usersOn;
				}
			}
			rs.close();
			limitCheck.close();
		} catch (Exception ex) {
			log.error("loginlimit error", ex);
		}
		return ret;
	}
}
