package com.l2jaln.gameserver.network.clientpackets;

import hwid.Hwid;
import hwid.HwidConfig;

import com.l2jaln.Config;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.serverpackets.KeyPacket;
import com.l2jaln.gameserver.network.serverpackets.L2GameServerPacket;

public final class ProtocolVersion extends L2GameClientPacket
{
	private int _version;
	private byte _data[];
	private String _hwidHdd = "NoHWID-HD";
	private String _hwidMac = "NoHWID-MAC";
	private String _hwidCPU = "NoHWID-CPU";
	Player player;
	
	@Override
	protected void readImpl()
	{
		
		_version = readD();
		
		if (Hwid.isProtectionOn())
		{
			if (_buf.remaining() > 260)
			{
				_data = new byte[260];
				readB(_data);
				if (Hwid.isProtectionOn())
				{
					_hwidHdd = readS();
					_hwidMac = readS();
					_hwidCPU = readS();
				}
			}
		}
		else if (Hwid.isProtectionOn())
		{
			getClient().close(new KeyPacket(getClient().enableCrypt()));
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_version == -2)
			getClient().close((L2GameServerPacket) null);
		else if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION)
		{
			_log.warning("Client: " + getClient().toString() + " -> Protocol Revision: " + _version + " is invalid. Minimum and maximum allowed are: " + Config.MIN_PROTOCOL_REVISION + " and " + Config.MAX_PROTOCOL_REVISION + ". Closing connection.");
			getClient().close((L2GameServerPacket) null);
		}
		else
			getClient().sendPacket(new KeyPacket(getClient().enableCrypt()));
		
		getClient().setRevision(_version);
		
		
		if (Hwid.isProtectionOn())
		{
			if (_hwidHdd.equals("NoGuard") && _hwidMac.equals("NoGuard") && _hwidCPU.equals("NoGuard"))
			{
				_log.info("HWID Status: No Client side dlls");
				getClient().close(new KeyPacket(getClient().enableCrypt()));
			}
			
			switch (HwidConfig.GET_CLIENT_HWID)
			{
				case 1:
					getClient().setHWID(_hwidHdd);
					break;
				case 2:
					getClient().setHWID(_hwidMac);
					break;
				case 3:
					getClient().setHWID(_hwidCPU);
					break;
			}
		}
		
	}
}