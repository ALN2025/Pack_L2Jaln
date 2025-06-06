package com.l2jaln.commons.geometry;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.gameserver.model.location.Location;

/**
 * @author Hasha
 */
public class Sphere extends Circle
{
	// sphere center Z coordinate
	private final int _z;
	
	/**
	 * Sphere constructor.
	 * @param x : Center X coordinate.
	 * @param y : Center Y coordinate.
	 * @param z : Center Z coordinate.
	 * @param r : Sphere radius.
	 */
	public Sphere(int x, int y, int z, int r)
	{
		super(x, y, r);
		
		_z = z;
	}
	
	@Override
	public final double getArea()
	{
		return 4 * Math.PI * _r * _r;
	}
	
	@Override
	public final double getVolume()
	{
		return (4 * Math.PI * _r * _r * _r) / 3;
	}
	
	@Override
	public final boolean isInside(int x, int y, int z)
	{
		final int dx = x - _x;
		final int dy = y - _y;
		final int dz = z - _z;
		
		return (dx * dx + dy * dy + dz * dz) <= _r * _r;
	}
	
	@Override
	public final Location getRandomLocation()
	{
		// get uniform distance and angles
		final double r = Math.cbrt(Rnd.nextDouble()) * _r;
		final double phi = Rnd.nextDouble() * 2 * Math.PI;
		final double theta = Math.acos(2 * Rnd.nextDouble() - 1);
		
		// calculate coordinates
		final int x = (int) (_x + (r * Math.cos(phi) * Math.sin(theta)));
		final int y = (int) (_y + (r * Math.sin(phi) * Math.sin(theta)));
		final int z = (int) (_z + (r * Math.cos(theta)));
		
		// return
		return new Location(x, y, z);
	}
}