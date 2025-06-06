/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jaln.gameserver.handler.itemhandlers.custom;

import com.l2jaln.gameserver.handler.IItemHandler;
import com.l2jaln.gameserver.model.actor.Playable;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.base.Experience;
import com.l2jaln.gameserver.model.item.instance.ItemInstance;
import com.l2jaln.gameserver.network.serverpackets.ExShowScreenMessage;

public class LevelCoin implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		
		if (activeChar.isOlympiadProtection())
		{
			activeChar.sendMessage("You can not do that.");
			return;
		}
		long pXp = activeChar.getExp();
		long tXp = Experience.LEVEL[81];
		
		playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		activeChar.addExpAndSp(tXp - pXp, 0);
		activeChar.sendPacket(new ExShowScreenMessage("Congratulations. You become level 81..", 6000, 0x02, true));
	
	}
}
