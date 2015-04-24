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

package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.world.MaplePartyCharacter;
import scripting.npc.NPCScriptManager;
import scripting.quest.QuestScriptManager;
import server.PlayerNPCEngine;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.maps.PlayerNPCMerchant;
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

// 0 = next button
// 1 = yes no
// 2 = accept decline
// 5 = select a link
// c.getSession().write(MaplePacketCreator.getNPCTalk(npc.getId(), (byte) 0, "Yo! I'm #p" + npc.getId() + "#, lulz! I can warp you lululululu.", "00 01"));

public class NPCTalkHandler extends AbstractMaplePacketHandler {
	
	protected static final String pnpcText = "Hello? I achieved level 200. it was really hard to get level 200. Passed through trial and sufferings, finally I made it. You too can be a master if you work hard. Good luck!!";

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int oid = slea.readInt();
		slea.readInt(); // dont know
		MapleMapObject obj = c.getPlayer().getMap().getMapObject(oid);
		MapleNPC npc;
		if (obj instanceof MapleNPC)
			npc = (MapleNPC) obj;
		else
			npc = ((PlayerNPCMerchant)obj).getNPC();
		if (System.currentTimeMillis() - c.getPlayer().getLastNpcTalk() < 1000) {
			return;
		}
		c.getPlayer().setLastNpcTalk(System.currentTimeMillis());
                if (npc.getId() == 9010009) {
                    if (c.isGuest()) {
                        c.getPlayer().dropMessage(1, "Duey is not available to Guests");
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                } else {
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
//                if (npc.getId() == 2042000 || npc.getId() == 2042001 || npc.getId() == 2042002) {
//			 c.getPlayer().getClient().getSession().write(MaplePacketCreator.getNPCTalk(9010009, (byte) 0, "Ola " + c.getPlayer().getName() + ", aguarde o lancamento oficial da CPQ!", "00 00"));
//			return;
//		}
                 if (npc.getId() == 2042004 || npc.getId() == 2042003) {
			for (MapleMapObject o : c.getPlayer().getMap().getAllPlayer()) {
				((MapleCharacter)o).changeMap(c.getChannelServer().getMapFactory().getMap(980000000),
						c.getChannelServer().getMapFactory().getMap(980000000).getPortal(0));
				((MapleCharacter)o).getClient().getSession().write(MaplePacketCreator.serverNotice(5, "[Monster Carnival] " + c.getPlayer().getName() + " clicked the warp NPC."));	
			}
			return;
		}
		        boolean tutMap = false;
        switch (c.getPlayer().getMapId()) {
            case 102000003:
            case 101000003:
            case 100000201:
            case 103000003:
                tutMap = true;
                break;
        }
        if (tutMap && npc.getId() >= PlayerNPCEngine.WARRIOR_ID && npc.getId() <= PlayerNPCEngine.THIEF_ID + 19) {
            c.getSession().write(MaplePacketCreator.getNPCTalk(npc.getId(), (byte) 0, pnpcText, "00 00"));
            return;
        }
        if (c.getCM() != null) {
            NPCScriptManager.getInstance().dispose(c);
        } else if (c.getQM() != null) {
            QuestScriptManager.getInstance().dispose(c);
        } else if (c.getPlayer().getShop() != null) {
            c.getPlayer().setShop(null);
            c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 20));
        }
        if (npc.hasShop()) {
            npc.sendShop(c);
        } else {
            NPCScriptManager.getInstance().start(c, npc.getId(), null, null);
        }
    }
}
