package com.l2jaln.loginserver.network.loginserverpackets;

import com.l2jaln.loginserver.network.serverpackets.ServerBasePacket;

public class KickPlayer extends ServerBasePacket
{
	public KickPlayer(final String account)
	{
		writeC(4);
		writeS(account);
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
