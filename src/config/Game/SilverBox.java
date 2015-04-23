/*
 * JavaScriptz (javascriptz@leaderms.com.br)
 * LeaderMS 2012 ▬ 2015
 * Brasil MapleStory Server
 * Gold Box (4280000)
 * www.leaderms.com.br
 */

package config.Game;

import java.util.Random;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;


public class SilverBox extends AbstractMaplePacketHandler {
    /* Mensagem ao usar Box */
    public static String Message = "You do not have to Silver Box key to open this box!";
    /* Inventario Full */
    public static String Inv_Cheio = "You do not have enough space in the inventory to receive the item. Please clear some space in your inventory.";
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readShort();
        int itemid = slea.readInt();
        if (!c.getPlayer().isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (!c.getPlayer().haveItem(5490000)) { 
            c.getPlayer().dropMessage(5, Message);
            return;
        }
        for (MapleInventoryType type : MapleInventoryType.values()) { 
            if (c.getPlayer().getInventory(type).isFull()) {
                c.getPlayer().dropMessage(5, Inv_Cheio + type.name().toLowerCase() + ".");
                return;
            }
        }
        int prizeid = 0;
        double chance = Math.random();
        /* Silver Box */
        if (itemid == 4280001) { 
            int[] common = {3010000, 3010001, 3010002, 3010003, 3010006, 1002253, 1002254, 1442019, 1402028, 1312024, 1002339, 1082116, 1452014, 1452015, 1452013, 1332029, 1332027, 1332036, 1472026, 1082210, 1482010, 2290124, 2290036, 2290088, 2290117, 2290123, 2044800, 2044900, 2040800, 5200000};
            int[] uncommon = {3010018, 3010019, 3010025, 3010011, 3010012, 1432018, 2040801, 2044701, 2043801, 2043301, 2040704, 2044801, 2044401, 2040701, 2022179, 5200001};
            int[] rare = {3010007, 3010008, 3010010, 3010040, 3010060, 3010062, 2290061, 2290085, 2290089, 2290063, 2290023, 2290003, 2049000, 2049001, 2049002, 2049003, 2022282, 2022283};
            /* Items Raros */
            if (chance < 0.05) {
                prizeid = rare[new Random().nextInt(rare.length)];
            } 
            /* Super Raros */
            else if (chance >= 0.06 && chance < 0.35) { 
                prizeid = uncommon[new Random().nextInt(uncommon.length)];
            } 
            /* Comuns */
            else { 
                prizeid = common[new Random().nextInt(common.length)];
            }
        /* Gold Box */    
        } else { 
            int[] common = {3010004, 3010015, 3010013, 3011000, 1382047, 1382048, 1372010, 1382010, 1002271, 1402016, 1442020, 1402037, 1442008, 1402035, 1422031, 1412019, 1412010, 1462018, 1462013, 1452009, 1332052, 1002283, 1002328, 1002327, 1082210, 1482010, 2290028, 2290056, 2290034, 2290044, 2290121, 2290026, 2044800, 2044900, 2040800, 5200000};
            int[] uncommon = {3010009, 3010016, 3010017, 3010014, 3010041, 2040809, 2040811, 2043805, 2043305, 2040715, 2044804, 2044405, 2040713, 2022179, 5200002};
            int[] rare = {3010046, 3010047, 3010058, 3010057, 3010043, 3010071, 3010085, 2290007, 2290049, 2290043, 2290122, 2290039, 2290055, 2290120, 2290087, 2290063, 2290103, 2290069, 2290033, 2049000, 2049001, 2049002, 2049003, 2022282, 2022283};
            /* Items Raros */
            if (chance < 0.1) {
                prizeid = rare[new Random().nextInt(rare.length)];
            } 
            /* Super Raros */
            else if (chance >= 0.11 && chance < 0.35) {
                prizeid = uncommon[new Random().nextInt(uncommon.length)];
            } 
            /* Comuns */
            else { 
                prizeid = common[new Random().nextInt(common.length)];
            }
        }
        if (prizeid != 0) {
            /* Remove Chave */
            c.getPlayer().gainItem(5490000, (short) -1, false, false); 
            /* Remove Box */
            c.getPlayer().gainItem(itemid, (short) -1, false, false); 
            /* Gain Item */
            c.getPlayer().gainItem(prizeid, (short) 1, true, true); 
            /* Shows Effect of the Box being opened */
            c.getSession().write(MaplePacketCreator.sendSilverBoxOpened(itemid)); 
        }
    }
}