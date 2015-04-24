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

package net.channel.handler;

import client.MapleBuffStat;
import client.MapleCharacter;
import java.rmi.RemoteException;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.world.remote.WorldChannelInterface;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
*
* @author Acrylic (Terry Han)
*/

/**
 *
 * @author Acrylic
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player.getNoPets() > 0) {
            player.unequipAllPets();
        }
        if (player.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
            player.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        } if (player.getBuffedValue(MapleBuffStat.SUMMON) != null) {
            player.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
        } if (player.getBuffedValue(MapleBuffStat.PUPPET) != null) {
            player.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
        try {
            WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
            wci.addBuffsToStorage(player.getId(), player.getAllBuffs());
            wci.addCooldownsToStorage(player.getId(), player.getAllCooldowns());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        
        player.getMap().removePlayer(player);
        c.getSession().write(MaplePacketCreator.warpCS(c, false));
        player.setInCS(true);
        c.getSession().write(MaplePacketCreator.enableCSUse0());
        c.getSession().write(MaplePacketCreator.enableCSUse1());
        c.getSession().write(MaplePacketCreator.enableCSUse2());
        c.getSession().write(MaplePacketCreator.enableCSUse3());
        c.getSession().write(MaplePacketCreator.showNXMapleTokens(player));
        c.getSession().write(MaplePacketCreator.sendWishList(player.getId(), false));
        player.saveToDB(true, true);
    }
}
