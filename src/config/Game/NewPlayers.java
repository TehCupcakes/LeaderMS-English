/*
 * JavaScriptz (javascriptz@leaderms.com.br)
 * LeaderMS 2012 ▬ 2015
 * Brasil MapleStory Server
 * Ajuda p/ Iniciantes
 * www.leaderms.com.br
 */

package config.Game;

import client.MapleCharacter;
import client.MapleClient;
import config.configuration.Configuration;

public class NewPlayers {
    
    /* Variaveis */
    private MapleClient client;
    /* Fim */
    
        public MapleClient getClient() {
        return client;
    }
    public static String NPCNATAL = "Cliff";
    public static String Level = "Hello #e#h ##n,\r\n" +
         "Please take a minute to read this message!\r\n" +
         "I want to remind you about the class requirements in "+Configuration.Server_Name+", which many players may forget or ignore.\r\n" +
         "This message shows how points should be destributed, so that you can choose the class you want.\r\n\r\n" +
         "#eMagician#n\r\nLevel - 08\r\nIntelligence - 20\r\n\r\n" +
         "#eBowman#n\r\nLevel - 10\r\nDexterity - 25\r\n\r\n" +
         "#eThief#n\r\nLevel - 10\r\nDexterity - 25\r\n\r\n" +
         "#eWarrior#n\r\nLevel - 10\r\nStrength - 35\r\n\r\n" +
         "#ePirate#n\r\nLevel - 10\r\nDexterity - 20\r\n\r\n" +
         "Remember that these requirements are mandatory. "+Configuration.Server_Name+" appologizes for any inconvenience this may cause. Thank you.\r\nUse @commands if you have other question. Enjoy the game!";
    
   public static String EventoDeNatal = "Hello #e#h ##n,\r\nThis is a small explanation of the "+Configuration.Server_Name+" Christmas Quest. Some of the items that you will need on your journey for this quest will be activated with the @dropnatal command. This activates dropping of winter films in Maple World. You will need to speak with " + NPCNATAL + " in Happy Village!";

}


