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


import client.ISkill;
import client.inventory.Item;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.status.MonsterStatus;
import client.inventory.MapleInventoryType;
import client.MapleStat;
import client.Skill;
import java.awt.Point;
import java.util.List;
import handling.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import server.life.MapleLifeFactory.loseItem;
import server.life.MapleMonster;
import server.life.MobAttackInfo;
import server.life.MobAttackInfoFactory;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

public class TakeDamageHandler extends AbstractMaplePacketHandler {
	    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TakeDamageHandler.class);

    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
      MapleCharacter player = c.getPlayer();
        slea.readInt();
        int damagefrom = slea.readByte();
        slea.readByte();
        int damage = slea.readInt();
        int oid = 0;
        int monsteridfrom = 0;
        int pgmr = 0;
        int direction = 0;
        int pos_x = 0;
        int pos_y = 0;
        int fake = 0;
        boolean is_pgmr = false;
        boolean is_pg = true;
        int mpattack = 0;
        MapleMonster attacker = null;
        if (damagefrom != -2) {
            monsteridfrom = slea.readInt();
            oid = slea.readInt();
            attacker = (MapleMonster) player.getMap().getMapObject(oid);
            if ((player.getMap().getMonsterById(monsteridfrom) == null || attacker == null) && monsteridfrom != 9300166) {
                return;
            } else if (monsteridfrom == 9300166) {
                if (player.haveItem(4031868)) {
                    if (player.getItemQuantity(4031868, false) > 1) {
                        int amount = player.getItemQuantity(4031868, false) / 2;
                        Point position = new Point(c.getPlayer().getPosition().x, c.getPlayer().getPosition().y);
                        MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(4031868), 4031868, amount, false, false);
                        for (int i = 0; i < amount; i++) {
                            position.setLocation(c.getPlayer().getPosition().x + (i % 2 == 0 ? (i * 15) : (-i * 15)), c.getPlayer().getPosition().y);
                            c.getPlayer().getMap().spawnItemDrop(c.getPlayer().getObjectId(), c.getPlayer().getPosition(), c.getPlayer(), new Item(4031868, (byte) 0, (short) 1), position, true, true);
                        }
                    } else {
                        MapleInventoryManipulator.removeById(c, MapleItemInformationProvider.getInstance().getInventoryType(4031868), 4031868, 1, false, false);
                        c.getPlayer().getMap().spawnItemDrop(c.getPlayer().getObjectId(), c.getPlayer().getPosition(), c.getPlayer(), new Item(4031868, (byte) 0, (short) 1), c.getPlayer().getPosition(), true, true);
                    }
                }
            }
            direction = slea.readByte();
        }
         if (damagefrom != -1 && damagefrom != -2 && attacker != null) {
            MobAttackInfo attackInfo = MobAttackInfoFactory.getMobAttackInfo(attacker, damagefrom);
            if (attackInfo.isDeadlyAttack()) {
                mpattack = player.getMp() - 1;
            }
            mpattack += attackInfo.getMpBurn();
            MobSkill skill = MobSkillFactory.getMobSkill(attackInfo.getDiseaseSkill(), attackInfo.getDiseaseLevel());
             if (skill != null && damage > 0) {
                skill.applyEffect(player, attacker, false);
             }
             if (attacker != null) {
                attacker.setMp(attacker.getMp() - attackInfo.getMpCon());
                if (player.getBuffedValue(MapleBuffStat.MANA_REFLECTION) != null && damage > 0 && !attacker.isBoss()) {
                    int jobid = player.getJob().getId();
                    if (jobid == 212 || jobid == 222 || jobid == 232) {
                        int id = jobid * 10000 + 2;
                        ISkill manaReflectSkill = SkillFactory.getSkill(id);
                        if (player.isBuffFrom(MapleBuffStat.MANA_REFLECTION, manaReflectSkill) && player.getSkillLevel(manaReflectSkill) > 0 && manaReflectSkill.getEffect(player.getSkillLevel(manaReflectSkill)).makeChanceResult()) {
                            int bouncedamage = (damage * manaReflectSkill.getEffect(player.getSkillLevel(manaReflectSkill)).getX() / 100);
                            if (bouncedamage > attacker.getMaxHp() / 5) {
                                bouncedamage = attacker.getMaxHp() / 5;
                            }
                            player.getMap().damageMonster(player, attacker, bouncedamage);
                            player.getMap().broadcastMessage(player, MaplePacketCreator.damageMonster(oid, bouncedamage), true);
                            player.getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(id, 5));
                            player.getMap().broadcastMessage(player, MaplePacketCreator.showBuffeffect(player.getId(), id, 5), false);
                        }
                    }
                }
            }
        }
         if (damage == -1) {
            fake = 4020002 + (player.getJob().getId() / 10 - 40) * 100000;
       }
       if (damage > 0 && !player.isHidden()) {
            if (attacker != null && !attacker.isBoss()) {
                if (damagefrom == -1 && player.getBuffedValue(MapleBuffStat.POWERGUARD) != null) {
                    int bouncedamage = (int) (damage * (player.getBuffedValue(MapleBuffStat.POWERGUARD).doubleValue() / 100));
                    bouncedamage = Math.min(bouncedamage, attacker.getMaxHp() / 10);
                    player.getMap().damageMonster(player, attacker, bouncedamage);
                    damage -= bouncedamage;
                    player.getMap().broadcastMessage(player, MaplePacketCreator.damageMonster(oid, bouncedamage), true, true);
                    player.checkMonsterAggro(attacker);
                }
            }
            if (damagefrom != -2) {
                int achilles = 0;
                ISkill achilles1 = null;
                int jobid = player.getJob().getId();
                if (jobid < 200 && jobid % 10 == 2) {
                    achilles1 = SkillFactory.getSkill(jobid * 10000 + jobid == 112 ? 4 : 5);
                    achilles = player.getSkillLevel(achilles);
                }
                if (achilles != 0 && achilles1 != null) {
                    damage *= (int) (achilles1.getEffect(achilles).getX() / 1000.0 * damage);
                }
            }
            Integer mesoguard = player.getBuffedValue(MapleBuffStat.MESOGUARD);
            if (player.getBuffedValue(MapleBuffStat.MAGIC_GUARD) != null && mpattack == 0) {
                int mploss = (int) (damage * (player.getBuffedValue(MapleBuffStat.MAGIC_GUARD).doubleValue() / 100.0));
                int hploss = damage - mploss;
                if (mploss > player.getMp()) {
                    hploss += mploss - player.getMp();
                    mploss = player.getMp();
                }
                player.addMPHP(-hploss, -mploss);
            } else if (mesoguard != null) {
                damage = Math.round(damage / 2);
                int mesoloss = (int) (damage * (mesoguard.doubleValue() / 100.0));
                if (player.getMeso() < mesoloss) {
                    player.gainMeso(-player.getMeso(), false);
                    player.cancelBuffStats(MapleBuffStat.MESOGUARD);
                } else {
                    player.gainMeso(-mesoloss, false);
                }
                player.addMPHP(-damage, -mpattack);
            } else if (player.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                if (player.getBuffedValue(MapleBuffStat.MONSTER_RIDING) == 5221999) {
                    player.decreaseBattleshipHp(damage);
                } else {
                    player.addMPHP(-damage, -mpattack);
                }
            } else {
                player.addMPHP(-damage, -mpattack);
            } 
        }
        if (!player.isHidden()) {
            player.getMap().broadcastMessage(player, MaplePacketCreator.damagePlayer(damagefrom, monsteridfrom, player.getId(), damage, fake, direction, is_pgmr, pgmr, is_pg, oid, pos_x, pos_y), false);
            player.updateSingleStat(MapleStat.HP, player.getHp());
            player.updateSingleStat(MapleStat.MP, player.getMp());
            player.checkBerserk();
        }
    }
}

