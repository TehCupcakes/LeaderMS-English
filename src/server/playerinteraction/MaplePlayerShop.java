package server.PlayerInteraction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import server.MapleInventoryManipulator;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

public class MaplePlayerShop extends PlayerInteractionManager {

    private MapleCharacter owner;
    private int boughtnumber = 0;
    private List<String> bannedList = new ArrayList<String>();

    public MaplePlayerShop(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId % 10, desc, 3);
        this.owner = owner;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = items.get(item);
        if (pItem.getBundles() > 0) {
            synchronized (items) {
                IItem newItem = pItem.getItem().copy();
                newItem.setQuantity(quantity);
                if (c.getPlayer().getMeso() >= pItem.getPrice() * quantity) {
                    if (MapleInventoryManipulator.addFromDrop(c, newItem, "")) {
                        c.getPlayer().gainMeso(-pItem.getPrice() * quantity, false);
                        pItem.setBundles((short) (pItem.getBundles() - quantity));
                        owner.gainMeso(pItem.getPrice() * quantity, false);
                        if (pItem.getBundles() == 0) {
                            boughtnumber++;
                            if (boughtnumber == items.size()) {
                                removeAllVisitors(10, 1);
                                owner.getClient().getSession().write(MaplePacketCreator.shopErrorMessage(10, 1));
                                closeShop(false);
                            }
                        }
                    } else {
                        c.getPlayer().dropMessage(1, "Your inventory is full!");
                    }
                } else {
                    c.getPlayer().dropMessage(1, "You do not have enough mesos.");
                }
            }
            owner.getClient().getSession().write(MaplePacketCreator.shopItemUpdate(this));
        }
    }

    @Override
    public byte getShopType() {
        return IPlayerInteractionManager.PLAYER_SHOP;
    }

    @Override
    public void closeShop(boolean saveItems) {
        owner.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(owner));
        owner.getMap().removeMapObject(this);
        try {
            if (saveItems) {
                saveItems();
            }
        } catch (SQLException se) {
        }
        owner.setInteraction(null);
    }
    
    
    public void banPlayer(String name) {
        if (!bannedList.contains(name)) {
            bannedList.add(name);
        }
        for (int i = 0; i < 3; i++) {
            if (visitors[i].getName().equals(name)) {
                visitors[i].getClient().getSession().write(MaplePacketCreator.shopErrorMessage(5, 1));
                visitors[i].setInteraction(null);
                removeVisitor(visitors[i]);
            }
        }
    }

    public boolean isBanned(String name) {
        if (bannedList.contains(name)) {
            return true;
        }
        return false;
    }

    public MapleCharacter getMCOwner() {
        return owner;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }
}