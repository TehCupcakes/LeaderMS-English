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
package client.inventory;

//import java.util.LinkedList;
import client.MapleCharacter;
import java.util.concurrent.ScheduledFuture;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 *
 * @author Patrick/PurpleMadness
 */
public class MapleMount {

    private int itemid;
    private int skillid;
    private int tiredness;
    private int exp;
    private int level;
    private ScheduledFuture<?> tirednessSchedule;
    private MapleCharacter owner;
    private boolean active;

    public MapleMount(MapleCharacter owner, int id, int skillid) {
        this.itemid = id;
        this.skillid = skillid;
        this.tiredness = 0;
        this.level = 1;
        this.exp = 0;
        this.owner = owner;
        active = true;
    }

    public int getItemId() {
        return itemid;
    }

    public int getSkillId() {
        return skillid;
    }

    public int getId() { //FIXME: this is a crappy way to do this, will mess up when mounts are added to maple from new patches
        switch (this.itemid) {
            case 1902000:
                return 1;
            case 1902001:
                return 2;
            case 1902002:
                return 3;
            case 1932000:
                return 4;
            case 1902008:
            case 1902009:
                return 5;
            default:
                return 0; //whatever, it won't get to this anyway unless there's a new patch that adds mounts
        }
    }

    public int getTiredness() {
        return tiredness;
    }

    public int getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public void setTiredness(int newtiredness) {
        this.tiredness = newtiredness;
        if (tiredness < 0) {
            tiredness = 0;
        }
    }

    public void increaseTiredness() {
        this.tiredness++;
        owner.getMap().broadcastMessage(MaplePacketCreator.updateMount(owner.getId(), this, false));
        if (tiredness > 100) {
            owner.dispelSkill(1004);
        }
    }

    public void setExp(int newexp) {
        this.exp = newexp;
    }

    public void setLevel(int newlevel) {
        this.level = newlevel;
    }

    public void setItemId(int newitemid) {
        this.itemid = newitemid;
    }

    public void setSkillId(int skillid) {
        this.skillid = skillid;
    }

    public void startSchedule() {
        this.tirednessSchedule = TimerManager.getInstance().register(new Runnable() {

            public void run() {
                increaseTiredness();
            }
        }, 60000, 60000);
    }

    public void cancelSchedule() {
        if (this.tirednessSchedule != null) {
            this.tirednessSchedule.cancel(false);
        }
    }

    public void setActive(boolean set) {
        active = set;
    }

    public boolean isActive() {
        return active;
    }

    public final void empty() {
        this.cancelSchedule();
        this.owner = null;
    }
}
