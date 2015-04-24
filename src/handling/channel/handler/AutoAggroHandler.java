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

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;

public class AutoAggroHandler extends AbstractMaplePacketHandler {

	//private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AutoAggroHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		// A0 00 7C 00 00 00 0C 00 00 00
		int oid = slea.readInt();
		MapleMap map = c.getPlayer().getMap();
		MapleMonster monster = map.getMonsterByOid(oid);
		if (monster != null && monster.getController() != null) {
			if (!monster.isControllerHasAggro()) {
				if (map.getCharacterById(monster.getController().getId()) == null) {
					monster.switchController(c.getPlayer(), true);
				} else {
					monster.switchController(monster.getController(), true);
				}
			} else {
				if (map.getCharacterById(monster.getController().getId()) == null) {
					monster.switchController(c.getPlayer(), true);
				}
			}
		} else if (monster != null && monster.getController() == null) {
			monster.switchController(c.getPlayer(), true);
		}
	}
}