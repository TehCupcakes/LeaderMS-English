package server.PlayerInteraction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import database.DatabaseConnection;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

public class MapleMiniGame extends PlayerInteractionManager {

    private MapleCharacter owner;
    private MiniGameType GameType;
    private int[] piece = new int[250];
    private List<Integer> list4x3 = new ArrayList<Integer>();
    private List<Integer> list5x4 = new ArrayList<Integer>();
    private List<Integer> list6x5 = new ArrayList<Integer>();
    boolean ready = false;
    private int loser = 1;
    private boolean started; // 0 = waiting, 1 = in progress
    private int firstslot = 0;
    private int visitorpoints = 0;
    private int ownerpoints = 0;
    private int matchestowin = 0;

    public enum MiniGameType {

        OMOK, MATCH_CARDS
    }

    public MapleMiniGame(MapleCharacter owner, int type, String desc) {
        super(owner, type, desc, 1);
        this.owner = owner;
    }

    @Override
    public void buy(MapleClient c, int item, short quantity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getShopType() {
        if (getGameType().equals(MiniGameType.OMOK)) {
            return IPlayerInteractionManager.OMOK;
        } else {
            return IPlayerInteractionManager.MATCH_CARD;
        }
    }

    @Override
    public void closeShop(boolean saveItems) {
        owner.getMap().broadcastMessage(MaplePacketCreator.removeCharBox(owner));
        owner.getMap().removeMapObject(this);
        owner.setInteraction(null);
    }

    public void setStarted(boolean start) {
        started = start;
    }

    public boolean getStarted() {
        return started;
    }

    public void setFirstSlot(int type) {
        firstslot = type;
    }

    public int getFirstSlot() {
        return firstslot;
    }

    public void setOwnerPoints() {
        ownerpoints++;
        if (ownerpoints + visitorpoints == matchestowin) {
            if (ownerpoints == visitorpoints) {
                broadcast(MaplePacketCreator.getMiniGameTie(this), true);
            } else if (ownerpoints > visitorpoints) {
                broadcast(MaplePacketCreator.getMiniGameWin(this, 0), true);
            } else if (visitorpoints > ownerpoints) {
                broadcast(MaplePacketCreator.getMiniGameWin(this, 1), true);
            }
        }
        ownerpoints = 0;
        visitorpoints = 0;
    }

    public void setVisitorPoints() {
        visitorpoints++;
        if ((ownerpoints + visitorpoints) == matchestowin) {
            if (ownerpoints > visitorpoints) {
                broadcast(MaplePacketCreator.getMiniGameWin(this, 0), true);
            } else if (visitorpoints > ownerpoints) {
                broadcast(MaplePacketCreator.getMiniGameWin(this, 1), true);
            } else if (ownerpoints == visitorpoints) {
                broadcast(MaplePacketCreator.getMiniGameTie(this), true);
            }
        }
        ownerpoints = 0;
        visitorpoints = 0;
    }

    public int getOwnerPoints() {
        return ownerpoints;
    }

    public void setMatchCardPoints(int winnerslot) { // 1 = owner, 2 = visitor 3 = tie
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        try {
            if (winnerslot < 3) {
                ps = con.prepareStatement("UPDATE characters SET matchcardwins = matchcardwins + 1 + WHERE name = ?");
                if (winnerslot == 1) {
                    ps.setString(1, owner.getName());
                } else if (winnerslot == 2) {
                    ps.setString(1, visitors[0].getName());
                }
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement("UPDATE characters SET matchcardlosses = matchcardlosses + 1 WHERE name = ?");
                if (winnerslot == 1) {
                    ps.setString(1, visitors[0].getName());
                } else if (winnerslot == 2) {
                    ps.setString(1, owner.getName());
                }
                ps.executeUpdate();
                ps.close();
            } else if (winnerslot == 3) {
                ps = con.prepareStatement("UPDATE characters SET matchcardties = matchcardties + 1 WHERE name = ? OR name = ?");
                ps.setString(1, owner.getName());
                ps.setString(2, visitors[0].getName());
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            owner.dropMessage("Exception has occured: " + e);
            return;
        }
    }

    public int getOmokPoints(String type, boolean getOwner) { // wins, losses, ties
        Connection con = DatabaseConnection.getConnection();
        int points = 0;

        try {
            PreparedStatement ps = con.prepareStatement("SELECT omok" + type + " FROM characters WHERE name = ?");
            ps.setString(1, owner.getName());
            ResultSet rs = ps.executeQuery();
            rs.next();
            points = rs.getInt("omok" + type);
            rs.close();
            ps.close();
            return points;
        } catch (SQLException e) {
            owner.dropMessage("Exception has occured: " + e);
        }
        return points;
    }

    public int getMatchCardPoints(String type) { // wins, losses, ties
        Connection con = DatabaseConnection.getConnection();
        int points = 0;
        try {
            PreparedStatement ps = con.prepareStatement("SELECT matchcard" + type + " FROM characters WHERE name = ?");
            ps.setString(1, owner.getName());
            ResultSet rs = ps.executeQuery();
            rs.next();
            points = rs.getInt("matchcard" + type);
            rs.close();
            ps.close();
            return points;
        } catch (SQLException e) {
            owner.dropMessage("Exception has occured: " + e);
        }
        return points;
    }

    public void setOmokPoints(int winnerslot) { // 1 = owner, 2 = visitor 3 = tie
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;

        try {
            if (winnerslot < 3) {
                ps = con.prepareStatement("UPDATE characters SET omokwins = omokwins + 1 WHERE name = ?");
                if (winnerslot == 1) {
                    ps.setString(1, owner.getName());
                }
                if (winnerslot == 2) {
                    ps.setString(1, visitors[0].getName());
                }
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("UPDATE characters SET omoklosses = omoklosses + 1 WHERE name = ?");
                if (winnerslot == 1) {
                    ps.setString(1, visitors[0].getName());
                }
                if (winnerslot == 2) {
                    ps.setString(1, owner.getName());
                }
                ps.executeUpdate();
                ps.close();
            } else if (winnerslot == 3) {
                ps = con.prepareStatement("UPDATE characters SET omokties = omokties + 1 WHERE name = ? OR name = ?");
                ps.setString(1, owner.getName());
                ps.setString(2, visitors[0].getName());
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            return;
        }
    }

    public int getVisitorPoints() {
        return ownerpoints;
    }

    public void setMatchesToWin(int type) {
        matchestowin = type;
    }

    public void setGameType(MiniGameType game) {
        GameType = game;
        if (game == GameType.MATCH_CARDS) {
            if (matchestowin == 6) {
                for (int i = 0; i < matchestowin; i++) {
                    list4x3.add(i);
                    list4x3.add(i);
                }
            } else if (matchestowin == 10) {
                for (int i = 0; i < matchestowin; i++) {
                    list5x4.add(i);
                    list5x4.add(i);
                }
            } else if (matchestowin == 15) {
                for (int i = 0; i < matchestowin; i++) {
                    list6x5.add(i);
                    list6x5.add(i);
                }
            }
        }
    }

    public MiniGameType getGameType() {
        return GameType;
    }

    public void shuffleList() {
        if (matchestowin == 6) {
            Collections.shuffle(list4x3);
        } else if (matchestowin == 10) {
            Collections.shuffle(list5x4);
        } else if (matchestowin == 15) {
            Collections.shuffle(list6x5);
        }
    }

    public int getCardId(int slot) {
        int cardid = 0;
        if (matchestowin == 6) {
            cardid = list4x3.get(slot - 1);
        } else if (matchestowin == 10) {
            cardid = list5x4.get(slot - 1);
        } else if (matchestowin == 15) {
            cardid = list6x5.get(slot - 1);
        }
        return cardid;
    }

    public int getMatchesToWin() {
        return matchestowin;
    }

    public void setLoser(int type) {
        loser = type;
    }

    public int getLoser() {
        return loser;
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public void setReady() {
        ready = !ready;
    }

    public boolean isReady() {
        return ready;
    }

    public void setPiece(int move1, int move2, int type, MapleCharacter chr) {
        int slot = ((move2 * 15) + (move1 + 1));
        if (piece[slot] == 0) {
            piece[slot] = type;
            broadcast(MaplePacketCreator.getMiniGameMoveOmok(move1, move2, type), true);
            for (int y = 0; y < 15; y++) {
                for (int x = 0; x < 11; x++) {
                    if (searchCombo(x, y, type)) {
                        if (isOwner(chr)) {
                            broadcast(MaplePacketCreator.getMiniGameWin(this, 0), true);
                            setStarted(false);
                            setLoser(0);
                        } else {
                            broadcast(MaplePacketCreator.getMiniGameWin(this, 1), true);
                            this.setStarted(false);
                            this.setLoser(1);
                        }
                        for (int y2 = 0; y2 < 15; y2++) {
                            for (int x2 = 0; x2 < 15; x2++) {
                                int slot2 = ((y2 * 15) + (x2 + 1));
                                piece[slot2] = 0;

                            }
                        }
                    }
                }
            }
            for (int y = 0; y < 15; y++) {
                for (int x = 4; x < 15; x++) {
                    if (searchCombo2(x, y, type)) {
                        if (isOwner(chr)) {
                            broadcast(MaplePacketCreator.getMiniGameWin(this, 0), true);
                            setStarted(false);
                            setLoser(0);
                        } else {
                            broadcast(MaplePacketCreator.getMiniGameWin(this, 1), true);
                            setStarted(false);
                            setLoser(1);
                        }
                        for (int y2 = 0; y2 < 15; y2++) {
                            for (int x2 = 0; x2 < 15; x2++) {
                                int slot2 = ((y2 * 15) + (x2 + 1));
                                piece[slot2] = 0;

                            }
                        }
                    }
                }
            }
        }

    }

    public boolean searchCombo(int x, int y, int type) {
        boolean winner = false;
        int slot = ((y * 15) + (x + 1));
        if (piece[slot] == type) {
            if (piece[slot + 1] == type) {
                if (piece[slot + 2] == type) {
                    if (piece[slot + 3] == type) {
                        if (piece[slot + 4] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        if (piece[slot] == type) {
            if (piece[slot + 16] == type) {
                if (piece[slot + 32] == type) {
                    if (piece[slot + 48] == type) {
                        if (piece[slot + 64] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        if (piece[slot] == type) {
            if (piece[slot + 15] == type) {
                if (piece[slot + 30] == type) {
                    if (piece[slot + 45] == type) {
                        if (piece[slot + 60] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        return winner;
    }

    public boolean searchCombo2(int x, int y, int type) {
        boolean winner = false;
        int slot = ((y * 15) + (x + 1));
        if (piece[slot] == type) {
            if (piece[slot + 15] == type) {
                if (piece[slot + 30] == type) {
                    if (piece[slot + 45] == type) {
                        if (piece[slot + 60] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        if (piece[slot] == type) {
            if (piece[slot + 14] == type) {
                if (piece[slot + 28] == type) {
                    if (piece[slot + 42] == type) {
                        if (piece[slot + 56] == type) {
                            winner = true;
                        }
                    }
                }
            }
        }
        return winner;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }
}