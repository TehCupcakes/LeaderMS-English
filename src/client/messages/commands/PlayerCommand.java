/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy~frz.cc>
Matthias Butz <matze~odinms.de>
Jan Christian Meyer <vimes~odinms.de>

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

 * JavaScriptz (javascriptz@leaderms.com.br)
 * LeaderMS 2012 ▬ 2015
 * Brasil MapleStory Server
 * CashPQ
 * www.leaderms.com.br
 */


package client.messages.commands;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import client.MapleClient;
import client.inventory.MapleInventory;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import config.configuration.Configuration;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import handling.channel.ChannelServer;
import scripting.npc.NPCScriptManager;
import server.MapleItemInformationProvider;
import server.maps.SavedLocationType;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class PlayerCommand implements Command {

    private Map<Integer, Long> gmUsages = new LinkedHashMap<Integer, Long>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

    @SuppressWarnings("static-access")
    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {
          MapleCharacter player = c.getPlayer();
          ChannelServer cserv = c.getChannelServer();
         if (splitted[0].equalsIgnoreCase("@gm") || splitted[0].equalsIgnoreCase("!gm")) {
            if (splitted.length == 1) {
                mc.dropMessage("Tip : @gm <message>");
                return;
            }
            if (gmUsages.get(c.getPlayer().getId()) != null) {
                long lastUse = gmUsages.get(c.getPlayer().getId());
                if (System.currentTimeMillis() - lastUse < 60 * 1000 * 2) {
                    mc.dropMessage("You can only send messages to GMs twice in 2 minutes.");
                    return;
                } else {
                    mc.dropMessage("Sending message...");
                    FilePrinter.printGM("GMSupport.rtf", "Message sent: " + StringUtil.joinStringFrom(splitted, 1) + "\r\nOn day: " + sdf.format(Calendar.getInstance().getTime()) + " at " + sdf2.format(Calendar.getInstance().getTime()) + ".\r\nPlayer: " + player.getName() + " (" + player.getAccountID() + ")");
                    c.getChannelServer().getWorldInterface().broadcastGMMessage(null, MaplePacketCreator.serverNotice(5, "[" + c.getPlayer().getName() + "] " + StringUtil.joinStringFrom(splitted, 1)).getBytes());
                    gmUsages.put(c.getPlayer().getId(), System.currentTimeMillis());
                    mc.dropMessage("Done. Wait for a response.");
                }
            } else {
                mc.dropMessage("Sending message...");
                c.getChannelServer().broadcastGMPacket(MaplePacketCreator.serverNotice(5, "[" + c.getPlayer().getName() + " - GM Message] " + StringUtil.joinStringFrom(splitted, 1)));
                gmUsages.put(c.getPlayer().getId(), System.currentTimeMillis());
                mc.dropMessage("Done. Wait for a response.");
            }
        } if (splitted[0].equalsIgnoreCase("@bug")) {
                    FilePrinter.printBug("Bugs.rtf", "Bug reported: " + StringUtil.joinStringFrom(splitted, 1) + "\r\nOn day: " + sdf.format(Calendar.getInstance().getTime()) + " at " + sdf2.format(Calendar.getInstance().getTime()) + ".\r\nPlayer: " + player.getName() + " (" + player.getAccountID() + ")");
                    mc.dropMessage("Sending your report...");
          } else if (splitted[0].equalsIgnoreCase("@commands") || splitted[0].equalsIgnoreCase("!commands")) {
            mc.dropMessage(" - "+Configuration.Server_Name+" v"+Configuration.Server_Version+" -");
            for (CommandDefinition cd : getDefinition()) {
                if (!cd.getCommand().equalsIgnoreCase("help")) {
                    mc.dropMessage("@" + cd.getCommand() + " - " + cd.getHelp());
                }
            }
        } else if (splitted[0].equalsIgnoreCase("@dispose")) {
                NPCScriptManager.getInstance().dispose(c);
                c.getSession().write(MaplePacketCreator.enableActions());
                mc.dropMessage("Fixed!");
        } else if (splitted[0].equalsIgnoreCase("@partyfix")) {
                    player.setParty(null);
                    player.dropMessage("Please recreate the party.");

         } else if (splitted[0].equals("@event")) {
            if (player.getClient().getChannelServer().eventOn == true) {
                  c.getPlayer().setPreviousMap(c.getPlayer().getMapId());
                  player.changeMap(player.getClient().getChannelServer().eventMap, 0);
                  player.saveLocation(SavedLocationType.EVENTO);
            } else {
                mc.dropMessage("There are no events at this time.");
            }
         } else if (splitted[0].equals("@exitevent")) {
            if (c.getChannelServer().eventOn == true) {
                    player.changeMap(player.getSavedLocation(SavedLocationType.EVENTO), 0);
                    player.clearSavedLocation(SavedLocationType.EVENTO);
            } else {
                c.getPlayer().dropMessage("There are no active events at this time. Please try again later!");
                return;
            }
        } else if (splitted[0].equals("@entertot")) {
            if (player.getMapId() == 240000110) {
                    player.changeMap(270000100);
            } else {
                c.getPlayer().dropMessage("You are not in the right map in Leafre!");
                return;
            }
        } else if (splitted[0].equals("@exittot")) {
            if (player.getMapId() == 270000100) {
                    player.changeMap(240000110);
            } else {
                c.getPlayer().dropMessage("You are not at the Temple entrance!");
                return;
            }
        }
         else if (splitted[0].equals("@jqrank")) {
                              try {
                    ResultSet rs = rankingJQ(false);
                    player.dropMessage(" .:: Top 3 best JQ players ::. ");
                    int i = 1;
                    while (rs.next()) {

                        player.dropMessage(i + ". <Nome> " + rs.getString("name") + " /  <JQ Points> " + rs.getInt("jqrank"));
                        i++;
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(PlayerCommand.class.getName()).log(Level.SEVERE, null, ex);
                }
         }
         else if (splitted[0].equals("@checkchar")) {
                player.saveToDB(true, false);
                player.yellowMessage(".::You currently have::.");
                player.dropMessage("BetaPoints: " + player.getCSPoints(5)); //remover dps
                player.dropMessage("LeaderPoints: " + player.getLeaderPoints());
                player.dropMessage("Q.Points: " + player.getpqPoints());
                player.dropMessage("JQ Points: " +  player.getJQPoints());
                player.dropMessage("Cash Points: " +  player.getCashPoints());
                player.dropMessage("Job: " + player.getOccupation());
         } 
        
         else if (splitted[0].equalsIgnoreCase("@smega")) {
                if (player.getMeso() >= 10000) {
                player.setSmegaEnabled(!player.getSmegaEnabled());
                String text = (!player.getSmegaEnabled() ? "[Disabled] Smegas have been disabled." : "[Activated] Smegas have been activated.");
                mc.dropMessage(text);
                player.gainMeso(-10000, true);
                } else {
                mc.dropMessage("To enable/disable you need 10,000 mesos.");
            }
        } 
    }
    

    public int itemQuantity(MapleClient c, int itemid) {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
        MapleInventory iv = c.getPlayer().getInventory(type);
        int possesed = iv.countById(itemid);
        return possesed;
    }
    
   private static ResultSet rankingJQ(boolean gm) {
        try {
            Connection con = (Connection) DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (!gm) {
                ps = (PreparedStatement) con.prepareStatement("SELECT jqrank , level, name FROM characters WHERE gm < 3 ORDER BY jqrank desc LIMIT 3");
            } else {
                ps = (PreparedStatement) con.prepareStatement("SELECT jqrank , name, gm FROM characters WHERE gm >= 3");
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
                        new CommandDefinition("commands", "", "Displays list of help.", 0),
			new CommandDefinition("gm", "", "Sends a message to an online GM.", 0),
                        new CommandDefinition("dispose", "", "Not able to speak to an NPC? Use this command.", 0),
                        new CommandDefinition("partyfix", "", "Fixes a bug in the creation of parties.", 0),
                        new CommandDefinition("event", "", "Takes you to the event in progress.", 0),
                        new CommandDefinition("exitevent", "", "Takes you back to the map where you were. (Leave event)", 0),
                        new CommandDefinition("smega", "", "This command Enables/Disables super megaphones (Rate: 10,000 Mesos).", 0),
                        new CommandDefinition("bug", "", "Send a bug report.", 0),
                        new CommandDefinition("entertot", "", "Enter the Temple.", 0),    
                        new CommandDefinition("exittot", "", "Exit the Temple.", 0),    
		};
    }
}