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

import java.util.regex.Pattern;

public class MapleCharacterUtil {
	private static Pattern namePattern = Pattern.compile("[a-zA-Z0-9_-]{3,12}");
	
	private MapleCharacterUtil() {
		// whoosh
	}
	
	 public static boolean canCreateChar(String name, int world) {
        if (name.length() < 4 || name.length() > 12)
            return false;
        if (java.util.regex.Pattern.compile("[a-zA-Z0-9_-]{3,12}").matcher(name).matches())
            return MapleCharacter.getIdByName(name, world) < 0 && !name.toLowerCase().contains("gm");
         return false;
       }
        
       public static boolean hasSymbols(String name) {
        String[] symbols = {"`","~","!","@","#","$","%","^","&","*","(",")","_","-","=","+","{","[","]","}","|",";",":","'",",","<",">",".","?","/"};
        for (byte s = 0; s < symbols.length; s++) {
            if (name.contains(symbols[s])) {
                return true;
            }
        }
        return false;
    }


	public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;
    }
}