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

package server;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.TimeZone;

import client.Equip;
import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleWeaponType;
import client.SkillFactory;
import java.util.WeakHashMap;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapleItemInformationProvider {
    static Logger log = LoggerFactory.getLogger(MapleItemInformationProvider.class);
    private static MapleItemInformationProvider instance = null;
    protected MapleDataProvider itemData;
    protected MapleDataProvider equipData;
    protected MapleDataProvider stringData;
    protected MapleData cashStringData;
    protected MapleData consumeStringData;
    protected MapleData eqpStringData;
    protected MapleData etcStringData;
    protected MapleData insStringData;
    protected MapleData petStringData;
    protected Map<Integer, MapleInventoryType> inventoryTypeCache = new WeakHashMap<Integer, MapleInventoryType>();
    protected Map<Integer, Short> slotMaxCache = new WeakHashMap<Integer, Short>();
    protected Map<Integer, MapleStatEffect> itemEffects = new WeakHashMap<Integer, MapleStatEffect>();
    protected Map<Integer, Map<String, Integer>> equipStatsCache = new WeakHashMap<Integer, Map<String, Integer>>();
    protected Map<Integer, Equip> equipCache = new WeakHashMap<Integer, Equip>();
    protected Map<Integer, Double> priceCache = new WeakHashMap<Integer, Double>();
    protected Map<Integer, Integer> wholePriceCache = new WeakHashMap<Integer, Integer>();
    protected Map<Integer, Integer> projectileWatkCache = new WeakHashMap<Integer, Integer>();
    protected Map<Integer, String> nameCache = new WeakHashMap<Integer, String>();
    protected Map<Integer, String> descCache = new WeakHashMap<Integer, String>();
    protected Map<Integer, String> msgCache = new WeakHashMap<Integer, String>();
    protected Map<Integer, Boolean> dropRestrictionCache = new WeakHashMap<Integer, Boolean>();
    protected Map<Integer, Boolean> pickupRestrictionCache = new WeakHashMap<Integer, Boolean>();
    protected Map<Integer, List<SummonEntry>> summonEntryCache = new WeakHashMap<Integer, List<SummonEntry>>();
    protected List<Pair<Integer, String>> itemNameCache = new ArrayList<Pair<Integer, String>>();
    protected Map<Integer, Map<String, String>> getExpCardTimes = new WeakHashMap<Integer, Map<String, String>>();
    protected Map<Integer, Integer> getMesoCache = new WeakHashMap<Integer, Integer>();
    protected Map<Integer, Boolean> consumeOnPickupCache = new WeakHashMap<>();
    private static Random rand = new Random();

    /** Creates a new instance of MapleItemInformationProvider */
    protected MapleItemInformationProvider() {
        itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
        equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
        stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
        cashStringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Cash.img");
        consumeStringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Consume.img");
        eqpStringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Eqp.img");
        etcStringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Etc.img");
        insStringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Ins.img");
        petStringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz")).getData("Pet.img");
    }

    public static MapleItemInformationProvider getInstance() {
        if (instance == null) {
            instance = new MapleItemInformationProvider();
        }
        return instance;
    }

    /* returns the inventory type for the specified item id */
    public MapleInventoryType getInventoryType(int itemId) {
        if (inventoryTypeCache.containsKey(itemId)) {
            return inventoryTypeCache.get(itemId);
        }
        MapleInventoryType ret;
        String idStr = "0" + String.valueOf(itemId);
		if (idStr.length() <= 4) {
			throw new RuntimeException("ItemID is wrong! ITEMID: " + idStr);
		}
        // first look in items...
        MapleDataDirectoryEntry root = itemData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            // we should have .img files here beginning with the first 4 IID
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
                    ret = MapleInventoryType.getByWZName(topDir.getName());
                    inventoryTypeCache.put(itemId, ret);
                    return ret;
                } else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
                    ret = MapleInventoryType.getByWZName(topDir.getName());
                    inventoryTypeCache.put(itemId, ret);
                    return ret;
                }
            }
        }
        // not found? maybe its equip...
        root = equipData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr + ".img")) {
                    ret = MapleInventoryType.EQUIP;
                    inventoryTypeCache.put(itemId, ret);
                    return ret;
                }
            }
        }
        ret = MapleInventoryType.UNDEFINED;
        inventoryTypeCache.put(itemId, ret);
        return ret;
    }
    
     public Equip expireItem(Equip equip, long period) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        equip.setExpiration(period);
        return equip;
    }

    public List<Pair<Integer, String>> getAllItems() {
        if (!itemNameCache.isEmpty()) {
            return itemNameCache;
        }
        List<Pair<Integer, String>> itemPairs = new ArrayList<Pair<Integer, String>>();
        MapleData itemsData;

        itemsData = stringData.getData("Cash.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            int itemId = Integer.parseInt(itemFolder.getName());
            String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
            itemPairs.add(new Pair<Integer, String>(itemId, itemName));
        }

        itemsData = stringData.getData("Consume.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            int itemId = Integer.parseInt(itemFolder.getName());
            String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
            itemPairs.add(new Pair<Integer, String>(itemId, itemName));
        }

        itemsData = stringData.getData("Eqp.img").getChildByPath("Eqp");
        for (MapleData eqpType : itemsData.getChildren()) {
            for (MapleData itemFolder : eqpType.getChildren()) {
                int itemId = Integer.parseInt(itemFolder.getName());
                String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
                itemPairs.add(new Pair<Integer, String>(itemId, itemName));
            }
        }

        itemsData = stringData.getData("Etc.img").getChildByPath("Etc");
        for (MapleData itemFolder : itemsData.getChildren()) {
            int itemId = Integer.parseInt(itemFolder.getName());
            String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
            itemPairs.add(new Pair<Integer, String>(itemId, itemName));
        }

        itemsData = stringData.getData("Ins.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            int itemId = Integer.parseInt(itemFolder.getName());
            String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
            itemPairs.add(new Pair<Integer, String>(itemId, itemName));
        }

        itemsData = stringData.getData("Pet.img");
        for (MapleData itemFolder : itemsData.getChildren()) {
            int itemId = Integer.parseInt(itemFolder.getName());
            String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
            itemPairs.add(new Pair<Integer, String>(itemId, itemName));
        }
        return itemPairs;
    }

    
    
    
    protected MapleData getStringData(int itemId) {
        String cat = "null";
        MapleData theData;
        if (itemId >= 5010000) {
            theData = cashStringData;
        } else if (itemId >= 2000000 && itemId < 3000000) {
            theData = consumeStringData;
        } else if (itemId >= 1010000 && itemId < 1040000 || itemId >= 1122000 && itemId < 1123000) {
            theData = eqpStringData;
            cat = "Accessory";
        } else if (itemId >= 1000000 && itemId < 1010000) {
            theData = eqpStringData;
            cat = "Cap";
        } else if (itemId >= 1102000 && itemId < 1103000) {
            theData = eqpStringData;
            cat = "Cape";
        } else if (itemId >= 1040000 && itemId < 1050000) {
            theData = eqpStringData;
            cat = "Coat";
        } else if (itemId >= 20000 && itemId < 22000) {
            theData = eqpStringData;
            cat = "Face";
        } else if (itemId >= 1080000 && itemId < 1090000) {
            theData = eqpStringData;
            cat = "Glove";
        } else if (itemId >= 30000 && itemId < 32000) {
            theData = eqpStringData;
            cat = "Hair";
        } else if (itemId >= 1050000 && itemId < 1060000) {
            theData = eqpStringData;
            cat = "Longcoat";
        } else if (itemId >= 1060000 && itemId < 1070000) {
            theData = eqpStringData;
            cat = "Pants";
        } else if (itemId >= 1802000 && itemId < 1810000) {
            theData = eqpStringData;
            cat = "PetEquip";
        } else if (itemId >= 1112000 && itemId < 1120000) {
            theData = eqpStringData;
            cat = "Ring";
        } else if (itemId >= 1092000 && itemId < 1100000) {
            theData = eqpStringData;
            cat = "Shield";
        } else if (itemId >= 1070000 && itemId < 1080000) {
            theData = eqpStringData;
            cat = "Shoes";
        } else if (itemId >= 1900000 && itemId < 2000000) {
            theData = eqpStringData;
            cat = "Taming";
        } else if (itemId >= 1300000 && itemId < 1800000) {
            theData = eqpStringData;
            cat = "Weapon";
        } else if (itemId >= 4000000 && itemId < 5000000) {
            theData = etcStringData;
        } else if (itemId >= 3000000 && itemId < 4000000) {
            theData = insStringData;
        } else if (itemId >= 5000000 && itemId < 5010000) {
            theData = petStringData;
        } else {
            return null;
        }
        if (cat.equalsIgnoreCase("null")) {
            return theData.getChildByPath(String.valueOf(itemId));
        } else {
            return theData.getChildByPath(cat + "/" + itemId);
        }
    }

    protected MapleData getItemData(int itemId) {
        MapleData ret = null;
        String idStr = "0" + String.valueOf(itemId);
        MapleDataDirectoryEntry root = itemData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            // we should have .img files here beginning with the first 4 IID
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
                    ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
                    if (ret == null) {
                        return null;
                    }
                    ret = ret.getChildByPath(idStr);
                    return ret;
                } else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
                    return itemData.getData(topDir.getName() + "/" + iFile.getName());
                }
            }
        }
        root = equipData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr + ".img")) {
                    return equipData.getData(topDir.getName() + "/" + iFile.getName());
                }
            }
        }
        return ret;
    }
    
        public boolean isConsumeOnPickup(int itemId) {
        if (consumeOnPickupCache.containsKey(itemId)) {
            return consumeOnPickupCache.get(itemId);
        }
        MapleData data = getItemData(itemId);
        boolean consume = MapleDataTool.getIntConvert("spec/consumeOnPickup", data, 0) == 1 || MapleDataTool.getIntConvert("specEx/consumeOnPickup", data, 0) == 1;
        consumeOnPickupCache.put(itemId, consume);
        return consume;
    }

     public short getSlotMax(MapleClient c, int itemId) {
        if (slotMaxCache.containsKey(itemId)) {
            return slotMaxCache.get(itemId);
        }
        short ret = 0;
        MapleData item = getItemData(itemId);
        if (item != null) {
            MapleData smEntry = item.getChildByPath("info/slotMax");
            if (smEntry == null) {
                if (getInventoryType(itemId).getType() == MapleInventoryType.EQUIP.getType()) {
                    ret = 1;
                } else {
                    ret = 100;
                }
            } else {
                ret = (short) MapleDataTool.getInt(smEntry);
                if (isThrowingStar(itemId)) {
                    ret += c.getPlayer().getSkillLevel(SkillFactory.getSkill(4100000)) * 10;
                } else {
                    ret += c.getPlayer().getSkillLevel(SkillFactory.getSkill(5200000)) * 10;
                }
            }
        }
        if (!isThrowingStar(itemId) && !isBullet(itemId)) {
            slotMaxCache.put(itemId, ret);
        }
        return ret;
    }

    public short getSlotMax(int itemId) {
		if (slotMaxCache.containsKey(itemId))
			return slotMaxCache.get(itemId);
		short ret = 0;
		MapleData item = getItemData(itemId);
		if (item != null) {
			MapleData smEntry = item.getChildByPath("info/slotMax");
			if (smEntry == null) {
				if (getInventoryType(itemId).getType() == MapleInventoryType.EQUIP.getType())
					ret = 1;
				else
					ret = 100;
			} else {
				if (isThrowingStar(itemId))
					ret = 1;
				else if (MapleDataTool.getInt(smEntry) == 0)
					ret = 1;
				ret = (short) MapleDataTool.getInt(smEntry);
			}
		}
		slotMaxCache.put(itemId, ret);
		return ret;
	}

    public int getMeso(int itemId) {
        if (getMesoCache.containsKey(itemId))
            return getMesoCache.get(itemId);
            MapleData item = getItemData(itemId);
        if (item == null)
            return -1;
            int pEntry = 0;
            MapleData pData = item.getChildByPath("info/meso");
        if (pData == null)
            return -1;
            pEntry = MapleDataTool.getInt(pData);
            getMesoCache.put(itemId, pEntry);
            return pEntry;
    }

    public int getWholePrice(int itemId) {
        if (wholePriceCache.containsKey(itemId)) {
            return wholePriceCache.get(itemId);
        }
        MapleData item = getItemData(itemId);
        if (item == null) {
            return -1;
        }
        int pEntry = 0;
        MapleData pData = item.getChildByPath("info/price");
        if (pData == null) {
            return -1;
        }
        pEntry = MapleDataTool.getInt(pData);

        wholePriceCache.put(itemId, pEntry);
        return pEntry;
    }

    public double getPrice(int itemId) {
        if (priceCache.containsKey(itemId)) {
            return priceCache.get(itemId);
        }
        MapleData item = getItemData(itemId);
        if (item == null) {
            return -1;        //TODO ULTRAHACK - prevent players gaining miriads of mesars with orbis/eos scrolls
        }
        if (itemId == 4001019 || itemId == 4001020) {
            return 0;
        }

        double pEntry = 0.0;
        MapleData pData = item.getChildByPath("info/unitPrice");
        if (pData != null) {
            try {
                pEntry = MapleDataTool.getDouble(pData);
            } catch (Exception e) {
                pEntry = (double) MapleDataTool.getInt(pData);
            }
        } else {
            pData = item.getChildByPath("info/price");
            if (pData == null) {
                return -1;
            }
            pEntry = (double) MapleDataTool.getInt(pData);
        }

        priceCache.put(itemId, pEntry);
        return pEntry;
    }

