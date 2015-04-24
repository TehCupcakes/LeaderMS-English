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

import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import server.MapleStatEffect;
import tools.packet.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class MagicDamageHandler extends AbstractDealDamageHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if(c.getPlayer().getMap().getDisableDamage() && !c.getPlayer().isGM())
        {
            c.getSession().write(MaplePacketCreator.serverNotice(5, "Attacking is currently disabled."));
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
		AttackInfo attack = parseDamage(slea, false);
		int skillId = attack.skill;
		ISkill skill = SkillFactory.getSkill(skillId);
		MapleCharacter player = c.getPlayer();

		//cooldowns
		if (skillId != 0 && skill.getEffect(player.getSkillLevel(skill)).getCooldown() > 0) {
			if (player.skillisCooling(skillId)) return;
			player.checkCoolDown(skill);
		}

		int charge = -1;
		switch (skillId) {
			case 2121001:
			case 2221001:
			case 2321001: charge = attack.charge; break;
		}
		player.getMap().broadcastMessage(player, MaplePacketCreator.magicAttack(player.getId(), skillId, attack.stance, attack.numAttackedAndDamage, attack.allDamage, charge, attack.speed), false, true);

		// TODO fix magic damage calculation
		// Any Weapon: {[(MAG * 0.8) + (LUK/4)]/18} * Spell Magic Attack * 0.8 * Mastery -- Taken from HiddenStreet
		int maxdamage = (int) (((player.getTotalMagic() * 0.8) + (player.getLuk() / 4) / 18) * skill.getEffect(player.getSkillLevel(skill)).getDamage() * 0.8 * (player.getMasterLevel(skill) * 10 / 100));
		// For criticals we skip to 99999 cause we are to lazy to find magic
		if (attack.numDamage > maxdamage) maxdamage = 99999;
                
                MapleStatEffect aef = attack.getAttackEffect(player);
                
		applyAttack(attack, player, maxdamage, attack.getAttackEffect(player).getAttackCount());

		// MP Eater
		for (int i = 1; i <= 3; i++) {
			ISkill eaterSkill = SkillFactory.getSkill(2000000 + i * 100000);
			int eaterLevel = player.getSkillLevel(eaterSkill);
			if (eaterLevel > 0) {
				for (Pair<Integer, List<Integer>> singleDamage : attack.allDamage) {
					eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getMapObject(singleDamage.getLeft()), 0);
				}
				break;
			}
		}
	}
}