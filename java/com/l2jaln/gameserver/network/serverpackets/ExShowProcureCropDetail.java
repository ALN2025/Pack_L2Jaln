package com.l2jaln.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import com.l2jaln.gameserver.instancemanager.CastleManager;
import com.l2jaln.gameserver.instancemanager.CastleManorManager;
import com.l2jaln.gameserver.model.entity.Castle;
import com.l2jaln.gameserver.model.manor.CropProcure;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private final int _cropId;
	private final Map<Integer, CropProcure> _castleCrops;
	
	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new HashMap<>();
		
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			final CropProcure cropItem = CastleManorManager.getInstance().getCropProcure(c.getCastleId(), cropId, false);
			if (cropItem != null && cropItem.getAmount() > 0)
				_castleCrops.put(c.getCastleId(), cropItem);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x22);
		
		writeD(_cropId);
		writeD(_castleCrops.size());
		
		for (Map.Entry<Integer, CropProcure> entry : _castleCrops.entrySet())
		{
			final CropProcure crop = entry.getValue();
			
			writeD(entry.getKey());
			writeD(crop.getAmount());
			writeD(crop.getPrice());
			writeC(crop.getReward());
		}
	}
}