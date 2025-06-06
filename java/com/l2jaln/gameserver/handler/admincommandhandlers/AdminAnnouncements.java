package com.l2jaln.gameserver.handler.admincommandhandlers;

import com.l2jaln.gameserver.data.xml.AnnouncementData;
import com.l2jaln.gameserver.handler.IAdminCommandHandler;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.actor.instance.Player;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>announce list|all|all_auto|add|add_auto|del : announcement management.</li>
 * <li>ann : announces to all players (basic usage).</li>
 * <li>say : critical announces to all players.</li>
 * </ul>
 */
public class AdminAnnouncements implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_announce",
		"admin_ann",
		"admin_say"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (activeChar.getAccessLevel().getLevel() < 7)
			return false;

		if (command.startsWith("admin_announce"))
		{
			try
			{
				final String[] tokens = command.split(" ", 3);
				switch (tokens[1])
				{
					case "list":
						AnnouncementData.getInstance().listAnnouncements(activeChar);
						break;
					
					case "all":
					case "all_auto":
						final boolean isAuto = tokens[1].equalsIgnoreCase("all_auto");
						for (Player player : World.getInstance().getPlayers())
							AnnouncementData.getInstance().showAnnouncements(player, isAuto);
						
						AnnouncementData.getInstance().listAnnouncements(activeChar);
						break;
					
					case "add":
						String[] split = tokens[2].split(" ", 2); // boolean string
						int crit = getAnnounce(split[0]);
						
						if (!AnnouncementData.getInstance().addAnnouncement(split[1], crit, false, -1, -1, -1))
							activeChar.sendMessage("Invalid //announce message content ; can't be null or empty.");
						
						AnnouncementData.getInstance().listAnnouncements(activeChar);
						break;
					
					case "add_auto":
						split = tokens[2].split(" ", 6); // boolean boolean int int int string
						crit = getAnnounce(split[0]);
						final boolean auto = Boolean.parseBoolean(split[1]);
						final int idelay = Integer.parseInt(split[2]);
						final int delay = Integer.parseInt(split[3]);
						final int limit = Integer.parseInt(split[4]);
						final String msg = split[5];
						
						if (!AnnouncementData.getInstance().addAnnouncement(msg, crit, auto, idelay, delay, limit))
							activeChar.sendMessage("Invalid //announce message content ; can't be null or empty.");
						
						AnnouncementData.getInstance().listAnnouncements(activeChar);
						break;
					
					case "del":
						AnnouncementData.getInstance().delAnnouncement(Integer.parseInt(tokens[2]));
						AnnouncementData.getInstance().listAnnouncements(activeChar);
						break;
					
					default:
						activeChar.sendMessage("Possible //announce parameters : <list|all|add|add_auto|del>");
						break;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Possible //announce parameters : <list|all|add|add_auto|del>");
			}
		}
		else if (command.startsWith("admin_ann") || command.startsWith("admin_say"))
			AnnouncementData.getInstance().handleAnnounce(command, 10, 0);
		
		return true;
	}
	
	private static int getAnnounce(String name)
	{
		if (name.equalsIgnoreCase("ANNOUNCEMENT"))
			return 0;
		if (name.equalsIgnoreCase("CRITICAL_ANN"))
			return 1;
		if (name.equalsIgnoreCase("SHOUT"))
			return 2;
		if (name.equalsIgnoreCase("TRADE"))
			return 3;
		if (name.equalsIgnoreCase("HERO_VOICE"))
			return 4;
		if (name.equalsIgnoreCase("TELL"))
			return 5;
		if (name.equalsIgnoreCase("PARTY"))
			return 6;
		if (name.equalsIgnoreCase("CLAN"))
			return 7;
		if (name.equalsIgnoreCase("ALLIANCE"))
			return 8;
		
		return 0;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}