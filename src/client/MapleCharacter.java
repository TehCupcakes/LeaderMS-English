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

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import client.MapleCharacter.MapleCoolDownValueHolder;
import client.anticheat.CheatTracker;
import client.messages.MessageCallback;
import client.messages.ServernoticeMapleClientMessageCallback;
import config.Game.NewPlayers;
import config.configuration.Configuration;
import config.Game.LeaderOccupations;
import config.skills.Corsair;
import config.skills.DarkKnight;
import config.skills.GameMaster;
import config.skills.Spearman;
import config.skills.SuperGM;
import database.DatabaseConnection;
import database.DatabaseException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.MaplePacket;
import net.PacketProcessor;
import net.channel.ChannelServer;
import net.world.MapleMessenger;
import net.world.MapleMessengerCharacter;
import net.world.MapleParty;
import net.world.MaplePartyCharacter;
import net.world.PartyOperation;
import net.world.PlayerBuffValueHolder;
import net.world.PlayerCoolDownValueHolder;
import net.world.remote.WorldChannelInterface;
import scripting.event.EventInstanceManager;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.TimerManager;
import server.life.MapleMonster;
import server.maps.AbstractAnimatedMapleMapObject;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.SavedLocationType;
import server.maps.SummonMovementType;
import server.quest.MapleQuest;
import tools.MaplePacketCreator;
import tools.Pair;
import net.world.guild.*;
import server.MapleInventoryManipulator;
import server.MonsterCarnival;
import server.PlayerInteraction.HiredMerchant;
import server.PlayerInteraction.IPlayerInteractionManager;
import server.PlayerInteraction.MaplePlayerShop;
import server.life.MobSkill;
import org.apache.mina.common.IoSession;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterStats;
import tools.FilePrinter;

