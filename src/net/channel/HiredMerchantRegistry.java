/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.channel;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import client.MapleCharacter;
import server.PlayerInteraction.HiredMerchant;
/**
 *
 * @author Simon
 */
public class HiredMerchantRegistry {
    private LinkedHashMap<Integer, HiredMerchant> registry;
    private LinkedHashMap<String, Integer> idLookup;
    private ReentrantReadWriteLock merchantLock = new ReentrantReadWriteLock();
    private int channelNum = -1;

    public HiredMerchantRegistry(int channel)
    {
        registry = new LinkedHashMap<Integer, HiredMerchant>();
        idLookup = new LinkedHashMap<String, Integer>();
        channelNum = -1;
    }

    public void registerMerchant(HiredMerchant h, MapleCharacter c)
    {
        merchantLock.writeLock().lock();
        try
        {
        idLookup.put(c.getName(), c.getId());
        registry.put(c.getId(), h);
        } finally {
            merchantLock.writeLock().unlock();
        }
    }

    public void deregisterMerchant(HiredMerchant h)
    {
        merchantLock.writeLock().lock();
        try
        {
            if(registry.containsValue(h))
            {
                idLookup.remove(h.getOwner());
                registry.remove(h.getOwnerId());
            }
        } finally {
            merchantLock.writeLock().unlock();
        }
    }

    public HiredMerchant getMerchantForPlayer(String playerName)
    {
        merchantLock.readLock().lock();
        try
        {
            if(idLookup.containsKey(playerName))
            {
                if(registry.containsKey(idLookup.get(playerName)))
                    return registry.get(idLookup.get(playerName));
            }
            return null;
        } finally {
            merchantLock.readLock().unlock();
        }
    }

    public HiredMerchant getMerchantForPlayer(int playerId)
    {
        merchantLock.readLock().lock();
        try
        {
            if(registry.containsKey(playerId))
                return registry.get(playerId);
            return null;
        } finally {
            merchantLock.readLock().unlock();
        }
    }

    public void closeAndDeregisterAll()
    {
        merchantLock.writeLock().lock();
        try
        {
            for(HiredMerchant h : registry.values())
            {
                    h.closeShop(true);
            }
            registry.clear();
        } finally
        {
            merchantLock.writeLock().unlock();
        }
    }

}
