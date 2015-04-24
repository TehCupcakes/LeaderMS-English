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

package handling.login.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AfterLoginHandler extends AbstractMaplePacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AfterLoginHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		byte c2 = slea.readByte();
		byte c3 = slea.readByte();
		if (c2 == 1 && c3 == 1) {
			c.getSession().write(MaplePacketCreator.pinAccepted());
		} else if (c2 == 1 && c3 == 0) {
			slea.seek(8);
			String pin = slea.readMapleAsciiString();
			log.info("Received Pin: " + pin);
			if (pin.equals("1234")) {
				c.getSession().write(MaplePacketCreator.pinAccepted());
			} else {
				c.getSession().write(MaplePacketCreator.requestPinAfterFailure());
			}
		} else {
		}
	}
}
