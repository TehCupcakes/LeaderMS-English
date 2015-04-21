package server.PlayerInteraction;

import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import net.MaplePacket;

public interface IPlayerInteractionManager {

    public final byte HIRED_MERCHANT = 1;
    public final byte PLAYER_SHOP = 2;
    public final byte MATCH_CARD = 3;
    public final byte OMOK = 4;

    public void broadcast(MaplePacket packet, boolean toOwner);

    public void addVisitor(MapleCharacter visitor);

    public void removeVisitor(MapleCharacter visitor);

    public int getVisitorSlot(MapleCharacter visitor);

    public void removeAllVisitors(int error, int type);

    public void buy(MapleClient c, int item, short quantity);

    public void closeShop(boolean saveItems);

    public String getOwnerName();

    public int getOwnerId();

    public String getDescription();

    public MapleCharacter[] getVisitors();

    public List<MaplePlayerShopItem> getItems();

    public void addItem(MaplePlayerShopItem item);

    public boolean removeItem(int item);

    public void removeFromSlot(int slot);

    public int getFreeSlot();

    public byte getItemType();

    public boolean isOwner(MapleCharacter chr);

    public byte getShopType();
}