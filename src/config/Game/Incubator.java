/*
 * JavaScriptz (javascriptz@leaderms.com.br)
 * LeaderMS 2012 ▬ 2015
 * Brasil MapleStory Server
 * Incubadora
 * www.leaderms.com.br
 */

package config.Game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import java.sql.Array;
import java.util.ArrayList;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.FilePrinter;
import tools.MaplePacketCreator;

public class Incubator {
    /* Message when using Incubator */
    public static String Message = "You just won an item, thank you for supporting us!";
    /* Time Settings */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
    
    public static final String[] Random = {"Normal", "Raros", "Medios"};
        
    public static boolean getIncubatedItem(MapleClient c) {
        double chance = Math.random();
        HashMap<String, String> IncubatedItem = new HashMap<String, String>();
        try {    
        FileReader fl = new FileReader("Game/CashItems/Incubator/" + Random[(int) (chance * Random.length)] + ".properties");
        BufferedReader br = new BufferedReader(fl);
        String[] readSplit = new String[2];
        String readLine = null;
        while ((readLine = br.readLine()) != null) {
            readSplit = readLine.split(" - ");
            IncubatedItem.put(readSplit[0], readSplit[1]);
        }
        fl.close();
        br.close();
        } catch (Exception e) {
            System.out.print(e);
            return false;
        }
        int rand = (int) (Math.random() * IncubatedItem.entrySet().size());
        int hmany = 0;
        int itemcode = 0;
        int amount = 0;
        int npc = 9050008;
        for (Entry<String, String> entry : IncubatedItem.entrySet()) {
            hmany++;
            if(hmany == rand) {
                try {
                    itemcode = Integer.parseInt(entry.getKey());
                    amount = Integer.parseInt(entry.getValue());
                    break;
                } catch (Exception e) {
                    System.out.print(e);
                    return false;
                }
            }
        }
        if (itemcode == 0 || amount == 0)
            return false;
        if (getInventory(c, MapleInventoryType.EQUIP).isFull(1) || getInventory(c, MapleInventoryType.USE).isFull(3) || getInventory(c, MapleInventoryType.ETC).isFull(1)) {
            c.getSession().write(MaplePacketCreator.getInventoryFull());
            c.getSession().write(MaplePacketCreator.getShowInventoryFull());
            return false;
        }
        MapleInventoryManipulator.addById(c, itemcode, (short) amount, "Incubator", "Donation Item");
        c.getSession().write(MaplePacketCreator.getShowItemGain(itemcode, (short) amount));
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        FilePrinter.printIncubadora(c.getPlayer().getName() + ".txt", "Item won: " + itemcode + "\r\nOn day: " + sdf.format(Calendar.getInstance().getTime()) + " at " + sdf2.format(Calendar.getInstance().getTime()) + ".");
        c.getPlayer().dropOverheadMessage(Message);
        return true;
      }             
           
            
    private static MapleInventory getInventory(MapleClient c, MapleInventoryType type) {
        return c.getPlayer().getInventory(type);
    }
}