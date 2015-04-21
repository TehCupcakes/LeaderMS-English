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

package client.anticheat;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ScheduledFuture;
import client.MapleCharacter;
import java.rmi.RemoteException;
import server.AutobanManager;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class CheatTracker {
	private Map<CheatingOffense, CheatingOffenseEntry> offenses = Collections.synchronizedMap(new LinkedHashMap<CheatingOffense, CheatingOffenseEntry>());
	private WeakReference<MapleCharacter> chr;

	private long regenHPSince;
	private long regenMPSince;
	private int numHPRegens;
	private int numMPRegens;
	private int numSequentialAttacks;
	private long lastAttackTime;
	private long lastDamage = 0;
        private int sameDamage = 0;
        private int lastSameDamage = -1;
	private long takingDamageSince;
	private int numSequentialDamage = 0;
	private long lastDamageTakenTime = 0;
	private int numSequentialSummonAttack = 0;
	private long summonSummonTime = 0;
	private int numSameDamage = 0;
	private long attackingSince;
	private Point lastMonsterMove;
	private int monsterMoveCount;
	private int attacksWithoutHit = 0;
	private Boolean pickupComplete = Boolean.TRUE;
        private long[] lastTime = new long[6];
	
	private ScheduledFuture<?> invalidationTask;

	public CheatTracker(MapleCharacter chr) {
		this.chr = new WeakReference<MapleCharacter>(chr);
		invalidationTask = TimerManager.getInstance().register(new InvalidationTask(), 60000);
		takingDamageSince = attackingSince = regenMPSince = regenHPSince = System.currentTimeMillis();
	}

	public boolean checkAttack(int skillId) {
		numSequentialAttacks++;

		long oldLastAttackTime = lastAttackTime;
		lastAttackTime = System.currentTimeMillis();
		long attackTime = lastAttackTime - attackingSince;
		if (numSequentialAttacks > 3) {
			// System.out.println(attackTime);
			// System.out.println(numSequentialAttacks);
			// System.out.println(attackTime / 400 + "(" + attackTime / numSequentialAttacks + ")");
			final int divisor;
			if (skillId == 3121004 || skillId == 5221004) { // hurricane
				divisor = 10;
			} else {
				divisor = 300;
			}
			if (attackTime / divisor < numSequentialAttacks) {
				registerOffense(CheatingOffense.FASTATTACK);
				return false;
			}
		}
		if (lastAttackTime - oldLastAttackTime > 1500) {
			attackingSince = lastAttackTime;
			numSequentialAttacks = 0;
		}
		return true;
	}
	
	public void checkTakeDamage() {
		numSequentialDamage++;
		long oldLastDamageTakenTime = lastDamageTakenTime;
		lastDamageTakenTime = System.currentTimeMillis();
		
		long timeBetweenDamage = lastDamageTakenTime - takingDamageSince;

		// System.out.println("tb" + timeBetweenDamage);
		// System.out.println("ns" + numSequentialDamage);
		// System.out.println(timeBetweenDamage / 1500 + "(" + timeBetweenDamage / numSequentialDamage + ")");

		if (timeBetweenDamage / 1000 < numSequentialDamage) {
			registerOffense(CheatingOffense.FAST_TAKE_DAMAGE);
		}
		
		if (lastDamageTakenTime - oldLastDamageTakenTime > 4500) {
			takingDamageSince = lastDamageTakenTime;
			numSequentialDamage = 0;
		}
	}

	public int checkDamage(long dmg) {
		if (lastDamage == dmg)
			numSameDamage++;
		else {
			lastDamage = dmg;
			numSameDamage = 0;
		}
		return numSameDamage;
	}

	public void checkMoveMonster(Point pos) {
		if (pos.equals(lastMonsterMove)) {
			monsterMoveCount++;
			if (monsterMoveCount > 3) {
				registerOffense(CheatingOffense.MOVE_MONSTERS);
			}
		} else {
			lastMonsterMove = pos;
			monsterMoveCount = 1;
		}
	}

	public boolean checkHPRegen() {
		numHPRegens++;
		if ((System.currentTimeMillis() - regenHPSince) / 10000 < numHPRegens) {
			registerOffense(CheatingOffense.FAST_HP_REGEN);
			return false;
		}
		return true;
	}

	public void resetHPRegen() {
		regenHPSince = System.currentTimeMillis();
		numHPRegens = 0;
	}

	public boolean checkMPRegen() {
		numMPRegens++;
		long allowedRegens = (System.currentTimeMillis() - regenMPSince) / 10000;
		// System.out.println(numMPRegens + "/" + allowedRegens);
		if (allowedRegens < numMPRegens) {
			registerOffense(CheatingOffense.FAST_MP_REGEN);
			return false;
		}
		return true;
	}

	public void resetMPRegen() {
		regenMPSince = System.currentTimeMillis();
		numMPRegens = 0;
	}

	public void resetSummonAttack() {
		summonSummonTime = System.currentTimeMillis();
		numSequentialSummonAttack = 0;
	}
	
	public boolean checkSummonAttack() {
		numSequentialSummonAttack++;
		//estimated
		long allowedAttacks = (System.currentTimeMillis() - summonSummonTime) / 3000 + 1;
		// System.out.println(numMPRegens + "/" + allowedRegens);
		if (allowedAttacks < numSequentialAttacks) {
			registerOffense(CheatingOffense.FAST_SUMMON_ATTACK);
			return false;
		}
		return true;
	}
	
	public void checkPickupAgain() {
		synchronized (pickupComplete) {
			if (pickupComplete) {
				pickupComplete = Boolean.FALSE;
			} else {
				registerOffense(CheatingOffense.TUBI);
			}
		}
	}

	public void pickupComplete() {
		synchronized (pickupComplete) {
			pickupComplete = Boolean.TRUE;
		}
	}

	public int getAttacksWithoutHit() {
		return attacksWithoutHit;
	}

	public void setAttacksWithoutHit(int attacksWithoutHit) {
		this.attacksWithoutHit = attacksWithoutHit;
	}

	public void registerOffense(CheatingOffense offense) {
		registerOffense(offense, null);
	}

	public void registerOffense(CheatingOffense offense, String param) {
		MapleCharacter chrhardref = chr.get();
		if (chrhardref == null || !offense.isEnabled()) {
			return;
		}
		
		CheatingOffenseEntry entry = offenses.get(offense);
		if (entry != null && entry.isExpired()) {
			expireEntry(entry);
			entry = null;
		}
		if (entry == null) {
			entry = new CheatingOffenseEntry(offense, chrhardref);
		}
		if (param != null) {
			entry.setParam(param);
		}
		entry.incrementCount();
		if (offense.shouldAutoban(entry.getCount())) {
			AutobanManager.getInstance().autoban(chrhardref.getClient(), StringUtil.makeEnumHumanReadable(offense.name()));
		}
		offenses.put(offense, entry);
		CheatingOffensePersister.getInstance().persistEntry(entry);
	}

	public void expireEntry(CheatingOffenseEntry coe) {
		offenses.remove(coe.getOffense());
	}

	public int getPoints() {
		int ret = 0;
		CheatingOffenseEntry []offenses_copy;
		synchronized (offenses) {
			offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
		}
		for (CheatingOffenseEntry entry : offenses_copy) {
			if (entry.isExpired()) {
				expireEntry(entry);
			} else {
				ret += entry.getPoints();
			}
		}
		return ret;
	}
	
	public Map<CheatingOffense, CheatingOffenseEntry> getOffenses() {
		return Collections.unmodifiableMap(offenses);
	}
	
	public String getSummary() {
		StringBuilder ret = new StringBuilder();
		List<CheatingOffenseEntry> offenseList = new ArrayList<CheatingOffenseEntry>();
		synchronized (offenses) {
			for (CheatingOffenseEntry entry : offenses.values()) {
				if (!entry.isExpired()) {
					offenseList.add(entry);
				}
			}
		}
		Collections.sort(offenseList, new Comparator<CheatingOffenseEntry>() {
			@Override
			public int compare(CheatingOffenseEntry o1, CheatingOffenseEntry o2) {
				int thisVal = o1.getPoints();
				int anotherVal = o2.getPoints();
				return (thisVal<anotherVal ? 1 : (thisVal==anotherVal ? 0 : -1));
			}
		});
		int to = Math.min(offenseList.size(), 4);
		for (int x = 0; x < to; x++) {
			ret.append(StringUtil.makeEnumHumanReadable(offenseList.get(x).getOffense().name()));
			ret.append(": ");
			ret.append(offenseList.get(x).getCount());
			if (x != to -1) {
				ret.append(" ");
			}
		}
		return ret.toString();
	}
	
	public void dispose() {
		invalidationTask.cancel(false);
	}
        
        public void checkSameDamage(int damage) {
        if ((damage >= 0 && damage <= 50) || damage == 99999) { //Watk buff and damage cap
            this.sameDamage = 0;
            this.lastDamage = damage;
            return;
        }
        if (damage == this.lastSameDamage) {
            this.sameDamage++;
            if (this.sameDamage > 5) {
                try {
                    chr.get().getClient().getChannelServer().getWorldInterface().broadcastGMMessage("", MaplePacketCreator.serverNotice(5, chr.get().getName() + " is attacking with same damage: " + sameDamage).getBytes());
                } catch (RemoteException ex) {
                    chr.get().getClient().getChannelServer().reconnectWorld();
                }
                this.chr.get().getClient().getSession().close();
            }
            lastSameDamage = damage;
        } else {
            this.sameDamage = 0;
            this.lastSameDamage = damage;
        }
    }

     public synchronized boolean Spam(int limit, int type) {
        if (type < 0 || lastTime.length < type) {
            type = 1; // Default.
        }
        if (!chr.get().isGM()) {
            if (System.currentTimeMillis() < limit + lastTime[type]) {
                return true;
            }
        }
        lastTime[type] = System.currentTimeMillis();
        return false;
    }

	private class InvalidationTask implements Runnable {
		@Override
		public void run() {
			CheatingOffenseEntry[] offenses_copy;
			synchronized (offenses) {
				offenses_copy = offenses.values().toArray(new CheatingOffenseEntry[offenses.size()]);
			}
			for (CheatingOffenseEntry offense : offenses_copy) {
				if (offense.isExpired()) {
					expireEntry(offense);
				}
			}
			
			if (chr.get() == null) {
				dispose();
			}
		}
	}
}
