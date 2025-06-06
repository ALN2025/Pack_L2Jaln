package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.instancemanager.CastleManager;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.entity.Castle;
import com.l2jaln.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestSiegeDefenderList extends L2GameClientPacket
{
	private int _castleId;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null)
			return;
		
		sendPacket(new SiegeDefenderList(castle));
	}
}