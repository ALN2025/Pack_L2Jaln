package com.l2jaln.commons.mmocore;

import java.nio.ByteBuffer;

/**
 * @author KenM
 * @param <T>
 */
public abstract class MMOClient<T extends MMOConnection<?>>
{
	private final T _con;
	
	public MMOClient(final T con)
	{
		_con = con;
	}
	
	public T getConnection()
	{
		return _con;
	}
	
	public abstract boolean decrypt(final ByteBuffer buf, final int size);
	
	public abstract boolean encrypt(final ByteBuffer buf, final int size);
	
	protected abstract void onDisconnection();
	
	protected abstract void onForcedDisconnection();
}