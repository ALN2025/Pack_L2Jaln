package com.l2jaln.gameserver.handler.itemhandlers;

import com.l2jaln.gameserver.handler.IItemHandler;
import com.l2jaln.gameserver.model.actor.Playable;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.item.instance.ItemInstance;
import com.l2jaln.gameserver.network.serverpackets.ActionFailed;
import com.l2jaln.gameserver.network.serverpackets.NpcHtmlMessage;

public class Book implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		final int itemId = item.getItemId();
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/help/" + itemId + ".htm");
		html.setItemId(itemId);
		activeChar.sendPacket(html);
		
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
}