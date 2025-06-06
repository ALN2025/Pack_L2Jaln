package com.l2jaln.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.L2DatabaseFactory;
import com.l2jaln.gameserver.data.PlayerNameTable;
import com.l2jaln.gameserver.data.cache.HtmCache;
import com.l2jaln.gameserver.model.BlockList;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.FriendList;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;

public class FriendsBBSManager extends BaseBBSManager
{
	private static final String FRIENDLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all friends from your Friends List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _friend;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>";
	private static final String BLOCKLIST_DELETE_BUTTON = "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all players from your Block List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _block;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>";
	
	protected FriendsBBSManager()
	{
	}
	
	public static FriendsBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void parseCmd(String command, Player activeChar)
	{
		if (command.startsWith("_friendlist"))
			showFriendsList(activeChar, false);
		else if (command.startsWith("_blocklist"))
			showBlockList(activeChar, false);
		else if (command.startsWith("_friend"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String action = st.nextToken();
			
			if (action.equals("select"))
			{
				activeChar.selectFriend((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showFriendsList(activeChar, false);
			}
			else if (action.equals("deselect"))
			{
				activeChar.deselectFriend((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showFriendsList(activeChar, false);
			}
			else if (action.equals("delall"))
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id = ? OR friend_id = ?");
					statement.setInt(1, activeChar.getObjectId());
					statement.setInt(2, activeChar.getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "could not delete friends objectid: ", e);
				}
				
				for (int friendId : activeChar.getFriendList())
				{
					Player player = World.getInstance().getPlayer(friendId);
					if (player != null)
					{
						player.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
						player.getSelectedFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
						
						player.sendPacket(new FriendList(player)); // update friendList *heavy method*
					}
				}
				
				activeChar.getFriendList().clear();
				activeChar.getSelectedFriendList().clear();
				showFriendsList(activeChar, false);
				
				activeChar.sendMessage("You have cleared your friend list.");
				activeChar.sendPacket(new FriendList(activeChar));
			}
			else if (action.equals("delconfirm"))
				showFriendsList(activeChar, true);
			else if (action.equals("del"))
			{
				try (Connection con = L2DatabaseFactory.getInstance().getConnection())
				{
					for (int friendId : activeChar.getSelectedFriendList())
					{
						PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)");
						statement.setInt(1, activeChar.getObjectId());
						statement.setInt(2, friendId);
						statement.setInt(3, friendId);
						statement.setInt(4, activeChar.getObjectId());
						statement.execute();
						statement.close();
						
						String name = PlayerNameTable.getInstance().getPlayerName(friendId);
						
						Player player = World.getInstance().getPlayer(friendId);
						if (player != null)
						{
							player.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
							player.sendPacket(new FriendList(player)); // update friendList *heavy method*
						}
						
						// Player deleted from your friendlist
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(name));
						
						activeChar.getFriendList().remove(Integer.valueOf(friendId));
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "could not delete friend objectid: ", e);
				}
				
				activeChar.getSelectedFriendList().clear();
				showFriendsList(activeChar, false);
				
				activeChar.sendPacket(new FriendList(activeChar)); // update friendList *heavy method*
			}
			else if (action.equals("mail"))
			{
				if (!activeChar.getSelectedFriendList().isEmpty())
					showMailWrite(activeChar);
			}
		}
		else if (command.startsWith("_block"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String action = st.nextToken();
			
			if (action.equals("select"))
			{
				activeChar.selectBlock((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showBlockList(activeChar, false);
			}
			else if (action.equals("deselect"))
			{
				activeChar.deselectBlock((st.hasMoreTokens()) ? Integer.valueOf(st.nextToken()) : 0);
				showBlockList(activeChar, false);
			}
			else if (action.equals("delall"))
			{
				List<Integer> list = new ArrayList<>();
				list.addAll(activeChar.getBlockList().getBlockList());
				
				for (Integer blockId : list)
					BlockList.removeFromBlockList(activeChar, blockId);
				
				activeChar.getSelectedBlocksList().clear();
				showBlockList(activeChar, false);
			}
			else if (action.equals("delconfirm"))
				showBlockList(activeChar, true);
			else if (action.equals("del"))
			{
				for (Integer blockId : activeChar.getSelectedBlocksList())
					BlockList.removeFromBlockList(activeChar, blockId);
				
				activeChar.getSelectedBlocksList().clear();
				showBlockList(activeChar, false);
			}
		}
		else
			super.parseCmd(command, activeChar);
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar)
	{
		if (ar1.equalsIgnoreCase("mail"))
		{
			MailBBSManager.getInstance().sendLetter(ar2, ar4, ar5, activeChar);
			showFriendsList(activeChar, false);
		}
		else
			super.parseWrite(ar1, ar2, ar3, ar4, ar5, activeChar);
	}
	
	private static void showFriendsList(Player activeChar, boolean delMsg)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-list.htm");
		if (content == null)
			return;
		
		// Retrieve activeChar's friendlist and selected
		final List<Integer> list = activeChar.getFriendList();
		final List<Integer> slist = activeChar.getSelectedFriendList();
		
		final StringBuilder sb = new StringBuilder();
		
		// Friendlist
		for (Integer id : list)
		{
			if (slist.contains(id))
				continue;
			
			final String friendName = PlayerNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
				continue;
			
			final Player friend = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _friend;select;", id, "\">[Select]</a>&nbsp;", friendName, " ", ((friend != null && friend.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replaceAll("%friendslist%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		
		// Selected friendlist
		for (Integer id : slist)
		{
			final String friendName = PlayerNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
				continue;
			
			final Player friend = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _friend;deselect;", id, "\">[Deselect]</a>&nbsp;", friendName, " ", ((friend != null && friend.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replaceAll("%selectedFriendsList%", sb.toString());
		
		// Delete button.
		content = content.replaceAll("%deleteMSG%", (delMsg) ? FRIENDLIST_DELETE_BUTTON : "");
		
		separateAndSend(content, activeChar);
	}
	
	private static void showBlockList(Player activeChar, boolean delMsg)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-blocklist.htm");
		if (content == null)
			return;
		
		// Retrieve activeChar's blocklist and selected
		final List<Integer> list = activeChar.getBlockList().getBlockList();
		final List<Integer> slist = activeChar.getSelectedBlocksList();
		
		final StringBuilder sb = new StringBuilder();
		
		// Blocklist
		for (Integer id : list)
		{
			if (slist.contains(id))
				continue;
			
			final String blockName = PlayerNameTable.getInstance().getPlayerName(id);
			if (blockName == null)
				continue;
			
			final Player block = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _block;select;", id, "\">[Select]</a>&nbsp;", blockName, " ", ((block != null && block.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replaceAll("%blocklist%", sb.toString());
		
		// Cleanup sb.
		sb.setLength(0);
		
		// Selected Blocklist
		for (Integer id : slist)
		{
			final String blockName = PlayerNameTable.getInstance().getPlayerName(id);
			if (blockName == null)
				continue;
			
			final Player block = World.getInstance().getPlayer(id);
			StringUtil.append(sb, "<a action=\"bypass _block;deselect;", id, "\">[Deselect]</a>&nbsp;", blockName, " ", ((block != null && block.isOnline()) ? "(on)" : "(off)"), "<br1>");
		}
		content = content.replaceAll("%selectedBlocksList%", sb.toString());
		
		// Delete button.
		content = content.replaceAll("%deleteMSG%", (delMsg) ? BLOCKLIST_DELETE_BUTTON : "");
		
		separateAndSend(content, activeChar);
	}
	
	public static final void showMailWrite(Player activeChar)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "friend/friend-mail.htm");
		if (content == null)
			return;
		
		final StringBuilder sb = new StringBuilder();
		for (int id : activeChar.getSelectedFriendList())
		{
			String friendName = PlayerNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
				continue;
			
			if (sb.length() > 0)
				sb.append(";");
			
			sb.append(friendName);
		}
		
		content = content.replaceAll("%list%", sb.toString());
		
		separateAndSend(content, activeChar);
	}
	
	@Override
	protected String getFolder()
	{
		return "friend/";
	}
	
	private static class SingletonHolder
	{
		protected static final FriendsBBSManager _instance = new FriendsBBSManager();
	}
}