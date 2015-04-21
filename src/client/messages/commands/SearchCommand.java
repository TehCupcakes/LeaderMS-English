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

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.io.File;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;
import server.MapleItemInformationProvider;
import tools.Pair;
import tools.StringUtil;

import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

public class SearchCommand implements Command {

    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

    /*
     * @Author	Snow(Raz)
     * @Notes	Convient ID Finder
     */
    @Override
    public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {
	if (splitted.length == 1) {
	    mc.dropMessage(splitted[0] + ": <NPC> <MOB> <ITEM> <MAP> <SKILL>");
	} else {
	    
	    String type = splitted[1];
	    String search = StringUtil.joinStringFrom(splitted, 2);
	    MapleData data = null;
	    MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz"));
	    mc.dropMessage("<<Type: " + type + " | Search: " + search + ">>");
	    if (type.equalsIgnoreCase("NPC") || type.equalsIgnoreCase("NPCS")) {
		List<String> retNpcs = new ArrayList<String>();
		data = dataProvider.getData("Npc.img");
		List<Pair<Integer, String>> npcPairList = new LinkedList<Pair<Integer, String>>();
		for (MapleData npcIdData : data.getChildren()) {
		    int npcIdFromData = Integer.parseInt(npcIdData.getName());
		    String npcNameFromData = MapleDataTool.getString(npcIdData.getChildByPath("name"), "NO-NAME");
		    npcPairList.add(new Pair<Integer, String>(npcIdFromData, npcNameFromData));
		}
		for (Pair<Integer, String> npcPair : npcPairList) {
		    if (npcPair.getRight().toLowerCase().contains(search.toLowerCase())) {
			retNpcs.add(npcPair.getLeft() + " - " + npcPair.getRight());
		    }
		}
		if (retNpcs != null && retNpcs.size() > 0) {
		    for (String singleRetNpc : retNpcs) {
			mc.dropMessage(singleRetNpc);
		    }
		} else {
		    mc.dropMessage("No NPC's Found");
		}

	    } else if (type.equalsIgnoreCase("MAP") || type.equalsIgnoreCase("MAPS")) {
		List<String> retMaps = new ArrayList<String>();
		data = dataProvider.getData("Map.img");
		List<Pair<Integer, String>> mapPairList = new LinkedList<Pair<Integer, String>>();
		for (MapleData mapAreaData : data.getChildren()) {
		    for (MapleData mapIdData : mapAreaData.getChildren()) {
			int mapIdFromData = Integer.parseInt(mapIdData.getName());
			String mapNameFromData = MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "NO-NAME");
			mapPairList.add(new Pair<Integer, String>(mapIdFromData, mapNameFromData));
		    }
		}
		for (Pair<Integer, String> mapPair : mapPairList) {
		    if (mapPair.getRight().toLowerCase().contains(search.toLowerCase())) {
			retMaps.add(mapPair.getLeft() + " - " + mapPair.getRight());
		    }
		}
		if (retMaps != null && retMaps.size() > 0) {
		    for (String singleRetMap : retMaps) {
			mc.dropMessage(singleRetMap);
		    }
		} else {
		    mc.dropMessage("No Maps Found");
		}

	    } else if (type.equalsIgnoreCase("MOB") || type.equalsIgnoreCase("MOBS") || type.equalsIgnoreCase("MONSTER") || type.equalsIgnoreCase("MONSTERS")) {
		List<String> retMobs = new ArrayList<String>();
		data = dataProvider.getData("Mob.img");
		List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
		for (MapleData mobIdData : data.getChildren()) {
		    int mobIdFromData = Integer.parseInt(mobIdData.getName());
		    String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
		    mobPairList.add(new Pair<Integer, String>(mobIdFromData, mobNameFromData));
		}
		for (Pair<Integer, String> mobPair : mobPairList) {
		    if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
			retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
		    }
		}
		if (retMobs != null && retMobs.size() > 0) {
		    for (String singleRetMob : retMobs) {
			mc.dropMessage(singleRetMob);
		    }
		} else {
		    mc.dropMessage("No Mob's Found");
		}

	    } else if (type.equalsIgnoreCase("REACTOR") || type.equalsIgnoreCase("REACTORS")) {
		mc.dropMessage("NOT ADDED YET");

	    } else if (type.equalsIgnoreCase("ITEM") || type.equalsIgnoreCase("ITEMS")) {
		List<String> retItems = new ArrayList<String>();
		for (Pair<Integer, String> itemPair : ii.getAllItems()) {
		    if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
			retItems.add(itemPair.getLeft() + " - " + itemPair.getRight());
		    }
		}
		if (retItems != null && retItems.size() > 0) {
		    for (String singleRetItem : retItems) {
			mc.dropMessage(singleRetItem);
		    }
		} else {
		    mc.dropMessage("No Item's Found");
		}

	    } else if (type.equalsIgnoreCase("SKILL") || type.equalsIgnoreCase("SKILLS")) {
		List<String> retSkills = new ArrayList<String>();
		data = dataProvider.getData("Skill.img");
		List<Pair<Integer, String>> skillPairList = new LinkedList<Pair<Integer, String>>();
		for (MapleData skillIdData : data.getChildren()) {
		    int skillIdFromData = Integer.parseInt(skillIdData.getName());
		    String skillNameFromData = MapleDataTool.getString(skillIdData.getChildByPath("name"), "NO-NAME");
		    skillPairList.add(new Pair<Integer, String>(skillIdFromData, skillNameFromData));
		}
		for (Pair<Integer, String> skillPair : skillPairList) {
		    if (skillPair.getRight().toLowerCase().contains(search.toLowerCase())) {
			retSkills.add(skillPair.getLeft() + " - " + skillPair.getRight());
		    }
		}
		if (retSkills != null && retSkills.size() > 0) {
		    for (String singleRetSkill : retSkills) {
			mc.dropMessage(singleRetSkill);
		    }
		} else {
		    mc.dropMessage("No Skills Found");
		}
            } else {
              mc.dropMessage("Sorry, that search call is unavailable");
            }
	}
    }

    @Override
    public CommandDefinition[] getDefinition() {
	return new CommandDefinition[]{
	    new CommandDefinition("find", "", "", 1),
	    new CommandDefinition("lookup", "", "", 1),
	    new CommandDefinition("search", "", "", 1),
	    };
    }
}