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
package scripting.quest;

import client.MapleClient;
import scripting.npc.NPCConversationManager;
import server.quest.MapleQuest;

/**
 *
 * @author RMZero213
 */
public class QuestActionManager extends NPCConversationManager {
    private boolean start; // this is if the script in question is start or end
    private int quest;

    public QuestActionManager(MapleClient c, int npc, int quest, boolean start) {
        super(c, npc);
        this.quest = quest;
        this.start = start;
    }

    public int getQuest() {
        return quest;
    }

    public boolean isStart() {
        return start;
    }

    @Override
    public void dispose() {
        QuestScriptManager.getInstance().dispose(this, getClient());
    }

    public void forceStartQuest() {
        forceStartQuest(quest);
    }

    public void forceStartQuest(int id) {
        MapleQuest.getInstance(id).forceStart(getPlayer(), getNpc());
    }

    public void forceCompleteQuest() {
        forceCompleteQuest(quest);
    }

    public void forceCompleteQuest(int id) {
        MapleQuest.getInstance(id).forceComplete(getPlayer(), getNpc());
    }
}
