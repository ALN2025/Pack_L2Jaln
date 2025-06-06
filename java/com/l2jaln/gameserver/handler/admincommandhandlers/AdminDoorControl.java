package com.l2jaln.gameserver.handler.admincommandhandlers;

import com.l2jaln.gameserver.data.DoorTable;
import com.l2jaln.gameserver.handler.IAdminCommandHandler;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.instance.Door;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;

/**
 * This class handles following admin commands
 * <ul>
 * <li>open = open a door using a doorId, or a targeted door if not found.</li>
 * <li>close = close a door using a doorId, or a targeted door if not found.</li>
 * <li>openall = open all doors registered on doors.xml.</li>
 * <li>closeall = close all doors registered on doors.xml.</li>
 * </ul>
 */
public class AdminDoorControl implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_open",
		"admin_close",
		"admin_openall",
		"admin_closeall"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_open"))
		{
			if (command.equals("admin_openall"))
			{
				for (Door door : DoorTable.getInstance().getDoors())
					door.openMe();
			}
			else
			{
				try
				{
					final Door door = DoorTable.getInstance().getDoor(Integer.parseInt(command.substring(11)));
					if (door != null)
						door.openMe();
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				catch (Exception e)
				{
					final WorldObject target = activeChar.getTarget();
					
					if (target instanceof Door)
						((Door) target).openMe();
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
		}
		else if (command.startsWith("admin_close"))
		{
			if (command.equals("admin_closeall"))
			{
				for (Door door : DoorTable.getInstance().getDoors())
					door.closeMe();
			}
			else
			{
				try
				{
					final Door door = DoorTable.getInstance().getDoor(Integer.parseInt(command.substring(12)));
					if (door != null)
						door.closeMe();
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				catch (Exception e)
				{
					final WorldObject target = activeChar.getTarget();
					
					if (target instanceof Door)
						((Door) target).closeMe();
					else
						activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}