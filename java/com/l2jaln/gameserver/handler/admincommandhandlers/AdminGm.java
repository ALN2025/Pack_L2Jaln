package com.l2jaln.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jaln.commons.concurrent.ThreadPool;

import com.l2jaln.gameserver.handler.IAdminCommandHandler;
import com.l2jaln.gameserver.model.actor.instance.Player;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>gm = turns gm mode off for a short period of time (by default 1 minute).</li>
 * </ul>
 */
public class AdminGm implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gm"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_gm"))
		{
			if (activeChar.isGM())
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				
				int numberOfMinutes = 1;
				if (st.hasMoreTokens())
				{
					try
					{
						numberOfMinutes = Integer.parseInt(st.nextToken());
					}
					catch (Exception e)
					{
						activeChar.sendMessage("Invalid timer setted for //gm ; default time is used.");
					}
				}
				
				// We keep the previous level to rehabilitate it later.
				final int previousAccessLevel = activeChar.getAccessLevel().getLevel();
				
				activeChar.setAccessLevel(0);
				activeChar.sendMessage("You no longer have GM status, but will be rehabilitated after " + numberOfMinutes + " minutes.");
				
				ThreadPool.schedule(new GiveBackAccess(activeChar, previousAccessLevel), numberOfMinutes * 60000);
			}
		}
		return true;
	}
	
	private class GiveBackAccess implements Runnable
	{
		private final Player _activeChar;
		private final int _previousAccessLevel;
		
		public GiveBackAccess(Player activeChar, int previousAccessLevel)
		{
			_activeChar = activeChar;
			_previousAccessLevel = previousAccessLevel;
		}
		
		@Override
		public void run()
		{
			if (!_activeChar.isOnline())
				return;
			
			_activeChar.setAccessLevel(_previousAccessLevel);
			_activeChar.sendMessage("Your previous access level has been rehabilitated.");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}