/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import server.quest.MapleQuest;
import tools.StringUtil;

/**
 *
 * @author Matze
 */
public class MapleQuestStatus {
    public enum Status {
        UNDEFINED(-1),
        NOT_STARTED(0),
        STARTED(1),
        COMPLETED(2);
        final int status;

        private Status(int id) {
            status = id;
        }

        public int getId() {
            return status;
        }

        public static Status getById(int id) {
            for (Status l : Status.values()) {
                if (l.getId() == id) {
                    return l;
                }
            }
            return null;
        }
    }
    private MapleQuest quest;
    private Status status;
    private Map<Integer, Integer> killedMobs = new LinkedHashMap<Integer, Integer>();
    private int npc;
    private long completionTime;
    private int forfeited = 0;

    public MapleQuestStatus(MapleQuest quest, Status status) {
        this.quest = quest;
        this.setStatus(status);
        this.completionTime = System.currentTimeMillis();
        if (status == Status.STARTED) {
            registerMobs();
        }
    }

    public MapleQuestStatus(MapleQuest quest, Status status, int npc) {
        this.quest = quest;
        this.setStatus(status);
        this.setNpc(npc);
        this.completionTime = System.currentTimeMillis();
        if (status == Status.STARTED) {
            registerMobs();
        }
    }

    public MapleQuest getQuest() {
        return quest;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getNpc() {
        return npc;
    }

    public void setNpc(int npc) {
        this.npc = npc;
    }

    private void registerMobs() {
        for (int i : quest.getRelevantMobs()) {
            killedMobs.put(i, 0);
        }
    }

    public boolean mobKilled(int id) {
        if (killedMobs.get(id) != null) {
            killedMobs.put(id, killedMobs.get(id) + 1);
            return true;
        }
        return false;
    }

    public void setMobKills(int id, int count) {
        killedMobs.put(id, count);
    }

    public boolean hasMobKills() {
        return killedMobs.size() > 0;
    }

    public int getMobKills(int id) {
        if (killedMobs.get(id) == null) {
            return 0;
        }
        return killedMobs.get(id);
    }

    public Map<Integer, Integer> getMobKills() {
        return Collections.unmodifiableMap(killedMobs);
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public int getForfeited() {
        return forfeited;
    }

    public void setForfeited(int forfeited) {
        if (forfeited >= this.forfeited) {
            this.forfeited = forfeited;
        } else {
            throw new IllegalArgumentException("Can't set forfeits to something lower than before.");
        }
    }

    public String getQuestData() {
        if (killedMobs.size() == 0) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        for (Integer mob : killedMobs.values()) {
            str.append(StringUtil.getLeftPaddedStr(mob.toString(), '\30', 3));
        }
        return str.toString();
    }
}