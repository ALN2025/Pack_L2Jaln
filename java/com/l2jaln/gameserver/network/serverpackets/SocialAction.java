package com.l2jaln.gameserver.network.serverpackets;

import com.l2jaln.gameserver.model.actor.Creature;

public class SocialAction extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _actionId;
	
	public SocialAction(Creature cha, int actionId)
	{
		_charObjId = cha.getObjectId();
		_actionId = actionId;
	}
	
	public SocialAction(int playerId, int actionId)
	{
		_charObjId = playerId;
		_actionId = actionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2d);
		writeD(_charObjId);
		writeD(_actionId);
	}
}