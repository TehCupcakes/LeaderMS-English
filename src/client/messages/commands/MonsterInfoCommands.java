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

import java.util.Arrays;
import java.util.List;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;

public class MonsterInfoCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		if (splitted[0].equals("!killall") || splitted[0].equals("!monsterdebug")) {
			String mapMessage = "";
			MapleMap map = c.getPlayer().getMap();
			double range = Double.POSITIVE_INFINITY;
			boolean drop = false;
			boolean diffMap = false;
			if (splitted.length > 1) {
				if (splitted[1].equalsIgnoreCase("-drop")) {
					drop = true;
				} else if (splitted[1].equalsIgnoreCase("-map")) {
					diffMap = true;
					map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
				}
			}
			if (splitted.length > 1 && !drop && !diffMap) {
				int irange = Integer.parseInt(splitted[1]);
				if (splitted.length <= 2)                              
				  range = irange * irange;
				else
				{
				  map = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(splitted[2]));
				  mapMessage = " in " + map.getStreetName() + " : " + map.getMapName();
				}
			}
			List<MapleMapObject> monsters = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays
				.asList(MapleMapObjectType.MONSTER));
			boolean kill = splitted[0].equals("!killall");
			for (MapleMapObject monstermo : monsters) {
				MapleMonster monster = (MapleMonster) monstermo;
				if (kill) {
					map.killMonster(monster, c.getPlayer(), drop);
				} else {
					mc.dropMessage("Monster " + monster.toString());
				}
			}
			if (kill) {
				mc.dropMessage("Killed " + monsters.size() + " monsters" + mapMessage + ".");
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("killall", "[range] [mapid]", "Kills all monsters. When mapid specified, range ignored.", 1),
			new CommandDefinition("monsterdebug", "[range]", "", 1),
		};
	}

}
