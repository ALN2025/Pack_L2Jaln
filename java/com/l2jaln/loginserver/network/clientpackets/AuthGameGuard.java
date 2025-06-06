package com.l2jaln.loginserver.network.clientpackets;

import com.l2jaln.loginserver.L2LoginClient;
import com.l2jaln.loginserver.network.serverpackets.GGAuth;
import com.l2jaln.loginserver.network.serverpackets.LoginFail;

public class AuthGameGuard extends L2LoginClientPacket
{
	private int _sessionId;
	private int _data1;
	private int _data2;
	private int _data3;
	private int _data4;

	public int getSessionId()
	{
		return _sessionId;
	}

	public int getData1()
	{
		return _data1;
	}

	public int getData2()
	{
		return _data2;
	}

	public int getData3()
	{
		return _data3;
	}

	public int getData4()
	{
		return _data4;
	}

	@Override
	protected boolean readImpl()
	{
		if (super._buf.remaining() >= 20)
		{
			_sessionId = readD();
			_data1 = readD();
			_data2 = readD();
			_data3 = readD();
			_data4 = readD();
			return true;
		}
		return false;
	}

	@Override
	public void run()
	{
		if (_sessionId == getClient().getSessionId())
		{
			getClient().setState(L2LoginClient.LoginClientState.AUTHED_GG);
			getClient().sendPacket(new GGAuth(getClient().getSessionId()));
		}
		else
			getClient().close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
	}
}
