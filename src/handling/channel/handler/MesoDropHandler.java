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
 * MesoDropHandler.java
 *
 * Created on 8. Dezember 2007, 14:57
 */

package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.AutobanManager;
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */

public class MesoDropHandler extends AbstractMaplePacketHandler {
	
	public MesoDropHandler() {
	}

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt(); // i don't know :)
		int meso = slea.readInt();
		if (!c.getPlayer().isAlive() || c.getPlayer().getCheatTracker().Spam(500, 2)) {
                     c.getSession().write(MaplePacketCreator.enableActions());
                  return;
                }
		if (!c.getPlayer().isAlive()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		if (meso < 10 || meso > 50000) {
                        AutobanManager.getInstance().addPoints(c, 1000, 0, "Dropando " + meso + " mesos!");
                        return;
                }
		if (meso <= c.getPlayer().getMeso()) {
			c.getPlayer().gainMeso(-meso, false, true);
			c.getPlayer().getMap().spawnMesoDrop(meso, meso, c.getPlayer().getPosition(), c.getPlayer(),c.getPlayer(), false);
		} else {
			//we should disconnect here though...
			c.disconnect();
			return;
		}
	}
}
