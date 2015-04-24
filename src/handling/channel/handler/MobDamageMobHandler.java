/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import server.maps.MapleMap;
import server.life.MapleMonster;
import tools.packet.MaplePacketCreator;

/**
 * Handler for Mobs damaging Mobs.
 * @author Jvlaple
 */
public class MobDamageMobHandler extends AbstractMaplePacketHandler {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MobDamageMobHandler.class);    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid1 = slea.readInt(); //Id of mob that got attacked?
        @SuppressWarnings("unused")
        int randomstuff = slea.readInt(); //Dunno
        int oid2 = slea.readInt(); //Oid of mob that attacked?
        MapleMap map = c.getPlayer().getMap();
        MapleMonster attacked;
        MapleMonster attacker;
        try {
            attacked = map.getMonsterByOid(oid2);
            attacker = map.getMonsterByOid(oid1);
        } catch (NullPointerException npe) {
            return;
        }
        if (attacker == null || attacked == null) return;
		if (attacker.getId() == attacked.getId()) return;
        int dmg = attacker.getLevel() * 8;
        if (attacker.getLevel() > 50) {
            dmg *= 2;
        }
        attacked.damage(c.getPlayer(), dmg, true);
        if (attacked.getShouldDrop() == true) {
            attacked.setShouldDrop(false);
            attacked.scheduleCanDrop(3000);
        }
        attacked.setShouldDrop(false);
        attacked.getMap().broadcastMessage(MaplePacketCreator.mobDamageMob(attacked, dmg, 0));
    }
}

