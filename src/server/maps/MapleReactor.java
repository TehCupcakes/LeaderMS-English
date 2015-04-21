/*
 * This file is part of the OdinMS Maple Story Server
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

package server.maps;

import java.awt.Rectangle;
import client.MapleClient;
import client.status.MonsterStatus;
import net.MaplePacket;
import scripting.reactor.ReactorScriptManager;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author Lerk
 */

public class MapleReactor extends AbstractMapleMapObject {
	//private static Logger log = LoggerFactory.getLogger(MapleReactor.class);
	private int rid;
	private MapleReactorStats stats;
	private byte state;
	private int delay;
    private int mode;
	private MapleMap map;
	private boolean alive;
	private String name;
	private boolean timerActive;
	private MonsterStatus cancelStatus = null;
	private GuardianSpawnPoint guardian = null;
	
	public MapleReactor(MapleReactorStats stats, int rid) {
		this.stats = stats;
		this.rid = rid;
		alive = true;
	}
	
	public void setTimerActive(boolean active) {
		this.timerActive = active;
	}
	
	public boolean isTimerActive() {
		return timerActive;
	}
	
	public int getReactorId() {
		return rid;
	}
	
	public void setState(byte state) {
		this.state = state;
	}
	
	public byte getState() {
		return state;
	}

    public void setMode(int state) {
		this.mode = state;
	}

	public int getMode() {
		return mode;
	}

	
	public int getId() {
		return rid;
	}
	
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	public int getDelay() {
		return delay;
	}
        
        public final byte getFacingDirection() {
	return stats.getFacingDirection();
        }

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.REACTOR;
	}
		
	public int getReactorType() {
		return stats.getType(state);
	}
	
	public void setMap(MapleMap map) {
		this.map = map;
	}
	
	public MapleMap getMap() {
		return map;
	}
	
	public Pair<Integer,Integer> getReactItem() {
		return stats.getReactItem(state);
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
        
        public void forceHitReactor(final byte newState) {
        setState((byte) newState);
        setTimerActive(false);
        map.broadcastMessage(MaplePacketCreator.triggerReactor(this, (short) 0));
        }
	
	@Override
	public void sendDestroyData(MapleClient client) {
		client.getSession().write(makeDestroyData());
	}
	
	public MaplePacket makeDestroyData() {
		return MaplePacketCreator.destroyReactor(this);
	}
	
	@Override
	public void sendSpawnData(MapleClient client) {
		client.getSession().write(makeSpawnData());
	}
	
	public MaplePacket makeSpawnData() {
		return MaplePacketCreator.spawnReactor(this);
	}
	
	public void delayedHitReactor(final MapleClient c, long delay) {
		TimerManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				hitReactor(c);
			}
		}, delay);
	}
	
	//hitReactor command for item-triggered reactors
	public void hitReactor(MapleClient c) {
		hitReactor(0, (short) 0, c);
	}
	
//	 public void hitReactor(int charPos, short stance, MapleClient c) {
//        if (stats.getType(state) < 999 && stats.getType(state) != -1) {
//            //type 2 = only hit from right (kerning swamp plants), 00 is air left 02 is ground left
//            if (!(stats.getType(state) == 2 && (charPos == 0 || charPos == 2))) {
//                //get next state
//                state = stats.getNextState(state);
//
//                if (stats.getNextState(state) == -1) {//end of reactor
//                    if (stats.getType(state) < 100) { //reactor broken
//                        if (delay > 0) {
//                            map.destroyReactor(getObjectId());
//                        } else {//trigger as normal
//                            map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//                        }
//                    } else { //item-triggered on final step
//                        map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//                    }
//                    ReactorScriptManager.getInstance().act(c, this);
//                } else { //reactor not broken yet
//                    map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//                    if (state == stats.getNextState(state)) { //current state = next state, looping reactor
//                        ReactorScriptManager.getInstance().act(c, this);
//                    }
//                }
//            }
//        }  else {
//			if (state > 4) {
//				if (this.getMap() != null) {
//					int team = 0;
//					switch (this.getId()) {
//						case 9980000:
//							team = 0;
//							break;
//						case 9980001:
//							team = 1;
//							break;
//					}
//					if (this.getMap().isCPQMap()) {
//						this.getMap().mapMessage(5, "[DEBUG] REACTOR DESTROYED");
//						if (this.getCancelStatus() != null) {
//							this.getMap().debuffMonsters(team, this.getCancelStatus());
//						}
//						if (this.getGuardian() != null) {
//							this.getGuardian().setTaken(false);
//						}
//						this.getMap().destroyReactor(this.getObjectId());
//					}
//				}
//			} 
//            state++;
//            map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//            ReactorScriptManager.getInstance().act(c, this);
//        }
//         if (map.getId() == 910010000) { // more HPQ things
//            for (int i = 9108000; i <= 9108005; i++) {
//                if (map.getReactorById(i).getState() != 1) {
//                    return;
//                }
//            }
//            map.broadcastMessage(MaplePacketCreator.triggerMoon(map.getReactorById(9101000).getObjectId()));
//            ReactorScriptManager.getInstance().act(c, map.getReactorById(9101000));
//        }
//  }
         
