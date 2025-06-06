package com.l2jaln.gameserver.geoengine.geodata;

import com.l2jaln.gameserver.geoengine.GeoEngine;
import com.l2jaln.gameserver.model.location.Location;

/**
 * @author Hasha
 */
public class GeoLocation extends Location
{
	private byte _nswe;
	
	public GeoLocation(int x, int y, int z)
	{
		super(x, y, GeoEngine.getInstance().getHeightNearest(x, y, z));
		_nswe = GeoEngine.getInstance().getNsweNearest(x, y, z);
	}
	
	public void set(int x, int y, short z)
	{
		super.set(x, y, GeoEngine.getInstance().getHeightNearest(x, y, z));
		_nswe = GeoEngine.getInstance().getNsweNearest(x, y, z);
	}
	
	public int getGeoX()
	{
		return _x;
	}
	
	public int getGeoY()
	{
		return _y;
	}
	
	@Override
	public int getX()
	{
		return GeoEngine.getWorldX(_x);
	}
	
	@Override
	public int getY()
	{
		return GeoEngine.getWorldY(_y);
	}
	
	public byte getNSWE()
	{
		return _nswe;
	}
}