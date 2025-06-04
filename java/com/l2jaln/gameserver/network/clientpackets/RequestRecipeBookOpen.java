package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.data.RecipeTable;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;

public final class RequestRecipeBookOpen extends L2GameClientPacket
{
	private boolean _isDwarvenCraft;
	
	@Override
	protected void readImpl()
	{
		_isDwarvenCraft = (readD() == 0);
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.isCastingNow() || activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(SystemMessageId.NO_RECIPE_BOOK_WHILE_CASTING);
			return;
		}
		
		RecipeTable.getInstance().requestBookOpen(activeChar, _isDwarvenCraft);
	}
}