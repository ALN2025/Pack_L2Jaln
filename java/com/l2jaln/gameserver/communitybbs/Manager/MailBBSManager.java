package com.l2jaln.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.L2DatabaseFactory;
import com.l2jaln.gameserver.data.PlayerNameTable;
import com.l2jaln.gameserver.data.cache.HtmCache;
import com.l2jaln.gameserver.model.BlockList;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.ExMailArrived;
import com.l2jaln.gameserver.network.serverpackets.PlaySound;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;

/**
 * @author JIV, Johan, Vital
 */
public class MailBBSManager extends BaseBBSManager
{
	private static enum MailType
	{
		INBOX("Inbox", "<a action=\"bypass _bbsmail\">Inbox</a>"),
		SENTBOX("Sent Box", "<a action=\"bypass _bbsmail;sentbox\">Sent Box</a>"),
		ARCHIVE("Mail Archive", "<a action=\"bypass _bbsmail;archive\">Mail Archive</a>"),
		TEMPARCHIVE("Temporary Mail Archive", "<a action=\"bypass _bbsmail;temp_archive\">Temporary Mail Archive</a>");
		
		private final String _description;
		private final String _bypass;
		
		private MailType(String description, String bypass)
		{
			_description = description;
			_bypass = bypass;
		}
		
		public String getDescription()
		{
			return _description;
		}
		
		public String getBypass()
		{
			return _bypass;
		}
		
		public static final MailType[] VALUES = values();
	}
	
	private final Map<Integer, List<Mail>> _mails = new HashMap<>();
	
	private int _lastid = 0;
	
	private static final String SELECT_CHAR_MAILS = "SELECT * FROM character_mail WHERE charId = ? ORDER BY letterId ASC";
	private static final String INSERT_NEW_MAIL = "INSERT INTO character_mail (charId, letterId, senderId, location, recipientNames, subject, message, sentDate, unread) VALUES (?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_MAIL = "DELETE FROM character_mail WHERE letterId = ?";
	private static final String MARK_MAIL_READ = "UPDATE character_mail SET unread = ? WHERE letterId = ?";
	private static final String SET_LETTER_LOC = "UPDATE character_mail SET location = ? WHERE letterId = ?";
	private static final String SELECT_LAST_ID = "SELECT letterId FROM character_mail ORDER BY letterId DESC LIMIT 1";
	
	public class Mail
	{
		int charId;
		int letterId;
		int senderId;
		MailType location;
		String recipientNames;
		String subject;
		String message;
		Timestamp sentDate;
		String sentDateString;
		boolean unread;
	}
	
	public static MailBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected MailBBSManager()
	{
		initId();
	}
	
	@Override
	public void parseCmd(String command, Player activeChar)
	{
		if (command.equals("_bbsmail") || command.equals("_maillist_0_1_0_"))
			showMailList(activeChar, 1, MailType.INBOX);
		else if (command.startsWith("_bbsmail"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String action = st.nextToken();
			
			if (action.equals("inbox") || action.equals("sentbox") || action.equals("archive") || action.equals("temparchive"))
			{
				final int page = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 1;
				final String sType = (st.hasMoreTokens()) ? st.nextToken() : "";
				final String search = (st.hasMoreTokens()) ? st.nextToken() : "";
				
				showMailList(activeChar, page, Enum.valueOf(MailType.class, action.toUpperCase()), sType, search);
			}
			else if (action.equals("crea"))
				showWriteView(activeChar);
			else if (action.equals("view"))
			{
				final int letterId = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : -1;
				
				Mail letter = getLetter(activeChar, letterId);
				if (letter == null)
					showLastForum(activeChar);
				else
				{
					showLetterView(activeChar, letter);
					if (letter.unread)
						setLetterToRead(activeChar, letter.letterId);
				}
			}
			else if (action.equals("reply"))
			{
				final int letterId = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : -1;
				
				Mail letter = getLetter(activeChar, letterId);
				if (letter == null)
					showLastForum(activeChar);
				else
					showWriteView(activeChar, getCharName(letter.senderId), letter);
			}
			else if (action.equals("del"))
			{
				final int letterId = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : -1;
				
				Mail letter = getLetter(activeChar, letterId);
				if (letter != null)
					deleteLetter(activeChar, letter.letterId);
				
				showLastForum(activeChar);
			}
			else if (action.equals("store"))
			{
				final int letterId = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : -1;
				
				Mail letter = getLetter(activeChar, letterId);
				if (letter != null)
					setLetterLocation(activeChar, letter.letterId, MailType.ARCHIVE);
				
				showMailList(activeChar, 1, MailType.ARCHIVE);
			}
		}
		else
			super.parseCmd(command, activeChar);
	}
	
	@Override
	public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar)
	{
		if (ar1.equals("Send"))
		{
			sendLetter(ar3, ar4, ar5, activeChar);
			showMailList(activeChar, 1, MailType.SENTBOX);
		}
		else if (ar1.startsWith("Search"))
		{
			StringTokenizer st = new StringTokenizer(ar1, ";");
			st.nextToken();
			
			showMailList(activeChar, 1, Enum.valueOf(MailType.class, st.nextToken().toUpperCase()), ar4, ar5);
		}
		else
			super.parseWrite(ar1, ar2, ar3, ar4, ar5, activeChar);
	}
	
