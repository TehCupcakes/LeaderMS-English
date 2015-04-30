/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
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
package tools.packet;

import client.IEquip;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import client.IItem;
import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import handling.login.LoginServer;
import handling.world.PlayerCoolDownValueHolder;
import java.util.LinkedHashMap;
import server.MapleItemInformationProvider;
import server.PlayerInteraction.IPlayerInteractionManager;
import server.PlayerInteraction.MapleMiniGame;
import server.PlayerInteraction.MaplePlayerShop;
import server.movement.LifeMovementFragment;
import tools.HexTool;
import tools.KoreanDateUtil;
import tools.Pair;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PacketHelper {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MaplePacketCreator.class);

    private final static byte[] CHAR_INFO_MAGIC = new byte[] { (byte) 0xff, (byte) 0xc9, (byte) 0x9a, 0x3b };
    private final static byte[] ITEM_MAGIC = new byte[] { (byte) 0x80, 5 };
    public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();

    private final static long FT_UT_OFFSET = 116444592000000000L; // EDT
    
    public static long getKoreanTimestamp(long realTimestamp) {
        long time = (realTimestamp / 1000 / 60); // convert to minutes
        return ((time * 600000000) + FT_UT_OFFSET);
    }

    public static long getTime(long realTimestamp) {
        long time = (realTimestamp / 1000); // convert to seconds
        return ((time * 10000000) + FT_UT_OFFSET);
    }
    
    /**
     * Gets a packet with a list of characters.
     * 
     * @param c The MapleClient to load characters of.
     * @param serverId The ID of the server requested.
     * @return The character list packet.
     */
    public static MaplePacket getCharList(MapleClient c, int serverId) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
            mplew.write(0);
            List<MapleCharacter> chars = c.loadCharacters(serverId);
            mplew.write((byte) chars.size());
            for (MapleCharacter chr : chars) {
                    addCharEntry(mplew, chr);
            }
            mplew.writeInt(LoginServer.getInstance().getMaxCharacters());

            return mplew.getPacket();
    }
    
    public static void addCharWarp(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeLong(-1);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddyCapacity());
        mplew.writeInt(chr.getMeso());
        mplew.write(100); // equip slots
        mplew.write(100); // use slots
        mplew.write(100); // set-up slots
        mplew.write(100); // etc slots
        mplew.write(100); // cash slots
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> equippedC = iv.list();
        List<Item> equipped = new ArrayList<Item>(equippedC.size());
        for (IItem item : equippedC) {
            equipped.add((Item) item);
        }
        Collections.sort(equipped);

        for (Item item : equipped) {
            addItemInfo(mplew, item);
        }
        mplew.writeShort(0); // start of equip inventory

        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of use inventory

        iv = chr.getInventory(MapleInventoryType.USE);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of set-up inventory

        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of etc inventory

        iv = chr.getInventory(MapleInventoryType.ETC);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of cash inventory

        iv = chr.getInventory(MapleInventoryType.CASH);
        for (IItem item : iv.list()) {
            addItemInfo(mplew, item);
        }
        mplew.write(0); // start of skills

        Map<ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
        mplew.writeShort(skills.size());
        for (Map.Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
            mplew.writeInt(skill.getKey().getId());
            mplew.writeInt(skill.getValue().skillevel);
            if (skill.getKey().isFourthJob()) {
                mplew.writeInt(skill.getValue().masterlevel);
            }
        }
        List<PlayerCoolDownValueHolder> coolDowns = chr.getAllCooldowns();
        mplew.writeShort(coolDowns.size());
        for (PlayerCoolDownValueHolder cooling : coolDowns) {
            mplew.writeInt(cooling.skillId);
            int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
            mplew.writeShort(timeLeft / 1000);
        }
        coolDowns.clear();
    }

    /**
     * Adds character stats to an existing MaplePacketLittleEndianWriter.
     * 
     * @param mplew The MaplePacketLittleEndianWrite instance to write the stats to.
     * @param chr The character to add the stats of.
     */
    public static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId()); // character id

        mplew.writeAsciiString(chr.getName());
        for (int x = chr.getName().length(); x < 13; x++) { // fill to maximum name length
            mplew.write(0);
        }
        mplew.write(chr.getGender()); // gender (0 = male, 1 = female)

        mplew.write(chr.getSkinColor().getId()); // skin color

        mplew.writeInt(chr.getFace()); // face

        mplew.writeInt(chr.getHair()); // hair

        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.write(chr.getLevel()); // level

        mplew.writeShort(chr.getJob().getId()); // job

        mplew.writeShort(chr.getStr()); // str

        mplew.writeShort(chr.getDex()); // dex

        mplew.writeShort(chr.getInt()); // int

        mplew.writeShort(chr.getLuk()); // luk

        mplew.writeShort(chr.getHp()); // hp (?)

        mplew.writeShort(chr.getMaxHp()); // maxhp

        mplew.writeShort(chr.getMp()); // mp (?)

        mplew.writeShort(chr.getMaxMp()); // maxmp

        mplew.writeShort(chr.getRemainingAp()); // remaining ap

        mplew.writeShort(chr.getRemainingSp()); // remaining sp

        mplew.writeInt(chr.getExp()); // current exp

        mplew.writeShort(chr.getFame()); // fame

        mplew.writeInt(0);
        mplew.writeInt(chr.getMapId()); // current map id

        mplew.write(chr.getInitialSpawnpoint()); // spawnpoint

        mplew.writeInt(0);
    }
    
    /**
     * Adds an entry for a character to an existing
     * MaplePacketLittleEndianWriter.
     * 
     * @param mplew The MaplePacketLittleEndianWrite instance to write the stats to.
     * @param chr The character to add.
     */
    public static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        addCharStats(mplew, chr);
        addCharLook(mplew, chr, false);
        if (chr.getJob().isA(MapleJob.GM)) {
            mplew.write(0);
            return;
        }
        mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled)
        mplew.writeInt(chr.getRank()); // world rank
        mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
        mplew.writeInt(chr.getJobRank()); // job rank
        mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)
    }
    
    /**
     * Adds the aesthetic aspects of a character to an existing
     * MaplePacketLittleEndianWriter.
     * 
     * @param mplew The MaplePacketLittleEndianWrite instance to write the stats to.
     * @param chr The character to add the looks of.
     * @param mega Unknown
     */
    public static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor().getId()); // skin color

        mplew.writeInt(chr.getFace()); // face
        // variable length

        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair()); // hair

        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
        // Map<Integer, Integer> equipped = new LinkedHashMap<Integer,
        // Integer>();
        Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        for (IItem item : equip.list()) {
            byte pos = (byte) (item.getPosition() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getItemId());
            } else if (pos > 100 && pos != 111) { // don't ask. o.o
                pos -= 100;
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getItemId());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getItemId());
            }
        }
        for (Map.Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF); // end of visible itens
        // masked itens

        for (Map.Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        /*
         * for (IItem item : equip.list()) { byte pos = (byte)(item.getPosition() * -1); if (pos > 100) {
         * mplew.write(pos - 100); mplew.writeInt(item.getItemId()); } }
         */
        // ending markers
        mplew.write(0xFF);
        IItem cWeapon = equip.getItem((byte) -111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon.getItemId());
        } else {
            mplew.writeInt(0); // cashweapon

        }
        mplew.writeInt(0);
        mplew.writeLong(0);
    }
    
    public static MaplePacket itemMegaphone(String msg, boolean whisper, int channel, IItem item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);
        if (item == null) {
            mplew.write(0);
        } else {
            addItemInfo(mplew, item);
        }
        return mplew.getPacket();
    }
    
    /**
     * Adds info about an item to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to write to.
     * @param item The item to write info about.
     */
    public static final void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item) {
        addItemInfo(mplew, item, false, false);
    }
  
   /**
     * Adds item info to existing MaplePacketLittleEndianWriter.
     * 
     * @param mplew The MaplePacketLittleEndianWriter to write to.
     * @param item The item to add info about.
     * @param zeroPosition Is the position zero?
     * @param leaveOut Leave out the item if position is zero?
     */
    public static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition, boolean leaveOut) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean ring = false;
        IEquip equip = null;
        if (item.getType() == IItem.EQUIP) {
            equip = (IEquip) item;
            if (equip.getRingId() > -1) {
                ring = true;
            }
        }
        byte pos = item.getPosition();
        boolean masking = false;
        boolean equipped = false;
        if (zeroPosition) {
            if (!leaveOut) {
                mplew.write(0);
            }
        } else if (pos <= (byte) -1) {
            pos *= -1;
            if (pos > 100 || ring) {
                masking = true;
                mplew.write(0);
                mplew.write(pos - 100);
            } else {
                mplew.write(pos);
            }
            equipped = true;
        } else {
            mplew.write(item.getPosition());
        }
        if (item.getPetId() > -1) {
            mplew.write(3);
        } else {
            mplew.write(item.getType());
        }
        mplew.writeInt(item.getItemId());
        if (ring) {
            mplew.write(1);
            mplew.writeInt(equip.getRingId());
            mplew.writeInt(0);
        }
        if (item.getPetId() > -1) {
            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getPosition(), item.getPetId());
            String petname = pet.getName();
            mplew.write(1);
            mplew.writeInt(item.getPetId());
            mplew.writeInt(0);
            mplew.write(0);
            mplew.write(ITEM_MAGIC);
            mplew.write(HexTool.getByteArrayFromHexString("BB 46 E6 17 02"));
            if (petname.length() > 13)
                petname = petname.substring(0, 13);
            mplew.writeAsciiString(petname);
            for (int i = petname.length(); i < 13; i++)
                mplew.write(0);
            mplew.write(pet.getLevel());
            mplew.writeShort(pet.getCloseness());
            mplew.write(pet.getFullness());
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
            mplew.writeInt(0);
            return;
        }
        if (masking && !ring) {
            mplew.write(HexTool.getByteArrayFromHexString("01 41 B4 38 00 00 00 00 00 80 20 6F"));
            addExpirationTime(mplew, 0, false);
        } else if (ring) {
            mplew.writeLong(getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
        } else {
            mplew.writeShort(0);
            mplew.write(ITEM_MAGIC);
            addExpirationTime(mplew, 0, false);
        }
        if (item.getType() == IItem.EQUIP) {
            mplew.write(equip.getUpgradeSlots());
            mplew.write(equip.getLevel());
            mplew.writeShort(equip.getStr()); // str

            mplew.writeShort(equip.getDex()); // dex

            mplew.writeShort(equip.getInt()); // int

            mplew.writeShort(equip.getLuk()); // luk

            mplew.writeShort(equip.getHp()); // hp

            mplew.writeShort(equip.getMp()); // mp

            mplew.writeShort(equip.getWatk()); // watk

            mplew.writeShort(equip.getMatk()); // matk

            mplew.writeShort(equip.getWdef()); // wdef

            mplew.writeShort(equip.getMdef()); // mdef

            mplew.writeShort(equip.getAcc()); // accuracy

            mplew.writeShort(equip.getAvoid()); // avoid

            mplew.writeShort(equip.getHands()); // hands

            mplew.writeShort(equip.getSpeed()); // speed

            mplew.writeShort(equip.getJump()); // jump

            mplew.writeMapleAsciiString(equip.getOwner());

            // 0 normal; 1 locked
            mplew.write(equip.getLocked());

            if (ring && !equipped) {
                mplew.write(0);
            }

            if (!masking && !ring) {
                mplew.write(0);
                mplew.writeLong(0); // values of these don't seem to matter at all
            }
        } else {
            mplew.writeShort(item.getQuantity());
            mplew.writeMapleAsciiString(item.getOwner());
            mplew.writeShort(0); // this seems to end the item entry
            // but only if its not a THROWING STAR :))9 O.O!

            if (ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
                mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 54 00 00 34"));
            }
        }
    }
    
    /**
     * Adds expiration time info to an existing MaplePacketLittleEndianWriter.
     *
     * @param mplew The MaplePacketLittleEndianWriter to write to.
     * @param time The expiration time.
     * @param showexpirationtime Show the expiration time?
     */
    public static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time, boolean showexpirationtime) {
        if (time != 0) {
            mplew.writeInt(KoreanDateUtil.getItemTimestamp(time));
        } else {
            mplew.writeInt(400967355);
        }
        mplew.write(showexpirationtime ? 1 : 2);
    }
    
    public static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
            lew.write(moves.size());
            for (LifeMovementFragment move : moves) {
                    move.serialize(lew);
            }
    }

    private static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, IPlayerInteractionManager interaction) {
        if (interaction.getShopType() == 2) {
            mplew.write(4); //Shop
            mplew.writeInt(((MaplePlayerShop) interaction).getObjectId());
        } else if (interaction.getShopType() == 3) {
            mplew.write(2); //Match Card
            mplew.writeInt(((MapleMiniGame) interaction).getObjectId());
        } else if (interaction.getShopType() == 4) {
            mplew.write(1); //Omok
            mplew.writeInt(((MapleMiniGame) interaction).getObjectId());
        }
        mplew.writeMapleAsciiString(interaction.getDescription()); // desc
        if (interaction.getPassword() != null)
            mplew.write(1); //private
        else
            mplew.write(0); //public
        mplew.write(interaction.getItemType());
        mplew.write(1);
        if (interaction.getShopType() == 2) {
            mplew.write(interaction.getFreeSlot() > -1 ? 4 : 1);
            mplew.write(0);
        } else {
            mplew.write(interaction.getFreeSlot() > -1 ? 2 : 1); //4 slots, but only 2 can enter
            mplew.write(((MapleMiniGame) interaction).getStarted() ? 1 : 0);
        }
    }
}
