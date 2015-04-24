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

import java.rmi.RemoteException;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import client.IEquip;
import client.IItem;
import client.inventory.InventoryException;
import client.ISkill;
import client.inventory.Item;
import client.MapleClient;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.SkillFactory;
import client.IEquip.ScrollResult;
import client.MapleCharacter;
import handling.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Matze
 * @author Frz
 */

public class ScrollHandler extends AbstractMaplePacketHandler {
//	private static Logger log = LoggerFactory.getLogger(ScrollHandler.class);
	final int gmBuff = 2022073;
	
	protected static int [] gmscrolls = {
		2040603,
		2044503,
		2041024,
		2041025,
		2044703,
		2044603,
		2043303,
		2040303,
		2040807,
		2040806,
		2040006,
		2040007,
		2043103,
		2043203,
		2043003,
		2040507,
		2040506,
		2044403,
		2040903,
		2043703,
		2040709,
		2040710,
		2040711,
		2044303,
		2040403,
		2044103,
		2044203,
		2044003,
		2044003
	};
	
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt(); // whatever...
		byte slot = (byte) slea.readShort();
		byte dst = (byte) slea.readShort();
		byte ws = (byte) slea.readShort();
		boolean whiteScroll = false; // white scroll being used?
		boolean legendarySpirit = false; // legendary spirit skill
		
		if ((ws & 2) == 2)
			whiteScroll = true;

		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		IEquip toScroll;
		if (dst < 0) {
			toScroll = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
		} else {
			// legendary spirit
			legendarySpirit = true;
			toScroll = (IEquip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
		}
		byte oldLevel = toScroll.getLevel();
 		byte oldSlots = toScroll.getUpgradeSlots();
		if (((IEquip) toScroll).getUpgradeSlots() < 1) {
			c.getSession().write(MaplePacketCreator.getInventoryFull());
			return;
		}
		MapleInventory useInventory = c.getPlayer().getInventory(MapleInventoryType.USE);
		IItem scroll = useInventory.getItem(slot);
		IItem wscroll = null;

		List<Integer> scrollReqs = ii.getScrollReqs(scroll.getItemId());
		if(scrollReqs.size() > 0 && !scrollReqs.contains(toScroll.getItemId())) {
			c.getSession().write(MaplePacketCreator.getInventoryFull()); 
			return; 
		} 

		if (whiteScroll) {
			wscroll = useInventory.findById(2340000);
			if (wscroll == null || wscroll.getItemId() != 2340000) {
				whiteScroll = false;
//				log.info("[h4x] Player {} is trying to scroll with non existant white scroll", new Object[] {c.getPlayer().getName() });
                                return;
			}
		}
		if (scroll.getItemId() != 2049100 && !ii.isCleanSlate(scroll.getItemId())) {
			if (!ii.canScroll(scroll.getItemId(), toScroll.getItemId())) {
//				log.info("[h4x] Player {} is trying to scroll {} with {} which should not work", new Object[] {c.getPlayer().getName(), toScroll.getItemId(), scroll.getItemId() });
				return;
			}
		}
		if (scroll.getQuantity() <= 0) {
			throw new InventoryException("<= 0 quantity when scrolling");
		}
		IEquip scrolled = (IEquip) ii.scrollEquipWithId(toScroll, scroll.getItemId(), whiteScroll, c.getPlayer().isGM());
		ScrollResult scrollSuccess = IEquip.ScrollResult.FAIL; // fail
		if (scrolled == null) {
			scrollSuccess = IEquip.ScrollResult.CURSE;
		} else if (scrolled.getLevel() > oldLevel || (ii.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() == oldSlots + 1)) {
			scrollSuccess = IEquip.ScrollResult.SUCCESS;
		}
		useInventory.removeItem(scroll.getPosition(), (short) 1, false);
		if (whiteScroll) {
			useInventory.removeItem(wscroll.getPosition(), (short) 1, false);
			if (wscroll.getQuantity() < 1) {
				c.getSession().write(MaplePacketCreator.clearInventoryItem(MapleInventoryType.USE, wscroll.getPosition(), false));
			} else {
				c.getSession().write(MaplePacketCreator.updateInventorySlot(MapleInventoryType.USE, (Item) wscroll));
			}
		}
		if (scrollSuccess == IEquip.ScrollResult.CURSE) {
			c.getSession().write(MaplePacketCreator.scrolledItem(scroll, toScroll, true));

			if (dst < 0) {
				c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
			} else {
				c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
			}
		} else {
			c.getSession().write(MaplePacketCreator.scrolledItem(scroll, scrolled, false));
		}
		c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getScrollEffect(c.getPlayer().getId(), scrollSuccess, legendarySpirit));
		
		ISkill LS = SkillFactory.getSkill(1003);
		int LSLevel = c.getPlayer().getSkillLevel(LS);
		
		if (legendarySpirit && LSLevel <= 0) {
			return;
		}
		// equipped item was scrolled and changed
		if (dst < 0 && (scrollSuccess == IEquip.ScrollResult.SUCCESS || scrollSuccess == IEquip.ScrollResult.CURSE)) {
			c.getPlayer().equipChanged();
		}
		
		switch (toScroll.getItemId()) {
			case 1122000:
				MapleMap map = c.getChannelServer().getMapFactory().getMap(240000000);
				map.broadcastMessage(MaplePacketCreator.serverNotice(5, 
						"A mysterious power arose as I heard the powerful cry of Nine Spirit Baby Dragon."));
				map.buffMap(2022109);
				break;
		}
		if (isGmScroll(scroll.getItemId()) && !(toScroll.getItemId() == 1122000)) {
			MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
            MapleStatEffect statEffect = mii.getItemEffect(2022118);
			for (MapleCharacter mc : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
				statEffect.applyTo(mc);
			}
			c.getChannelServer().broadcastPacket(
					MaplePacketCreator.serverNotice(5, 
					"A Mysterious power arose as I heard the power of the super scroll."));
			try {
				c.getChannelServer().getWorldInterface().broadcastGMMessage("", MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " is using a GM scroll, itemID: " + scroll.getItemId()).getBytes());
			} catch (RemoteException ex) {
				c.getChannelServer().reconnectWorld();
			}
		}
	}
	
	protected static boolean isGmScroll(int a) {
		for (int k : gmscrolls) {
			if (k == a) return true;
		}
		return false;
	}
}