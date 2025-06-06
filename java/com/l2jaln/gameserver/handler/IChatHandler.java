package com.l2jaln.gameserver.handler;

import com.l2jaln.gameserver.model.actor.instance.Player;

/**
 * Interface for chat handlers
 * @author durgus
 */
public interface IChatHandler
{
	/**
	 * Handles a specific type of chat messages
	 * @param type
	 * @param activeChar
	 * @param target
	 * @param text
	 */
	public void handleChat(int type, Player activeChar, String target, String text);
	
	/**
	 * Returns a list of all chat types registered to this handler
	 * @return
	 */
	public int[] getChatTypeList();
}
