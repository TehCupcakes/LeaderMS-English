package net.channel.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
//import tools.StringUtil;
import tools.Pair;

/**
 *
 * @author Bassoe
 */
public class OXHandler {
    private static MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
	
        public static String getOXQuestion(int imgdir, int id){
            List<Pair<Integer, String>> itemPairs = new ArrayList<Pair<Integer, String>>();
            MapleData itemsData;
            itemsData = stringData.getData("OXQuiz.img").getChildByPath("" + imgdir + "");
	MapleData itemFolder = itemsData.getChildByPath("" + id + "");
	    int itemId = Integer.parseInt(itemFolder.getName());
	    String itemName = MapleDataTool.getString("q", itemFolder, "NO-NAME");
	    itemPairs.add(new Pair<Integer, String>(itemId, itemName));
	
        
        return itemPairs.toString();
        }
        
        public static int getOXAnswer(int imgdir, int id){
            List<Pair<Integer, String>> itemPairs = new ArrayList<Pair<Integer, String>>();
            MapleData itemsData;
            itemsData = stringData.getData("OXQuiz.img").getChildByPath("" + imgdir + "");
            MapleData itemFolder = itemsData.getChildByPath("" + id + "");
            int bla = MapleDataTool.getInt(itemFolder.getChildByPath("a"));
	
        
        return bla;
        }
        
        public static String getOXExplain(int imgdir, int id){
            List<Pair<Integer, String>> itemPairs = new ArrayList<Pair<Integer, String>>();
            MapleData itemsData;
            itemsData = stringData.getData("OXQuiz.img").getChildByPath("" + imgdir + "");
	MapleData itemFolder = itemsData.getChildByPath("" + id + "");
	    int itemId = Integer.parseInt(itemFolder.getName());
	    String itemName = MapleDataTool.getString("d", itemFolder, "NO-NAME");
	    itemPairs.add(new Pair<Integer, String>(itemId, itemName));
	
        
        return itemPairs.toString();
        }
}