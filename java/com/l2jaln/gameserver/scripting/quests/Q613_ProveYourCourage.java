package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q613_ProveYourCourage extends Quest
{
	private static final String qn = "Q613_ProveYourCourage";
	
	// Items
	private static final int HEAD_OF_HEKATON = 7240;
	private static final int FEATHER_OF_VALOR = 7229;
	private static final int VARKA_ALLIANCE_3 = 7223;
	
	public Q613_ProveYourCourage()
	{
		super(613, "Prove your courage!");
		
		setItemsIds(HEAD_OF_HEKATON);
		
		addStartNpc(31377); // Ashas Varka Durai
		addTalkId(31377);
		
		addKillId(25299); // Hekaton
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31377-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31377-07.htm"))
		{
			if (st.hasQuestItems(HEAD_OF_HEKATON))
			{
				st.takeItems(HEAD_OF_HEKATON, -1);
				st.giveItems(FEATHER_OF_VALOR, 1);
				st.rewardExpAndSp(10000, 0);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31377-06.htm";
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getLevel() < 75)
					htmltext = "31377-03.htm";
				else if (player.getAllianceWithVarkaKetra() <= -3 && st.hasQuestItems(VARKA_ALLIANCE_3) && !st.hasQuestItems(FEATHER_OF_VALOR))
					htmltext = "31377-01.htm";
				else
					htmltext = "31377-02.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.hasQuestItems(HEAD_OF_HEKATON)) ? "31377-05.htm" : "31377-06.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		for (Player partyMember : getPartyMembers(player, npc, "cond", "1"))
		{
			if (partyMember.getAllianceWithVarkaKetra() <= -3)
			{
				QuestState st = partyMember.getQuestState(qn);
				if (st.hasQuestItems(VARKA_ALLIANCE_3))
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(HEAD_OF_HEKATON, 1);
				}
			}
		}
		
		return null;
	}
}