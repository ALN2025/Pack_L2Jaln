package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q614_SlayTheEnemyCommander extends Quest
{
	private static final String qn = "Q614_SlayTheEnemyCommander";
	
	// Quest Items
	private static final int HEAD_OF_TAYR = 7241;
	private static final int FEATHER_OF_WISDOM = 7230;
	private static final int VARKA_ALLIANCE_4 = 7224;
	
	public Q614_SlayTheEnemyCommander()
	{
		super(614, "Slay the enemy commander!");
		
		setItemsIds(HEAD_OF_TAYR);
		
		addStartNpc(31377); // Ashas Varka Durai
		addTalkId(31377);
		
		addKillId(25302); // Tayr
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
			if (st.hasQuestItems(HEAD_OF_TAYR))
			{
				st.takeItems(HEAD_OF_TAYR, -1);
				st.giveItems(FEATHER_OF_WISDOM, 1);
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
				if (player.getLevel() >= 75)
				{
					if (player.getAllianceWithVarkaKetra() <= -4 && st.hasQuestItems(VARKA_ALLIANCE_4) && !st.hasQuestItems(FEATHER_OF_WISDOM))
						htmltext = "31377-01.htm";
					else
						htmltext = "31377-02.htm";
				}
				else
					htmltext = "31377-03.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.hasQuestItems(HEAD_OF_TAYR)) ? "31377-05.htm" : "31377-06.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		for (Player partyMember : getPartyMembers(player, npc, "cond", "1"))
		{
			if (partyMember.getAllianceWithVarkaKetra() <= -4)
			{
				QuestState st = partyMember.getQuestState(qn);
				if (st.hasQuestItems(VARKA_ALLIANCE_4))
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.giveItems(HEAD_OF_TAYR, 1);
				}
			}
		}
		
		return null;
	}
}