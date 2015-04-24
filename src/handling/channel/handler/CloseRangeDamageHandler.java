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
import client.ISkill;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.SkillFactory;
import server.MapleStatEffect;
import tools.packet.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CloseRangeDamageHandler extends AbstractDealDamageHandler {

	private boolean isFinisher(int skillId) {
		return skillId >= 1111003 && skillId <= 1111006;
	}

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if(c.getPlayer().getMap().getDisableDamage() && !c.getPlayer().isGM())
        {
            c.getSession().write(MaplePacketCreator.serverNotice(5, "Attacking here is disabled."));
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
		AttackInfo attack = parseDamage(slea, false);
		int skillId = attack.skill;
		ISkill skill = SkillFactory.getSkill(skillId);
		MapleCharacter player = c.getPlayer();
		MapleStatEffect aef = attack.getAttackEffect(player);
                ISkill energycharge = SkillFactory.getSkill(5110001);
                int energyChargeSkillLevel = player.getSkillLevel(energycharge);

		//cooldowns
		if (skillId != 0 && skill.getEffect(c.getPlayer().getSkillLevel(skill)).getCooldown() > 0) {
			if (player.skillisCooling(skillId)) return;
			player.checkCoolDown(skill);
		}

		player.getMap().broadcastMessage(player, MaplePacketCreator.closeRangeAttack(player.getId(), skillId, attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.speed), false, true);

		// handle combo orbconsume
        int numFinisherOrbs = 0;
        Integer comboBuff = player.getBuffedValue(MapleBuffStat.COMBO);
        if (isFinisher(attack.skill)) {
            if (comboBuff != null) {
                numFinisherOrbs = comboBuff.intValue() - 1;
            }
            player.handleOrbconsume();
        } else if (attack.numAttacked > 0 && comboBuff != null) {
            // handle combo orbgain
            if (attack.skill != 1111008) { // shout should not give orbs
                player.handleOrbgain();
            }
        }
        if (energyChargeSkillLevel > 0 && player.getJob().equals(MapleJob.BUCCANEER) || player.getJob().equals(MapleJob.MARAUDER)) {
            for (int i = 0; i < attack.numAttacked; i++) {
                player.handleEnergyChargeGain();
            }
        }
		
            // Handle sacrifice hp loss.
                if (attack.numAttacked > 0 && attack.skill == 1311005) {
                int totDamageToOneMonster = attack.allDamage.get(0).getRight().get(0).intValue(); // sacrifice attacks only 1 mob with 1 attack
                int remainingHP = player.getHp() - totDamageToOneMonster * attack.getAttackEffect(player).getX() / 100;
                if (remainingHP > 1) {
                player.setHp(remainingHP);
                 } else {
                 player.setHp(1);
                 }
                 player.updateSingleStat(MapleStat.HP, player.getHp());
               }
		// handle charged blow
		if (attack.numAttacked > 0 && attack.skill == 1211002) {
                boolean advcharge_prob = false;
                int advcharge_level = player.getSkillLevel(SkillFactory.getSkill(1220010));
                if (advcharge_level > 0) {
                MapleStatEffect advcharge_effect = SkillFactory.getSkill(1220010).getEffect(advcharge_level);
                advcharge_prob = advcharge_effect.makeChanceResult();
                } else {
                advcharge_prob = false;
                }
                if (!advcharge_prob) {
                player.cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
                  }
                }
		int maxdamage = player.getCurrentMaxBaseDamage();
		int attackCount = 1;
		if (skillId != 0) {
			MapleStatEffect effect = attack.getAttackEffect(player);
			attackCount = effect.getAttackCount();
			maxdamage *= effect.getDamage() / 100.0;
			maxdamage *= attackCount;
		}
	 maxdamage = Math.min(maxdamage, 99999);
        if (skillId == 4211006) {
            maxdamage = 700000;
        } else if (numFinisherOrbs > 0) {
            maxdamage *= numFinisherOrbs;
        } else if (comboBuff != null) {
            ISkill combo = SkillFactory.getSkill(1111002);
            maxdamage *= (double) 1.0 + (combo.getEffect(player.getSkillLevel(combo)).getDamage() / 100.0 - 1.0) * (comboBuff.intValue() - 1);
        }
        if (numFinisherOrbs == 0 && isFinisher(skillId)) {
            return; // can only happen when lagging o.o
        }
        if (isFinisher(skillId)) {
            maxdamage = 99999; // FIXME reenable damage calculation for finishers
        }
        applyAttack(attack, player, maxdamage, attackCount);
    }
}
