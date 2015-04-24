package handling.channel.handler;

import java.util.Random;
import client.ExpTable;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.packet.PetPacket;
import tools.data.input.SeekableLittleEndianAccessor;

public class PetFoodHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getNoPets() == 0) {
            return;
        }
        int slot = 0;
        MaplePet[] pets = c.getPlayer().getPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getFullness() < 100) {
                    slot = i;
                }
            } else {
                break;
            }
        }
        MaplePet pet = c.getPlayer().getPet(slot);
        slea.readInt();
        slea.readShort();
        int itemId = slea.readInt();
        if (c.getPlayer().haveItem(itemId, 1, false, true)) {
            boolean gainCloseness = new Random().nextInt(101) <= 50;
            int newFullness = pet.getFullness() + 30;
            if (pet.getFullness() < 100) {
                if (newFullness > 100) {
                    newFullness = 100;
                }
                pet.setFullness(newFullness);
                if (gainCloseness && pet.getCloseness() < 30000) {
                    int newCloseness = pet.getCloseness() + (1 * c.getChannelServer().getPetExpRate());
                    if (newCloseness > 30000) {
                        newCloseness = 30000;
                    }
                    pet.setCloseness(newCloseness);
                    if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                        pet.setLevel(pet.getLevel() + 1);
                        c.getSession().write(PetPacket.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
                        c.getPlayer().getMap().broadcastMessage(PetPacket.showPetLevelUp(c.getPlayer(), c.getPlayer().getPetIndex(pet)));
                    }
                }
            } else {
                if (gainCloseness) {
                    int newCloseness = pet.getCloseness() - (1 * c.getChannelServer().getPetExpRate());
                    if (newCloseness < 0) {
                        newCloseness = 0;
                    }
                    pet.setCloseness(newCloseness);
                    if (newCloseness < ExpTable.getClosenessNeededForLevel(pet.getLevel())) {
                        pet.setLevel(pet.getLevel() - 1);
                    }
                }
            }
            c.getSession().write(PetPacket.updatePet(pet, true));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(c.getPlayer().getId(), (byte) 1, slot, true, true), true);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
        }
    }
}