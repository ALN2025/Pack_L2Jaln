package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.serverpackets.ActionFailed;
import com.l2jaln.gameserver.network.serverpackets.GetOffVehicle;
import com.l2jaln.gameserver.network.serverpackets.StopMoveInVehicle;

public final class RequestGetOffVehicle extends L2GameClientPacket
{
	private int _boatId;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	protected void readImpl()
	{
		_boatId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!activeChar.isInBoat() || activeChar.getBoat().getObjectId() != _boatId || activeChar.getBoat().isMoving() || !activeChar.isInsideRadius(_x, _y, _z, 1000, true, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.broadcastPacket(new StopMoveInVehicle(activeChar, _boatId));
		activeChar.setVehicle(null);
		sendPacket(ActionFailed.STATIC_PACKET);
		activeChar.broadcastPacket(new GetOffVehicle(activeChar.getObjectId(), _boatId, _x, _y, _z));
		activeChar.setXYZ(_x, _y, _z + 50);
		activeChar.revalidateZone(true);
	}
}