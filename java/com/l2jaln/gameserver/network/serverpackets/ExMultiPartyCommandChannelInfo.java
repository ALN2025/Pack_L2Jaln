/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jaln.gameserver.network.serverpackets;

import com.l2jaln.gameserver.model.L2CommandChannel;
import com.l2jaln.gameserver.model.L2Party;

/**
 * @author chris_00 ch sdd d[sdd]
 */
public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
	private final L2CommandChannel _channel;
	
	public ExMultiPartyCommandChannelInfo(L2CommandChannel channel)
	{
		_channel = channel;
	}
	
	@Override
	protected void writeImpl()
	{
		if (_channel == null)
			return;
		
		writeC(0xfe);
		writeH(0x30);
		
		writeS(_channel.getChannelLeader().getName());
		writeD(0); // Channel loot
		writeD(_channel.getMemberCount());
		
		writeD(_channel.getPartys().size());
		for (L2Party p : _channel.getPartys())
		{
			writeS(p.getLeader().getName());
			writeD(p.getPartyLeaderOID());
			writeD(p.getMemberCount());
		}
	}
}