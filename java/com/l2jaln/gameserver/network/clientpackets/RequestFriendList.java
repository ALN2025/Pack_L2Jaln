package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.data.PlayerNameTable;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		// ======<Friend List>======
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER);
		
		for (int id : activeChar.getFriendList())
		{
			final String friendName = PlayerNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
				continue;
			
			final Player friend = World.getInstance().getPlayer(id);
			
			activeChar.sendPacket(SystemMessage.getSystemMessage((friend == null || !friend.isOnline()) ? SystemMessageId.S1_OFFLINE : SystemMessageId.S1_ONLINE).addString(friendName));
		}
		
		// =========================
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
	}
}