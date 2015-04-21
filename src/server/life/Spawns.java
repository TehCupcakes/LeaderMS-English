/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 ~ 2010 Patrick Huy &lt;patrick.huy@frz.cc&gt; 
                       Matthias Butz &lt;matze@odinms.de&gt;
                       Jan Christian Meyer &lt;vimes@odinms.de&gt;

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
    along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
*/
package server.life;

import server.maps.MapleMap;


public abstract class Spawns {
    public abstract MapleMonster getMonster();
    public abstract byte getCarnivalTeam();
    public abstract boolean shouldSpawn();
    public abstract MapleMonster spawnMonster(MapleMap map);
}