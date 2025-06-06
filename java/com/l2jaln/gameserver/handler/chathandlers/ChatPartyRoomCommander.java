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
package com.l2jaln.gameserver.handler.chathandlers;

import com.l2jaln.gameserver.handler.IChatHandler;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.clientpackets.Say2;
import com.l2jaln.gameserver.network.serverpackets.CreatureSay;

/**
 * A chat handler
 * @author durgus
 */
public class ChatPartyRoomCommander implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		15
	};
	
	/**
	 * Handle chat type 'party room commander'
	 */
	@Override
	public void handleChat(int type, Player activeChar, String target, String text)
	{
		if (!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel() || !activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
 			return;
	
		if (activeChar.ChatProtection(activeChar.getHWID()) && activeChar.isChatBlocked() && ((activeChar.getChatBanTimer()-1500) > System.currentTimeMillis()))
		{
			if (((activeChar.getChatBanTimer() - System.currentTimeMillis()) / 1000) >= 60)
				activeChar.sendChatMessage(0, Say2.TELL, "SYS", "Your chat was suspended for " + (activeChar.getChatBanTimer() - System.currentTimeMillis()) / (1000*60) + " minute(s).");
			else
				activeChar.sendChatMessage(0, Say2.TELL, "SYS", "Your chat was suspended for " + (activeChar.getChatBanTimer() - System.currentTimeMillis()) / 1000 + " second(s).");
			
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		activeChar.getParty().getCommandChannel().broadcastCSToChannelMembers(new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text), activeChar);
	}
	
	/**
	 * Returns the chat types registered to this handler
	 * @see com.l2jaln.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
