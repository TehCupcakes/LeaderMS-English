/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import static java.lang.Math.abs;

import database.DatabaseConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Jvlaple
 */
public class PlayerNPCWorker implements Runnable {
	/**
	 * Updates the player NPCs...
	 * @param Name - The name of char you want to update
	 * @author Jvlaple
	 */
        
	private Connection con;
	//private long lastUpdate = System.currentTimeMillis();
	private static Logger log = LoggerFactory.getLogger(PlayerNPCWorker.class);
	
	public void run() {
		try {
			con = DatabaseConnection.getConnection();
			con.setAutoCommit(false);
			updateAllNPCs();
			con.commit();
			con.setAutoCommit(true);
			//lastUpdate = System.currentTimeMillis();
		} catch (SQLException ex) {
			try {
				con.rollback();
				con.setAutoCommit(true);
				log.warn("Could not update player NPCs", ex);
			} catch (SQLException ex2) {
				log.error("Could not rollback unfinished player npc transaction", ex2);
			}
		}
	}
        public static void updatePlayerNPC(String charname) {
                try {
                    //String npcname;
                    int npcid;
                    PreparedStatement update;
                    String query = "SELECT id, skincolor, hair, face FROM characters WHERE name=?";
                    String query1 = "SELECT itemid, position FROM inventoryitems WHERE characterid=? AND inventorytype = '-1' ORDER BY position DESC";
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setString(1, charname);
                    ResultSet results = ps.executeQuery();
                    results.next();
                    int id = results.getInt("id");
                    int skin = results.getInt("skincolor");
                    int hair = results.getInt("hair");
                    int face = results.getInt("face");
                    PreparedStatement ps1 = con.prepareStatement(query1);
                    ps1.setInt(1, id);
                    ResultSet rs = ps1.executeQuery();
                    String uq = "UPDATE playernpcs SET hair = ?, face = ?, skin = ? WHERE name = ?";
                    String findQ = "SELECT * FROM playernpcs WHERE name = ?";
                    PreparedStatement ps9 = con.prepareStatement(findQ);
                    ps9.setString(1, charname);
                    ResultSet rs1 = ps9.executeQuery();
                    rs1.next();
                    npcid = rs1.getInt("id");
                    rs1.close();
                    PreparedStatement ps2 = con.prepareStatement(uq);
                    ps2.setInt(1, hair);
                    ps2.setInt(2, face);
                    ps2.setInt(3, skin);
                    ps2.setString(4, charname);
                    ps2.executeUpdate();
                    String del = "DELETE FROM playernpcs_equip WHERE npcid = ?";
                    PreparedStatement shh = con.prepareStatement(del);
                    shh.setInt(1, npcid);
                    shh.executeUpdate();
                    int pos;
                    while (rs.next()) {
                        pos = 999;
                        if(rs.getInt("position") == -1 || rs.getInt("position") == -101) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -2 || rs.getInt("position") == -102) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -3 || rs.getInt("position") == -103) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -4 || rs.getInt("position") == -104) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -5 || rs.getInt("position") == -105) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -6 || rs.getInt("position") == -106) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -7 || rs.getInt("position") == -107) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -8 || rs.getInt("position") == -108) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -9 || rs.getInt("position") == -109) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -10 || rs.getInt("position") == -110) {
                            pos = rs.getInt("position");
                            if (pos > 100) {//NS check
                                PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = ?");
                                sss.setInt(1, npcid);
                                sss.setInt(2, abs(pos) - 100);
                                sss.executeUpdate();
                                sss.close();
                            }
                        }
                        else if(rs.getInt("position") == -11) {
                            pos = rs.getInt("position");
                        }
                        else if(rs.getInt("position") == -111) {
                            pos = rs.getInt("position");
                            PreparedStatement sss = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ? AND equippos = 11");
                            sss.setInt(1, npcid);
                            sss.executeUpdate();
                            sss.close();
                        } else {
                            continue;
                        }
                        int newpos = abs(pos);
                        if (newpos > 100) {newpos -= 100;}
                        update = con.prepareStatement("INSERT INTO playernpcs_equip(npcid, equipid, equippos, type) VALUES (?, ?, ?, 0);");
                        update.setInt(1, npcid);
                        update.setInt(2, rs.getInt("itemid"));
                        update.setInt(3, newpos);
                        update.executeUpdate();
                    }
                    results.close(); // Close all statements and resultsets.
                    rs.close();
                    rs1.close();
                    ps.close();
                    ps1.close();
                    ps2.close();
                    ps9.close();
                } catch (SQLException se) {
                    System.out.println("SEVERE ERROR : ");
                    System.out.println(se.toString());
                    return;
                }
        }
        
        public static void updateAllNPCs() throws SQLException { 
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement query = con.prepareStatement("SELECT * FROM playernpcs WHERE name != '';");
            ResultSet result = query.executeQuery();
            while (result.next()) {
                String name = result.getString("name");
                updatePlayerNPC(name);
            }
            System.out.println("Player NPCs have all been updated.");
            System.out.println("Closing SQL Connections.");
            query.close();
            result.close();
            //con.close();
        }
}
