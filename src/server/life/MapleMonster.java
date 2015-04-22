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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

import client.IItem;
import client.Item;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleQuestStatus;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import config.configuration.Configuration;
import java.awt.Point;
import net.MaplePacket;
import net.channel.ChannelServer;
import server.quest.MapleQuest;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import server.TimerManager;
import server.life.MapleMonsterInformationProvider.DropEntry;
import server.life.MapleMonsterQuestInformationProvider.QuestDropEntry;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;


public class MapleMonster extends AbstractLoadedMapleLife {
	private MapleMonsterStats stats;
	private MapleMonsterStats overrideStats;
	private int hp;
	private int mp;
	private WeakReference<MapleCharacter> controller = new WeakReference<MapleCharacter>(null);
	private boolean controllerHasAggro, controllerKnowsAboutAggro;
	private Collection<AttackerEntry> attackers = new LinkedList<AttackerEntry>();
	private EventInstanceManager eventInstance = null;
	private Collection<MonsterListener> listeners = new LinkedList<MonsterListener>();
	private MapleCharacter highestDamageChar;
	private Map<MonsterStatus, MonsterStatusEffect> stati = new LinkedHashMap<MonsterStatus, MonsterStatusEffect>();
	private List<MonsterStatusEffect> activeEffects = new ArrayList<MonsterStatusEffect>();
	private MapleMap map;
    private boolean canDamage = true;
    private int dropItemPeriod;
    private boolean shouldDrop = true;
    private int droppedCount = 0;
    private boolean justSpawned = true;
	private int VenomMultiplier = 0;
	private boolean fake = false;
	private boolean dropsDisabled = false;
	private List<Pair<Integer, Integer>> usedSkills = new ArrayList<Pair<Integer, Integer>>();
	private Map<Pair<Integer, Integer>, Integer> skillsUsed = new HashMap<Pair<Integer, Integer>, Integer>();
	private List<MonsterStatus> monsterBuffs = new ArrayList<MonsterStatus>();
	private int team = -1; //-1 means no team
	private boolean random = false;
	private MapleCharacter target = null;
	private boolean hypnotized = false;
	private MapleCharacter hypnotizer = null;

	public MapleMonster(int id, MapleMonsterStats stats) {
		super(id);
		initWithStats(stats);
	}
	
	public MapleMonster(MapleMonster monster) {
		super(monster);
		initWithStats(monster.stats);
	}
	
	private void initWithStats (MapleMonsterStats stats) {
		setStance(5);
		this.stats = stats;
		hp = stats.getHp();
		mp = stats.getMp();
	}
	
	public boolean hasPublicReward() {
		return stats.hasPublicReward();
	}

	public boolean isHypnotized() {
		return hypnotized;
	}

	public void setHypnotized(boolean hypnotized) {
		this.hypnotized = hypnotized;
	}
        
        public MapleMonsterStats getStats() {
                return stats;
        } 

	public MapleCharacter getHypnotizer() {
		return hypnotizer;
	}

	public void setHypnotizer(MapleCharacter hypnotizer) {
		this.hypnotizer = hypnotizer;
	}
	
	public void hypnotize(MapleCharacter chr, long time) {
		this.hypnotized = true;
		this.hypnotizer = chr;
		TimerManager.getInstance().schedule(new Runnable() {
			public void run() {
				if (MapleMonster.this.isAlive()) {
					setHypnotized(false);
					setHypnotizer(null);
				}
			}
		}, time);
		hypnotizeDamage();
	}
	
