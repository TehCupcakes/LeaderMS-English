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

package client.messages.commands;

import static client.messages.CommandProcessor.getNamedDoubleArg;
import static client.messages.CommandProcessor.getNamedIntArg;
import static client.messages.CommandProcessor.getOptionalIntArg;
import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import config.configuration.Configuration;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import tools.MaplePacketCreator;
public class SpawnMonsterCommand implements Command {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpawnMonsterCommand.class);

	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		int mid = Integer.parseInt(splitted[1]);
		int num = Math.min(getOptionalIntArg(splitted, 2, 1), 500);
                
                if(c.getPlayer().getGMLevel() <= 3 && c.getChannelServer().eventOn != true) {
                    c.getSession().write(MaplePacketCreator.serverNotice(5, "This command is only available during events."));
                    return;
                 }
                
		if (splitted.length > 3 && !c.getPlayer().isInvincible()) {
			mc.dropMessage(Configuration.Server_Name + " owns you.");
			return;
		}

		Integer hp = getNamedIntArg(splitted, 1, "hp");
		Integer exp = getNamedIntArg(splitted, 1, "exp");
		Double php = getNamedDoubleArg(splitted, 1, "php");
		Double pexp = getNamedDoubleArg(splitted, 1, "pexp");

		MapleMonster onemob = MapleLifeFactory.getMonster(mid);

		int newhp = 0;
		int newexp = 0;

		double oldExpRatio = ((double) onemob.getHp() / onemob.getExp());

		if (hp != null) {
			newhp = hp.intValue();
		} else if (php != null) {
			newhp = (int) (onemob.getMaxHp() * (php.doubleValue() / 100));
		} else {
			newhp = onemob.getMaxHp();
		}
		if (exp != null) {
			newexp = exp.intValue();
		} else if (pexp != null) {
			newexp = (int) (onemob.getExp() * (pexp.doubleValue() / 100));
		} else {
			newexp = onemob.getExp();
		}

		if (newhp < 1) {
			newhp = 1;
		}
		double newExpRatio = ((double) newhp / newexp);
		if (newExpRatio < oldExpRatio && newexp > 0) {
			mc.dropMessage("The new hp/exp ratio is better than the old one. (" + newExpRatio + " < " + oldExpRatio + ")");
                        mc.dropMessage("Please consider that you're spawning a better monster than the original.");
			//return; We don't want to quit, just want to warn the GM so he/she can do !killall if messed up
		}
		
		MapleMonsterStats overrideStats = new MapleMonsterStats();
		overrideStats.setHp(newhp);
		overrideStats.setExp(newexp);
		overrideStats.setMp(onemob.getMaxMp());
		
		for (int i = 0; i < num; i++) {
			MapleMonster mob = MapleLifeFactory.getMonster(mid);
			mob.setHp(newhp);
			mob.setOverrideStats(overrideStats);
			c.getPlayer().getMap().spawnMonsterOnGroundBelow(mob, c.getPlayer().getPosition());
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("spawn", "monsterid [hp newHp] [exp newExp] [php procentual Hp] [pexp procentual Exp]", "Spawns the monster with the given id", 2),
		};
	}
}
