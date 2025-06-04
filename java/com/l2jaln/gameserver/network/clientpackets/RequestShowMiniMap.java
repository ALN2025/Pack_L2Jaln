package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.serverpackets.ShowMiniMap;

public final class RequestShowMiniMap extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected final void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(ShowMiniMap.REGULAR_MAP);
	}
}