package com.l2jaln.gameserver.network.serverpackets;

import com.l2jaln.gameserver.model.tradelist.TradeItem;

/**
 * @author Yme
 */
public class TradeOwnAdd extends L2GameServerPacket
{
	private final TradeItem _item;
	
	public TradeOwnAdd(TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x20);
		
		writeH(1); // item count
		
		writeH(_item.getItem().getType1()); // item type1
		writeD(_item.getObjectId());
		writeD(_item.getItem().getItemId());
		writeD(_item.getCount());
		writeH(_item.getItem().getType2()); // item type2
		writeH(0x00); // ?
		
		writeD(_item.getItem().getBodyPart()); // slot
		writeH(_item.getEnchant()); // enchant level
		writeH(0x00); // ?
		writeH(0x00);
	}
}