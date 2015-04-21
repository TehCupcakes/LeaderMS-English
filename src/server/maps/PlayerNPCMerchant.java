/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * This file is part of the "Renoria" Game.
 * Copyright (C) 2008
 * IDGames.
 */

package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import tools.MaplePacketCreator;

/**
 *
 * @author David
 */
public class PlayerNPCMerchant extends AbstractMapleMapObject {
	private String name;
	private int ownerId;
	private int npcid;
	private int hair, face, skin;
	private int hat, top, bottom, shoes, earring, weapon, mask, glove, cape, shield, eyes;
	private MapleNPC npc;
	
	public PlayerNPCMerchant(MapleCharacter owner, int npcid) {
		this.npcid = npcid;
                this.npc = MapleLifeFactory.getNPC(npcid);
                this.npc.setPosition(owner.getPosition());
                this.npc.setFh(owner.getMap().getFootholds().findBelow(owner.getPosition()).getId());
                this.npc.setCy(owner.getPosition().y);
                this.npc.setRx0(owner.getPosition().x + 50);
                this.npc.setRx1(owner.getPosition().x - 50);
		this.setPosition(owner.getPosition());
                
		update(owner);
		owner.getMap().addMapObject(this);
		owner.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(this.npc));
                owner.getMap().broadcastMessage(MaplePacketCreator.getPlayerNPC(this));
	}
	
	public void update(MapleCharacter owner) {
		hat = top = bottom = shoes = earring = weapon = mask = glove = cape = shield = eyes = -1;
		this.name = owner.getName();
		this.hair = owner.getHair();
		this.face = owner.getFace();
		this.skin = owner.getSkinColor().getId();
		MapleInventory inv = owner.getInventory(MapleInventoryType.EQUIPPED);
		if (inv.getItem((byte) -1) != null) {
			this.hat = inv.getItem((byte) -1).getItemId();
		}
		if (inv.getItem((byte) -2) != null) {
			this.mask = inv.getItem((byte) -2).getItemId();
		}
		if (inv.getItem((byte) -3) != null) {
			this.eyes = inv.getItem((byte) -3).getItemId();
		}
		if (inv.getItem((byte) -4) != null) {
			this.earring = inv.getItem((byte) -4).getItemId();
		}
		if (inv.getItem((byte) -5) != null) {
			this.top = inv.getItem((byte) -5).getItemId();
		}
		if (inv.getItem((byte) -6) != null) {
			this.bottom = inv.getItem((byte) -6).getItemId();
		}
		if (inv.getItem((byte) -7) != null) {
			this.shoes = inv.getItem((byte) -7).getItemId();
		}
		if (inv.getItem((byte) -8) != null) {
			this.glove = inv.getItem((byte) -8).getItemId();
		}
		if (inv.getItem((byte) -9) != null) {
			this.cape = inv.getItem((byte) -9).getItemId();
		}
		if (inv.getItem((byte) -10) != null) {
			this.shield = inv.getItem((byte) -10).getItemId();
		}
		if (inv.getItem((byte) -11) != null) {
			this.weapon = inv.getItem((byte) -11).getItemId();
		}
		/** Neckson cash **/
		if (inv.getItem((byte) -101) != null) {
			this.hat = inv.getItem((byte) -101).getItemId();
		}
		if (inv.getItem((byte) -102) != null) {
			this.mask = inv.getItem((byte) -102).getItemId();
		}
		if (inv.getItem((byte) -103) != null) {
			this.eyes = inv.getItem((byte) -103).getItemId();
		}
		if (inv.getItem((byte) -104) != null) {
			this.earring = inv.getItem((byte) -104).getItemId();
		}
		if (inv.getItem((byte) -105) != null) {
			this.top = inv.getItem((byte) -105).getItemId();
		}
		if (inv.getItem((byte) -106) != null) {
			this.bottom = inv.getItem((byte) -106).getItemId();
		}
		if (inv.getItem((byte) -107) != null) {
			this.shoes = inv.getItem((byte) -107).getItemId();
		}
		if (inv.getItem((byte) -108) != null) {
			this.glove = inv.getItem((byte) -108).getItemId();
		}
		if (inv.getItem((byte) -109) != null) {
			this.cape = inv.getItem((byte) -109).getItemId();
		}
		if (inv.getItem((byte) -110) != null) {
			this.shield = inv.getItem((byte) -110).getItemId();
		}
		if (inv.getItem((byte) -111) != null) {
			this.weapon = inv.getItem((byte) -111).getItemId();
		}
	}

	public int getBottom() {
		return bottom;
	}

	public int getCape() {
		return cape;
	}

	public int getEarring() {
		return earring;
	}

	public int getEyes() {
		return eyes;
	}

	public int getFace() {
		return face;
	}

	public int getGlove() {
		return glove;
	}

	public int getHair() {
		return hair;
	}

	public int getHat() {
		return hat;
	}

	public int getMask() {
		return mask;
	}

	public int getNpcId() {
		return npcid;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public int getShield() {
		return shield;
	}

	public int getShoes() {
		return shoes;
	}

	public int getSkin() {
		return skin;
	}

	public int getTop() {
		return top;
	}

	public int getWeapon() {
		return weapon;
	}

	public String getName() {
		return name;
	}
	
	public MapleNPC getNPC() {
		return this.npc;
	}

	@Override
	public MapleMapObjectType getType() {
		return MapleMapObjectType.PLAYER_NPC_MERCHANT;
	}

	public void sendSpawnData(MapleClient client) {
		client.getSession().write(MaplePacketCreator.spawnNPC(this.npc));
		client.getSession().write(MaplePacketCreator.getPlayerNPC(this));
	}

	public void sendDestroyData(MapleClient client) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
