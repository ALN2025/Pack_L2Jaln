package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.Config;
import com.l2jaln.gameserver.communitybbs.CommunityBoard;

public final class RequestShowBoard extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
	}
}