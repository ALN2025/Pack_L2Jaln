package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.data.xml.AdminData;
import com.l2jaln.gameserver.model.actor.instance.Player;

public final class RequestGmList extends L2GameClientPacket
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
		
		AdminData.getInstance().sendListToPlayer(activeChar);
	}
}