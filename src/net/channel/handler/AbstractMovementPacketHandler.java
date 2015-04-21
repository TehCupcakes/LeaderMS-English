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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import net.AbstractMaplePacketHandler;
import server.maps.AnimatedMapleMapObject;
import server.movement.AbsoluteLifeMovement;
import server.movement.ChairMovement;
import server.movement.ChangeEquipSpecialAwesome;
import server.movement.JumpDownMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import server.movement.RelativeLifeMovement;
import server.movement.TeleportMovement;
import tools.data.input.LittleEndianAccessor;


public abstract class AbstractMovementPacketHandler extends AbstractMaplePacketHandler {

//	private static Logger log = LoggerFactory.getLogger(AbstractMovementPacketHandler.class);

	protected List<LifeMovementFragment> parseMovement(LittleEndianAccessor lea) {
		List<LifeMovementFragment> res = new ArrayList<LifeMovementFragment>();
		int numCommands = lea.readByte();
		for (int i = 0; i < numCommands; i++) {
			int command = lea.readByte();
			switch (command) {
				case 0: // normal move
				case 5:
				case 17: // Float
				{
					int xpos = lea.readShort();
					int ypos = lea.readShort();
					int xwobble = lea.readShort();
					int ywobble = lea.readShort();
					int unk = lea.readShort();
					int newstate = lea.readByte();
					int duration = lea.readShort();
					AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
					alm.setUnk(unk);
					alm.setPixelsPerSecond(new Point(xwobble, ywobble));
					// log.trace("Move to {},{} command {} wobble {},{} ? {} state {} duration {}", new Object[] { xpos,
					// xpos, command, xwobble, ywobble, newstate, duration });
					res.add(alm);
					break;
				}
				case 1:
				case 2:
				case 6: // fj
				case 12:
				case 13: // Shot-jump-back thing
				case 16: // Float
				{
					int xmod = lea.readShort();
					int ymod = lea.readShort();
					int newstate = lea.readByte();
					int duration = lea.readShort();
					RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xmod, ymod), duration, newstate);
					res.add(rlm);
					// log.trace("Relative move {},{} state {}, duration {}", new Object[] { xmod, ymod, newstate,
					// duration });
					break;
				}
				case 3:
				case 4: // tele... -.-
				case 7: // assaulter
				case 8: // assassinate
				case 9: // rush
				case 14: {
					int xpos = lea.readShort();
					int ypos = lea.readShort();
					int xwobble = lea.readShort();
					int ywobble = lea.readShort();
					int newstate = lea.readByte();
					TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), newstate);
					tm.setPixelsPerSecond(new Point(xwobble, ywobble));
					res.add(tm);
					break;
				}
				case 10: // change equip ???
				{
					res.add(new ChangeEquipSpecialAwesome(lea.readByte()));
					break;
				}
				case 11: // chair
				{
					int xpos = lea.readShort();
					int ypos = lea.readShort();
					int unk = lea.readShort();
					int newstate = lea.readByte();
					int duration = lea.readShort();
					ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
					cm.setUnk(unk);
					res.add(cm);
					break;
				}
				case 15:
				{
					int xpos = lea.readShort();
					int ypos = lea.readShort();
					int xwobble = lea.readShort();
					int ywobble = lea.readShort();
					int unk = lea.readShort();
					int fh = lea.readShort();
					int newstate = lea.readByte();
					int duration = lea.readShort();
					JumpDownMovement jdm = new JumpDownMovement(command, new Point(xpos, ypos), duration, newstate);
					jdm.setUnk(unk);
					jdm.setPixelsPerSecond(new Point(xwobble, ywobble));
					jdm.setFH(fh);
					// log.trace("Move to {},{} command {} wobble {},{} ? {} state {} duration {}", new Object[] { xpos,
					// xpos, command, xwobble, ywobble, newstate, duration });
					res.add(jdm);
					break;
				}
				default: {
//					log.warn("Unhandeled movement command {} received", command);
//					log.warn("Movement packet: {}", lea.toString());
					return null;
				}
			}
		}
		if (numCommands != res.size()) {
//			log.warn("numCommands ({}) does not match the number of deserialized movement commands ({})", numCommands, res.size());
		}
		return res;
	}

	protected void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
		for (LifeMovementFragment move : movement) {
			if (move instanceof LifeMovement) {
				if (move instanceof AbsoluteLifeMovement) {
					Point position = ((LifeMovement) move).getPosition();
					position.y += yoffset;
					target.setPosition(position);
				}
				target.setStance(((LifeMovement) move).getNewstate());
			}
		}
	}
}