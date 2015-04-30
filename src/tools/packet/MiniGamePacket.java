/*
 * TehCupcakes (tehcupcakes@hotmail.com)
 * LeaderMS English 2015
 * Maplestory Private Server
 */
package tools.packet;

import client.MapleCharacter;
import client.MapleClient;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import server.PlayerInteraction.MapleMiniGame;
import tools.HexTool;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MiniGamePacket {
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MaplePacketCreator.class);
    
    public static MaplePacket getMiniGameStart(int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x37);
        mplew.write(loser);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkipTurn(int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x39);
        mplew.write(slot);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameReady() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x34);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameUnReady() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x35);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRequestTie() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x2C);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameDenyTie() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x2D);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameMoveOmok(int move1, int move2, int move3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x3A);
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameWin(MapleMiniGame game, int person) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 00"));
        mplew.write(person);
        mplew.writeInt(1); // start of owner; unknown
        mplew.writeInt(game.getOmokPoints("wins", true)); // wins
        mplew.writeInt(game.getOmokPoints("ties", true)); // ties
        mplew.writeInt(game.getOmokPoints("losses", true) + 1); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getOmokPoints("wins", false) + 1); // wins
        mplew.writeInt(game.getOmokPoints("ties", false)); // ties
        mplew.writeInt(game.getOmokPoints("losses", false)); // losses
        mplew.writeInt(2000); // points
        game.setOmokPoints(person + 1);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameTie(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 01"));
        mplew.writeInt(1); // unknown
        mplew.writeInt(game.getOmokPoints("wins", true)); // wins
        mplew.writeInt(game.getOmokPoints("ties", true) + 1); // ties
        mplew.writeInt(game.getOmokPoints("losses", true)); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getOmokPoints("wins", false)); // wins
        mplew.writeInt(game.getOmokPoints("ties", false) + 1); // ties
        mplew.writeInt(game.getOmokPoints("losses", false)); // losses
        mplew.writeInt(2000); // points
        game.setMatchCardPoints(3);
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameForfeit(MapleMiniGame game, int person) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("38 02"));
        mplew.write(person);
        mplew.writeInt(1); // start of owner; unknown
        mplew.writeInt(game.getOmokPoints("wins", true)); // wins
        mplew.writeInt(game.getOmokPoints("ties", true)); // ties
        mplew.writeInt(game.getOmokPoints("losses", true) + 1); // losses
        mplew.writeInt(2000); // points
        mplew.writeInt(1); // start of visitor; unknown
        mplew.writeInt(game.getOmokPoints("wins", false) + 1); // wins
        mplew.writeInt(game.getOmokPoints("ties", false)); // ties
        mplew.writeInt(game.getOmokPoints("losses", false)); // losses
        mplew.writeInt(2000); // points
        game.setOmokPoints(person + 1);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardStart(MapleMiniGame game) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x37);
        mplew.write(game.getLoser());
        int times;
        if (game.getMatchesToWin() > 10) {
            times = 30;
        } else if (game.getMatchesToWin() > 6) {
            times = 20;
        } else {
            times = 12;
        }
        mplew.write(times);
        for (int i = 1; i <= times; i++) {
            mplew.writeInt(game.getCardId(i));
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardSelect(int turn, int slot, int firstslot, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0x3E);
        mplew.write(turn);
        if (turn == 1) {
            mplew.write(slot);
        } else if (turn == 0) {
            mplew.write(slot);
            mplew.write(firstslot);
            mplew.write(type);
        }
        return mplew.getPacket();
    }
    
    public static MaplePacket getMiniGameClose(byte number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(0xA);
        mplew.write(1);
        mplew.write(3);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardStart(MapleMiniGame game, int loser) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("37 0" + loser));
        mplew.write(HexTool.getByteArrayFromHexString("0C"));
        mplew.writeInt(game.getCardId(1));
        mplew.writeInt(game.getCardId(2));
        mplew.writeInt(game.getCardId(3));
        mplew.writeInt(game.getCardId(4));
        mplew.writeInt(game.getCardId(5));
        mplew.writeInt(game.getCardId(6));
        mplew.writeInt(game.getCardId(7));
        mplew.writeInt(game.getCardId(8));
        mplew.writeInt(game.getCardId(9));
        mplew.writeInt(game.getCardId(10));
        mplew.writeInt(game.getCardId(11));
        mplew.writeInt(game.getCardId(12));
        if (game.getMatchesToWin() > 6) {
            mplew.writeInt(game.getCardId(13));
            mplew.writeInt(game.getCardId(14));
            mplew.writeInt(game.getCardId(15));
            mplew.writeInt(game.getCardId(16));
            mplew.writeInt(game.getCardId(17));
            mplew.writeInt(game.getCardId(18));
            mplew.writeInt(game.getCardId(19));
            mplew.writeInt(game.getCardId(20));
        }
        if (game.getMatchesToWin() > 10) {
            mplew.writeInt(game.getCardId(21));
            mplew.writeInt(game.getCardId(22));
            mplew.writeInt(game.getCardId(23));
            mplew.writeInt(game.getCardId(24));
            mplew.writeInt(game.getCardId(25));
            mplew.writeInt(game.getCardId(26));
            mplew.writeInt(game.getCardId(27));
            mplew.writeInt(game.getCardId(28));
            mplew.writeInt(game.getCardId(29));
            mplew.writeInt(game.getCardId(30));
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardNewVisitor(MapleCharacter c, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        PacketHelper.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(1);
        mplew.writeInt(c.getMatchCardPoints("wins"));
        mplew.writeInt(c.getMatchCardPoints("ties"));
        mplew.writeInt(c.getMatchCardPoints("losses"));
        mplew.writeInt(2000);
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("3E 0" + turn));
        if (turn == 1) {
            mplew.write(slot);
        }
        if (turn == 0) {
            mplew.write(slot);
            mplew.write(firstslot);
            mplew.write(type);
        }
        log.info(mplew.toString());
        return mplew.getPacket();
    }
    
    public static MaplePacket removeOmokBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }

    public static MaplePacket removeMatchcardBox(MapleCharacter c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        return mplew.getPacket();
    }
    
}
