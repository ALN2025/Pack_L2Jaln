package com.l2jaln.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jaln.gameserver.data.PlayerNameTable;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.actor.instance.Player;

/**
 * Support for "Chat with Friends" dialog.
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	private final List<FriendInfo> _info;
	
	private static class FriendInfo
	{
		int _objId;
		String _name;
		boolean _online;
		
		public FriendInfo(int objId, String name, boolean online)
		{
			_objId = objId;
			_name = name;
			_online = online;
		}
	}
	
	public FriendList(Player player)
	{
		_info = new ArrayList<>(player.getFriendList().size());
		
		for (int objId : player.getFriendList())
		{
			final String name = PlayerNameTable.getInstance().getPlayerName(objId);
			final Player player1 = World.getInstance().getPlayer(objId);
			
			_info.add(new FriendInfo(objId, name, (player1 != null && player1.isOnline())));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfa);
		writeD(_info.size());
		for (FriendInfo info : _info)
		{
			writeD(info._objId);
			writeS(info._name);
			writeD(info._online ? 0x01 : 0x00);
			writeD(info._online ? info._objId : 0x00);
		}
	}
}