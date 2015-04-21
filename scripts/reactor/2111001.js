/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
					   Matthias Butz <matze@odinms.de>
					   Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* @Author Lerk
 * 
 * 2111001.js: Zakum Altar - Summons Zakum.
 * 
 * Note that Zakum is currently spawned body + 8 arms at once, with no special handling for the body
 * before the arms are defeated. Use caution.
*/

function act() {
	rm.closeDoor(211042300); //activate the reactor at map 211042300 this map is where the door is 
	rm.changeMusic("Bgm06/FinalFight");
	rm.spawnMonster(8800000);
	rm.spawnMonster(8800003);
	rm.spawnMonster(8800004);
	rm.spawnMonster(8800005);
	rm.spawnMonster(8800006);
	rm.spawnMonster(8800007);
	rm.spawnMonster(8800008);
	rm.spawnMonster(8800009);
	rm.spawnMonster(8800010);
}