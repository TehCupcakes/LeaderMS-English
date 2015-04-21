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

import java.sql.SQLException;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
*
* @author Penguins (Acrylic)
*/
public class CouponCodeHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.skip(2);
		String code = slea.readMapleAsciiString();
		boolean validcode = false;
		int type = -1;
		int item = -1;
		
		try {
			validcode = c.getPlayer().getNXCodeValid(code.toUpperCase(), validcode);
		} catch (SQLException e) {}
		
		if (validcode) {
			try {
				type = c.getPlayer().getNXCodeType(code);
			} catch (SQLException e) {}
			try {
				item = c.getPlayer().getNXCodeItem(code);
			} catch (SQLException e) {}
			if (type != 5) {
				try {
					c.getPlayer().setNXCodeUsed(code);
				} catch (SQLException e) {}
			}
			/*
			 * Explanation of type!
			 * Basically, this makes coupon codes do
			 * different things!
			 * 
			 * Type 0: NX, Type 1: Maple Points,
			 * Type 2: Gift Tokens, Type 3: NX + Gift Tokens
			 * Type 4: Item
			 * Type 5: NX Coupon that can be used over and over
			 * 
			 * When using Types 0-3, the item is the amount
			 * of NX or Maple Points you get. When using Type 4
			 * the item is the ID of the item you get. Enjoy!
			 */
			switch(type) {
			    case 0:
			    case 1:
			    case 2:
				c.getPlayer().modifyCSPoints(type, item);
				break;
			    case 3:
				c.getPlayer().modifyCSPoints(0, item);
				c.getPlayer().modifyCSPoints(2, (item/5000));
				break;
			    case 4:
                                MapleInventoryManipulator.addById(c, item, (short) 1, "Um item foi obtido a partir de um cupom.", null, -1);
                                
				c.getSession().write(MaplePacketCreator.showCouponRedeemedItem(item));
				break;
			    case 5:
				c.getPlayer().modifyCSPoints(0, item);
				break;
                            case 6:
                                MapleInventoryManipulator.addById(c, item, (short) 1, "Um item foi obtido a partir de um cupom.", null, -1);
                                break;
			}
			c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
		} else {
			c.getSession().write(MaplePacketCreator.wrongCouponCode());
		}

		c.getSession().write(MaplePacketCreator.enableCSUse0());
		c.getSession().write(MaplePacketCreator.enableCSUse1());
		c.getSession().write(MaplePacketCreator.enableCSUse2());
	}
}
