/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package handling.channel.handler;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import client.inventory.Equip;
import client.IItem;
import client.inventory.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MapleInventoryType;
import database.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import handling.AbstractMaplePacketHandler;
import handling.channel.ChannelServer;
import server.DueyPackages;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;

/**
 *
 * @author XoticStory
 */
public class DueyHandler extends AbstractMaplePacketHandler {

    public enum Actions {

        TOSERVER_SEND_ITEM(0x02),
        TOSERVER_CLOSE_DUEY(0x07),
        TOSERVER_CLAIM_PACKAGE(0x04),
        TOSERVER_REMOVE_PACKAGE(0x05),
        TOCLIENT_OPEN_DUEY(0x08),
        TOCLIENT_NOT_ENOUGH_MESOS(0x0A),
        TOCLIENT_NAME_DOES_NOT_EXIST(0x0C),
        TOCLIENT_SAMEACC_ERROR(0x0D),
        TOCLIENT_PACKAGE_MSG(0x1B),
        TOCLIENT_SUCCESSFUL_MSG(0x17), // ending byte 4 if recieved. 3 if delete
        TOCLIENT_SUCCESSFULLY_SENT(0x12),;
        final byte code;

        private Actions(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }

        public static Actions getByType(byte type) {
            for (Actions a : Actions.values()) {
                if (a.getCode() == type) {
                    return a;
                }
            }
            return null;
        }
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
      //  c.getPlayer().resetAfkTimer();
        byte operation = slea.readByte();
        if (operation == Actions.TOSERVER_SEND_ITEM.getCode()) {
            final int fee = 5000;
            byte inventId = slea.readByte();
            short itemPos = slea.readShort();
            short amount = slea.readShort();
            int mesos = slea.readInt();
            String recipient = slea.readMapleAsciiString();

            int finalcost = mesos + fee + getFee(mesos);
            boolean send = false;
            if (c.getPlayer().getMeso() >= finalcost) {
                int accid = MapleCharacter.getAccIdFromCNAME(recipient);
                if (accid != -1) {
                    if (accid != c.getAccID()) {
                        c.getPlayer().gainMeso(finalcost, false); // cbf.
                        c.getSession().write(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_SUCCESSFULLY_SENT.getCode()));
                        send = true;
                    } else {
                        c.getSession().write(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_SAMEACC_ERROR.getCode()));
                    }
                } else {
                    c.getSession().write(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_NAME_DOES_NOT_EXIST.getCode()));
                }
            } else {
                c.getSession().write(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_NOT_ENOUGH_MESOS.getCode()));
            }
            boolean recipientOn = false;
            MapleClient rClient = null;
            try {
                int channel = c.getChannelServer().getWorldInterface().find(recipient);
                if (channel > -1) {
                    recipientOn = true;
                    ChannelServer rcserv = ChannelServer.getInstance(channel);
                    rClient = rcserv.getPlayerStorage().getCharacterByName(recipient).getClient();
                }
            } catch (RemoteException re) {
                c.getChannelServer().reconnectWorld();
            }
            if (send) {
                if (inventId > 0) {
                    MapleInventoryType inv = MapleInventoryType.getByType(inventId);
                    IItem item = c.getPlayer().getInventory(inv).getItem((byte) itemPos);
                    // NOTE. The checks arent in gMS order.
                    if (item != null && c.getPlayer().getItemQuantity(item.getItemId(), false) >= amount) {
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        if (ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
                            MapleInventoryManipulator.removeFromSlot(c, inv, (byte) itemPos, item.getQuantity(), true);
                        } else {
                            MapleInventoryManipulator.removeFromSlot(c, inv, (byte) itemPos, amount, true, false);
                        }
                        addItemToDB(item, amount, mesos, c.getPlayer().getName(), MapleCharacter.getIdByName(recipient, 0), recipientOn);
                    } else {  // hax.
                        c.disconnect();
                        c.getSession().close();
                        return;
                    }
                } else {
                    addMesoToDB(mesos, c.getPlayer().getName(), MapleCharacter.getIdByName(recipient, 0), recipientOn);
                }
                if (recipientOn && rClient != null) {
                    rClient.getSession().write(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_PACKAGE_MSG.getCode()));
                }
                c.getPlayer().gainMeso(-fee, false);
            }
        } else if (operation == Actions.TOSERVER_REMOVE_PACKAGE.getCode()) {
            int packageid = slea.readInt();
            removeItemFromDB(packageid);
            c.getSession().write(MaplePacketCreator.removeItemFromDuey(true, packageid));
        } else if (operation == Actions.TOSERVER_CLAIM_PACKAGE.getCode()) {
            int packageid = slea.readInt();
            DueyPackages dp = loadSingleItem(packageid);
            if (dp.getItem() != null) {
                if (!MapleInventoryManipulator.checkSpace(c, dp.getItem().getItemId(), dp.getItem().getQuantity(), dp.getItem().getOwner())) {
                    c.getPlayer().dropMessage(1, "Your inventory is full");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                } else {
                    MapleInventoryManipulator.addFromDrop(c, dp.getItem(), "", false);
                }
            }
            c.getPlayer().gainMeso(dp.getMesos(), false);
            removeItemFromDB(packageid);
            c.getSession().write(MaplePacketCreator.removeItemFromDuey(false, packageid));
        }
    }

    private void addMesoToDB(int mesos, String sName, int recipientID, boolean isOn) {
        addItemToDB(null, 1, mesos, sName, recipientID, isOn);
    }

    private void addItemToDB(IItem item, int quantity, int mesos, String sName, int recipientID, boolean isOn) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO dueypackages (RecieverId, SenderName, Mesos, TimeStamp, Checked, Type) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, recipientID);
            ps.setString(2, sName);
            ps.setInt(3, mesos);
            ps.setString(4, getCurrentDate());
            ps.setInt(5, isOn ? 0 : 1);
            if (item == null) {
                ps.setInt(6, 3);
                ps.executeUpdate();
            } else {
                ps.setInt(6, item.getType());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                PreparedStatement ps2;
                if (item.getType() == 1) { // equips
                    ps2 = con.prepareStatement("INSERT INTO dueyitems (PackageId, itemid, quantity, upgradeslots, level, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    Equip eq = (Equip) item;
                    ps2.setInt(2, eq.getItemId());
                    ps2.setInt(3, quantity); // 1 ?
                    ps2.setInt(4, eq.getUpgradeSlots());
                    ps2.setInt(5, eq.getLevel());
                    ps2.setInt(6, eq.getStr());
                    ps2.setInt(7, eq.getDex());
                    ps2.setInt(8, eq.getInt());
                    ps2.setInt(9, eq.getLuk());
                    ps2.setInt(10, eq.getHp());
                    ps2.setInt(11, eq.getMp());
                    ps2.setInt(12, eq.getWatk());
                    ps2.setInt(13, eq.getMatk());
                    ps2.setInt(14, eq.getWdef());
                    ps2.setInt(15, eq.getMdef());
                    ps2.setInt(16, eq.getAcc());
                    ps2.setInt(17, eq.getAvoid());
                    ps2.setInt(18, eq.getHands());
                    ps2.setInt(19, eq.getSpeed());
                    ps2.setInt(20, eq.getJump());
                    ps2.setString(21, eq.getOwner());
                } else {
                    ps2 = con.prepareStatement("INSERT INTO dueyitems (PackageId, itemid, quantity, owner) VALUES (?, ?, ?, ?)");
                    ps2.setInt(2, item.getItemId());
                    ps2.setInt(3, quantity);
                    ps2.setString(4, item.getOwner());
                }
                ps2.setInt(1, rs.getInt(1));
                ps2.executeUpdate();
                ps2.close();
                rs.close();
            }
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static List<DueyPackages> loadItems(MapleCharacter chr) {
        List<DueyPackages> packages = new LinkedList<DueyPackages>();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages " + "LEFT JOIN dueyitems USING (PackageId) " + "WHERE RecieverId = ?"); // PLEASE WORK D:
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DueyPackages dueypack = getItemByPID(rs);
                dueypack.setSender(rs.getString("SenderName"));
                dueypack.setMesos(rs.getInt("Mesos"));
                dueypack.setSentTime(rs.getString("TimeStamp"));
                packages.add(dueypack);
            }
            rs.close();
            ps.close();
            return packages;
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        }
    }

    public static DueyPackages loadSingleItem(int packageid) {
        List<DueyPackages> packages = new LinkedList<DueyPackages>();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages " + "LEFT JOIN dueyitems USING (PackageId) " + "WHERE PackageId = ?"); // PLEASE WORK D:
            ps.setInt(1, packageid);
            ResultSet rs = ps.executeQuery();
            DueyPackages dueypack = null;
            if (rs.next()) {
                dueypack = getItemByPID(rs);
                dueypack.setSender(rs.getString("SenderName"));
                dueypack.setMesos(rs.getInt("Mesos"));
                dueypack.setSentTime(rs.getString("TimeStamp"));
                packages.add(dueypack);
            }
            rs.close();
            ps.close();
            return dueypack;
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        }
    }

    public static void reciveMsg(MapleClient c, int recipientId) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE dueypackages SET Checked = 0 where RecieverId = ?");
            ps.setInt(1, recipientId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        c.getSession().write(MaplePacketCreator.sendDueyMSG(Actions.TOCLIENT_PACKAGE_MSG.getCode()));
    }

    public static int dueyStorageSize(MapleCharacter chr) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) as dueypackages FROM Duey WHERE RecieverId = ?");
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            rs.next();
            int size = rs.getInt("dueySize");
            rs.close();
            ps.close();
            return size;
        } catch (SQLException se) {
            se.printStackTrace();
            return 0;
        }

    }

    private String getCurrentDate() {
        String date = "";
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DATE) - 1; // isntant duey ?
        int month = cal.get(Calendar.MONTH) + 1; // its an array of months.
        int year = cal.get(Calendar.YEAR);
        if (day < 9) {
            date += "0" + day + "-";
        } else {
            date += day + "-";
        }

        if (month < 9) {
            date += "0" + month + "-";
        } else {
            date += month + "-";
        }

        date += year;
        return date;
    }

    private int getFee(int meso) {
        int fee = 0;
        if (meso >= 10000000) {
            fee = (int) Math.round(0.04 * meso);
        } else if (meso >= 5000000) {
            fee = (int) Math.round(0.03 * meso);
        } else if (meso >= 1000000) {
            fee = (int) Math.round(0.02 * meso);
        } else if (meso >= 100000) {
            fee = (int) Math.round(0.01 * meso);
        } else if (meso >= 50000) {
            fee = (int) Math.round(0.005 * meso);
        }

        return fee;
    }

    private void removeItemFromDB(int packageid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM dueypackages WHERE PackageId = ?");
            ps.setInt(1, packageid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM dueyitems WHERE PackageId = ?");
            ps.setInt(1, packageid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private static DueyPackages getItemByPID(ResultSet rs) {
        try {
            DueyPackages dueypack;
            if (rs.getInt("type") == 1) {
                Equip eq = new Equip(rs.getInt("itemid"), (byte) 0, -1);
                eq.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                eq.setLevel((byte) rs.getInt("level"));
                eq.setStr((short) rs.getInt("str"));
                eq.setDex((short) rs.getInt("dex"));
                eq.setInt((short) rs.getInt("int"));
                eq.setLuk((short) rs.getInt("luk"));
                eq.setHp((short) rs.getInt("hp"));
                eq.setMp((short) rs.getInt("mp"));
                eq.setWatk((short) rs.getInt("watk"));
                eq.setMatk((short) rs.getInt("matk"));
                eq.setWdef((short) rs.getInt("wdef"));
                eq.setMdef((short) rs.getInt("mdef"));
                eq.setAcc((short) rs.getInt("acc"));
                eq.setAvoid((short) rs.getInt("avoid"));
                eq.setHands((short) rs.getInt("hands"));
                eq.setSpeed((short) rs.getInt("speed"));
                eq.setJump((short) rs.getInt("jump"));
                eq.setOwner(rs.getString("owner"));
                dueypack = new DueyPackages(rs.getInt("PackageId"), eq);
            } else if (rs.getInt("type") == 2) {
                Item newItem = new Item(rs.getInt("itemid"), (byte) 0, (short) rs.getInt("quantity"));
                newItem.setOwner(rs.getString("owner"));
                dueypack = new DueyPackages(rs.getInt("PackageId"), newItem);
            } else {
                dueypack = new DueyPackages(rs.getInt("PackageId"));
            }
            return dueypack;
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        }
    }
}