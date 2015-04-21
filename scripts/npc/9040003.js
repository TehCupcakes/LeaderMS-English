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
 * @Author TheRamon
 * 
 * Sharen III's Soul, Sharenian: Sharen III's Grave (990000700)
 * 
 * Guild Quest - end of stage 4
 */

 var status = 0;
function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
	status++;
    else {
	cm.dispose();
	return;
	}

    if (status == 0) {
	if (cm.getEventInstance().getProperty("leader").equals(cm.getPlayer().getName())) {
	    if (cm.getEventInstance().getProperty("stage4clear") != null && cm.getEventInstance().getProperty("stage4clear").equals("true")) {
			cm.sendOk("I thought it would be an eternal sleep. I finally found someone who will save Sharenian! I can truly rest in peace now.");
			cm.safeDispose();
	    } else {
			var prev = cm.getEventInstance().setProperty("stage4clear","true",true);
			if (prev == null) {
				cm.sendNext("I thought it would be an eternal sleep. I finally found someone who will save Sharenian! This will pave the way for you to complete the quest...");
			} else { 
				cm.sendOk("I thought it would be an eternal sleep. I finally found someone who will save Sharenian! I can truly rest in peace now.");
				cm.safeDispose();
			}
	    }
	} else {
	    if (cm.getEventInstance().getProperty("stage4clear") != null && cm.getEventInstance().getProperty("stage4clear").equals("true"))
		cm.sendOk("I thought it would be an eternal sleep. I finally found someone who will save Sharenian! I can truly rest in peace now.");
	    else
		cm.sendOk("Please tell the leader of your group to talk to me, no one else.");
	    cm.safeDispose();
	}
    } else if (status == 1) {
	cm.gainGP(180);
	cm.getMap().getReactorByName("ghostgate").hitReactor(cm.getC());
	cm.showEffect(true, "quest/party/clear");
	cm.playSound(true, "Party1/Clear");
	cm.dispose();
    }
}