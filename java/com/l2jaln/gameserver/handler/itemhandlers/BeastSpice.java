package com.l2jaln.gameserver.handler.itemhandlers;

import com.l2jaln.gameserver.data.SkillTable;
import com.l2jaln.gameserver.handler.IItemHandler;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.actor.Playable;
import com.l2jaln.gameserver.model.actor.instance.FeedableBeast;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.item.instance.ItemInstance;
import com.l2jaln.gameserver.network.SystemMessageId;

public class BeastSpice implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		Player activeChar = (Player) playable;
		
		if (!(activeChar.getTarget() instanceof FeedableBeast))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		int skillId = 0;
		switch (item.getItemId())
		{
			case 6643:
				skillId = 2188;
				break;
			case 6644:
				skillId = 2189;
				break;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
		if (skill != null)
			activeChar.useMagic(skill, false, false);
	}
}