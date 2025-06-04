package com.l2jaln.loginserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jaln.commons.mmocore.ReceivablePacket;

import com.l2jaln.loginserver.L2LoginClient;

public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{
	private static Logger _log;

	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			L2LoginClientPacket._log.severe("ERROR READING: " + this.getClass().getSimpleName());
			e.printStackTrace();
			return false;
		}
	}

	protected abstract boolean readImpl();

	static
	{
		L2LoginClientPacket._log = Logger.getLogger(L2LoginClientPacket.class.getName());
	}
}
