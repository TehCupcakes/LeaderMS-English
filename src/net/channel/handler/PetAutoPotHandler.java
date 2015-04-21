package net.channel.handler;

import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PetAutoPotHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte type = slea.readByte();
        slea.skip(12);
        byte slot = slea.readByte();
        slea.readByte();
        int itemId = slea.readInt();
        IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (toUse != null && toUse.getQuantity() > 0) {
            if (toUse.getItemId() != itemId) {
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            ii.getItemEffect(toUse.getItemId()).applyTo(c.getPlayer());
        }
    }
}