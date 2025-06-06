package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.item.instance.ItemInstance;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.ExConfirmVariationGemstone;

/**
 * Format:(ch) dddd
 * @author -Wooden-
 */
public final class RequestConfirmGemStone extends AbstractRefinePacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private int _gemStoneCount;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemStoneCount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		if (targetItem == null)
			return;
		
		final ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if (refinerItem == null)
			return;
		
		final ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);
		if (gemStoneItem == null)
			return;
		
		// Make sure the item is a gemstone
		if (!isValid(activeChar, targetItem, refinerItem, gemStoneItem))
		{
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		
		// Check for gemstone count
		final LifeStone ls = getLifeStone(refinerItem.getItemId());
		if (ls == null)
			return;
		
		if (_gemStoneCount != getGemStoneCount(targetItem.getItem().getCrystalType()))
		{
			activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
			return;
		}
		
		activeChar.sendPacket(new ExConfirmVariationGemstone(_gemstoneItemObjId, _gemStoneCount));
	}
}