	private void hypnotizeDamage() {
		if (this.isHypnotized()) {
			TimerManager.getInstance().schedule(new Runnable() {
				public void run() {
					if (MapleMonster.this.isAlive() && isHypnotized()) {
						if (getHypnotizer() == null) {
							return;
						}
						int damage = hypnotizer.getLevel() * 10 + (int) (Math.random() * 300);
						damage(hypnotizer, damage, true);
						getMap().broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage));
						if (isAlive()) {
							hypnotizeDamage();
						} else {
							getMap().killMonster(MapleMonster.this, hypnotizer, true);
						}
					}
				}
			}, 5000);			
		}
	}

	public boolean isRandom() {
		return random;
	}

	public void setRandom(boolean random) {
		this.random = random;
	}

	public MapleCharacter getTarget() {
		return target;
	}

	public void setTarget(MapleCharacter target) {
		this.target = target;
	}
	
	public void destroyRandomEventMob() {
		if (!this.isRandom()) return;
		if (target != null) {
			this.getMap().killMonster(this, target, false);
			target.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "[Random Event] Monster got bored and left."));
		} else {
			this.getMap().killMonster(this, null, false);
		}
	}

	public int getTeam() {
		return team;
	}

	public void setTeam(int team) {
		this.team = team;
	}
	
	public void disableDrops() {
		this.dropsDisabled = true;
	}
	
	public boolean dropsDisabled() {
		return dropsDisabled;
	}
	
	public void setMap(MapleMap map) {
		this.map = map;
	}
        
	 public boolean hasDrop() {
        MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        MapleMonsterQuestInformationProvider mi1 = MapleMonsterQuestInformationProvider.getInstance();
        List<QuestDropEntry> dl = mi1.retrieveDropChances(getId());
        List<DropEntry> dl1 = mi.retrieveDropChances(getId());
        return (dl.size() > 0 || dl1.size() > 0);
    }

    public int getDrop() {
        MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        int lastAssigned = -1;
        int minChance = 1;
        List<DropEntry> dl = mi.retrieveDropChances(getId());
        for (DropEntry d : dl) {
            if (d.chance > minChance) {
                minChance = d.chance;
            }
        }
        for (DropEntry d : dl) {
            d.assignedRangeStart = lastAssigned + 1;
            d.assignedRangeLength = (int) Math.ceil(((double) 1 / (double) d.chance) * minChance);
            lastAssigned += d.assignedRangeLength;
        }
        // now produce the randomness o.o
        Random r = new Random();
        int c = r.nextInt(minChance);
        for (DropEntry d : dl) {
            if (c >= d.assignedRangeStart && c < (d.assignedRangeStart + d.assignedRangeLength)) {
                return d.itemId;
            }
        }
        return -1;
    }

    public int getQuestDrop(MapleCharacter chr) {
                MapleMonsterQuestInformationProvider mi = MapleMonsterQuestInformationProvider.getInstance();
                int lastAssigned = -1;
                int minChance = 1;
                List<QuestDropEntry> dl = mi.retrieveDropChances(getId());
                for (QuestDropEntry d : dl) {
                        if (d.chance > minChance)
                                minChance = d.chance;
                }
                for (QuestDropEntry d : dl) {
                        d.assignedRangeStart = lastAssigned + 1;
                        d.assignedRangeLength = (int) Math.ceil(((double) 1 / (double) d.chance) * minChance);
                        lastAssigned += d.assignedRangeLength;
                }
                // now produce the randomness o.o
                Random r = new Random();
                int c = r.nextInt(minChance);
                for (QuestDropEntry d : dl) {
                        if (c >= d.assignedRangeStart && c < (d.assignedRangeStart + d.assignedRangeLength) && chr.getQuest(MapleQuest.getInstance(d.questid)).getStatus() == MapleQuestStatus.Status.STARTED)
                                return d.itemId;
                }
                return 1;
        }


	public int getHp() {
		return hp;
	}

        public boolean canDamage() {
        return this.canDamage;
    }

    public void setCanDamage(boolean dmg) {
        this.canDamage = dmg;
    }

    public int getDropItemPeriod() {
        return dropItemPeriod;
    }

    public void setDropItemPeriod(int se) {
        dropItemPeriod = se;
    }

    public void scheduleCanDamage(long time) {
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(new timeDamage(this), time);
    }

    public void scheduleCanDrop(long time) {
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(new pauseDrop(this), time);
    }

    public void scheduleDrop(int time, int itemid) {
        TimerManager timerManager = TimerManager.getInstance();
        final MapleMonster mob = this;
        final int itemidX = itemid;
        final Runnable s = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (mob.getShouldDrop() == false) {
                        return;
                    }
                    IItem t = new Item(itemidX, (byte)0, (short)1);
                    mob.getMap().spawnItemDrop(mob, mob.getEventInstance().getPlayers().get(0), t, mob.getPosition(), mob.isBoss(), true);
                    if (mob.getId() == 9300061) {
                        int d = mob.getDropped() + 1;
                        mob.setDropped(d);
                        mob.getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, "[Sactualize] O coelhinho da Lua fez número bolo de arroz " + d + "."));
                    }
                    mob.setShouldDrop(true);
                    mob.scheduleDrop(6000, itemidX);
                }
            }
        };
        timerManager.schedule(s, 6000);
    }

    public MaplePacket makeHPBarPacket(MapleMonster w) {
        byte tagcolor = 01;
        byte tagbgcolor = 05;
		return MaplePacketCreator.showBossHP(9400711, w.getHp(), w.getMaxHp(), tagcolor, tagbgcolor);
    }

	public void setHp(int hp) {
		this.hp = hp;
	}

    public boolean getJustSpawned() {
        return justSpawned;
    }

    public void setJustSpawned(boolean f) {
        justSpawned = f;
    }

	public int getMaxHp() {
		if (overrideStats != null) {
			return overrideStats.getHp();
		}
		return stats.getHp();
	}

	public int getMp() {
		return mp;
	}

	public void setMp(int mp) {
		if (mp < 0) {
			mp = 0;
		}
		this.mp = mp;
	}

	public int getMaxMp() {
		if (overrideStats != null) {
			return overrideStats.getMp();
		}
		return stats.getMp();
	}

	public int getExp() {
		if (overrideStats != null) {
			return overrideStats.getExp();
		}
		return stats.getExp();
	}

	public int getLevel() {
		return stats.getLevel();
	}

	public int getRemoveAfter() {
		return stats.getRemoveAfter();
	}

	public int getVenomMulti() {
		return this.VenomMultiplier;
	}

	public void setVenomMulti(int multiplier) {
		this.VenomMultiplier = multiplier;
	}

	public boolean isBoss() {
		return stats.isBoss() || getId() == 8810018;
	}
	
	public boolean isFfaLoot() {
		return stats.isFfaLoot();
	}
	
	public int getAnimationTime(String name) {
		return stats.getAnimationTime(name);
	}
	
	public List<Integer> getRevives() {
		return stats.getRevives();
	}

	public void setOverrideStats(MapleMonsterStats overrideStats) {
		this.overrideStats = overrideStats;
	}
	
	public byte getTagColor() {
		return stats.getTagColor();
	}
	
	public byte getTagBgColor() {
		return stats.getTagBgColor();
	}
    
    public void setShouldDrop(boolean t) {
        shouldDrop = t;
    }

    public boolean getShouldDrop() {
        return shouldDrop;
    }

    public void setDropped(int dr) {
        this.droppedCount = dr;
    }

    public int getDropped() {
        return droppedCount;
    }

    public boolean getUndead(){
        return stats.getUndead();
 	}
	
	public MaplePacket makeHPBarPacket00(Object retard) {
		byte tagcolor = 01;
		byte tagbgcolor = 05;
		return MaplePacketCreator.showBossHP(9400711, getHp(), getMaxHp(), tagcolor, tagbgcolor);
	}

	/**
	 * 
	 * @param from the player that dealt the damage
	 * @param damage
	 */
	public void damage(MapleCharacter from, int damage, boolean updateAttackTime) {
        AttackerEntry attacker = null;
        if (from.getParty() != null) {
            attacker = new PartyAttackerEntry(from.getParty().getId(), from.getClient().getChannelServer());
        } else {
            attacker = new SingleAttackerEntry(from, from.getClient().getChannelServer());
        }
        boolean replaced = false;
        for (AttackerEntry aentry : attackers) {
            if (aentry.equals(attacker)) {
                attacker = aentry;
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            attackers.add(attacker);
        }
		int rDamage = Math.max(0, Math.min(damage, this.hp));
		attacker.addDamage(from, rDamage, updateAttackTime);
		this.hp -= rDamage;
		int remhppercentage = (int) Math.ceil((this.hp * 100.0) / getMaxHp());
		if (remhppercentage < 1) {
			remhppercentage = 1;
		}
		long okTime = System.currentTimeMillis() - 4000;
		if (hasBossHPBar()) {
			from.getMap().broadcastMessage(makeBossHPBarPacket(), getPosition());
		} else if (!isBoss()) {
			for (AttackerEntry mattacker : attackers) {
				for (AttackingMapleCharacter cattacker : mattacker.getAttackers()) {
					// current attacker is on the map of the monster
					if (cattacker.getAttacker().getMap() == from.getMap()) {
						if (cattacker.getLastAttackTime() >= okTime) {
							cattacker.getAttacker().getClient().getSession().write(MaplePacketCreator.showMonsterHP(getObjectId(), remhppercentage));
						}
					}
				}
			}
		}
        if (this.hp < 1 && this.getId() == 9300061) {
            this.getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "[Notice] You failed the mission."));
            this.getMap().killAllMonsters(false);
            this.getEventInstance().disbandParty();
        }
        if (this.hp < 1 && this.getId() == 9300093) {
            this.getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, "[Notice] You failed the mission."));
            this.getMap().killAllMonsters(false);
            this.getMap().quitEPQ();
        }
}

	public void heal(int hp, int mp) {
//		int finalHP = hp / 10000 * ((int) (8000 + 10000 * Math.random()));
		int hp2Heal = getHp() + hp;
		int mp2Heal = getMp() + mp;

		if (hp2Heal >= getMaxHp()) {
			hp2Heal = getMaxHp();
		}
		if (mp2Heal >= getMaxMp()) {
			mp2Heal = getMaxMp();
		}

		setHp(hp2Heal);
		setMp(mp2Heal);
		getMap().broadcastMessage(MaplePacketCreator.healMonster(getObjectId(), hp));
	}
	
	public boolean isAttackedBy(MapleCharacter chr) {
		for (AttackerEntry aentry : attackers) {
			if (aentry.contains(chr)) {
				return true;
			}
		}
		return false;
	}
        
 public void giveExpToCharacter(MapleCharacter attacker, int exp, boolean highestDamage, int numExpSharers) {
        if (highestDamage) {
            if (eventInstance != null) {
                eventInstance.monsterKilled(attacker, this);
            }
            highestDamageChar = attacker;
        }
        if (attacker.getHp() > 0) {
            int personalExp = exp;
            if (exp > 0) {
                Integer holySymbol = attacker.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
                if (holySymbol != null) {
                    if (numExpSharers == 1) {
                        personalExp *= 1.0 + (holySymbol.doubleValue() / 500.0);
                    } else {
                        personalExp *= 1.0 + (holySymbol.doubleValue() / 100.0);
                    }
                }
                if (stati.containsKey(MonsterStatus.SHOWDOWN)) {
                    personalExp *= (stati.get(MonsterStatus.SHOWDOWN).getStati().get(MonsterStatus.SHOWDOWN).doubleValue() / 100.0 + 1.0);
                }
            }
            if (exp < 0) {//O.O ><
                personalExp = Integer.MAX_VALUE;
            }
            attacker.gainExp(personalExp, true, false, highestDamage);
            attacker.mobKilled(this.getId());
            //LeaderPoints :D
//            if ((int) (Math.random() * 200) < 3) {
//                attacker.gainLeaderPoints(configuracoes.LeaderPoints_1);
//                attacker.dropOverheadMessage("Voce ganhou " + configuracoes.LeaderPoints_1 + " [#r#eLeaderPoints#k#n]"); // Ganha Leaderpoints 
//           } if ((int) (Math.random() * 300) < 3) {
//                attacker.gainLeaderPoints(configuracoes.LeaderPoints_2);
//                attacker.dropOverheadMessage("Voce ganhou " + configuracoes.LeaderPoints_2 + " [#r#eLeaderPoints#k#n]"); //Ganha Leaderpoints  
//           } if ((int) (Math.random() * 400) < 3) {
//                attacker.gainLeaderPoints(configuracoes.LeaderPoints_3);
//                attacker.dropOverheadMessage("Voce ganhou " + configuracoes.LeaderPoints_3 + " [#r#eLeaderPoints#k#n]"); //Ganha Leaderpoints  
//             }
//            int typedouble = (int) Math.floor(Math.random() * 4 + 1);
//            int typenormal = (int) Math.floor(Math.random() * 2 + 1);
//            int cashpoints = (int) Math.floor(Math.random() * typenormal + 5);
//            int cashpointsdouble = (int) Math.floor(Math.random() * typedouble + 5);
//            if (attacker.getMapId() == 107000200) {
//            if(attacker.getClient().getChannelServer().doublecash == true) {
//                attacker.gainCashPoints(cashpointsdouble);
//                attacker.dropMessage("["+Configuration.Server_Name+" CashPQ] Voce tem um total de (" + attacker.getCashPoints() + ") CashPoints."); // CashPoints
//            } else {
//                attacker.gainCashPoints(cashpoints);
//                attacker.dropMessage("["+Configuration.Server_Name+" CashPQ] Voce tem um total de (" + attacker.getCashPoints() + ") CashPoints."); // CashPoints      
//        }
//     }
   }
 }
