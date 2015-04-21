/*
 * JavaScriptz (javascriptz@leaderms.com.br)
 * LeaderMS 2012 ▬ 2015
 * Brasil MapleStory Server
 * Compras CS / Sistema LeaderPoints
 * www.leaderms.com.br
 */


package net.channel.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import client.IItem;
import client.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.MapleRing;
import config.configuration.Configuration;
import database.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import server.AutobanManager;
import server.CashItemFactory;
import server.CashItemInfo;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class BuyCSItemHandler extends AbstractMaplePacketHandler {

    private static final boolean ringsEnabled = false;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");

    
    private void updateInformation(MapleClient c, int item) {
        CashItemInfo Item = CashItemFactory.getItem(item);
        c.getSession().write(MaplePacketCreator.showBoughtCSItem(Item.getId()));
        updateInformation(c);
    }

    private void updateInformation(MapleClient c) {
        c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(MaplePacketCreator.enableCSUse0());
        c.getSession().write(MaplePacketCreator.enableCSUse1());
        c.getSession().write(MaplePacketCreator.enableCSUse2());
        c.getSession().write(MaplePacketCreator.enableCSUse3());
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int action = slea.readByte();
        if (action == 3) {
            slea.skip(1);
            int useNX = slea.readInt();
            int snCS = slea.readInt();
            CashItemInfo item = CashItemFactory.getItem(snCS);
            int itemID = item.getId();
            if (contains(Configuration.BLOCKED_CS, item.getId())) {
            c.getSession().write(MaplePacketCreator.serverNotice(1, "This item is now blocked from the Cash Shop!"));
            updateInformation(c);
            return;
            } if (!MapleInventoryManipulator.checkSpace(c, itemID, item.getCount(), "")) {
                c.getPlayer().dropMessage(1, "Your inventory is full! Check to make sure you have space.");
                updateInformation(c);
                return; 
            } if (useNX != 2 && item.getDonor() == 1) {
                c.getPlayer().dropMessage(1, "Buy only with LeaderPoints!");
                updateInformation(c);
                return;
            } if (itemID == 5200000) {
                c.getSession().write(MaplePacketCreator.enableActions());
                AutobanManager.getInstance().autoban(c, "Buying item that does not exist in Cash Shop!");
                return;
            } if (c.getPlayer().getCSPoints(useNX) >= item.getPrice()) {
                c.getPlayer().modifyCSPoints(useNX, -item.getPrice());
            } else {
                c.getSession().write(MaplePacketCreator.enableActions());
                AutobanManager.getInstance().autoban(c, "Trying to buy in Cash Shop without NX!");
                return;
            } if (itemID >= 5000000 && itemID <= 5000370) {
                int petId = MaplePet.createPet(itemID);
                if (petId == -1) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                  MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), "Cash item was purchased!", null, MaplePet.createPet(item.getId()));
            } else {
                  MapleInventoryManipulator.addFromDrop(c, getItemFromCashInfo(item), null);
                  FilePrinter.printCashShop(c.getPlayer().getName() + ".txt", "Purchased Item: " + item.getId() + "\r\nValue: " + item.getPrice() + "\r\nRemaining Cash: (PayPal) " + c.getPlayer().getCSPoints(1) + "/ (MaplePoints) " + c.getPlayer().getCSPoints(2) + "/ (CardCash) " + c.getPlayer().getCSPoints(4) + "\r\nOn day: " + sdf.format(Calendar.getInstance().getTime()) + " at " + sdf2.format(Calendar.getInstance().getTime()) + ".");
            }
            updateInformation(c, snCS);
        } else if (action == 4) {
            if (checkBirthday(c, slea.readInt())) {
                int SN = slea.readInt();
                CashItemInfo item = CashItemFactory.getItem(SN);
                String recipient = slea.readMapleAsciiString();
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                String text = slea.readMapleAsciiString();
                if (victim != null) {
                    if (item == null || c.getPlayer().getCSPoints(4) < item.getPrice() || text.length() > 73 || text.length() < 1) { //dont want packet editors gifting random stuff =P
                    c.getPlayer().dropMessage(1, "You do not have NX!");
                    updateInformation(c);
                    return; 
                 } if (item.getId() == 5220000) {
                     c.getPlayer().dropMessage(1, "This item is not available for gifting.");
                     updateInformation(c);
                     return;
                 } if (!MapleInventoryManipulator.checkSpace(victim.getClient(), item.getId(), item.getCount(), "")) {
                    c.getPlayer().dropMessage(1, "Your inventory is full! Check to make sure you have space.");
                    updateInformation(c);
                    return; 
                   } if (item.getId() >= 5000000 && item.getId() <= 5000370) {
                    int petId = MaplePet.createPet(item.getId());
                    if (petId == -1) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                    }
                     c.getPlayer().dropMessage(1, "Gift successfully sent!");
                     MapleInventoryManipulator.addById(victim.getClient(), item.getId(), (short) item.getCount(), "", null, MaplePet.createPet(item.getId()));
                     c.getPlayer().modifyCSPoints(4, -item.getPrice());
                     updateInformation(c);
                     } else {
                     c.getPlayer().dropMessage(1, "Gift successfully sent!");
                     MapleInventoryManipulator.addFromDrop(victim.getClient(), getItemFromCashInfo(item), null, false);
                     c.getPlayer().modifyCSPoints(4, -item.getPrice());
                     updateInformation(c);
                    } try {
                        victim.sendNote(victim.getName(), text);
                        victim.showNote();
                    } catch (SQLException s) {
                    }
                } else {
                     c.getPlayer().dropMessage(1, "Player is not on the same channel.");
                     updateInformation(c);
                }
            } else {
                 c.getPlayer().dropMessage(1, "Birthday is incorrect!");
                 updateInformation(c);
            }
        } else if (action == 5) {
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM wishlist WHERE charid = ?");
                ps.setInt(1, c.getPlayer().getId());
                ps.executeUpdate();
                ps.close();

                int i = 10;
                while (i > 0) {
                    int sn = slea.readInt();
                    if (sn != 0) {
                        ps = con.prepareStatement("INSERT INTO wishlist(charid, sn) VALUES(?, ?) ");
                        ps.setInt(1, c.getPlayer().getId());
                        ps.setInt(2, sn);
                        ps.executeUpdate();
                        ps.close();
                    }
                    i--;
                }
            } catch (SQLException se) {
            }
            c.getSession().write(MaplePacketCreator.sendWishList(c.getPlayer().getId(), true));
        } else if (action == 7) {
                slea.skip(1);
                byte toCharge = slea.readByte();
                int toIncrease = slea.readInt();
                if (c.getPlayer().getCSPoints(toCharge) >= 4000 && c.getPlayer().getStorage().getSlots() < 48) { // 48 is max.
                    c.getPlayer().modifyCSPoints(toCharge, -4000);
                    if (toIncrease == 0)
                        c.getPlayer().getStorage().gainSlots((byte) 4);
                    c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.getSession().write(MaplePacketCreator.enableCSUse0());
                    c.getSession().write(MaplePacketCreator.enableCSUse1());
                    c.getSession().write(MaplePacketCreator.enableCSUse2());
                    c.getSession().write(MaplePacketCreator.enableCSUse3());
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
            } else if (action == 27 || action == 33) {
            int birthdate = slea.readInt();
            int toCharge = slea.readInt();
            int SN = slea.readInt();
            String recipient = slea.readMapleAsciiString();
            String text = slea.readMapleAsciiString();
            CashItemInfo ring = CashItemFactory.getItem(SN);
            MapleCharacter partnerChar = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
            if (partnerChar == null) {
                c.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(1, "The partner specified could not be found. \\r\\nPlease make sure your partner is online and in the same channel."));
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112000) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112000) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112001) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112001) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112002) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112002) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112003) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112003) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112005) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112005) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112006) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112006) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112800) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112800) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112801) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112801) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).findById(1112802) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1112802) != null) {
                c.getPlayer().dropMessage(1, "You already have a ring with another person!");
                return;
            } else {
                c.getPlayer().modifyCSPoints(toCharge, -ring.getPrice());
                MapleRing.createRing(ring.getId(), c.getPlayer(), partnerChar, text);
                c.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(1, "Successfully created a ring for you and your partner!\\r\\nIf you can not see the effect, try re-logging in."));
            }
        } else if (action == 7) {
            slea.skip(1);
            byte toCharge = slea.readByte();
            int toIncrease = slea.readInt();
            if (c.getPlayer().getCSPoints(toCharge) >= 4000 && c.getPlayer().getStorage().getSlots() < 48) {
                c.getPlayer().modifyCSPoints(toCharge, -4000);
                if (toIncrease == 0) {
                    c.getPlayer().getStorage().gainSlots(4);
                }
                updateInformation(c);
            }
        } else if (action == 28) { // Pacotes
            slea.skip(1);
            int useNX = slea.readInt();
            int snCS = slea.readInt();
            CashItemInfo item = CashItemFactory.getItem(snCS);
            if (item.getId() == 5220000) {
                
            }
            if (c.getPlayer().getCSPoints(useNX) >= item.getPrice()) {
                c.getPlayer().modifyCSPoints(useNX, -item.getPrice());
            } else {
                c.getPlayer().dropMessage(1, "You do not have NX!");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            for (int i : CashItemFactory.getPackageItems(item.getId())) {
                if (i >= 5000000 && i <= 5000100) {
                    int petId = MaplePet.createPet(i);
                    if (petId == -1) {
                        c.getSession().write(MaplePacketCreator.enableActions());
                        return;
                    }
                MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), "Cash item was purchased!", null, MaplePet.createPet(item.getId()));
               } else {
                 MapleInventoryManipulator.addFromDrop(c, getItemFromCashInfo(item), null);
               }
            }
            updateInformation(c, snCS);
        } else if (action == 30) { //Quest items
            int snCS = slea.readInt();
            CashItemInfo item = CashItemFactory.getItem(snCS);
            if (item != null && c.getPlayer().getMeso() >= item.getPrice()) {
                if (MapleItemInformationProvider.getInstance().isQuestItem(item.getId())) {
                    c.getPlayer().gainMeso(-item.getPrice(), false);
                    MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), "");
                } else {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    c.getPlayer().dropMessage(1, "You can not buy items with 0 mesos!");
                    return;
                }
            }
        }
    }

    public IItem getItemFromCashInfo(CashItemInfo cii) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType mit = ii.getInventoryType(cii.getId());
        long wtf = Long.valueOf(cii.getPeriod());
        long add = 1000L * 60L * 60L * 24L * wtf;
        if (cii.getId() == 5211048 || cii.getId() == 5360042) {
            add = 1000L * 60L * 60L * 4L;
        }
        if (mit.equals(MapleInventoryType.EQUIP)) {
            IItem ret = ii.getEquipById(cii.getId());
            ret.setExpiration(System.currentTimeMillis() + add);
            return ret;
        } else {
            IItem nItem = new Item(cii.getId(), (byte) -1, (short) cii.getCount());
            nItem.setExpiration(System.currentTimeMillis() + add);
            return nItem;
        }
    }
 
    private boolean contains(int[] b, int id) {
        for (int i : b) {
            if (i == id) {
                return true;
            }
        }
        return false;
    }
    
 private boolean checkBirthday(MapleClient c, int idate) {
        int year = idate / 10000;
        int month = (idate - year * 10000) / 100;
        int day = idate - year * 10000 - month * 100;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month - 1, day);
        return c.checkBirthDate(cal);
    }
}
