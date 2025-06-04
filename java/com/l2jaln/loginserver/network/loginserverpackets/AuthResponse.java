package com.l2jaln.loginserver.network.loginserverpackets;

import com.l2jaln.loginserver.GameServerTable;
import com.l2jaln.loginserver.network.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket
{
	public AuthResponse(final int serverId)
	{
		writeC(2);
		writeC(serverId);
		writeS(GameServerTable.getInstance().getServerNames().get(serverId));
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
