package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import tools.packet.PetPacket;
import tools.data.input.SeekableLittleEndianAccessor;

public class PetChatHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        slea.readInt();
        int unknownShort = slea.readShort();
        String text = slea.readMapleAsciiString();
        MapleCharacter player = c.getPlayer();
        player.getMap().broadcastMessage(player, PetPacket.petChat(player.getId(), unknownShort, text, c.getPlayer().getPetIndex(petId)), true);
    }
}