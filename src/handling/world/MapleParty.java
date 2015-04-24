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

package handling.world;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import client.MapleCharacter;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import tools.packet.MaplePacketCreator;

public class MapleParty implements Serializable {
	private static final long serialVersionUID = 9179541993413738569L;
	private MaplePartyCharacter leader;
	private List<MaplePartyCharacter> members = new LinkedList<MaplePartyCharacter>();
	private int id;
        private int CP;
        private int team;
        private int totalCP;
        private MapleParty enemy = null;
	
	public MapleParty(int id, MaplePartyCharacter chrfor) {
		this.leader = chrfor;
		this.members.add(this.leader);
		this.id = id;
	}
	
	public boolean containsMembers (MaplePartyCharacter member) {
		return members.contains(member);
	}
	
	public void addMember (MaplePartyCharacter member) {
		members.add(member);
	}
	
	public void removeMember (MaplePartyCharacter member) {
		members.remove(member);
	}
	
	public void updateMember(MaplePartyCharacter member) {
		for (int i = 0; i < members.size(); i++) {
			MaplePartyCharacter chr = members.get(i);
			if (chr.equals(member)) {
				members.set(i, member);
			}
		}
	}
	
	public MaplePartyCharacter getMemberById(int id) {
		for (MaplePartyCharacter chr : members) {
			if (chr.getId() == id) {
				return chr;
			}
		}
		return null;
	}
	
	public Collection<MaplePartyCharacter> getMembers () {
		return Collections.unmodifiableList(members);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
        
        public int getCP() {
            return this.CP;
        }

        public int getTeam() {
            return this.team;
        }

        public int getTotalCP() {
            return this.totalCP;
        }

        public void setCP(int cp) {
            this.CP = cp;
        }

        public void setTeam(int team) {
            this.team = team;
        }

        public void setTotalCP(int totalcp) {
            this.totalCP = totalcp;
        }

	public MapleParty getEnemy() {
		return enemy;
	}

	public void setEnemy(MapleParty enemy) {
		this.enemy = enemy;
	}
        
	public MaplePartyCharacter getLeader() {
		return leader;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MapleParty other = (MapleParty) obj;
		if (id != other.id)
			return false;
		return true;
	}

    	public void setLeader(MaplePartyCharacter nLeader) {
		leader = nLeader;
	}

    	public MaplePartyCharacter getMemberByPos(int pos) {
		int i = 0;
		for (MaplePartyCharacter chr : members) {
			if (pos == i) {
				return chr;
			}
			i++;
		}
		return null;		
	}

}
