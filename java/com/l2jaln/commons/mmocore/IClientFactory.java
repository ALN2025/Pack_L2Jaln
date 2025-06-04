package com.l2jaln.commons.mmocore;

/**
 * @author KenM
 * @param <T>
 */
public interface IClientFactory<T extends MMOClient<?>>
{
	public T create(final MMOConnection<T> con);
}