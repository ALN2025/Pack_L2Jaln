package com.l2jaln.gameserver.templates;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used in order to have a set of couples (key,value).<BR>
 * Methods deployed are accessors to the set (add/get value from its key) and addition of a whole set in the current one.
 * @author mkizub, G1ta0
 */
@SuppressWarnings("serial")
public class StatsSet extends HashMap<String, Object>
{
	public StatsSet()
	{
		super();
	}
	
	public StatsSet(final int size)
	{
		super(size);
	}
	
	public StatsSet(final StatsSet set)
	{
		super(set);
	}
	
	public void set(final String key, final Object value)
	{
		put(key, value);
	}
	
	public void set(final String key, final String value)
	{
		put(key, value);
	}
	
	public void set(final String key, final boolean value)
	{
		put(key, value ? Boolean.TRUE : Boolean.FALSE);
	}
	
	public void set(final String key, final int value)
	{
		put(key, value);
	}
	
	public void set(final String key, final int[] value)
	{
		put(key, value);
	}
	
	public void set(final String key, final long value)
	{
		put(key, value);
	}
	
	public void set(final String key, final double value)
	{
		put(key, value);
	}
	
	public void set(final String key, final Enum<?> value)
	{
		put(key, value);
	}
	
	public void unset(final String key)
	{
		remove(key);
	}
	
	public boolean getBool(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof Boolean)
			return (Boolean) val;
		if (val instanceof String)
			return Boolean.parseBoolean((String) val);
		if (val instanceof Number)
			return ((Number) val).intValue() != 0;
		
