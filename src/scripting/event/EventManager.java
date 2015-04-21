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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptException;
import net.channel.ChannelServer;
import net.world.MapleParty;
import server.MapleSquad;
import server.TimerManager;
import server.Timer.EventTimer;
import server.maps.MapleMap;
import tools.FilePrinter;

/**
 *
 * @author Matze
 */
public class EventManager {

	private Invocable iv;
	private ChannelServer cserv;
	private Map<String, EventInstanceManager> instances = new HashMap<String, EventInstanceManager>();
	private Properties props = new Properties();
	private String name;
        protected int channel, playerCount = 0;
        private ScheduledFuture<?> schedule = null;

	public EventManager(ChannelServer cserv, Invocable iv, String name) {
		this.iv = iv;
		this.cserv = cserv;
		this.name = name;
	}

	public void cancel() {
        try {
            iv.invokeFunction("cancelSchedule", (Object) null);
        } catch (Exception ex) {
            System.out.println("Event name : " + name + ", method Name : cancelSchedule:\n" + ex);
             FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name : " + name + ", method Name : cancelSchedule:\n" + ex);
        }
    }
        
       public void schedule(final String methodName, long delay) {
        schedule = TimerManager.getInstance().schedule(new Runnable() {
            public void run() {
                try {
                    iv.invokeFunction(methodName);
                } catch (ScriptException ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                    FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                } catch (NoSuchMethodException ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                    FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, delay);
    }
       
     public void schedule(final String methodName, final EventInstanceManager eim, long delay) {
        schedule = TimerManager.getInstance().schedule(new Runnable() {
            public void run() {
                try {
                    iv.invokeFunction(methodName, eim);
                } catch (ScriptException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, delay);
    }
       
        public ScheduledFuture<?> schedule(final String methodName, long delay, final EventInstanceManager eim) {
        return EventTimer.getInstance().schedule(new Runnable() {

            public void run() {
                try {
                    iv.invokeFunction(methodName, eim);
                } catch (Exception ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                    FilePrinter.printError(FilePrinter.ScriptEx_Log, "Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, delay);
    }

   public void cancelSchedule() {
        schedule.cancel(true);
    }

     public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
        return EventTimer.getInstance().scheduleAtTimestamp(new Runnable() {

            public void run() {
                try {
                    iv.invokeFunction(methodName, (Object) null);
                } catch (ScriptException ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                } catch (NoSuchMethodException ex) {
                    System.out.println("Event name : " + name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, timestamp);
    }


   public ChannelServer getChannelServer() {
        return cserv;
    }

    public EventInstanceManager getInstance(String name) {
        return instances.get(name);
    }

    public Collection<EventInstanceManager> getInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public EventInstanceManager newInstance(String name) {
        EventInstanceManager ret = new EventInstanceManager(this, name);
        instances.put(name, ret);
        return ret;
    }

	public void disposeInstance(String name) {
        instances.remove(name);
    }

    public Invocable getIv() {
        return iv;
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public String getName() {
        return name;
    }

    //PQ method: starts a PQ
    public void startInstance(MapleParty party, MapleMap map) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerParty(party, map);
        } catch (ScriptException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //non-PQ method for starting instance
    public void startInstance(EventInstanceManager eim, String leader) {
        try {
            iv.invokeFunction("setup", eim);
            eim.setProperty("leader", leader);
        } catch (ScriptException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
//Squad method: starts a Squad quest Zakum, HT, etc
	public void startInstance(MapleSquad squad, MapleMap map) {
		try {
			EventInstanceManager eim = (EventInstanceManager)(iv.invokeFunction("setup", (Object) null));
			eim.registerSquad(squad, map);
		} catch (ScriptException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
