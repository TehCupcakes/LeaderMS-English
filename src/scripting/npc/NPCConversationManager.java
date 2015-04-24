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

package scripting.npc;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.SkillFactory;
import config.configuration.Configuration;
import scripting.AbstractPlayerInteraction;
import scripting.event.EventManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import client.MapleStat;
import net.world.guild.MapleGuild;
import server.MapleSquad;
import server.MapleSquadType;
import server.maps.MapleMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import client.Equip;
import client.ISkill;
import client.MapleBuffStat;
import client.MapleQuestStatus;
import config.Game.LeaderOccupations;
import database.DatabaseConnection;
import net.channel.ChannelServer;
import net.channel.handler.DueyHandler;
import net.world.MaplePartyCharacter;
import net.world.guild.MapleAlliance;
import server.MaplePortal;
import server.MapleStatEffect;
import server.MonsterCarnival;
import server.PlayerNPCEngine;
import server.TimerManager;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.market.MarketEngine;
import server.market.MarketEngine.ItemEntry;
import tools.Randomizer;

/**
 *
 * @author Matze
 */
public class NPCConversationManager extends AbstractPlayerInteraction {

	private MapleClient c;
        protected int channel, playerCount = 0;
	private int npc;
	private String getText;
	private MapleCharacter chr;
        private Collection<MapleCharacter> characters = new LinkedHashSet<MapleCharacter>();
	private List<MaplePartyCharacter> otherParty;

	public NPCConversationManager(MapleClient c, int npc) {
		super(c);
		this.c = c;
		this.npc = npc;
	}
        
	public NPCConversationManager(MapleClient c, int npc, MapleCharacter chr) {
		super(c);
		this.c = c;
		this.npc = npc;
		this.chr = chr;
	}
	
	public NPCConversationManager(MapleClient c, int npc, List<MaplePartyCharacter> otherParty, int b) {
		super(c);
		this.c = c;
		this.npc = npc;
		this.otherParty = otherParty;
	}	
	
	/**
	 * Added in for consistency with PQ Npcs
	 * @return
	 */
	public MapleCharacter getChar() {
		return c.getPlayer();
	}
	
   public void closeDoor(int mapid)
   {
	   getClient().getChannelServer().getMapFactory().getMap(mapid).setReactorState();
   }

   public void openDoor(int mapid)
   {
	   getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
   }
   
      public void resetMap(int mapid) {
	   getClient().getChannelServer().getMapFactory().getMap(mapid).resetReactors();
   }
   
	public int getNpc() {
		return npc;
	}
        
	public boolean isMorphed() {
		boolean morph = false;

		Integer morphed = getPlayer().getBuffedValue(MapleBuffStat.MORPH);
		if (morphed != null) {
			morph = true;
		}
		return morph;
	}
        
	public int getMorphValue() { // 1= mushroom, 2= pig, 3= alien, 4= cornian, 5= arab retard
		try {
			int morphid = getPlayer().getBuffedValue(MapleBuffStat.MORPH).intValue();
			return morphid;
		} catch (NullPointerException n) {
			return -1;
		}
	}

	public void dispose() {
		NPCScriptManager.getInstance().dispose(this);
	}

	public void sendNext(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 01"));
	}

	public void sendPrev(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 00"));
	}

