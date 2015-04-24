package handling.channel.handler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import client.IItem;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import handling.AbstractMaplePacketHandler;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStorage;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class StorageHandler extends AbstractMaplePacketHandler {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
    
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        byte mode = slea.readByte();
        final MapleStorage storage = c.getPlayer().getStorage();
        if (mode == 4) { // Take out.
            byte type = slea.readByte();
            byte slot = slea.readByte();
            slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
            IItem item = storage.takeOut(slot);
            if (item != null) {
                if (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                    MapleInventoryManipulator.addFromDrop(c, item, "Taken out from storage by " + c.getPlayer().getName(), false);
                } else {
                    storage.store(item);
                    c.getSession().write(MaplePacketCreator.serverNotice(1, "Your inventory is full"));
                }
                storage.sendTakenOut(c, ii.getInventoryType(item.getItemId()));
            } else {
                AutobanManager.getInstance().autoban(c, "Trying to take storage item that does not exist.");
                return;
            }
        } else if (mode == 5) { // Store.
            byte slot = (byte) slea.readShort();
            int itemId = slea.readInt();
            short quantity = slea.readShort();
            if (quantity < 1) {
                AutobanManager.getInstance().autoban(c, "Trying to store " + quantity + " of " + itemId);
                return;
            }
            if (storage.isFull()) {
                c.getSession().write(MaplePacketCreator.getStorageFull());
                return;
            }
            if (c.getPlayer().getMeso() < 100) {
                c.getSession().write(MaplePacketCreator.serverNotice(1, "You don't have enough mesos to store this item."));
            } else {
                MapleInventoryType type = ii.getInventoryType(itemId);
                IItem item = c.getPlayer().getInventory(type).getItem(slot).copy();
                if (item.getItemId() == itemId && (item.getQuantity() >= quantity || ii.isThrowingStar(itemId) || ii.isBullet(itemId))) {
                    if (ii.isThrowingStar(itemId) || ii.isBullet(itemId)) {
                        quantity = item.getQuantity();
                    }
                    c.getPlayer().gainMeso(-100, false, true, false);
                    MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
                    item.setQuantity(quantity);
                    storage.store(item);
                    FilePrinter.printBanco(c.getPlayer().getName() + ".txt", "Deposit of " +  item + "\r\nOn day: " + sdf.format(Calendar.getInstance().getTime()) + " at " + sdf2.format(Calendar.getInstance().getTime()) + ".");
                } else {
                    AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to store non-matching itemid (" + itemId + "/" + item.getItemId() + ") or quantity not in posession (" + quantity + "/" + item.getQuantity() + ")");
                    return;
                }
            }
            storage.sendStored(c, ii.getInventoryType(itemId));
        } else if (mode == 6) {
            c.getPlayer().dropMessage(1, "Sorry, this storage is currently unavailable.");
        } else if (mode == 7) { // Meso.
            int meso = slea.readInt();
            int storageMesos = storage.getMeso();
            int playerMesos = c.getPlayer().getMeso();
            if ((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)) {
                if (meso < 0 && (storageMesos - meso) < 0) { // Storing with overflow.
                    meso = -(Integer.MAX_VALUE - storageMesos);
                    if ((-meso) > playerMesos) { // should never happen just a failsafe.
                        throw new RuntimeException("everything sucks");
                    }
                } else if (meso > 0 && (playerMesos + meso) < 0) { // Taking out with overflow.
                    meso = (Integer.MAX_VALUE - playerMesos);
                    if ((meso) > storageMesos) { // should never happen just a failsafe.
                        throw new RuntimeException("everything sucks");
                    }
                }
                storage.setMeso(storageMesos - meso);
                c.getPlayer().gainMeso(meso, false, true, false);
                FilePrinter.printBanco(c.getPlayer().getName() + ".txt", "Deposited (-) / Removed (+) : " +  meso + "\r\nTotal before the deposit: " + storageMesos + "\r\nOn day: " + sdf.format(Calendar.getInstance().getTime()) + " at " + sdf2.format(Calendar.getInstance().getTime()) + ".");
            } else {
                AutobanManager.getInstance().addPoints(c, 1000, 0, "Trying to store or withdraw more mesos than available (" + meso + "/" + storage.getMeso() + "/" + c.getPlayer().getMeso() + ")");
                return;
            }
            storage.sendMeso(c);
        } else if (mode == 8) { // Close.
            storage.close();
        }
    }
}