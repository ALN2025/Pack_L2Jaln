package com.l2jaln.gameserver.handler.usercommandhandlers;

import com.l2jaln.gameserver.handler.IUserCommandHandler;
import com.l2jaln.gameserver.model.L2CommandChannel;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;

public class ChannelListUpdate implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		97
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (!activeChar.isInParty())
			return false;
		
		final L2CommandChannel channel = activeChar.getParty().getCommandChannel();
		if (channel == null)
			return false;
		
		activeChar.sendPacket(new ExMultiPartyCommandChannelInfo(channel));
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}