package server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.StringUtil;

/**
 *
 * @author Lerk
 */
public class CashItemFactory {
        private static CashItemFactory instance = null;
	private static Map<Integer, Integer> snLookup = new HashMap<Integer,Integer>();
	private static Map<Integer, CashItemInfo> itemStats = new HashMap<Integer, CashItemInfo>();
	private static MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
	private static MapleData commodities = data.getData(StringUtil.getLeftPaddedStr("Commodity.img", '0', 11));;
        
      public static CashItemFactory getInstance() {
        if (instance == null) {
            instance = new CashItemFactory();
        }
        return instance;
   }

	
	public static CashItemInfo getItem(int sn) {
		CashItemInfo stats = itemStats.get(sn);
		if (stats == null) {
			int cid = getCommodityFromSN(sn);
			
			int itemId = MapleDataTool.getIntConvert(cid + "/ItemId",commodities);
			int count = MapleDataTool.getIntConvert(cid + "/Count",commodities,1);
			int price = MapleDataTool.getIntConvert(cid + "/Price",commodities,0);
                        int period = MapleDataTool.getIntConvert(cid + "/Period",commodities,1);
                        int donor = MapleDataTool.getIntConvert(cid + "/Donor",commodities,0);
			stats = new CashItemInfo(itemId, count, price, period, donor);
			
			itemStats.put(sn, stats);
		}
		
		return stats;
	}
	
	private static int getCommodityFromSN(int sn) {
		int cid;
		
		if (snLookup.get(sn) == null) {
			int curr = snLookup.size() - 1;
			int currSN = 0;
			if (curr == -1) {
				curr = 0;
				currSN = MapleDataTool.getIntConvert("0/SN",commodities);
				snLookup.put(currSN, curr);
				
			}
			for (int i = snLookup.size() - 1; currSN != sn; i++) {
				curr = i;
				currSN = MapleDataTool.getIntConvert(curr + "/SN",commodities);
				snLookup.put(currSN, curr);
			}
			cid = curr;
		}
		else {
			cid = snLookup.get(sn);
		}
		return cid;
	}

   public static List<Integer> getPackageItems(int itemId) {
        List<Integer> packageItems = new ArrayList<Integer>();
        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/Etc.wz"));
        MapleData a = dataProvider.getData("CashPackage.img");
        if (packageItems.contains(itemId)) {
            return packageItems;
        }
        for (MapleData b : a.getChildren()) {
            if (itemId == Integer.parseInt(b.getName())) {
                for (MapleData c : b.getChildren()) {
                    for (MapleData d : c.getChildren()) {
                        int sn = MapleDataTool.getIntConvert("" + Integer.parseInt(d.getName()), c);
                        CashItemInfo item = getItem(sn);
                        packageItems.add(item.getId());
                    }
                }
                break;
            }
        }
        return packageItems;
    }
}