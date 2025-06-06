package com.l2jaln.gameserver.model.location;

import com.l2jaln.gameserver.templates.StatsSet;

/**
 * A datatype extending {@link Location}, used to retain a single Gatekeeper teleport location.
 */
public class TeleportLocation extends Location
{
	public TeleportLocation(StatsSet set)
	{
		super(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
		
		_price = set.getInteger("price");
		_isNoble = set.getBool("isNoble");
	}
	
	private final int _price;
	private final boolean _isNoble;
	
	public int getPrice()
	{
		return _price;
	}
	
	public boolean isNoble()
	{
		return _isNoble;
	}
}