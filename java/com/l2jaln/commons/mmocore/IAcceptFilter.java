package com.l2jaln.commons.mmocore;

import java.nio.channels.SocketChannel;

/**
 * @author KenM
 */
public interface IAcceptFilter
{
	public boolean accept(SocketChannel sc);
}