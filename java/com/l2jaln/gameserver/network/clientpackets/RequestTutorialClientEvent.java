package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.QuestState;

public class RequestTutorialClientEvent extends L2GameClientPacket
{
	int eventId;
	
	@Override
	protected void readImpl()
	{
		eventId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		QuestState qs = player.getQuestState("Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("CE" + eventId + "", null, player);
	}
}