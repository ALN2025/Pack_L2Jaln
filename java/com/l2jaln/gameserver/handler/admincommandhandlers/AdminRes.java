package com.l2jaln.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;

import com.l2jaln.Config;
import com.l2jaln.gameserver.handler.IAdminCommandHandler;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.taskmanager.DecayTaskManager;

/**
 * This class handles following admin commands:<br>
 * - res = resurrects a player<br>
 * - res_monster = resurrects a Npc/Monster/...
 */
public class AdminRes implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminRes.class.getName());
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_res",
		"admin_res_monster"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_res "))
			handleRes(activeChar, command.split(" ")[1]);
		else if (command.equals("admin_res"))
			handleRes(activeChar);
		else if (command.startsWith("admin_res_monster "))
			handleNonPlayerRes(activeChar, command.split(" ")[1]);
		else if (command.equals("admin_res_monster"))
			handleNonPlayerRes(activeChar);
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void handleRes(Player activeChar)
	{
		handleRes(activeChar, null);
	}
	
	private static void handleRes(Player activeChar, String resParam)
	{
		WorldObject obj = activeChar.getTarget();
		
		if (resParam != null)
		{
			// Check if a player name was specified as a param.
			Player plyr = World.getInstance().getPlayer(resParam);
			
			if (plyr != null)
				obj = plyr;
			else
			{
				// Otherwise, check if the param was a radius.
				try
				{
					int radius = Integer.parseInt(resParam);
					
					for (Player knownPlayer : activeChar.getKnownTypeInRadius(Player.class, radius))
						doResurrect(knownPlayer);
					
					activeChar.sendMessage("Resurrected all players within a " + radius + " unit radius.");
					return;
				}
				catch (NumberFormatException e)
				{
					activeChar.sendMessage("Enter a valid player name or radius.");
					return;
				}
			}
		}
		
		if (obj == null)
			obj = activeChar;
		
		doResurrect((Creature) obj);
		
		if (Config.DEBUG)
			_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") resurrected character " + obj.getObjectId());
	}
	
	private static void handleNonPlayerRes(Player activeChar)
	{
		handleNonPlayerRes(activeChar, "");
	}
	
	private static void handleNonPlayerRes(Player activeChar, String radiusStr)
	{
		WorldObject obj = activeChar.getTarget();
		
		try
		{
			int radius = 0;
			
			if (!radiusStr.isEmpty())
			{
				radius = Integer.parseInt(radiusStr);
				
				for (Creature knownChar : activeChar.getKnownTypeInRadius(Creature.class, radius))
					if (!(knownChar instanceof Player))
						doResurrect(knownChar);
				
				activeChar.sendMessage("Resurrected all non-players within a " + radius + " unit radius.");
			}
		}
		catch (NumberFormatException e)
		{
			activeChar.sendMessage("Enter a valid radius.");
			return;
		}
		
		if (obj instanceof Player)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		doResurrect((Creature) obj);
	}
	
	private static void doResurrect(Creature targetChar)
	{
		if (!targetChar.isDead())
			return;
		
		// If the target is a player, then restore the XP lost on death.
		if (targetChar instanceof Player)
			((Player) targetChar).restoreExp(100.0);
		// If the target is an NPC, then abort it's auto decay and respawn.
		else
			DecayTaskManager.getInstance().cancel(targetChar);
		
		targetChar.doRevive();
	}
}