package com.l2jaln.gameserver.network.serverpackets;

import com.l2jaln.gameserver.model.pledge.Clan;

public class ManagePledgePower extends L2GameServerPacket
{
	private final int _action;
	private final Clan _clan;
	private final int _rank;
	
	public ManagePledgePower(Clan clan, int action, int rank)
	{
		_clan = clan;
		_action = action;
		_rank = rank;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x30);
		writeD(_rank);
		writeD(_action);
		writeD(_clan.getPriviledgesByRank(_rank));
	}
}