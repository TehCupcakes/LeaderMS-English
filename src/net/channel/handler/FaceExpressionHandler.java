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

package net.channel.handler;

import java.rmi.RemoteException;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class FaceExpressionHandler extends AbstractMaplePacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FaceExpressionHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int emote = slea.readInt();
		if (emote > 7) {
			int emoteid = 5159992 + emote;
			MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(emoteid);
			MapleInventory iv = c.getPlayer().getInventory(type);
			if (iv.findById(emoteid) == null) {
                                try {
                                    c.getPlayer().getClient().getChannelServer().getWorldInterface().broadcastGMMessage("", MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " is using an unavailable item, ItemID: " + emoteid).getBytes());
                                } catch (RemoteException ex) {
                                    c.getPlayer().getClient().getChannelServer().reconnectWorld();
                                }
				c.getSession().close();
				return;
			}
		}
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.facialExpression(c.getPlayer(), emote), false);
	}
}
