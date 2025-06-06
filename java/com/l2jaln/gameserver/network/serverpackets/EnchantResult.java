package com.l2jaln.gameserver.network.serverpackets;

public class EnchantResult extends L2GameServerPacket
{
	public static final EnchantResult SUCCESS = new EnchantResult(0);
	public static final EnchantResult UNK_RESULT_1 = new EnchantResult(1);
	public static final EnchantResult CANCELLED = new EnchantResult(2);
	public static final EnchantResult UNSUCCESS = new EnchantResult(3);
	public static final EnchantResult UNK_RESULT_4 = new EnchantResult(4);
	
	private final int _result;
	
	public EnchantResult(int result)
	{
		_result = result;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x81);
		writeD(_result);
	}
}