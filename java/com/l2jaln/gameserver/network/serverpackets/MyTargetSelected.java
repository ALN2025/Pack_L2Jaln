package com.l2jaln.gameserver.network.serverpackets;

public class MyTargetSelected extends L2GameServerPacket
{
	private final int _objectId;
	private final int _color;
	
	public MyTargetSelected(int objectId, int color)
	{
		_objectId = objectId;
		_color = color;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xa6);
		writeD(_objectId);
		writeH(_color);
	}
}