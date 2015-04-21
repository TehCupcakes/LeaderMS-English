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

package client.messages;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.channel.ChannelServer;
import net.channel.handler.GeneralchatHandler;
import server.TimerManager;
import server.maps.MapleMap;
import tools.ClassFinder;
import tools.FilePrinter;
import tools.MockIOSession;
import tools.Pair;
import tools.StringUtil;

public class CommandProcessor implements CommandProcessorMBean {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GeneralchatHandler.class);
	private static final List<Pair<String, String>> gmlog = new LinkedList<Pair<String, String>>();
	private Map<String, DefinitionCommandPair> commands = new LinkedHashMap<String, DefinitionCommandPair>();
	private static CommandProcessor instance = new CommandProcessor();
	private static Runnable persister;
	private ScriptEngineFactory sef;
        private static final Lock rl = new ReentrantLock();

	static {
		persister = new PersistingTask();
		TimerManager.getInstance().register(persister, 62000);
	}
	
	public List<CommandDefinition> getCommands() {
		List<CommandDefinition> ret = new LinkedList<CommandDefinition>();
		for (DefinitionCommandPair dcp : this.commands.values()) {
			ret.add(dcp.getDefinition());
		} 
		return ret;
	}
        
     private CommandProcessor() {
        ScriptEngineManager sem = new ScriptEngineManager();
        sef = sem.getEngineByName("javascript").getFactory();
        instance = this; // hackydihack
        reloadCommands();
    }

	
    public static class PersistingTask implements Runnable {

        @Override
	public void run() {
	    final StringBuilder sb = new StringBuilder();

	    rl.lock();
	    try {
		final String time = FilePrinter.CurrentReadable_Time();

		for (Pair<String, String> logentry : gmlog) {
		    sb.append("NOME : ");
		    sb.append(logentry.getLeft());
		    sb.append(", COMMAND : ");
		    sb.append(logentry.getRight());
		    sb.append(", HORARIO : ");
		    sb.append(time);
		    sb.append("\n");
		}
		gmlog.clear();
	    } finally {
		rl.unlock();
	    }
	    FilePrinter.log(FilePrinter.GMCommand_Log, sb.toString());
	}
    }

	 public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mBeanServer.registerMBean(instance, new ObjectName("client.messages:name=CommandProcessor"));
        } catch (Exception e) {
            System.out.println("Error registering CommandProcessor MBean");
        }
    }

	public static String joinAfterString(String splitted[], String str) {
		for (int i = 1; i < splitted.length; i++) {
			if (splitted[i].equalsIgnoreCase(str) && i + 1 < splitted.length) {
				return StringUtil.joinStringFrom(splitted, i + 1);
			}
		}
		return null;
	}

	public static int getOptionalIntArg(String splitted[], int position, int def) {
		if (splitted.length > position) {
			try {
				return Integer.parseInt(splitted[position]);
			} catch (NumberFormatException nfe) {
				return def;
			}
		}
		return def;
	}

	public static String getNamedArg(String splitted[], int startpos, String name) {
		for (int i = startpos; i < splitted.length; i++) {
			if (splitted[i].equalsIgnoreCase(name) && i + 1 < splitted.length) {
				return splitted[i + 1];
			}
		}
		return null;
	}

	public static Integer getNamedIntArg(String splitted[], int startpos, String name) {
		String arg = getNamedArg(splitted, startpos, name);
		if (arg != null) {
			try {
				return Integer.parseInt(arg);
			} catch (NumberFormatException nfe) {
				// swallow - we don't really care
			}
		}
		return null;
	}

	public static int getNamedIntArg(String splitted[], int startpos, String name, int def) {
		Integer ret = getNamedIntArg(splitted, startpos, name);
		if (ret == null) {
			return def;
		}
		return ret.intValue();
	}

	public static Double getNamedDoubleArg(String splitted[], int startpos, String name) {
		String arg = getNamedArg(splitted, startpos, name);
		if (arg != null) {
			try {
				return Double.parseDouble(arg);
			} catch (NumberFormatException nfe) {
				// swallow - we don't really care
			}
		}
		return null;
	}

	public boolean processCommand(MapleClient c, String line) {
		return instance.processCommandInternal(c, new ServernoticeMapleClientMessageCallback(c), line);
	}

	/* (non-Javadoc)
	 * @see client.messages.CommandProcessorMBean#processCommandJMX(int, int, java.lang.String)
	 */
	  public String processCommandJMX(int cserver, int mapid, String command) {
        ChannelServer cserv = ChannelServer.getInstance(cserver);
        if (cserv == null) {
            return "The specified channel Server does not exist in this serverprocess";
        }
        MapleClient c = new MapleClient(null, null, new MockIOSession());
        MapleCharacter chr = MapleCharacter.getDefault(c, 26023);
        c.setPlayer(chr);
        MapleMap map = cserv.getMapFactory().getMap(mapid);
        if (map != null) {
            chr.setMap(map);
            SkillFactory.getSkill(9101004).getEffect(1).applyTo(chr);
            map.addPlayer(chr);
        }
        cserv.addPlayer(chr);
        MessageCallback mc = new StringMessageCallback();
        try {
            processCommandInternal(c, mc, command);
        } finally {
            if (map != null) {
                map.removePlayer(chr);
            }
            cserv.removePlayer(chr);
        }
        return mc.toString();
    }

    public static void forcePersisting() {
        persister.run();
    }

    public static CommandProcessor getInstance() {
        return instance;
    }

    public void reloadCommands() {
        commands.clear();
        try {
            ClassFinder classFinder = new ClassFinder();
            String[] classes = classFinder.listClasses("client.messages.commands", true);
            for (String clazz : classes) {
                Class<?> clasz = Class.forName(clazz);
                if (Command.class.isAssignableFrom(clasz)) {
                    try {
                        Command newInstance = (Command) clasz.newInstance();
                        registerCommand(newInstance);
                    } catch (Exception e) {
                        System.out.println("ERROR INSTANCIATING COMMAND CLASS" + e);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("THROW" + e);
        }
    }

   private void registerCommand(Command command) {
        CommandDefinition[] definition = command.getDefinition();
        for (CommandDefinition def : definition) {
            commands.put(def.getCommand().toLowerCase(), new DefinitionCommandPair(command, def));
        }
    }
         
	public void dropHelp(MapleCharacter chr, MessageCallback mc, int page) {
		List<DefinitionCommandPair> allCommands = new ArrayList<DefinitionCommandPair>(commands.values());
		int startEntry = (page - 1) * 20;
		mc.dropMessage("Command Page : --------" + page + "---------");
		for (int i = startEntry; i < startEntry + 20 && i < allCommands.size(); i++) {
			CommandDefinition commandDefinition = allCommands.get(i).getDefinition();
			if (chr.hasGmLevel(commandDefinition.getRequiredLevel())) {
				dropHelpForDefinition(mc, commandDefinition);
			}
		}
	}

	private void dropHelpForDefinition(MessageCallback mc, CommandDefinition commandDefinition) {
		mc.dropMessage(commandDefinition.getCommand() + " " + commandDefinition.getParameterDescription() + ": " + commandDefinition.getHelp());
	}

	/* (non-Javadoc)
	 * @see client.messages.CommandProcessorMBean#processCommandInstance(client.MapleClient, java.lang.String)
	 */
	private boolean processCommandInternal(MapleClient c, MessageCallback mc, String line) {
		MapleCharacter player = c.getPlayer();
		if (line.charAt(0) == '!' || line.charAt(0) == '@' || line.charAt(0) == '~') {
			if (line.charAt(0) == '~') {
				mc.dropMessage("You can only use player commands (@).");
				return false;
			}
			String[] splitted = line.split(" ");
			if (splitted.length > 0 && splitted[0].length() > 1) {
				DefinitionCommandPair definitionCommandPair = commands.get(splitted[0].substring(1));
				if (definitionCommandPair != null && (player.getGMLevel() >= definitionCommandPair.getDefinition().getRequiredLevel()) ||
						player.isLord() && definitionCommandPair.getDefinition().getRequiredLevel() == 50) {
					synchronized (gmlog) {
						if (definitionCommandPair.getDefinition().getRequiredLevel() > 0)
//							gmlog.add(new Pair<MapleCharacter, String>(player, line));
                                                gmlog.add(new Pair<String, String>(c.getPlayer().getName(), line));
					}
					try {
						definitionCommandPair.getCommand().execute(c, mc, splitted);
					} catch (IllegalCommandSyntaxException e) {
						mc.dropMessage("Illegal command syntax: " + e.getMessage());
						dropHelpForDefinition(mc, definitionCommandPair.getDefinition());
					} catch (Exception e) {
						mc.dropMessage("An error occured: " + e.getClass().getName() + " " + e.getMessage());
						// why do we need to spam the console when someone makes a typo? D:
						//log.error("COMMAND ERROR", e);
					}
					return true;
				} else {
					if (definitionCommandPair == null || player.getGMLevel() >= definitionCommandPair.getDefinition().getRequiredLevel()) {
						mc.dropMessage("The command " + splitted[0] + " does not exist or you do not have the necessary privileges.");
						if (line.charAt(0) == '~') return false;
						return true;
					}
				}
			}
		}
		return false;
	}
}

class DefinitionCommandPair {

    private Command command;
    private CommandDefinition definition;

    public DefinitionCommandPair(Command command, CommandDefinition definition) {
        super();
        this.command = command;
        this.definition = definition;
    }

    public Command getCommand() {
        return command;
    }

    public CommandDefinition getDefinition() {
        return definition;
    }
}