package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
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
		
		activeChar.sendPacket(new ExCursedWeaponList(CursedWeaponsManager.getInstance().getCursedWeaponsIds()));
	}
}