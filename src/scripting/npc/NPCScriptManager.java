package scripting.npc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;
import client.MapleClient;
import client.MapleCharacter;
import client.ScriptDebug;
import java.lang.reflect.UndeclaredThrowableException;
import javax.script.ScriptException;
import handling.world.MaplePartyCharacter;
import scripting.AbstractScriptManager;
import tools.FilePrinter;

/**
 *
 * @author Matze
 */
public class NPCScriptManager extends AbstractScriptManager {

    private Map<MapleClient, NPCConversationManager> cms = new HashMap<MapleClient, NPCConversationManager>();
    private Map<MapleClient, NPCScript> scripts = new HashMap<MapleClient, NPCScript>();
    private static NPCScriptManager instance = new NPCScriptManager();

    public synchronized static NPCScriptManager getInstance() {
        return instance;
    }
	
    
  public void start(String filename, MapleClient c, int npc, List<MaplePartyCharacter> chrs) {
        try {
            NPCConversationManager cm = new NPCConversationManager(c, npc, chrs, 0);
            cm.dispose();
            if (cms.containsKey(c)) {
                return;
            }
            cms.put(c, cm);
            Invocable iv = getInvocable("npc/" + filename + ".js", c);
            NPCScriptManager npcsm = NPCScriptManager.getInstance();

            if (iv == null || NPCScriptManager.getInstance() == null) {
                cm.dispose();
                return;
            }
            if (iv == null || npcsm == null) {
                cm.dispose();
                return;
            }
            engine.put("cm", cm);
            NPCScript ns = iv.getInterface(NPCScript.class);
            scripts.put(c, ns);
	    ns.start(chrs);
        } catch (Exception e) {
            FilePrinter.printError(FilePrinter.NPC + npc + ".txt", e);
            dispose(c);
            cms.remove(c);
        }		
	}
  

       public void start(MapleClient c, int npc, String filename, MapleCharacter chr) {
        try {
            NPCConversationManager cm = new NPCConversationManager(c, npc);
            if (cms.containsKey(c)) {
                return;
            }
            cms.put(c, cm);
            Invocable iv = null;
            String path = "";
            if (filename != null) {
                path = "npc/" + filename + ".js";
                iv = getInvocable("npc/" + filename + ".js", c);
            }
            if (iv == null) {
                path = "npc/" + npc + ".js";
                iv = getInvocable("npc/" + npc + ".js", c);
            }
            if (iv == null || NPCScriptManager.getInstance() == null) {
                dispose(c);
                return;
            }
            engine.put("cm", cm);
            NPCScript ns = iv.getInterface(NPCScript.class);
            scripts.put(c, ns);
            c.setScriptDebug(new ScriptDebug(c, path, cm));
            if (chr == null) {
                ns.start();
            } else {
                ns.start(chr);
            }
        } catch (UndeclaredThrowableException ute) {
            FilePrinter.printError(FilePrinter.NPC + npc + ".txt", ute);
            FilePrinter.printError(String.valueOf(getCM(c).getNpc()), ute);
            dispose(c);
            cms.remove(c);
            notice(c, npc);
        } catch (Exception e) {
            FilePrinter.printError(FilePrinter.NPC + npc + ".txt", e);
            FilePrinter.printError(String.valueOf(getCM(c).getNpc()), e);
            dispose(c);
            cms.remove(c);
            notice(c, npc);
        }
    }

    public void action(MapleClient c, byte mode, byte type, int selection) {
        NPCScript ns = scripts.get(c);
        if (ns != null) {
            try {
                ns.action(mode, type, selection);
            } catch (Exception e) {
                FilePrinter.printError(FilePrinter.NPC + getCM(c).getNpc() + ".txt", e);
                notice(c, getCM(c).getNpc());
                dispose(c);
            }
        }
    }

    public void dispose(NPCConversationManager cm) {
        cms.remove(cm.getC());
        scripts.remove(cm.getC());
        resetContext("npc/" + cm.getNpc() + ".js", cm.getC());
    }

     public void dispose(MapleClient c) {
        if (cms.get(c) != null) {
            dispose(cms.get(c));
        }
    }
     
        private void notice(MapleClient c, int id) {
        if (c != null) {
            c.getPlayer().dropMessage(1, "An unknown error occurred during the execution of this NPC. Please report this to one of the Admins! ID: " + id);
        }
    }

    public NPCConversationManager getCM(MapleClient c) {
        return cms.get(c);
    }
}