	public void sendNextPrev(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "01 01"));
	}

	public void sendOk(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0, text, "00 00"));
	}

	public void sendYesNo(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 1, text, ""));
	}

	public void sendAcceptDecline(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 0x0C, text, ""));
	}

	public void sendSimple(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalk(npc, (byte) 4, text, ""));
	}

	public void sendStyle(String text, int styles[]) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalkStyle(npc, text, styles));
	}

	public void sendGetNumber(String text, int def, int min, int max) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalkNum(npc, text, def, min, max));
	}

	public void sendGetText(String text) {
		getClient().getSession().write(MaplePacketCreator.getNPCTalkText(npc, text));
	}

	public void setGetText(String text) {
		this.getText = text;
	}

	public String getText() {
		return this.getText;
	}

	public void openShop(int id) {
		MapleShopFactory.getInstance().getShop(id).sendShop(getClient());
	}

	public void openNpc(int id) {
		dispose();
		NPCScriptManager.getInstance().start(getClient(), id, null, null);
	}

	public void changeJob(MapleJob job) {
		getPlayer().changeJob(job);
	}

	public MapleJob getJob() {
		return getPlayer().getJob();
	}

    public void startQuest(int id) {
        MapleQuest.getInstance(id).start(getPlayer(), npc);
    }

    public void completeQuest(int id) {
        MapleQuest.getInstance(id).complete(getPlayer(), npc);
    }

    public void forfeitQuest(int id) {
        MapleQuest.getInstance(id).forfeit(getPlayer());
    }

	public void gainMeso(int gain) {
		getPlayer().gainMeso(gain, true, false, true);
	}

	public void gainExp(int gain) {
		getPlayer().gainExp(gain * c.getChannelServer().getExpRate(), true, true);
	}

	/**
	 * use getPlayer().getLevel() instead
	 * @return
	 */
	@Deprecated
	public int getLevel() {
		return getPlayer().getLevel();
	}
        
        public int getPlayerCount(int mapid) {
        return c.getChannelServer().getMapFactory().getMap(mapid).getCharacters().size();
        }
        
       public void warpOutOfBossPQ(int map, boolean wholeMap) { 
        if (wholeMap) { 
            for (MapleCharacter currmap : getPlayer().getMap().getCharacters()) { 
                currmap.getTimerManager().stop(); 
                currmap.changeMap(map == 0 ? 910000000 : map); 
                currmap.getTimerManager().start(); 
            } 
        } else { 
            TimerManager.getInstance().stop(); 
            getPlayer().changeMap(map == 0 ? 910000000 : map); 
            TimerManager.getInstance().start(); 
        } 
    } 
        
	public void unequipEverything() {
		MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
		MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
		List<Byte> ids = new LinkedList<Byte>();
		for (IItem item : equipped.list()) {
			ids.add(item.getPosition());
		}
		for (byte id : ids) {
			MapleInventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
		}
	}

	public void teachSkill(int id, int level, int masterlevel) {
		getPlayer().changeSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
	}
        
         public int getJobId() { 
        return getPlayer().getJob().getId(); 
    }

	public void clearSkills() {
		Map<ISkill, MapleCharacter.SkillEntry> skills = getPlayer().getSkills();
		for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
			getPlayer().changeSkillLevel(skill.getKey(), 0, 0);
		}
	}

	public MapleClient getC() {
		return getClient();
	}

	public void rechargeStars() {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		IItem stars = getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) 1);
		if (ii.isThrowingStar(stars.getItemId()) || ii.isBullet(stars.getItemId())) {
			stars.setQuantity(ii.getSlotMax(getClient(), stars.getItemId()));
			getC().getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) stars));
		}
	}

	public EventManager getEventManager(String event) {
		return getClient().getChannelServer().getEventSM().getEventManager(event);
	}

	public void showEffect(String effect) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
	}

	public void playSound(String sound) {
		getClient().getPlayer().getMap().broadcastMessage(MaplePacketCreator.playSound(sound));
	}

	@Override
	public String toString() {
		return "Conversation with NPC: " + npc;
	}

	public void updateBuddyCapacity(int capacity) {
		getPlayer().setBuddyCapacity(capacity);
	}

	public int getBuddyCapacity() {
		return getPlayer().getBuddyCapacity();
	}

	public void setHair(int hair) {
		getPlayer().setHair(hair);
		getPlayer().updateSingleStat(MapleStat.HAIR, hair);
		getPlayer().equipChanged();
	}

	public void setFace(int face) {
		getPlayer().setFace(face);
		getPlayer().updateSingleStat(MapleStat.FACE, face);
		getPlayer().equipChanged();
	}

	@SuppressWarnings("static-access")
	public void setSkin(int color) {
		getPlayer().setSkinColor(getPlayer().getSkinColor().getById(color));
		getPlayer().updateSingleStat(MapleStat.SKIN, color);
		getPlayer().equipChanged();
	}

	public void warpParty(int mapId) {
		MapleMap target = getMap(mapId);
		for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
			MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
			if ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
				curChar.changeMap(target, target.getPortal(0));
			}
		}
	}
        
            public String getName() {
            return getPlayer().getName();
    }

	public void warpPartyWithExp(int mapId, int exp) {
		MapleMap target = getMap(mapId);
		for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
			MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
			if ((curChar.getEventInstance() == null && c.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
				curChar.changeMap(target, target.getPortal(0));
				curChar.gainExp(exp, true, false, true);
			}
		}
	}

	public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
		MapleMap target = getMap(mapId);
		for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
			MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
			if ((curChar.getEventInstance() == null && c.getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance()) {
				curChar.changeMap(target, target.getPortal(0));
				curChar.gainExp(exp, true, false, true);
				curChar.gainMeso(meso, true);
			}
		}
	}

	public void warpRandom(int mapid) {
		MapleMap target = c.getChannelServer().getMapFactory().getMap(mapid);
		Random rand = new Random();
		MaplePortal portal = target.getPortal(rand.nextInt(target.getPortals().size())); //generate random portal
		getPlayer().changeMap(target, portal);
	}

	public int itemQuantity(int itemid) {
		MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
		MapleInventory iv = getPlayer().getInventory(type);
		int possesed = iv.countById(itemid);
		return possesed;
	}

	public MapleSquad createMapleSquad(MapleSquadType type) {
		MapleSquad squad = new MapleSquad(c.getChannel(), getPlayer());
		if (getSquadState(type) == 0) {
			c.getChannelServer().addMapleSquad(squad, type);
		} else {
			return null;
		}
		return squad;
	}

	public MapleCharacter getSquadMember(MapleSquadType type, int index) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		MapleCharacter ret = null;
		if (squad != null) {
			ret = squad.getMembers().get(index);
		}
		return ret;
	}

	public int getSquadState(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			return squad.getStatus();
		} else {
			return 0;
		}
	}

	public void setSquadState(MapleSquadType type, int state) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.setStatus(state);
		}
	}

	public boolean checkSquadLeader(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			if (squad.getLeader().getId() == getPlayer().getId()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void removeMapleSquad(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			if (squad.getLeader().getId() == getPlayer().getId()) {
				squad.clear();
				c.getChannelServer().removeMapleSquad(squad, type);
			}
		}
	}

	public int numSquadMembers(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		int ret = 0;
		if (squad != null) {
			ret = squad.getSquadSize();
		}
		return ret;
	}

	public boolean isSquadMember(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		boolean ret = false;
		if (squad.containsMember(getPlayer())) {
			ret = true;
		}
		return ret;
	}

	public void addSquadMember(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.addMember(getPlayer());
		}
	}

	public void removeSquadMember(MapleSquadType type, MapleCharacter chr, boolean ban) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			squad.banMember(chr, ban);
		}
	}

	public void removeSquadMember(MapleSquadType type, int index, boolean ban) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			MapleCharacter chr = squad.getMembers().get(index);
			squad.banMember(chr, ban);
		}
	}
        
        public ChannelServer getCanalServer() {
        return ChannelServer.getInstance(channel);
    }
        
        public void gainExpCard() {
        long add = 1000L * 60L * 60L * 6L;
        IItem ret = MapleItemInformationProvider.getInstance().getEquipById(5211048);
        ret.setExpiration(System.currentTimeMillis() + add);
        MapleInventoryManipulator.addFromDrop(c, ret, "Vote Reward");
    }
        
        	public void gainFame(int fame) {
                getPlayer().gainFame(fame);
        }
    
    public void gainDropCard() {
        long add = 1000L * 60L * 60L * 6L;
        IItem ret = MapleItemInformationProvider.getInstance().getEquipById(5360042);
        ret.setExpiration(System.currentTimeMillis() + add);
        MapleInventoryManipulator.addFromDrop(c, ret, "Vote Reward");
    }

	public boolean canAddSquadMember(MapleSquadType type) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		if (squad != null) {
			if (squad.isBanned(getPlayer())) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	public void warpSquadMembers(MapleSquadType type, int mapId) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
		if (squad != null) {
			if (checkSquadLeader(type)) {
				for (MapleCharacter chr : squad.getMembers()) {
					chr.changeMap(map, map.getPortal(0));
				}
			}
		}
	}
        
   public int getBossLog(String bossid) {
        return getPlayer().getBossLog(bossid);
    }

    public int getGiftLog(String bossid) {
        return getPlayer().getGiftLog(bossid);
    }

    public void setBossLog(String bossid) {
        getPlayer().setBossLog(bossid);
    }


	public void resetReactors() {
		getPlayer().getMap().resetReactors();
	}

	public void displayGuildRanks() {
		MapleGuild.displayGuildRanks(getClient(), npc);
	}

	public void openDuey() {
            c.getSession().write(MaplePacketCreator.sendDuey((byte) 8, DueyHandler.loadItems(this.getPlayer())));
	}
        
     public int[] hpqmaps = {
         /* Henesys PQ */
        910010000, 910010200, 910010300, 910010400,/* Henesys fim */
        0
    };
    
    public boolean inHPQ() {
        boolean inhpq = false;
        for (int i = 0; i < hpqmaps.length; i++) {
            if (getMapId() == hpqmaps[i]) {
                inhpq = true;
            }
        }
        return inhpq;
    }
    
	/**
	 * This returns the OTHER character associated with this CM.
	 * @return
	 */
	public MapleCharacter getCharacter() {
		return chr;
	}

   public MapleCharacter getCharByName(String namee) {
	   try {
		return getClient().getChannelServer().getPlayerStorage().getCharacterByName(namee);
	   } catch (Exception e) {
		   return null;
	   }
   }

   
   public void updateAlianca(MapleCharacter player) throws RemoteException {
       int allianceId = player.getGuild().getAllianceId();
       MapleAlliance newAlliance = c.getChannelServer().getWorldInterface().getAlliance(allianceId);
       c.getSession().write(MaplePacketCreator.getAllianceInfo(newAlliance));
   }
   
   
	public void warpAllInMap(int mapid, int portal) {
			MapleMap outMap;
			MapleMapFactory mapFactory;
			mapFactory = ChannelServer.getInstance(c.getChannel()).getMapFactory();
			outMap = mapFactory.getMap(mapid);
			for (MapleCharacter aaa : outMap.getCharacters()) {
				//Warp everyone out
				mapFactory = ChannelServer.getInstance(aaa.getClient().getChannel()).getMapFactory();
				aaa.getClient().getPlayer().changeMap(outMap, outMap.getPortal(portal));
				outMap = mapFactory.getMap(mapid);
				aaa.getClient().getPlayer().getEventInstance().unregisterPlayer(aaa.getClient().getPlayer()); //Unregister them all
			}
	}
        
            public void addRandomItem(int id) {
           MapleItemInformationProvider i = MapleItemInformationProvider.getInstance();
           MapleInventoryManipulator.addFromDrop(getClient(), i.randomizeStats((Equip) i.getEquipById(id)), "");
        }

        
        public void processGachapon(int[] id, boolean remote) {
        int[] gacMap = {100000000, 101000000, 102000000, 103000000, 105040300, 800000000, 809000101, 809000201, 600000000, 120000000};
        int itemid = id[Randomizer.nextInt(id.length)];
        addRandomItem(itemid);
        if (!remote) {
            gainItem(5220000, (short) -1);
        }
        sendNext("You got one #b#t" + itemid + "##k.");
        dispose();
        }
        
        
        
    public void changeJobById(int a) { 
        getPlayer().changeJob(MapleJob.getById(a)); 
    } 
    
    public boolean isQuestCompleted(int quest) {
        try {
            return getQuestStatus(quest) == MapleQuestStatus.Status.COMPLETED;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean isQuestStarted(int quest) {
        try {
            return getQuestStatus(quest) == MapleQuestStatus.Status.STARTED;
        } catch (NullPointerException e) {
            return false;
        }
    }

   public int countMonster() {
		MapleMap map = c.getPlayer().getMap();
		double range = Double.POSITIVE_INFINITY;
		List<MapleMapObject> monsters = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays
				.asList(MapleMapObjectType.MONSTER));
		return monsters.size();
	}

	public int countReactor() {
		MapleMap map = c.getPlayer().getMap();
		double range = Double.POSITIVE_INFINITY;
			List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays
					.asList(MapleMapObjectType.REACTOR));
			return reactors.size();
	}

	public int getDayOfWeek() {
		Calendar cal = Calendar.getInstance();
		int dayy = cal.get(Calendar.DAY_OF_WEEK);
		return dayy;
	}

	public void giveNPCBuff(MapleCharacter chr, int itemID) {
		MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
		MapleStatEffect statEffect = mii.getItemEffect(itemID);
		statEffect.applyTo(chr);
	}

	public void giveWonkyBuff(MapleCharacter chr){
		long what = Math.round(Math.random() * 4);
		int what1 = (int)what;
		int Buffs[] = {2022090, 2022091, 2022092, 2022093} ;
		int buffToGive = Buffs[what1];
		MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
		MapleStatEffect statEffect = mii.getItemEffect(buffToGive);
		//for (MapleMapObject mmo =  this.getParty()) {
		MapleCharacter character = (MapleCharacter) chr;
		statEffect.applyTo(character);
		//}
	}
        
    public void jqComplete() {
            getPlayer().gainJQPoints();
            getPlayer().alertEndedJQ();
    }
    
    public void initiateJQ(int map) {
        getPlayer().alertInitiateJQ(map);
    }
         
    private int getChannel() {
        return c.getChannel();
    }
	
	
	public void addItemToMarket(int itemid, int quantity, int price) {
		this.c.getChannelServer().getMarket().addItem(itemid, quantity, price, c.getPlayer().getId());
	}
        
        public void editEquipById(int input, byte stat, short value, boolean supergacha) {
if(supergacha) {
MapleInventoryManipulator.editEquipById(getPlayer(), input, stat, value, true);
return;
}
MapleInventoryManipulator.editEquipById(getPlayer(), input, stat, value);
}

