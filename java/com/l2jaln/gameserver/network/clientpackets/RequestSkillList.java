package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.model.actor.instance.Player;

public final class RequestSkillList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player cha = getClient().getActiveChar();
		if (cha == null)
			return;
		
		cha.sendSkillList();
	}
}