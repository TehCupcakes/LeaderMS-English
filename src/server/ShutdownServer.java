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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.sql.SQLException;

import database.DatabaseConnection;
import net.channel.ChannelServer;

/**
 *
 * @author Frz
 */

public class ShutdownServer implements Runnable {

    private int channel;

    public ShutdownServer(int channel) {
	this.channel = channel;
    }
    
   @Override
  public void run() {
	try {
	    ChannelServer.getInstance(channel).shutdown();
	} catch (Throwable t) {
	    System.err.println("SHUTDOWN ERROR" + t);
	}

	while (ChannelServer.getInstance(channel).getPlayerStorage().getConnectedClients() > 0) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		System.err.println("ERROR" + e);
	    }
	}

	System.out.println("Channel " + channel + ", Deregistering channel");

	try {
	    ChannelServer.getWorldRegistry().deregisterChannelServer(channel);
	} catch (Exception e) {
	    // we are shutting down
	}

	System.out.println("Channel " + channel + ", Unbinding ports...");

	boolean error = true;
	while (error) {
	    try {
		ChannelServer.getInstance(channel).unbind();
		error = false;
	    } catch (Exception e) {
		error = true;
	    }
	}

	System.out.println("Channel " + channel + ", closing...");

	for (ChannelServer cserv : ChannelServer.getAllInstances()) {
	    while (!cserv.hasFinishedShutdown()) {
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    System.err.println("ERROR" + e);
		}
	    }
	}
	TimerManager.getInstance().stop();
	try {
	    DatabaseConnection.closeAll();
	} catch (SQLException e) {
	    System.err.println("THROW" + e);
	}
	System.exit(0);
    }
}
