package com.l2jaln.gameserver.network.serverpackets;

import com.l2jaln.gameserver.model.actor.Vehicle;

public class VehicleDeparture extends L2GameServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _moveSpeed;
	private final int _rotationSpeed;
	
	public VehicleDeparture(Vehicle boat)
	{
		_objectId = boat.getObjectId();
		_x = boat.getXdestination();
		_y = boat.getYdestination();
		_z = boat.getZdestination();
		_moveSpeed = (int) boat.getStat().getMoveSpeed();
		_rotationSpeed = boat.getStat().getRotationSpeed();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x5A);
		writeD(_objectId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}