package scripting;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.Equip;
import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MaplePet;
import client.MapleQuestStatus;
import config.configuration.Configuration;
import net.MaplePacket;
import net.channel.ChannelServer;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.guild.MapleGuild;
import server.MaplePortal;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleReactor;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;

public class AbstractPlayerInteraction {

	private MapleClient c;

	public AbstractPlayerInteraction(MapleClient c) {
		this.c = c;
	}

	protected MapleClient getClient() {
		return c;
	}

	public MapleCharacter getPlayer() {
		return c.getPlayer();
	}

	public void warp(int map) {
		MapleMap target = getWarpMap(map);
		MaplePortal portal = target.getPortal(0);
		c.getPlayer().changeMap(target, portal);
	}

	public void warp(int map, int portal) {
		MapleMap target = getWarpMap(map);
		c.getPlayer().changeMap(target, target.getPortal(portal));
	}

	public void warp(int map, String portal) {
		MapleMap target = getWarpMap(map);
		c.getPlayer().changeMap(target, target.getPortal(portal));
	}

	private MapleMap getWarpMap(int map) {
        MapleMap target;
        if (getPlayer().getEventInstance() == null) {
            target = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(map);
        } else {
            target = getPlayer().getEventInstance().getMapInstance(map);
        }
        return target;
    }

	public MapleMap getMap(int map) {
		return getWarpMap(map);
	}
        
         public final MapleMap getMap() {
           return c.getPlayer().getMap();
        }

	public boolean haveItem(int itemid) {
		return haveItem(itemid, 1);
	}

	public boolean haveItem(int itemid, int quantity) {
		return haveItem(itemid, quantity, false, true);
	}

