/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * This file is part of the HurricaneMS Server.
 */

package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import client.MapleCharacter;
import database.DatabaseConnection;
import tools.MaplePacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David <Jvlaple>
 */
public class PlayerNPCEngine {
	public static final int WARRIOR_ID = 9901000;
	public static final int MAGICIAN_ID = 9901100;
	public static final int BOWMAN_ID = 9901200;
	public static final int THIEF_ID = 9901300;
	
	protected static Logger log = LoggerFactory.getLogger(PlayerNPCEngine.class);
	
	public static boolean createGeneralNPC(int ID, MapleCharacter chr) throws Throwable {
		List<Integer> taken = new LinkedList<Integer>();
		int mapId;
		switch (ID) {
			case WARRIOR_ID:
				mapId = 102000003;
				break;
			case MAGICIAN_ID:
				mapId = 101000003;
				break;
			case BOWMAN_ID:
				mapId = 100000201;
				break;
			case THIEF_ID:
				mapId = 103000003;
				break;
			default:
				throw new RuntimeException("Unknown type: " + ID);
		}
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps;
		ps = con.prepareStatement("SELECT * FROM playernpcs WHERE name = ?");
		ps.setString(1, chr.getName());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return false;
		}
		rs.close();
		ps = con.prepareStatement("SELECT * FROM playernpcs WHERE map = ?");
		ps.setInt(1, mapId);
		rs = ps.executeQuery();
		while (rs.next()) {
			taken.add(rs.getInt("npcid"));
		}
		if (taken.size() >= 19) {
			return false;
		}
		int freeID = -1;
		for (int i = 0; i < 19; i++) {
			if (taken.contains(ID + i)) {
				continue;
			} else {
				freeID = ID + i;
				break;
			}
		}
		if (freeID == -1) {
			return false;
		}
		rs.close();
		ps = con.prepareStatement("INSERT INTO playernpcs(name, hair, face, skin, dir, map, npcid)" + 
				"VALUES (?, ?, ?, ?, ?, ?, ?)");
		ps.setString(1, chr.getName());
		ps.setInt(2, chr.getHair());
		ps.setInt(3, chr.getFace());
		ps.setInt(4, chr.getSkinColor().getId());
		ps.setInt(5, MapleCharacter.rand(0, 1));
		ps.setInt(6, mapId);
		ps.setInt(7, freeID);
		ps.executeUpdate();
		ps.close();
		PlayerNPCWorker.updatePlayerNPC(chr.getName());
		ps = con.prepareStatement("SELECT * FROM playernpcs WHERE name = ?");
		ps.setString(1, chr.getName());
		rs = ps.executeQuery();
		int nid = -1;
		if (rs.next()) {
			nid = rs.getInt("id");
		}
		ps.close();
		rs.close();
		if (nid != -1)
			chr.getClient().getChannelServer().getMapFactory().getMap(mapId).broadcastMessage(MaplePacketCreator.getPlayerNPC(nid));
		return true;
	}
	
	public static boolean createWarriorNPC(MapleCharacter chr) {
		try {
			return createGeneralNPC(WARRIOR_ID, chr);
		} catch (Throwable shit) {
			log.error("Error creating PlayerNPC", shit);
			return false;
		}
	}
	
	public static boolean createThiefNPC(MapleCharacter chr) {
		try {
			return createGeneralNPC(THIEF_ID, chr);
		} catch (Throwable shit) {
			log.error("Error creating PlayerNPC", shit);
			return false;
		}
	}
	
	public static boolean createMagicianNPC(MapleCharacter chr) {
		try {
			return createGeneralNPC(MAGICIAN_ID, chr);
		} catch (Throwable shit) {
			log.error("Error creating PlayerNPC", shit);
			return false;
		}
	}
	
	public static boolean createBowmanNPC(MapleCharacter chr) {
		try {
			return createGeneralNPC(BOWMAN_ID, chr);
		} catch (Throwable shit) {
			log.error("Error creating PlayerNPC", shit);
			return false;
		}
	}
}
