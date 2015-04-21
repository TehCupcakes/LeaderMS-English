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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.life;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import database.DatabaseConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jvlaple
 */
public class MapleMonsterQuestInformationProvider {

    public static class QuestDropEntry {

        public QuestDropEntry(int itemId, int chance, int questid) {
            this.itemId = itemId;
            this.chance = chance;
            this.questid = questid;
        }

        public int itemId;
        public int chance;
        public int questid;
        public int assignedRangeStart;
        public int assignedRangeLength;

        @Override
        public String toString() {
            return itemId + " chance: " + chance;
        }
    }

    public static final int APPROX_FADE_DELAY = 90;

    private static MapleMonsterQuestInformationProvider instance = null;

    private Map<Integer, List<QuestDropEntry>> drops = new HashMap<Integer, List<QuestDropEntry>>();

    private static final Logger log = LoggerFactory.getLogger(MapleMonsterInformationProvider.class);

    private MapleMonsterQuestInformationProvider() {

    }

    public static MapleMonsterQuestInformationProvider getInstance() {
        if (instance == null) {
            instance = new MapleMonsterQuestInformationProvider();
        }
        return instance;
    }

    public List<QuestDropEntry> retrieveDropChances(int monsterId) {
        if (drops.containsKey(monsterId)) {
            return drops.get(monsterId);
        }
        List<QuestDropEntry> ret = new LinkedList<QuestDropEntry>();
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT itemid, chance, monsterid, questid FROM monsterquestdrops "
                    + "WHERE (monsterid = ? AND chance >= 0) OR (monsterid <= 0)");
            ps.setInt(1, monsterId);
            ResultSet rs = ps.executeQuery();
            MapleMonster theMonster = null;
            while (rs.next()) {
                int rowMonsterId = rs.getInt("monsterid");
                int chance = rs.getInt("chance");
                int questid = rs.getInt("questid");
                if (rowMonsterId != monsterId && rowMonsterId != 0) {
                    if (theMonster == null) {
                        theMonster = MapleLifeFactory.getMonster(monsterId);
                    }
                    chance += theMonster.getLevel() * rowMonsterId;
                }
                ret.add(new QuestDropEntry(rs.getInt("itemid"), chance, questid));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            log.error("ERROR", e);
        }
        drops.put(monsterId, ret);
        return ret;
    }

    public void clearDrops() {
        drops.clear();
    }
}
