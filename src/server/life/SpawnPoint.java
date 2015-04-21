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

import java.awt.Point;
import java.util.concurrent.atomic.AtomicInteger;

import client.MapleCharacter;
import server.maps.MapleMap;

public class SpawnPoint {
	private MapleMonster monster;
	private Point pos;
	private long nextPossibleSpawn;
	private int mobTime;
	private AtomicInteger spawnedMonsters = new AtomicInteger(0);
	private int team;
		
	/**
	 * Wether the spawned monster is immobile
	 */
	private boolean immobile;
	private boolean temporary = false;
	
	public SpawnPoint(MapleMonster monster, Point pos, int mobTime, int team) {
		super();
		this.monster = monster;
		this.pos = new Point(pos);
		this.mobTime = mobTime;
		this.immobile = !monster.isMobile();
		this.nextPossibleSpawn = System.currentTimeMillis();
		this.team = team;
	}
	
	public SpawnPoint(MapleMonster monster, Point pos, int mobTime) {
		super();
		this.monster = monster;
		this.pos = new Point(pos);
		this.mobTime = mobTime;
		this.immobile = !monster.isMobile();
		this.nextPossibleSpawn = System.currentTimeMillis();
		this.team = -1;
	}

	public boolean shouldSpawn() {
		return shouldSpawn(System.currentTimeMillis());
	}
	
	// intentionally package private
	boolean shouldSpawn(long now) {
		if (mobTime < 0) {
			return false;
		}
		// regular spawnpoints should spawn a maximum of 3 monsters; immobile spawnpoints or spawnpoints with mobtime a
		// maximum of 1
		if (((mobTime != 0 || immobile) && spawnedMonsters.get() > 0) || spawnedMonsters.get() > 2) {
			return false;
		}
		return nextPossibleSpawn <= now;
	}

	/**
	 * Spawns the monster for this spawnpoint. Creates a new MapleMonster instance for that and returns it.
	 * 
	 * @param mapleMap
	 * @return
	 */
	public MapleMonster spawnMonster(MapleMap mapleMap) {
		MapleMonster mob = new MapleMonster(monster);
		mob.setPosition(new Point(pos));
		spawnedMonsters.incrementAndGet();
		mob.addListener(new MonsterListener() {
			@Override
			public void monsterKilled(MapleMonster monster, MapleCharacter highestDamageChar) {
				nextPossibleSpawn = System.currentTimeMillis();
				if (mobTime > 0) {
					nextPossibleSpawn += mobTime * 1000;
				} else {
					nextPossibleSpawn += monster.getAnimationTime("die1");
				}
				spawnedMonsters.decrementAndGet();
			}
		});
		if (team == 0 || team == 1) {
			mob.setTeam(team);
		}
		if (team != 0 && team != 1)
			mapleMap.spawnMonster(mob);
		else
			mapleMap.spawnCPQMonster(mob, team);
		if (mobTime == 0) {
			nextPossibleSpawn = System.currentTimeMillis() + 5000;
		}
		return mob;
	}
	
	public Point getPosition() {
		return pos;
	}
	
	public int getTeam() {
		return team;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}
}