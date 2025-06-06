package com.l2jaln.loginserver.model;

import java.util.Objects;

public final class AccountInfo
{
	private final String _login;
	private final String _passHash;
	private final int _accessLevel;
	private final int _lastServer;

	public AccountInfo(final String login, final String passHash, final int accessLevel, final int lastServer)
	{
		Objects.<String> requireNonNull(login, "login");
		Objects.<String> requireNonNull(passHash, "passHash");
		if (login.isEmpty())
			throw new IllegalArgumentException("login");
		if (passHash.isEmpty())
			throw new IllegalArgumentException("passHash");
		_login = login.toLowerCase();
		_passHash = passHash;
		_accessLevel = accessLevel;
		_lastServer = lastServer;
	}

	public boolean checkPassHash(final String passHash)
	{
		return _passHash.equals(passHash);
	}

	public String getLogin()
	{
		return _login;
	}

	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public int getLastServer()
	{
		return _lastServer;
	}
}
