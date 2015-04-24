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
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharInfoRequestHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readShort(); // most likely two shorts rather than one int but dunno ^___^
		slea.readShort();
		int cid = slea.readInt();
		boolean me = cid == c.getPlayer().getId();
		MapleCharacter player = (MapleCharacter) c.getPlayer().getMap().getMapObject(cid);
              //  c.getPlayer().dropMessage("O jogador possui a ocupacao - " + c.getPlayer().getOccupation() + ".");
		if (!player.isGM() || (c.getPlayer().isGM() && player.isGM()))
			c.getSession().write(MaplePacketCreator.charInfo(player));
		else
			c.getSession().write(MaplePacketCreator.enableActions());
		return;
	}
}