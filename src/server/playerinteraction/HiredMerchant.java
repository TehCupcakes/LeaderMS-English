package server.PlayerInteraction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import database.DatabaseConnection;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.TimerManager;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import tools.packet.MaplePacketCreator;

/**
 *
 * @author XoticStory
 */
public class HiredMerchant extends PlayerInteractionManager {

    private boolean open;
    public ScheduledFuture<?> schedule = null;
    private MapleMap map;
    private int itemId;
    private String ownerName = "";

    public HiredMerchant(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId % 10, desc, 3);
        this.itemId = itemId;
        this.map = owner.getMap();
        this.schedule = TimerManager.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                HiredMerchant.this.closeShop(true);
            }
        }, 1000 * 60 * 60 * 24);
    }

    public byte getShopType() {
        return IPlayerInteractionManager.HIRED_MERCHANT;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = items.get(item);
        if (pItem.getBundles() > 0) {
            synchronized (items) {
                IItem newItem = pItem.getItem().copy();
                newItem.setQuantity((short) (newItem.getQuantity() * quantity));
            if (quantity < 1 || pItem.getBundles() < 1 || newItem.getQuantity() > pItem.getBundles() || !pItem.isExist()) {
                return;
            } else if (newItem.getType() == 1 && newItem.getQuantity() > 1) {
                return;
            } else if (!pItem.isExist()) {
                return;
            }
                if (c.getPlayer().getMeso() >= pItem.getPrice() * quantity) {
                    if (quantity > 0 && pItem.getBundles() >= quantity && pItem.getBundles() > 0) {
                        if (MapleInventoryManipulator.addFromDrop(c, newItem, "")) {
                            Connection con = DatabaseConnection.getConnection();
                            try {
                                PreparedStatement ps = con.prepareStatement("UPDATE characters SET MerchantMesos = MerchantMesos + " + pItem.getPrice() * quantity + " WHERE id = ?");
                                ps.setInt(1, getOwnerId());
                                ps.executeUpdate();
                                ps.close();
                            } catch (SQLException se) {
                                se.printStackTrace();
                            }
                            c.getPlayer().gainMeso(-pItem.getPrice() * quantity, false);
                            pItem.setBundles((short) (pItem.getBundles() - quantity));
                            tempItemsUpdate();
                        } else {
                            c.getPlayer().dropMessage(1, "èƒŒåŒ…å·²æ»¡");
                            c.getSession().write(MaplePacketCreator.enableActions());
                        }
                    } else {
                        AutobanManager.getInstance().autoban(c.getPlayer().getClient(), "XSource| Attempted to Merchant dupe.");
                    }
                } else {
                    c.getPlayer().dropMessage(1, "é‡‘å¸ä¸è¶³");
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
            }
        }
    }
    
    @Override
    public void closeShop(boolean saveItems) {
        map.removeMapObject(this);
        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(getOwnerId()));
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?");
            ps.setInt(1, getOwnerId());
            ps.executeUpdate();
            ps.close();
            tempItems(false);
            if (saveItems) {
                saveItems();
            }
        } catch (SQLException se) {
        }
        schedule.cancel(false);
    }
    


    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean set) {
        this.open = set;
    }

    public MapleMap getMap() {
        return map;
    }

    public int getItemId() {
        return itemId;
    }
    
    public String getOwner() {
        return ownerName;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnHiredMerchant(this));
    }
}