//    protected Map<String, Integer> getEquipStats(int itemId) {
//        if (equipStatsCache.containsKey(itemId)) {
//            return equipStatsCache.get(itemId);
//        }
//        Map<String, Integer> ret = new LinkedHashMap<String, Integer>();
//        MapleData item = getItemData(itemId);
//        if (item == null) {
//            return null;
//        }
//        MapleData info = item.getChildByPath("info");
//        if (info == null) {
//            return null;
//        }
//        for (MapleData data : info.getChildren()) {
//            if (data.getName().startsWith("inc")) {
//                ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
//            }
//        }
//        ret.put("tuc", MapleDataTool.getInt("tuc", info, 0));
//        ret.put("reqLevel", MapleDataTool.getInt("reqLevel", info, 0));
//        ret.put("cursed", MapleDataTool.getInt("cursed", info, 0));
//        ret.put("success", MapleDataTool.getInt("success", info, 0));
//        ret.put("fs", MapleDataTool.getInt("fs", info, 0));
//        
//        equipStatsCache.put(itemId, ret);
//        return ret;
//    }
    
        protected Map<String, Integer> getEquipStats(int itemId) {
        if (equipStatsCache.containsKey(itemId)) {
            return equipStatsCache.get(itemId);
        }
        Map<String, Integer> ret = new LinkedHashMap<>();
        MapleData item = getItemData(itemId);
        if (item == null) {
            return null;
        }
        MapleData info = item.getChildByPath("info");
        if (info == null) {
            return null;
        }
        for (MapleData data : info.getChildren()) {
            if (data.getName().startsWith("inc")) {
                ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
            }
            /*else if (data.getName().startsWith("req"))
             ret.put(data.getName(), MapleDataTool.getInt(data.getName(), info, 0));*/
        }
        ret.put("reqJob", MapleDataTool.getInt("reqJob", info, 0));
        ret.put("reqLevel", MapleDataTool.getInt("reqLevel", info, 0));
        ret.put("reqDEX", MapleDataTool.getInt("reqDEX", info, 0));
        ret.put("reqSTR", MapleDataTool.getInt("reqSTR", info, 0));
        ret.put("reqINT", MapleDataTool.getInt("reqINT", info, 0));
        ret.put("reqLUK", MapleDataTool.getInt("reqLUK", info, 0));
        ret.put("reqPOP", MapleDataTool.getInt("reqPOP", info, 0));
        ret.put("isCashItem", MapleDataTool.getInt("cash", info, 0));
        ret.put("tuc", MapleDataTool.getInt("tuc", info, 0));
        ret.put("cursed", MapleDataTool.getInt("cursed", info, 0));
        ret.put("success", MapleDataTool.getInt("success", info, 0));
        ret.put("fs", MapleDataTool.getInt("fs", info, 0));
        equipStatsCache.put(itemId, ret);
        return ret;
    }
  

    public int getReqLevel(int itemId) {
        final Integer req = getEquipStats(itemId).get("reqLevel");
        return req == null ? 0 : req.intValue();
    }

    public List<Integer> getScrollReqs(int itemId) {
        List<Integer> ret = new ArrayList<Integer>();
        MapleData data = getItemData(itemId);
        data = data.getChildByPath("req");
        if (data == null) {
            return ret;
        }
        for (MapleData req : data.getChildren()) {
            ret.add(MapleDataTool.getInt(req));
        }
        return ret;
    }

    public MapleWeaponType getWeaponType(int itemId) {
        int cat = itemId / 10000;
        cat = cat % 100;
        switch (cat) {
            case 30:
                return MapleWeaponType.SWORD1H;
            case 31:
                return MapleWeaponType.AXE1H;
            case 32:
                return MapleWeaponType.BLUNT1H;
            case 33:
                return MapleWeaponType.DAGGER;
            case 37:
                return MapleWeaponType.WAND;
            case 38:
                return MapleWeaponType.STAFF;
            case 40:
                return MapleWeaponType.SWORD2H;
            case 41:
                return MapleWeaponType.AXE2H;
            case 42:
                return MapleWeaponType.BLUNT2H;
            case 43:
                return MapleWeaponType.SPEAR;
            case 44:
                return MapleWeaponType.POLE_ARM;
            case 45:
                return MapleWeaponType.BOW;
            case 47:
                return MapleWeaponType.CLAW;
            case 46:
                return MapleWeaponType.CROSSBOW;
            case 49:
                return MapleWeaponType.GUN;

        }
        return MapleWeaponType.NOT_A_WEAPON;
    }

    public boolean isShield(int itemId) {
        int cat = itemId / 10000;
        cat = cat % 100;
        return cat == 9;
    }

    public boolean isEquip(int itemId) {
        return itemId / 1000000 == 1;
    }

    public boolean isCleanSlate(int scrollId) {
        switch (scrollId) {
            case 2049000:
            case 2049001:
            case 2049002:
            case 2049003:
                return true;
        }
        return false;

    }

    public IItem scrollEquipWithId(IItem equip, int scrollId, boolean usingWhiteScroll, boolean isGM) {
        if (equip instanceof Equip) {
            Equip nEquip = (Equip) equip;
            Map<String, Integer> stats = this.getEquipStats(scrollId);
            Map<String, Integer> eqstats = this.getEquipStats(equip.getItemId());
            if ((nEquip.getUpgradeSlots() > 0 || isCleanSlate(scrollId)) && Math.ceil(Math.random() * 100.0) <= stats.get("success") || isGM) {
                switch (scrollId) {
                    case 2049000:
                    case 2049001:
                    case 2049002:
                    case 2049102: // Maple Syrup 100%
                    case 2049101: // Liar Tree Sap 100%
                    case 2049003:
                        if (nEquip.getUpgradeSlots() <= eqstats.get("tuc") && nEquip.getLevel() != eqstats.get("tuc")) {
                            byte newSlots = (byte) (nEquip.getUpgradeSlots() + 1);
                            nEquip.setUpgradeSlots(newSlots);
                            return equip;
                        }
                        break;
                    case 2049100:
                        int increase = 1;
                        if (Math.ceil(Math.random() * 100.0) <= 50) {
                            increase = increase * -1;
                        }
                        if (nEquip.getStr() > 0) {
                            short newStat = (short) (nEquip.getStr() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setStr(newStat);
                        }
                        if (nEquip.getDex() > 0) {
                            short newStat = (short) (nEquip.getDex() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setDex(newStat);
                        }
                        if (nEquip.getInt() > 0) {
                            short newStat = (short) (nEquip.getInt() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setInt(newStat);
                        }
                        if (nEquip.getLuk() > 0) {
                            short newStat = (short) (nEquip.getLuk() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setLuk(newStat);
                        }
                        if (nEquip.getWatk() > 0) {
                            short newStat = (short) (nEquip.getWatk() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setWatk(newStat);
                        }
                        if (nEquip.getWdef() > 0) {
                            short newStat = (short) (nEquip.getWdef() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setWdef(newStat);
                        }
                        if (nEquip.getMatk() > 0) {
                            short newStat = (short) (nEquip.getMatk() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setMatk(newStat);
                        }
                        if (nEquip.getMdef() > 0) {
                            short newStat = (short) (nEquip.getMdef() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setMdef(newStat);
                        }
                        if (nEquip.getAcc() > 0) {
                            short newStat = (short) (nEquip.getAcc() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setAcc(newStat);
                        }
                        if (nEquip.getAvoid() > 0) {
                            short newStat = (short) (nEquip.getAvoid() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setAvoid(newStat);
                        }
                        if (nEquip.getSpeed() > 0) {
                            short newStat = (short) (nEquip.getSpeed() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setSpeed(newStat);
                        }
                        if (nEquip.getJump() > 0) {
                            short newStat = (short) (nEquip.getJump() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setJump(newStat);
                        }
                        if (nEquip.getHp() > 0) {
                            short newStat = (short) (nEquip.getHp() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setHp(newStat);
                        }
                        if (nEquip.getMp() > 0) {
                            short newStat = (short) (nEquip.getMp() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setMp(newStat);
                        }
                        break;
                    default:
                        for (Entry<String, Integer> stat : stats.entrySet()) {
                            if (stat.getKey().equals("STR")) {
                                nEquip.setStr((short) (nEquip.getStr() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("DEX")) {
                                nEquip.setDex((short) (nEquip.getDex() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("INT")) {
                                nEquip.setInt((short) (nEquip.getInt() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("LUK")) {
                                nEquip.setLuk((short) (nEquip.getLuk() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("PAD")) {
                                nEquip.setWatk((short) (nEquip.getWatk() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("PDD")) {
                                nEquip.setWdef((short) (nEquip.getWdef() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("MAD")) {
                                nEquip.setMatk((short) (nEquip.getMatk() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("MDD")) {
                                nEquip.setMdef((short) (nEquip.getMdef() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("ACC")) {
                                nEquip.setAcc((short) (nEquip.getAcc() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("EVA")) {
                                nEquip.setAvoid((short) (nEquip.getAvoid() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("Speed")) {
                                nEquip.setSpeed((short) (nEquip.getSpeed() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("Jump")) {
                                nEquip.setJump((short) (nEquip.getJump() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("MHP")) {
                                nEquip.setHp((short) (nEquip.getHp() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("MMP")) {
                                nEquip.setMp((short) (nEquip.getMp() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("afterImage")) {
                            }
                        }
                        break;
                }
                nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
                nEquip.setLevel((byte) (nEquip.getLevel() + 1));
            } else {
                if (!usingWhiteScroll) {
                    nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
                }
                if (Math.ceil(1.0 + Math.random() * 100.0) < stats.get("cursed")) {
                    // DESTROY :) (O.O!)
                    return null;
                }
            }
        }
        return equip;
    }

    public IItem getEquipById(int equipId) {
        return getEquipById(equipId, -1);
    }
    

    public IItem getEquipById(int equipId, int ringId) {
        Equip nEquip;
        nEquip = new Equip(equipId, (byte) 0, ringId);
        nEquip.setQuantity((short) 1);
        Map<String, Integer> stats = this.getEquipStats(equipId);
        if (stats != null) {
            for (Entry<String, Integer> stat : stats.entrySet()) {
                if (stat.getKey().equals("STR")) {
                    nEquip.setStr((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("DEX")) {
                    nEquip.setDex((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("INT")) {
                    nEquip.setInt((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("LUK")) {
                    nEquip.setLuk((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("PAD")) {
                    nEquip.setWatk((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("PDD")) {
                    nEquip.setWdef((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("MAD")) {
                    nEquip.setMatk((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("MDD")) {
                    nEquip.setMdef((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("ACC")) {
                    nEquip.setAcc((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("EVA")) {
                    nEquip.setAvoid((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("Speed")) {
                    nEquip.setSpeed((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("Jump")) {
                    nEquip.setJump((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("MHP")) {
                    nEquip.setHp((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("MMP")) {
                    nEquip.setMp((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("tuc")) {
                    nEquip.setUpgradeSlots((byte) stat.getValue().intValue());
                } else if (stat.getKey().equals("afterImage")) {
                }
            }
        }
        equipCache.put(equipId, nEquip);
        if (ringId > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("GMT+3"));
            cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 3, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            nEquip.setExpiration(cal.getTimeInMillis());
        }
        return nEquip.copy();
    }

    private short getRandStat(short defaultValue, int maxRange) {
        if (defaultValue == 0) {
            return 0;
        }
        int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1), maxRange);
        return (short) ((defaultValue - lMaxRange) + Math.floor(rand.nextDouble() * (lMaxRange * 2 + 1)));
    }

    private short getRandStat1(short defaultValue, int maxRange) {
		if (defaultValue == 0) {
			return 0;
		}
		return (short) Math.round(defaultValue + Math.random() * maxRange);
	}
	
    public boolean isGun(int itemId) {
        return itemId >= 1492000 && itemId <= 1492024;
    }

    public Equip randomizeStats(Equip equip) {
		equip.setStr(getRandStat(equip.getStr(), 5));
		equip.setDex(getRandStat(equip.getDex(), 5));
		equip.setInt(getRandStat(equip.getInt(), 5));
		equip.setLuk(getRandStat(equip.getLuk(), 5));
		equip.setMatk(getRandStat(equip.getMatk(), 5));
		equip.setWatk(getRandStat(equip.getWatk(), 5));
		equip.setAcc(getRandStat(equip.getAcc(), 5));
		equip.setAvoid(getRandStat(equip.getAvoid(), 5));
		equip.setJump(getRandStat(equip.getJump(), 5));
		equip.setSpeed(getRandStat(equip.getSpeed(), 5));
		equip.setWdef(getRandStat(equip.getWdef(), 10));
		equip.setMdef(getRandStat(equip.getMdef(), 10));
		equip.setHp(getRandStat(equip.getHp(), 10));
		equip.setMp(getRandStat(equip.getMp(), 10));
		return equip;
	}
    
     public Equip randomizeStatsMalady(Equip equip) {
		equip.setStr(getRandStat(equip.getStr(), 8));
		equip.setDex(getRandStat(equip.getDex(), 8));
		equip.setInt(getRandStat(equip.getInt(), 8));
		equip.setLuk(getRandStat(equip.getLuk(), 8));
		equip.setMatk(getRandStat(equip.getMatk(), 8));
		equip.setWatk(getRandStat(equip.getWatk(), 8));
		equip.setAcc(getRandStat(equip.getAcc(), 8));
		equip.setAvoid(getRandStat(equip.getAvoid(), 8));
		equip.setJump(getRandStat(equip.getJump(), 8));
		equip.setSpeed(getRandStat(equip.getSpeed(), 8));
		equip.setWdef(getRandStat(equip.getWdef(), 12));
		equip.setMdef(getRandStat(equip.getMdef(), 12));
		equip.setHp(getRandStat(equip.getHp(), 12));
		equip.setMp(getRandStat(equip.getMp(), 12));
		return equip;
	}

    public Equip randomizeStats(Equip equip, int max) {
		equip.setStr(getRandStat1(equip.getStr(), max));
		equip.setDex(getRandStat1(equip.getDex(), max));
		equip.setInt(getRandStat1(equip.getInt(), max));
		equip.setLuk(getRandStat1(equip.getLuk(), max));
		equip.setMatk(getRandStat1(equip.getMatk(), max));
		equip.setWatk(getRandStat1(equip.getWatk(), max));
		equip.setAcc(getRandStat1(equip.getAcc(), max));
		equip.setAvoid(getRandStat1(equip.getAvoid(), max));
		equip.setJump(getRandStat1(equip.getJump(), max));
		equip.setSpeed(getRandStat1(equip.getSpeed(), max));
		equip.setWdef(getRandStat1(equip.getWdef(), max * 2));
		equip.setMdef(getRandStat1(equip.getMdef(), max * 2));
		equip.setHp(getRandStat1(equip.getHp(), max * 2));
		equip.setMp(getRandStat1(equip.getMp(), max * 2));
    //    equip.setUpgradeSlots((byte)(equip.getUpgradeSlots() + max/5));
		return equip;
	}

    public MapleStatEffect getItemEffect(int itemId) {
        MapleStatEffect ret = itemEffects.get(Integer.valueOf(itemId));
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if (item == null) {
                return null;
            }
            MapleData spec = item.getChildByPath("spec");
            ret = MapleStatEffect.loadItemEffectFromData(spec, itemId);
            itemEffects.put(Integer.valueOf(itemId), ret);
        }
        return ret;
    }

   public List<SummonEntry> getSummonMobs(int itemId) {
        if (this.summonEntryCache.containsKey(itemId)) {
            return summonEntryCache.get(itemId);
        }
        MapleData data = getItemData(itemId);
        int mobSize = data.getChildByPath("mob").getChildren().size();
        List<SummonEntry> ret = new LinkedList<SummonEntry>();
        for (int x = 0; x < mobSize; x++) {
            ret.add(new SummonEntry(MapleDataTool.getIntConvert("mob/" + x + "/id", data), MapleDataTool.getIntConvert("mob/" + x + "/prob", data)));
        }
        if (ret.size() == 0) {
            log.warn("Empty summon bag, itemID: {}", itemId);
        }
        summonEntryCache.put(itemId, ret);
        return ret;
    }

    public boolean isWeapon(int itemId) {
        return itemId >= 1302000 && itemId < 1492024;
    }

   
     public boolean isThrowingStar(int itemId) {
        return (itemId >= 2070000 && itemId < 2080000);
    }

    public boolean isBullet(int itemId) {
        int id = itemId / 10000;
        if (id == 233) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isRechargable(int itemId) {
        int id = itemId / 10000;
        if (id == 233 || id == 207) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOverall(int itemId) {
        return itemId >= 1050000 && itemId < 1060000;
    }

    public boolean isPet(int itemId) {
        return itemId >= 5000000 && itemId <= 5000053;
    }

    public boolean isArrowForCrossBow(int itemId) {
        return itemId >= 2061000 && itemId < 2062000;
    }

    public boolean isArrowForBow(int itemId) {
        return itemId >= 2060000 && itemId < 2061000;
    }

    public boolean isTwoHanded(int itemId) {
        switch (getWeaponType(itemId)) {
            case AXE2H:
                return true;
            case BLUNT2H:
                return true;
            case BOW:
                return true;
            case CLAW:
                return true;
            case CROSSBOW:
                return true;
            case POLE_ARM:
                return true;
            case SPEAR:
                return true;
            case SWORD2H:
                return true;
            case GUN:
                return true;
            case KNUCKLE:
                return true;
            default:
                return false;
        }
    }
    
    public boolean isTownScroll(int itemId) {
		return (itemId >= 2030000 && itemId < 2030020);
	}

    public int getWatkForProjectile(int itemId) {
        Integer atk = projectileWatkCache.get(itemId);
        if (atk != null) {
            return atk.intValue();
        }
        MapleData data = getItemData(itemId);
        atk = Integer.valueOf(MapleDataTool.getInt("info/incPAD", data, 0));
        projectileWatkCache.put(itemId, atk);
        return atk.intValue();
    }

    public boolean canScroll(int scrollid, int itemid) {
        int scrollCategoryQualifier = (scrollid / 100) % 100;
        int itemCategoryQualifier = (itemid / 10000) % 100;
        return scrollCategoryQualifier == itemCategoryQualifier;
    }

    public String getName(int itemId) {
        if (nameCache.containsKey(itemId)) {
            return nameCache.get(itemId);
        }
        MapleData strings = getStringData(itemId);
        if (strings == null) {
            return null;
        }
        String ret = MapleDataTool.getString("name", strings, null);
        nameCache.put(itemId, ret);
        return ret;
    }

    public String getDesc(int itemId) {
        if (descCache.containsKey(itemId)) {
            return descCache.get(itemId);
        }
        MapleData strings = getStringData(itemId);
        if (strings == null) {
            return null;
        }
        String ret = MapleDataTool.getString("desc", strings, null);
        descCache.put(itemId, ret);
        return ret;
    }

    public String getMsg(int itemId) {
        if (msgCache.containsKey(itemId)) {
            return msgCache.get(itemId);
        }
        MapleData strings = getStringData(itemId);
        if (strings == null) {
            return null;
        }
        String ret = MapleDataTool.getString("msg", strings, null);
        msgCache.put(itemId, ret);
        return ret;
    }
    
      public boolean isCash(int itemId) {
        return itemId / 1000000 == 5 || getEquipStats(itemId).get("cash") == 1;
    }

   public boolean isDropRestricted(int itemId) {
        if (dropRestrictionCache.containsKey(itemId)) {
            return dropRestrictionCache.get(itemId);
        }
        MapleData data = getItemData(itemId);

        boolean bRestricted = MapleDataTool.getIntConvert("info/tradeBlock", data, 0) == 1;
        if (!bRestricted) {
            bRestricted = MapleDataTool.getIntConvert("info/quest", data, 0) == 1;
            bRestricted = MapleDataTool.getIntConvert("info/cash", data, 0) == 1;
        }
        dropRestrictionCache.put(itemId, bRestricted);

        return bRestricted;
    }


    public boolean isPickupRestricted(int itemId) {
        if (pickupRestrictionCache.containsKey(itemId)) {
            return pickupRestrictionCache.get(itemId);
        }
        MapleData data = getItemData(itemId);
        boolean bRestricted = MapleDataTool.getIntConvert("info/only", data, 0) == 1;

        pickupRestrictionCache.put(itemId, bRestricted);
        return bRestricted;
    }

    public Map<String, Integer> getSkillStats(int itemId, double playerJob) {
        Map<String, Integer> ret = new LinkedHashMap<String, Integer>();
        MapleData item = getItemData(itemId);
        if (item == null) {
            return null;
        }
        MapleData info = item.getChildByPath("info");
        if (info == null) {
            return null;
        }
        for (MapleData data : info.getChildren()) {
            if (data.getName().startsWith("inc")) {
                ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
            }
        }
        ret.put("masterLevel", MapleDataTool.getInt("masterLevel", info, 0));
        ret.put("reqSkillLevel", MapleDataTool.getInt("reqSkillLevel", info, 0));
        ret.put("success", MapleDataTool.getInt("success", info, 0));

        MapleData skill = info.getChildByPath("skill");
        int curskill = 1;
 	    int size = skill.getChildren().size();
 		for (int i=0; i < size; i++) {
            curskill = MapleDataTool.getInt(Integer.toString(i), skill, 0);
            if (curskill == 0) {
                break;
            }
            double skillJob = Math.floor(curskill / 10000);
            if (skillJob == playerJob) {
                ret.put("skillid", curskill);
                break;
            }
        }

        if (ret.get("skillid") == null) {
            ret.put("skillid", 0);
        }
        return ret;
    }

 public List<Integer> petsCanConsume(int itemId) {
        List<Integer> ret = new ArrayList<Integer>();
        MapleData data = getItemData(itemId);
        int curPetId = 0;
        int size = data.getChildren().size();
        for (int i = 0; i < size; i++) {
            curPetId = MapleDataTool.getInt("spec/" + Integer.toString(i), data, 0);
            if (curPetId == 0) {
                break;
            }
            ret.add(Integer.valueOf(curPetId));
        }
        return ret;
    }
    protected Map<Integer, Boolean> isQuestItemCache = new HashMap<Integer, Boolean>();

    public boolean isQuestItem(int itemId) {
        if (isQuestItemCache.containsKey(itemId)) {
            return isQuestItemCache.get(itemId);
        }
        MapleData data = getItemData(itemId);
        boolean questItem = MapleDataTool.getIntConvert("info/quest", data, 0) == 1;
        isQuestItemCache.put(itemId, questItem);
        return questItem;
    }
    
    public boolean isMiniDungeonMap(int mapId) {
        switch (mapId) {
            case 100020000:
            case 105040304:
            case 105050100:
            case 221023400:
                return true;
            default:
                return false;
        }

    }

     public boolean isExpOrDropCardTime(int itemId) {
        TimeZone timeZone = TimeZone.getTimeZone("GMT-3");
        Calendar cal = Calendar.getInstance(timeZone);
        String day = MapleDayInt.getDayInt(cal.get(Calendar.DAY_OF_WEEK));
       // log.info(""+cal.get(Calendar.HOUR_OF_DAY));
        Map<String, String> times;
        if (getExpCardTimes.containsKey(itemId)) {
            times = getExpCardTimes.get(itemId);
        } else {
            List<MapleData> data = getItemData(itemId).getChildByPath("info").getChildByPath("time").getChildren();
            Map<String, String> hours = new HashMap<String, String>();
            for (MapleData childdata : data) {
                //MON:03-07
                String[] time = MapleDataTool.getString(childdata).split(":");
                hours.put(time[0], time[1]);
            }
            times = hours;
            getExpCardTimes.put(itemId, hours);
            cal.get(Calendar.DAY_OF_WEEK);
        }
        if (times.containsKey(day)) {
            String[] hourspan = times.get(day).split("-");
            int starthour = Integer.parseInt(hourspan[0]);
            int endhour = Integer.parseInt(hourspan[1]);
            if (cal.get(Calendar.HOUR_OF_DAY) >= starthour && cal.get(Calendar.HOUR_OF_DAY) <= endhour) {
                return true;
            }
        }
        return false;
    }

     public boolean isProperSlot(int itemId, byte slot) {
        byte comp = 0;

        if (getEquipStats(itemId).get("isCashItem") == 1) {
            comp -= 100;
        }
        if (itemId >= 1000000 && itemId < 1010000) {
            comp -= 1;
        } else if (itemId >= 1010000 && itemId < 1020000) {
            comp -= 2;
        } else if (itemId >= 1020000 && itemId < 1030000) {
            comp -= 3;
        } else if (itemId >= 1030000 && itemId < 1040000) {
            comp -= 4;
        } else if (itemId >= 1040000 && itemId < 1050000) {
            comp -= 5;
        } else if (itemId >= 1050000 && itemId < 1060000) {
            comp -= 5;
        } else if (itemId >= 1060000 && itemId < 1070000) {
            comp -= 6;
        } else if (itemId >= 1070000 && itemId < 1080000) {
            comp -= 7;
        } else if (itemId >= 1080000 && itemId < 1090000) {
            comp -= 8;
        } else if (itemId >= 1102000 && itemId < 1103000) {
            comp -= 9;
        } else if (itemId >= 1092000 && itemId < 1100000) {
            comp -= 10;
        } else if (itemId >= 1300000 && itemId < 1800000) {
            comp -= 11;
        } else if (itemId >= 1112000 && itemId < 1120000) {
            comp -= 12;
            if (slot <= comp && slot >= comp - 4) {
                return true;
            } else {
                return false;
            }
        } else if (itemId >= 1122000 && itemId < 1123000) {
            comp -= 17;
        } else if (itemId >= 1802000 && itemId < 1840000) {
            if (slot == -114 || (slot >= -128 && slot <= -122)) {
                return true;
            } else {
                return false;
            }
        } else if (itemId >= 1900000 && itemId < 2000000) {
            comp -= 18;
            if (slot <= comp && slot >= comp - 1) {
                return true;
            } else {
                return false;
            }
        } else if (itemId >= 1142000 && itemId < 1143000) {
            comp -= 49;
        } else if (itemId >= 1132000 && itemId < 1133000) {
            comp -= 50;
        }
        return slot == comp;
    }
    
    /**
     * @author Jvlaple
     */
    public static class SummonEntry {
        private int chance, mobId;
        
        public SummonEntry(int a, int b) {
            this.mobId = a;
            this.chance = b;
        }

        public int getChance() {
            return chance;
        }

        public int getMobId() {
            return mobId;
        }
    }
    
  public static class MapleDayInt {
        public static String getDayInt(int day) {
            if (day == 1) {
                return "SUN";
            } else if (day == 2) {
                return "MON";
            } else if (day == 3) {
                return "TUE";
            } else if (day == 4) {
                return "WED";
            } else if (day == 5) {
                return "THU";
            } else if (day == 6) {
                return "FRI";
            } else if (day == 7) {
                return "SAT";
            }
            return null;
        }
    }
}
