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

package net.channel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import client.MapleCharacter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerStorage implements IPlayerStorage {
        private final Lock mutex = new ReentrantLock();
        private final Lock mutex2 = new ReentrantLock();
        private final Map<String, MapleCharacter> nameToChar = new HashMap<String, MapleCharacter>();
        private final Map<Integer, MapleCharacter> idToChar = new HashMap<Integer, MapleCharacter>();


    public final void registerPlayer(final MapleCharacter chr) {
	mutex.lock();
	try {
	    nameToChar.put(chr.getName().toLowerCase(), chr);
	    idToChar.put(chr.getId(), chr);
	} finally {
	    mutex.unlock();
	}
    }
	public final void deregisterPlayer(final MapleCharacter chr) {
	mutex.lock();
	try {
	    nameToChar.remove(chr.getName().toLowerCase());
	    idToChar.remove(chr.getId());
	} finally {
	    mutex.unlock();
	}
    }
	public MapleCharacter getCharacterByName(String name) {
		return nameToChar.get(name.toLowerCase());
	}

	public MapleCharacter getCharacterById(int id) {
		return idToChar.get(Integer.valueOf(id));
	}

	public Collection<MapleCharacter> getAllCharacters() {
		return nameToChar.values();
	}
        
       public final int getConnectedClients() {
	return idToChar.size();
    }

     public final void disconnectAll() {
	mutex.lock();
	try {
	    final Iterator<MapleCharacter> itr = nameToChar.values().iterator();
	    MapleCharacter chr;
	    while (itr.hasNext()) {
		chr = itr.next();

		if (!chr.isGM()) {
		    chr.getClient().disconnect(false);
		    chr.getClient().getSession().close();
		    itr.remove();
		}
	    }
	} finally {
	    mutex.unlock();
	}
    }
}
