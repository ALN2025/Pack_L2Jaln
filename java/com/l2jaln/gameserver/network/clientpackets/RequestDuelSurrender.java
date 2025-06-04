package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.instancemanager.DuelManager;

/**
 * Format:(ch)
 * @author -Wooden-
 */
public final class RequestDuelSurrender extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		DuelManager.getInstance().doSurrender(getClient().getActiveChar());
	}
}