	public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
		return c.getPlayer().haveItem(itemid, quantity, checkEquipped, greaterOrEquals);
	}
        
       public void giveItemBuff(int itemID) {
           c.getPlayer().giveItemBuff(itemID);
       }

	public boolean canHold(int itemid) {
		MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
		MapleInventory iv = c.getPlayer().getInventory(type);

		return iv.getNextFreeSlot() > -1;
	}

     public MapleQuestStatus.Status getQuestStatus(int id) {
        return c.getPlayer().getQuest(MapleQuest.getInstance(id)).getStatus();
    }

     public void gainItem(int id, short quantity) {
		gainItem(id, quantity, false);
	}
        
       public void gainItem(int id) {
        gainItem(id, (short) 1, false);
      }
      

	/**
	 * Gives item with the specified id or takes it if the quantity is negative. Note that this does NOT take items from the equipped inventory. randomStats for generating random stats on the generated equip.
	 * @param id
	 * @param quantity
	 * @param randomStats
	 */
	public void gainItem(int id, short quantity, boolean randomStats) {
		if (quantity >= 0) {
			boolean space = false;
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			IItem item = ii.getEquipById(id);
			MapleInventoryType type = ii.getInventoryType(id);
			StringBuilder logInfo = new StringBuilder(c.getPlayer().getName());
			logInfo.append(" received ");
			logInfo.append(quantity);
			logInfo.append(" from a scripted PlayerInteraction (");
			logInfo.append(this.toString());
			logInfo.append(")");
			if (!MapleInventoryManipulator.checkSpace(c, id, quantity, "")) {
				MapleInventoryType invtype = ii.getInventoryType(id);
				c.getSession().write(MaplePacketCreator.serverNotice(1, "Your inventory is full. Please remove an item from your " + type.name() + " inventory."));
				return;
			}
			if (type.equals(MapleInventoryType.EQUIP) && !ii.isThrowingStar(item.getItemId()) && !ii.isBullet(item.getItemId())) {
				if (randomStats) {
					MapleInventoryManipulator.addFromDrop(c, ii.randomizeStats((Equip) item), logInfo.toString(), false);
				} else {
					MapleInventoryManipulator.addFromDrop(c, (Equip) item, logInfo.toString(), false);
				}
			} else {
				MapleInventoryManipulator.addById(c, id, quantity, logInfo.toString(), null, -1);
			}
		} else {
			MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
		}
		c.getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
	}

	public void changeMusic(String songName) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
	}
        
    public void setPlayerVariable(String name, String value) {
        c.getPlayer().setPlayerVariable(name, value);
    }
    
    public String getPlayerVariable(String name) {
        return c.getPlayer().getPlayerVariable(name);
    }
    
    public void deletePlayerVariable(String name) {
        c.getPlayer().deletePlayerVariable(name);
    }

	public void playerMessage(String message) {
		playerMessage(5, message);
	}

	public void mapMessage(String message) {
		mapMessage(5, message);
	}

	public void guildMessage(String message) {
		guildMessage(5, message);
	}

	public void playerMessage(int type, String message) {
		c.getSession().write(MaplePacketCreator.serverNotice(type, message));
	}

	public void mapMessage(int type, String message) {
		getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
	}
        
        public void mapClock(int time) {
		//getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
                getPlayer().getMap().broadcastMessage(MaplePacketCreator.getClock(time));
	}

	public void guildMessage(int type, String message) {
		MapleGuild guild = getGuild();
		if (guild != null) {
			guild.guildMessage(MaplePacketCreator.serverNotice(type, message));
		//guild.broadcast(MaplePacketCreator.serverNotice(type, message));
		}
	}

	public MapleGuild getGuild() {
		try {
			return c.getChannelServer().getWorldInterface().getGuild(getPlayer().getGuildId(), null);
		} catch (RemoteException ex) {
			Logger.getLogger(AbstractPlayerInteraction.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public MapleParty getParty() {
		return (c.getPlayer().getParty());
	}

	public boolean isLeader() {
		return (getParty().getLeader().equals(new MaplePartyCharacter(c.getPlayer())));
	}
	//PQ methods: give items/exp to all party members

	public void givePartyItems(int id, short quantity, List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			MapleClient cl = chr.getClient();
			if (quantity >= 0) {
				StringBuilder logInfo = new StringBuilder(cl.getPlayer().getName());
				logInfo.append(" received ");
				logInfo.append(quantity);
				logInfo.append(" from event ");
				logInfo.append(chr.getEventInstance().getName());
				MapleInventoryManipulator.addById(cl, id, quantity, logInfo.toString(), null, -1);
			} else {
				MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, true, false);
			}
			cl.getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
		}
	}
	//PQ gain EXP: Multiplied by channel rate here to allow global values to be input direct into NPCs

	public void givePartyExp(int amount, List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			chr.gainExp(amount * c.getChannelServer().getExpRate(), true, true);
		}
	}
        
        public void givePartyQPoints(int amount, List<MapleCharacter> party) {
		for (MapleCharacter chr : party) {
			chr.gainpqPoints(amount);
                        chr.dropMessage("["+Configuration.Server_Name+" Quest] Completed quest successfully. You now have (" + chr.getpqPoints() + ") Q.Points!");
		}
	}
	//remove all items of type from party
	//combination of haveItem and gainItem

	public void removeFromParty(int id, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            MapleClient cl = chr.getClient();
            MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(id);
            MapleInventory iv = cl.getPlayer().getInventory(type);
            int possesed = iv.countById(id);
            if (possesed > 0) {
                MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possesed, true, false);
                cl.announce(MaplePacketCreator.getShowItemGain(id, (short) -possesed, true));
            }
        }
    }

	public void removeAll(int id) {
		removeAll(id, c);
	}

	 public void removeAll(int id, MapleClient cl) {
        int possessed = cl.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(id)).countById(id);
        if (possessed > 0) {
            MapleInventoryManipulator.removeById(cl, MapleItemInformationProvider.getInstance().getInventoryType(id), id, possessed, true, false);
            cl.announce(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
        }
    }
	
	public int countMonster() {
		MapleMap map = c.getPlayer().getMap();
		double range = Double.POSITIVE_INFINITY;
		List<MapleMapObject> monsters = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays
				.asList(MapleMapObjectType.MONSTER));
		for (MapleMapObject monstermo : monsters) {
				MapleMonster monster = (MapleMonster) monstermo;
		}
		return monsters.size();
	}

	public int countReactor() {
		MapleMap map = c.getPlayer().getMap();
		double range = Double.POSITIVE_INFINITY;
		List<MapleMapObject> reactors = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays
				.asList(MapleMapObjectType.REACTOR));
		for (MapleMapObject reactormo : reactors) {
				MapleReactor reactor = (MapleReactor) reactormo;
		}
		return reactors.size();
	}

	public int countReactor(byte st) {
		MapleMap map = c.getPlayer().getMap();
		double range = Double.POSITIVE_INFINITY;
		List<MapleMapObject>  reactorz = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays
				.asList(MapleMapObjectType.REACTOR));
		ArrayList<MapleMapObject> reactors = new ArrayList<MapleMapObject>();

		for (MapleMapObject reactormo : reactorz) {
				MapleReactor reactor = (MapleReactor) reactormo;
				if (reactor.getState() == st) {
					reactors.add(reactor);
				}
		}
		return reactors.size();
	}

	public void gainCloseness(int closeness, int index) {
        MaplePet pet = getPlayer().getPet(index);
        if (pet != null) {
            pet.setCloseness(pet.getCloseness() + closeness);
            getClient().getSession().write(MaplePacketCreator.updatePet(pet, true));
        }
    }

    public void gainClosenessAll(int closeness) {
        for (MaplePet pet : getPlayer().getPets()) {
            if (pet != null) {
                pet.setCloseness(pet.getCloseness() + closeness);
                getClient().getSession().write(MaplePacketCreator.updatePet(pet, true));
            }
        }
    }
    

	public int getMapId() {
		return c.getPlayer().getMap().getId();
	}

	public int getPlayerCount(int mapid) {
		return c.getChannelServer().getMapFactory().getMap(mapid).getCharacters().size();
	}

	public int getCurrentPartyId(int mapid) {
		return getMap(mapid).getCurrentPartyId();
	}

	public void showInstruction(String msg, int width, int height) {
		c.getSession().write(MaplePacketCreator.sendHint(msg, width, height));
	}
        
	public void worldMessage(int type, String message) {
		net.MaplePacket packet = MaplePacketCreator.serverNotice(type, message);
		MapleCharacter chr = c.getPlayer();
		try {
			ChannelServer.getInstance(chr.getClient().getChannel()).getWorldInterface().broadcastMessage(chr.getName(), packet.getBytes());
		} catch (RemoteException e) {
			chr.getClient().getChannelServer().reconnectWorld();
		}
	}
	
	public int getZakumLevel() {
		return c.getPlayer().getZakumLevel();
	}

	public void setZakumLevel(int level) {
		c.getPlayer().setZakumLevel(level);
	}

	public void addZakumLevel() {
		c.getPlayer().addZakumLevel();
	}

	public void subtractZakumLevel() {
		c.getPlayer().subtractZakumLevel();
	}
	
	public void gainForgedItem(int id, int max) {
		short quantity = 1;
		StringBuilder logInfo = new StringBuilder(c.getPlayer().getName());
		logInfo.append(" received ");
		logInfo.append(quantity);
		logInfo.append(" from a forged scripted PlayerInteraction (");
		logInfo.append(this.toString());
		logInfo.append(")");
		MapleInventoryManipulator.addSuperFromNPC(c, id, logInfo.toString(), max);
		c.getSession().write(MaplePacketCreator.getShowItemGain(id,quantity, true));
	}
	/**
	 * This function autorandomizes equips.
	 * @param itemId
	 */
	public void gainEquip(int itemId, int quantity) {
		if (MapleItemInformationProvider.getInstance().isEquip(itemId)) {
			Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId);
			MapleItemInformationProvider.getInstance().randomizeStats(eq);
			MapleInventoryManipulator.addFromDrop(this.getClient(), eq, "", false);
			this.getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, (short)1, true));
		} else {
			Item item = new Item(itemId, (byte) -1, (short) quantity);
			MapleInventoryManipulator.addFromDrop(this.getClient(), item, "", false);
			this.getClient().getSession().write(MaplePacketCreator.getShowItemGain(itemId, (short) quantity, true));
		}
	}
	
	public void gainEquip(int itemId) {
		gainEquip(itemId, 1);
	}
}
