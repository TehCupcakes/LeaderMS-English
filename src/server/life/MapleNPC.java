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

package server.life;

import client.MapleClient;
import server.MapleShopFactory;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

public class MapleNPC extends AbstractLoadedMapleLife {
	private MapleNPCStats stats;
	private boolean custom = false;
	
	public MapleNPC(int id, MapleNPCStats stats) {
		super(id);
		this.stats = stats;
	}
	
	public boolean hasShop() {
		return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
	}
	
	public void sendShop(MapleClient c) {
		MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
        int[] tv = {9201066, 9250023, 9250024, 9250025, 9250026, 9250042, 9250043, 9250044, 9250045, 9250046, 9270000, 9270001, 9270002, 9270003, 9270004, 9270005, 9270006, 9270007, 9270008, 9270009, 9270010, 9270011, 9270012, 9270013, 9270014, 9270015, 9270016, 9270040, 9270066};
        for(int t = 0; t < tv.length; t++ ){
        if (getId() == tv[t]){
            return;
             }
        }
	if (getId() >= 9901000 && getId() <= 9901551) {
	    if (!stats.getName().equals("")) {
		client.getSession().write(MaplePacketCreator.spawnNPCRequestController(this, false));
	    }
	} else {
	    client.getSession().write(MaplePacketCreator.spawnNPC(this, true));
	    client.getSession().write(MaplePacketCreator.spawnNPCRequestController(this, true));
	}
    }
	
	@Override
	public void sendDestroyData(MapleClient client) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.NPC;
	}
	
	public String getName() {
		return stats.getName();
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}
	
	@Override public String toString() {
		return getName() + " ID: " + getId() + " Position: (" + this.getPosition().x + ", " + this.getPosition().y + ") Oid: " + this.getObjectId();
	}
}