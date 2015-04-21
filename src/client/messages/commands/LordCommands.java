/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * This file is part of the "Renoria" Game.
 * Copyright (C) 2008
 * IDGames.
 */

package client.messages.commands;

import client.MapleClient;
import client.messages.Command;
import client.messages.CommandDefinition;
import client.messages.IllegalCommandSyntaxException;
import client.messages.MessageCallback;

/**
 *
 * @author David
 */
public class LordCommands implements Command {
	public void execute(MapleClient c, MessageCallback mc, String[] splittedLine) throws Exception, IllegalCommandSyntaxException {
	
	}

	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			
		};
	}

}
