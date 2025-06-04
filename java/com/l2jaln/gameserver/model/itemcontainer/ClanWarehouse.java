package com.l2jaln.gameserver.model.itemcontainer;

import com.l2jaln.Config;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.item.instance.ItemInstance.ItemLocation;
import com.l2jaln.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends ItemContainer
{
	private final Clan _clan;
	
	public ClanWarehouse(Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	public String getName()
	{
		return "ClanWarehouse";
	}
	
	@Override
	public int getOwnerId()
	{
		return _clan.getClanId();
	}
	
	@Override
	public Player getOwner()
	{
		return _clan.getLeader().getPlayerInstance();
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.CLANWH;
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return _items.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN;
	}
}