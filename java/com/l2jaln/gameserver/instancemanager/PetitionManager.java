package com.l2jaln.gameserver.instancemanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.Config;
import com.l2jaln.gameserver.data.xml.AdminData;
import com.l2jaln.gameserver.idfactory.IdFactory;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.clientpackets.Say2;
import com.l2jaln.gameserver.network.serverpackets.CreatureSay;
import com.l2jaln.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jaln.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;

public final class PetitionManager
{
	protected static final Logger _log = Logger.getLogger(PetitionManager.class.getName());
	
	private final Map<Integer, Petition> _pendingPetitions;
	private final Map<Integer, Petition> _completedPetitions;
	
	private static enum PetitionState
	{
		Pending,
		Responder_Cancel,
		Responder_Missing,
		Responder_Reject,
		Responder_Complete,
		Petitioner_Cancel,
		Petitioner_Missing,
		In_Process,
		Completed
	}
	
	private static enum PetitionType
	{
		Immobility,
		Recovery_Related,
		Bug_Report,
		Quest_Related,
		Bad_User,
		Suggestions,
		Game_Tip,
		Operation_Related,
		Other
	}
	
	public static PetitionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private class Petition
	{
		private final long _submitTime = System.currentTimeMillis();
		
		private final int _id;
		private final PetitionType _type;
		private PetitionState _state = PetitionState.Pending;
		private final String _content;
		
		private final List<CreatureSay> _messageLog = new ArrayList<>();
		
		private final Player _petitioner;
		private Player _responder;
		
		public Petition(Player petitioner, String petitionText, int petitionType)
		{
			petitionType--;
			_id = IdFactory.getInstance().getNextId();
			if (petitionType >= PetitionType.values().length)
				_log.warning("PetitionManager: invalid petition type (received type was +1) : " + petitionType);
			
			_type = PetitionType.values()[petitionType];
			_content = petitionText;
			_petitioner = petitioner;
		}
		
		protected boolean addLogMessage(CreatureSay cs)
		{
			return _messageLog.add(cs);
		}
		
		protected List<CreatureSay> getLogMessages()
		{
			return _messageLog;
		}
		
		public boolean endPetitionConsultation(PetitionState endState)
		{
			setState(endState);
			
			if (getResponder() != null && getResponder().isOnline())
			{
				if (endState == PetitionState.Responder_Reject)
					getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
				else
				{
					// Ending petition consultation with <Player>.
					getResponder().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_ENDED_WITH_S1).addCharName(getPetitioner()));
					
					// Receipt No. <ID> petition cancelled.
					if (endState == PetitionState.Petitioner_Cancel)
						getResponder().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RECENT_NO_S1_CANCELED).addNumber(getId()));
				}
			}
			
			// End petition consultation and inform them, if they are still online.
			if (getPetitioner() != null && getPetitioner().isOnline())
				getPetitioner().sendPacket(SystemMessageId.THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK);
			
			getCompletedPetitions().put(getId(), this);
			return (getPendingPetitions().remove(getId()) != null);
		}
		
		public String getContent()
		{
			return _content;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public Player getPetitioner()
		{
			return _petitioner;
		}
		
		public Player getResponder()
		{
			return _responder;
		}
		
		public long getSubmitTime()
		{
			return _submitTime;
		}
		
		public PetitionState getState()
		{
			return _state;
		}
		
		public String getTypeAsString()
		{
			return _type.toString().replace("_", " ");
		}
		
		public void sendPetitionerPacket(L2GameServerPacket responsePacket)
		{
			if (getPetitioner() == null || !getPetitioner().isOnline())
				return;
			
			getPetitioner().sendPacket(responsePacket);
		}
		
		public void sendResponderPacket(L2GameServerPacket responsePacket)
		{
			if (getResponder() == null || !getResponder().isOnline())
			{
				endPetitionConsultation(PetitionState.Responder_Missing);
				return;
			}
			
			getResponder().sendPacket(responsePacket);
		}
		
		public void setState(PetitionState state)
		{
			_state = state;
		}
		
		public void setResponder(Player respondingAdmin)
		{
			if (getResponder() != null)
				return;
			
			_responder = respondingAdmin;
		}
	}
	
	protected PetitionManager()
	{
		_pendingPetitions = new HashMap<>();
		_completedPetitions = new HashMap<>();
	}
	
	public void clearCompletedPetitions()
	{
		int numPetitions = getPendingPetitionCount();
		
		getCompletedPetitions().clear();
		_log.info("PetitionManager: Completed petition data cleared. " + numPetitions + " petition(s) removed.");
	}
	
	public void clearPendingPetitions()
	{
		int numPetitions = getPendingPetitionCount();
		
		getPendingPetitions().clear();
		_log.info("PetitionManager: Pending petition queue cleared. " + numPetitions + " petition(s) removed.");
	}
	
	public boolean acceptPetition(Player respondingAdmin, int petitionId)
	{
		if (!isValidPetition(petitionId))
			return false;
		
		Petition currPetition = getPendingPetitions().get(petitionId);
		
		if (currPetition.getResponder() != null)
			return false;
		
		currPetition.setResponder(respondingAdmin);
		currPetition.setState(PetitionState.In_Process);
		
		// Petition application accepted. (Send to Petitioner)
		currPetition.sendPetitionerPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_APP_ACCEPTED));
		
		// Petition application accepted. Reciept No. is <ID>
		currPetition.sendResponderPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1).addNumber(currPetition.getId()));
		
		// Petition consultation with <Player> underway.
		currPetition.sendResponderPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_WITH_S1_UNDER_WAY).addCharName(currPetition.getPetitioner()));
		return true;
	}
	
	public boolean cancelActivePetition(Player player)
	{
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.Petitioner_Cancel));
			
			if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.Responder_Cancel));
		}
		
		return false;
	}
	
	public void checkPetitionMessages(Player petitioner)
	{
		if (petitioner != null)
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
					continue;
				
				if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId())
				{
					for (CreatureSay logMessage : currPetition.getLogMessages())
						petitioner.sendPacket(logMessage);
					
					return;
				}
			}
	}
	
	public boolean endActivePetition(Player player)
	{
		if (!player.isGM())
			return false;
		
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;
			
			if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
				return (currPetition.endPetitionConsultation(PetitionState.Completed));
		}
		
		return false;
	}
	
	protected Map<Integer, Petition> getCompletedPetitions()
	{
		return _completedPetitions;
	}
	
	protected Map<Integer, Petition> getPendingPetitions()
	{
		return _pendingPetitions;
	}
	
	public int getPendingPetitionCount()
	{
		return getPendingPetitions().size();
	}
	
	public int getPlayerTotalPetitionCount(Player player)
	{
		if (player == null)
			return 0;
		
		int petitionCount = 0;
		
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;
			
			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
				petitionCount++;
		}
		
		for (Petition currPetition : getCompletedPetitions().values())
		{
			if (currPetition == null)
				continue;
			
			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
				petitionCount++;
		}
		
		return petitionCount;
	}
	
	public boolean isPetitionInProcess()
	{
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;
			
			if (currPetition.getState() == PetitionState.In_Process)
				return true;
		}
		
		return false;
	}
	
	public boolean isPetitionInProcess(int petitionId)
	{
		if (!isValidPetition(petitionId))
			return false;
		
		Petition currPetition = getPendingPetitions().get(petitionId);
		return (currPetition.getState() == PetitionState.In_Process);
	}
	
	public boolean isPlayerInConsultation(Player player)
	{
		if (player != null)
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
					continue;
				
				if (currPetition.getState() != PetitionState.In_Process)
					continue;
				
				if ((currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId()) || (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId()))
					return true;
			}
		
		return false;
	}
	
	public static boolean isPetitioningAllowed()
	{
		return Config.PETITIONING_ALLOWED;
	}
	
	public boolean isPlayerPetitionPending(Player petitioner)
	{
		if (petitioner != null)
			for (Petition currPetition : getPendingPetitions().values())
			{
				if (currPetition == null)
					continue;
				
				if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId())
					return true;
			}
		
		return false;
	}
	
	private boolean isValidPetition(int petitionId)
	{
		return getPendingPetitions().containsKey(petitionId);
	}
	
	public boolean rejectPetition(Player respondingAdmin, int petitionId)
	{
		if (!isValidPetition(petitionId))
			return false;
		
		Petition currPetition = getPendingPetitions().get(petitionId);
		
		if (currPetition.getResponder() != null)
			return false;
		
		currPetition.setResponder(respondingAdmin);
		return (currPetition.endPetitionConsultation(PetitionState.Responder_Reject));
	}
	
	public boolean sendActivePetitionMessage(Player player, String messageText)
	{
		// if (!isPlayerInConsultation(player))
		// return false;
		
		CreatureSay cs;
		
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;
			
			if (currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
			{
				cs = new CreatureSay(player.getObjectId(), Say2.PETITION_PLAYER, player.getName(), messageText);
				currPetition.addLogMessage(cs);
				
				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}
			
			if (currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
			{
				cs = new CreatureSay(player.getObjectId(), Say2.PETITION_GM, player.getName(), messageText);
				currPetition.addLogMessage(cs);
				
				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}
		}
		
		return false;
	}
	
	public void sendPendingPetitionList(Player activeChar)
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		final StringBuilder sb = new StringBuilder("<html><body><center><font color=\"LEVEL\">Current Petitions</font><br><table width=\"300\">");
		
		if (getPendingPetitionCount() == 0)
			sb.append("<tr><td colspan=\"4\">There are no currently pending petitions.</td></tr>");
		else
			sb.append("<tr><td></td><td><font color=\"999999\">Petitioner</font></td><td><font color=\"999999\">Petition Type</font></td><td><font color=\"999999\">Submitted</font></td></tr>");
		
		for (Petition currPetition : getPendingPetitions().values())
		{
			if (currPetition == null)
				continue;
			
			sb.append("<tr><td>");
			
			if (currPetition.getState() != PetitionState.In_Process)
				StringUtil.append(sb, "<button value=\"View\" action=\"bypass -h admin_view_petition ", currPetition.getId(), "\" width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			else
				sb.append("<font color=\"999999\">In Process</font>");
			
			StringUtil.append(sb, "</td><td>", currPetition.getPetitioner().getName(), "</td><td>", currPetition.getTypeAsString(), "</td><td>", sdf.format(currPetition.getSubmitTime()), "</td></tr>");
		}
		
		sb.append("</table><br><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"50\" " + "height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><button value=\"Back\" action=\"bypass -h admin_admin\" " + "width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	public int submitPetition(Player petitioner, String petitionText, int petitionType)
	{
		// Create a new petition instance and add it to the list of pending petitions.
		Petition newPetition = new Petition(petitioner, petitionText, petitionType);
		int newPetitionId = newPetition.getId();
		getPendingPetitions().put(newPetitionId, newPetition);
		
		// Notify all GMs that a new petition has been submitted.
		String msgContent = petitioner.getName() + " has submitted a new petition."; // (ID: " + newPetitionId + ").";
		AdminData.getInstance().broadcastToGMs(new CreatureSay(petitioner.getObjectId(), 17, "Petition System", msgContent));
		
		return newPetitionId;
	}
	
	public void viewPetition(Player activeChar, int petitionId)
	{
		if (!activeChar.isGM())
			return;
		
		if (!isValidPetition(petitionId))
			return;
		
		Petition currPetition = getPendingPetitions().get(petitionId);
		final StringBuilder sb = new StringBuilder("<html><body>");
		
		sb.append("<center><br><font color=\"LEVEL\">Petition #" + currPetition.getId() + "</font><br1>");
		sb.append("<img src=\"L2UI.SquareGray\" width=\"200\" height=\"1\"></center><br>");
		sb.append("Submit Time: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(currPetition.getSubmitTime()) + "<br1>");
		sb.append("Petitioner: " + currPetition.getPetitioner().getName() + "<br1>");
		sb.append("Petition Type: " + currPetition.getTypeAsString() + "<br>" + currPetition.getContent() + "<br>");
		sb.append("<center><button value=\"Accept\" action=\"bypass -h admin_accept_petition " + currPetition.getId() + "\"" + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1>");
		sb.append("<button value=\"Reject\" action=\"bypass -h admin_reject_petition " + currPetition.getId() + "\" " + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		sb.append("<button value=\"Back\" action=\"bypass -h admin_view_petitions\" width=\"40\" height=\"15\" back=\"sek.cbui94\" " + "fore=\"sek.cbui92\"></center>");
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static class SingletonHolder
	{
		protected static final PetitionManager _instance = new PetitionManager();
	}
}