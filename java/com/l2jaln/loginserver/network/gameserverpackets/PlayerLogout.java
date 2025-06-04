package com.l2jaln.loginserver.network.gameserverpackets;

import com.l2jaln.loginserver.network.clientpackets.ClientBasePacket;

public class PlayerLogout extends ClientBasePacket
{
	private final String _account;

	public PlayerLogout(final byte[] decrypt)
	{
		super(decrypt);
		_account = readS();
	}

	public String getAccount()
	{
		return _account;
	}
}
