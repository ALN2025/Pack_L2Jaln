package com.l2jaln.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import com.l2jaln.gameserver.handler.chathandlers.ChatAll;
import com.l2jaln.gameserver.handler.chathandlers.ChatAlliance;
import com.l2jaln.gameserver.handler.chathandlers.ChatClan;
import com.l2jaln.gameserver.handler.chathandlers.ChatHeroVoice;
import com.l2jaln.gameserver.handler.chathandlers.ChatParty;
import com.l2jaln.gameserver.handler.chathandlers.ChatPartyMatchRoom;
import com.l2jaln.gameserver.handler.chathandlers.ChatPartyRoomAll;
import com.l2jaln.gameserver.handler.chathandlers.ChatPartyRoomCommander;
import com.l2jaln.gameserver.handler.chathandlers.ChatPetition;
import com.l2jaln.gameserver.handler.chathandlers.ChatShout;
import com.l2jaln.gameserver.handler.chathandlers.ChatTell;
import com.l2jaln.gameserver.handler.chathandlers.ChatTrade;

public class ChatHandler
{
	private final Map<Integer, IChatHandler> _datatable = new HashMap<>();
	
	public static ChatHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ChatHandler()
	{
		registerChatHandler(new ChatAll());
		registerChatHandler(new ChatAlliance());
		registerChatHandler(new ChatClan());
		registerChatHandler(new ChatHeroVoice());
		registerChatHandler(new ChatParty());
		registerChatHandler(new ChatPartyMatchRoom());
		registerChatHandler(new ChatPartyRoomAll());
		registerChatHandler(new ChatPartyRoomCommander());
		registerChatHandler(new ChatPetition());
		registerChatHandler(new ChatShout());
		registerChatHandler(new ChatTell());
		registerChatHandler(new ChatTrade());
	}
	
	public void registerChatHandler(IChatHandler handler)
	{
		for (int id : handler.getChatTypeList())
			_datatable.put(id, handler);
	}
	
	public IChatHandler getChatHandler(int chatType)
	{
		return _datatable.get(chatType);
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final ChatHandler _instance = new ChatHandler();
	}
}