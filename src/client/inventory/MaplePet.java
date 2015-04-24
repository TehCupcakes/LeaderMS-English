package client.inventory;

import client.inventory.Item;
import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import database.DatabaseConnection;
import server.MapleItemInformationProvider;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;

public class MaplePet extends Item {

    private String name;
    private int uniqueid;
    private int closeness = 0;
    private int level = 1;
    private int fullness = 100;
    private int Fh;
    private Point pos;
    private int stance;

    private MaplePet(int id, byte position, int uniqueid) {
        super(id, position, (short) 1);
        this.uniqueid = uniqueid;
    }

     public static MaplePet loadFromDb(int itemid, byte position, int petid) {
        try {
            MaplePet ret = new MaplePet(itemid, position, petid);
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, level, closeness, fullness FROM pets WHERE petid = ?"); // Get pet details..
            ps.setInt(1, petid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret.setName(rs.getString("name"));
            ret.setCloseness(Math.min(rs.getInt("closeness"), 30000));
            ret.setLevel(Math.min(rs.getInt("level"), 30));
            ret.setFullness(Math.min(rs.getInt("fullness"), 100));
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException e) {
            return null;
        }
    }
     
   public void saveToDb() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ? WHERE petid = ?");
            ps.setString(1, getName());
            ps.setInt(2, getLevel());
            ps.setInt(3, getCloseness());
            ps.setInt(4, getFullness());
            ps.setInt(5, getUniqueId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
        }
    }

     public static int createPet(int itemid) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO pets (name, level, closeness, fullness) VALUES (?, 1, 0, 100)");
            ps.setString(1, MapleItemInformationProvider.getInstance().getName(itemid));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int ret = -1;
            if (rs.next()) {
                ret = rs.getInt(1);
                rs.close();
                ps.close();
            }
            return ret;
        } catch (SQLException e) {
            return -1;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUniqueId() {
        return uniqueid;
    }

    public void setUniqueId(int id) {
        this.uniqueid = id;
    }

    public int getCloseness() {
        return closeness;
    }

    public void setCloseness(int closeness) {
        this.closeness = closeness;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFullness() {
        return fullness;
    }

    public void setFullness(int fullness) {
        this.fullness = fullness;
    }

    public int getFh() {
        return Fh;
    }

    public void setFh(int Fh) {
        this.Fh = Fh;
    }

    public Point getPos() {
        return pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public int getStance() {
        return stance;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

      public boolean canConsume(int itemId) {
        for (int petId : MapleItemInformationProvider.getInstance().petsCanConsume(itemId)) {
            if (petId == this.getItemId()) {
                return true;
            }
        }
        return false;
    }

    public void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    this.setPos(((LifeMovement) move).getPosition());
                }
                this.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}