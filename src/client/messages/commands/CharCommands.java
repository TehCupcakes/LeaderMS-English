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

package client.messages.commands;

import java.awt.Point;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import static client.messages.CommandProcessor.getOptionalIntArg;
import client.IItem;
import client.ISkill;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleStat;
import client.SkillFactory;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import client.messages.ServernoticeMapleClientMessageCallback;
import config.configuration.Configuration;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShop;
import server.MapleShopFactory;
import net.channel.ChannelServer;
import server.life.MapleLifeFactory;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.StringUtil;

public class CharCommands implements Command {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
    
	@SuppressWarnings("static-access")
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
		IllegalCommandSyntaxException {
		MapleCharacter player = c.getPlayer();
                ChannelServer cserva = c.getChannelServer();
		if (splitted[0].equals("!lowhp")) {
			player.setHp(1);
			player.setMp(500);
			player.updateSingleStat(MapleStat.HP, 1);
			player.updateSingleStat(MapleStat.MP, 500);
		} else if (splitted[0].equals("!fullhp")) {
			player.setHp(player.getMaxHp());
			player.setMp(player.getMaxMp());
			player.updateSingleStat(MapleStat.HP, player.getMaxHp());
			player.updateSingleStat(MapleStat.MP, player.getMaxMp());
		} else if (splitted[0].equals("!skill")) {
			ISkill skill = SkillFactory.getSkill(Integer.parseInt(splitted[1]));
			int level = getOptionalIntArg(splitted, 2, 1);
			int masterlevel = getOptionalIntArg(splitted, 3, 1);
			if (level > skill.getMaxLevel()) {
				level = skill.getMaxLevel();
			}
			if (masterlevel > skill.getMaxLevel() && skill.isFourthJob()) {
				masterlevel = skill.getMaxLevel();
			} else {
				masterlevel = 0;
			}
			player.changeSkillLevel(skill, level, masterlevel);
		}   else if (splitted[0].equalsIgnoreCase("!reloadmapspawns")) {
                        for (Iterator<Entry<Integer, MapleMap>> it = c.getChannelServer().getMapFactory().getMaps().entrySet().iterator(); it.hasNext();) {
                            Entry<Integer, MapleMap> map = it.next();
                            map.getValue().respawn();
                        }
                } else if (splitted[0].equals("!sp")) {
			int sp = Integer.parseInt(splitted[1]);
			if (sp + player.getRemainingSp() > Short.MAX_VALUE) {
				sp = Short.MAX_VALUE;
			}
			player.setRemainingSp(sp);
			player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
		} else if (splitted[0].equals("!job")) {
			int jobId = Integer.parseInt(splitted[1]);
			if (MapleJob.getById(jobId) != null) {
				player.changeJob(MapleJob.getById(jobId));
			}
		} else if (splitted[0].equals("!whereami")) {
			new ServernoticeMapleClientMessageCallback(c).dropMessage("You are on map " + player.getMap().getId());
		} else if (splitted[0].equals("!shop")) {
			MapleShopFactory sfact = MapleShopFactory.getInstance();
			int shopId = Integer.parseInt(splitted[1]);
			if (sfact.getShop(shopId) != null) {
				MapleShop shop = sfact.getShop(shopId);
				shop.sendShop(c);
			}
		} else if (splitted[0].equals("!gainmeso")) {
			player.gainMeso(Integer.MAX_VALUE - player.getMeso(), true);
		} else if (splitted[0].equals("!levelup")) {
			if (player.getLevel() < 200) {
				player.levelUp();
				player.setExp(0);
			}
		}  else if (splitted[0].equalsIgnoreCase("!event")) {
                if (c.getChannelServer().eventOn == false) {
                int mapid = getOptionalIntArg(splitted, 1, c.getPlayer().getMapId());
                c.getChannelServer().eventOn = true;
                c.getChannelServer().eventMap = mapid;
                try {
                    c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Evento] O evento foi iniciado no canal (" + c.getChannel() + "). Use @evento para participar.").getBytes());
                } catch (RemoteException e) {
                    c.getChannelServer().reconnectWorld();
                }
            } else {
                c.getChannelServer().eventOn = false;
                try {
                    c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(6, c.getChannel(), "[Evento] O evento terminou, obrigado aqueles que participaram.").getBytes());
                } catch (RemoteException e) {
                    c.getChannelServer().reconnectWorld();
                }
            }
        } else if (splitted[0].equalsIgnoreCase("checarstats")) {
                MapleCharacter victim = cserva.getPlayerStorage().getCharacterByName(splitted[1]);
                cserva.broadcastPacket(MaplePacketCreator.serverNotice(6,"Status do personagem <" + splitted[1] +">"));
                player.dropMessage("For: " + victim.getStr());
                player.dropMessage("Des: " + victim.getDex());
                player.dropMessage("Int: " + victim.getInt());
                player.dropMessage("Sorte: " + victim.getLuk());
                player.dropMessage("Mesos: " + victim.getMeso());
                player.dropMessage("LeaderPoints: " + victim.getCSPoints(2));
                player.dropMessage("AP Disponivel: " + victim.getRemainingAp());
                player.dropMessage("SP Disponivel: " + victim.getRemainingSp());
       } else if (splitted[0].equals("!item")) {
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			short quantity = (short) getOptionalIntArg(splitted, 2, 1);
			if (Integer.parseInt(splitted[1]) >= 5000000 && Integer.parseInt(splitted[1]) <= 5000100) {
				if (quantity > 1) {
					quantity = 1;
				}
				int petId = MaplePet.createPet(Integer.parseInt(splitted[1]));
				//player.equipChanged();
				MapleInventoryManipulator.addById(c, Integer.parseInt(splitted[1]), quantity, player.getName() + "used !item with quantity " + quantity, player.getName(), petId);
				return;
			} else if (ii.isRechargable(Integer.parseInt(splitted[1]))) {
				quantity = (short) ii.getSlotMax(c, Integer.parseInt(splitted[1]));
				MapleInventoryManipulator.addById(c, Integer.parseInt(splitted[1]), quantity, "Rechargable item created.", player.getName(), -1);
				return;
			}
			MapleInventoryManipulator.addById(c, Integer.parseInt(splitted[1]), quantity, player.getName() + "used !item with quantity " + quantity, player.getName(), -1);
		 } else if (splitted[0].equals("!speakall")) {
            String text = StringUtil.joinStringFrom(splitted, 1);
            for (MapleCharacter mch : player.getMap().getCharacters())
                mch.getMap().broadcastMessage(MaplePacketCreator.getChatText(mch.getId(), text, false, 0));
        } else if (splitted[0].equals("!seduce")) {
            ChannelServer cserv = c.getChannelServer();
            if(player.isGM()){
                return;
            }
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);            
            victim.setChair(0);            
            victim.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
            victim.getMap().broadcastMessage(victim, MaplePacketCreator.showChair(c.getPlayer().getId(), 0), false);
            victim.giveDebuff(MapleDisease.SEDUCE, MobSkillFactory.getMobSkill(128, Integer.parseInt(splitted[2])));
        } else if (splitted[0].equals("!seducemap")) {
            if(player.isGM()){
                return;
            }
            for (MapleCharacter map : player.getMap().getCharacters()) {
                map.setChair(0);                    
                map.getClient().getSession().write(MaplePacketCreator.cancelChair(-1));
                map.getMap().broadcastMessage(map, MaplePacketCreator.showChair(c.getPlayer().getId(), 0), false);
                map.giveDebuff(MapleDisease.SEDUCE, MobSkillFactory.getMobSkill(128, Integer.parseInt(splitted[1])));
            }
  } else if (splitted[0].equals("!bomb")) {
    for (MapleCharacter chr : player.getMap().getCharacters()) 
        player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300166), chr.getPosition());
    for (int i = 0; i < 250; i += 50) {
        player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300166), new Point(player.getPosition().x - i, player.getPosition().y));
        player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9300166), new Point(player.getPosition().x + i, player.getPosition().y));
    }
  }  else if (splitted[0].equals("!unbuffmap")) {
            for (MapleCharacter map : player.getMap().getCharacters()) {
                if (map != null && map != player) {
                    map.cancelAllBuffs();
                }
            }
         } else if (splitted[0].equals("setall")) {
            final int x = Short.parseShort(splitted[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, x);
            player.updateSingleStat(MapleStat.DEX, x);
            player.updateSingleStat(MapleStat.INT, x);
            player.updateSingleStat(MapleStat.LUK, x);
        } else if (splitted[0].equals("!drop")) {
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			int itemId = Integer.parseInt(splitted[1]);
			short quantity = (short) (short) getOptionalIntArg(splitted, 2, 1);
			IItem toDrop;
			if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
				toDrop = ii.getEquipById(itemId);
			} else {
				toDrop = new Item(itemId, (byte) 0, (short) quantity);
			}
			toDrop.log("Created by " + player.getName() + " using !drop. Quantity: " + quantity, false);
			toDrop.setOwner(player.getName());
			player.getMap().spawnItemDrop(player, player, toDrop, player.getPosition(), true, true);
		} else if (splitted[0].equals("!maxlevel")) {
			player.setExp(0);
			while (player.getLevel() < 200) {
				player.levelUp();
			}
		} else if (splitted[0].equals("!closemerchants")) {
            mc.dropMessage("Fechando e salvando os comerciantes, por favor aguarde ...");
            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                for (MapleCharacter players : channel.getPlayerStorage().getAllCharacters()) {
                    players.getInteraction().closeShop(true);
                }
            }
            mc.dropMessage("All merchants have been closed and saved.");
        } else if (splitted[0].equalsIgnoreCase("!clearinv")) {
            if (splitted.length < 2) {
                mc.dropMessage("eq, use, setup, etc, cash.");
            }
            int x = 0;
            if (splitted[1].equalsIgnoreCase("all")) {
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, (byte) x, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) x).getQuantity(), false, true);
                }
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (byte) x, c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) x).getQuantity(), false, true);
                }
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, (byte) x, c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) x).getQuantity(), false, true);
                }
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, (byte) x, c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((byte) x).getQuantity(), false, true);
                }
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (byte) x, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) x).getQuantity(), false, true);
                }
                mc.dropMessage("All slots cleared.");
            } else if (splitted[1].equalsIgnoreCase("eq")) {
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, (byte) x, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) x).getQuantity(), false, true);
                }
                mc.dropMessage("Eq inventory slots cleared.");
            } else if (splitted[1].equalsIgnoreCase("use")) {
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (byte) x, c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) x).getQuantity(), false, true);
                }
                mc.dropMessage("Use inventory slots cleared.");
            } else if (splitted[1].equalsIgnoreCase("setup")) {
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, (byte) x, c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) x).getQuantity(), false, true);
                }
                mc.dropMessage("Setup inventory slots cleared.");
            } else if (splitted[1].equalsIgnoreCase("etc")) {
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, (byte) x, c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((byte) x).getQuantity(), false, true);
                }
                mc.dropMessage("Etc inventory slots cleared.");
            } else if (splitted[1].equalsIgnoreCase("cash")) {
                while (x < 101) {
                    if (c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) x) == null) {
                        x++;
                    }
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (byte) x, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) x).getQuantity(), false, true);
                }
                mc.dropMessage("Cash inventory slots cleared.");
            } else {
                mc.dropMessage("!#clearslot " + splitted[1] + " does not exist!");
            }
        } else if (splitted[0].equals("!online")) {
			mc.dropMessage("Characters connected to channel " + c.getChannel() + ":");
			Collection<MapleCharacter> chrs = c.getChannelServer().getInstance(c.getChannel()).getPlayerStorage().getAllCharacters();
			for (MapleCharacter chr : chrs) {
				mc.dropMessage(chr.getName() + " at map ID: " + chr.getMapId());
			}
			mc.dropMessage("Total characters on channel " + c.getChannel() + ": " + chrs.size());
		} else if (splitted[0].equals("!saveall")) {
			Collection<ChannelServer> cservs = ChannelServer.getAllInstances();
			for (ChannelServer cserv : cservs) {
				mc.dropMessage("Saving all characters in channel " + cserv.getChannel() + "...");
				Collection<MapleCharacter> chrs = cserv.getPlayerStorage().getAllCharacters();
				for (MapleCharacter chr : chrs) {
					chr.saveToDB(true, false);
				}
			}
			mc.dropMessage("All characters saved.");
		} else if (splitted[0].equals("!ariantpq")) {
			if (splitted.length < 2) {
				player.getMap().AriantPQStart();
			} else {
				c.getSession().write(MaplePacketCreator.updateAriantPQRanking(splitted[1], 5, false));
			}
		} else if (splitted[0].equals("!scoreboard")) {
			player.getMap().broadcastMessage(MaplePacketCreator.showAriantScoreBoard());
		}  else if (splitted[0].equals("!votepoints")) {
            cserva.getPlayerStorage().getCharacterByName(splitted[1]).gainvotePoints(Integer.parseInt(splitted[2]));
            player.dropMessage(6, "Feito, foi enviado para " + splitted[1] + " a quantia de " + splitted[2] + ".");
        } else if (splitted[0].equals("!leaderpoints")) {
            cserva.getPlayerStorage().getCharacterByName(splitted[1]).gainLeaderPoints(Integer.parseInt(splitted[2]));
            player.dropMessage(6, "Feito, foi enviado para " + splitted[1] + " a quantia de " + splitted[2] + ".");
        } else if (splitted[0].equals("!betapoints")) {
            cserva.getPlayerStorage().getCharacterByName(splitted[1]).modifyCSPoints(5, Integer.parseInt(splitted[2]));
            player.dropMessage(6, "Feito, foi enviado para " + splitted[1] + " a quantia de " + splitted[2] + ".");
        } else if (splitted[0].equals("!jqpoints")) {
            cserva.getPlayerStorage().getCharacterByName(splitted[1]).addjqpoints(Integer.parseInt(splitted[2]));
            player.dropMessage(6, "Feito, foi enviado para " + splitted[1] + " a quantia de " + splitted[2] + ".");
        } else if (splitted[0].equals("!pqpoints")) {
            cserva.getPlayerStorage().getCharacterByName(splitted[1]).gainpqPoints(Integer.parseInt(splitted[2]));
            player.dropMessage(6, "Feito, foi enviado para " + splitted[1] + " a quantia de " + splitted[2] + ".");
        } else if (splitted[0].equals("!givepresent")) {
            cserva.getPlayerStorage().getCharacterByName(splitted[1]).gainItem();
            player.dropMessage(6, "Feito, foi enviado um presente para " + splitted[1] + ".");
        } else if (splitted[0].equals("!giftnx")) {
            for (int i = 1; i < 5; i *= 2) {
                cserva.getPlayerStorage().getCharacterByName(splitted[1]).modifyCSPoints(i, Integer.parseInt(splitted[2]));
            }
            player.dropMessage(6, "Feito, foi enviado para " + splitted[1] + " a quantia de " + splitted[2] + ".");
        } else if (splitted[0].equals("!conectados")) {
            try {
                Map<Integer, Integer> connected = cserva.getWorldInterface().getConnected();
                StringBuilder conStr = new StringBuilder("Connected Clients : ");
                boolean first = true;
                for (int i : connected.keySet()) {
                    if (!first) {
                        conStr.append(", ");
                    } else {
                        first = false;
                    }
                    if (i == 0) {
                        conStr.append("Total : " + connected.get(i));
                    } else {
                        conStr.append("Ch" + i + ": " + connected.get(i));
                    }
                }
                player.dropMessage(6, conStr.toString());
            } catch (RemoteException e) {
                cserva.reconnectWorld();
            }

        }else if (splitted[0].equals("!buffme")) {
            int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002, 5121009, 5221010};
            for (int i : array)
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
        } else if (splitted[0].equalsIgnoreCase("!killmap")) {
            int players = 0;
            for (MapleCharacter mch : c.getPlayer().getMap().getCharacters()) {
                if (mch != null && !mch.isGM()) {
                    mch.kill();
                    players++;
                }
            }
            mc.dropMessage("[Message] " + players + " players have been killed.");
        }
    }

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[]{
				new CommandDefinition("lowhp", "", "", 4),
				new CommandDefinition("fullhp", "", "", 4),
				new CommandDefinition("skill", "", "", 4),
				new CommandDefinition("sp", "", "", 4),
				new CommandDefinition("job", "", "", 4),
				new CommandDefinition("whereami", "", "", 4),
				new CommandDefinition("shop", "", "", 4),
				new CommandDefinition("gainmeso", "", "", 4),
				new CommandDefinition("levelup", "", "", 4),
				new CommandDefinition("item", "", "", 4),
				new CommandDefinition("drop", "", "", 4),
				new CommandDefinition("maxlevel", "", "", 4),
				new CommandDefinition("online", "", "",4),
				new CommandDefinition("ring", "", "", 4),
                                new CommandDefinition("closemerchants", "", "", 4),
				new CommandDefinition("saveall", "", "Saves all chars. Please use it wisely, quite expensive command.", 4),
				new CommandDefinition("ariantpq", "", "", 4),
				new CommandDefinition("event", "", "", 4),
                                new CommandDefinition("votepoints", "", "", 4),
                                new CommandDefinition("leaderpoints", "", "", 4),
                                new CommandDefinition("betapoints", "", "", 4),
                                new CommandDefinition("pqpoints", "", "", 4),
                                new CommandDefinition("jqpoints", "", "", 4),
                                new CommandDefinition("givepresent", "", "", 4),
                                new CommandDefinition("conectados", "", "", 4),
                                new CommandDefinition("giftnx", "", "", 4),
                                new CommandDefinition("bomb", "", "", 4),
                                new CommandDefinition("seduce", "", "", 4),
                                new CommandDefinition("seducemap", "", "", 4),
                                new CommandDefinition("speakall", "", "", 4),
                                new CommandDefinition("clearinv", "", "", 4),
                                new CommandDefinition("killmap", "", "", 4),
                                new CommandDefinition("buffme", "", "", 4),
                                new CommandDefinition("doublecash", "", "", 4),
                                new CommandDefinition("checarstats", "", "", 4),
                                new CommandDefinition("reloadmapspawns", "", "", 4),
		};
	}
}

