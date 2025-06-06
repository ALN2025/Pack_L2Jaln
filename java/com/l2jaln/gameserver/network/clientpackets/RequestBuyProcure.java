package com.l2jaln.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jaln.Config;
import com.l2jaln.gameserver.model.holder.IntIntHolder;

public class RequestBuyProcure extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 8;
	
	private int _manorId;
	private List<IntIntHolder> _items;
	
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		
		final int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
			return;
		
		_items = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
		{
			readD(); // service
			final int itemId = readD();
			final int cnt = readD();
			
			if (itemId < 1 || cnt < 1)
			{
				_items = null;
				return;
			}
			
			_items.add(new IntIntHolder(itemId, cnt));
		}
	}
	
	@Override
	protected void runImpl()
	{
		_log.info("RequestBuyProcure: normally unused, but infos found for manorId :" + _manorId);
	}
}