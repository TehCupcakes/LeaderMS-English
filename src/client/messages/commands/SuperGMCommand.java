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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static client.messages.CommandProcessor.getOptionalIntArg;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import database.DatabaseConnection;
import tools.packet.MaplePacketCreator;

public class SuperGMCommand implements Command {

    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {

 if(splitted[0].equals("!unban"))
            {
                String playerName = splitted[1];

                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps;

                int accountid = 0;

                try
                {
                    //get points according to town
                    ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
                    ps.setString(1, playerName);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next())
                    {
                        ps.close();
                    }
                    accountid = rs.getInt("accountid");
                    ps.close();
                }
                catch (SQLException e) {System.out.println("SQL Exception: " + e);}

                String banString = "";
                String macsOrig = "";

                try
                {
                    ps = con.prepareStatement("SELECT banreason, macs FROM accounts WHERE id = " + accountid);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next())
                    {
                        ps.close();
                    }
                    banString = rs.getString("banreason");
                    macsOrig = rs.getString("macs");
                    ps.close();
                }
                catch (SQLException e) {System.out.println("SQL Exception: " + e);}

                if(macsOrig != null)
                {
                    int occurs = 0;

                    for(int i = 0; i < macsOrig.length(); i++)
                    {

                          char next = macsOrig.charAt(i);

                          if(next == ',')
                          {
                               occurs++;
                          }
                    }

                    String macs[] = new String[occurs + 1];

                    System.out.println("Creating macs array...");
                    for(int i = 0; i <= occurs; i++)
                    {
                        int offset = 0;

                        if(i > 0)
                            offset = 2;

                        macs[i] = macsOrig.substring((i * 17) + offset, (i * 17) + 17);
                        System.out.println(macs[i]);
                    }

                    for(int i = 0; i < macs.length; i++)
                    {
                        try
                        {
                            ps = con.prepareStatement("DELETE FROM macbans WHERE mac = ?");
                            ps.setString(1, macs[i]);
                            ps.executeUpdate();
                            ps.close();
                        }
                        catch (SQLException e) {System.out.println("SQL Exception: " + e);}
                    }
                    mc.dropMessage("Macs Unbanned");
                }

                if(banString.indexOf("IP: /") != -1)
                {
                    String ip = banString.substring(banString.indexOf("IP: /") + 5, banString.length() - 1);
                    try
                    {
                        ps = con.prepareStatement("DELETE FROM ipbans WHERE ip = ?");
                        ps.setString(1, ip);
                        ps.executeUpdate();
                        ps.close();
                        mc.dropMessage("IP Address Unbanned");
                    }
                    catch (SQLException e) {System.out.println("SQL Exception: " + e);}
                }


                try
                {
                    ps = con.prepareStatement("UPDATE accounts SET banned = -1, banreason = null WHERE id = " + accountid);
                    ps.executeUpdate();
                    ps.close();
                    mc.dropMessage("Account Unbanned");
                }
                catch (SQLException e) {System.out.println("SQL Exception: " + e);}

        }
    }
        @Override
    public CommandDefinition[] getDefinition() {
        return new CommandDefinition[]{
                new CommandDefinition("unban", "", "", 3),
			    //new CommandDefinition("clock", "[time]", "Shows a clock to everyone in the map", 3),
             };
    }
}