	private void initId()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SELECT_LAST_ID);
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				if (result.getInt(1) > _lastid)
					_lastid = result.getInt(1);
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(getClass().getSimpleName() + ": data error on MailBBS (initId): " + e);
			e.printStackTrace();
		}
	}
	
	private synchronized int getNewMailId()
	{
		return ++_lastid;
	}
	
	private List<Mail> getPlayerMails(int objId)
	{
		List<Mail> _letters = _mails.get(objId);
		if (_letters == null)
		{
			_letters = new ArrayList<>();
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement(SELECT_CHAR_MAILS);
				statement.setInt(1, objId);
				ResultSet result = statement.executeQuery();
				while (result.next())
				{
					Mail letter = new Mail();
					letter.charId = result.getInt("charId");
					letter.letterId = result.getInt("letterId");
					letter.senderId = result.getInt("senderId");
					letter.location = Enum.valueOf(MailType.class, result.getString("location").toUpperCase());
					letter.recipientNames = result.getString("recipientNames");
					letter.subject = result.getString("subject");
					letter.message = result.getString("message");
					letter.sentDate = result.getTimestamp("sentDate");
					letter.sentDateString = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(letter.sentDate);
					letter.unread = result.getInt("unread") != 0;
					_letters.add(0, letter);
				}
				result.close();
				statement.close();
			}
			catch (Exception e)
			{
				_log.warning("couldnt load mail for ID:" + objId + " " + e.getMessage());
			}
			_mails.put(objId, _letters);
		}
		return _letters;
	}
	
	private Mail getLetter(Player activeChar, int letterId)
	{
		for (Mail letter : getPlayerMails(activeChar.getObjectId()))
		{
			if (letter.letterId == letterId)
				return letter;
		}
		return null;
	}
	
	private static String abbreviate(String s, int maxWidth)
	{
		return s.length() > maxWidth ? s.substring(0, maxWidth) : s;
	}
	
	public int checkUnreadMail(Player activeChar)
	{
		int count = 0;
		for (Mail letter : getPlayerMails(activeChar.getObjectId()))
		{
			if (letter.unread)
				count++;
		}
		return count;
	}
	
	private void showMailList(Player activeChar, int page, MailType type)
	{
		showMailList(activeChar, page, type, "", "");
	}
	
	private void showMailList(Player activeChar, int page, MailType type, String sType, String search)
	{
		List<Mail> letters;
		if (!sType.equals("") && !search.equals(""))
		{
			letters = new ArrayList<>();
			
			boolean byTitle = sType.equalsIgnoreCase("title");
			
			for (Mail letter : getPlayerMails(activeChar.getObjectId()))
			{
				if (byTitle && letter.subject.toLowerCase().contains(search.toLowerCase()))
					letters.add(letter);
				else if (!byTitle)
				{
					String writer = getCharName(letter.senderId);
					if (writer.toLowerCase().contains(search.toLowerCase()))
						letters.add(letter);
				}
			}
		}
		else
			letters = getPlayerMails(activeChar.getObjectId());
		
		final int countMails = getCountLetters(activeChar.getObjectId(), type, sType, search);
		final int maxpage = getMaxPageId(countMails);
		
		if (page > maxpage)
			page = maxpage;
		if (page < 1)
			page = 1;
		
		activeChar.setMailPosition(page);
		int index = 0, minIndex = 0, maxIndex = 0;
		maxIndex = (page == 1 ? page * 9 : (page * 10) - 1);
		minIndex = maxIndex - 9;
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + "mail/mail.htm");
		content = content.replace("%inbox%", Integer.toString(getCountLetters(activeChar.getObjectId(), MailType.INBOX, "", "")));
		content = content.replace("%sentbox%", Integer.toString(getCountLetters(activeChar.getObjectId(), MailType.SENTBOX, "", "")));
		content = content.replace("%archive%", Integer.toString(getCountLetters(activeChar.getObjectId(), MailType.ARCHIVE, "", "")));
		content = content.replace("%temparchive%", Integer.toString(getCountLetters(activeChar.getObjectId(), MailType.TEMPARCHIVE, "", "")));
		content = content.replace("%type%", type.getDescription());
		content = content.replace("%htype%", type.toString().toLowerCase());
		
		final StringBuilder sb = new StringBuilder();
		for (Mail letter : letters)
		{
			if (letter.location.equals(type))
			{
				if (index < minIndex)
				{
					index++;
					continue;
				}
				
				if (index > maxIndex)
					break;
				
				StringUtil.append(sb, "<table width=610><tr><td width=5></td><td width=150>", getCharName(letter.senderId), "</td><td width=300><a action=\"bypass _bbsmail;view;", letter.letterId, "\">");
				
				if (letter.unread)
					sb.append("<font color=\"LEVEL\">");
				
				sb.append(abbreviate(letter.subject, 51));
				
				if (letter.unread)
					sb.append("</font>");
				
				StringUtil.append(sb, "</a></td><td width=150>", letter.sentDateString, "</td><td width=5></td></tr></table><img src=\"L2UI.Squaregray\" width=610 height=1>");
				index++;
			}
		}
		content = content.replace("%maillist%", sb.toString());
		
		// CLeanup sb.
		sb.setLength(0);
		
		final String fullSearch = (!sType.equals("") && !search.equals("")) ? ";" + sType + ";" + search : "";
		
		StringUtil.append(sb, "<td><table><tr><td></td></tr><tr><td><button action=\"bypass _bbsmail;", type, ";", (page == 1 ? page : page - 1), fullSearch, "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td></tr></table></td>");
		
		int i = 0;
		if (maxpage > 21)
		{
			if (page <= 11)
			{
				for (i = 1; i <= (10 + page); i++)
				{
					if (i == page)
						StringUtil.append(sb, "<td> ", i, " </td>");
					else
						StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
				}
			}
			else if (page > 11 && (maxpage - page) > 10)
			{
				for (i = (page - 10); i <= (page - 1); i++)
				{
					if (i == page)
						continue;
					
					StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
				}
				for (i = page; i <= (page + 10); i++)
				{
					if (i == page)
						StringUtil.append(sb, "<td> ", i, " </td>");
					else
						StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
				}
			}
			else if ((maxpage - page) <= 10)
			{
				for (i = (page - 10); i <= maxpage; i++)
				{
					if (i == page)
						StringUtil.append(sb, "<td> ", i, " </td>");
					else
						StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
				}
			}
		}
		else
		{
			for (i = 1; i <= maxpage; i++)
			{
				if (i == page)
					StringUtil.append(sb, "<td> ", i, " </td>");
				else
					StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
			}
		}
		StringUtil.append(sb, "<td><table><tr><td></td></tr><tr><td><button action=\"bypass _bbsmail;", type, ";", (page == maxpage ? page : page + 1), fullSearch, "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td></tr></table></td>");
		
		content = content.replace("%maillistlength%", sb.toString());
		
		separateAndSend(content, activeChar);
	}
	
	private void showLetterView(Player activeChar, Mail letter)
	{
		if (letter == null)
		{
			showMailList(activeChar, 1, MailType.INBOX);
			return;
		}
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + "mail/mail-show.htm");
		
		String link = letter.location.getBypass() + "&nbsp;&gt;&nbsp;" + letter.subject;
		content = content.replace("%maillink%", link);
		
		content = content.replace("%writer%", getCharName(letter.senderId));
		content = content.replace("%sentDate%", letter.sentDateString);
		content = content.replace("%receiver%", letter.recipientNames);
		content = content.replace("%delDate%", "Unknown");
		content = content.replace("%title%", letter.subject.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
		content = content.replace("%mes%", letter.message.replaceAll("\r\n", "<br>").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
		content = content.replace("%letterId%", letter.letterId + "");
		separateAndSend(content, activeChar);
	}
	
	private static void showWriteView(Player activeChar)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "mail/mail-write.htm");
		separateAndSend(content, activeChar);
	}
	
	private static void showWriteView(Player activeChar, String parcipientName, Mail letter)
	{
		String content = HtmCache.getInstance().getHtm(CB_PATH + "mail/mail-reply.htm");
		
		String link = letter.location.getBypass() + "&nbsp;&gt;&nbsp;<a action=\"bypass _bbsmail;view;" + letter.letterId + "\">" + letter.subject + "</a>&nbsp;&gt;&nbsp;";
		content = content.replace("%maillink%", link);
		
		content = content.replace("%recipients%", letter.senderId == activeChar.getObjectId() ? letter.recipientNames : getCharName(letter.senderId));
		content = content.replace("%letterId%", letter.letterId + "");
		send1001(content, activeChar);
		send1002(activeChar, " ", "Re: " + letter.subject, "0");
	}
	
	public void sendLetter(String recipients, String subject, String message, Player activeChar)
	{
		// Current time.
		final long currentDate = Calendar.getInstance().getTimeInMillis();
		
		// Get the current time - 1 day under timestamp format.
		final Timestamp ts = new Timestamp(currentDate - 86400000L);
		
		// Check sender mails based on previous timestamp. If more than 10 mails have been found for today, then cancel the use.
		if (getPlayerMails(activeChar.getObjectId()).stream().filter(l -> l.sentDate.after(ts) && l.location == MailType.SENTBOX).count() >= 10)
		{
			activeChar.sendPacket(SystemMessageId.NO_MORE_MESSAGES_TODAY);
			return;
		}
		
		// Format recipient names. If more than 5 are found, cancel the mail.
		final String[] recipientNames = recipients.trim().split(";");
		if (recipientNames.length > 5 && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_FIVE_RECIPIENTS);
			return;
		}
		
		// Edit subject, if none.
		if (subject == null || subject.isEmpty())
			subject = "(no subject)";
		
		// Edit message.
		message = message.replaceAll("\n", "<br1>");
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Get the current time under timestamp format.
			final Timestamp time = new Timestamp(currentDate);
			
			PreparedStatement statement = null;
			
			for (String recipientName : recipientNames)
			{
				// Recipient is an invalid player, or is the sender.
				final int recipientId = PlayerNameTable.getInstance().getPlayerObjectId(recipientName);
				if (recipientId <= 0 || recipientId == activeChar.getObjectId())
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					continue;
				}
				
				final Player recipientPlayer = World.getInstance().getPlayer(recipientId);
				
				if (!activeChar.isGM())
				{
					// Sender is a regular player, while recipient is a GM.
					if (isGM(recipientId))
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MAIL_GM_S1).addString(recipientName));
						continue;
					}
					
					// The recipient is on block mode.
					if (isBlocked(activeChar, recipientId))
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_BLOCKED_YOU_CANNOT_MAIL).addString(recipientName));
						continue;
					}
					
					// The recipient box is already full.
					if (isRecipInboxFull(recipientId))
					{
						activeChar.sendPacket(SystemMessageId.MESSAGE_NOT_SENT);
						if (recipientPlayer != null)
							recipientPlayer.sendPacket(SystemMessageId.MAILBOX_FULL);
						
						continue;
					}
				}
				
				final int id = getNewMailId();
				
				if (statement == null)
				{
					statement = con.prepareStatement(INSERT_NEW_MAIL);
					statement.setInt(3, activeChar.getObjectId());
					statement.setString(4, "inbox");
					statement.setString(5, recipients);
					statement.setString(6, abbreviate(subject, 128));
					statement.setString(7, message);
					statement.setTimestamp(8, time);
					statement.setInt(9, 1);
				}
				statement.setInt(1, recipientId);
				statement.setInt(2, id);
				statement.execute();
				
				final Mail letter = new Mail();
				letter.charId = recipientId;
				letter.letterId = id;
				letter.senderId = activeChar.getObjectId();
				letter.location = MailType.INBOX;
				letter.recipientNames = recipients;
				letter.subject = abbreviate(subject, 128);
				letter.message = message;
				letter.sentDate = time;
				letter.sentDateString = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(letter.sentDate);
				letter.unread = true;
				getPlayerMails(recipientId).add(0, letter);
				
				if (recipientPlayer != null)
				{
					recipientPlayer.sendPacket(SystemMessageId.NEW_MAIL);
					recipientPlayer.sendPacket(new PlaySound("systemmsg_e.1233"));
					recipientPlayer.sendPacket(ExMailArrived.STATIC_PACKET);
				}
			}
			
			// Create a copy into activeChar's sent box, if at least one recipient has been reached.
			if (statement != null)
			{
				final int id = getNewMailId();
				
				statement.setInt(1, activeChar.getObjectId());
				statement.setInt(2, id);
				statement.setString(4, "sentbox");
				statement.setInt(9, 0);
				statement.execute();
				statement.close();
				
				final Mail letter = new Mail();
				letter.charId = activeChar.getObjectId();
				letter.letterId = id;
				letter.senderId = activeChar.getObjectId();
				letter.location = MailType.SENTBOX;
				letter.recipientNames = recipients;
				letter.subject = abbreviate(subject, 128);
				letter.message = message;
				letter.sentDate = time;
				letter.sentDateString = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(letter.sentDate);
				letter.unread = false;
				getPlayerMails(activeChar.getObjectId()).add(0, letter);
				
				activeChar.sendPacket(SystemMessageId.SENT_MAIL);
			}
		}
		catch (Exception e)
		{
			_log.warning("couldnt send letter for " + activeChar.getName() + " " + e.getMessage());
		}
	}
	
	private int getCountLetters(int objId, MailType location, String sType, String search)
	{
		int count = 0;
		if (!sType.equals("") && !search.equals(""))
		{
			boolean byTitle = sType.equalsIgnoreCase("title");
			for (Mail letter : getPlayerMails(objId))
			{
				if (!letter.location.equals(location))
					continue;
				
				if (byTitle && letter.subject.toLowerCase().contains(search.toLowerCase()))
					count++;
				else if (!byTitle)
				{
					String writer = getCharName(letter.senderId);
					if (writer.toLowerCase().contains(search.toLowerCase()))
						count++;
				}
			}
		}
		else
		{
			for (Mail letter : getPlayerMails(objId))
			{
				if (letter.location.equals(location))
					count++;
			}
		}
		return count;
	}
	
	private static boolean isBlocked(Player activeChar, int recipId)
	{
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.getObjectId() == recipId)
			{
				if (BlockList.isInBlockList(player, activeChar))
					return true;
				
				return false;
			}
		}
		return false;
	}
	
	private void deleteLetter(Player activeChar, int letterId)
	{
		for (Mail letter : getPlayerMails(activeChar.getObjectId()))
		{
			if (letter.letterId == letterId)
			{
				getPlayerMails(activeChar.getObjectId()).remove(letter);
				break;
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(DELETE_MAIL);
			statement.setInt(1, letterId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("couldnt delete letter " + letterId + " " + e);
		}
	}
	
	private void setLetterToRead(Player activeChar, int letterId)
	{
		getLetter(activeChar, letterId).unread = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(MARK_MAIL_READ);
			statement.setInt(1, 0);
			statement.setInt(2, letterId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("couldnt set unread to false for " + letterId + " " + e);
		}
	}
	
	private void setLetterLocation(Player activeChar, int letterId, MailType location)
	{
		getLetter(activeChar, letterId).location = location;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SET_LETTER_LOC);
			statement.setString(1, location.toString().toLowerCase());
			statement.setInt(2, letterId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("couldnt set location to false for " + letterId + " " + e);
		}
	}
	
	private static String getCharName(int charId)
	{
		String name = PlayerNameTable.getInstance().getPlayerName(charId);
		return name == null ? "Unknown" : name;
	}
	
	private static boolean isGM(int charId)
	{
		boolean isGM = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT accesslevel FROM characters WHERE obj_Id = ?");
			statement.setInt(1, charId);
			ResultSet result = statement.executeQuery();
			result.next();
			isGM = result.getInt(1) > 0;
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(e.getMessage());
		}
		return isGM;
	}
	
	private boolean isRecipInboxFull(int charId)
	{
		return getCountLetters(charId, MailType.INBOX, "", "") >= 100;
	}
	
	private void showLastForum(Player activeChar)
	{
		final int page = activeChar.getMailPosition() % 1000;
		final int type = activeChar.getMailPosition() / 1000;
		
		showMailList(activeChar, page, MailType.VALUES[type]);
	}
	
	private static int getMaxPageId(int letterCount)
	{
		if (letterCount < 1)
			return 1;
		
		if (letterCount % 10 == 0)
			return letterCount / 10;
		
		return (letterCount / 10) + 1;
	}
	
	private static class SingletonHolder
	{
		protected static final MailBBSManager _instance = new MailBBSManager();
	}
}