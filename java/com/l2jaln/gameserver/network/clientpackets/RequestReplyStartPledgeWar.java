package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.data.sql.ClanTable;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;

public final class RequestReplyStartPledgeWar extends L2GameClientPacket
{
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final Player requestor = activeChar.getActiveRequester();
		if (requestor == null)
			return;
		
		if (_answer == 1)
			ClanTable.getInstance().storeClansWars(requestor.getClanId(), activeChar.getClanId());
		else
			requestor.sendPacket(SystemMessageId.WAR_PROCLAMATION_HAS_BEEN_REFUSED);
		
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}