public class MapleCharacter extends AbstractAnimatedMapleMapObject implements InventoryContainer {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PacketProcessor.class);
    private static final Lock save_mutex = new ReentrantLock();
    public static final double MAX_VIEW_RANGE_SQ = 850 * 850;
    private int world;
    private int accountid;
    private int rank;
    private int rankMove;
    private int jobRank;
    private int jobRankMove;
    private String name;
    private String nome;
    private int level;
    private int str, dex, luk, int_;
    private AtomicInteger exp = new AtomicInteger();
    private int hp, maxhp;
    private int mp, maxmp;
    private int mpApUsed, hpApUsed;
    private int hair, face;
    private AtomicInteger meso = new AtomicInteger();
    private int remainingAp, remainingSp;
    private int savedLocations[];
    private int fame;
    private long lastfametime;
    private List<Integer> lastmonthfameids;
    private int gender;
    private int gmLevel;
    private long afkTimer = 0;
    private double sword;
    private double blunt;
    private double axe;
    private double spear;
    private double polearm;
    private double claw;
    private double dagger;
    private double staffwand = 0.1;
    private double crossbow;
    private double bow;
    private int skill = 0;
    private int gmtext = 0;
    private ISkill skil;
    private int maxDis;
    private long latestMarriageReq = 0;
    private int bossPoints;
    final String[] invincible = {"Leader"};
    private List<ScheduledFuture<?>> timers = new ArrayList<>();
    private transient int localmaxhp, localmaxmp;
    private transient int localstr, localdex, localluk, localint_;
    private transient int magic, watk;
    private transient int acc, eva;
    private transient int wdef, mdef;
    private transient double speedMod, jumpMod;
    private int expRate = 1, mesoRate = 1, dropRate = 1;
    private transient int localmaxbasedamage;
    private int id;
    private MapleClient client;
    private MapleMap map;
    private NewPlayers noobajuda;
    private int initialSpawnPoint;
    private int clan;
    private boolean isbanned = false;
    private boolean canSmega = true;
    private boolean smegaEnabled = true;
    private boolean canNatal = true;
    private boolean NatalEnabled = true;
    private int mapid;
    private MapleShop shop = null;
    private MaplePlayerShop playerShop = null;
    private MapleStorage storage = null;
    private MaplePet[] pets = new MaplePet[3];
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private MapleTrade trade = null;
    private MapleSkinColor skinColor = MapleSkinColor.NORMAL;
    private MapleJob job = MapleJob.BEGINNER;
    private short classe;
    private int energybar = 0;
    private boolean hidden;
    private boolean canDoor = true;
    private int battleshipHp = 0;
    private int chair;
    private int itemEffect;
    public boolean isfake;
    private MapleParty party;
    private EventInstanceManager eventInstance = null;
    private MapleInventory[] inventory;
    private LeaderOccupations occupation = LeaderOccupations.Iniciante;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Set<MapleMonster> controlled = new LinkedHashSet<MapleMonster>();
    private Set<MapleMapObject> visibleMapObjects = new LinkedHashSet<MapleMapObject>();
    private Map<ISkill, SkillEntry> skills = new LinkedHashMap<ISkill, SkillEntry>();
   // private Map<MapleBuffStat, MapleBuffStatValueHolder> effects = new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>();
  //  private transient Map<MapleBuffStat, MapleBuffStatValueHolder> effects = new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>(50);
    private Map<MapleBuffStat, MapleBuffStatValueHolder> effects = Collections.synchronizedMap(new LinkedHashMap<MapleBuffStat, MapleBuffStatValueHolder>());
    private Map<Integer, MapleKeyBinding> keymap = new LinkedHashMap<Integer, MapleKeyBinding>();
    private List<MapleDoor> doors = new ArrayList<MapleDoor>();
    private Map<Integer, MapleSummon> summons = new LinkedHashMap<Integer, MapleSummon>();
    private BuddyList buddylist;
    private Map<Integer, MapleCoolDownValueHolder> coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
    // anticheat related information
    private CheatTracker anticheat;
    private ScheduledFuture<?> dragonBloodSchedule;
    private ScheduledFuture<?> mapTimeLimitTask = null;
    // mounts
    private MapleMount maplemount;
    //guild related information
    private int guildid;
    private int guildrank,  allianceRank;
    private MapleGuildCharacter mgc = null;
    private boolean hasMerchant;
    private int merchantMesos;
    private byte prefixstuff, smega;
    private String chalktext, legend;
    // cash shop related information
    private int paypalnx;
    private int maplepoints;
    private int cardnx;
    private int BetaPoints;
    private boolean allowMapChange = true;
    // misc information
    private List<MapleDisease> diseases = new ArrayList<MapleDisease>();
    private boolean incs;
    private MapleMessenger messenger = null;
    int messengerposition = 4;
    private int slots = 0;
    private ScheduledFuture<?> fullnessSchedule;
    private ScheduledFuture<?> fullnessSchedule_1;
    private ScheduledFuture<?> fullnessSchedule_2;
    private ScheduledFuture<?> hpDecreaseTask;
    private ScheduledFuture<?> beholderHealingSchedule;
    private ScheduledFuture<?> beholderBuffSchedule;
    private ScheduledFuture<?> BerserkSchedule;
    private boolean Berserk = false;
    public SummonMovementType getMovementType;
    private int canTalk;
    private int zakumLvl; //zero means they havent started yet
    private int pvpScore = 0;
    private int donatorPoints = 0;
    private IPlayerInteractionManager interaction;
    //irc bot
    private boolean ircAllTalk = false;
    //marriage
    private int married;
    private int partnerid;
    private int marriageQuestLevel;
    private boolean logchat = false;
    private long lastChatLog = 0;
    private MessageCallback mc;
  //  private ChatLog chatlog;
    private int cp = 0;
    private int totCP = 0;
    private MonsterCarnival monsterCarnival;
    private long lastPortalEntry = 0;
    private boolean lord = false;
    private int battleShipHp = 0;
    private List<MapleSummon> pirateSummons = new LinkedList<MapleSummon>();
    private long lastNpcTalk = 0;
    private int cpqRanking = 0;
    private long lastCatch = 0;
    private int team = -1;
    private boolean shield = false;
    private int currentPage = 0, currentType = 0, currentTab = 1;
    private boolean inmts;
    private int pvpDeaths;
    private int pvpKills;
    private boolean challenged = false;
    private long lastFJ = 0;
    private int lastY = 0;
    private HiredMerchant hiredMerchant = null;
    private int job1;
    private static List<Pair<Byte, Integer>> inventorySlots = new ArrayList<Pair<Byte, Integer>>();
    // PQ's
    private int LeaderPoints;
    private int votePoints;
    private int pqPoints;
    private int CashPoints;
    
    public static long Levelcompleto = 0;
    public static long Levelcompara = 0;
        //JQ
    private String jqStart;
    private String lastJQFinish = "In 1947";
    private int jqrank;
    private int jqpoints;
    private int previousMap;
     //Aneis
    private List<MapleRing> crushRings = new ArrayList<MapleRing>();
    private List<MapleRing> friendshipRings = new ArrayList<MapleRing>();
    private List<MapleRing> marriageRings = new ArrayList<MapleRing>();
    
    /**
     * 0 - regular chat 1 - GM chat 2 - blue chat 3 - pink chat 4 - yellow chat
     * 5 - smega chat 6 - avatar mega chat
     */
    private int chatMode = 0;
    private long useTime = 0;
    /**
     * 0 - map 1 - channel 2 - world
     */
    private int chatRange = 0;
    private int ringRequest;

    private MapleCharacter() {
        setStance(0);
        inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            inventory[type.ordinal()] = new MapleInventory(type, (byte) 100);
        }

        savedLocations = new int[SavedLocationType.values().length];
        for (int i = 0; i < SavedLocationType.values().length; i++) {
            savedLocations[i] = -1;
        }   
        doors = new ArrayList<MapleDoor>();
        controlled = new LinkedHashSet<MapleMonster>();
        visibleMapObjects = new LinkedHashSet<MapleMapObject>();
        quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
        anticheat = new CheatTracker(this);
        setPosition(new Point(0, 0));
    }

    public void dropMessage(int a, String string) {
        this.getClient().getSession().write(MaplePacketCreator.serverNotice(a, string));
    }

    public void dropMessage(String string) {
        dropMessage(5, string);
    }

    public MapleCharacter getThis() {
        return this;
    }

     public void setChalkboard(String text) {
        if (interaction != null) {
            return;
        }
        this.chalktext = text;
        if (chalktext == null) {
            getMap().broadcastMessage(MaplePacketCreator.useChalkboard(this, true));
        } else {
            getMap().broadcastMessage(MaplePacketCreator.useChalkboard(this, false));
        }
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public void changeIRCAllTalk(boolean b) {
        this.ircAllTalk = b;
    }

    public boolean getIRCAllTalk() {
        return ircAllTalk;
    }

    public void addPirateSummon(MapleSummon summon) {
        pirateSummons.add(summon);
    }

    public void removePirateSummon(MapleSummon summon) {
        if (!pirateSummons.contains(summon)) {
            log.warn(name + " trying to remove summon which doesn't exist");
            return;
        }
        pirateSummons.remove(pirateSummons.indexOf(summon));
    }

    public List<MapleSummon> getPirateSummons() {
        return pirateSummons;
    }

    public boolean hasPirateSummon(MapleSummon summon) {
        return pirateSummons.contains(summon);
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) throws SQLException {
        MapleCharacter ret = new MapleCharacter();
        ret.client = client;
        ret.id = charid;

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
        ps.setInt(1, charid);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) {
            throw new RuntimeException("Loading the Char Failed (char not found)");
        }
        ret.name = rs.getString("name");
        ret.level = rs.getInt("level");
        ret.fame = rs.getInt("fame");
        ret.str = rs.getInt("str");
        ret.dex = rs.getInt("dex");
        ret.int_ = rs.getInt("int");
        ret.luk = rs.getInt("luk");
        ret.exp.set(rs.getInt("exp"));

        ret.hp = rs.getInt("hp");
        ret.maxhp = rs.getInt("maxhp");
        ret.mp = rs.getInt("mp");
        ret.maxmp = rs.getInt("maxmp");

        ret.hpApUsed = rs.getInt("hpApUsed");
        ret.mpApUsed = rs.getInt("mpApUsed");
        ret.remainingSp = rs.getInt("sp");
        ret.remainingAp = rs.getInt("ap");

        ret.meso.set(rs.getInt("meso"));

        ret.gmLevel = rs.getInt("gm");
        ret.votePoints = rs.getInt("votePoints");
        ret.LeaderPoints = rs.getInt("LeaderPoints");
        ret.pqPoints = rs.getInt("pqPoints");
        ret.jqpoints = rs.getInt("jqpoints");
        ret.CashPoints = rs.getInt("CashPoints");
        ret.jqrank = rs.getInt("jqrank");
        ret.skinColor = MapleSkinColor.getById(rs.getInt("skincolor"));
        ret.gender = rs.getInt("gender");
        ret.job = MapleJob.getById(rs.getInt("job"));
        ret.occupation = LeaderOccupations.getById(rs.getInt("occupation"));
        //cantalk
        ret.canTalk = rs.getInt("cantalk");
        //marriage
        ret.married = rs.getInt("married");
        ret.partnerid = rs.getInt("partnerid");
        ret.marriageQuestLevel = rs.getInt("marriagequest");

        //mount
        int mountexp = rs.getInt("mountexp");
        int mountlevel = rs.getInt("mountlevel");
        int mounttiredness = rs.getInt("mounttiredness");

        ret.merchantMesos = rs.getInt("MerchantMesos");
        ret.hasMerchant = rs.getInt("HasMerchant") == 1;

        ret.zakumLvl = rs.getInt("zakumLvl");

        ret.hair = rs.getInt("hair");
        ret.face = rs.getInt("face");
        ret.accountid = rs.getInt("accountid");

        ret.mapid = rs.getInt("map");
        ret.initialSpawnPoint = rs.getInt("spawnpoint");
        ret.world = rs.getInt("world");

        ret.rank = rs.getInt("rank");
        ret.rankMove = rs.getInt("rankMove");
        ret.jobRank = rs.getInt("jobRank");
        ret.jobRankMove = rs.getInt("jobRankMove");

        ret.guildid = rs.getInt("guildid");
        ret.guildrank = rs.getInt("guildrank");
        ret.allianceRank = rs.getInt("allianceRank");
        if (ret.guildid > 0) {
            ret.mgc = new MapleGuildCharacter(ret);
        }

        int buddyCapacity = rs.getInt("buddyCapacity");
        ret.buddylist = new BuddyList(buddyCapacity);
        ret.bossPoints = rs.getInt("bosspoints");

        if (channelserver) {
           // ret.chatlog = ChatLog.load(ret.name);
            MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map == null) { //char is on a map that doesn't exist warp it to henesys
                ret.map = mapFactory.getMap(100000000);
            }
            int rMap = ret.map.getForcedReturnId();
            if (rMap != 999999999) {
                ret.map = mapFactory.getMap(rMap);
            }
            MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
            if (portal == null) {
                portal = ret.map.getPortal(0); // char is on a spawnpoint that doesn't exist - select the first spawnpoint instead
                ret.initialSpawnPoint = 0;
            }
            ret.setPosition(portal.getPosition());

            int partyid = rs.getInt("party");
            if (partyid >= 0) {
                try {
                    MapleParty party = client.getChannelServer().getWorldInterface().getParty(partyid);
                    if (party != null && party.getMemberById(ret.id) != null) {
                        ret.party = party;
                    }
                } catch (RemoteException e) {
                    client.getChannelServer().reconnectWorld();
                }
            }

            int messengerid = rs.getInt("messengerid");
            int position = rs.getInt("messengerposition");
            if (messengerid > 0 && position < 4 && position > -1) {
                try {
                    WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                    MapleMessenger messenger = wci.getMessenger(messengerid);
                    if (messenger != null) {
                        ret.messenger = messenger;
                        ret.messengerposition = position;
                    }
                } catch (RemoteException e) {
                    client.getChannelServer().reconnectWorld();
                }
            }
            if (ChannelServer.getInstance(1).getLordId() == ret.id) {
                ret.lord = true;
            }
            //ret.loadCooldowns(con);
        }

        rs.close();
        ps.close();

        ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
        ps.setInt(1, ret.accountid);
        rs = ps.executeQuery();
        while (rs.next()) {
            ret.getClient().setAccountName(rs.getString("name"));
            ret.paypalnx = rs.getInt("paypalNX");
            ret.maplepoints = rs.getInt("mPoints");
            ret.cardnx = rs.getInt("cardNX");
            ret.donatorPoints = rs.getInt("donatorpoints");
            ret.BetaPoints = rs.getInt("BetaPoints");
        }
        rs.close();
        ps.close();

        String sql = "SELECT * FROM inventoryitems " + "LEFT JOIN inventoryequipment USING (inventoryitemid) " + "WHERE characterid = ?";
        if (!channelserver) {
            sql += " AND inventorytype = " + MapleInventoryType.EQUIPPED.getType();
        }
        ps = con.prepareStatement(sql);
        ps.setInt(1, charid);
        // PreparedStatement itemLog = con.prepareStatement("SELECT msg FROM inventorylog WHERE inventoryitemid = ?");
        rs = ps.executeQuery();
        while (rs.next()) {
            MapleInventoryType type = MapleInventoryType.getByType((byte) rs.getInt("inventorytype"));
            if (type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.EQUIPPED)) {
                int itemid = rs.getInt("itemid");
                Equip equip = new Equip(itemid, (byte) rs.getInt("position"), rs.getInt("ringid"));
                equip.setOwner(rs.getString("owner"));
                equip.setQuantity((short) rs.getInt("quantity"));
                equip.setAcc((short) rs.getInt("acc"));
                equip.setAvoid((short) rs.getInt("avoid"));
                equip.setDex((short) rs.getInt("dex"));
                equip.setHands((short) rs.getInt("hands"));
                equip.setHp((short) rs.getInt("hp"));
                equip.setInt((short) rs.getInt("int"));
                equip.setJump((short) rs.getInt("jump"));
                equip.setLuk((short) rs.getInt("luk"));
                equip.setMatk((short) rs.getInt("matk"));
                equip.setMdef((short) rs.getInt("mdef"));
                equip.setMp((short) rs.getInt("mp"));
                equip.setSpeed((short) rs.getInt("speed"));
                equip.setStr((short) rs.getInt("str"));
                equip.setWatk((short) rs.getInt("watk"));
                equip.setWdef((short) rs.getInt("wdef"));
                equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                equip.setLocked((byte) rs.getInt("locked"));
                equip.setLevel((byte) rs.getInt("level"));
                equip.setExpiration(rs.getLong("expiration"));
                ret.getInventory(type).addFromDB(equip);
            } else {
                Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) rs.getInt("quantity"), rs.getInt("petid"));
                item.setOwner(rs.getString("owner"));
                item.setExpiration(rs.getLong("expiration"));
                
                ret.getInventory(type).addFromDB(item);
            }
        }
        rs.close();
        ps.close();
        // itemLog.close();

         if (channelserver) {
                ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                PreparedStatement pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
                while (rs.next()) {
                    MapleQuest q = MapleQuest.getInstance(rs.getInt("quest"));
                    MapleQuestStatus status = new MapleQuestStatus(q, MapleQuestStatus.Status.getById(rs.getInt("status")));
                    long cTime = rs.getLong("time");
                    if (cTime > -1) {
                        status.setCompletionTime(cTime * 1000);
                    }
                    status.setForfeited(rs.getInt("forfeited"));
                    ret.quests.put(q, status);
                    pse.setInt(1, rs.getInt("queststatusid"));
                    ResultSet rsMobs = pse.executeQuery();
                    while (rsMobs.next()) {
                        status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                    }
                    rsMobs.close();
                }
                rs.close();
                ps.close();
                pse.close();

            ps = con.prepareStatement("SELECT skillid,skilllevel,masterlevel FROM skills WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.skills.put(SkillFactory.getSkill(rs.getInt("skillid")), new SkillEntry(rs.getInt("skilllevel"), rs.getInt("masterlevel")));
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int skill1 = rs.getInt("skill1");
                int skill2 = rs.getInt("skill2");
                int skill3 = rs.getInt("skill3");
                String name = rs.getString("name");
                int shout = rs.getInt("shout");
                int position = rs.getInt("position");
                SkillMacro macro = new SkillMacro(skill1, skill2, skill3, name, shout, position);
                ret.skillMacros[position] = macro;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                int key = rs.getInt("key");
                int type = rs.getInt("type");
                int action = rs.getInt("action");
                ret.keymap.put(Integer.valueOf(key), new MapleKeyBinding(type, action));
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            while (rs.next()) {
                String locationType = rs.getString("locationtype");
                int mapid = rs.getInt("map");
                ret.savedLocations[SavedLocationType.valueOf(locationType).ordinal()] = mapid;
            }
            rs.close();
            ps.close();

            ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            ret.lastfametime = 0;
            ret.lastmonthfameids = new ArrayList<Integer>(31);
            while (rs.next()) {
                ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
            }
            rs.close();
            ps.close();

            ret.buddylist.loadFromDb(charid);
            ret.storage = MapleStorage.loadOrCreateFromDB(ret.accountid);
        }
       if (ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) {
            ret.maplemount = new MapleMount(ret, ret.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId(), 1004);
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
        } else {
            ret.maplemount = new MapleMount(ret, 0, 1004);
            ret.maplemount.setExp(mountexp);
            ret.maplemount.setLevel(mountlevel);
            ret.maplemount.setTiredness(mounttiredness);
            ret.maplemount.setActive(false);
        }
        ret.recalcLocalStats();
        ret.silentEnforceMaxHpMp();
        return ret;
    }
    
     public void changeOccupation(LeaderOccupations newoccupation) {
        this.occupation = newoccupation;
    }

    public void Setoccupation(int occ) {
        changeOccupation(LeaderOccupations.getById(occ));
    }

    public LeaderOccupations getOccupation() {
        return occupation;
    }

    public static MapleCharacter getDefault(MapleClient client, int chrid) {
        MapleCharacter ret = getDefault(client);
        ret.id = chrid;
        return ret;
    }

    public static MapleCharacter getDefault(MapleClient client) {
        MapleCharacter ret = new MapleCharacter();
        ret.client = client;
        ret.hp = 50;
        ret.maxhp = 50;
        ret.mp = 5;
        ret.maxmp = 5;
        ret.map = null;
        ret.exp.set(0);
        ret.gmLevel = 0;
        ret.job = MapleJob.BEGINNER;
        ret.occupation = LeaderOccupations.Iniciante;
        ret.meso.set(0);
        ret.level = 1;
        ret.accountid = client.getAccID();
        ret.buddylist = new BuddyList(20);
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            ResultSet rs = ps.executeQuery();
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.getClient().setAccountName(rs.getString("name"));
                ret.paypalnx = rs.getInt("paypalNX");
                ret.maplepoints = rs.getInt("mPoints");
                ret.cardnx = rs.getInt("cardNX");
             //   ret.BetaPoints = rs.getInt("BetaPoints");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            log.error("ERROR", e);
        }
        ret.incs = false;
        ret.inmts = false;
        ret.maplemount = null;

        ret.keymap.put(Integer.valueOf(2), new MapleKeyBinding(4, 10));
        ret.keymap.put(Integer.valueOf(3), new MapleKeyBinding(4, 12));
        ret.keymap.put(Integer.valueOf(4), new MapleKeyBinding(4, 13));
        ret.keymap.put(Integer.valueOf(5), new MapleKeyBinding(4, 18));
        ret.keymap.put(Integer.valueOf(6), new MapleKeyBinding(4, 24));
        ret.keymap.put(Integer.valueOf(7), new MapleKeyBinding(4, 21));
        ret.keymap.put(Integer.valueOf(16), new MapleKeyBinding(4, 8));
        ret.keymap.put(Integer.valueOf(17), new MapleKeyBinding(4, 5));
        ret.keymap.put(Integer.valueOf(18), new MapleKeyBinding(4, 0));
        ret.keymap.put(Integer.valueOf(19), new MapleKeyBinding(4, 4));
        ret.keymap.put(Integer.valueOf(23), new MapleKeyBinding(4, 1));
        ret.keymap.put(Integer.valueOf(25), new MapleKeyBinding(4, 19));
        ret.keymap.put(Integer.valueOf(26), new MapleKeyBinding(4, 14));
        ret.keymap.put(Integer.valueOf(27), new MapleKeyBinding(4, 15));
        ret.keymap.put(Integer.valueOf(29), new MapleKeyBinding(5, 52));
        ret.keymap.put(Integer.valueOf(31), new MapleKeyBinding(4, 2));
        ret.keymap.put(Integer.valueOf(34), new MapleKeyBinding(4, 17));
        ret.keymap.put(Integer.valueOf(35), new MapleKeyBinding(4, 11));
        ret.keymap.put(Integer.valueOf(37), new MapleKeyBinding(4, 3));
        ret.keymap.put(Integer.valueOf(38), new MapleKeyBinding(4, 20));
        ret.keymap.put(Integer.valueOf(40), new MapleKeyBinding(4, 16));
        ret.keymap.put(Integer.valueOf(41), new MapleKeyBinding(4, 23));
        ret.keymap.put(Integer.valueOf(43), new MapleKeyBinding(4, 9));
        ret.keymap.put(Integer.valueOf(44), new MapleKeyBinding(5, 50));
        ret.keymap.put(Integer.valueOf(45), new MapleKeyBinding(5, 51));
        ret.keymap.put(Integer.valueOf(46), new MapleKeyBinding(4, 6));
        ret.keymap.put(Integer.valueOf(48), new MapleKeyBinding(4, 22));
        ret.keymap.put(Integer.valueOf(50), new MapleKeyBinding(4, 7));
        ret.keymap.put(Integer.valueOf(56), new MapleKeyBinding(5, 53));
        ret.keymap.put(Integer.valueOf(57), new MapleKeyBinding(5, 54));
        ret.keymap.put(Integer.valueOf(59), new MapleKeyBinding(6, 100));
        ret.keymap.put(Integer.valueOf(60), new MapleKeyBinding(6, 101));
        ret.keymap.put(Integer.valueOf(61), new MapleKeyBinding(6, 102));
        ret.keymap.put(Integer.valueOf(62), new MapleKeyBinding(6, 103));
        ret.keymap.put(Integer.valueOf(63), new MapleKeyBinding(6, 104));
        ret.keymap.put(Integer.valueOf(64), new MapleKeyBinding(6, 105));
        ret.keymap.put(Integer.valueOf(65), new MapleKeyBinding(6, 106));

        ret.recalcLocalStats();

        return ret;
    }

    public void save() {
        Connection con = DatabaseConnection.getConnection();
        try {
            // clients should not be able to log back before their old state is saved (see MapleClient#getLoginState) so we are save to switch to a very low isolation level here
            con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            // connections are thread local now, no need to synchronize anymore =)
            con.setAutoCommit(false);
            PreparedStatement ps;
            ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpApUsed = ?, mpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, mountlevel = ?, mountexp = ?, mounttiredness = ? WHERE id = ?");

            ps.setInt(1, level);
            ps.setInt(2, fame);
            ps.setInt(3, str);
            ps.setInt(4, dex);
            ps.setInt(5, luk);
            ps.setInt(6, int_);
            ps.setInt(7, exp.get());
            ps.setInt(8, hp);
            ps.setInt(9, mp);
            ps.setInt(10, maxhp);
            ps.setInt(11, maxmp);
            ps.setInt(12, remainingSp);
            ps.setInt(13, remainingAp);
            ps.setInt(14, gmLevel);
            ps.setInt(15, skinColor.getId());
            ps.setInt(16, gender);
            ps.setInt(17, job.getId());
            ps.setInt(18, hair);
            ps.setInt(19, face);
            if (map == null) {
                ps.setInt(20, 0);
            } else {
                if (map.getForcedReturnId() != 999999999) {
                    ps.setInt(20, map.getForcedReturnId());
                } else {
                    ps.setInt(20, map.getId());
                }
            }
            ps.setInt(21, meso.get());
            ps.setInt(22, hpApUsed);
            ps.setInt(23, mpApUsed);
            if (map == null) {
                ps.setInt(24, 0);
            } else {
                MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                if (closest != null) {
                    ps.setInt(24, closest.getId());
                } else {
                    ps.setInt(24, 0);
                }
            }
            if (party != null) {
                ps.setInt(25, party.getId());
            } else {
                ps.setInt(25, -1);
            }
            ps.setInt(26, buddylist.getCapacity());
            if (messenger != null) {
                ps.setInt(27, messenger.getId());
                ps.setInt(28, messengerposition);
            } else {
                ps.setInt(27, 0);
                ps.setInt(28, 4);
            }
             if (maplemount != null) {
                ps.setInt(29, maplemount.getLevel());
                ps.setInt(30, maplemount.getExp());
                ps.setInt(31, maplemount.getTiredness());
            } else {
                ps.setInt(29, 1);
                ps.setInt(30, 0);
                ps.setInt(31, 0);
            }
            
            ps.setInt(32, id);

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                this.id = rs.getInt(1);
            } else {
                throw new DatabaseException("Inserting char failed.");
            }

            ps.close();

            con.commit();
        } catch (Exception e) {
            log.error(MapleClient.getLogMessage(this, "[charsave] Error saving character data"), e);
            try {
                con.rollback();
            } catch (SQLException e1) {
                log.error(MapleClient.getLogMessage(this, "[charsave] Error Rolling Back"), e);
            }
        } finally {
            try {
                con.setAutoCommit(true);
                con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } catch (SQLException e) {
                log.error(MapleClient.getLogMessage(this, "[charsave] Error going back to autocommit mode"), e);
            }
        }
    }

  public void saveToDB(boolean update, boolean full) {
        try {
            save_mutex.lock();
            Connection con = DatabaseConnection.getConnection();
            try {
                con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                con.setAutoCommit(false);
                PreparedStatement ps;
            if (update) {
                ps = con.prepareStatement("UPDATE characters " + "SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, " + "exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, " + "gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, " + "meso = ?, hpApUsed = ?, mpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, married = ?, partnerid = ?, cantalk = ?, zakumlvl = ?, marriagequest = ?, mountlevel = ?, mountexp = ?, mounttiredness = ?, alliancerank = ?, LeaderPoints = ?, pqPoints = ?, votePoints = ?, occupation = ?, jqpoints = ?, CashPoints = ?, jqrank =?, bosspoints = ? WHERE id = ?");
             } else {
                ps = con.prepareStatement("INSERT INTO characters (" + "level, fame, str, dex, luk, `int`, exp, hp, mp, " + "maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpApUsed, mpApUsed, spawnpoint, party, buddyCapacity, messengerid, messengerposition, married, partnerid, cantalk, zakumlvl, marriagequest,  mountlevel, mounttiredness, mountexp, alliancerank, LeaderPoints, pqPoints, votePoints, occupation, jqpoints, CashPoints, jqrank, bosspoints, accountid, name, world" + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            ps.setInt(1, level);
            ps.setInt(2, fame);
            ps.setInt(3, str);
            ps.setInt(4, dex);
            ps.setInt(5, luk);
            ps.setInt(6, int_);
            ps.setInt(7, exp.get());
            ps.setInt(8, hp);
            ps.setInt(9, mp);
            ps.setInt(10, maxhp);
            ps.setInt(11, maxmp);
            ps.setInt(12, remainingSp);
            ps.setInt(13, remainingAp);
            ps.setInt(14, gmLevel);
            ps.setInt(15, skinColor.getId());
            ps.setInt(16, gender);
            ps.setInt(17, job.getId());
            ps.setInt(18, hair);
            ps.setInt(19, face);
            if (map == null) {
                ps.setInt(20, 0);
            } else {
                if (map.getForcedReturnId() != 999999999) {
                    ps.setInt(20, map.getForcedReturnId());
                } else {
                    ps.setInt(20, map.getId());
                }
            }
            ps.setInt(21, meso.get());
            ps.setInt(22, hpApUsed);
            ps.setInt(23, mpApUsed);
            if (map == null || map.getId() == 610020000 || map.getId() == 610020001) {
                ps.setInt(24, 0);
            } else {
                MaplePortal closest = map.findClosestSpawnpoint(getPosition());
                if (closest != null) {
                    ps.setInt(24, closest.getId());
                } else {
                    ps.setInt(24, 0);
                }
            }
            if (party != null) {
                ps.setInt(25, party.getId());
            } else {
                ps.setInt(25, -1);
            }
            ps.setInt(26, buddylist.getCapacity());

            if (messenger != null) {
                ps.setInt(27, messenger.getId());
                ps.setInt(28, messengerposition);
            } else {
                ps.setInt(27, 0);
                ps.setInt(28, 4);
            }

            ps.setInt(29, married);
            ps.setInt(30, partnerid);
            ps.setInt(31, canTalk);
            if (zakumLvl <= 2) //Don't let zakumLevel exceed three ;)
            {
                ps.setInt(32, zakumLvl);
            } else {
                ps.setInt(32, 2);
            }
            ps.setInt(33, marriageQuestLevel);

            if (false) {
            } else {
                ps.setInt(34, 1);
                ps.setInt(35, 0);
                ps.setInt(36, 0);
            }
            ps.setInt(37, this.allianceRank);
            ps.setInt(38, LeaderPoints);
            ps.setInt(39, pqPoints);
            ps.setInt(40, votePoints);
            ps.setInt(41, occupation.getId());
            ps.setInt(42, jqpoints);
            ps.setInt(43, CashPoints);
            ps.setInt(44, jqrank);
            ps.setInt(45, jqrank);
            if (update) {
                ps.setInt(46, id);
            } else {
                ps.setInt(46, accountid);
                ps.setString(47, name);
                ps.setInt(48, world); // TODO store world somewhere ;)

            }
            if (!full) {
                ps.executeUpdate();
                ps.close();
            } else {
                int updateRows = ps.executeUpdate();
                if (!update) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        this.id = rs.getInt(1);
                    } else {
                        throw new DatabaseException("Inserting char failed.");
                    }
                    rs.close();
                } else if (updateRows < 1) {
                    throw new DatabaseException("Character not in database (" + id + ")");
                }
                ps.close();
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        pets[i].saveToDb();
                    } else {
                        break;
                    }
                }
                ps.close();
                deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
                for (int i = 0; i < 5; i++) {
                    SkillMacro macro = skillMacros[i];
                    if (macro != null) {
                        ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
                        ps.setInt(1, id);
                        ps.setInt(2, macro.getSkill1());
                        ps.setInt(3, macro.getSkill2());
                        ps.setInt(4, macro.getSkill3());
                        ps.setString(5, macro.getName());
                        ps.setInt(6, macro.getShout());
                        ps.setInt(7, i);
                        ps.executeUpdate();
                        ps.close();
                    }
            }
           deleteWhereCharacterId(con, "DELETE FROM inventoryitems WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO inventoryitems (characterid, itemid, inventorytype, position, quantity, owner, petid, expiration) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                PreparedStatement pse = con.prepareStatement("INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                for (MapleInventory iv : inventory) {
                ps.setInt(3, iv.getType().getType());
                for (IItem item : iv.list()) {
                    ps.setInt(1, id);
                    ps.setInt(2, item.getItemId());
                    ps.setInt(4, item.getPosition());
                    ps.setInt(5, item.getQuantity());
                    ps.setString(6, item.getOwner());
                    ps.setInt(7, item.getPetId());
                    ps.setLong(8, item.getExpiration());
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    int itemid;
                    if (rs.next()) {
                        itemid = rs.getInt(1);
                    } else {
                        throw new DatabaseException("Inserting char failed.");
                    }
                        rs.close();
                        if (iv.getType().equals(MapleInventoryType.EQUIP) || iv.getType().equals(MapleInventoryType.EQUIPPED)) {
                            pse.setInt(1, itemid);
                            IEquip equip = (IEquip) item;
                            pse.setInt(2, equip.getUpgradeSlots());
                            pse.setInt(3, equip.getLevel());
                            pse.setInt(4, equip.getStr());
                            pse.setInt(5, equip.getDex());
                            pse.setInt(6, equip.getInt());
                            pse.setInt(7, equip.getLuk());
                            pse.setInt(8, equip.getHp());
                            pse.setInt(9, equip.getMp());
                            pse.setInt(10, equip.getWatk());
                            pse.setInt(11, equip.getMatk());
                            pse.setInt(12, equip.getWdef());
                            pse.setInt(13, equip.getMdef());
                            pse.setInt(14, equip.getAcc());
                            pse.setInt(15, equip.getAvoid());
                            pse.setInt(16, equip.getHands());
                            pse.setInt(17, equip.getSpeed());
                            pse.setInt(18, equip.getJump());
                            pse.setInt(19, equip.getRingId());
                            pse.setInt(20, equip.getLocked());
                            pse.executeUpdate();
                        }
                    }
                }
            ps.close();
            pse.close();
            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`) VALUES (DEFAULT, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
                ps.setInt(1, id);
                for (MapleQuestStatus q : quests.values()) {
                    ps.setInt(2, q.getQuest().getId());
                    ps.setInt(3, q.getStatus().getId());
                    ps.setInt(4, (int) (q.getCompletionTime() / 1000));
                    ps.setInt(5, q.getForfeited());
                    ps.executeUpdate();
                    ResultSet rs = ps.getGeneratedKeys();
                    rs.next();
                    for (int mob : q.getMobKills().keySet()) {
                        pse.setInt(1, rs.getInt(1));
                        pse.setInt(2, mob);
                        pse.setInt(3, q.getMobKills(mob));
                        pse.executeUpdate();
                    }
                    rs.close();
                }
             ps.close();
             pse.close();
             deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel) VALUES (?, ?, ?, ?)");
                ps.setInt(1, id);
                for (Entry<ISkill, SkillEntry> skill_ : skills.entrySet()) {
                    ps.setInt(2, skill_.getKey().getId());
                    ps.setInt(3, skill_.getValue().skillevel);
                    ps.setInt(4, skill_.getValue().masterlevel);
                    ps.executeUpdate();
                }
                ps.close();
                deleteWhereCharacterId(con, "DELETE FROM keymap WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
                ps.setInt(1, id);
                for (Entry<Integer, MapleKeyBinding> keybinding : keymap.entrySet()) {
                    ps.setInt(2, keybinding.getKey().intValue());
                    ps.setInt(3, keybinding.getValue().getType());
                    ps.setInt(4, keybinding.getValue().getAction());
                    ps.executeUpdate();
                }
                ps.close();
                deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
                ps.setInt(1, id);
                for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                    if (savedLocations[savedLocationType.ordinal()] != -1) {
                        ps.setString(2, savedLocationType.name());
                        ps.setInt(3, savedLocations[savedLocationType.ordinal()]);
                        ps.executeUpdate();
                    }
                }
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ? AND pending = 0");
                ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, 0)");
                ps.setInt(1, id);
                for (BuddylistEntry entry : buddylist.getBuddies()) {
                    if (entry.isVisible()) {
                        ps.setInt(2, entry.getCharacterId());
                        ps.executeUpdate();
                    }
                }
             ps.close();
             ps = con.prepareStatement("UPDATE accounts SET `paypalNX` = ?, `mPoints` = ?, `cardNX` = ?, `donatorpoints` = ?, `BetaPoints` = ? WHERE id = ?");
                ps.setInt(1, paypalnx);
                ps.setInt(2, maplepoints);
                ps.setInt(3, cardnx);
                ps.setInt(4, donatorPoints);
                ps.setInt(5, BetaPoints);
                ps.setInt(6, client.getAccID());
                ps.executeUpdate();
                ps.close();
            if (storage != null) {
                    storage.saveToDB();
                }
            }
            con.commit();
        } catch (Exception e) {
                log.error(MapleClient.getLogMessage(this, "[charsave] Error saving character data"), e);
                try {
                    con.rollback();
                } catch (SQLException e1) {
                    log.error(MapleClient.getLogMessage(this, "[charsave] Error Rolling Back"), e);
                }
            } finally {
                try {
                    con.setAutoCommit(true);
                    con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                } catch (SQLException e) {
                    log.error(MapleClient.getLogMessage(this, "[charsave] Error going back to autocommit mode"), e);
                }
            }
        } finally {
            save_mutex.unlock();
        }
  }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }
    
    public MapleQuestStatus getQuest(MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            return new MapleQuestStatus(quest, MapleQuestStatus.Status.NOT_STARTED);
        }
        return quests.get(quest);
    }

    public void updateQuest(MapleQuestStatus quest) {
        quests.put(quest.getQuest(), quest);
        if (quest.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
            client.getSession().write(MaplePacketCreator.startQuest(this, (short) quest.getQuest().getId()));
            client.getSession().write(MaplePacketCreator.updateQuestInfo(this, (short) quest.getQuest().getId(), quest.getNpc(), (byte) 8));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
            client.getSession().write(MaplePacketCreator.completeQuest(this, (short) quest.getQuest().getId()));
        } else if (quest.getStatus().equals(MapleQuestStatus.Status.NOT_STARTED)) {
            client.getSession().write(MaplePacketCreator.forfeitQuest(this, (short) quest.getQuest().getId()));
        }
    }

    public final MapleQuestStatus getQuestNAdd(final MapleQuest quest) {
        if (!quests.containsKey(quest)) {
            final MapleQuestStatus status;
            status = new MapleQuestStatus(quest, (MapleQuestStatus.Status.NOT_STARTED));
            quests.put(quest, status);
            return status;
        }
        return quests.get(quest);
    }
     
    public void expirationTask() {
        long expiration, currenttime = System.currentTimeMillis();

        List<IItem> toberemove = new ArrayList<>(); 

        for (MapleInventory inv : inventory) {
            for (IItem item : inv.list()) {
                expiration = item.getExpiration();

                if (expiration != -1 && expiration != 0) {
                    if (currenttime > expiration) {
                        toberemove.add(item);
                    }
                }
            }

            for (IItem item : toberemove) {
                dropMessage("[Cash Item] An item reached its expiration date, so it was removed from your inventory.");
                MapleInventoryManipulator.removeFromSlot(client, inv.getType(), item.getPosition(), item.getQuantity(), true);
            }

            toberemove.clear();
        }
    }

   public void setBossPoints(int points) {
        bossPoints = points;
    }
    public int getBossPoints() {
        return bossPoints;
    }


    public static int getIdByName(String name, int world) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT id FROM characters WHERE name = ? AND world = ?");
            ps.setString(1, name);
            ps.setInt(2, world);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id = rs.getInt("id");
            rs.close();
            ps.close();
            return id;
        } catch (Exception e) {
        }
        return -1;
    }
    
        
     
    

    public boolean isActiveBuffedValue(int skillid) {
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.value);
    }

    public boolean isBuffFrom(MapleBuffStat stat, ISkill skill) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return false;
        }
        return mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = effects.get(stat);
        if (mbsvh == null) {
            return -1;
        }
        return mbsvh.effect.getSourceId();
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
        MapleInventory iv = inventory[type.ordinal()];
        int possesed = iv.countById(itemid);
        if (checkEquipped) {
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }

        return possesed;
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Long.valueOf(mbsvh.startTime);
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return mbsvh.effect;
    }
    
      private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        dragonBloodSchedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                addHP(-bloodEffect.getX());
                getClient().getSession().write(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
                getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), bloodEffect.getSourceId(), 5), false);
                checkBerserk();
            }
        }, 4000, 4000);
    }

     public void startFullnessSchedule(final int decrease, final MaplePet pet, int petSlot) {
        ScheduledFuture<?> schedule = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (pet != null) {
                    int newFullness = pet.getFullness() - decrease;
                    if (newFullness <= 5) {
                        pet.setFullness(15);
                        unequipPet(pet, true, true);
                    } else {
                        pet.setFullness(newFullness);
                        getClient().getSession().write(MaplePacketCreator.updatePet(pet, true));
                    }
                }
            }
        }, 60000, 60000);
        switch (petSlot) {
            case 0:
                fullnessSchedule = schedule;
                break;
            case 1:
                fullnessSchedule_1 = schedule;
                break;
            case 2:
                fullnessSchedule_2 = schedule;
                break;
            default:
                break;
        }
    }


    public void cancelFullnessSchedule(int petSlot) {
        switch (petSlot) {
            case 0:
                if (fullnessSchedule != null) fullnessSchedule.cancel(false);
            case 1:
                if (fullnessSchedule_1 != null) fullnessSchedule_1.cancel(false);
            case 2:
                if (fullnessSchedule_2 != null) fullnessSchedule_2.cancel(false);
            default:
                break;
        }
    }


   public void startMapTimeLimitTask(final MapleMap from, final MapleMap to) {
        if (to.getTimeLimit() > 0 && from != null) {
            final MapleCharacter chr = this;
            mapTimeLimitTask = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    MaplePortal pfrom = null;
                    if (from.isMiniDungeonMap()) {
                        pfrom = from.getPortal("MD00");
                    } else {
                        pfrom = from.getPortal(0);
                    }
                    if (pfrom != null) {
                        chr.changeMap(from, pfrom);
                    }
                }
            }, from.getTimeLimit() * 1000, from.getTimeLimit() * 1000);
        }
    }

    public void cancelMapTimeLimitTask() {
        if (mapTimeLimitTask != null) {
            mapTimeLimitTask.cancel(false);
        }
    }

       public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule) {
	if (effect.isHide()) {
	    this.hidden = true;
	    map.broadcastMessage(this, MaplePacketCreator.removePlayerFromMap(getId()), false);
	} else if (effect.isDragonBlood()) {
	    prepareDragonBlood(effect);
	} else if (effect.isBerserk()) {
	    checkBerserk();
	} else if (effect.isBeholder()) {
	    prepareBeholderEffect();
	}
	for (Pair<MapleBuffStat, Integer> statup : effect.getStatups()) {
	    effects.put(statup.getLeft(), new MapleBuffStatValueHolder(effect, starttime, schedule, statup.getRight().intValue()));
	}
	recalcLocalStats();
    }
    
        public int getSlot() {
        return slots;
    }

    public int getSlots(byte b) {
        for (Pair curPair : inventorySlots)
            if ((Byte) curPair.getLeft() == b)
                return (Integer) curPair.getRight();
        return 100;
    }

    public int getSlots(MapleInventoryType t) {
        return getSlots(t.getType());
    }

        private List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List<MapleBuffStat> stats = new ArrayList<MapleBuffStat>();
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : effects.entrySet()) {
            if (stateffect.getValue().effect.sameSource(effect) && (startTime == -1 || startTime == stateffect.getValue().startTime)) {
                stats.add(stateffect.getKey());
            }
        }
        return stats;
    }

   
    private void deregisterBuffStats(List<MapleBuffStat> stats) {
        synchronized (stats) {
            List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
            for (MapleBuffStat stat : stats) {
                MapleBuffStatValueHolder mbsvh = effects.get(stat);
                if (mbsvh != null) {
                    effects.remove(stat);
                    boolean addMbsvh = true;
                    for (MapleBuffStatValueHolder contained : effectsToCancel) {
                        if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                            addMbsvh = false;
                        }
                    }
                    if (addMbsvh) {
                        effectsToCancel.add(mbsvh);
                    }
                    if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                        int summonId = mbsvh.effect.getSourceId();
                        MapleSummon summon = summons.get(summonId);
                        if (summon != null) {
                            getMap().broadcastMessage(MaplePacketCreator.removeSpecialMapObject(summon, true), summon.getPosition());
                            getMap().removeMapObject(summon);
                            removeVisibleMapObject(summon);
                            summons.remove(summonId);
                        }
                        if (summon.getSkill() == DarkKnight.BEHOLDER) {
                            if (beholderHealingSchedule != null) {
                                beholderHealingSchedule.cancel(false);
                                beholderHealingSchedule = null;
                            }
                            if (beholderBuffSchedule != null) {
                                beholderBuffSchedule.cancel(false);
                                beholderBuffSchedule = null;
                            }
                        }
                    } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                        dragonBloodSchedule.cancel(false);
                        dragonBloodSchedule = null;
                    }
                }
            }
            for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
                if (getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).isEmpty()) {
                    cancelEffectCancelTasks.schedule.cancel(false);
                }
            }
        }
    }
    // Adcionado por Java =3
    public int[] pqmaps = {
        /* Ludi PQ */
        922010100, 922010200, 922010300, 922010400, 922010500, 922010600, 922010700, 922010800, 922010900, 922011000,/* Ludi fim */
        /* Henesys PQ */
        910010000, 910010200, 910010300, 910010400,/* Henesys fim */
        /* Orbis PQ */
        920010100, 920010200, 920010300, 920010400, 920010500, 920010600, 920010700, 920010800, 920010900, 920011000, 920011100, 920011300,/* Orbis fim */
        /* Ariant PQ */
        980010010, 980010020, 980010101, 980010201, 980010301,/* Ariant fim */
        /* Kerning PQ */
        103000800, 103000801, 103000802, 103000803, 103000804, 103000890,/* Kerning PQ */
        /* Ludi Maze PQ */
        809050000, 809050001, 809050002, 809050003, 809050004, 809050005, 809050006, 809050007, 809050008, 809050009, 809050010, 809050011, 809050012, 809050013, 809050014, 809050015, 809050016, 809050017,/* Ludi Maze fim */
        /* Carnaval de Monstros */
        980000100, 980000101, 980000102, 980000103, 980000104, 980000200, 980000201, 980000202, 980000203, 980000204, 980000300, 980000301, 980000302, 980000303, 980000304, 980000400, 980000401, 980000402, 980000403, 980000404, 980000500, 980000501, 980000502, 980000503, 980000504, 980000600, 980000601, 980000602, 980000603, 980000604,/* CPQ fim */
        /* Maple Road */
        1, 2, 3, 4,
        /* CashPQ */
        107000200,
        /* Mapa Nulo */
        0
    };

    public boolean inPQ() {
        boolean inpq = false;
        for (int i = 0; i < pqmaps.length; i++) {
            if (getMapId() == pqmaps[i]) {
                inpq = true;
            }
        }
        return inpq;
    }
    
    public int[] hpqmaps = {
         /* Henesys PQ */
        910010000, 910010200, 910010300, 910010400,/* Henesys fim */
        0
    };
    
    public boolean inHPQ() {
        boolean inhpq = false;
        for (int i = 0; i < hpqmaps.length; i++) {
            if (getMapId() == hpqmaps[i]) {
                inhpq = true;
            }
        }
        return inhpq;
    }
    
    
    /**
     * @param effect
     * @param overwrite when overwrite is set no data is sent and all the Buffstats in the StatEffect are deregistered
     * @param startTime
     */


   public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            List<Pair<MapleBuffStat, Integer>> statups = effect.getStatups();
            buffstats = new ArrayList<>(statups.size());
            for (Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!getDoors().isEmpty()) {
                MapleDoor door = getDoors().iterator().next();
                for (MapleCharacter chr : door.getTarget().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleCharacter chr : door.getTown().getCharacters()) {
                    door.sendDestroyData(chr.client);
                }
                for (MapleDoor destroyDoor : getDoors()) {
                    door.getTarget().removeMapObject(destroyDoor);
                    door.getTown().removeMapObject(destroyDoor);
                }
                clearDoors();
                silentPartyUpdate();
            }
        }
        if (effect.getSourceId() == Spearman.HYPER_BODY || effect.getSourceId() == GameMaster.HYPER_BODY || effect.getSourceId() == SuperGM.HYPER_BODY) {
            List<Pair<MapleStat, Integer>> statup = new ArrayList<>(4);
            statup.add(new Pair<>(MapleStat.HP, Math.min(hp, maxhp)));
            statup.add(new Pair<>(MapleStat.MP, Math.min(mp, maxmp)));
            statup.add(new Pair<>(MapleStat.MAXHP, maxhp));
            statup.add(new Pair<>(MapleStat.MAXMP, maxmp));
            client.announce(MaplePacketCreator.updatePlayerStats(statup));
        }
        if (effect.isMonsterRiding()) {
            if (effect.getSourceId() != Corsair.BATTLE_SHIP) {
                this.getMount().cancelSchedule();
                this.getMount().setActive(false);
            }
        }
         if (!overwrite) {
            cancelPlayerBuffs(buffstats);
            if (effect.isHide() && (MapleCharacter) getMap().getMapObject(getObjectId()) != null) {
                this.hidden = false;
                getMap().broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                for (int i = 0; i < 3; i++) {
                    if (pets[i] != null) {
                        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pets[i], false, false), false);
                    }
                }
            }
        }
    }

    public void removeDoor() {
        final MapleDoor door = getDoors().iterator().next();
        for (final MapleCharacter chr : door.getTarget().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (final MapleCharacter chr : door.getTown().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (final MapleDoor destroyDoor : getDoors()) {
            door.getTarget().removeMapObject(destroyDoor);
            door.getTown().removeMapObject(destroyDoor);
        }
        clearDoors();
    }
  
  
  
      public boolean isFake() {
        return this.isfake;
    }
  
      public void setOffOnline(boolean online) {
        try {
            WorldChannelInterface wci = client.getChannelServer().getWorldInterface();
            if (online) {
                wci.loggedOn(getName(), getId(), client.getChannel(), getBuddylist().getBuddyIds());
            } else {
                wci.loggedOff(getName(), getId(), client.getChannel(), getBuddylist().getBuddyIds());
            }
        } catch (RemoteException e) {
            client.getChannelServer().reconnectWorld();
        }
    }

    public void cancelBuffStats(MapleBuffStat stat) {
        List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList);
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        cancelEffect(effects.get(stat).effect, false, -1);
    }

     private void cancelPlayerBuffs(List<MapleBuffStat> buffstats) {
        if (client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
            recalcLocalStats();
            enforceMaxHpMp();
            client.announce(MaplePacketCreator.cancelBuff(buffstats));
            if (buffstats.size() > 0) {
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), buffstats), false);
            }
        }
    }

    public void dispel() {
        if (!isHidden()) {
            final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
            for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                if (mbsvh.effect.isSkill() && mbsvh.schedule != null && !mbsvh.effect.isMorph() && !mbsvh.effect.isGmBuff() && !mbsvh.effect.isMonsterRiding()) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
        }
    }

    public void cancelAllBuffs() {
        for (MapleBuffStatValueHolder mbsvh : new LinkedList<MapleBuffStatValueHolder>(effects.values()))
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
    }

    public void cancelMorphs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMorph() && mbsvh.effect.getSourceId() != 5111005 && mbsvh.effect.getSourceId() != 5121003) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime);
        }
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        for (MapleBuffStatValueHolder mbsvh : effects.values()) {
            ret.add(new PlayerBuffValueHolder(mbsvh.startTime, mbsvh.effect));
        }
        return ret;
    }

    public void cancelMagicDoor() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void handleOrbgain() {
        int orbcount = getBuffedValue(MapleBuffStat.COMBO);
        ISkill combo = SkillFactory.getSkill(1111002);
        ISkill advcombo = SkillFactory.getSkill(1120003);

        MapleStatEffect ceffect = null;
        int advComboSkillLevel = getSkillLevel(advcombo);
        if (advComboSkillLevel > 0) {
            ceffect = advcombo.getEffect(advComboSkillLevel);
        } else {
            ceffect = combo.getEffect(getSkillLevel(combo));
        }

        if (orbcount < ceffect.getX() + 1) {
            int neworbcount = orbcount + 1;
            if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
                if (neworbcount < ceffect.getX() + 1) {
                    neworbcount++;
                }
            }

            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, neworbcount));
            setBuffedValue(MapleBuffStat.COMBO, neworbcount);
            int duration = ceffect.getDuration();
            duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

            getClient().getSession().write(MaplePacketCreator.giveBuff(1111002, duration, stat));
            getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, false), false);
        }
    }

    public void handleOrbconsume() {
        ISkill combo = SkillFactory.getSkill(1111002);
        MapleStatEffect ceffect = combo.getEffect(getSkillLevel(combo));
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, 1));
        setBuffedValue(MapleBuffStat.COMBO, 1);
        int duration = ceffect.getDuration();
        duration += (int) ((getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis()));

        getClient().getSession().write(MaplePacketCreator.giveBuff(1111002, duration, stat, false, false, getMount()));
        getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(getId(), stat, false), false);
    }

    private void silentEnforceMaxHpMp() {
        setMp(getMp());
        setHp(getHp(), true);
    }
    
     public int getBossLog(String bossid) {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            int ret_count = 0;
            PreparedStatement ps;
            ps = con1.prepareStatement("select count(*) from bosslog where characterid = ? and bossid = ? and lastattempt >= subtime(current_timestamp, '1 0:0:0.0')");
            ps.setInt(1, id);
            ps.setString(2, bossid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) 
                ret_count = rs.getInt(1);
            else
                ret_count = -1;
            rs.close();
            ps.close();
            return ret_count;
        } catch (Exception Ex) {
            return -1;
        }
    }

    public int getGiftLog(String bossid) {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            int ret_count = 0;
            PreparedStatement ps;
            ps = con1.prepareStatement("select count(*) from bosslog where accountid = ? and bossid = ? and lastattempt >= subtime(current_timestamp, '1 0:0:0.0')");
            ps.setInt(1, accountid);
            ps.setString(2, bossid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) 
                ret_count = rs.getInt(1);
            else
                ret_count = -1;
            rs.close();
            ps.close();
            return ret_count;
        } catch (Exception Ex) {
            return -1;
        }
    }

    //setBossLog module
    public void setBossLog(String bossid) {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps;
            ps = con1.prepareStatement("insert into bosslog (accountid, characterid, bossid) values (?,?,?)");
            ps.setInt(1, accountid);
            ps.setInt(2, id);
            ps.setString(3, bossid);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    private void enforceMaxHpMp() {
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>(2);
        if (getMp() > getCurrentMaxMp()) {
            setMp(getMp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(getMp())));
        }
        if (getHp() > getCurrentMaxHp()) {
            setHp(getHp());
            stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(getHp())));
        }
        if (stats.size() > 0) {
            getClient().getSession().write(MaplePacketCreator.updatePlayerStats(stats));
        }
    }

    public MapleMap getMap() {
        return map;
    }

    /**
     * only for tests
     *
     * @param newmap
     */
    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public int getMapId() {
        if (map != null) {
            return map.getId();
        }
        return mapid;
    }

    public int getInitialSpawnpoint() {
        return initialSpawnPoint;
    }

    public int getId() {
        return id;
    }
    

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getRank() {
        return rank;
    }

    public int getRankMove() {
        return rankMove;
    }

    public int getJobRank() {
        return jobRank;
    }

    public int getJobRankMove() {
        return jobRankMove;
    }

    public int getAriantScore() {
        return this.countItem(4031868);
    }

    public int getFame() {
        return fame;
    }

    public int getStr() {
        return str;
    }

    public int getDex() {
        return dex;
    }

    public int getLuk() {
        return luk;
    }

    public int getInt() {
        return int_;
    }

    public MapleClient getClient() {
        return client;
    }

    public int getExp() {
        return exp.get();
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxhp;
    }

    public int getMp() {
        return mp;
    }

    public int getMaxMp() {
        return maxmp;
    }

    public int getRemainingAp() {
        return remainingAp;
    }

    public int getRemainingSp() {
        return remainingSp;
    }

    public int getMpApUsed() {
        return mpApUsed;
    }

    public void setMpApUsed(int mpApUsed) {
        this.mpApUsed = mpApUsed;
    }

    public int getHpApUsed() {
        return hpApUsed;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHpApUsed(int hpApUsed) {
        this.hpApUsed = hpApUsed;
    }

    public MapleSkinColor getSkinColor() {
        return skinColor;
    }

    public MapleJob getJob() {
        return job;
    }
    
    public short getClasse() {
        return classe;
    }
    
     public int getJob1() {
        return job1;
    }

    public int getGender() {
        return gender;
    }

    public int getHair() {
        return hair;
    }

    public int getFace() {
        return face;
    }
    
    public void addCrushRing(MapleRing r) {
        crushRings.add(r);
    }
    public void addMarriageRing(MapleRing r) {
        marriageRings.add(r);
    }

    public void addFriendshipRing(MapleRing r) {
        friendshipRings.add(r);
    }
    
     public List<MapleRing> getFriendshipRings() {
        Collections.sort(friendshipRings);
        return friendshipRings;
    }
     
     public List<MapleRing> getCrushRings() {
        Collections.sort(crushRings);
        return crushRings;
    }
     
       public List<MapleRing> getMarriageRings() {
        Collections.sort(marriageRings);
        return marriageRings;
    }

    public void setName(String name, boolean changeName) {
        if (!changeName) {
            this.name = name;
        } else {
            Connection con = DatabaseConnection.getConnection();
            try {
                con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                con.setAutoCommit(false);
                PreparedStatement sn = con.prepareStatement("UPDATE characters SET name = ? WHERE id = ?");
                sn.setString(1, name);
                sn.setInt(2, id);
                sn.execute();
                con.commit();
                sn.close();
                this.name = name;
            } catch (SQLException e) {
                sqlException(e);
            }
        }
    }
    
        public final boolean hasEquipped(int itemid) {
        return inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid) >= 1;
    }
    
   public void setName(String name) {
        this.name = name;
    }

    public void setStr(int str) {
        this.str = str;
        recalcLocalStats();
    }

    public void setDex(int dex) {
        this.dex = dex;
        recalcLocalStats();
    }

    public void setLuk(int luk) {
        this.luk = luk;
        recalcLocalStats();
    }

    public void setInt(int int_) {
        this.int_ = int_;
        recalcLocalStats();
    }

    public void setExp(int exp) {
        this.exp.set(exp);
    }

    public void setMaxHp(int hp) {
        this.maxhp = hp;
        recalcLocalStats();
    }

    public void setMaxMp(int mp) {
        this.maxmp = mp;
        recalcLocalStats();
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void updateAriantScore() {
        this.getMap().broadcastMessage(MaplePacketCreator.updateAriantPQRanking(this.getName(), getAriantScore(), false));
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp = remainingSp;
    }
    
    public void ganhaSp(int remainingSp) {
       this.remainingSp += remainingSp;
    }

    public void setSkinColor(MapleSkinColor skinColor) {
        this.skinColor = skinColor;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setGM(int gmlevel) {
        this.gmLevel = gmlevel;
    }

    public CheatTracker getCheatTracker() {
        return anticheat;
    }

    public BuddyList getBuddylist() {
        return buddylist;
    }

    public void addFame(int famechange) {
        this.fame += famechange;
    }
    

      
     public int getExpRate() {
        return expRate;
    }

     public int getMesoRate() {
        return mesoRate;
    }

    public int getDropRate() {
        return dropRate;
    }  
      

    public void removeItem(int id, int quantity) {
        MapleInventoryManipulator.removeById(client, Configuration.getInventoryType(id), id, -quantity, true, false);
        client.getSession().write(MaplePacketCreator.getShowItemGain(id, (short) quantity, true));
    }

    public void removeAll(int id) {
        removeAll(id, true);
    }

    public void removeAll(int id, boolean show) {
        MapleInventoryType type = Configuration.getInventoryType(id);
        int possessed = getInventory(type).countById(id);

        if (possessed > 0) {
            MapleInventoryManipulator.removeById(getClient(), type, id, possessed, true, false);
            if (show) {
                getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, (short) -possessed, true));
            }
        }
        /*
         * if (type == MapleInventoryType.EQUIP) { //check equipped type =
         * MapleInventoryType.EQUIPPED; possessed =
         * getInventory(type).countById(id);
         *
         * if (possessed > 0) {
         * MapleInventoryManipulator.removeById(getClient(), type, id,
         * possessed, true, false);
         * getClient().getSession().write(CField.getShowItemGain(id,
         * (short)-possessed, true)); } }
         */
    }

    public void changeMap(int map) {
        changeMap(map, 0);
    }
        public void changeMap(MapleMap to) {
        changeMap(to, to.getPortal(0));
    }
       

       public void changeMap(final MapleMap to, final Point pos) {
        /*getClient().getSession().write(MaplePacketCreator.spawnPortal(map.getId(), to.getId(), pos));
        if (getParty() != null) {
        getClient().getSession().write(MaplePacketCreator.partyPortal(map.getId(), to.getId(), pos));
        }*/
        MaplePacket warpPacket = MaplePacketCreator.getWarpToMap(to, 0x80, this);
        changeMapInternal(to, pos, warpPacket);
    }
       
    public void changeMap(final MapleMap to, final MaplePortal pto) {
        if (to.getId() == 100000200 || to.getId() == 211000100 || to.getId() == 220000300) {
            MaplePacket warpPacket = MaplePacketCreator.getWarpToMap(to, pto.getId() - 2, this);
            changeMapInternal(to, pto.getPosition(), warpPacket);
        } else {
            MaplePacket warpPacket = MaplePacketCreator.getWarpToMap(to, pto.getId(), this);
            changeMapInternal(to, pto.getPosition(), warpPacket);
        }
    }
        
    public void changeMap(int map, int portal) {
        MapleMap warpMap = client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        dropMessage(5, msg);
        MapleMap map_ = client.getChannelServer().getMapFactory().getMap(mapid);
        changeMap(map_, map_.getPortal(portal));
    }

   private void changeMapInternal(final MapleMap to, final Point pos, MaplePacket warpPacket) {
        int a = 0, b = 0;
        MapleParty e = null;
        if (this.getParty() != null) {
            if (this.getParty().getEnemy() != null) {
                e = this.getParty().getEnemy();
            }
        }
        final MapleParty k = e;
        warpPacket.setOnSend(new Runnable() {

            @Override
            public void run() {
				IPlayerInteractionManager interaction = MapleCharacter.this.getInteraction();
				if (interaction != null) {
					if (interaction.isOwner(MapleCharacter.this)) {
						if (interaction.getShopType() == 2) {
							interaction.removeAllVisitors(3, 1);
							interaction.closeShop(((MaplePlayerShop) interaction).returnItems(getClient()));
						} else if (interaction.getShopType() == 1) {
							getClient().getSession().write(MaplePacketCreator.shopVisitorLeave(0));
							if (interaction.getItems().size() == 0) {
								interaction.removeAllVisitors(3, 1);
								interaction.closeShop(((HiredMerchant) interaction).returnItems(getClient()));
							}
						} else if (interaction.getShopType() == 3 || interaction.getShopType() == 4) {
							interaction.removeAllVisitors(3, 1);
						}
					} else {
						interaction.removeVisitor(MapleCharacter.this);
					}
				}
				MapleCharacter.this.setInteraction(null);
                map.removePlayer(MapleCharacter.this);
                if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) {
                    map = to;
                    setPosition(pos);
                    to.addPlayer(MapleCharacter.this);
                    if (to.isCPQMap()) {
                        if (getParty() != null) {
                            if (getTeam() != 0 &&
                                    getTeam() != 1) {
                                setTeam(0);
                                getClient().getSession().write(
                                        MaplePacketCreator.serverNotice(5, "You have been assigned to Maple Red team."));
                            }
                        } else {
                            getClient().getSession().write(
                                    MaplePacketCreator.serverNotice(5, "You are not in a party."));
                        }
                        getClient().getSession().write(MaplePacketCreator.startMonsterCarnival(getTeam()));
                    }
                    if (party != null) {
                        silentPartyUpdate();
                        getClient().getSession().write(MaplePacketCreator.updateParty(getClient().getChannel(), party, PartyOperation.SILENT_UPDATE, null));
                        updatePartyMemberHP();
                    }
                    if (getMap().getHPDec() > 0) {
                        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {

                            @Override
                            public void run() {
                                doHurtHp();
                            }
                        }, 10000);
                    }
                    if (MapleCharacter.this.getParty() != null) {
                        MapleCharacter.this.getParty().setEnemy(k);
                    }
                }
            }
        });
        getClient().getSession().write(warpPacket);
    }
 
        public int getRingRequested() {
        return this.ringRequest;
    }

    public void setRingRequested(int set) {
        ringRequest = set;
    }

    public void leaveMap() {
        controlled.clear();
        visibleMapObjects.clear();
        if (chair != 0) {
            chair = 0;
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
    }

    public void doHurtHp() {
        if (this.getInventory(MapleInventoryType.EQUIPPED).findById(getMap().getHPDecProtect()) != null) {
            return;
        }
        addHP(-getMap().getHPDec());
        hpDecreaseTask = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                doHurtHp();
            }
        }, 10000);
    }

    public void changeJob(MapleJob newJob) {
        this.job = newJob;
        this.remainingSp++;
        if (newJob.getId() % 10 == 2) {
            this.remainingSp += 2;
        }
        updateSingleStat(MapleStat.AVAILABLESP, this.remainingSp);
        updateSingleStat(MapleStat.JOB, newJob.getId());
        switch (this.job.getId()) {
            case 100:
                maxhp += rand(200, 250);
                break;
            case 200:
                maxmp += rand(100, 150);
                break;
            case 300:
            case 400:
            case 500:
                maxhp += rand(100, 150);
                maxmp += rand(25, 50);
                break;
            case 110:
            case 111:
            case 112:
            case 120:
            case 121:
            case 122:
            case 130:
            case 131:
            case 132:
                maxhp += rand(300, 350);
                break;
            case 210:
            case 211:
            case 212:
            case 220:
            case 221:
            case 222:
            case 230:
            case 231:
            case 232:
                maxmp += rand(450, 500);
                break;
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322:
            case 410:
            case 411:
            case 412:
            case 420:
            case 421:
            case 422:
            case 510:
            case 511:
            case 512:
            case 520:
            case 521:
            case 522:
                maxhp += rand(300, 350);
                maxmp += rand(150, 200);
                break;
            default:
                break;
        }
        if (maxhp >= 30000) {
            maxhp = 30000;
        }
        if (maxmp >= 30000) {
            maxmp = 30000;
        }
        setHp(maxhp);
        setMp(maxmp);
        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(2);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
        recalcLocalStats();
        getClient().getSession().write(MaplePacketCreator.updatePlayerStats(statup));
        getMap().broadcastMessage(this, MaplePacketCreator.showJobChange(getId()), false);
        silentPartyUpdate();
        guildUpdate();
    }

    public void gainAp(int ap) {
        this.remainingAp += ap;
        updateSingleStat(MapleStat.AVAILABLEAP, this.remainingAp);
    }

    public void changeSkillLevel(ISkill skill, int newLevel, int newMasterlevel) {
        skills.put(skill, new SkillEntry(newLevel, newMasterlevel));
        this.getClient().getSession().write(MaplePacketCreator.updateSkill(skill.getId(), newLevel, newMasterlevel));
    }

    public void setHp(int newhp) {
        setHp(newhp, false);
    }

    public void setHp(int newhp, boolean silent) {
        int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;

        if (!silent) {
            updatePartyMemberHP();
        }
        if (oldHp > hp && !isAlive()) {
            playerDead();
        }
    }
    

    private void playerDead() {
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        if (getClient().getChannelServer().eventOn == true) {
          getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Sua experiencia nao foi reduzida, pelo fato de estar havendo evento no momento!"));  
          return;
        }
         if (this.getMap().isCPQMap()) {
            int lost = getCP();
            if (lost > 6) {
                lost = 6;
            }
            getMap().broadcastMessage(MaplePacketCreator.playerDiedMessage(getName(), lost, getTeam()));
            gainCP(-lost);
        }
        dispelSkill(0);
        dispelDebuffs();
        cancelMorphs();
        cancelBeholder();
        cancelPhoenix();
        cancelMarksMan();
        cancelMageSummon();
        cancelMageSummon2();
        int[] charmID = {5130000, 4031283, 4140903}; //NOTE Also checks in this order
        MapleCharacter player = getClient().getPlayer();
        int possesed = 0;
        int i;

        //Check for charms
        for (i = 0; i < charmID.length; i++) {
            int quantity = getItemQuantity(charmID[i], false);
            if (possesed == 0 && quantity > 0) {
                possesed = quantity;
                break;
            }
        }

        if (possesed > 0) {
            //Our player got lucky this time!
            possesed -= 1;
            getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You have used the safety charm once, so your EXP points have not been decreased. (" + possesed + "time(s) left)"));
            MapleInventoryManipulator.removeById(getClient(), MapleItemInformationProvider.getInstance().getInventoryType(charmID[i]), charmID[i], 1, true, false);
        } else {
            if (player.getJob() != MapleJob.BEGINNER) {
                //Lose XP
                int XPdummy = ExpTable.getExpNeededForLevel(player.getLevel() + 1);
                if (player.getMap().isTown()) {
                    XPdummy *= 0.01;
                }

                if (XPdummy == ExpTable.getExpNeededForLevel(player.getLevel() + 1)) {
                    //Thank you LaiLaiNoob for the information
                    if (player.getLuk() <= 100 && player.getLuk() > 8) {
                        XPdummy *= 0.10 - (player.getLuk() * 0.0005);
                    } else if (player.getLuk() < 8) {
                        XPdummy *= 0.10; //Otherwise they lose about 9 percent
                    } else {
                        XPdummy *= 0.10 - (100 * 0.0005);
                    }
                }

                if ((player.getExp() - XPdummy) > 0) {
                    player.gainExp(-XPdummy, false, false);
                } else {
                    player.gainExp(-player.getExp(), false, false);
                }
            }
        }

        getClient().getSession().write(MaplePacketCreator.enableActions());
    }


    public void updatePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.getClient().getSession().write(MaplePacketCreator.updatePartyMemberHP(getId(), this.hp, localmaxhp));
                    }
                }
            }
        }
    }
    
      public void cancelBeholder() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isBeholder()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }
      
      public void cancelPhoenix() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isPhoenix()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }
      
      public void cancelMarksMan() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMarksMan()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }
      
      public void cancelMageSummon() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMageSummon()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }
      
      public void cancelMageSummon2() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMageSummon2()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }
      

    public void receivePartyMemberHP() {
        if (party != null) {
            int channel = client.getChannel();
            for (MaplePartyCharacter partychar : party.getMembers()) {
                if (partychar.getMapid() == getMapId() && partychar.getChannel() == channel) {
                    MapleCharacter other = ChannelServer.getInstance(channel).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        getClient().getSession().write(
                                MaplePacketCreator.updatePartyMemberHP(other.getId(), other.getHp(), other.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void setMp(int newmp) {
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > localmaxmp) {
            tmp = localmaxmp;
        }
        this.mp = tmp;
    }

    /**
     * Convenience function which adds the supplied parameter to the current hp
     * then directly does a updateSingleStat.
     *
     * @see MapleCharacter#setHp(int)
     * @param delta
     */
    public void addHP(int delta) {
        setHp(hp + delta);
        updateSingleStat(MapleStat.HP, hp);
    }

    /**
     * Convenience function which adds the supplied parameter to the current mp
     * then directly does a updateSingleStat.
     *
     * @see MapleCharacter#setMp(int)
     * @param delta
     */
    public void addMP(int delta) {
        setMp(mp + delta);
        updateSingleStat(MapleStat.MP, mp);
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        setHp(hp + hpDiff);
        setMp(mp + mpDiff);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        stats.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(hp)));
        stats.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(mp)));
        MaplePacket updatePacket = MaplePacketCreator.updatePlayerStats(stats);
        client.getSession().write(updatePacket);
    }

    /**
     * Updates a single stat of this MapleCharacter for the client. This method
     * only creates and sends an update packet, it does not update the stat
     * stored in this MapleCharacter instance.
     *
     * @param stat
     * @param newval
     * @param itemReaction
     */
    public void updateSingleStat(MapleStat stat, int newval, boolean itemReaction) {
        Pair<MapleStat, Integer> statpair = new Pair<MapleStat, Integer>(stat, Integer.valueOf(newval));
        MaplePacket updatePacket = MaplePacketCreator.updatePlayerStats(Collections.singletonList(statpair), itemReaction);
        client.getSession().write(updatePacket);
    }

    public void updateSingleStat(MapleStat stat, int newval) {
        updateSingleStat(stat, newval, false);
    }
    
    
    
    public boolean hasMerchant() {
        return hasMerchant;
    }
    
    
        public boolean haveItem(int itemid) {
        return haveItem(itemid, 1, false, true);
    }
        
        
     public int hasEXPCard() {
        int[] expCards = {5210000, 5210001, 5210002, 5210003, 5210004, 5210005, 5211000, 5211003, 5211004, 5211005, 5211006, 5211007, 5211008, 5211009, 5211010, 5211011, 5211012, 5211013, 5211014, 5211015, 5211016, 5211017, 5211018, 5211037, 5211038, 5211039, 5211040, 5211041, 5211042, 5211043, 5211044, 5211045, 5211046, 5211047, 5211048, 5211049, 5211001, 5211002};
        MapleInventory iv = getInventory(MapleInventoryType.CASH);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Integer id : expCards) {
            if (iv.countById(id) > 0) {
                if (ii.isExpOrDropCardTime(id)) {
                    return 2;
                }
            }
        }
        return 1;
    }
    public int hasDropCard() {
        int[] dropCards = {5360000, 5360001, 5360002, 5360003, 5360004, 5360005, 5360006, 5360007, 5360008, 5360009, 5360010, 5360011, 5360012, 5360013, 5360014, 5360042,};
        MapleInventory iv = getInventory(MapleInventoryType.CASH);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (Integer id : dropCards) {
            if (iv.countById(id) > 0) {
                if (ii.isExpOrDropCardTime(id)) {
                    return 2;
                }
            }
        }
        return 1;
    }
    
    public int getEXPMod() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (haveItem(5211000, 1, false, true) && hour >= 18 && hour <= 20) {
            return 2;
        } else if (haveItem(5211014, 1, false, true) && hour >= 7 && hour <= 11) {
            return 2;
        } else if (haveItem(5211015, 1, false, true) && hour >= 10 && hour <= 14) {
            return 2;
        } else if (haveItem(5211016, 1, false, true) && hour >= 13 && hour <= 17) {
            return 2;
        } else if (haveItem(5211017, 1, false, true) && hour >= 16 && hour <= 20) {
            return 2;
        } else if (haveItem(5211018, 1, false, true) && hour >= 15 && hour <= 23) {
            return 2;
        } else if (haveItem(5211039, 1, false, true) && hour >= 0 && hour <= 4) {
            return 2;
        } else if (haveItem(5211042, 1, false, true) && hour >= 3 && hour <= 7) {
            return 2;
        } else if (haveItem(5211045, 1, false, true) && hour >= 6 && hour <= 10) {
            return 2;
        }
        return 1;
    }

    public void gainExp(int gain, boolean show, boolean inChat, boolean white) {
        if (this.exp.get() < 0) {
            this.exp.set(0);
        }
        if (getLevel() < 200) {
            if ((long) this.exp.get() + (long) gain > (long) Integer.MAX_VALUE) {
                int gainFirst = ExpTable.getExpNeededForLevel(level + 1) - this.exp.get();
                gain -= gainFirst + 1;
                this.gainExp(gainFirst + 1, false, inChat, white);
            }
            int newexp = this.exp.addAndGet(gain);
            updateSingleStat(MapleStat.EXP, newexp);
        }

        if (show && gain != 0) { // still show the exp gain even if it's not there
            client.getSession().write(MaplePacketCreator.getShowExpGain(gain, inChat, white));
        }
        if (level > 10) {
            if (level < 200 && exp.get() >= ExpTable.getExpNeededForLevel(level + 1)) {
                levelUp();
            }
        } else {
            while (level < 200 && exp.get() >= ExpTable.getExpNeededForLevel(level + 1)) {
                levelUp();
            }
        }
    }

    public void handleEnergyChargeGain() {

        if (energybar >= 10000) {
            return;
        }
        if (getSkillLevel(SkillFactory.getSkill(5110001)) > 0) {
            gainEnergy((int) ((level * Math.random()) * 100));


            if (energybar >= 10000) {
                energyFull();
            }

        }
    }

    public void gainEnergy(int amt) {
        energybar += amt;
        energybar = energybar > 10000 ? 10000 : energybar;
        energybar = energybar < 0 ? 0 : energybar;
        getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(energybar, 0));
    }

    public void setEnergy(int amt) {
        energybar = amt > 10000 ? 10000 : amt;
        energybar = energybar < 0 ? 0 : energybar;
        getClient().getSession().write(MaplePacketCreator.giveEnergyCharge(energybar, 0));
    }

    public int getEnergy() {
        return energybar;
    }

    public void energyFull() {
        energyFull(SkillFactory.getSkill(5110001).getEffect(getSkillLevel(SkillFactory.getSkill(5110001))).getDuration());
    }

    public void energyFull(int duration) {
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(new Runnable() {
            @Override
            public void run() {
                setEnergy(0);

            }
        }, duration);
    }

    public void silentPartyUpdate() {
        if (party != null) {
            try {
                getClient().getChannelServer().getWorldInterface().updateParty(party.getId(),
                        PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(MapleCharacter.this));
            } catch (RemoteException e) {
                log.error("REMOTE THROW", e);
                getClient().getChannelServer().reconnectWorld();
            }
        }
    }
    

    public void gainExp(int gain, boolean show, boolean inChat) {
        gainExp(gain, show, inChat, true);
    }

    public boolean isGM() {
        return gmLevel > 1;
    }

    public int gmLevel() {
        return gmLevel;
    }

    public int getGMLevel() {
        return gmLevel;
    }

    public boolean hasGmLevel(int level) {
        return gmLevel >= level;
    }

    public MapleInventory getInventory(MapleInventoryType type) {
        return inventory[type.ordinal()];
    }

    public MapleShop getShop() {
        return shop;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public int getMeso() {
        return meso.get();
    }

    public int getSavedLocation(SavedLocationType type) {
        return savedLocations[type.ordinal()];
    }

    public void saveLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = getMapId();
    }

    public void clearSavedLocation(SavedLocationType type) {
        savedLocations[type.ordinal()] = -1;
    }
    
     private ScheduledFuture<?> expiretask;
        public void cancelExpirationTask() {
        if (expiretask != null) {
            expiretask.cancel(false);
            expiretask = null;
        }
    }


    public void gainMeso(int gain, boolean show) {
        gainMeso(gain, show, false, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions) {
        gainMeso(gain, show, enableActions, false);
    }

    public void gainMeso(int gain, boolean show, boolean enableActions, boolean inChat) {
        if (meso.get() + gain < 0) {
            client.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int newVal = meso.addAndGet(gain);
        updateSingleStat(MapleStat.MESO, newVal, enableActions);
        if (show) {
            client.getSession().write(MaplePacketCreator.getShowMesoGain(gain, inChat));
        }
    }

    /**
     * Adds this monster to the controlled list. The monster must exist on the
     * Map.
     *
     * @param monster
     */
    public void controlMonster(MapleMonster monster, boolean aggro) {
        monster.setController(this);
        controlled.add(monster);
        client.getSession().write(MaplePacketCreator.controlMonster(monster, false, aggro));
    }

    public void stopControllingMonster(MapleMonster monster) {
        controlled.remove(monster);
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if (!monster.isControllerHasAggro()) {
            if (monster.getController() == this) {
                monster.setControllerHasAggro(true);
            } else {
                monster.switchController(this, true);
            }
        }
    }

    public Collection<MapleMonster> getControlledMonsters() {
        return Collections.unmodifiableCollection(controlled);
    }

    public int getNumControlledMonsters() {
        return controlled.size();
    }

    @Override
    public String toString() {
        return "Character: " + this.name;
    }

    public int getAccountID() {
        return accountid;
    }
    
     public void dispelDebuffs() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases) {
            if (disease == MapleDisease.WEAKEN || disease != MapleDisease.DARKNESS || disease != MapleDisease.SEAL || disease != MapleDisease.POISON) {
                disease_.add(disease);
                getClient().getSession().write(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            } else {
                return;
            }
        }
        this.diseases.clear();
    }

   public void kill() {
        setHp(0);
        setMp(0);
        updateSingleStat(MapleStat.HP, 0);
        updateSingleStat(MapleStat.MP, 0);
    }

   public void mobKilled(int id) {
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus() == MapleQuestStatus.Status.COMPLETED || q.getQuest().canComplete(this, null)) {
                continue;
            }
            if (q.mobKilled(id)) {
                client.getSession().write(MaplePacketCreator.updateQuestMobKills(q));
                if (q.getQuest().canComplete(this, null)) {
                    client.getSession().write(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
                }
            }
        }
    }

     public final List<MapleQuestStatus> getStartedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.STARTED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (MapleQuestStatus q : quests.values()) {
            if (q.getStatus().equals(MapleQuestStatus.Status.COMPLETED)) {
                ret.add(q);
            }
        }
        return Collections.unmodifiableList(ret);
    }


    public MaplePlayerShop getPlayerShop() {
        return playerShop;
    }

    public void setPlayerShop(MaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public Map<ISkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    public void removeBuffs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
        }
    }

   public void dispelSkill(int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public int getSkillLevel(ISkill skill) {
        SkillEntry ret = skills.get(skill);
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }
    
        public int getSkillLevel(int skill) {
        SkillEntry ret = skills.get(SkillFactory.getSkill(skill));
        if (ret == null) {
            return 0;
        }
        return ret.skillevel;
    }

    public int getMasterLevel(ISkill skill) {
        SkillEntry ret = skills.get(skill);
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }

    // the equipped inventory only contains equip... I hope
    public int getTotalDex() {
        return localdex;
    }

    public int getTotalInt() {
        return localint_;
    }

    public int getTotalStr() {
        return localstr;
    }

    public int getTotalLuk() {
        return localluk;
    }

    public int getTotalMagic() {
        return magic;
    }

    public double getSpeedMod() {
        return speedMod;
    }

    public double getJumpMod() {
        return jumpMod;
    }

    public int getTotalWatk() {
        return watk;
    }

    public static int rand(int lbound, int ubound) {
        return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
    }

    public void levelUp() {
        int[] Leveltempo = {10};
        ISkill improvingMaxHP = null;
        int improvingMaxHPLevel = 0;
        ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
        int improvingMaxMPLevel = getSkillLevel(improvingMaxMP);
        remainingAp += 5;
        if (job == MapleJob.BEGINNER) {
            maxhp += rand(12, 16);
            maxmp += rand(10, 12);
        } else if (job.isA(MapleJob.WARRIOR)) {
            improvingMaxHP = SkillFactory.getSkill(1000001);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(24, 28);
            maxmp += rand(4, 6);
        } else if (job.isA(MapleJob.MAGICIAN)) {
            maxhp += rand(10, 14);
            maxmp += rand(22, 24);
        } else if (job.isA(MapleJob.BOWMAN) || job.isA(MapleJob.THIEF) || job.isA(MapleJob.GM)) {
            maxhp += rand(20, 24);
            maxmp += rand(14, 16);
        } else if (job.isA(MapleJob.PIRATE)) {
            improvingMaxHP = SkillFactory.getSkill(5100000);
            improvingMaxHPLevel = getSkillLevel(improvingMaxHP);
            maxhp += rand(22, 28);
            maxmp += rand(18, 23);
        }
        if (improvingMaxHPLevel > 0) {
            maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
        }
        if (improvingMaxMPLevel > 0) {
            maxmp += improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
        }
        maxmp += getTotalInt() / 10;
        exp.addAndGet(-ExpTable.getExpNeededForLevel(level + 1));
        level += 1;
        if (level == 200 && !isGM()) {
            exp.set(0);
            MaplePacket packet = MaplePacketCreator.serverNotice(6, "[Notice] " + getName() + " has reached level 200!");
            try {
                getClient().getChannelServer().getWorldInterface().broadcastMessage(getName(), packet.getBytes());
            } catch (RemoteException e) {
                getClient().getChannelServer().reconnectWorld();
            }
        }
         if (level == 5) {
            /* Envia mensagem de Ajuda de nivel */
            getClient().getSession().write(MaplePacketCreator.getNPCTalk(9200000, (byte) 0, NewPlayers.Nivel, "00 00"));  
        }
        maxhp = Math.min(30000, maxhp);
        maxmp = Math.min(30000, maxmp);

        List<Pair<MapleStat, Integer>> statup = new ArrayList<Pair<MapleStat, Integer>>(8);
        statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, Integer.valueOf(remainingAp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, Integer.valueOf(maxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.HP, Integer.valueOf(maxhp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.MP, Integer.valueOf(maxmp)));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.EXP, Integer.valueOf(exp.get())));
        statup.add(new Pair<MapleStat, Integer>(MapleStat.LEVEL, Integer.valueOf(level)));

        if (job != MapleJob.BEGINNER) {
            remainingSp += 3;
            statup.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLESP, Integer.valueOf(remainingSp)));
        }

        setHp(maxhp);
        setMp(maxmp);
        getClient().getSession().write(MaplePacketCreator.updatePlayerStats(statup));
        getMap().broadcastMessage(this, MaplePacketCreator.showLevelup(getId()), false);
        recalcLocalStats();
        silentPartyUpdate();
        guildUpdate();
         if (mgc != null && mgc.getGuildId() > 0) {
            MapleGuild g;
            try {
                g = client.getChannelServer().getWorldInterface().getGuild(mgc.getGuildId(), mgc);
                g.broadcast(MaplePacketCreator.serverNotice(5, String.format("<Cla> %s atingiu o Lv. %d.", getName(), getLevel())));
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }
    
     public void decreaseBattleshipHp(int decrease) {
        this.battleshipHp -= decrease;
        if (battleshipHp <= 0) {
            this.battleshipHp = 0;
            ISkill battleship = SkillFactory.getSkill(5221999);
            int cooldown = battleship.getEffect(getSkillLevel(battleship)).getCooldown();
            getClient().getSession().write(MaplePacketCreator.skillCooldown(5221999, cooldown));
            addCooldown(5221999, System.currentTimeMillis(), cooldown, TimerManager.getInstance().schedule(new CancelCooldownAction(this, 5221999), cooldown * 1000));
            removeCooldown(5221999);
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        } else {
            getClient().getSession().write(MaplePacketCreator.skillCooldown(5221999, battleshipHp / 10));   //:D
            addCooldown(5221999, 0, battleshipHp, null);
        }
    }

    public void changeKeybinding(int key, MapleKeyBinding keybinding) {
        if (keybinding.getType() != 0) {
            keymap.put(Integer.valueOf(key), keybinding);
        } else {
            keymap.remove(Integer.valueOf(key));
        }
    }
    
    
    public void sendKeymap() {
        getClient().getSession().write(MaplePacketCreator.getKeymap(keymap));
    }

    public void sendMacros() {
        boolean macros = false;
        for (int i = 0; i < 5; i++) {
            if (skillMacros[i] != null) {
                macros = true;
            }
        }
        if (macros) {
            getClient().getSession().write(MaplePacketCreator.getMacros(skillMacros));
        }
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        skillMacros[position] = updateMacro;
    }

    public void tempban(String reason, Calendar duration, int greason) {
        if (lastmonthfameids == null) {
            throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
        }
        tempban(reason, duration, greason, client.getAccID());
        client.getSession().close();
    }

    public static boolean tempban(String reason, Calendar duration, int greason, int accountid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
            Timestamp TS = new Timestamp(duration.getTimeInMillis());
            ps.setTimestamp(1, TS);
            ps.setString(2, reason);
            ps.setInt(3, greason);
            ps.setInt(4, accountid);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException ex) {
            log.error("Error while tempbanning", ex);
        }
        return false;
    }

    public void ban(String reason) {
        if (lastmonthfameids == null) {
            throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
        }
        try {
            getClient().banMacs();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
            ps.setInt(1, 1);
            ps.setString(2, reason);
            ps.setInt(3, accountid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
            String[] ipSplit = client.getSession().getRemoteAddress().toString().split(":");
            ps.setString(1, ipSplit[0]);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            log.error("Error while banning", ex);
        }
        //client.getSession().write(MaplePacketCreator.sendGMPolice());
        getClient().getSession().write(MaplePacketCreator.getNPCTalk(9000019, (byte) 0, "You have been banned for the following reason:\\r\\n" + reason, "00 00"));
        TimerManager.getInstance().schedule(new Runnable() {
            public void run() {
                client.getSession().close();
            }
        }, 10000);
    }

    public static boolean ban(String id, String reason, boolean accountId) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
                psb.setString(1, reason);
                psb.setInt(2, rs.getInt(1));
                psb.executeUpdate();
                psb.close();
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
            log.error("Error while banning", ex);
        }
        return false;
    }
    
    public void ban(String reason, boolean dc) {
        try {
            client.banMacs();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?");
            ps.setString(1, reason);
            ps.setInt(2, accountid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
            ps.setString(1, client.getSession().getRemoteAddress().toString().split(":")[0]);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
        }
        client.disconnect();
    }
    /**
     * Oid of players is always = the cid
     */
    @Override
    public int getObjectId() {
        return getId();
    }

    /**
     * Throws unsupported operation exception, oid of players is read only
     */
    @Override
    public void setObjectId(int id) {
        throw new UnsupportedOperationException();
    }

    public MapleStorage getStorage() {
        return storage;
    }

    public int getCurrentMaxHp() {
        return localmaxhp;
    }

    public int getCurrentMaxMp() {
        return localmaxmp;
    }

    public int getCurrentMaxBaseDamage() {
        return localmaxbasedamage;
    }
    
       public void gainCashPoints(int gain){
       this.CashPoints += gain;
    }
       
       public void spawnMonster(int mobid, int HP, int EXP, int amount) { 
        MapleMonsterStats newStats = new MapleMonsterStats(); 
        if (HP != 0) { 
            newStats.setHp(HP); 
        } 
        if (EXP != 0) { 
            newStats.setExp(EXP); 
        } 
        for (int i = 0; i < amount; i++) { 
            MapleMonster npcmob = MapleLifeFactory.getMonster(mobid); 
            npcmob.setOverrideStats(newStats); 
            npcmob.setHp(npcmob.getMaxHp()); 
            npcmob.setMp(npcmob.getMaxMp()); 
            getMap().spawnMonsterOnGroundBelow(npcmob, this.getPosition()); 
        } 
    } 
    public void warpToBossPQ(int mapid, int time) { 
        MapleMap map = client.getChannelServer().getMapFactory().getMap(mapid); 
        time *= 1000; 
        map.clearDrops(); 
        map.killAllMonsters(); 
        for (MapleCharacter party : getPartyMembers()) { 
            party.changeMap(map); 
            party.getClient().announce(MaplePacketCreator.getClock(time / 1000)); 
            party.dropMessage("Prepare-se para lutar, comecaremos em " + (time / 1000) + " segundos."); 
        } 
        TimerManager.getInstance().schedule(new Runnable() { 

            @Override 
            public void run() { 
                spawnMonster(3220000, 100000000, 0, 1); 
            } 
        }, time); 
    } 

     public List<MapleCharacter> getPartyMembers() { 
        if (getParty() == null) { 
            return null; 
        } 
        List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); 
        for (ChannelServer channel : ChannelServer.getAllInstances()) { 
            for (MapleCharacter chr : channel.getPartyMembers(getParty())) { 
                if (chr != null) { 
                    chars.add(chr); 
                } 
            } 
        } 
        return chars; 
    } 

    private int bosspoints; 
  

    public void addBossPoints(int amt) { 
        setBossPoints(getBossPoints() + amt); 
    }  

   public void gainLeaderPoints(int gain){
       this.LeaderPoints += gain;
    }

    public int getLeaderPoints(){
        return this.LeaderPoints;
    }
   
    public void gainvotePoints(int gain){
      this.votePoints += gain;
    }

    public int getvotePoints(){
        return this.votePoints;
    }
    
    
    public boolean allowedMapChange() {
        return this.allowMapChange;
    }

    public void setallowedMapChange(boolean allowed) {
        this.allowMapChange = allowed;
    }
    
    
   public void gainpqPoints(int gain){
       this.pqPoints += gain;
    }
   public void gainItem(){
      short quantity = 1;
      int itemid = 4031442;
      MapleInventoryManipulator.addById(getClient(), itemid, quantity, "Item de Evento");
    }

    public int getpqPoints(){
        return this.pqPoints;
    }

    public int calculateMaxBaseDamage(int watk) {
        int maxbasedamage;
        if (watk == 0) {
            maxbasedamage = 1;
        } else {
            IItem weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                int mainstat;
                int secondarystat;
                if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW) {
                    mainstat = localdex;
                    secondarystat = localstr;
                } else if (getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.CLAW || weapon == MapleWeaponType.DAGGER)) {
                    mainstat = localluk;
                    secondarystat = localdex + localstr;
                } else if (getJob().isA(MapleJob.PIRATE) && (weapon == MapleWeaponType.GUN)) {
                    mainstat = localdex;
                    secondarystat = localstr;
                } else if (getJob().isA(MapleJob.PIRATE) && (weapon == MapleWeaponType.KNUCKLE)) {
                    mainstat = localstr;
                    secondarystat = localdex;
                } else {
                    mainstat = localstr;
                    secondarystat = localdex;
                }
                maxbasedamage = (int) (((weapon.getMaxDamageMultiplier() * mainstat + secondarystat) / 100.0) * watk);
                //just some saveguard against rounding errors, we want to a/b for this
                maxbasedamage += 10;
            } else {
                maxbasedamage = 0;
            }
        }
        return maxbasedamage;
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.add(mo);
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        visibleMapObjects.remove(mo);
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        return visibleMapObjects.contains(mo);
    }

    public Collection<MapleMapObject> getVisibleMapObjects() {
        return Collections.unmodifiableCollection(visibleMapObjects);
    }

    public boolean isAlive() {
        return this.hp > 0;
    }

    public void setSlot(int slotid) {
        slots = slotid;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (!this.isHidden()) {
            client.getSession().write(MaplePacketCreator.spawnPlayerMapobject(this));
            for (int i = 0; i < 3; i++) {
                if (pets[i] != null) {
                    client.getSession().write(MaplePacketCreator.showPet(this, pets[i], false, false));
                }
            }
        }
    }

    private void recalcLocalStats() {
        int oldmaxhp = localmaxhp;
        localmaxhp = getMaxHp();
        localmaxmp = getMaxMp();
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        int speed = 100;
        int jump = 100;
        magic = localint_;
        watk = 0;
        acc = calculateAcc(this.getJob());
        eva = calculateEva(this.getJob());
        wdef = 0;
        mdef = 0;
        for (IItem item : getInventory(MapleInventoryType.EQUIPPED)) {
            IEquip equip = (IEquip) item;
            localmaxhp += equip.getHp();
            localmaxmp += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            magic += equip.getMatk() + equip.getInt();
            watk += equip.getWatk();
            wdef += equip.getWdef();
            mdef += equip.getMdef();
            acc += equip.getAcc();
            eva += equip.getAvoid();
            speed += equip.getSpeed();
            jump += equip.getJump();
        }
        magic = Math.min(magic, 2000);
        Integer hbhp = getBuffedValue(MapleBuffStat.HYPERBODYHP);
        if (hbhp != null) {
            localmaxhp += (hbhp.doubleValue() / 100) * localmaxhp;
        }
        Integer hbmp = getBuffedValue(MapleBuffStat.HYPERBODYMP);
        if (hbmp != null) {
            localmaxmp += (hbmp.doubleValue() / 100) * localmaxmp;
        }
        localmaxhp = Math.min(30000, localmaxhp);
        localmaxmp = Math.min(30000, localmaxmp);
        Integer watkbuff = getBuffedValue(MapleBuffStat.WATK);
        if (watkbuff != null) {
            watk += watkbuff.intValue();
        }
        if (job.isA(MapleJob.BOWMAN)) {
            ISkill expert = null;
            if (job.isA(MapleJob.CROSSBOWMASTER)) {
                expert = SkillFactory.getSkill(3220004);
            } else if (job.isA(MapleJob.BOWMASTER)) {
                expert = SkillFactory.getSkill(3120005);
            }
            if (expert != null) {
                int boostLevel = getSkillLevel(expert);
                if (boostLevel > 0) {
                    watk += expert.getEffect(boostLevel).getX();
                }
            }
        }
        Integer matkbuff = getBuffedValue(MapleBuffStat.MATK);
        if (matkbuff != null) {
            magic += matkbuff.intValue();
        }
        Integer speedbuff = getBuffedValue(MapleBuffStat.SPEED);
        if (speedbuff != null) {
            speed += speedbuff.intValue();
        }
        Integer jumpbuff = getBuffedValue(MapleBuffStat.JUMP);
        if (jumpbuff != null) {
            jump += jumpbuff.intValue();
        }
        if (speed > 140) {
            speed = 140;
        }
        if (jump > 123) {
            jump = 123;
        }
        speedMod = speed / 100.0;
        jumpMod = jump / 100.0;
        Integer mount = getBuffedValue(MapleBuffStat.MONSTER_RIDING);
        if (mount != null) {
            jumpMod = 1.23;
            switch (mount.intValue()) {
                case 1:
                    speedMod = 1.5;
                    break;
                case 2:
                    speedMod = 1.7;
                    break;
                case 3:
                    speedMod = 1.8;
                    break;
                case 5:
                    speedMod = 1.0;
                    jumpMod = 1.0;
                    break;
                default:
                    speedMod = 2.0;
            }
        }
        localmaxbasedamage = calculateMaxBaseDamage(watk);
        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            updatePartyMemberHP();
        }
    }

    public void Mount(int id, int skillid) {
        maplemount = new MapleMount(this, id, skillid);
    }

    public MapleMount getMount() {
        return maplemount;
    }

    public void equipChanged() {
        getMap().broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        recalcLocalStats();
        enforceMaxHpMp();
        if (getClient().getPlayer().getMessenger() != null) {
            WorldChannelInterface wci = ChannelServer.getInstance(getClient().getChannel()).getWorldInterface();
            try {
                wci.updateMessenger(getClient().getPlayer().getMessenger().getId(), getClient().getPlayer().getName(), getClient().getChannel());
            } catch (RemoteException e) {
                getClient().getChannelServer().reconnectWorld();
            }
        }
    }

    public MaplePet getPet(int index) {
        return pets[index];
    }

    public void addPet(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                pets[i] = pet;
                return;
            }
        }
    }
    
    public void removePet(MaplePet pet, boolean shift_left) {
        int slot = -1;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    pets[i] = null;
                    slot = i;
                    break;
                }
            }
        }
        if (shift_left) {
            if (slot > -1) {
                for (int i = slot; i < 3; i++) {
                    if (i != 2) {
                        pets[i] = pets[i + 1];
                    } else {
                        pets[i] = null;
                    }
                }
            }
        }
    }

    public int getNoPets() {
        int ret = 0;
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                ret++;
            } else {
                break;
            }
        }
        return ret;
    }

    public int getPetIndex(MaplePet pet) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == pet.getUniqueId()) {
                    return i;
                }
            } else {
                break;
            }
        }
        return -1;
    }

    public int getPetIndex(int petId) {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                if (pets[i].getUniqueId() == petId) {
                    return i;
                }
            } else {
                break;
            }
        }
        return -1;
    }

    public int getNextEmptyPetIndex() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] == null) {
                return i;
            }
        }
        return 3;
    }

    public MaplePet[] getPets() {
        return pets;
    }

    public void unequipAllPets() {
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                unequipPet(pets[i], true);
                cancelFullnessSchedule(i);
            } else {
                break;
            }
        }
    }
    
     public int getJQPoints(){
    return jqpoints;
     }

    public void setJQPoints(byte amt){ // you shouldn't be adding more than 128 anyways, right?
    this.jqpoints = amt;
    }

     public void addjqpoints(int quantity) {
     this.jqpoints += quantity;
    }
    
     public void addjqrankpoints(int quantity) {
     this.jqrank += quantity;
    }
     
     
    public void addJQPoints(byte pts){ // @above comment
    this.jqpoints += pts;
    }
    
    public int getJQRank(){
    return jqrank;
     }

    public void setJQRank(byte amt){ // you shouldn't be adding more than 128 anyways, right?
    this.jqrank = amt;
    }

    public void addJQRankPoints(byte pts){ // @above comment
    this.jqrank += pts;
    }
    
    
    
     public int getCashPoints(){
    return CashPoints;
     }

    public void setCashPoints(byte amt){ // you shouldn't be adding more than 128 anyways, right?
    this.CashPoints = amt;
    }

    public void addCashPoints(byte pts){ // @above comment
    this.CashPoints += pts;
    }

    public void unequipPet(MaplePet pet, boolean shift_left) {
        unequipPet(pet, shift_left, false);
    }

    public void unequipPet(MaplePet pet, boolean shift_left, boolean hunger) {
        cancelFullnessSchedule(getPetIndex(pet));
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                pets[i].saveToDb();
            }
        }
        getMap().broadcastMessage(this, MaplePacketCreator.showPet(this, pet, true, hunger), true);
        List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
        stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(0)));
        getClient().getSession().write(MaplePacketCreator.petStatUpdate(this));
        getClient().getSession().write(MaplePacketCreator.enableActions());
        removePet(pet, shift_left);
    }

    public void shiftPetsRight() {
        if (pets[2] == null) {
            pets[2] = pets[1];
            pets[1] = pets[0];
            pets[0] = null;
        }
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (lastfametime >= System.currentTimeMillis() - 60 * 60 * 24 * 1000) {
            return FameStatus.NOT_TODAY;
        } else if (lastmonthfameids.contains(Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        } else {
            return FameStatus.OK;
        }
    }

    public void hasGivenFame(MapleCharacter to) {
        lastfametime = System.currentTimeMillis();
        lastmonthfameids.add(Integer.valueOf(to.getId()));
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            log.error("ERROR writing famelog for char " + getName() + " to " + to.getName(), e);
        }
    }

    public MapleParty getParty() {
        return party;
    }

    public int getPartyId() {
        return (party != null ? party.getId() : -1);
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }

    public void setParty(MapleParty party) {
        this.party = party;
    }

    public MapleTrade getTrade() {
        return trade;
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public EventInstanceManager getEventInstance() {
        return eventInstance;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void addDoor(MapleDoor door) {
        doors.add(door);
    }

    public void clearDoors() {
        doors.clear();
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>(doors);
    }

    public boolean canDoor() {
        return canDoor;
    }

    public void disableDoor() {
        canDoor = false;
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(new Runnable() {
            @Override
            public void run() {
                canDoor = true;
            }
        }, 5000);
    }

    public Map<Integer, MapleSummon> getSummons() {
        return summons;
    }

    public int getChair() {
        return chair;
    }

    public int getItemEffect() {
        return itemEffect;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    @Override
    public Collection<MapleInventory> allInventories() {
        return Arrays.asList(inventory);
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    public int getGuildId() {
        return guildid;
    }

    public int getGuildRank() {
        return guildrank;
    }

    public void setGuildId(int _id) {
        guildid = _id;
        if (guildid > 0) {
            if (mgc == null) {
                mgc = new MapleGuildCharacter(this);
            } else {
                mgc.setGuildId(guildid);
            }
        } else {
            mgc = null;
        }
    }

    public void setGuildRank(int _rank) {
        guildrank = _rank;
        if (mgc != null) {
            mgc.setGuildRank(_rank);
        }
    }

    public MapleGuildCharacter getMGC() {
        return mgc;
    }

    public void guildUpdate() {
        if (this.guildid <= 0) {
            return;
        }
        mgc.setLevel(this.level);
        mgc.setJobId(this.job.getId());
        try {
            this.client.getChannelServer().getWorldInterface().memberLevelJobUpdate(this.mgc);
            int allianceId = getGuild().getAllianceId();
            if (allianceId > 0) {
                client.getChannelServer().getWorldInterface().allianceMessage(allianceId, MaplePacketCreator.updateAllianceJobLevel(this), getId(), -1);
            }
        } catch (RemoteException re) {
            log.error("RemoteExcept while trying to update level/job in guild.", re);
        }
    }
    
    private NumberFormat nf = new DecimalFormat("#,###,###,###");

    public String guildCost() {
        return nf.format(MapleGuild.CREATE_GUILD_COST);
    }

    public String emblemCost() {
        return nf.format(MapleGuild.CHANGE_EMBLEM_COST);
    }

    public String capacityCost() {
        return nf.format(MapleGuild.INCREASE_CAPACITY_COST);
    }

    public void genericGuildMessage(int code) {
        this.client.getSession().write(MaplePacketCreator.genericGuildMessage((byte) code));
    }

    public void disbandGuild() {
        if (guildid <= 0 || guildrank != 1) {
            log.warn(this.name + " tried to disband and s/he is either not in a guild or not leader.");
            return;
        }

        try {
            client.getChannelServer().getWorldInterface().disbandGuild(this.guildid);
        } catch (Exception e) {
            log.error("Error while disbanding guild.", e);
        }
    }

    public void increaseGuildCapacity() {
        if (this.getMeso() < MapleGuild.INCREASE_CAPACITY_COST) {
            client.getSession().write(MaplePacketCreator.serverNotice(1, "You do not have enough mesos."));
            return;
        }

        if (this.guildid <= 0) {
            log.info(this.name + " is trying to increase guild capacity without being in the guild.");
            return;
        }

        try {
            client.getChannelServer().getWorldInterface().increaseGuildCapacity(this.guildid);
        } catch (Exception e) {
            log.error("Error while increasing capacity.", e);
            return;
        }

        this.gainMeso(-MapleGuild.INCREASE_CAPACITY_COST, true, false, true);
    }

    public void saveGuildStatus() {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET guildid = ?, guildrank = ?, allianceRank = ? WHERE id = ?");
            ps.setInt(1, this.guildid);
            ps.setInt(2, this.guildrank);
            ps.setInt(3, this.allianceRank);
            ps.setInt(4, this.id);
            ps.execute();
            ps.close();
        } catch (SQLException se) {
            log.error("SQL error: " + se.getLocalizedMessage(), se);
        }
    }

    public boolean tempHasItems() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT ownerid FROM hiredmerchanttemp WHERE ownerid = ?");
            ps.setInt(1, getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.close();
                ps.close();
                return true;
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            sqlException(e);
        }
        return false;
    }

    public void setHasMerchant(boolean set) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?");
            ps.setInt(1, set ? 1 : 0);
            ps.setInt(2, getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            sqlException(e);
        }
        hasMerchant = set;
    }

    private static void sqlException(SQLException e) {
        System.out.println("SQL Error: " + e);
    }

    private static void sqlException(RemoteException e) {
        System.out.println("SQL Error: " + e);
    }

    /**
     * Allows you to change someone's NXCash, Maple Points, and Gift Tokens!
     *
     * Created by Acrylic/Penguins
     *
     * @param type: 1 = Paypal, 2 = MP, 4 = GT
     * @param quantity: how much to modify it by. Negatives subtract points,
     * Positives add points.
     */
    public void modifyCSPoints(int type, int quantity) {
        switch (type) {
            case 1:
                this.paypalnx += quantity;
                break;
            case 2:
                this.maplepoints += quantity;
                break;
            case 4:
                this.cardnx += quantity;
                break;
            case 5:
                this.BetaPoints += quantity;
                break;
        }
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
                return this.paypalnx;
            case 2:
                return this.maplepoints;
            case 4:
                return this.cardnx;
            case 5:
                return this.BetaPoints;
            default:
                return 0;
        }
    }
    
        public void empty() {
        this.cancelMapTimeLimitTask();
        this.cancelAllBuffs();
        this.anticheat = null;
        if (dragonBloodSchedule != null) {
            dragonBloodSchedule.cancel(false);
        }
        if (hpDecreaseTask != null) {
            hpDecreaseTask.cancel(false);
        }
        if (beholderHealingSchedule != null) {
            beholderHealingSchedule.cancel(false);
        }
        if (beholderBuffSchedule != null) {
            beholderBuffSchedule.cancel(false);
        }
        if (BerserkSchedule != null) {
            BerserkSchedule.cancel(false);
        }
        if (fullnessSchedule != null) {
            fullnessSchedule.cancel(false);
        }

        if (fullnessSchedule_1 != null) {
            fullnessSchedule_1.cancel(false);
        }

        if (fullnessSchedule_2 != null) {
            fullnessSchedule_2.cancel(false);
        }
        for (ScheduledFuture<?> sf : timers) {
            sf.cancel(false);
        }
        timers.clear();
        if (maplemount != null) {
            maplemount.empty();
            maplemount = null;
        }
        if (this.mgc != null) {
            this.mgc.setOnline(false);
        }
        this.mgc = null;
    }
        
    
    
        public boolean haveItemEquipped(int itemid) {
        if (getInventory(MapleInventoryType.EQUIPPED).findById(itemid) != null) {
            return true;
        }
        return false;
   }
    
    public boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        int possesed = inventory[MapleItemInformationProvider.getInstance().getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped)
            possesed += inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        return greaterOrEquals ? possesed >= quantity : possesed == quantity;
}

    public final void dropOverheadMessage(String msg) {
        getClient().announce(MaplePacketCreator.sendHint(msg, 500, 10));
        getClient().announce(MaplePacketCreator.enableActions());
    }

    public void announce(MaplePacket packet) {
        client.announce(packet);
    }

    public HiredMerchant getHiredMerchant() {
        return hiredMerchant;
    }

    public void autoban(String reason, int greason) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        Timestamp TS = new Timestamp(cal.getTimeInMillis());
        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banreason = ?, tempban = ?, greason = ? WHERE id = ?")) {
                ps.setString(1, reason);
                ps.setTimestamp(2, TS);
                ps.setInt(3, greason);
                ps.setInt(4, accountid);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
        }
    }

    public void setClan(int num) {
        clan = num;
    }

    public int getClan() {
        return clan;
    }

    public void setGMText(int text) {
        gmtext = text;
    }

    public int getGMText() {
        return gmtext;
    }

   
    public void resetAfkTimer(){
        this.afkTimer = System.currentTimeMillis();
    }

    public long getAfkTimer(){
        return System.currentTimeMillis() - this.afkTimer;
    }

    public void setMeso(int set) {
        meso.set(set);
        updateSingleStat(MapleStat.MESO, set, false);
    }

    public boolean getCanSmega() {
        return canSmega;
    }

    public void setCanSmega(boolean yn) {
        canSmega = yn;
    }

    public boolean getSmegaEnabled() {
        return smegaEnabled;
    }

    public void setSmegaEnabled(boolean yn) {
        smegaEnabled = yn;
    }
    
    /* Natal */
     public boolean getCanNatal() {
        return canNatal;
    }

    public void setCanNatal(boolean yn) {
        canNatal = yn;
    }

    public boolean getNatalEnabled() {
        return NatalEnabled;
    }

    public void setNatalEnabled(boolean yn) {
        NatalEnabled = yn;
    }
    

  public void sendServerNotice(String msg) {
  MaplePacket packet = MaplePacketCreator.serverNotice(5, msg);
  try {
  client.getChannelServer().getWorldInterface().broadcastMessage(name, packet.getBytes());
  } catch (RemoteException e) {
  getClient().getChannelServer().reconnectWorld();
     }
  }

        public void gainFame(int delta) {
        this.addFame(delta);
        this.updateSingleStat(MapleStat.FAME, this.fame);
    }

    public void yellowMessage(String m) {
        announce(MaplePacketCreator.sendYellowTip(m));
    }
//
//    public int getDropRate() {
//        return dropRate;
//    }
    
   public void checkBerserk() {
        if (BerserkSchedule != null) {
            BerserkSchedule.cancel(false);
        }
        final MapleCharacter chr = this;
        ISkill BerserkX = SkillFactory.getSkill(1320006);
        final int skilllevel = getSkillLevel(BerserkX);
        if (chr.getJob().equals(MapleJob.DARKKNIGHT) && skilllevel >= 1) {
            MapleStatEffect ampStat = BerserkX.getEffect(skilllevel);
            int x = ampStat.getX();
            int HP = chr.getHp();
            int MHP = chr.getMaxHp();
            int ratio = HP * 100 / MHP;
            Berserk = ratio < x;
            BerserkSchedule = TimerManager.getInstance().register(new Runnable() {
                @Override
                public void run() {
                    getClient().getSession().write(MaplePacketCreator.showOwnBerserk(skilllevel, Berserk));
                    getMap().broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBerserk(getId(), skilllevel, Berserk), false);
                }
            }, 5000, 3000);
        }
    }

    // Smega Stuff
    public void setPrefixStuff(int lol) {
        this.prefixstuff = (byte) lol;
    }

    public byte getPrefixStuff() {
        return prefixstuff;
    }
   
    public String getLegend() {
        return this.legend;
    }

        public void setGMLevel(int level) {
        if (level >= 5) {
            this.gmLevel = 5;
        } else if (level < 0) {
            this.gmLevel = 0;
        } else {
            this.gmLevel = level;
        }
    }

      public void alertaInicioJQ(int jq) {
        StringBuilder sb = new StringBuilder();
        String readableTargetName = MapleCharacterUtil.makeMapleReadable(getName());
        sb.append("[Alerta] ");
        sb.append("(").append(readableTargetName).append(")");
        sb.append(" acaba de comecar o JQ: ");
        sb.append(jq);
        sb.append(".");
        jqStart = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(System.currentTimeMillis()));
        sb.append(" Iniciado em : ").append(jqStart).append(". ");
        try {
            getClient().getChannelServer().broadcastGMPacket(MaplePacketCreator.serverNotice(5, sb.toString()));
            FilePrinter.JumpQuest(readableTargetName + ".txt", sb.toString(), true);
        } catch (Exception ignored) {
        }
    }    
        
        
   public void alertaTerminoJQ() {
        StringBuilder sb = new StringBuilder();
        String readableTargetName = MapleCharacterUtil.makeMapleReadable(getName());
        sb.append("[Alert] ");
        sb.append("(").append(readableTargetName).append(")");
        sb.append(" just finished the JQ: ");
        sb.append(getMapId());
        lastJQFinish = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(System.currentTimeMillis()));
        sb.append(" Started in : ").append(jqStart).append(", and completed in : ").append(lastJQFinish).append(". ");
        try {
            getClient().getChannelServer().broadcastGMPacket(MaplePacketCreator.serverNotice(5, sb.toString()));
            FilePrinter.JumpQuest(readableTargetName + ".txt", sb.toString(), true);
        } catch (Exception ignored) {
        }

    }

    public void ganhaJQPoints() {
         if (jqStart == null) {
            dropMessage("You did not begin the JQ properly and therefore there is no reward for you!");
            return;

        }
         getClient().getSession().write(MaplePacketCreator.serverNotice(6, "[JQ Points] You gained 3 JQ Points."));
         addjqpoints(3);
         addjqrankpoints(3);
         changeMap(100000000);
    }

    public long getLatestMarriageRequest() {
        return latestMarriageReq;
    }

    public void setLatestMarriageRequest() {
        latestMarriageReq = System.currentTimeMillis();
    }

    public void Hide(boolean hide, boolean login) {
        if (isGM() && hide != this.hidden) {
            if (!hide) {
                this.hidden = false;
                getMap().broadcastNonGmMessage(this, MaplePacketCreator.spawnPlayerMapobject(this));
                updatePartyMemberHP();
            } else {
                this.hidden = true;
                if (!login) {
                    getMap().broadcastNonGmMessage(this, MaplePacketCreator.removePlayerFromMap(getId()));
                }
            }
            dropMessage(5, "You are currently " + (hidden ? "hidden." : "not hidden."));
        }
    }

    public int getPreviousMap() {
        return previousMap;
    }

    public void setPreviousMap(int mid) {
        previousMap = mid;
    }

    public void removeDisease(MapleDisease disease) {
        synchronized (diseases) {
            if (diseases.contains(disease)) {
                diseases.remove(disease);
            }
        }
    }

        public long getUseTime() {
        return useTime;
    }

    public void sendPolice(String text) {
         getClient().getSession().write(MaplePacketCreator.getNPCTalk(9000019, (byte) 0, text, "00 00"));
        this.isbanned = true;
        TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                client.disconnect(false); //FAGGOTS
            }
        }, 6000);
    }

   public TimerManager getTimerManager() {
        return TimerManager.getInstance();
    }

   
   
    public static class MapleCoolDownValueHolder {

        public int skillId;
        public long startTime;
        public long length;
        public ScheduledFuture<?> timer;

        public MapleCoolDownValueHolder(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
            super();
            this.skillId = skillId;
            this.startTime = startTime;
            this.length = length;
            this.timer = timer;
        }
    }

    public static class SkillEntry {

        public int skillevel;
        public int masterlevel;

        public SkillEntry(int skillevel, int masterlevel) {
            this.skillevel = skillevel;
            this.masterlevel = masterlevel;
        }

        @Override
        public String toString() {
            return skillevel + ":" + masterlevel;
        }
    }

    public enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH
    }

    public int getBuddyCapacity() {
        return buddylist.getCapacity();
    }

    public void setBuddyCapacity(int capacity) {
        buddylist.setCapacity(capacity);
        client.getSession().write(MaplePacketCreator.updateBuddyCapacity(capacity));
    }

    public MapleMessenger getMessenger() {
        return messenger;
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void checkMessenger() {
        if (messenger != null && messengerposition < 4 && messengerposition > -1) {
            try {
                WorldChannelInterface wci = ChannelServer.getInstance(client.getChannel()).getWorldInterface();
                MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(client.getPlayer(), messengerposition);
                wci.silentJoinMessenger(messenger.getId(), messengerplayer, messengerposition);
                wci.updateMessenger(getClient().getPlayer().getMessenger().getId(), getClient().getPlayer().getName(), getClient().getChannel());
            } catch (RemoteException e) {
                client.getChannelServer().reconnectWorld();
            }
        }
    }

    public int getMessengerPosition() {
        return messengerposition;
    }

    public void setMessengerPosition(int position) {
        this.messengerposition = position;
    }

    public int getOmokPoints(String type) { // wins, losses, ties
        ServernoticeMapleClientMessageCallback cm = new ServernoticeMapleClientMessageCallback(this.getClient());
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        String name = this.getName();
        int points = 0;

        try {
            ps = con.prepareStatement("SELECT * FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
            }
            points = rs.getInt("omok" + type);
            ps.close();
            return points;
        } catch (SQLException e) {
            cm.dropMessage("Exception has occured: " + e);
        }
        return points;
    }

    public void setOmokPoints(MapleCharacter visitor, int winnerslot) { // 1 = owner, 2 = visitor 3 = tie
        ServernoticeMapleClientMessageCallback cm = new ServernoticeMapleClientMessageCallback(this.getClient());
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        PreparedStatement ps2;
        String name = this.getName();
        String name2 = visitor.getName();

        try {
            if (winnerslot < 3) {
                ps = con.prepareStatement("UPDATE characters SET omokwins = omokwins + 1 WHERE name = ?");
                if (winnerslot == 1) {
                    ps.setString(1, name);
                }
                if (winnerslot == 2) {
                    ps.setString(1, name2);
                }
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement("UPDATE characters SET omoklosses = omoklosses + 1 WHERE name = ?");
                if (winnerslot == 1) {
                    ps.setString(1, name2);
                }
                if (winnerslot == 2) {
                    ps.setString(1, name);
                }
                ps.executeUpdate();
                ps.close();
            }
            if (winnerslot == 3) {
                ps = con.prepareStatement("UPDATE characters SET omokties = omokties + 1 WHERE name = ? OR name = ?");
                ps.setString(1, name);
                ps.setString(2, name2);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            cm.dropMessage("Exception has occured: " + e);
            return;
        }
    }

    public int getMatchCardPoints(String type) { // wins, losses, ties
        ServernoticeMapleClientMessageCallback cm = new ServernoticeMapleClientMessageCallback(this.getClient());
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        String name = this.getName();
        int points = 0;

        try {
            ps = con.prepareStatement("SELECT * FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
            }
            points = rs.getInt("matchcard" + type);
            ps.close();
            return points;
        } catch (SQLException e) {
            cm.dropMessage("Exception has occured: " + e);
        }
        return points;
    }

    public void setMatchCardPoints(MapleCharacter visitor, int winnerslot) { // 1 = owner, 2 = visitor 3 = tie
        ServernoticeMapleClientMessageCallback cm = new ServernoticeMapleClientMessageCallback(this.getClient());
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        PreparedStatement ps2;
        String name = this.getName();
        String name2 = visitor.getName();

        try {
            if (winnerslot < 3) {
                ps = con.prepareStatement("UPDATE characters SET matchcardwins = matchcardwins + 1 + WHERE name = ?");
                if (winnerslot == 1) {
                    ps.setString(1, name);
                }
                if (winnerslot == 2) {
                    ps.setString(1, name2);
                }
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement("UPDATE characters SET matchcardlosses = matchcardlosses + 1 WHERE name = ?");
                if (winnerslot == 1) {
                    ps.setString(1, name2);
                }
                if (winnerslot == 2) {
                    ps.setString(1, name);
                }
                ps.executeUpdate();
                ps.close();
            }
            if (winnerslot == 3) {
                ps = con.prepareStatement("UPDATE characters SET matchcardties = matchcardties + 1 WHERE name = ? OR name = ?");
                ps.setString(1, name);
                ps.setString(2, name2);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            cm.dropMessage("Exception has occured: " + e);
            return;
        }
    }

    public boolean getNXCodeValid(String code, boolean validcode) throws SQLException {

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT `valid` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            validcode = rs.getInt("valid") == 0 ? false : true;
        }

        rs.close();
        ps.close();

        return validcode;
    }

    public int getNXCodeType(String code) throws SQLException {

        int type = -1;
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT `type` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            type = rs.getInt("type");
        }

        rs.close();
        ps.close();

        return type;
    }

    public int getNXCodeItem(String code) throws SQLException {

        int item = -1;
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT `item` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            item = rs.getInt("item");
        }

        rs.close();
        ps.close();

        return item;
    }

    public void setNXCodeUsed(String code) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET `valid` = 0 WHERE code = ?");
        ps.setString(1, code);
        ps.executeUpdate();
        ps = con.prepareStatement("UPDATE nxcode SET `user` = ? WHERE code = ?");
        ps.setString(1, this.getName());
        ps.setString(2, code);
        ps.executeUpdate();
        ps.close();
    }

    public void setInCS(boolean yesno) {
        this.incs = yesno;
    }

    public boolean inCS() {
        return this.incs;
    }

    public void setInMTS(boolean yesno) {
        this.inmts = yesno;
    }

    public boolean inMTS() {
        return this.inmts;
    }

    public void checkCoolDown(ISkill skill) {
        if (gmLevel >= 4) {
            return;
        }
        if (this.coolDowns.containsKey(Integer.valueOf(skill.getId()))) {
            this.getClient().getSession().close();
        }
        int coolDown = skill.getEffect(getSkillLevel(skill)).getCooldown();
        getClient().getSession().write(MaplePacketCreator.skillCooldown(skill.getId(), coolDown));
        ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, skill.getId()), coolDown * 1000);
        addCooldown(skill.getId(), System.currentTimeMillis(), coolDown * 1000, timer);
    }
    
    

    public void addCooldown(int skillId, long startTime, long length, ScheduledFuture<?> timer) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
            this.coolDowns.remove(skillId);
        }
        MapleCoolDownValueHolder holder = new MapleCoolDownValueHolder(skillId, startTime, length, timer);
        this.coolDowns.put(Integer.valueOf(skillId), holder);
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
            this.coolDowns.remove(Integer.valueOf(skillId));
            this.getClient().getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
        } else {
            log.warn("Could not remove cooldown, skillID: " + skillId);
        }
    }

    public boolean skillisCooling(int skillId) {
        return this.coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void giveCoolDowns(final List<PlayerCoolDownValueHolder> cooldowns) {
        for (PlayerCoolDownValueHolder cooldown : cooldowns) {
            int time = (int) ((cooldown.length + cooldown.startTime) - System.currentTimeMillis());
            ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, cooldown.skillId), time);
            addCooldown(cooldown.skillId, System.currentTimeMillis(), time, timer);
        }
    }

    public List<PlayerCoolDownValueHolder> getAllCooldowns() {
        List<PlayerCoolDownValueHolder> ret = new ArrayList<PlayerCoolDownValueHolder>();
        for (MapleCoolDownValueHolder mcdvh : coolDowns.values()) {
            ret.add(new PlayerCoolDownValueHolder(mcdvh.skillId, mcdvh.startTime, mcdvh.length));
        }
        return ret;
    }

    public static class CancelCooldownAction implements Runnable {

        private int skillId;
        private WeakReference<MapleCharacter> target;

        public CancelCooldownAction(MapleCharacter target, int skillId) {
            this.target = new WeakReference<MapleCharacter>(target);
            this.skillId = skillId;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.removeCooldown(skillId);
                realTarget.getClient().getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
            }
        }
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill) {
        giveDebuff(disease, skill, false);
    }

   public void giveCoolDowns(final int skillid, long starttime, long length) {
        int time = (int) ((length + starttime) - System.currentTimeMillis());
        ScheduledFuture<?> timer = TimerManager.getInstance().schedule(new CancelCooldownAction(this, skillid), time);
        addCooldown(skillid, System.currentTimeMillis(), time, timer);
    }

    
     public String getMapName(int mapId) {
        return client.getChannelServer().getMapFactory().getMap(mapId).getMapName();
    }



   //should we synch these for the luls? YES.
    public void giveDebuff(MapleDisease disease, MobSkill skill, boolean cpq) {
        synchronized (diseases) {
            if (immune) {
                return;
            }
            if (isAlive() && !isActiveBuffedValue(2321005) && !diseases.contains(disease) && (diseases.size() < 2) || cpq) {
                diseases.add(disease);
                List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(skill.getX())));
                long mask = 0;
                for (Pair<MapleDisease, Integer> statup : debuff) {
                    mask |= statup.getLeft().getValue();
                }
                getClient().getSession().write(MaplePacketCreator.giveDebuff(mask, debuff, skill));
                getMap().broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(id, mask, skill), false);

                if (isAlive() && diseases.contains(disease)) {
                    final MapleCharacter character = this;
                    final MapleDisease disease_ = disease;
                    TimerManager.getInstance().schedule(new Runnable() {

                        @Override
                        public void run() {
                            if (character.diseases.contains(disease_)) {
                                dispelDebuff(disease_);
                            }
                        }
                    }, skill.getDuration());
                }
            }
        }
    }

    public List<MapleDisease> getDiseases() {
        return diseases;
    }

 public void dispelDebuff(MapleDisease debuff) {
        if (diseases.contains(debuff)) {
            diseases.remove(debuff);
            long mask = debuff.getValue();
            getClient().getSession().write(MaplePacketCreator.cancelDebuff(mask));
            getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);
        }
    }
    
    public void dispelSeduce() {
        List<MapleDisease> disease_ = new ArrayList<MapleDisease>();
        for (MapleDisease disease : diseases)
            if (disease == MapleDisease.SEDUCE) {
                disease_.add(disease);
                getClient().getSession().write(MaplePacketCreator.cancelDebuff(disease_));
                getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, disease_), false);
                disease_.clear();
            }
        this.diseases.clear();
    }

   public void cancelAllDebuffs() {
        for (int i = 0; i < diseases.size(); i++) {
            diseases.remove(i);
            long mask = 0;
            for (MapleDisease statup : diseases) {
                mask |= statup.getValue();
            }
            getClient().getSession().write(MaplePacketCreator.cancelDebuff(mask));
            getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(id, mask), false);
        }
    }

    public void setLevel(int level) {
        this.level = level - 1;
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public void sendNote(String to, String msg) throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO notes (`to`, `from`, `message`, `timestamp`) VALUES (?, ?, ?, ?)");
        ps.setString(1, to);
        ps.setString(2, this.getName());
        ps.setString(3, msg);
        ps.setLong(4, System.currentTimeMillis());
        ps.executeUpdate();
        ps.close();
    }

     public void showNote() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM notes WHERE `to`=?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, this.getName());
            ResultSet rs = ps.executeQuery();
            rs.last();
            rs.first();
            client.getSession().write(MaplePacketCreator.showNotes(rs, rs.getRow()));
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
    }
    
     public static Map<String, String> getCharacterFromDatabase(String name) {
        Map<String, String> character = new LinkedHashMap<>();

        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `id`, `accountid`, `name` FROM `characters` WHERE `name` = ?")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return null;
                    }

                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        character.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                    }
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return character;
    }

    public void deleteNote(int id) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

   private void prepareBeholderEffect() {
	if (beholderHealingSchedule != null) {
	    beholderHealingSchedule.cancel(false);
	}
	if (beholderBuffSchedule != null) {
	    beholderBuffSchedule.cancel(false);
	}
	ISkill bHealing = SkillFactory.getSkill(1320008);
	int bHealingLvl = getSkillLevel(bHealing);
	if (bHealingLvl > 0) {
	    final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
	    int healInterval = healEffect.getX() * 1000;
	    beholderHealingSchedule = TimerManager.getInstance().register(new Runnable() {

		@Override
		public void run() {
		    addHP(healEffect.getHp());
		    client.getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
		    map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), 1321007, 5), true);
		    map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), 1321007, 2), false);
		}
	    }, healInterval, healInterval);
	}
	ISkill bBuff = SkillFactory.getSkill(1320009);
	int bBuffLvl = getSkillLevel(bBuff);
	if (bBuffLvl > 0) {
	    final MapleStatEffect buffEffect = bBuff.getEffect(bBuffLvl);
	    int buffInterval = buffEffect.getX() * 1000;
	    beholderBuffSchedule = TimerManager.getInstance().register(new Runnable() {

		@Override
		public void run() {
		    buffEffect.applyTo(MapleCharacter.this);
		    client.getSession().write(MaplePacketCreator.showOwnBuffEffect(1321007, 2));
		    map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.summonSkill(getId(), 1321007, (int) (Math.random() * 3) + 6), true);
		    map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(getId(), 1321007, 2), false);
		}
	    }, buffInterval, buffInterval);
	}
    }


    public int getMarriageQuestLevel() {
        return marriageQuestLevel;
    }

    public void setMarriageQuestLevel(int nf) {
        marriageQuestLevel = nf;
    }

    public void addMarriageQuestLevel() {
        marriageQuestLevel += 1;
    }

    public void subtractMarriageQuestLevel() {
        marriageQuestLevel -= 1;
    }

    public void setCanTalk(int yesno) {
        this.canTalk = yesno;
    }

    public int getCanTalk() {
        return this.canTalk;
    }

    public void setZakumLevel(int level) {
        this.zakumLvl = level;
    }

    public int getZakumLevel() {
        return this.zakumLvl;
    }

    public void addZakumLevel() {
        this.zakumLvl += 1;
    }

    public void subtractZakumLevel() {
        this.zakumLvl -= 1;
    }

    public void setMarried(int mmm) {
        this.married = mmm;
    }

    public void setPartnerId(int pem) {
        this.partnerid = pem;
    }

    public int isMarried() {
        return married;
    }

    public MapleCharacter getPartner() {
        MapleCharacter test = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(partnerid);
        if (test != null) {
            return test;
        }
        return null;
    }


    public int countItem(int itemid) {
        MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemid);
        MapleInventory iv = inventory[type.ordinal()];
        int possesed = iv.countById(itemid);
        return possesed;
    }

    public boolean isLogchat() {
        return logchat;
    }

    public void setLogchat(boolean logchat) {
        this.logchat = logchat;
    }

    public long getLastChatLog() {
        return lastChatLog;
    }

    public void setLastChatLog(long lastChatLog) {
        this.lastChatLog = lastChatLog;
    }

    public int getCP() {
        return cp;
    }

     public void gainCP(int gain) {
        if (gain > 0) {
            this.setTotalCP(this.getTotalCP() + gain);
        }
        this.setCP(this.getCP() + gain);
        if (this.getParty() != null) {
            this.getMonsterCarnival().setCP(this.getMonsterCarnival().getCP(team) + gain, team);
            if (gain > 0) {
                this.getMonsterCarnival().setTotalCP(this.getMonsterCarnival().getTotalCP(team) + gain, team);
            }
        }
        if (this.getCP() > this.getTotalCP()) {
            this.setTotalCP(this.getCP());
        }
        this.getClient().getSession().write(MaplePacketCreator.CPUpdate(false, this.getCP(), this.getTotalCP(), getTeam()));
        if (this.getParty() != null && getTeam() != -1) {
            this.getMap().broadcastMessage(MaplePacketCreator.CPUpdate(true, this.getMonsterCarnival().getCP(team), this.getMonsterCarnival().getTotalCP(team), getTeam()));
        } else {
            log.warn(getName() + " is either not in a party or .. team: " + getTeam());
        }
    }

    public void setTotalCP(int a) {
        this.totCP = a;
    }

    public void setCP(int a) {
        this.cp = a;
    }

    public int getTotalCP() {
        return totCP;
    }

    public void resetCP() {
        this.cp = 0;
        this.totCP = 0;
        this.monsterCarnival = null;
    }

    public MonsterCarnival getMonsterCarnival() {
        return monsterCarnival;
    }

    public void setMonsterCarnival(MonsterCarnival monsterCarnival) {
        this.monsterCarnival = monsterCarnival;
    }

    public long getLastPortalEntry() {
        return lastPortalEntry;
    }

    public void setLastPortalEntry(long lastPortalEntry) {
        this.lastPortalEntry = lastPortalEntry;
    }

    public void assassinate() {
        addHP(-30000);
        //addMP(-30000);
    }

    public boolean isInvincible() {
        for (String iname : invincible) {
            if (iname.equalsIgnoreCase(this.getName())) {
                return true;
            }
        }
        return false;
    }

    public void giveItemBuff(int itemID) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        MapleStatEffect statEffect = mii.getItemEffect(itemID);
        statEffect.applyTo(this);
    }

    public boolean isLord() {
        return lord;
    }

    public void setLord(boolean a) {
        this.lord = a;
    }

    public int getBattleshipMaxHp() {
        return ((this.getLevel() - 120) * 2000) + (this.getSkillLevel(SkillFactory.getSkill(5221006)) * 4000);
    }

    public int getBattleShipHp() {
        return battleShipHp;
    }

    public void setBattleShipHp(int a) {
        this.battleShipHp = a;
    }

    public void damageBattleShip(int dmg) {
        this.battleShipHp -= dmg;
        if (this.battleShipHp <= 0) {
            destroyBattleShip();
        }
    }

    public boolean hasBattleShip() {
        try {
            LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
            for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                if (mbsvh.effect.getSourceId() == 5221006) {
                    return true;
                }
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public void destroyBattleShip() {
        if (hasBattleShip()) {
            LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>(effects.values());
            for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                if (mbsvh.effect.getSourceId() == 5221006) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
            getClient().getSession().write(MaplePacketCreator.serverNotice(5, "Your battleship has been destroyed."));
            battleShipCooldown();
        }
    }

    private void battleShipCooldown() {
        ISkill skill = SkillFactory.getSkill(5221006);
        this.checkCoolDown(skill);
    }

    public long getLastNpcTalk() {
        return lastNpcTalk;
    }

    public void setLastNpcTalk(long lastNpcTalk) {
        this.lastNpcTalk = lastNpcTalk;
    }

    public boolean hasVotedForLord() {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM lordvoted WHERE charid = ?");
            ps.setInt(1, this.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    public boolean voteForLord(int cid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM lordvoted WHERE charid = ?");
            ps.setInt(1, this.getId());
            if (ps.executeQuery().next()) {
                return false;
            }
            ps = con.prepareStatement("SELECT * FROM lordvotes WHERE charid = ?");
            ps.setInt(1, this.getId());
            ResultSet rs = ps.executeQuery();
            boolean hasColum = false;
            int votes = 0;
            if (rs.next()) {
                hasColum = true;
                votes = rs.getInt("votes") + 1;
            }
            if (hasColum) {
                ps = con.prepareStatement("UPDATE lordvotes SET votes = ? WHERE charid = ?");
                ps.setInt(1, votes);
                ps.setInt(2, cid);
                ps.executeUpdate();
            } else {
                ps = con.prepareStatement("INSERT INTO lordvotes(charid, votes) VALUES (?, ?)");
                ps.setInt(1, cid);
                ps.setInt(2, 1);
                ps.executeUpdate();
            }
            ps = con.prepareStatement("INSERT INTO lordvoted (charid) VALUES (?)");
            ps.setInt(1, this.getId());
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            log.error("Error", ex);
            return false;
        }
    }

    public int getVotesForLord(int cid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM lordvotes WHERE charid = ?");
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("votes");
            }
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public int applyForLord() {
        if (this.getLevel() < 120) {
            return -1;
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM lordvotes WHERE charid = ?");
            ps.setInt(1, this.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return 0;
            }
            ps = con.prepareStatement("INSERT INTO lordvotes (charid, votes) VALUES (?, ?)");
            ps.setInt(1, this.getId());
            ps.setInt(2, 0);
            ps.executeUpdate();
            return 1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public int getCpqRanking() {
        return cpqRanking;
    }

    public void setCpqRanking(int cpqRanking) {
        this.cpqRanking = cpqRanking;
    }

    public long getLastCatch() {
        return lastCatch;
    }

    public void setLastCatch(long lastCatch) {
        this.lastCatch = lastCatch;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public boolean hasShield() {
        return shield;
    }

    public void setShield(boolean shield) {
        this.shield = shield;
    }

    public void shield(ScheduledFuture<?> schedule) {
        if (this.shield) {
            return;
        }
        List<Pair<MapleBuffStat, Integer>> statup = Collections.singletonList(
                new Pair<MapleBuffStat, Integer>(MapleBuffStat.ARIANT_PQ_SHIELD, Integer.valueOf(1)));
        this.shield = true;
        this.getClient().getSession().write(MaplePacketCreator.giveBuff(2022269, 60 * 1000, statup));
        //this.getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(this.getId(), statup, false), false);
    }

    public void cancelShield() {
        if (getClient().getChannelServer().getPlayerStorage().getCharacterById(getId()) != null) { // are we still connected ?
            if (!this.shield) {
                return;
            }
            recalcLocalStats();
            enforceMaxHpMp();
            getClient().getSession().write(MaplePacketCreator.cancelBuff(Collections.singletonList(MapleBuffStat.ARIANT_PQ_SHIELD)));
            //getMap().broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(getId(), Collections.singletonList(MapleBuffStat.ARIANT_PQ_SHIELD)), false);
            this.shield = false;
        }
    }

    public void changePage(int page) {
        this.currentPage = page;
    }

    public void changeTab(int tab) {
        this.currentTab = tab;
    }

    public void changeType(int type) {
        this.currentType = type;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getMaxDis(MapleCharacter player) {
        IItem weapon_item = player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -11);
        if (weapon_item != null) {
            MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
            if (weapon == MapleWeaponType.SPEAR || weapon == MapleWeaponType.POLE_ARM) {
                maxDis = 106;
            }
            if (weapon == MapleWeaponType.DAGGER || weapon == MapleWeaponType.SWORD1H || weapon == MapleWeaponType.AXE1H || weapon == MapleWeaponType.BLUNT1H || weapon == MapleWeaponType.KNUCKLE) {
                maxDis = 63;
            }
            if (weapon == MapleWeaponType.SWORD2H || weapon == MapleWeaponType.AXE1H || weapon == MapleWeaponType.BLUNT1H) {
                maxDis = 73;
            }
            if (weapon == MapleWeaponType.STAFF || weapon == MapleWeaponType.WAND) {
                maxDis = 51;
            }
            if (weapon == MapleWeaponType.CLAW) {
                skil = SkillFactory.getSkill(4000001);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    maxDis = (skil.getEffect(player.getSkillLevel(skil)).getRange()) + 205;
                } else {
                    maxDis = 205;
                }
            }
            if (weapon == MapleWeaponType.BOW || weapon == MapleWeaponType.CROSSBOW) {
                skil = SkillFactory.getSkill(3000002);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    maxDis = (skil.getEffect(player.getSkillLevel(skil)).getRange()) + 270;
                } else {
                    maxDis = 270;
                }
            }
            if (weapon == MapleWeaponType.GUN) {
                maxDis = 270;
            }
        }
        return maxDis;
    }

    public int calculateAcc(MapleJob t) {
        if (t.isA(MapleJob.BEGINNER)) {
            return this.getTotalStr() + this.getTotalDex() * 2;
        } else if (t.isA(MapleJob.WARRIOR)) {
            return this.getTotalDex() * 5 + this.getTotalStr();
        } else if (t.isA(MapleJob.MAGICIAN)) {
            return this.getTotalInt() * this.getTotalLuk() * 4;
        } else if (t.isA(MapleJob.BOWMAN)) {
            return this.getTotalDex() * 6;
        } else if (t.isA(MapleJob.THIEF)) {
            ISkill skilt = SkillFactory.getSkill(4000000);
            int skillp = this.getSkillLevel(skilt);
            return this.getTotalDex() + this.getTotalLuk() * 5 + skillp;
        } else if (t.isA(MapleJob.PIRATE)) {
            return this.getTotalDex() * 3 + this.getTotalStr();
        } else if (t.isA(MapleJob.GM)) {
            return 2147483647;
        } else if (t.isA(MapleJob.SUPERGM)) {
            return 2147483647;
        } else {
            throw new RuntimeException("Job out of range.");
        }
    }

    public int calculateEva(MapleJob t) {
        if (t.isA(MapleJob.BEGINNER)) {
            return this.getTotalDex() * 2;
        } else if (t.isA(MapleJob.WARRIOR)) {
            return this.getTotalDex() * 4;
        } else if (t.isA(MapleJob.MAGICIAN)) {
            return this.getTotalLuk() * 3;
        } else if (t.isA(MapleJob.BOWMAN)) {
            return this.getTotalDex() * 2;
        } else if (t.isA(MapleJob.THIEF)) {
            ISkill skilt = SkillFactory.getSkill(4000000);
            int skillp = this.getSkillLevel(skilt);
            return this.getTotalLuk() * 4 + skillp;
        } else if (t.isA(MapleJob.PIRATE)) {
            return this.getTotalDex() * 3;
        } else if (t.isA(MapleJob.GM)) {
            return 2147483647;
        } else if (t.isA(MapleJob.SUPERGM)) {
            return 2147483647;
        } else {
            throw new RuntimeException("Job out of range.");
        }
    }

    public int calculateMinBaseDamage(MapleCharacter player) {
        int minbasedamage = 0;
        int atk = player.getTotalWatk();
        if (atk == 0) {
            minbasedamage = 1;
        } else {
            IItem weapon_item = getInventory(MapleInventoryType.EQUIPPED).getItem((byte) - 11);
            if (weapon_item != null) {
                MapleWeaponType weapon = MapleItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId());
                //mastery start
                if (player.getJob().isA(MapleJob.FIGHTER)) {
                    skil = SkillFactory.getSkill(1100000);
                    skill = player.getSkillLevel(skil);
                    if (skill > 0) {
                        sword = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                    } else {
                        sword = 0.1;
                    }
                } else {
                    skil = SkillFactory.getSkill(1200000);
                    skill = player.getSkillLevel(skil);
                    if (skill > 0) {
                        sword = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                    } else {
                        sword = 0.1;
                    }
                }
                skil = SkillFactory.getSkill(1100001);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    axe = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    axe = 0.1;
                }
                skil = SkillFactory.getSkill(1200001);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    blunt = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    blunt = 0.1;
                }
                skil = SkillFactory.getSkill(1300000);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    spear = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    spear = 0.1;
                }
                skil = SkillFactory.getSkill(1300001);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    polearm = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    polearm = 0.1;
                }
                skil = SkillFactory.getSkill(3200000);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    crossbow = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    crossbow = 0.1;
                }
                skil = SkillFactory.getSkill(3100000);
                skill = player.getSkillLevel(skil);
                if (skill > 0) {
                    bow = ((skil.getEffect(player.getSkillLevel(skil)).getMastery() * 5 + 10) / 100);
                } else {
                    bow = 0.1;
                }
                //end mastery
                if (weapon == MapleWeaponType.CROSSBOW) {
                    minbasedamage = (int) (localdex * 0.9 * 3.6 * crossbow + localstr) / 100 * (atk + 15);
                }
                if (weapon == MapleWeaponType.BOW) {
                    minbasedamage = (int) (localdex * 0.9 * 3.4 * bow + localstr) / 100 * (atk + 15);
                }
                if (getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.DAGGER)) {
                    minbasedamage = (int) (localluk * 0.9 * 3.6 * dagger + localstr + localdex) / 100 * atk;
                }
                if (!getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.DAGGER)) {
                    minbasedamage = (int) (localstr * 0.9 * 4.0 * dagger + localdex) / 100 * atk;
                }
                if (getJob().isA(MapleJob.THIEF) && (weapon == MapleWeaponType.CLAW)) {
                    minbasedamage = (int) (localluk * 0.9 * 3.6 * claw + localstr + localdex) / 100 * (atk + 15);
                }
                if (weapon == MapleWeaponType.SPEAR) {
                    minbasedamage = (int) (localstr * 0.9 * 3.0 * spear + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.POLE_ARM) {
                    minbasedamage = (int) (localstr * 0.9 * 3.0 * polearm + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.SWORD1H) {
                    minbasedamage = (int) (localstr * 0.9 * 4.0 * sword + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.SWORD2H) {
                    minbasedamage = (int) (localstr * 0.9 * 4.6 * sword + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.AXE1H) {
                    minbasedamage = (int) (localstr * 0.9 * 3.2 * axe + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.BLUNT1H) {
                    minbasedamage = (int) (localstr * 0.9 * 3.2 * blunt + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.AXE2H) {
                    minbasedamage = (int) (localstr * 0.9 * 3.4 * axe + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.BLUNT2H) {
                    minbasedamage = (int) (localstr * 0.9 * 3.4 * blunt + localdex) / 100 * atk;
                }
                if (weapon == MapleWeaponType.STAFF || weapon == MapleWeaponType.WAND) {
                    minbasedamage = (int) (localstr * 0.9 * 3.0 * staffwand + localdex) / 100 * atk;
                }
            }
        }
        return minbasedamage;
    }

    @SuppressWarnings("static-access")
    public int getRandomage(MapleCharacter player) {
        int maxdamage = player.getCurrentMaxBaseDamage();
        int mindamage = player.calculateMinBaseDamage(player);
        return player.rand(mindamage, maxdamage);
    }

    @SuppressWarnings("Black-Pussy")
    public int getMinDmg(MapleCharacter player) {
        int mindamage = player.calculateMinBaseDamage(player);
        return mindamage;
    }

    @SuppressWarnings("Black-Pussy")
    public int getMaxDmg(MapleCharacter player) {
        int maxdamage = player.getCurrentMaxBaseDamage();
        return maxdamage;
    }

    @SuppressWarnings("Black-Pussy")
    public MaplePacket makeHPBarPacket(MapleCharacter player) {
        byte tagcolor = 01;
        byte tagbgcolor = 05;
        return MaplePacketCreator.showBossHP(9400711, player.getHp(), player.getMaxHp(), tagcolor, tagbgcolor);
    }

    public int getPvpDeaths() {
        return pvpDeaths;
    }

    public void setPvpDeaths(int pvpDeaths) {
        this.pvpDeaths = pvpDeaths;
    }

    public int getPvpKills() {
        return pvpKills;
    }

    public void setPvpKills(int pvpKills) {
        this.pvpKills = pvpKills;
    }

    public void gainPvpKill() {
        this.pvpKills++;
    }

    public void gainPvpDeath() {
        this.pvpDeaths++;
    }

    public int getTotalAcc() {
        return this.acc;
    }

    public int getTotalEva() {
        return this.eva;
    }

    public int getTotalWdef() {
        return wdef;
    }

    public int getTotalMdef() {
        return mdef;
    }

    public int getPvpScore() {
        return pvpScore;
    }

    public void setPvpScore(int pvpScore) {
        this.pvpScore = pvpScore;
    }

    public void gainPvpScore() {
        this.pvpScore++;
    }

    public void resetPvpScore() {
        this.pvpScore = 0;
    }

    public boolean isChallenged() {
        return challenged;
    }

    public void setChallenged(boolean challenged) {
        this.challenged = challenged;
    }
    
     public void setPlayerVariable(String name, String value) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM player_variables WHERE name = ? AND characterid = ?");
            ps.setString(1, name);
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();
            PreparedStatement ps2;
            if (rs.next()) {
                ps2 = DatabaseConnection.getConnection().prepareStatement("UPDATE player_variables SET value = ? WHERE characterid = ? AND name = ?");
                ps2.setString(1, value);
                ps2.setInt(2, id);
                ps2.setString(3, name);
            } else {
                ps2 = DatabaseConnection.getConnection().prepareStatement("INSERT INTO player_variables (characterid, name, value) VALUES (?, ?, ?)");
                ps2.setInt(1, id);
                ps2.setString(2, name);
                ps2.setString(3, value);
            }
            ps.close();
            rs.close();
            ps2.execute();
            ps2.close();
        } catch (SQLException ex) {
            System.out.println("Error setting player variable: " + ex);
        }
    }
    
    public String getPlayerVariable(String name) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM player_variables WHERE name = ? AND characterid = ?");
            ps.setString(1, name);
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String value = rs.getString("value");
                ps.close();
                rs.close();
                return value;
            } else {
                ps.close();
                rs.close();
                return null;
            }
        } catch (SQLException ex) {
            System.out.println("Error getting player variable: " + ex);
            return null;
        }
    }
    
    public void deletePlayerVariable(String name) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM player_variables WHERE name = ? AND characterid = ?");
            ps.setString(1, name);
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ps.close();
                rs.close();
                ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM player_variables WHERE name = ? AND characterid = ?");
                ps.setString(1, name);
                ps.setInt(2, id);
                ps.execute();
            }
            ps.close();
            rs.close();
        } catch (SQLException ex) {
            System.out.println("Error deleting player variable: " + ex);
        }
    }
    
    
    boolean immune;

    public boolean getImmune() {
        return immune;
    }

    public void setImmune(boolean tf) {
        this.immune = tf;
    }

    public int getDonatorPoints() {
        return this.donatorPoints;
    }

    public void setDonatorPoints(int a) {
        this.donatorPoints = a;
    }

    public void gainDonatorPoints(int d) {
        this.donatorPoints += d;
    }
    
    public void warpToCashShop() {

        if (getClient().getChannelServer().allowCashshop()) {
            IoSession is = getClient().getSession();
            if (getNoPets() > 0) {
                unequipAllPets();
            }
            try {
                WorldChannelInterface wci = ChannelServer.getInstance(getClient().getChannel()).getWorldInterface();
                wci.addBuffsToStorage(getId(), getAllBuffs());
                wci.addCooldownsToStorage(getId(), getAllCooldowns());
            } catch (RemoteException e) {
                getClient().getChannelServer().reconnectWorld();
            }
            getMap().removePlayer(this);
            getClient().getSession().write(MaplePacketCreator.warpCS(getClient()));
            setInCS(true);
            is.write(MaplePacketCreator.enableCSUse0());
            is.write(MaplePacketCreator.enableCSUse1());
            is.write(MaplePacketCreator.enableCSUse2());
            is.write(MaplePacketCreator.enableCSUse3());
            is.write(MaplePacketCreator.showNXMapleTokens(this));
            is.write(MaplePacketCreator.sendWishList(getId()));
            saveToDB(true, true);
        }
    }

    public long getLastFJ() {
        return lastFJ;
    }

    public void setLastFJ(long lastFJ) {
        this.lastFJ = lastFJ;
    }

    public int getLastY() {
        return lastY;
    }

    public void setLastY(int lastY) {
        this.lastY = lastY;
    }

    public void setAllianceRank(int rank) {
        allianceRank = rank;
        if (mgc != null) {
            mgc.setAllianceRank(rank);
        }
    }

    public int getAllianceRank() {
        return this.allianceRank;
    }
    

    public MapleGuild getGuild() {
        try {
            return getClient().getChannelServer().getWorldInterface().getGuild(getGuildId(), null);
        } catch (RemoteException ex) {
            client.getChannelServer().reconnectWorld();
        }
        return null;
    }

    public void maxAllSkills() {
        int[] skillId = {8, /*1000, 1001, 1002,*/ 1003, 1004, 1000000, 1000001, 1000002, 1001003, 1001004, 1001005, 2000000, 2000001,
            2001002, 2001003, 2001004, 2001005, 3000000, 3000001, 3000002, 3001003, 3001004, 3001005, 4000000, 4000001, 4001002, 4001003,
            4001334, 4001344, 1100000, 1100001, 1100002, 1100003, 1101004, 1101005, 1101006, 1101007, 1200000, 1200001, 1200002, 1200003,
            1201004, 1201005, 1201006, 1201007, 1300000, 1300001, 1300002, 1300003, 1301004, 1301005, 1301006, 1301007, 2100000, 2101001,
            2101002, 2101003, 2101004, 2101005, 2200000, 2201001, 2201002, 2201003, 2201004, 2201005, 2300000, 2301001, 2301002, 2301003,
            2301004, 2301005, 3100000, 3100001, 3101002, 3101003, 3101004, 3101005, 3200000, 3200001, 3201002, 3201003, 3201004, 3201005,
            4100000, 4100001, 4100002, 4101003, 4101004, 4101005, 4200000, 4200001, 4201002, 4201003, 4201004, 4201005, 1110000, 1110001,
            1111002, 1111003, 1111004, 1111005, 1111006, 1111007, 1111008, 1210000, 1210001, 1211002, 1211003, 1211004, 1211005, 1211006,
            1211007, 1211008, 1211009, 1310000, 1311001, 1311002, 1311003, 1311004, 1311005, 1311006, 1311007, 1311008, 2110000, 2110001,
            2111002, 2111003, 2111004, 2111005, 2111006, 2210000, 2210001, 2211002, 2211003, 2211004, 2211005, 2211006, 2310000, 2311001,
            2311002, 2311003, 2311004, 2311005, 2311006, 3110000, 3110001, 3111002, 3111003, 3111004, 3111005, 3111006, 3210000, 3210001,
            3211002, 3211003, 3211004, 3211005, 3211006, 4110000, 4111001, 4111002, 4111003, 4111004, 4111005, 4111006, 4210000, 4211001,
            4211002, 4211003, 4211004, 4211005, 4211006, 1120003, 1120004, 1120005, 1121000, 1121001, 1121002, 1121006, 1121008, 1121010,
            1121011, 1220005, 1220006, 1220010, 1221000, 1221001, 1221002, 1221003, 1221004, 1221007, 1221009, 1221011, 1221012, 1320005,
            1320006, 1320008, 1320009, 1321000, 1321001, 1321002, 1321003, 1321007, 1321010, 2121000, 2121001, 2121002, 2121003, 2121004,
            2121005, 2121006, 2121007, 2121008, 2221000, 2221001, 2221002, 2221003, 2221004, 2221005, 2221006, 2221007, 2221008, 2321000,
            2321001, 2321002, 2321003, 2321004, 2321005, 2321006, 2321007, 2321008, 2321009, 3120005, 3121000, 3121002, 3121003, 3121004,
            3121006, 3121007, 3121008, 3121009, 3220004, 3221000, 3221001, 3221002, 3221003, 3221005, 3221006, 3221007, 3221008, 4120002,
            4120005, 4121000, 4121003, 4121004, 4121006, 4121007, 4121008, 4121009, 4220002, 4220005, 4221000, 4221001, 4221003, 4221004,
            4221006, 4221007, 4221008, 5000000, 5001001, 5001002, 5001003, 5001005, 5100000, 5100001, 5101002, 5101003, 5101004, 5101005,
            5101006, 5101007, 5200000, 5201001, 5201002, 5201003, 5201004, 5201005, 5201006, 5110000, 5110001, 5111002, 5111004, 5111005,
            5111006, 5220011, 5221010, 5221009, 5221008, 5221007, 5221006, 5221004, 5221003, 5220002, 5220001, 5221000, 5121010, 5121009,
            5121008, 5121007, 5121005, 5121004, 5121003, 5121002, 5121001, 5121000, 5211006, 5211005, 5211004, 5211002, 5211001, 5210000,
            9001000, 9001001, 9001002, 9101000, 9101001, 9101002, 9101003, 9101004, 9101005, 9101006, 9101007, 9101008
        };
        for (int skillzors_ : skillId) {
            maxSkill(skillzors_);
        }
    }

    public void maxSkill(int skillid) {
        if (Math.floor(skillid / 10000) == getJob().getId() || isGM() || skillid < 2000) { // lmao im lazy
            ISkill skill_ = SkillFactory.getSkill(skillid);
            int maxlevel = skill_.getMaxLevel(); // TODO - Find a less laggy way.. our xml style skill maxer was fine T____T
            changeSkillLevel(skill_, maxlevel, maxlevel);
        }
    }

        public List<Integer> getVIPRockMaps(int type) {
        List<Integer> rockmaps = new LinkedList<Integer>();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT mapid FROM VIPRockMaps WHERE cid = ? AND type = ?");
            ps.setInt(1, id);
            ps.setInt(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rockmaps.add(rs.getInt("mapid"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            return null;
        }
        return rockmaps;
    }

    public IPlayerInteractionManager getInteraction() {
        return interaction;
    }

    public void setInteraction(IPlayerInteractionManager interaction) {
        this.interaction = interaction;
    }

    public static int getAccIdFromCNAME(String name) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
                return -1;
            }
            int id_ = rs.getInt("accountid");
            ps.close();
            return id_;
        } catch (SQLException e) {
            log.error("ERROR", e);
        }
        return -1;
    }

    public int getChatMode() {
        return chatMode;
    }

    public void setChatMode(int chatMode) {
        this.chatMode = chatMode;
    }

    public int getChatRange() {
        return chatRange;
    }

    public void setChatRange(int chatRange) {
        this.chatRange = chatRange;
    }
  public boolean gainItem(int id, short quantity, boolean randomStats, boolean show) {
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            IItem item = ii.getEquipById(id);
            MapleInventoryType type = ii.getInventoryType(id);
            if (type.equals(MapleInventoryType.EQUIP) && !Configuration.isThrowingStar(item.getItemId()) && !Configuration.isBullet(item.getItemId())) {
                if (!getInventory(type).isFull()) {
                    if (randomStats) {
                        MapleInventoryManipulator.addFromDrop(getClient(), ii.randomizeStats((Equip) item), "");
                    } else {
                        MapleInventoryManipulator.addFromDrop(getClient(), (Equip) item, "");
                    }
                } else {

                    dropMessage(1, "Seu inventario esta cheio. Por favor, remover um item do seu " + type.name().toLowerCase() + ".");
                    return false;
                }
            } else if (MapleInventoryManipulator.checkSpace(getClient(), id, quantity, "")) {
                if (id >= 5000000 && id <= 5000100) {
                    if (quantity > 1) {
                        quantity = 1;
                    }
                    int petId = MaplePet.createPet(id);
                    MapleInventoryManipulator.addById(getClient(), id, (short) 1, "O item de Cash foi comprado!", null, petId);
                    if (show) {
                        this.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, quantity));
                    }
                } else {
                    MapleInventoryManipulator.addById(getClient(), id, quantity, "");
                }
            } else {

                dropMessage(1, "Seu inventario esta cheio. Por favor, remover um item do seu " + type.name().toLowerCase() + ".");
                return false;
            }
            if (show) {
                this.getClient().getSession().write(MaplePacketCreator.getShowItemGain(id, quantity, true));
            }
        } else {
            MapleInventoryManipulator.removeById(getClient(), MapleItemInformationProvider.getInstance().getInventoryType(id), id, -quantity, false, false);
        }
        return true;
    }
}