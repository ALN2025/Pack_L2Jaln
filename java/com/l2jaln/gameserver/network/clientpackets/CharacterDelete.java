package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.network.FloodProtectors;
import com.l2jaln.gameserver.network.FloodProtectors.Action;
import com.l2jaln.gameserver.network.serverpackets.CharDeleteFail;
import com.l2jaln.gameserver.network.serverpackets.CharDeleteOk;
import com.l2jaln.gameserver.network.serverpackets.CharSelectInfo;

public final class CharacterDelete extends L2GameClientPacket
{
	private int _slot;
	
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!FloodProtectors.performAction(getClient(), Action.CHARACTER_SELECT))
		{
			sendPacket(CharDeleteFail.REASON_DELETION_FAILED);
			return;
		}
		
		switch (getClient().markToDeleteChar(_slot))
		{
			default:
			case -1: // Error
				break;
			
			case 0: // Success!
				sendPacket(CharDeleteOk.STATIC_PACKET);
				break;
			
			case 1:
				sendPacket(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER);
				break;
			
			case 2:
				sendPacket(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED);
				break;
		}
		
		final CharSelectInfo csi = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(csi);
		getClient().setCharSelection(csi.getCharInfo());
	}
}