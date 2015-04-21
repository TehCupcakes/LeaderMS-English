package server.maps;

import java.awt.Point;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import tools.MaplePacketCreator;

public class MapleMapItem extends AbstractMapleMapObject {
    protected IItem item;
    protected MapleMapObject dropper;
    protected MapleCharacter owner;
    protected int meso;
    protected int displayMeso;
    protected int dropperId;
    protected Point dropperPos;
    protected boolean pickedUp = false;
    protected boolean ffa = true;

    /** Creates a new instance of MapleMapItem */
    public MapleMapItem(IItem item, Point position, MapleMapObject dropper, MapleCharacter owner) {
        setPosition(position);
        this.item = item;
        this.dropper = dropper;
        this.owner = owner;
        this.meso = 0;
    }

    public MapleMapItem(int meso, int displayMeso, Point position, MapleMapObject dropper, MapleCharacter owner) {
        setPosition(position);
        this.item = null;
        this.meso = meso;
        this.displayMeso = displayMeso;
        this.dropper = dropper;
        this.owner = owner;
    }

    public MapleMapItem(IItem item, Point position, int _dropperId, Point _dropperPos, MapleCharacter owner) {
        setPosition(position);
        this.item = item;
        this.dropperId = _dropperId;
        this.dropperPos = _dropperPos;
        this.owner = owner;
        this.meso = 0;
    }

    public IItem getItem() {
        return item;
    }

    public MapleMapObject getDropper() {
        return dropper;
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public int getMeso() {
        return meso;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removeItemFromMap(getObjectId(), 1, 0));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.ITEM;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (getMeso() > 0) {
            client.getSession().write(MaplePacketCreator.dropMesoFromMapObject(displayMeso, getObjectId(),
            getDropper().getObjectId(), getOwner().getId(), null, getPosition(), (byte) 2));
        } else {
            client.getSession().write(MaplePacketCreator.dropItemFromMapObject(getItem().getItemId(), getObjectId(),
            0, getOwner().getId(), null, getPosition(), (byte) 2));
        }
    }

	public boolean isFfa() {
		return ffa;
	}

	public void setFfa(boolean ffa) {
		this.ffa = ffa;
	}
}