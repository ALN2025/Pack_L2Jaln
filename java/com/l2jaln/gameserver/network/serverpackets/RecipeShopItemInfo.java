package com.l2jaln.gameserver.network.serverpackets;

import com.l2jaln.gameserver.model.actor.instance.Player;

/**
 * ddddd
 */
public class RecipeShopItemInfo extends L2GameServerPacket
{
	private final Player _player;
	private final int _recipeId;
	
	public RecipeShopItemInfo(Player player, int recipeId)
	{
		_player = player;
		_recipeId = recipeId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xda);
		writeD(_player.getObjectId());
		writeD(_recipeId);
		writeD((int) _player.getCurrentMp());
		writeD(_player.getMaxMp());
		writeD(0xffffffff);
	}
}