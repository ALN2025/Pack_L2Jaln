package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.Config;
import com.l2jaln.gameserver.instancemanager.FishingChampionshipManager;
import com.l2jaln.gameserver.model.actor.instance.Player;

/**
 * Format: (ch)
 * @author -Wooden-
 */
public final class RequestExFishRanking extends L2GameClientPacket
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
		
		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionshipManager.getInstance().showMidResult(activeChar);
	}
}