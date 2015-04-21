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

package client;

//import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class Item implements IItem {

    private int id;
    private byte position;
    private short quantity;
    private int petid;
    private String owner = "";
    protected List<String> log;
    private long expiration;

    public Item(int id, byte position, short quantity) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.petid = -1;
        this.log = new LinkedList<String>();
    }

    public Item(int id, byte position, short quantity, int petid) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.petid = petid;
        this.log = new LinkedList<String>();
    }

    public IItem copy() {
        Item ret = new Item(id, position, quantity, petid);
        ret.expiration = expiration;
        ret.owner = owner;
        ret.log = new LinkedList<String>(log);
        return ret;
    }

     public void setPosition(byte position) {
        this.position = position;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    @Override
    public int getItemId() {
        return id;
    }

    @Override
    public byte getPosition() {
        return position;
    }

    @Override
    public short getQuantity() {
        return quantity;
    }

    @Override
    public byte getType() {
        return IItem.ITEM;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public int getPetId() {
        return petid;
    }

    @Override
    public int compareTo(IItem other) {
        if (Math.abs(position) < Math.abs(other.getPosition())) {
            return -1;
        } else if (Math.abs(position) == Math.abs(other.getPosition())) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "Item: " + id + " quantity: " + quantity;
    }

    public List<String> getLog() {
        return Collections.unmodifiableList(log);
    }

    public void log(String msg, boolean fromDB) {
    }

    @Override
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expire) {
        this.expiration = expire;
    }
}
