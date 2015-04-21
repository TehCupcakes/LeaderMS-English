/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.channel;

import java.util.HashMap;
import java.util.Map;
import client.MapleCharacter;
import client.MaplePet;

/**
 *
 * @author Administrator
 */
public class PetStorage {

    // HACK FIX
    private static Map<Integer, MaplePet[]> petz = new HashMap<>();

    public static void savePetz(MapleCharacter c) {
        if (petz.containsKey(c.getId())) {
            petz.remove(c.getId());
        }

        MaplePet[] pet = new MaplePet[3];
        for (int i = 0; i < 3; i++) {
            if (c.getPet(i) != null) {
                pet[i] = c.getPet(i);
            }
        }

        petz.put(c.getId(), pet);
    }

    public static MaplePet[] getPetz(int cid) {
        if (petz.containsKey(cid)) {
            return petz.get(cid);
        }
        return null;
    }
}
