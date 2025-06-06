package com.l2jaln.gameserver.handler.usercommandhandlers;

import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.gameserver.handler.IUserCommandHandler;
import com.l2jaln.gameserver.instancemanager.CastleManager;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.entity.Castle;
import com.l2jaln.gameserver.model.entity.Siege.SiegeSide;
import com.l2jaln.gameserver.model.pledge.Clan;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.NpcHtmlMessage;

public class SiegeStatus implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		99
	};
	
	private static final String IN_PROGRESS = "Castle Siege in Progress";
	private static final String OUTSIDE_ZONE = "Outside Castle Siege Zone";
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (!activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS);
			return false;
		}
		
		if (!activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW);
			return false;
		}
		
		final Clan clan = activeChar.getClan();
		
		final StringBuilder sb = new StringBuilder();
		
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			// Search on lists : as a clan can only be registered in a single siege, break after one case is found.
			if (!castle.getSiege().isInProgress() || !castle.getSiege().checkSides(clan, SiegeSide.ATTACKER, SiegeSide.DEFENDER, SiegeSide.OWNER))
				continue;
			
			for (Player member : clan.getOnlineMembers())
				StringUtil.append(sb, "<tr><td width=170>", member.getName(), "</td><td width=100>", (castle.getSiegeZone().isInsideZone(member)) ? IN_PROGRESS : OUTSIDE_ZONE, "</td></tr>");
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/siege_status.htm");
			html.replace("%kills%", clan.getSiegeKills());
			html.replace("%deaths%", clan.getSiegeDeaths());
			html.replace("%content%", sb.toString());
			activeChar.sendPacket(html);
			return true;
		}
		
		activeChar.sendPacket(SystemMessageId.ONLY_DURING_SIEGE);
		return false;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}