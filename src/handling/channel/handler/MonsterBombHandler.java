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

import java.rmi.RemoteException;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class MonsterBombHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int oid = slea.readInt();

		MapleMap map = c.getPlayer().getMap();
		MapleMonster monster = map.getMonsterByOid(oid);

		if (!c.getPlayer().isAlive() || monster == null) {
			return;
		}

		switch (monster.getId()) {
			case 8500003:
			case 8500004:
				monster.getMap().broadcastMessage(MaplePacketCreator.killMonster(monster.getObjectId(), 4));
				map.removeMapObject(oid);
				break;
			default:
                                try {
                                    c.getChannelServer().getWorldInterface().broadcastGMMessage("", MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " is using mob instant kill hack, mobID: " + monster.getId()).getBytes());
                                } catch (RemoteException ex) {
                                    c.getChannelServer().reconnectWorld();
                                }
				c.getSession().close();
				break;
		}
	}
}