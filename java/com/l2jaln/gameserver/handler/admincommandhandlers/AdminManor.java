package com.l2jaln.gameserver.handler.admincommandhandlers;

import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.gameserver.handler.IAdminCommandHandler;
import com.l2jaln.gameserver.instancemanager.CastleManager;
import com.l2jaln.gameserver.instancemanager.CastleManorManager;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.entity.Castle;
import com.l2jaln.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminManor implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_manor"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_manor"))
		{
			final CastleManorManager manor = CastleManorManager.getInstance();
			
			final NpcHtmlMessage msg = new NpcHtmlMessage(0);
			msg.setFile("data/html/admin/manor.htm");
			msg.replace("%status%", manor.getCurrentModeName());
			msg.replace("%change%", manor.getNextModeChange());
			
			final StringBuilder sb = new StringBuilder(3400);
			for (Castle c : CastleManager.getInstance().getCastles())
			{
				StringUtil.append(sb, "<tr><td width=110>Name:</td><td width=160><font color=008000>" + c.getName() + "</font></td></tr>");
				StringUtil.append(sb, "<tr><td>Current period cost:</td><td><font color=FF9900>", StringUtil.formatNumber(manor.getManorCost(c.getCastleId(), false)), " Adena</font></td></tr>");
				StringUtil.append(sb, "<tr><td>Next period cost:</td><td><font color=FF9900>", StringUtil.formatNumber(manor.getManorCost(c.getCastleId(), true)), " Adena</font></td></tr>");
				StringUtil.append(sb, "<tr><td>&nbsp;</td></tr>");
			}
			msg.replace("%castleInfo%", sb.toString());
			activeChar.sendPacket(msg);
			
			sb.setLength(0);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}