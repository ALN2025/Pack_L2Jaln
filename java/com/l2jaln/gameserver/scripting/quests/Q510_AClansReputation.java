package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.pledge.Clan;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q510_AClansReputation extends Quest
{
	private static final String qn = "Q510_AClansReputation";
	
	// NPC
	private static final int VALDIS = 31331;
	
	// Quest Item
	private static final int TYRANNOSAURUS_CLAW = 8767;
	
	public Q510_AClansReputation()
	{
		super(510, "A Clan's Reputation");
		
		setItemsIds(TYRANNOSAURUS_CLAW);
		
		addStartNpc(VALDIS);
		addTalkId(VALDIS);
		
		addKillId(22215, 22216, 22217);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31331-3.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31331-6.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = (!player.isClanLeader() || player.getClan().getLevel() < 5) ? "31331-0.htm" : "31331-1.htm";
				break;
			
			case STATE_STARTED:
				final int count = 50 * st.getQuestItemsCount(TYRANNOSAURUS_CLAW);
				if (count > 0)
				{
					final Clan clan = player.getClan();
					
					htmltext = "31331-7.htm";
					st.takeItems(TYRANNOSAURUS_CLAW, -1);
					
					clan.addReputationScore(count);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(count));
					clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				}
				else
					htmltext = "31331-4.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		// Retrieve the qs of the clan leader.
		QuestState st = getClanLeaderQuestState(player, npc);
		if (st == null || !st.isStarted())
			return null;
		
		st.dropItemsAlways(TYRANNOSAURUS_CLAW, 1, 0);
		
		return null;
	}
}