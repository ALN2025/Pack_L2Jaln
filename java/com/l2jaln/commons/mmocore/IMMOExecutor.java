package com.l2jaln.commons.mmocore;

/**
 * @author KenM
 * @param <T>
 */
public interface IMMOExecutor<T extends MMOClient<?>>
{
	public void execute(ReceivablePacket<T> packet);
}