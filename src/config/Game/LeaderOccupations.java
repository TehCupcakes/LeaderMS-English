/*
 * JavaScriptz (javascriptz@leaderms.com.br)
 * LeaderMS 2012 ▬ 2015
 * Brasil MapleStory Server
 * Ocupacoes
 * www.leaderms.com.br
 */

package config.Game;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import config.configuration.Configuration;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;



public enum LeaderOccupations {
    
    

   // SEMCLASSE(0),
    Iniciante(1),
    Alpha(100), 
    Platinium(110), 
    Senior(120), 
    LeaderReaper(130),
    DevilLeader(140), 
    NightLeader(150), 
    BankaiLeader(160), 
    HolyLeader(170), 
    LeutenantLeader(180),
    MasterLeader(190),
    GM(200), 
    Grim(800);
    final int jobid;

    
    private LeaderOccupations(int id) {
        jobid = id;
    }

    public int getId() {
        return jobid;
    }

    public static LeaderOccupations getById(int id) {
        for (LeaderOccupations l : LeaderOccupations.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }
    
    

    public boolean isA(LeaderOccupations basejob) {
        return getId() >= basejob.getId() && getId() / 10 == basejob.getId() / 10;
    }
    
    /* Message when getting item */
    public static String Message = "#e<"+Configuration.Server_Name+" Occupations>#n You received a prize for advancement!";
    /* Time Settings */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
    
    public static boolean getOcupacaoItem(MapleClient c) {
        HashMap<String, String> IncubatedItem = new HashMap<String, String>();
        try {
        FileReader fl = new FileReader("Game/CashItems/Occupations.properties");
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
        for (Map.Entry<String, String> entry : IncubatedItem.entrySet()) {
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
        MapleInventoryManipulator.addById(c, itemcode, (short) amount, "Occupation", "Advance Occupation");
        c.getSession().write(MaplePacketCreator.getShowItemGain(itemcode, (short) amount));
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        c.getPlayer().dropOverheadMessage(Message);        
        return true;
    }

    private static MapleInventory getInventory(MapleClient c, MapleInventoryType type) {
        return c.getPlayer().getInventory(type);
    }
}
