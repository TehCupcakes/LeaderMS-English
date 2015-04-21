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
package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import scripting.quest.QuestScriptManager;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class QuestActionHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte action = slea.readByte();
        short quest = slea.readShort();
        //System.out.println("Quest Run: " + slea);
        MapleCharacter player = c.getPlayer();
        if (action == 1) { //Start Quest
            int npc = slea.readInt();
            if (slea.available() >= 4) {
                slea.readInt();
            } else if (npc == 1090000) {
                slea.readShort();
                slea.readShort();
            } else {
                slea.readShort();
            }
            MapleQuest.getInstance(quest).start(player, npc);
        } else if (action == 2) { // Complete Quest
            int npc = slea.readInt();
            slea.readInt();
            if (slea.available() >= 4) {
                int selection = slea.readInt();
                MapleQuest.getInstance(quest).complete(player, npc, selection);
            } else {
                MapleQuest.getInstance(quest).complete(player, npc);
            }
        } else if (action == 3) {// forfeit quest
            MapleQuest.getInstance(quest).forfeit(player);
        } else if (action == 4) { // scripted start quest
            //System.out.println(slea.toString());
            int npc = slea.readInt();
            slea.readInt();
            QuestScriptManager.getInstance().start(c, npc, quest);
        } else if (action == 5) { // scripted end quests
            //System.out.println(slea.toString());
            int npc = slea.readInt();
            slea.readInt();
            QuestScriptManager.getInstance().end(c, npc, quest);
        }
    }
}
