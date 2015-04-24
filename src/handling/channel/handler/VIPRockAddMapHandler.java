/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package handling.channel.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import client.MapleClient;
import client.MapleCharacter;
import database.DatabaseConnection;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matze
 */
public class VIPRockAddMapHandler extends AbstractMaplePacketHandler {

    private static Logger log = LoggerFactory.getLogger(VIPRockAddMapHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        Connection con = DatabaseConnection.getConnection();
        int operation = slea.readByte();
        int type = slea.readByte();
        MapleCharacter player = c.getPlayer();

        switch (operation) {
            case 0: // Remove map
                int mapid = slea.readInt();
                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM viprockmaps WHERE cid = ? AND mapid = ? AND type = ?");
                    ps.setInt(1, player.getId());
                    ps.setInt(2, mapid);
                    ps.setInt(3, type);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException lawl) {
                }
                break;
            case 1: // Add map
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO viprockmaps (`cid`, `mapid`, `type`) VALUES (?, ?, ?)");
                    ps.setInt(1, player.getId());
                    ps.setInt(2, player.getMapId());
                    ps.setInt(3, type);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException lawl) {
                }
                break;
            default:
                log.info("Unhandled VIP Rock operation: " + slea.toString());
                break;
        }
        c.getSession().write(MaplePacketCreator.refreshVIPRockMapList(player.getVIPRockMaps(type), type));
    }
}