public void reloadChar() {
getPlayer().getClient().getSession().write(MaplePacketCreator.getCharInfo(getPlayer()));
getPlayer().getMap().removePlayer(getPlayer());
getPlayer().getMap().addPlayer(getPlayer());
}

public byte gainItemRetPos(int itemid) {
MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
IItem it = ii.getEquipById(itemid);
byte ret = getPlayer().getInventory(MapleInventoryType.EQUIP).addItem(it);
c.getSession().write(MaplePacketCreator.addInventorySlot(MapleInventoryType.EQUIP, it));
c.getSession().write(MaplePacketCreator.getShowItemGain(itemid, (short)1, true));
c.getSession().write(MaplePacketCreator.enableActions());
return ret;
}

public void serverNotice(String msg) {
getPlayer().sendServerNotice(msg);
}  
	
 public void changeOccupationById(int occ) {
        getPlayer().changeOccupation(LeaderOccupations.getById(occ));
        LeaderOccupations.getOcupacaoItem(c);
        getPlayer().sendServerNotice("<Job Leader> Congratulations " + getPlayer().getName() + ", you are now a " + getPlayer().getOccupation() + ".");
    }
 
  
     public boolean HasOccupation() {
        return (getPlayer().getOccupation().getId() % 100 == 0);
    }public boolean HasOccupation0() {
        return (getPlayer().getOccupation().getId() == 1);
    }public boolean HasOccupation1() {
        return (getPlayer().getOccupation().getId() == 100);
    }public boolean HasOccupation2() {
        return (getPlayer().getOccupation().getId() == 110);
    }public boolean HasOccupation3() {
        return (getPlayer().getOccupation().getId() == 120);
    }public boolean HasOccupation4() {
        return (getPlayer().getOccupation().getId() == 130);
    }public boolean HasOccupation5() {
        return (getPlayer().getOccupation().getId() == 140);
    }public boolean HasOccupation6() {
        return (getPlayer().getOccupation().getId() == 150);
    }public boolean HasOccupation7() {
        return (getPlayer().getOccupation().getId() == 160);
    }public boolean HasOccupation8() {
        return (getPlayer().getOccupation().getId() == 170);
    }public boolean HasOccupation9() {
        return (getPlayer().getOccupation().getId() == 180);
    }public boolean HasOccupation10() {
        return (getPlayer().getOccupation().getId() == 190);
    }

    public boolean isPlayerInstance() {
        if (c.getPlayer().getEventInstance() != null) {
            return true;
        }
        return false;
    }
    

	public void gainNeckson(int gain) {
		c.getPlayer().modifyCSPoints(4, gain);
		if (gain > 0) {
			playerMessage(5, "You gained LeaderNX (+" + gain + ")");
		} else {
			playerMessage(5, "You lost LeaderNX (-" + gain + ")");
		}
	}
	
	public void removeItemFromMarket(int itemid, int quantity) {
		this.c.getChannelServer().getMarket().removeItem(itemid, quantity, c.getPlayer().getId());
	}
	
        
            public int getMeso() {
        return getPlayer().getMeso();
    }
            
        public int getPlayersInMap(int mapId)  {                 
        return(getClient().getChannelServer().getMapFactory().getMap(mapId).getAllPlayer().size());         
    }
        
        
	public void buyItem(int itemId, int quantity, int price, int charId) {
		try {
			for (ItemEntry ie : c.getChannelServer().getMarket().getItems()) {
				if (ie.getId() == itemId && ie.getPrice() == price &&
						ie.getOwner() == charId) {
					if (ie.getQuantity() < quantity) {
						c.getSession().write(MaplePacketCreator.serverNotice(1, 
								"You're trying to buy more than available!"));
						return;
					}
					if (ie.getQuantity() * ie.getPrice() > c.getPlayer().getMeso()) {
						c.getSession().write(MaplePacketCreator.serverNotice(1, 
								"You don't have enough mesos!"));
						return;					
					}
					int cost = ie.getPrice() * ie.getQuantity();
					c.getChannelServer().getMarket().removeItem(itemId, quantity, charId);
					c.getPlayer().gainMeso(-cost, true, true, true);
					gainItem(itemId, (short)quantity);
					for (ChannelServer cs : ChannelServer.getAllInstances()) {
						for (MapleCharacter mc : cs.getPlayerStorage().getAllCharacters()) {
							if (mc.getId() == charId) {
								mc.gainMeso(cost, false, true, true);
								mc.getClient().getSession().write(MaplePacketCreator.
										serverNotice(5, "[Market] You have gained " + cost + " mesos from " + 
										c.getPlayer().getName() + "."));
								return;
							}
						}
					}
					//OMG the other player was not found..
					Connection con = DatabaseConnection.getConnection();
					try {
						PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
						ps.setInt(1, charId);
						ResultSet rs = ps.executeQuery();
						if (rs.next()) {
							int meso = rs.getInt("meso");
							int gain = meso + cost;
							ps = con.prepareStatement("UPDATE characters SET meso = ? WHERE id = ?");
							ps.setInt(1, gain);
							ps.setInt(2, charId);
							ps.executeUpdate();
						}
						ps.close();
						rs.close();
					} catch (SQLException dang) {

					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void showInventory(int type) {
		String send = "";
		MapleInventory invy = c.getPlayer().getInventory(MapleInventoryType.getByType((byte)type));
		for (IItem item : invy.list()) {
			send += "#L" + item.getPosition() +
					"##v" + item.getItemId() + "# Quantity: #b" + item.getQuantity() + "#k#l\\r\\n";
		}
		sendSimple(send);
	}
	
	public String getInventory (int type) {
		String send = "";
		MapleInventory invy = c.getPlayer().getInventory(MapleInventoryType.getByType((byte)type));
		for (IItem item : invy.list()) {
			send += "#L" + item.getPosition() + 
					"##v" + item.getItemId() + "# Quantity: #b" + item.getQuantity() + "#k#l\\r\\n";
		}
		return send;
	}
	
	public IItem getItem(int slot, int type) {
		MapleInventory invy = c.getPlayer().getInventory(MapleInventoryType.getByType((byte)type));
		for (IItem item : invy.list()) {
			if (item.getPosition() == slot) {
				return item;
			}
		}
		return null;
	}
        
	     
	public String getMarket() {
		MarketEngine me = c.getChannelServer().getMarket();
		String ret = "";
		int count = 0;
		for (ItemEntry ie : me.getItems()) {
			if (ie.getOwner() == c.getPlayer().getId()) //Don't let him see their own items
				continue;
			ret += "#L" + count + "##v" + 
					ie.getId() + 
					"# #bQuantity#k: " + 
					ie.getQuantity() + 
					" #bCost#k: " + 
					ie.getPrice() + " mesos" + 
					" #b Owner: #k" + 
					me.getCharacterName(ie.getOwner()) + 
					"#l\\r\\n";
			count ++;
		}
		return ret;
	}
	
	public String getMarketRetrival() {
		MarketEngine me = c.getChannelServer().getMarket();
		String ret = "";
		int count = 0;
		for (ItemEntry ie : me.getItems()) {
			if (ie.getOwner() != c.getPlayer().getId()) //Only own items
				continue;
			ret += "#L" + count + "##v" + 
					ie.getId() + 
					"# #bQuantity#k: " + 
					ie.getQuantity() + 
					" #bCost#k: " + 
					ie.getPrice() + " mesos" +
					"#l\\r\\n";
			count ++;
		}
		return ret;		
	}
	
	public List<ItemEntry> getMyMarketItems() {
		List<ItemEntry> ret = new LinkedList<ItemEntry>();
		synchronized (c.getChannelServer().getMarket().getItems()) {
			for (ItemEntry ie : c.getChannelServer().getMarket().getItems()) {
				if (ie.getOwner() == c.getPlayer().getId()) {
					ret.add(ie);
				}
			}
		}
		return ret;
	}
	
	public void retrieveMarketItem(int position) {
		List<ItemEntry> items = getMyMarketItems();
		ItemEntry ie = items.get(position);
		gainItem(ie.getId(), (short) ie.getQuantity());
		removeItemFromMarket(ie.getId(), ie.getQuantity());
	}
	
	public List<ItemEntry> getMarketItems() {
		List<ItemEntry> ret = new LinkedList<ItemEntry>();
		synchronized (c.getChannelServer().getMarket().getItems()) {
			for (ItemEntry ie : c.getChannelServer().getMarket().getItems()) {
				if (ie.getOwner() != c.getPlayer().getId())
					ret.add(ie);
			}
		}
		return ret;
	}
	
	public void warpSquadMembersClock(MapleSquadType type, int mapId, int clock, int mapExit) {
		MapleSquad squad = c.getChannelServer().getMapleSquad(type);
		MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
                MapleMap map1 = c.getChannelServer().getMapFactory().getMap(mapExit);
		if (squad != null) {
			if (checkSquadLeader(type)) {
				for (MapleCharacter ch : squad.getMembers()) {
					ch.changeMap(map, map.getPortal(0));
						ch.getClient().getSession().write(MaplePacketCreator.getClock(clock));
				}
				map.scheduleWarp(map, map1, (long) clock * 1000);
			}
		}
	}
        
	public MapleSquad getSquad(MapleSquadType Type) {
		return c.getChannelServer().getMapleSquad(Type);
	}
        
     public int getLeaderPoints(){
        return getPlayer().getLeaderPoints();
    }

    public void gainLeaderPoints(int gain){
        getPlayer().gainLeaderPoints(gain);
        getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, "[LeaderPoints] You "+(gain > 0 ? "gained":"lost") +" (" + gain + ") LeaderPoints."));
    }
    
    public int getvotePoints(){
        return getPlayer().getvotePoints();
    }

    public void gainvotePoints(int gain){
        getPlayer().gainvotePoints(gain);
        getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(6, "[Vote Points] You "+(gain > 0 ? "gained":"lost") +" (" + gain + ") Vote Points."));
    }
    
     public int getPQPoints(){
        return getPlayer().getpqPoints();
    }

    public void gainPQPoints(int gain){
        getPlayer().gainpqPoints(gain);
        getPlayer().dropMessage("["+Configuration.Server_Name+" Quest] Quest complete successfully. You now have (" + getPlayer().getpqPoints() + ") Q.Points!");    }
	
 
	
	public void startCPQ(final MapleCharacter challenger, int field) {
		try {
			if (challenger != null) {
				if (challenger.getParty() == null) throw new RuntimeException("Challenger's party was null!");
				for (MaplePartyCharacter mpc : challenger.getParty().getMembers()) {
					MapleCharacter mc;
					mc = c.getChannelServer().getPlayerStorage().getCharacterByName(mpc.getName());
					if (mc != null) {
						mc.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().getPortal(0));
						mc.getClient().getSession().write(MaplePacketCreator.getClock(10));
					}
				}
			}
			final int mapid = c.getPlayer().getMap().getId() + 1;
			TimerManager.getInstance().schedule(new Runnable() {
				        @Override
                                        public void run() {
					MapleMap map;
					ChannelServer cs = c.getChannelServer();
					map = cs.getMapFactory().getMap(mapid);
					new MonsterCarnival(getPlayer().getParty(), challenger.getParty(), mapid);
					map.broadcastMessage(MaplePacketCreator.serverNotice(5, "Monster Carnival has begun!"));
				}
			}, 10000);
			mapMessage(5, "Monster Carnival will begin in 10 seconds!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
        
        
       public int calcAvgLvl(int map) {
		int num = 0;
		int avg = 0;
		for (MapleMapObject mmo : 
			c.getChannelServer().getMapFactory().getMap(map).getAllPlayer()) {
				avg += ((MapleCharacter) mmo).getLevel();
			num ++;
		}
		avg /= num;
		return avg;
	}
	
	public void sendCPQMapLists() {
		String msg = "Pick a field:\\r\\n";
		for (int i = 0; i < 6; i++) {
			if (fieldTaken(i)) {
				if (fieldLobbied(i)) {
					msg += "#b#L" + i + "#Monster Carnival Field " + (i + 1) + " Avg Lvl: " + 
							calcAvgLvl(980000100 + i * 100) + "#l\\r\\n";
				} else {
					continue;
				}
			} else {
				msg += "#b#L" + i + "#Monster Carnival Field " + (i + 1) + "#l\\r\\n";
			}
		}
		sendSimple(msg);
	}
	
	public boolean fieldTaken(int field) {
		if (c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().size() != 0)
			return true;
		if (c.getChannelServer().getMapFactory().getMap(980000101 + field * 100).getAllPlayer().size() != 0)
			return true;		
		if (c.getChannelServer().getMapFactory().getMap(980000102 + field * 100).getAllPlayer().size() != 0)
			return true;
		return false;		
	}
	
	public boolean fieldLobbied(int field) {
		if (c.getChannelServer().getMapFactory().getMap(980000100 + field * 100).getAllPlayer().size() != 0)
			return true;
		return false;
	}
	
	public void cpqLobby(int field) {
		try {
			MapleMap map;
			ChannelServer cs = c.getChannelServer();
			map = cs.getMapFactory().getMap(980000100 + 100 * field);
			for (MaplePartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
				MapleCharacter mc;
				mc = cs.getPlayerStorage().getCharacterByName(mpc.getName());
				if (mc != null) {
					mc.changeMap(map, map.getPortal(0));
					mc.getClient().getSession().write(MaplePacketCreator.serverNotice(5, 
					"You will now recieve challenges from other parties. If you do not accept a challenge in 3 minutes, you will be kicked out."));
					mc.getClient().getSession().write(MaplePacketCreator.getClock(3 * 60));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public MapleCharacter getChrById(int id) {
		ChannelServer cs = c.getChannelServer();
		return cs.getPlayerStorage().getCharacterById(id);
	}
	
//	public void startCPQ(final MapleCharacter challenger, int field) {
//		try {
//			if (challenger != null) {
//				if (challenger.getParty() == null) throw new RuntimeException("Challenger's party was null!");
//				for (MaplePartyCharacter mpc : challenger.getParty().getMembers()) {
//					MapleCharacter mc;
//					mc = c.getChannelServer().getPlayerStorage().getCharacterByName(mpc.getName());
//					if (mc != null) {
//						mc.changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().getPortal(0));
//						mc.getClient().getSession().write(MaplePacketCreator.getClock(10));
//					}
//				}
//			}
//			final int mapid = c.getPlayer().getMap().getId() + 1;
//			TimerManager.getInstance().schedule(new Runnable() {
//				@Override public void run() {
//					MapleMap map;
//					ChannelServer cs = c.getChannelServer();
//					map = cs.getMapFactory().getMap(mapid);
//					new MonsterCarnival(getPlayer().getParty(), challenger.getParty(), mapid);
//					map.broadcastMessage(MaplePacketCreator.serverNotice(5, 
//							"Monster Carnival has begun!"));
//				}
//			}, 10000);
//			mapMessage(5, "Monster Carnival will begin in 10 seconds!");
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	public void challengeParty(int field) {
		MapleCharacter leader = null;
		MapleMap map = c.getChannelServer().getMapFactory().getMap(980000100 + 100 * field);
		for (MapleMapObject mmo : map.getAllPlayer()) {
			MapleCharacter mc = (MapleCharacter) mmo;
			if (mc.getParty().getLeader().getId() == mc.getId()) {
				leader = mc;
				break;
			}
		}
		if (leader != null) {
			if (!leader.isChallenged()) {
				List<MaplePartyCharacter> party = new LinkedList<>();
				for (MaplePartyCharacter player : c.getPlayer().getParty().getMembers()) {
					party.add(player);
				}
				NPCScriptManager.getInstance().start("cpqchallenge", leader.getClient(), npc, party);
			} else {
				sendOk("The other party is currently taking on a different challenge.");
			}
		} else {
			sendOk("Could not find leader!");
		}
	}
        
	
	public int applyForLord() {
		return c.getPlayer().applyForLord();
	}
	
	public String getLords() {
		Connection con = DatabaseConnection.getConnection();
		String ret = "";
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM lordvotes");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int votes = rs.getInt("votes");
				int charid = rs.getInt("charid");
				String name = c.getChannelServer().getMarket().getCharacterName(charid);
				ret += "#L" + charid + "##b" + name + "#k #evotes#n: " + votes + "\\r\\n";
			}
			return ret;
		} catch (Exception ex) {
			return "There was a database error.\\r\\n";
		}
	}
	
	public boolean voteForLord(int cid) {
		return c.getPlayer().voteForLord(cid);
	}
	
	public String getCharName(int id) {
		return c.getChannelServer().getMarket().getCharacterName(id);
	}
	
	//public int calculateCPQRanking() {
	//	return getPlayer().getCpqRanking();
	//}
        
        public int calculateCPQRanking() {
            if (c.getPlayer().getMap().isCPQWinnerMap()) {
                if (c.getPlayer().getTotalCP() > 250) {
                    return 1; // Rank [A] Ganhador
                } else if (c.getPlayer().getTotalCP() > 100) {
                    return 2; // Rank [B] Ganhador
                } else if (c.getPlayer().getTotalCP() > 50) {
                    return 3; // Rank [C] Ganhador
                } else if (c.getPlayer().getTotalCP() < 50) {
                    return 4; // Rank [D] Ganhador
                }  else if (c.getPlayer().getTotalCP() == 0) {
                    return 4; // Rank [D] Ganhador
                }
            } else if (c.getPlayer().getMap().isCPQLoserMap()) {
                if (c.getPlayer().getTotalCP() > 250) {
                    return 10; // Rank [A] Perdedor
                } else if (c.getPlayer().getTotalCP() > 100) {
                    return 20; // Rank [B] Perdedor
                } else if (c.getPlayer().getTotalCP() > 50) {
                    return 30; // Rank [C] Perdedor
                } else if (c.getPlayer().getTotalCP() < 50) {
                    return 40; // Rank [D] Perdedor
                }  else if (c.getPlayer().getTotalCP() == 0) {
                     return 40; // Rank [D] Perdedor
                }     
            }
            return 999; //
        }
	
	public void createEngagement(MapleCharacter arg1, MapleCharacter arg2) {
		Marriage.createEngagement(arg1, arg2);
	}
       
	public int createMarriage(int hchr, int wchr) {
		Marriage.createMarriage(hchr, wchr);
		return 1;
	}
	
        public boolean createEngagement(String partner_) {
        MapleCharacter partner = getCharByName(partner_);
        if (partner == null) {
            return false;
        }
        if (partner.getGender() > 0) {
            Marriage.createEngagement(getPlayer(), partner);
        } else {
            Marriage.createEngagement(partner, getPlayer());
        }
        return true;
    }
        
        
	public List<MapleCharacter> getPartyMembers() {
		if (getPlayer().getParty() == null) {
			return null;
		}
		List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); // creates an empty array full of stuff..
		for (ChannelServer channel : ChannelServer.getAllInstances()) {
			for (MaplePartyCharacter a : getPlayer().getParty().getMembers()) {
				MapleCharacter ch = channel.getPlayerStorage().getCharacterByName(a.getName());
				if (ch != null) { // double check <3
					chars.add(ch);
				}
			}
		}
		return chars;
	}
	
	public int partyMembersInMap() {
		int inMap = 0;
		for (MapleCharacter char2 : getPlayer().getMap().getCharacters()) {
			if (char2.getParty() == getPlayer().getParty()) {
				inMap++;
			}
		}
		return inMap;
	}
	
     public boolean hasTemp() {
        if (!getPlayer().hasMerchant() && getPlayer().tempHasItems()) {
            return true;
        } else {
            return false;
        }
    }

    public int getHiredMerchantMesos() {
        Connection con = DatabaseConnection.getConnection();
        int mesos;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT MerchantMesos FROM characters WHERE id = ?");
            ps.setInt(1, getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            rs.next();
            mesos = rs.getInt("MerchantMesos");
            rs.close();
            ps.close();
        } catch (SQLException se) {
            return 0;
        }
        return mesos;
    }

    public void setHiredMerchantMesos(int set) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = ? WHERE id = ?");
            ps.setInt(1, set);
            ps.setInt(2, getPlayer().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public void removeHiredMerchantItem(boolean tempItem, int itemId) {
        String Table = "";
        if (tempItem) Table = "temp";
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM hiredmerchant" + Table + " WHERE itemid = ? AND ownerid = ? LIMIT 1");
            ps.setInt(1, itemId);
            ps.setInt(2, getPlayer().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
    }
    
     public void removeDonationItem(boolean tempItem, int itemId) {
        String Table = "";
        if (tempItem) Table = "temp";
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM donationitems" + Table + " WHERE itemid = ? AND ownerid = ? LIMIT 1");
            ps.setInt(1, itemId);
            ps.setInt(2, getPlayer().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public boolean getHiredMerchantItems(boolean tempTable) {
        boolean temp = false, compleated = false;
        String Table = "";
        if (tempTable) {
            Table = "temp";
            temp = true;
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM hiredmerchant" + Table + " WHERE ownerid = ?");
            ps.setInt(1, getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("type") == 1) {
                    Equip spItem = new Equip(rs.getInt("itemid"), (byte) 0, -1);
                    spItem.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    spItem.setLevel((byte) rs.getInt("level"));
                    spItem.setStr((short) rs.getInt("str"));
                    spItem.setDex((short) rs.getInt("dex"));
                    spItem.setInt((short) rs.getInt("int"));
                    spItem.setLuk((short) rs.getInt("luk"));
                    spItem.setHp((short) rs.getInt("hp"));
                    spItem.setMp((short) rs.getInt("mp"));
                    spItem.setWatk((short) rs.getInt("watk"));
                    spItem.setMatk((short) rs.getInt("matk"));
                    spItem.setWdef((short) rs.getInt("wdef"));
                    spItem.setMdef((short) rs.getInt("mdef"));
                    spItem.setAcc((short) rs.getInt("acc"));
                    spItem.setAvoid((short) rs.getInt("avoid"));
                    spItem.setHands((short) rs.getInt("hands"));
                    spItem.setSpeed((short) rs.getInt("speed"));
                    spItem.setJump((short) rs.getInt("jump"));
                    spItem.setOwner(rs.getString("owner"));
                    if (!getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                        MapleInventoryManipulator.addFromDrop(c, spItem, "", true);
                        removeHiredMerchantItem(temp, spItem.getItemId());
                    } else {
                        rs.close();
                        ps.close();
                        return false;
                    }
                } else {
                    Item spItem = new Item(rs.getInt("itemid"), (byte) 0, (short) rs.getInt("quantity"));
                    spItem.setOwner(rs.getString("owner"));
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    MapleInventoryType type = ii.getInventoryType(spItem.getItemId());
                    if (!getPlayer().getInventory(type).isFull()) {
                        MapleInventoryManipulator.addFromDrop(c, spItem, "", true);
                        removeHiredMerchantItem(temp, spItem.getItemId());
                    } else {
                        rs.close();
                        ps.close();
                        return false;
                    }
                }
            }
            rs.close();
            ps.close();
            compleated = true;
        } catch (SQLException se) {
            se.printStackTrace();
            return compleated;
        }
        return compleated;
    }
    
    
    public boolean getDonationItems(boolean tempTable) {
        boolean temp = false, compleated = false;
        String Table = "";
        if (tempTable) {
            Table = "temp";
            temp = true;
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM donationitems" + Table + " WHERE ownerid = ?");
            ps.setInt(1, getPlayer().getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                     Equip spItem = new Equip(rs.getInt("itemid"), (byte) 0, -1);
                     MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    if (!getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                        MapleInventoryManipulator.addFromDrop(c, ii.randomizeStats((Equip) spItem), "Donation Item");; 
                        removeDonationItem(temp, spItem.getItemId());
                    } else {
                        rs.close();
                        ps.close();
                        return false;
                    }
            }
            rs.close();
            ps.close();
            compleated = true;
        } catch (SQLException se) {
            se.printStackTrace();
            return compleated;
        }
        return compleated;
    }
	
    public boolean createPlayerNPC() {
            try {
                    if (this.getPlayer().getLevel() < 200) return false;
                    int type = 0;
                    if (this.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
                            type = PlayerNPCEngine.WARRIOR_ID;
                    } else if (this.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
                            type = PlayerNPCEngine.MAGICIAN_ID;
                    } else if (this.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
                            type = PlayerNPCEngine.BOWMAN_ID;
                    } else if (this.getPlayer().getJob().isA(MapleJob.THIEF)) {
                            type = PlayerNPCEngine.THIEF_ID;
                    } else {
                            return false;
                    }
                    try {
                            return PlayerNPCEngine.createGeneralNPC(type, this.getPlayer());
                    } catch (Throwable ex) {
                            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
                            return false;
                    }
            } catch (Exception ex) {
                    Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
                    return false;		
            }
    }
}
