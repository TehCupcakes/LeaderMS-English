package net.channel.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.MapleStat;
import client.PetDataFactory;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class SpawnPetHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        byte slot = slea.readByte();
        slea.readByte();
        boolean lead = slea.readByte() == 1;
        MapleCharacter player = c.getPlayer();
        IItem item = player.getInventory(MapleInventoryType.CASH).getItem(slot);
        if (item.getItemId() == 5000028 || item.getItemId() == 5000047) {
            boolean done = false;
            int petno;
            int[] pet;
            int[] dragon = {5000029, 5000030, 5000031, 5000032, 5000033};
            int[] robot = {5000048, 5000049, 5000050, 5000051, 5000052, 5000053};
            pet = item.getItemId() == 5000028 ? dragon : robot;
            Random egg = new Random();
            for (int i = 0; i < pet.length && !done; i++) {
                petno = egg.nextInt(pet.length);
                if (!player.haveItem(pet[petno], 1, true, true)) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, item.getPosition(), (short) 1, true, false);
                    MapleInventoryManipulator.addById(c, pet[petno], (short) 1, "Apperantly, Your Dragon egg hatched!.", null, MaplePet.createPet(pet[petno]));
                    done = true;
                }
            }
            if (!done) {
                player.dropMessage(1, "You currently have all the dragons or robots.");
                return;
            }
        }
        MaplePet pet = MaplePet.loadFromDb(player.getInventory(MapleInventoryType.CASH).getItem(slot).getItemId(), slot, player.getInventory(MapleInventoryType.CASH).getItem(slot).getPetId());
        if (pet == null) {
            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, item.getItemId(), (short) item.getQuantity(), false, false);
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (player.getPetIndex(pet) != -1) {
            player.unequipPet(pet, true);
        } else {
            if (player.getSkillLevel(SkillFactory.getSkill(8)) == 0 && player.getPet(0) != null) {
                player.unequipPet(player.getPet(0), false);
            }
            if (lead) {
                player.shiftPetsRight();
            }
            Point pos = player.getPosition();
            pos.y -= 12;
            pet.setPos(pos);
            pet.setFh(player.getMap().getFootholds().findBelow(pet.getPos()).getId());
            pet.setStance(0);
            player.addPet(pet);
            player.getMap().broadcastMessage(player, MaplePacketCreator.showPet(player, pet, false), true);
            int uniqueid = pet.getUniqueId();
            List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
            stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(uniqueid)));
            c.getSession().write(MaplePacketCreator.petStatUpdate(player));
            c.getSession().write(MaplePacketCreator.enableActions());
            int hunger = PetDataFactory.getHunger(pet.getItemId());
            player.startFullnessSchedule(hunger, pet, player.getPetIndex(pet));
        }
    }
}