package server.PlayerInteraction;

import client.IItem;

public class MaplePlayerShopItem {

    private IItem item;
    private short bundles;
    private short perBundle;
    private int price;
    private boolean doesExist; 
    
    public MaplePlayerShopItem(IItem item, short bundles, int price) {
        this.item = item;
        this.bundles = bundles;
        this.price = price;
        this.doesExist = true; 
    }

    public IItem getItem() {
        return item;
    }

    public short getBundles() {
        return bundles;
    }

    public int getPrice() {
        return price;
    }
    
        public boolean isExist() { 
        return doesExist; 
    } 

    public void setBundles(short bundles) {
        this.bundles = bundles;
    }

    public void setDoesExist(boolean tf) {
        this.doesExist = tf;
    }

   public int getPerBundles() {
        return perBundle;
    }
}