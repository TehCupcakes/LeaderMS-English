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

import client.ISkill;
import client.MapleClient;
import client.SkillFactory;
import handling.AbstractMaplePacketHandler;
import handling.MaplePacketHandler;
import server.MapleStatEffect;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.MaplePacketCreator;

public class CancelBuffHandler extends AbstractMaplePacketHandler implements MaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int sourceid = slea.readInt();
		MapleStatEffect effect;
		ISkill skill = SkillFactory.getSkill(sourceid);
		
		if (sourceid == 3121004 || sourceid == 3221001 || sourceid == 2121001 || sourceid == 2221001 || sourceid == 2321001  || sourceid == 5221004) {
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillCancel(c.getPlayer(), sourceid), false);
		}
		
		effect = skill.getEffect(1); // hack but we don't know the level that was casted on us ï¿½.o
		c.getPlayer().cancelEffect(effect, false, -1);
	}
}