//         public void hitReactor(int charPos, short stance, MapleClient c) {
//        if (stats.getType(state) < 999 && stats.getType(state) != -1) {//type 2 = only hit from right (kerning swamp plants), 00 is air left 02 is ground left
//            if (!(stats.getType(state) == 2 && (charPos == 0 || charPos == 2))) { //get next state
//                state = stats.getNextState(state);
//                if (stats.getNextState(state) == -1) {//end of reactor
//                    if (stats.getType(state) < 100) {//reactor broken
//                        if (delay > 0) {
//                            map.destroyReactor(getObjectId());
//                        } else {//trigger as normal
//                            map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//                        }
//                    } else {//item-triggered on final step
//                        map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//                    }
//                    ReactorScriptManager.getInstance().act(c, this);
//                } else { //reactor not broken yet
//                    map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//                    if (state == stats.getNextState(state)) {//current state = next state, looping reactor
//                        ReactorScriptManager.getInstance().act(c, this);
//                    }
//                }
//            }
//        } else {
//            if (state > 4) {
//				if (this.getMap() != null) {
//					int team = 0;
//					switch (this.getId()) {
//						case 9980000:
//							team = 0;
//							break;
//						case 9980001:
//							team = 1;
//							break;
//					}
//					if (this.getMap().isCPQMap()) {
//						this.getMap().mapMessage(5, "[DEBUG] REACTOR DESTROYED");
//						if (this.getCancelStatus() != null) {
//							this.getMap().debuffMonsters(team, this.getCancelStatus());
//						}
//						if (this.getGuardian() != null) {
//							this.getGuardian().setTaken(false);
//						}
//						this.getMap().destroyReactor(this.getObjectId());
//					}
//				}
//			} 
//            state++;
//            map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//            ReactorScriptManager.getInstance().act(c, this);
//        }
////        if (map.getId() == 910010000) { // more HPQ things
////            for (int i = 9108000; i <= 9108005; i++) {
////                if (map.getReactorById(i).getState() != 1) {
////                    return;
////                }
////            }
////            map.broadcastMessage(MaplePacketCreator.triggerMoon(map.getReactorById(9101000).getObjectId()));
////            ReactorScriptManager.getInstance().act(c, map.getReactorById(9101000));
////        }
//    }
      
        public void hitReactor(int charPos, short stance, MapleClient c) {
		if (stats.getType(state) < 999 && stats.getType(state) != -1) {
			//type 2 = only hit from right (kerning swamp plants), 00 is air left 02 is ground left
			if (!(stats.getType(state) == 2 && (charPos == 0 || charPos == 2))) {
				//get next state
				state = stats.getNextState(state);

				if (stats.getNextState(state) == -1) {//end of reactor
					if (stats.getType(state) < 100
							&& this.getId() != 9980000
							&& this.getId() != 9980001) { //reactor broken
						if (delay > 0) {
							map.destroyReactor(getObjectId());
						}
						else {//trigger as normal
							map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
						}
					}
					else { //item-triggered on final step
						map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
					}
					ReactorScriptManager.getInstance().act(c, this);
				}
				else { //reactor not broken yet
					map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
					if (state == stats.getNextState(state)) { //current state = next state, looping reactor
						ReactorScriptManager.getInstance().act(c, this);
					}
				}
			}
		} else {
			if (state > 4) {
				if (this.getMap() != null) {
					int team = 0;
					switch (this.getId()) {
						case 9980000:
							team = 0;
							break;
						case 9980001:
							team = 1;
							break;
					}
					if (this.getMap().isCPQMap()) {
						this.getMap().mapMessage(5, "[DEBUG] REACTOR DESTROYED");
						if (this.getCancelStatus() != null) {
							this.getMap().debuffMonsters(team, this.getCancelStatus());
						}
						if (this.getGuardian() != null) {
							this.getGuardian().setTaken(false);
						}
						this.getMap().destroyReactor(this.getObjectId());
					}
				}
			}
			state++;
			map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
			ReactorScriptManager.getInstance().act(c, this);
		}
	}
        
//        public void hitReactor(int charPos, short stance, MapleClient c) {
//	if (stats.getType(state) < 999 && stats.getType(state) != -1) {
//	    //type 2 = only hit from right (kerning swamp plants), 00 is air left 02 is ground left
//
//	    if (!(stats.getType(state) == 2 && (charPos == 0 || charPos == 2))) { // next state
//		state = stats.getNextState(state);
//
//		if (stats.getNextState(state) == -1) { //end of reactor
//		    if (stats.getType(state) < 100) { //reactor broken
//			if (delay > 0) {
//			    map.destroyReactor(getObjectId());
//			} else {//trigger as normal
//			    map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//			}
//		    } else { //item-triggered on final step
//			map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//		    }
//		    ReactorScriptManager.getInstance().act(c, this);
//		} else { //reactor not broken yet
//		    map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
//		    if (state == stats.getNextState(state)) { //current state = next state, looping reactor
//			ReactorScriptManager.getInstance().act(c, this);
//		    }
//		}
//	    }
//	}
//    }
	
	public Rectangle getArea() {
		int height = stats.getBR().y - stats.getTL().y;
		int width = stats.getBR().x - stats.getTL().x;
		int origX = getPosition().x + stats.getTL().x;
		int origY = getPosition().y + stats.getTL().y;
		
		return new Rectangle(origX,origY,width,height);
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public MonsterStatus getCancelStatus() {
		return cancelStatus;
	}

	public void setCancelStatus(MonsterStatus cancelStatus) {
		this.cancelStatus = cancelStatus;
	}

	public GuardianSpawnPoint getGuardian() {
		return guardian;
	}

	public void setGuardian(GuardianSpawnPoint guardian) {
		this.guardian = guardian;
	}
	
	@Override
	public String toString() {
		return "Reactor " + getObjectId() + " of id " + rid + " at position " + getPosition().toString() + " state" + state + " type " + stats.getType(state);
	}
}