/*Adicionado*/
 
 
 int[] mobs = { 
    3220000, // Stumpy, 
    9300003, // Slime King 
    4130103, // Rombot 
    9300012, // Alishar 
    8220001, // Yeti on Skis 
    8220000, // Elliza 
    9300119, // Lord Pirate 
    9300152, // Angry Franken Lloyd 
    9300039, // Papa Pixie 
    9300032, // Knight Statue B 
    9300028, // Ergoth 
    9400549, // Headless Horseman 
    8180001, // Griffey 
    8180000, // Manon 
    8500001, // Papulatus 
    9400575, // Big Foot 
    9400014, // Black Crow 
    8800002, // Zakum Body 3 
    9400121, // Female Boss 
    9400300 // The Boss 
    }; 

    public String getMobName(boolean next) { 
        switch (this.getId()) { 
            case 3220000: return !next ? "Stumpy" : "Slime King"; 
            case 9300003: return !next ? "Slime King" : "Rombot"; 
            case 4130103: return !next ? "Rombot" : "Alishar"; 
            case 9300012: return !next ? "Alishar" : "Snowman"; 
            case 8220001: return !next ? "Snowman" : "Eliza"; 
            case 8220000: return !next ? "Eliza" : "Lord Pirate"; 
            case 9300119: return !next ? "Lord Pirate" : "Angry Franken Lloyd"; 
            case 9300152: return !next ? "Angry Franken Lloyd" :"Papa Pixie"; 
            case 9300039: return !next ? "Papa Pixie" : "Knight Statue B"; 
            case 9300032: return !next ? "Knight Statue B" : "Ergoth"; 
            case 9300028: return !next ? "Ergoth" : "Headless Horseman"; 
            case 9400549: return !next ? "Headless Horseman" : "Griffey"; 
            case 8180001: return !next ? "Griffey" : "Manon"; 
            case 8180000: return !next ? "Manon" : "Papulatus"; 
            case 8500001: return !next ? "Papulatus" : "Big Foot"; 
            case 9400575: return !next ? "Big Foot" : "Black Crow"; 
            case 9400014: return !next ? "Black Crow" : "Zakum Body"; 
            case 8800002: return !next ? "Zakum Body" : "Female Boss"; 
            case 9400121: return !next ? "Female Boss" : "The Boss"; 
            case 9400300: return !next ? "The Boss" : "Nenhum."; 
        } 
        return "Null"; 
    }
	
   public MapleCharacter killBy(final MapleCharacter killer) {
       if (killer.getMapId() == 980000000) { 
            for (int i = 0; i < mobs.length; i++)
                if (this.getId() == mobs[i]) { 
                    for (MapleCharacter currmap : killer.getMap().getCharacters()) { 
                        currmap.addBossPoints(this.getId() == mobs[0] ? 1 : this.getId() == mobs[1] ? 2 : this.getId() == mobs[2] ? 3 : this.getId() == mobs[3] ? 4 : this.getId() == mobs[4] ? 5 : this.getId() == mobs[5] ? 6 : this.getId() == mobs[6] ? 7 : this.getId() == mobs[7] ? 8 : this.getId() == mobs[8] ? 9 : this.getId() == mobs[9] ? 10 : this.getId() == mobs[10] ? 11 : this.getId() == mobs[11] ? 12 : this.getId() == mobs[12] ? 13 : this.getId() == mobs[13] ? 14 : this.getId() == mobs[14] ? 15 : this.getId() == mobs[15] ? 16 : this.getId() == mobs[16] ? 17 : this.getId() == mobs[17] ? 18 : this.getId() == mobs[18] ? 19 : 20); 
                        currmap.getClient().announce(MaplePacketCreator.getClock(15)); // 15 seconds 
                        currmap.dropMessage("["+Configuration.Server_Name+" BPQ] Congratulations, you killed " + getMobName(false) + ".");
                        currmap.dropMessage("["+Configuration.Server_Name+" BPQ] You now have (" + currmap.getBossPoints() + ") Boss Points.");
                        currmap.dropMessage("["+Configuration.Server_Name+" BPQ] The next monster is: " + getMobName(true) + "."); 
                    } 
                   final int x = i; 
                   TimerManager.getInstance().schedule(new Runnable() { 
                        @Override 
                        public void run() { 
                            if (MapleMonster.this.getId() == 9400300) { 
                                for (MapleCharacter party : killer.getPartyMembers()) { 
                                    party.dropMessage("["+Configuration.Server_Name+" BPQ] You managed to complete the BPQ. ");
                                    party.dropMessage("["+Configuration.Server_Name+" BPQ] You currently have (" + party.getBossPoints() + ") Boss Points!");
                                    party.changeMap(103000000); 
                                } 
                            } else { 
                                killer.spawnMonster(mobs[x + 1], MapleMonster.this.getId() == mobs[0] ? 200000000 : MapleMonster.this.getId() == mobs[1] ? 300000000 : MapleMonster.this.getId() == mobs[2] ? 400000000 : MapleMonster.this.getId() == mobs[3] ? 500000000 : MapleMonster.this.getId() == mobs[4] ? 600000000 : MapleMonster.this.getId() == mobs[5] ? 700000000 : MapleMonster.this.getId() == mobs[6] ? 800000000 : MapleMonster.this.getId() == mobs[7] ? 900000000 : MapleMonster.this.getId() == mobs[8] ? 1000000000 : MapleMonster.this.getId() == mobs[9] ? 1100000000 : MapleMonster.this.getId() == mobs[10] ? 1200000000 : MapleMonster.this.getId() == mobs[11] ? 1300000000 : MapleMonster.this.getId() == mobs[12] ? 1400000000 : MapleMonster.this.getId() == mobs[13] ? 1500000000 : MapleMonster.this.getId() == mobs[14] ? 1600000000 : MapleMonster.this.getId() == mobs[15] ? 1700000000 : MapleMonster.this.getId() == mobs[16] ? 1800000000 : MapleMonster.this.getId() == mobs[17] ? 1900000000 : MapleMonster.this.getId() == mobs[18] ? 2000000000 : Integer.MAX_VALUE, 0, 1); 
                            } 
                       } 
                    }, 1000 * 15); // 15 seconds 
                } 
            }
         /* Fim */
        long totalBaseExpL = this.getExp() * ChannelServer.getInstance(killer.getClient().getChannel()).getExpRate() * killer.hasEXPCard();
        int totalBaseExp = (int) (Math.min(Integer.MAX_VALUE, totalBaseExpL)); 
        AttackerEntry highest = null;
        int highdamage = 0;
        for (AttackerEntry attackEntry : attackers) {
            if (attackEntry.getDamage() > highdamage) {
                highest = attackEntry;
                highdamage = attackEntry.getDamage();
            }
        }
        for (AttackerEntry attackEntry : attackers) {
            attackEntry.killedMob(killer.getMap(), (int) Math.ceil(totalBaseExp * ((double) attackEntry.getDamage() / getMaxHp())), attackEntry == highest);
        }
        if (this.getController() != null) { // this can/should only happen when a hidden gm attacks the monster
            getController().getClient().getSession().write(MaplePacketCreator.stopControllingMonster(this.getObjectId()));
            getController().stopControllingMonster(this);
        }
        final List<Integer> toSpawn = this.getRevives();
        if (toSpawn != null) {
            final MapleMap reviveMap = killer.getMap();
            if (toSpawn.contains(9300216) && reviveMap.getId() > 925000000 && reviveMap.getId() < 926000000) {
                reviveMap.broadcastMessage(MaplePacketCreator.playSound("Dojang/clear"));
                reviveMap.broadcastMessage(MaplePacketCreator.showEffect("dojang/end/clear"));
            }
            TimerManager.getInstance().schedule(new Runnable() {
		public void run() {
                    for(Integer mid : toSpawn) {
			MapleMonster mob = MapleLifeFactory.getMonster(mid);
                        if (eventInstance != null) {
                            eventInstance.registerMonster(mob);
			}
			mob.setPosition(getPosition());
			if (dropsDisabled()) {
                            mob.disableDrops();
			}
			reviveMap.spawnRevives(mob);
                    }
		}
            }, this.getAnimationTime("die1"));
	}
        if (eventInstance != null) {
            eventInstance.unregisterMonster(this);
        }
        for (MonsterListener listener : listeners.toArray(new MonsterListener[listeners.size()])) {
            listener.monsterKilled(this, highestDamageChar);
        }
        MapleCharacter ret = highestDamageChar;
        highestDamageChar = null; // may not keep hard references to chars outside of PlayerStorage or MapleMap
        return ret;
    }



	public boolean isAlive() {
		return this.hp > 0;
	}

	public MapleCharacter getController() {
        return controller.get();
    }
        
    public void setController(MapleCharacter controller) {
        this.controller = new WeakReference<>(controller);
    }
	
    public void switchController(MapleCharacter newController, boolean immediateAggro) {
        MapleCharacter controllers = getController();
        if (controllers == newController) {
            return;
        }
        if (controllers != null) {
            controllers.stopControllingMonster(this);
            controllers.getClient().getSession().write(MaplePacketCreator.stopControllingMonster(getObjectId()));
        }
        newController.controlMonster(this, immediateAggro);
        setController(newController);
        if (immediateAggro) {
            setControllerHasAggro(true);
        }
        setControllerKnowsAboutAggro(false);
    }
	
	public void addListener (MonsterListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener (MonsterListener listener) {
		listeners.remove(listener);
	}

	public boolean isControllerHasAggro() {
		if (fake) {
			return false;
		}
		return controllerHasAggro;
	}

	public void setControllerHasAggro(boolean controllerHasAggro) {
		if (fake) {
			return;
		}
		this.controllerHasAggro = controllerHasAggro;
	}

	public boolean isControllerKnowsAboutAggro() {
		if (fake) {
			return false;
		}
		return controllerKnowsAboutAggro;
	}

	public void setControllerKnowsAboutAggro(boolean controllerKnowsAboutAggro) {
		if (fake) {
			return;
		}
		this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
	}

	public MaplePacket makeBossHPBarPacket() {
		return MaplePacketCreator.showBossHP(getId(), getHp(), getMaxHp(), getTagColor(), getTagBgColor());
	}
	
	public boolean hasBossHPBar() {
		return (isBoss() && getTagColor() > 0) || isHT();
	}
	
	public boolean isHT() {
		return this.getId() == 8810018;
	}
	
	@Override
      public void sendSpawnData(MapleClient c) {
      if (!isAlive() || c.getPlayer().isFake()) {
            return;
        }
        if (isFake()) {
            c.getSession().write(MaplePacketCreator.spawnFakeMonster(this, 0));
        } else {
            c.getSession().write(MaplePacketCreator.spawnMonster(this, false));
        }
        if (stati.size() > 0) {
            for (MonsterStatusEffect mse : activeEffects) {
                MaplePacket packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), mse.getStati(), mse.getSkill().getId(), false, 0);
                c.getSession().write(packet);
            }
        }
        if (hasBossHPBar()) {
            if (this.getMap().countMonster(8810026) > 2 && this.getMap().getId() == 240060200) {
                this.getMap().killAllMonsters();
                return;
            }
            c.getSession().write(makeBossHPBarPacket());
        }
    }

	@Override
	public void sendDestroyData(MapleClient client) {
		client.getSession().write(MaplePacketCreator.killMonster(getObjectId(), false));
	}

	@Override
	public String toString() {
		return getName() + "(" + getId() + ") at " + getPosition().x + "/" + getPosition().y + " with " + getHp() + "/" + getMaxHp() +
			"hp, " + getMp() + "/" + getMaxMp() + " mp (alive: " + isAlive() + " oid: " + getObjectId() + ")";
	}

	@Override
	public MapleMapObjectType getType() {
		return 	MapleMapObjectType.MONSTER;
	}

	public EventInstanceManager getEventInstance() {
		return eventInstance;
	}

	public void setEventInstance(EventInstanceManager eventInstance) {
		this.eventInstance = eventInstance;
	}

	public boolean isMobile() {
		return stats.isMobile();
	}

	public ElementalEffectiveness getEffectiveness (Element e) {
		if (activeEffects.size() > 0 && stati.get(MonsterStatus.DOOM) != null) {
			return ElementalEffectiveness.NORMAL; // like blue snails
		}
		return stats.getEffectiveness(e);
	}
	
	public boolean applyStatus (MapleCharacter from, final MonsterStatusEffect status, boolean poison, long duration) {
		return applyStatus(from, status, poison, duration, false);
	}

	public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status, boolean poison, long duration, boolean venom) {
        switch (stats.getEffectiveness(status.getSkill().getElement())) {
            case IMMUNE:
            case STRONG:
                return false;
            case NORMAL:
            case WEAK:
                break;
            default:
                throw new RuntimeException("Unknown elemental effectiveness: " + stats.getEffectiveness(status.getSkill().getElement()));
        }
        ElementalEffectiveness effectiveness = null;
        switch (status.getSkill().getId()) {
            case 2111006:
                effectiveness = stats.getEffectiveness(Element.POISON);
                if (effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) {
                    return false;
                }
                break;
            case 2211006:
                effectiveness = stats.getEffectiveness(Element.ICE);
                if (effectiveness == ElementalEffectiveness.IMMUNE || effectiveness == ElementalEffectiveness.STRONG) {
                    return false;
                }
                break;
            case 4120005:
            case 4220005:
                effectiveness = stats.getEffectiveness(Element.POISON);
                if (effectiveness == ElementalEffectiveness.WEAK) {
                    return false;
                }
                break;
        }
        if (poison && getHp() <= 1) {
            return false;
        }
        if (isBoss() && !(status.getStati().containsKey(MonsterStatus.SPEED))) {
            return false;
        }
        for (MonsterStatus stat : status.getStati().keySet()) {
            MonsterStatusEffect oldEffect = stati.get(stat);
            if (oldEffect != null) {
                oldEffect.removeActiveStatus(stat);
                if (oldEffect.getStati().size() == 0) {
                    oldEffect.getCancelTask().cancel(false);
                    oldEffect.cancelPoisonSchedule();
                    activeEffects.remove(oldEffect);
                }
            }
        }
        TimerManager timerManager = TimerManager.getInstance();
        final Runnable cancelTask = new Runnable() {
            @Override
            public void run() {
                if (isAlive()) {
                    MaplePacket packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), status.getStati());
                    map.broadcastMessage(packet, getPosition());
                    if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                        getController().getClient().getSession().write(packet);
                    }
                }
                activeEffects.remove(status);
                for (MonsterStatus stat : status.getStati().keySet()) {
                    stati.remove(stat);
                }
                setVenomMulti(0);
                status.cancelPoisonSchedule();
            }
        };
        if (poison) {
            int poisonLevel = from.getSkillLevel(status.getSkill());
            int poisonDamage = Math.min(Short.MAX_VALUE, (int) (getMaxHp() / (70.0 - poisonLevel) + 0.999));
            status.setValue(MonsterStatus.POISON, Integer.valueOf(poisonDamage));
            status.setPoisonSchedule(timerManager.register(new PoisonTask(poisonDamage, from, status, cancelTask, false), 1000, 1000));
        } else if (venom) {
            if (from.getJob() == MapleJob.NIGHTLORD || from.getJob() == MapleJob.SHADOWER) {
                int poisonLevel = 0;
                int matk = 0;
                if (from.getJob() == MapleJob.NIGHTLORD) {
                    poisonLevel = from.getSkillLevel(SkillFactory.getSkill(4120005));
                    if (poisonLevel <= 0) {
                        return false;
                    }
                    matk = SkillFactory.getSkill(4120005).getEffect(poisonLevel).getMatk();
                } else if (from.getJob() == MapleJob.SHADOWER) {
                    poisonLevel = from.getSkillLevel(SkillFactory.getSkill(4220005));
                    if (poisonLevel <= 0) {
                        return false;
                    }
                    matk = SkillFactory.getSkill(4220005).getEffect(poisonLevel).getMatk();
                } else {
                    return false;
                }
                Random r = new Random();
                int luk = from.getLuk();
                int maxDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.2 * luk * matk));
                int minDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.1 * luk * matk));
                int gap = maxDmg - minDmg;
                if (gap == 0) {
                    gap = 1;
                }
                int poisonDamage = 0;
                for (int i = 0; i < getVenomMulti(); i++) {
                    poisonDamage = poisonDamage + (r.nextInt(gap) + minDmg);
                }
                poisonDamage = Math.min(Short.MAX_VALUE, poisonDamage);
                status.setValue(MonsterStatus.POISON, Integer.valueOf(poisonDamage));
                status.setPoisonSchedule(timerManager.register(new PoisonTask(poisonDamage, from, status, cancelTask, false), 1000, 1000));
            } else {
                return false;
            }
        } else if (status.getSkill().getId() == 4111003) {
            int webDamage = (int) (getMaxHp() / 50.0 + 0.999);
            status.setPoisonSchedule(timerManager.schedule(new PoisonTask(webDamage, from, status, cancelTask, true), 3500));
        }
        for (MonsterStatus stat : status.getStati().keySet()) {
            stati.put(stat, status);
        }
        activeEffects.add(status);
        int animationTime = status.getSkill().getAnimationTime();
        MaplePacket packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), status.getStati(), status.getSkill().getId(), false, 0);
        map.broadcastMessage(packet, getPosition());
        if (getController() != null && !getController().isMapObjectVisible(this)) {
            getController().getClient().getSession().write(packet);
        }
        ScheduledFuture<?> schedule = timerManager.schedule(cancelTask, duration + animationTime);
        status.setCancelTask(schedule);
        return true;
    }
	
	public void applyMonsterBuff(final MonsterStatus status, final int x, int skillId, long duration, MobSkill skill) {
		TimerManager timerManager = TimerManager.getInstance();
		final Runnable cancelTask = new Runnable() {
			@Override
			public void run() {
				if (isAlive()) {
					MaplePacket packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), Collections.singletonMap(status, Integer.valueOf(x)));
					map.broadcastMessage(packet, getPosition());
					if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
						getController().getClient().getSession().write(packet);
					}
					removeMonsterBuff(status);
				}
			}
		};
		MaplePacket packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), Collections.singletonMap(status, x), skillId, true, 0, skill);
		map.broadcastMessage(packet, getPosition());
		if (getController() != null && !getController().isMapObjectVisible(this)) {
			getController().getClient().getSession().write(packet);
		}
		timerManager.schedule(cancelTask, duration);
		addMonsterBuff(status);
		
	}
	
	public void addMonsterBuff(MonsterStatus status) {
		this.monsterBuffs.add(status);
	}
	
	public void removeMonsterBuff(MonsterStatus status) {
		this.monsterBuffs.remove(status);
	}
	
	public void cancelMonsterBuff(MonsterStatus status) {
		if (isAlive()) {
			MaplePacket packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), 
					Collections.singletonMap(status, Integer.valueOf(1)));
			map.broadcastMessage(packet, getPosition());
			if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
				getController().getClient().getSession().write(packet);
			}
			removeMonsterBuff(status);
		}
	}
	
	public void dispel() {
		if (isAlive()) {
			MonsterStatus[] remove = {MonsterStatus.ACC, MonsterStatus.AVOID, MonsterStatus.MAGIC_ATTACK_UP, MonsterStatus.MAGIC_DEFENSE_UP, MonsterStatus.MATK, MonsterStatus.MDEF, MonsterStatus.WATK, MonsterStatus.WDEF, MonsterStatus.WEAPON_ATTACK_UP, MonsterStatus.WEAPON_DEFENSE_UP, MonsterStatus.WEAPON_IMMUNITY, MonsterStatus.MAGIC_IMMUNITY, MonsterStatus.SPEED};
			for (int i = 0; i < remove.length; i++) {
				if (monsterBuffs.contains(remove[i])) {
					removeMonsterBuff(remove[i]);
					MaplePacket packet = MaplePacketCreator.cancelMonsterStatus(getObjectId(), Collections.singletonMap(remove[i], Integer.valueOf(1)));
					map.broadcastMessage(packet, getPosition());
					if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
						getController().getClient().getSession().write(packet);
					}
				}
			}
		}
	}
	
	public boolean isBuffed(MonsterStatus status) {
		return this.monsterBuffs.contains(status);
	}
	
	public void setFake(boolean fake) {
		this.fake = fake;
	}
	
	public boolean isFake() {
		return fake;
	}
	
	public MapleMap getMap() {
		return map;
	}
	
	public List<Pair<Integer, Integer>> getSkills() {
		return this.stats.getSkills();
	}
	
	public boolean hasSkill(int skillId, int level) {
		return stats.hasSkill(skillId, level);
	}
	
	public boolean canUseSkill(MobSkill toUse) {
		if (toUse == null) {
			return false;
		}
		for (Pair<Integer, Integer> skill : usedSkills) {
			if (skill.getLeft() == toUse.getSkillId() && skill.getRight() == toUse.getSkillLevel()) {
				return false;
			}
		}
		if (toUse.getLimit() > 0) {
			if (this.skillsUsed.containsKey(new Pair<Integer, Integer>(toUse.getSkillId(), toUse.getSkillLevel()))) {
				int times = this.skillsUsed.get(new Pair<Integer, Integer>(toUse.getSkillId(), toUse.getSkillLevel()));
				if (times >= toUse.getLimit()) {
					return false;
				}
			}
		}
		if (toUse.getSkillId() == 200) {
			Collection<MapleMapObject> mmo = getMap().getMapObjects();
			int i = 0;
			for (MapleMapObject mo : mmo) {
				if (mo.getType() == MapleMapObjectType.MONSTER) {
					i++;
				}
			}
			if (i > 100) {
				return false;
			}
		}
		return true;
	}
	
	public void usedSkill(final int skillId, final int level, long cooltime) {
		this.usedSkills.add(new Pair<Integer, Integer>(skillId, level));
		
		if (this.skillsUsed.containsKey(new Pair<Integer, Integer>(skillId, level))) {
			int times = this.skillsUsed.get(new Pair<Integer, Integer>(skillId, level)) + 1;
			this.skillsUsed.remove(new Pair<Integer, Integer>(skillId, level));
			this.skillsUsed.put(new Pair<Integer, Integer>(skillId, level), times);
		} else {
			this.skillsUsed.put(new Pair<Integer, Integer>(skillId, level), 1);
		}
		
		final MapleMonster mons = this;
		TimerManager tMan = TimerManager.getInstance();
		tMan.schedule(
			new Runnable() {
				@Override
				public void run() {
					mons.clearSkill(skillId, level);
				}
			}, cooltime);
	}
	
	public void clearSkill(int skillId, int level) {
		int index = -1;
		for (Pair<Integer, Integer> skill : usedSkills) {
			if (skill.getLeft() == skillId && skill.getRight() == level) {
				index = usedSkills.indexOf(skill);
				break;
			}
		}
		if (index != -1) {
			usedSkills.remove(index);
		}
	}
	
	public int getNoSkills() {
		return this.stats.getNoSkills();
	}
	
	public boolean isFirstAttack() {
		return this.stats.isFirstAttack();
	}
	
	public int getBuffToGive() {
		return this.stats.getBuffToGive();
	}
	
	public int getCP() {
		return this.stats.getCp();
	}

	public List<MonsterStatus> getMonsterBuffs() {
		return monsterBuffs;
	}


	private final class PoisonTask implements Runnable {
		private final int poisonDamage;
		private final MapleCharacter chr;
		private final MonsterStatusEffect status;
		private final Runnable cancelTask;
		private final boolean shadowWeb;
		private final MapleMap map; 
		
		private PoisonTask(int poisonDamage, MapleCharacter chr, MonsterStatusEffect status, Runnable cancelTask, boolean shadowWeb) {
			this.poisonDamage = poisonDamage;
			this.chr = chr;
			this.status = status;
			this.cancelTask = cancelTask;
			this.shadowWeb = shadowWeb;
			this.map = chr.getMap();
		}
		
		@Override
		public void run() {
			int damage = poisonDamage;
			if (damage >= hp) {
				damage = hp - 1;
				if (!shadowWeb) {
					cancelTask.run();
					status.getCancelTask().cancel(false);
				}
			}
			if (hp > 1 && damage > 0) {
				damage(chr, damage, false);
				if (shadowWeb) {
					map.broadcastMessage(MaplePacketCreator.damageMonster(getObjectId(), damage), getPosition());
				}
			}
		}
	}
	
	public String getName() {
		return stats.getName();
	}

	private class AttackingMapleCharacter {
		private MapleCharacter attacker;
		private long lastAttackTime;
				
		public AttackingMapleCharacter(MapleCharacter attacker, long lastAttackTime) {
			super();
			this.attacker = attacker;
			this.lastAttackTime = lastAttackTime;
		}

		public long getLastAttackTime() {
			return lastAttackTime;
		}

		public void setLastAttackTime(long lastAttackTime) {
			this.lastAttackTime = lastAttackTime;
		}

		public MapleCharacter getAttacker() {
			return attacker;
		}
	}
	
	private interface AttackerEntry {
		List<AttackingMapleCharacter> getAttackers();

		public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime);

		public int getDamage();

		public boolean contains(MapleCharacter chr);

		public void killedMob(MapleMap map, int baseExp, boolean mostDamage);
	}

	private class SingleAttackerEntry implements AttackerEntry {
		private int damage;
		private int chrid;
		private long lastAttackTime;
		private ChannelServer cserv;

		public SingleAttackerEntry(MapleCharacter from, ChannelServer cserv) {
			this.chrid = from.getId();
			this.cserv = cserv;
		}

		@Override
		public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime) {
			if (chrid == from.getId()) {
				this.damage += damage;
			} else {
				throw new IllegalArgumentException("Not the attacker of this entry");
			}
			if (updateAttackTime) {
				lastAttackTime = System.currentTimeMillis();
			}
		}

		@Override
		public List<AttackingMapleCharacter> getAttackers() {
			MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(chrid);
			if (chr != null) {
				return Collections.singletonList(new AttackingMapleCharacter(chr, lastAttackTime));
			} else {
				return Collections.emptyList();
			}
		}

		@Override
		public boolean contains(MapleCharacter chr) {
			return chrid == chr.getId();
		}

		@Override
		public int getDamage() {
			return damage;
		}

		@Override
		public void killedMob(MapleMap map, int baseExp, boolean mostDamage) {
            MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(chrid);
            if (chr != null && chr.getMap() == map) {
                giveExpToCharacter(chr, baseExp, mostDamage, 1);
            }
        }

		@Override
		public int hashCode() {
			return chrid;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final SingleAttackerEntry other = (SingleAttackerEntry) obj;
			return chrid == other.chrid;
		}
	}

	private static class OnePartyAttacker {
		public MapleParty lastKnownParty;
		public int damage;
		public long lastAttackTime;
		
		public OnePartyAttacker(MapleParty lastKnownParty, int damage) {
			super();
			this.lastKnownParty = lastKnownParty;
			this.damage = damage;
			this.lastAttackTime = System.currentTimeMillis();
		}
	}
	
	private class PartyAttackerEntry implements AttackerEntry {
		private int totDamage;
		//private Map<String, Pair<Integer, MapleParty>> attackers;
		private Map<Integer, OnePartyAttacker> attackers;
		private int partyid;
		private ChannelServer cserv;

		public PartyAttackerEntry(int partyid, ChannelServer cserv) {
			this.partyid = partyid;
			this.cserv = cserv;
			attackers = new HashMap<Integer, OnePartyAttacker>(6);
		}

		public List<AttackingMapleCharacter> getAttackers() {
			List<AttackingMapleCharacter> ret = new ArrayList<AttackingMapleCharacter>(attackers.size());
			for (Entry<Integer, OnePartyAttacker> entry : attackers.entrySet()) {
				MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(entry.getKey());
				if (chr != null) {
					ret.add(new AttackingMapleCharacter(chr, entry.getValue().lastAttackTime));
				}
			}
			return ret;
		}

		private Map<MapleCharacter, OnePartyAttacker> resolveAttackers() {
			Map<MapleCharacter, OnePartyAttacker> ret = new HashMap<MapleCharacter, OnePartyAttacker>(attackers.size());
			for (Entry<Integer, OnePartyAttacker> aentry : attackers.entrySet()) {
				MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(aentry.getKey());
				if (chr != null) {
					ret.put(chr, aentry.getValue());
				}
			}
			return ret;
		}

		@Override
		public boolean contains(MapleCharacter chr) {
			return attackers.containsKey(chr.getId());
		}

		@Override
		public int getDamage() {
			return totDamage;
		}

		 public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime) {
            OnePartyAttacker oldPartyAttacker = attackers.get(from.getId());
            if (oldPartyAttacker != null) {
                oldPartyAttacker.damage += damage;
                oldPartyAttacker.lastKnownParty = from.getParty();
                if (updateAttackTime) {
                    oldPartyAttacker.lastAttackTime = System.currentTimeMillis();
                }
            } else {
                // TODO actually this causes wrong behaviour when the party changes between attacks
                // only the last setup will get exp - but otherwise we'd have to store the full party
                // constellation for every attack/everytime it changes, might be wanted/needed in the
                // future but not now
                OnePartyAttacker onePartyAttacker = new OnePartyAttacker(from.getParty(), damage);
                attackers.put(from.getId(), onePartyAttacker);
                if (!updateAttackTime) {
                    onePartyAttacker.lastAttackTime = 0;
                }
            }
            totDamage += damage;
        }

		@Override
		public void killedMob(MapleMap map, int baseExp, boolean mostDamage) {
			Map<MapleCharacter, OnePartyAttacker> attackers_ = resolveAttackers();

			MapleCharacter highest = null;
			int highestDamage = 0;

			Map<MapleCharacter, Integer> expMap = new ArrayMap<MapleCharacter, Integer>(6);
			for (Entry<MapleCharacter, OnePartyAttacker> attacker : attackers_.entrySet()) {
				MapleParty party = attacker.getValue().lastKnownParty;
				double averagePartyLevel = 0;

				List<MapleCharacter> expApplicable = new ArrayList<MapleCharacter>();
				for (MaplePartyCharacter partychar : party.getMembers()) {
					if (attacker.getKey().getLevel() - partychar.getLevel() <= 5 ||
						getLevel() - partychar.getLevel() <= 5) {
						MapleCharacter pchr = cserv.getPlayerStorage().getCharacterByName(partychar.getName());
						if (pchr != null) {
							if (pchr.isAlive() && pchr.getMap() == map) {
								expApplicable.add(pchr);
								averagePartyLevel += pchr.getLevel();
							}
						}
					}
				}
				double expBonus = 1.0;
				if (expApplicable.size() > 1) {
					expBonus = 1.10 + 0.05 * expApplicable.size();
					averagePartyLevel /= expApplicable.size();
				}

				int iDamage = attacker.getValue().damage;
				if (iDamage > highestDamage) {
					highest = attacker.getKey();
					highestDamage = iDamage;
				}
				double innerBaseExp = baseExp * ((double) iDamage / totDamage);
				double expFraction = (innerBaseExp * expBonus) / (expApplicable.size() + 1);

				for (MapleCharacter expReceiver : expApplicable) {
					Integer oexp = expMap.get(expReceiver);
					int iexp;
					if (oexp == null) {
						iexp = 0;
					} else {
						iexp = oexp.intValue();
					}
					double expWeight = (expReceiver == attacker.getKey() ? 2.0 : 1.0);
					double levelMod = expReceiver.getLevel() / averagePartyLevel;
					if (levelMod > 1.0 || this.attackers.containsKey(expReceiver.getId())) {
						levelMod = 1.0;
					}
					iexp += (int) Math.round(expFraction * expWeight * levelMod);
					expMap.put(expReceiver, Integer.valueOf(iexp));
				}
			}
			// we are done -.-
			for (Entry<MapleCharacter, Integer> expReceiver : expMap.entrySet()) {
				boolean white = mostDamage ? expReceiver.getKey() == highest : false;
				giveExpToCharacter(expReceiver.getKey(), expReceiver.getValue(), white, expMap.size());
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + partyid;
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
			final PartyAttackerEntry other = (PartyAttackerEntry) obj;
			if (partyid != other.partyid)
				return false;
			return true;
		}
	}
}
final class timeDamage implements Runnable {
        private MapleMonster cc;

        public timeDamage(MapleMonster ccc) {
                this.cc = ccc;
        }

        @Override
        public void run() {
            synchronized (cc) {
                cc.setCanDamage(true);
            }
        }
}

final class pauseDrop implements Runnable {
        private MapleMonster cc;

        public pauseDrop(MapleMonster ccc) {
                this.cc = ccc;
        }

        @Override
        public void run() {
            synchronized (cc) {
                cc.setShouldDrop(true);
            }
        }
}
