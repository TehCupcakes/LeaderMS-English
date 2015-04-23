/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.channel.handler;

import java.rmi.RemoteException;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.FilePrinter;

/**
 *
 * @author Ido
 */
public class DisconnectHandler extends AbstractMaplePacketHandler {
    private static Logger log = LoggerFactory.getLogger(DisconnectHandler.class);
    
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            c.getPlayer().getClient().getChannelServer().getWorldInterface().broadcastGMMessage("", MaplePacketCreator.serverNotice(5, c.getPlayer().getName() + " is using item vac.").getBytes());
             FilePrinter.printHackerItemVac(c.getPlayer().getName() + ".rtf", "The player is using item vac.");
        } catch (RemoteException ex) {
            c.getPlayer().getClient().getChannelServer().reconnectWorld();
        }
        log.warn(c.getPlayer().getName() + " has been disconnected!");
        c.disconnect();
    }
}