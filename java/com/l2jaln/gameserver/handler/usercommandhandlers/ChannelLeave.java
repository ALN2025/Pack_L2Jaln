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
package com.l2jaln.gameserver.handler.usercommandhandlers;

import com.l2jaln.gameserver.handler.IUserCommandHandler;
import com.l2jaln.gameserver.model.L2CommandChannel;
import com.l2jaln.gameserver.model.L2Party;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Chris
 */
public class ChannelLeave implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		96
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		
		final L2Party party = activeChar.getParty();
		if (party != null)
		{
			if (party.isLeader(activeChar) && party.isInCommandChannel())
			{
				final L2CommandChannel channel = party.getCommandChannel();
				channel.removeParty(party);
				
				party.getLeader().sendPacket(SystemMessageId.LEFT_COMMAND_CHANNEL);
				channel.broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_LEFT_COMMAND_CHANNEL).addCharName(party.getLeader()));
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}