package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.serverpackets.UserInfo;

public final class Appearing extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.isTeleporting())
			activeChar.onTeleported();
		
		sendPacket(new UserInfo(activeChar));
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}