		throw new IllegalArgumentException("StatsSet : Boolean value required, but found: " + val + " for key: " + key + ".");
	}
	
	public boolean getBool(final String key, final boolean defaultValue)
	{
		final Object val = get(key);
		
		if (val instanceof Boolean)
			return (Boolean) val;
		if (val instanceof String)
			return Boolean.parseBoolean((String) val);
		if (val instanceof Number)
			return ((Number) val).intValue() != 0;
		
		return defaultValue;
	}
	
	public byte getByte(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).byteValue();
		if (val instanceof String)
			return Byte.parseByte((String) val);
		
		throw new IllegalArgumentException("StatsSet : Byte value required, but found: " + val + " for key: " + key + ".");
	}
	
	public byte getByte(final String key, final byte defaultValue)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).byteValue();
		if (val instanceof String)
			return Byte.parseByte((String) val);
		
		return defaultValue;
	}
	
	public double getDouble(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (val instanceof String)
			return Double.parseDouble((String) val);
		if (val instanceof Boolean)
			return (Boolean) val ? 1. : 0.;
		
		throw new IllegalArgumentException("StatsSet : Double value required, but found: " + val + " for key: " + key + ".");
	}
	
	public double getDouble(final String key, final double defaultValue)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).doubleValue();
		if (val instanceof String)
			return Double.parseDouble((String) val);
		if (val instanceof Boolean)
			return (Boolean) val ? 1. : 0.;
		
		return defaultValue;
	}
	
	public double[] getDoubleArray(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof double[])
			return (double[]) val;
		if (val instanceof Number)
			return new double[]
			{
				((Number) val).doubleValue()
			};
		if (val instanceof String)
		{
			final String[] vals = ((String) val).split(";");
			
			final double[] result = new double[vals.length];
			
			int i = 0;
			for (final String v : vals)
				result[i++] = Double.parseDouble(v);
			
			return result;
		}
		
		throw new IllegalArgumentException("StatsSet : Double array required, but found: " + val + " for key: " + key + ".");
	}
	
	public float getFloat(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).floatValue();
		if (val instanceof String)
			return Float.parseFloat((String) val);
		if (val instanceof Boolean)
			return (Boolean) val ? 1 : 0;
		
		throw new IllegalArgumentException("StatsSet : Float value required, but found: " + val + " for key: " + key + ".");
	}
	
	public float getFloat(final String key, final float defaultValue)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).floatValue();
		if (val instanceof String)
			return Float.parseFloat((String) val);
		if (val instanceof Boolean)
			return (Boolean) val ? 1 : 0;
		
		return defaultValue;
	}
	
	public int getInteger(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).intValue();
		if (val instanceof String)
			return Integer.parseInt((String) val);
		if (val instanceof Boolean)
			return (Boolean) val ? 1 : 0;
		
		throw new IllegalArgumentException("StatsSet : Integer value required, but found: " + val + " for key: " + key + ".");
	}
	
	public int getInteger(final String key, final int defaultValue)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).intValue();
		if (val instanceof String)
			return Integer.parseInt((String) val);
		if (val instanceof Boolean)
			return (Boolean) val ? 1 : 0;
		
		return defaultValue;
	}
	
	public int[] getIntegerArray(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof int[])
			return (int[]) val;
		if (val instanceof Number)
			return new int[]
			{
				((Number) val).intValue()
			};
		if (val instanceof String)
		{
			final String[] vals = ((String) val).split(";");
			
			final int[] result = new int[vals.length];
			
			int i = 0;
			for (final String v : vals)
				result[i++] = Integer.parseInt(v);
			
			return result;
		}
		
		throw new IllegalArgumentException("StatsSet : Integer array required, but found: " + val + " for key: " + key + ".");
	}
	
	public int[] getIntegerArray(final String key, final int[] defaultArray)
	{
		try
		{
			return getIntegerArray(key);
		}
		catch (IllegalArgumentException e)
		{
			return defaultArray;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(final String key)
	{
		final Object val = get(key);
		
		if (val == null)
			return Collections.emptyList();
		
		return (List<T>) val;
	}
	
	public long getLong(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).longValue();
		if (val instanceof String)
			return Long.parseLong((String) val);
		if (val instanceof Boolean)
			return (Boolean) val ? 1L : 0L;
		
		throw new IllegalArgumentException("StatsSet : Long value required, but found: " + val + " for key: " + key + ".");
	}
	
	public long getLong(final String key, final long defaultValue)
	{
		final Object val = get(key);
		
		if (val instanceof Number)
			return ((Number) val).longValue();
		if (val instanceof String)
			return Long.parseLong((String) val);
		if (val instanceof Boolean)
			return (Boolean) val ? 1L : 0L;
		
		return defaultValue;
	}
	
	public long[] getLongArray(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof long[])
			return (long[]) val;
		if (val instanceof Number)
			return new long[]
			{
				((Number) val).longValue()
			};
		if (val instanceof String)
		{
			final String[] vals = ((String) val).split(";");
			
			final long[] result = new long[vals.length];
			
			int i = 0;
			for (final String v : vals)
				result[i++] = Integer.parseInt(v);
			
			return result;
		}
		
		throw new IllegalArgumentException("StatsSet : Long array required, but found: " + val + " for key: " + key + ".");
	}
	
	@SuppressWarnings("unchecked")
	public <T, U> Map<T, U> getMap(final String key)
	{
		final Object val = get(key);
		
		if (val == null)
			return Collections.emptyMap();
		
		return (Map<T, U>) val;
	}
	
	public String getString(final String key)
	{
		final Object val = get(key);
		
		if (val != null)
			return String.valueOf(val);
		
		throw new IllegalArgumentException("StatsSet : String value required, but unspecified for key: " + key + ".");
	}
	
	public String getString(final String key, final String defaultValue)
	{
		final Object val = get(key);
		
		if (val != null)
			return String.valueOf(val);
		
		return defaultValue;
	}
	
	public String[] getStringArray(final String key)
	{
		final Object val = get(key);
		
		if (val instanceof String[])
			return (String[]) val;
		if (val instanceof String)
			return ((String) val).split(";");
		
		throw new IllegalArgumentException("StatsSet : String array required, but found: " + val + " for key: " + key + ".");
	}
	
	@SuppressWarnings("unchecked")
	public <A> A getObject(final String key, final Class<A> type)
	{
		final Object val = get(key);
		
		if (val == null || !type.isAssignableFrom(val.getClass()))
			return null;
		
		return (A) val;
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> E getEnum(final String name, final Class<E> enumClass)
	{
		final Object val = get(name);
		
		if (val != null && enumClass.isInstance(val))
			return (E) val;
		if (val instanceof String)
			return Enum.valueOf(enumClass, (String) val);
		
		throw new IllegalArgumentException("Enum value of type " + enumClass.getName() + "required, but found: " + val + ".");
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Enum<E>> E getEnum(final String name, final Class<E> enumClass, final E defaultValue)
	{
		final Object val = get(name);
		
		if (val != null && enumClass.isInstance(val))
			return (E) val;
		if (val instanceof String)
			return Enum.valueOf(enumClass, (String) val);
		
		return defaultValue;
	}
}