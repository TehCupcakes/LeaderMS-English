package handling.channel.handler;

import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Bassoe
 */
public class PassiveEnergyHandler extends AbstractMaplePacketHandler {
    //private Logger log = LoggerFactory.getLogger(EnergyPassiveHandler.class);
        @Override
        public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
            if (c.getPlayer().isHidden()) return;
            slea.readByte(); // 02
            slea.readInt(); // 11 F1 F8 4D
            slea.readShort(); // 00 00
            slea.readByte(); // 00
            slea.readShort(); // 08 00
            slea.readInt(); // D7 8A 11 00
            int oid = slea.readInt(); // 6B 00 00 00
            slea.readInt(); // 06 80 05 05
            slea.readShort(); // D4 00
            slea.readShort(); // D7 00
            slea.readShort(); // D6 00
            slea.readInt(); // D7 00 00 00
            int damage = slea.readInt(); // 9F 86 01 00
            slea.readInt(); // CA 98 39 DA
            slea.readShort(); // C5 00
            slea.readShort(); // D7 00
            
            if (damage > 200000 && !c.getPlayer().isGM()) {
                c.getPlayer().ban("Wtf? why are you hitting " + damage + " with energy charge??");
            }

            MapleMonster attacker = (MapleMonster) c.getPlayer().getMap().getMapObject(oid);
			if (attacker == null) return;
            c.getPlayer().getMap().damageMonster(c.getPlayer(), attacker, damage);
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.damageMonster(oid, damage), false, true);

        }
}  