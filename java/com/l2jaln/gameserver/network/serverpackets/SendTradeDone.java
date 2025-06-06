package com.l2jaln.gameserver.network.serverpackets;

public class SendTradeDone extends L2GameServerPacket
{
	private final int _num;
	
	public SendTradeDone(int num)
	{
		_num = num;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x22);
		writeD(_num);
	}
}