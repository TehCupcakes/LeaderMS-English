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
status = -1;
close = false;
oldSelection = -1;

function start() {
    var text = "Hello , I am the ticket gate.";
    if(cm.haveItem(4031713))
        text += " Which ticket do you want to use? You will be warped immediately.#b";
    else
        close = true;
    if(cm.haveItem(4031713))
        text += "\r\n#L0##t4031713#";
    if(close){
        cm.sendOk(text);
        cm.dispose();
    }else
        cm.sendSimple(text);
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(mode == 0)
            cm.sendNext("You must have some business to take care of here, right?");
        cm.dispose();
        return;
    }
    if (status == 0) {
        if(selection == 0){
            cm.sendYesNo("It seems like there is still plenty of room on this ride. Please keep your ticket ready so I can let you on. The journey may be long, but you will get to your destination safely. What do you think? Do you want to go on this ride?");
        }
        oldSelection = selection;
    } else if(status == 1){
        if(oldSelection == 0){
            cm.gainItem(4031713, -1);
            cm.warp(103000100);
        }
        cm.dispose();
    }
}