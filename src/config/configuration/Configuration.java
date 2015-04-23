/*
 * JavaScriptz (javascriptz@leaderms.com.br)
 * LeaderMS 2012 ▬ 2015
 * Brasil MapleStory Server
 * Configurações Gerais
 * www.leaderms.com.br
 */


package config.configuration;

import java.util.Properties;
import client.MapleInventoryType;
import java.util.Arrays;
import java.util.List;
import net.world.WorldServer;
import server.maps.MapleMapObjectType;

public class Configuration {
    private static final Properties server;
    public static final byte getChannelLimit;
    
    static {
        server = WorldServer.getInstance().getWorldProp();
        getChannelLimit = Byte.parseByte(server.getProperty("ChannelCount", "6"));
    }
    
    public static String getProperty(String name) {
        if (server.containsKey(name)) {
            return server.getProperty(name);
        } else {
            System.out.println("Error locating the properties for: " + name + ".");
            return null;
        }
    }

    public static Properties worldServerProperties() {
        return server;
    }
    
    public static final List<MapleMapObjectType> rangedMapobjectTypes = Arrays.asList(
        MapleMapObjectType.ITEM,
        MapleMapObjectType.MONSTER,
        MapleMapObjectType.DOOR,
        MapleMapObjectType.REACTOR,
        MapleMapObjectType.SUMMON,
        MapleMapObjectType.NPC,
        MapleMapObjectType.MIST);
    
    
    /*    Server Information     */
    public static final String MS_Version = ".62";
    public static final String Server_Version = "1.0";
    public static String Server_Name = "LeaderMS";
    /*   Login Messages     */
    public static final String Player_Login = "Welcome back. Don't forget to vote for our server (#r#e"+Configuration.Server_Name+"#k#n).";
    public static final String Player_Newcomer = "Hello beginner, use the command @commands and enjoy the game.";
    public static final String Player_Buffed = "<You have been buffed by LeaderBot>";
    public static final String New_Player = "Joined our server!";
    /*      LeaderPoints      */
    public static int LeaderPoints_1 = 1;
    public static int LeaderPoints_2 = 2;
    public static int LeaderPoints_3 = 3;
    /*       CashShop        */
    public static int[] BLOCKED_CS = new int[]{
        1812006,/*Magic Scales*/ 
        1812007,/*Item Ignore Pendant*/  
        5230000,/*The Owl Of Minerva*/  
        5220000,/* Gachapon Ticket */
        5400000,/*Character Name Change*/  
        5401000,/*Character Transfer*/ 
        5430000,/*Extra Character Slot Coupon*/
        5140000,
        5140001,
        5140002,
        5140003,
        5140004,
        1912004,
        1902009,
        1912003,
        1902008,
        5140006,
        5370000,
        5370001,
        5281000
            
    };
    /*  System messages */
   public static final String[] botMessages = {
   "Welcome to the best server in the world!",
   "Report any bugs or errors on our community.",
   "Nostalgia is in the air. Participate in our daily events!",
   "Remember to register on our forums.",
   "Have an idea to improve the game? Leave your suggestions on our community!"
   };

    
    /* Other adjustments */   
    public static MapleInventoryType getInventoryType(final int itemId) {
        final byte type = (byte) (itemId / 1000000);
        if (type < 1 || type > 5) {
            return MapleInventoryType.UNDEFINED;
        }
        return MapleInventoryType.getByType(type);
    }
    
     public static boolean isBeginnerJob(final int job) {
        return job == 0 || job == 1 || job == 1000 || job == 2000 || job == 2001 || job == 3000 || job == 3001 || job == 2002;
    }
     
    public static boolean isWeapon(final int itemId) {
        return itemId >= 1300000 && itemId < 1533000;
    }
         
    public static boolean isRechargable(int itemId) {
        return itemId / 10000 == 233 || itemId / 10000 == 207;
    }
    
    public static final boolean isThrowingStar(int itemId) {
        return itemId / 10000 == 207;
    }

    public static final boolean isBullet(int itemId) {
        return itemId / 10000 == 233;
    }
        
     public static boolean isForceRespawn(int mapid) {
        switch (mapid) {
            case 103000800:
            case 925100100: 
                return true;
            default:
                return mapid / 100000 == 9800 && (mapid % 10 == 1 || mapid % 1000 == 100);
        }
    }

     public static final int maxViewRangeSq() {
	return 800000; // 800 * 800
    }
}