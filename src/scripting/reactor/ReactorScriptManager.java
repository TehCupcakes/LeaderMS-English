/*
 * This file is part of the OdinMS Maple Story Server
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

package scripting.reactor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;

import client.MapleClient;
import database.DatabaseConnection;
import scripting.AbstractScriptManager;
import server.life.MapleMonsterInformationProvider.DropEntry;
import server.life.MapleMonsterQuestInformationProvider.QuestDropEntry;
import server.maps.MapleReactor;
import tools.FilePrinter;

/**
 * @author Lerk
 */

public class ReactorScriptManager extends AbstractScriptManager {
	private static ReactorScriptManager instance = new ReactorScriptManager();
	private Map<Integer, List<DropEntry>> drops = new HashMap<Integer, List<DropEntry>>();
	private Map<Integer, List<QuestDropEntry>> questDrops = new HashMap<Integer, List<QuestDropEntry>>();

	public synchronized static ReactorScriptManager getInstance() {
		return instance;
	}

	public void act(MapleClient c, MapleReactor reactor) {
		try {
			ReactorActionManager rm = new ReactorActionManager(c, reactor);

			Invocable iv = getInvocable("reactor/" + reactor.getId() + ".js", c);
			if (iv == null) {
				return;
			}
			engine.put("rm", rm);
			ReactorScript rs = iv.getInterface(ReactorScript.class);
			rs.act();
		} catch (Exception e) {
                        FilePrinter.printError(FilePrinter.REACTOR, "Error executing reactor script. ReactorID: " + reactor.getReactorId() + ", ReactorName: " + reactor.getName() + ":" + e);
		}
	}

	public List<DropEntry> getDrops(int rid) {
		List<DropEntry> ret = drops.get(rid);
		if (ret == null) {
			ret = new LinkedList<DropEntry>();
			try {
				Connection con = DatabaseConnection.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT itemid, chance FROM reactordrops WHERE reactorid = ? AND chance >= 0");
				ps.setInt(1, rid);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					ret.add(new DropEntry(rs.getInt("itemid"), rs.getInt("chance")));
				}
				rs.close();
				ps.close();
			} catch (Exception e) {
				log.error("Could not retrieve drops for reactor " + rid, e);
			}
			drops.put(rid, ret);
		}
		return ret;
	}
	
	public List<QuestDropEntry> getQuestDrops(int rid) {
		List<QuestDropEntry> ret = questDrops.get(rid);
		if (ret == null) {
			ret = new LinkedList<QuestDropEntry>();
			try {
				Connection con = DatabaseConnection.getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT itemid, chance, reactorid, questid FROM reactorquestdrops " +
				"WHERE (reactorid = ? AND chance >= 0) OR (reactorid <= 0)");
				ps.setInt(1, rid);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					ret.add(new QuestDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("questid")));
				}
				rs.close();
				ps.close();
			} catch (Exception e) {
				log.error("Could not retrieve drops for reactor " + rid, e);
			}
			questDrops.put(rid, ret);
		}
		return ret;
	}
	
	public void clearDrops() {
		drops.clear();
	}
}
