/* 
 * This file is part of the OdinMS Maple Story Server
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

/* 
 * @Author Lerk
 * 
 * Bulletin Board, Victoria Road: Excavation Site<Camp> (101030104) AND Sharenian: Excavation Site (990000000)
 * 
 * Start of Guild Quest
 */

function start() {
	cm.sendOk("<Notice> \r\n Are you part of a guild that has a lot of courage and confidence? Then take the Guild Quest and challenge yourself!\r\n\r\n#b To participate:#k\r\n1. The Guild must consist of at least 6 people!\r\n2. The leader of the group must be a Master or a Jr. Master in the Guild!\r\n3. The Guild Quest may end earlier if the number of guild members participating is below 6, or if the leader decides to end early!");
        cm.dispose();
}

function action(mode, type, selection) {
        
}