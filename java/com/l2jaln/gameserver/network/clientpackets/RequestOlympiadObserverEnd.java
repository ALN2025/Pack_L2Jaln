package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.model.actor.instance.Player;

/**
 * format ch
 * @author -Wooden-
 */
public final class RequestOlympiadObserverEnd extends L2GameClientPacket
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
		
		if (activeChar.isInObserverMode())
			activeChar.leaveOlympiadObserverMode();
	}
}