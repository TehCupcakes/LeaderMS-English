package handling.channel.handler;

import java.awt.Point;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import server.movement.LifeMovementFragment;
import tools.packet.PetPacket;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.input.StreamUtil;

    public class MovePetHandler extends AbstractMovementPacketHandler {
    //private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MovePetHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        slea.readInt();
        Point startPos = StreamUtil.readShortPoint(slea);
        List<LifeMovementFragment> res = parseMovement(slea);
        if (res.size() == 0) {
            return;
        }
        MapleCharacter player = c.getPlayer();
        int slot = player.getPetIndex(petId);
        if (player.inCS() || slot == -1) {
            return;
        }
        player.getPet(slot).updatePosition(res);
        player.getMap().broadcastMessage(player, PetPacket.movePet(player.getId(), petId, slot, res), false);
    }
}