package com.l2jaln.gameserver.network.serverpackets;

public class SetupGauge extends L2GameServerPacket
{
	public enum GaugeColor
	{
		BLUE,
		RED,
		CYAN,
		GREEN
	}
	
	private final GaugeColor _color;
	private final int _time;
	private final int _maxTime;
	
	public SetupGauge(GaugeColor color, int time)
	{
		_color = color;
		_time = time;
		_maxTime = time;
	}
	
	public SetupGauge(GaugeColor color, int currentTime, int maxTime)
	{
		_color = color;
		_time = currentTime;
		_maxTime = maxTime;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6d);
		writeD(_color.ordinal());
		writeD(_time);
		writeD(_maxTime);
	}
}