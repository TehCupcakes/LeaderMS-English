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

package client.anticheat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import client.MapleCharacter;
import client.MapleClient;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import tools.MaplePacketCreator;


/**
 *
 * @author Carrino
 */
public class LeaderTracker {
   private int TOO_MUCH_DAMAGE;
   private int TRYING_TO_HACK;
   private int CHEATING_MERCHANT;
   private int SPAMMING_MEGA;
   private int SPAMMING_SUMMON_BAGS;
   private MapleClient c;

   public void Tracker() {
       if (SPAMMING_MEGA >= 100 || TOO_MUCH_DAMAGE >= 100 || CHEATING_MERCHANT >= 15 || TRYING_TO_HACK >= 10 || SPAMMING_SUMMON_BAGS > 15) {
           MapleCharacter player = c.getPlayer();
           ChannelServer cserv = null;
            if (player != null) {
                if (!player.isGM() || player.isGM()) {
                    String readableTargetName =player.getName();
                    String ip = player.getClient().getSession().getRemoteAddress().toString().split(":")[0];
                    player.ban("lulz", false);
                    try {
                        String originalReason = "being a noob server"; //lulz!
                        cserv.getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, readableTargetName + " has been banned for " + originalReason).getBytes());
                    } catch (Exception e) {
                        //error your server is a noob
                    }
              }
         }
    }
 }

   public void addSPAMMING_MEGA() {
       SPAMMING_MEGA++;
   }

   public void addSPAMMING_SUMMON_BAGS() {
       SPAMMING_SUMMON_BAGS++;
   }

   public void addCHEATING_MERCHANT () {
       CHEATING_MERCHANT++;
   }

   public void addTRYING_TO_HACK() {
       TRYING_TO_HACK++;
   }

   public void addTOO_MUCH_DAMAGE() {
       TOO_MUCH_DAMAGE++;
   }

      private static String getBannedReason(String name) {//THIS IS USELESS REMOVE
        Connection con = (Connection) DatabaseConnection.getConnection();
        try {
            PreparedStatement ps;
            ResultSet rs;
            ps = (PreparedStatement) con.prepareStatement("SELECT name, banned, banreason, macs FROM accounts WHERE name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs.next()) {
                if (rs.getInt("banned") > 0) {
                    String user, reason, mac;
                    user = rs.getString("name");
                    reason = rs.getString("banreason");
                    mac = rs.getString("macs");
                    rs.close();
                    ps.close();
                    return "Username: " + user + " | BanReason: " + reason + " | Macs: " + mac;
                } else {
                    rs.close();
                    ps.close();
                    return "Player is not banned.";
                }
            }
            rs.close();
            ps.close();
            int accid;
            ps = (PreparedStatement) con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return "This character / account does not exist.";
            } else {
                accid = rs.getInt("accountid");
            }
            ps =    (PreparedStatement) con.prepareStatement("SELECT name, banned, banreason, macs FROM accounts WHERE id = ?");
            ps.setInt(1, accid);
            rs = ps.executeQuery();
            if (rs.getInt("banned") > 0) {
                String user, reason, mac;
                user = rs.getString("name");
                reason = rs.getString("banreason");
                mac = rs.getString("macs");
                rs.close();
                ps.close();
                return "Username: " + user + " | BanReason: " + reason + " | Macs: " + mac;
            } else {
                rs.close();
                ps.close();
                return "Player is not banned.";
            }
        } catch (SQLException exe) {
        }
        return "Player is not banned.";
    }
}