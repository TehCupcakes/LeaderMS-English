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
import client.IItem;
import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import client.MapleJob;
import client.inventory.MapleWeaponType;
import client.SkillFactory;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class RangedAttackHandler extends AbstractDealDamageHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if(c.getPlayer().getMap().getDisableDamage() && !c.getPlayer().isGM())
        {
            c.getSession().write(MaplePacketCreator.serverNotice(5, "Attacking here is disabled."));
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
		AttackInfo attack = parseDamage(slea, true);
                
		int skillId = attack.skill;
		ISkill skill = SkillFactory.getSkill(skillId);
		MapleCharacter player = c.getPlayer();
		MapleStatEffect aef = attack.getAttackEffect(player);
		

		//cooldowns
		if (skillId != 0 && skill.getEffect(player.getSkillLevel(skill)).getCooldown() > 0) {
			if (player.skillisCooling(skillId)) return;
			player.checkCoolDown(skill);
		}
                
               if (attack.skill == 5221003 && attack.skill == 2121007 && attack.skill == 1311006 && attack.skill == 2221007 || attack.skill == 2321008 && player.getMapId() == 107000200) {
                player.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "[CashPQ] This skill is disabled on this map!"));
                return;
               }
 
		IItem weapon = player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
		MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
		MapleWeaponType type = mii.getWeaponType(weapon.getItemId());
		if (type == MapleWeaponType.NOT_A_WEAPON) throw new RuntimeException("Player " + player.getName() + " is attacking with something that is not a weapon.");

		int projectile = 0;
		int bulletCount = 1;
		MapleStatEffect effect = null;
		if (skillId != 0) {
			effect = attack.getAttackEffect(player);
			bulletCount = effect.getBulletCount();
		}

		boolean hasShadowPartner = player.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null;
		int damageBulletCount = bulletCount;
		if (hasShadowPartner) bulletCount *= 2;
		for (int i = 0; i < 255; i++) { // impose order...
			IItem item = player.getInventory(MapleInventoryType.USE).getItem((byte) i);
			if (item != null) {
				boolean clawCondition = type == MapleWeaponType.CLAW && mii.isThrowingStar(item.getItemId()) && weapon.getItemId() != 1472063;
				boolean bowCondition = type == MapleWeaponType.BOW && mii.isArrowForBow(item.getItemId());
				boolean crossbowCondition = type == MapleWeaponType.CROSSBOW && mii.isArrowForCrossBow(item.getItemId());
				boolean gunCondition = type == MapleWeaponType.GUN && mii.isBullet(item.getItemId());
				boolean mittenCondition = weapon.getItemId() == 1472063 && (mii.isArrowForBow(item.getItemId()) || mii.isArrowForCrossBow(item.getItemId()));
				if ((clawCondition || bowCondition || crossbowCondition || mittenCondition || gunCondition) && item.getQuantity() >= bulletCount) {
					projectile = item.getItemId(); break;
				}
			}
		}
		boolean soulArrow = player.getBuffedValue(MapleBuffStat.SOULARROW) != null;
		boolean shadowClaw = player.getBuffedValue(MapleBuffStat.SHADOW_CLAW) != null;
		if (!soulArrow && !shadowClaw && !c.getPlayer().isGM()) {
			int bulletConsume = bulletCount;
			if (effect != null && effect.getBulletConsume() != 0) {
				bulletConsume = effect.getBulletConsume() * (hasShadowPartner ? 2 : 1);
			}
			MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, projectile, bulletConsume, false, true);
		}

		if (projectile != 0 || soulArrow || attack.skill == 5121002) {
			int visProjectile = projectile; //visible projectile sent to players
			if (mii.isThrowingStar(projectile)) {
				//see if player has cash stars / cash stars have prefix 5021xxx
				for (int i = 0; i < 255; i++) { // impose order...
					IItem item = player.getInventory(MapleInventoryType.CASH).getItem((byte) i);
					if (item != null) {
						if (item.getItemId() / 1000 == 5021) visProjectile = item.getItemId(); break;
					}
				}
			} else if (soulArrow || skillId == 3111004 || skillId == 3211004) visProjectile = 0; //bow, crossbow //arrow rain/eruption show no arrows

			int stance = attack.stance;
			switch (skillId) {
				case 3121004: // Hurricane // Pierce // Rapid Fire
				case 3221001:
				case 5221004: stance = attack.direction; break;
			}
			player.getMap().broadcastMessage(player, MaplePacketCreator.rangedAttack(player.getId(), skillId, stance, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed), false, true);

			int basedamage;
			int projectileWatk = 0;
			int totalWatk = player.getTotalWatk();
			if (projectile != 0) projectileWatk = mii.getWatkForProjectile(projectile);
			if (skillId != 4001344) { // not lucky 7
				basedamage = (projectileWatk != 0) ? player.calculateMaxBaseDamage(totalWatk + projectileWatk) : player.getCurrentMaxBaseDamage();
			} else basedamage = (int) (((player.getTotalLuk() * 5.0) / 100.0) * (totalWatk + projectileWatk)); // l7 has a different formula :>
			if (skillId == 3101005) basedamage *= effect.getX() / 100.0; //arrowbomb

			double critdamagerate = 0.0;
			if (player.getJob().isA(MapleJob.ASSASSIN)) {
				ISkill criticalthrow = SkillFactory.getSkill(4100001);
				if (player.getSkillLevel(criticalthrow) > 0) critdamagerate = (criticalthrow.getEffect(player.getSkillLevel(criticalthrow)).getDamage() / 100.0);
			} else if (player.getJob().isA(MapleJob.BOWMAN)) {
				ISkill criticalshot = SkillFactory.getSkill(3000001);
				int critlevel = player.getSkillLevel(criticalshot);
				if (critlevel > 0) critdamagerate = (criticalshot.getEffect(critlevel).getDamage() / 100.0) - 1.0;
			}

			//maxdamage
			int maxdamage = basedamage;
			if (effect != null) maxdamage *= effect.getDamage() / 100.0;
			maxdamage += (int) (basedamage * critdamagerate);
			maxdamage *= damageBulletCount;
			if (hasShadowPartner) {
				ISkill shadowPartner = SkillFactory.getSkill(4111002);
				MapleStatEffect shadowPartnerEffect = shadowPartner.getEffect(player.getSkillLevel(shadowPartner));
				maxdamage *= (skillId != 0) ? (1.0 + shadowPartnerEffect.getY() / 100.0) : (1.0 + shadowPartnerEffect.getX() / 100.0);
			}
			if (skillId == 4111004) maxdamage = 35000;

			if (effect != null) {
				int money = effect.getMoneyCon();
				if (money != 0) {
					money = (int) (money + Math.random() * (money * 0.5));
					if (money > player.getMeso()) money = player.getMeso();
					player.gainMeso(-money, false);
				}
			}

			applyAttack(attack, player, maxdamage, bulletCount);
		}
	}
}