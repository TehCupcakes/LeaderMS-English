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

package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;

public class CharInfoCommand implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splittedLine) throws Exception,
																					IllegalCommandSyntaxException {
		try {
			StringBuilder builder = new StringBuilder();
			MapleCharacter other = c.getChannelServer().getPlayerStorage().getCharacterByName(splittedLine[1]);

			builder.append(MapleClient.getLogMessage(other, ""));
			builder.append(" at ");
			builder.append(other.getPosition().x);
			builder.append("/");
			builder.append(other.getPosition().y);
			builder.append(" FootHold: ");
			builder.append(other.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
			builder.append(" ");
			builder.append("RX0:");
			builder.append(other.getPosition().x + 50);
			builder.append(" ");
			builder.append("RX1:");
			builder.append(other.getPosition().x - 50);
			builder.append(" ");
			builder.append(other.getHp());
			builder.append("/");
			builder.append(other.getCurrentMaxHp());
			builder.append(" hp ");
			builder.append(other.getMp());
			builder.append("/");
			builder.append(other.getCurrentMaxMp());
			builder.append("mp ");
			builder.append(other.getExp());
			builder.append("exp hasParty: ");
			builder.append(other.getParty() != null);
			builder.append(" hasTrade: ");
			builder.append(other.getTrade() != null);
			builder.append(" remoteAddress: ");
			builder.append(other.getClient().getSession().getRemoteAddress());
			mc.dropMessage(builder.toString());
			other.getClient().dropDebugMessage(mc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("charinfo", "charname", "Shows info about the charcter with the given name", 50),
		};
	}
}
