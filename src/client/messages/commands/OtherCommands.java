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

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DatabaseConnection;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import handling.channel.handler.OXHandler;
import server.MapleOxQuiz;
import tools.packet.*;
import tools.StringUtil;

public class OtherCommands implements Command {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OtherCommands.class);
    final private static String[] reasons = {"Hacking", "Botting", "Scamming", "Fake GM", "Harassment", "Advertising"};
    private int zz = 1;
    private MapleClient cc;

        @SuppressWarnings("static-access")
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {

            if (splitted[0].equals("!listreports")) {
                ResultSet rs = getReports();
                try {
                    while(rs.next()){
                        mc.dropMessage("id: " + rs.getInt("id") + " | time reported: " + rs.getTimestamp("reporttime").toString() + " | reason: " + reasons[rs.getByte("reason")]);
                    }
                } catch(Exception ex) {}
            } else if (splitted[0].equals("!getreport")) {
                if (splitted.length != 2) {
                    int reportid = Integer.parseInt(splitted[1]);
                    ResultSet rs = getReport(reportid);
                    try {
                        while(rs.next()){
                            mc.dropMessage("id: " + rs.getInt("id") + " | time reported: " + rs.getTimestamp("reporttime").toString() + " | reason: " + reasons[rs.getByte("reason")]);
                            mc.dropMessage("reporter charid: " + rs.getInt("reporterid"));
                            mc.dropMessage("victim charid: " + rs.getInt("victimid"));
                            mc.dropMessage("chatlog: ");
                            mc.dropMessage(rs.getString("chatlog"));
                            mc.dropMessage("Status: " + rs.getString("status"));
                        }
                    } catch(Exception ex){}
                }
            } else if (splitted[0].equals("!delreport")) {   
                if (splitted.length != 2) {
                    int reportid = Integer.parseInt(splitted[1]);
                    deleteReport(reportid);
                }
            } else if (splitted[0].equals("!setreportstatus")) {  
                if (splitted.length < 3) {
                    int reportid = Integer.parseInt(splitted[1]);
                    String status = StringUtil.joinStringFrom(splitted, 2);
                    setReportStatus(reportid, status);
                }
            } else if (splitted[0].equals("!getnamebyid")) {
                if (splitted.length != 2) {
                    int cid = Integer.parseInt(splitted[1]);
                    mc.dropMessage(getCharInfoById(cid));
                }
            } else if (splitted[0].equals("!getnamebyid")) {
                if (splitted.length != 2) {
                    int cid = Integer.parseInt(splitted[1]);
                    mc.dropMessage(getCharInfoById(cid));
                }
            } else if (splitted[0].equals("!startox")) {
                    if (c.getPlayer().getMapId() != 109020001) {
                        mc.dropMessage("You are not allowed to start the OX Quiz here, please go to map 109020001");
                    } else {
						MapleOxQuiz quiz = new MapleOxQuiz(c.getPlayer().getMap(), 1, 1);
						quiz.scheduleOx();
                    }
            } else if (splitted[0].equals("!oxquestion")) {
                    int bla1 = Integer.parseInt(splitted[1]);
                    int bla2 = Integer.parseInt(splitted[2]);
                    if (splitted.length != 3) {
                        mc.dropMessage("Use it as !oxquestion <imgdir> <id>");
                    } else {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, "[OX Quiz] " + OXHandler.getOXQuestion(bla1, bla2)));
                    }
            } else if (splitted[0].equals("!oxexplain")) {
                    int bla1 = Integer.parseInt(splitted[1]);
                    int bla2 = Integer.parseInt(splitted[2]);
                    if (splitted.length != 3) {
                        mc.dropMessage("Use it as !oxexplain <imgdir> <id>");
                    } else {
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "[Explanation] " + OXHandler.getOXExplain(bla1, bla2)));
                    }
            } else if (splitted[0].equals("!oxanswer")) {
                    int bla1 = Integer.parseInt(splitted[1]);
                    int bla2 = Integer.parseInt(splitted[2]);
                    int well = OXHandler.getOXAnswer(bla1, bla2);
                    if (splitted.length != 3) {
                        mc.dropMessage("Use it as !oxanswer <imgdir> <id>");
                    } else {
                        mc.dropMessage("The answer is " + (well == 1));/*+ ", 0 means false, 1 means true");*/
                    }
            } else if (splitted[0].equals("!maphint")) {
                    String hint = StringUtil.joinStringFrom(splitted, 1);
                    MaplePacket cbp = MaplePacketCreator.sendHint(hint);
                    c.getPlayer().getMap().broadcastMessage(cbp);
            } else if (splitted[0].equals("!worldhint")) {
                    String hint = StringUtil.joinStringFrom(splitted, 1);
                    c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.sendHint(hint).getBytes());
 } else if (splitted[0].equals("!sendnote")) {
     if (splitted.length >= 2) {
     try {
         String text = StringUtil.joinStringFrom(splitted, 2);
	MaplePacketCreator.sendUnkwnNote(splitted[1], text, c.getPlayer().getName());
	} catch (SQLException e) {log.error("SAVING NOTE", e);}
     } else {
         mc.dropMessage("Use it like this, !sendnote <charactername> <text>");
     }
  } else if (splitted[0].equals("!sendallnote")) {
     if (splitted.length >= 1) {
     try {
        String text = StringUtil.joinStringFrom(splitted, 1);
        ChannelServer cserv = c.getChannelServer();
        for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()){
            MaplePacketCreator.sendUnkwnNote(mch.getName(), text, c.getPlayer().getName());
        }
    } catch (SQLException e) {log.error("SAVING NOTE", e);}
        } else {
            mc.dropMessage("Use it like this, !sendallnote <text>");
        }
    } else if (splitted[0].equals("!setteam")) {
       ChannelServer cserv = c.getChannelServer();
       MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
       if (splitted[2].equals("red")) {
           victim.setTeam(0);
           mc.dropMessage(c.getPlayer().getName() + " is now in team red");
       } else if (splitted[2].equals("blue")) {
           victim.setTeam(1);
           mc.dropMessage(c.getPlayer().getName() + " is now in team blue");
       } else if (splitted[1].equals("red")) {
           c.getPlayer().setTeam(0);
           mc.dropMessage("You are now in team red");
       } else if (splitted[1].equals("blue")) {
           c.getPlayer().setTeam(1);
           mc.dropMessage("You are now in team blue");
       } else {
           mc.dropMessage("Use it like !setteam <playername> <red or blue>");
           mc.dropMessage("You can also use it as !setteam <red or blue>");
       }
	} else if (splitted[0].equals("startcpq")) {
            c.getSession().write(MonsterCarnivalPacket.startCPQ());
	} else if (splitted[0].equals("gaincp")) {
		if (splitted.length != 4) {
			mc.dropMessage("Please use !gaincp <team> <amount> <newamount>");
		} else {
			c.getSession().write(MonsterCarnivalPacket.obtainCP(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3])));
			c.getSession().write(MonsterCarnivalPacket.updateCP(Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3])));
			c.getSession().write(MonsterCarnivalPacket.showCPQMobs());
		}
	}
}
 
	private static ResultSet getReports(){
		try {
		Connection dcon = DatabaseConnection.getConnection();
		PreparedStatement ps = dcon.prepareStatement("SELECT * FROM reports");
		return ps.executeQuery();

		} catch(Exception ex) {}
		return null;

	}
	
	private static void deleteReport(int id){
		try {
		Connection dcon = DatabaseConnection.getConnection();
		PreparedStatement ps = dcon.prepareStatement("DELETE FROM reports WHERE id = ?");
		ps.setInt(1, id);
		ps.executeUpdate();
		ps.close();       


		} catch(Exception ex) {}


	}
	
	private static ResultSet getReport(int id){
		try {
		Connection dcon = DatabaseConnection.getConnection();
		PreparedStatement ps = dcon.prepareStatement("SELECT * FROM reports where id = ?");
		ps.setInt(1, id);
		return ps.executeQuery();

		} catch(Exception ex) {}
		return null;
	}
	
	private static void setReportStatus(int id, String status){
					try {
		Connection dcon = DatabaseConnection.getConnection();
		PreparedStatement ps = dcon.prepareStatement("UPDATE reports SET status = ? WHERE id = ?");
		ps.setString(1, status);
		ps.setInt(2, id);
		ps.executeUpdate();
		ps.close();       


		} catch(Exception ex) {}

	}

	private static String getCharInfoById(int id){
					try {
		Connection dcon = DatabaseConnection.getConnection();
		PreparedStatement ps = dcon.prepareStatement("SELECT * FROM characters where id = ?");
		ps.setInt(1, id);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
		return rs.getString("name");

		} catch(Exception ex) {}
		return "error while trying to get name";

	}

        
	@Override public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			            new CommandDefinition("listreports", "", "", 4),
                        new CommandDefinition("getreport", "", "", 4),
                        new CommandDefinition("delreport", "", "", 4),
                        new CommandDefinition("setreportstatus", "", "", 4),
                        new CommandDefinition("sendnote", "", "", 4),
                        new CommandDefinition("sendallnote", "", "", 4),
                        new CommandDefinition("setteam", "", "", 4),
                        new CommandDefinition("maphint", "", "", 4),
                        new CommandDefinition("worldhint", "", "", 4),
                        new CommandDefinition("hiredmerchant", "", "", 4),
                        new CommandDefinition("oxquestion", "", "", 4),
                        new CommandDefinition("oxanswer", "", "", 4),
                        new CommandDefinition("oxexplain", "", "", 4),
                        new CommandDefinition("startox", "", "", 4),
                        new CommandDefinition("stopox", "", "", 4),
                        new CommandDefinition("startcpq", "", "", 4),
                        new CommandDefinition("gaincp", "", "", 4),
		};
	}

 }