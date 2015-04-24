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

import java.net.InetAddress;

import client.MapleCharacter;
import client.MapleClient;
import handling.AbstractMaplePacketHandler;
import handling.MaplePacket;
import handling.channel.ChannelServer;
import server.MaplePortal;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.MapleTrade;

public class ChangeMapHandler extends AbstractMaplePacketHandler {

    private static Logger log = LoggerFactory.getLogger(ChangeMapHandler.class);

 @Override
       public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() == 0) {
            int channel = c.getChannel();
            String ip = ChannelServer.getInstance(c.getChannel()).getIP(channel);
            String[] socket = ip.split(":");
            c.getPlayer().saveToDB(true, true);
            c.getPlayer().setInCS(false);
            c.getPlayer().setInMTS(false);
            ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            try {
                MaplePacket packet = MaplePacketCreator.getChannelChange(
                        InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
                c.getSession().write(packet);
                c.getSession().close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            @SuppressWarnings("unused")
            byte something = slea.readByte(); //?
            int targetid = slea.readInt(); //FF FF FF FF

            String startwp = slea.readMapleAsciiString();
            MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);

            MapleCharacter player = c.getPlayer();
            if (targetid != -1 && !c.getPlayer().isAlive()) {
                boolean executeStandardPath = true;
                if (player.getEventInstance() != null) {
                    executeStandardPath = player.getEventInstance().revivePlayer(player);
                }
                if (executeStandardPath) {
                    //player.setHp(50);
                    //player.gainExp(-player.getExp(), false, false);
                    player.addHP(30000);
                    player.dispelSkill(0);
                    player.saveToDB(true, true);
                    if (c.getPlayer().getMap().getForcedReturnId() != 999999999) {
                        if (c.getPlayer().getMap().isCPQMap()) {
                            MapleMap to = c.getChannelServer().getMapFactory().getMap(
                                    c.getPlayer().getMap().getId() + 1);
                            MaplePortal pto = to.getPortal(0);
                            player.setStance(0);
                            player.changeMap(to, pto);

                        } else {
                            MapleMap to = c.getPlayer().getMap().getForcedReturnMap();
                            MaplePortal pto = to.getPortal(0);
                            player.setStance(0);
                            player.changeMap(to, pto);

                        }
                    } else {
                        if (c.getPlayer().getMap().isCPQMap()) {
                            MapleMap to = c.getChannelServer().getMapFactory().getMap(
                                    c.getPlayer().getMap().getId() + 1);
                            MaplePortal pto = to.getPortal(0);


                        } else {
                            MapleMap to = c.getPlayer().getMap().getReturnMap();
                            MaplePortal pto = to.getPortal(0);
                            player.setStance(0);
                            player.changeMap(to, pto);

                        }
                    }
                }
            } else if (targetid != -1 && c.getPlayer().isGM()) {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                MaplePortal pto = to.getPortal(0);
                player.changeMap(to, pto);
            } else if (targetid != -1 && !c.getPlayer().isGM()) {
                log.warn("Player {} attempted Mapjumping without being a gm", c.getPlayer().getName());
            } else {
                if (portal != null) {
                    portal.enterPortal(c);
                } else {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    log.warn("Portal {} not found on map {}", startwp, c.getPlayer().getMap().getId());
                }
            }
        }
    }
}
