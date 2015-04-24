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

import java.util.ArrayList;
import java.util.List;

import client.ISkill;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.SkillFactory;
import handling.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class DistributeAPHandler extends AbstractMaplePacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DistributeAPHandler.class);
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
		c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true));
		slea.readInt();
		int update = slea.readInt();
                 if (c.getPlayer().getRemainingAp() <= -1) {
                     return;
                 }
		if (c.getPlayer().getRemainingAp() > 0) {
			switch (update) {
				case 64: // Str
					if (c.getPlayer().getStr() >= 999)
						return;
					c.getPlayer().setStr(c.getPlayer().getStr() + 1);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, c.getPlayer().getStr()));
					break;
				case 128: // Dex
					if (c.getPlayer().getDex() >= 999)
						return;
					c.getPlayer().setDex(c.getPlayer().getDex() + 1);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, c.getPlayer().getDex()));
					break;
				case 256: // Int
					if (c.getPlayer().getInt() >= 999)
						return;
					c.getPlayer().setInt(c.getPlayer().getInt() + 1);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, c.getPlayer().getInt()));
					break;
				case 512: // Luk
					if (c.getPlayer().getLuk() >= 999)
						return;
					c.getPlayer().setLuk(c.getPlayer().getLuk() + 1);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, c.getPlayer().getLuk()));
					break;
				case 2048: // HP
					int MaxHP = c.getPlayer().getMaxHp();
					if (c.getPlayer().getHpApUsed() == 10000 || MaxHP == 30000)
						return;
					ISkill improvingMaxHP = null;
					int improvingMaxHPLevel = 0;
					if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
						MaxHP += rand(8, 12);
					} else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
						improvingMaxHP = SkillFactory.getSkill(1000001);
						improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
						if (improvingMaxHPLevel >= 1) {
							MaxHP += rand(20, 24) + improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
						} else {
							MaxHP += rand(20, 24);
						}
					} else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
						MaxHP += rand(6, 10);
					} else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
						MaxHP += rand(16, 20);
					} else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
						MaxHP += rand(20, 24);
					} else if (c.getPlayer().getJob().isA(MapleJob.PIRATE)) {
						improvingMaxHP = SkillFactory.getSkill(5100000);
						improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
						if (improvingMaxHPLevel >= 1) {
							MaxHP += rand(16, 20) + improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
						} else {
							MaxHP += rand(16, 20);
						}
					}
					MaxHP = Math.min(30000, MaxHP);
					c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() + 1);
					c.getPlayer().setMaxHp(MaxHP);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, MaxHP));
					break;
				case 8192: // MP
					int MaxMP = c.getPlayer().getMaxMp();
					if (c.getPlayer().getMpApUsed() == 10000 || c.getPlayer().getMaxMp() == 30000)
						return;
					if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
						MaxMP += rand(6, 8);
					} else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
						MaxMP += rand(2, 4);
					} else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
						ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
						int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
						if (improvingMaxMPLevel >= 1) {
							MaxMP += rand(18, 20) + improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
						} else {
							MaxMP += rand(18, 20);
						}
					} else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
						MaxMP += rand(10, 12);
					} else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
						MaxMP += rand(10, 12);
					} else if (c.getPlayer().getJob().isA(MapleJob.PIRATE)) {
						MaxMP += rand(10, 12);
					}
					MaxMP = Math.min(30000, MaxMP);
					c.getPlayer().setMpApUsed(c.getPlayer().getMpApUsed() + 1);
					c.getPlayer().setMaxMp(MaxMP);
					statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, MaxMP));
					break;
				default:
					c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
					return;
			}
			c.getPlayer().setRemainingAp(c.getPlayer().getRemainingAp() - 1);
			statupdate.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, c.getPlayer().getRemainingAp()));
			c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true));
		}
	}
	
	private static int rand(int lbound, int ubound) {
		return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
	}
}
