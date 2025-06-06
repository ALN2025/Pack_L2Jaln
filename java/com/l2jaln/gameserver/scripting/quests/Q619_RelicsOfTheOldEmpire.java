package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q619_RelicsOfTheOldEmpire extends Quest
{
	private static final String qn = "Q619_RelicsOfTheOldEmpire";
	
	// NPC
	private static int GHOST_OF_ADVENTURER = 31538;
	
	// Items
	private static int RELICS = 7254;
	private static int ENTRANCE = 7075;
	
	// Rewards ; all S grade weapons recipe (60%)
	private static int[] RCP_REWARDS = new int[]
	{
		6881,
		6883,
		6885,
		6887,
		6891,
		6893,
		6895,
		6897,
		6899,
		7580
	};
	
	public Q619_RelicsOfTheOldEmpire()
	{
		super(619, "Relics of the Old Empire");
		
		setItemsIds(RELICS);
		
		addStartNpc(GHOST_OF_ADVENTURER);
		addTalkId(GHOST_OF_ADVENTURER);
		
		for (int id = 21396; id <= 21434; id++)
			// IT monsters
			addKillId(id);
		
		// monsters at IT entrance
		addKillId(21798, 21799, 21800);
		
		for (int id = 18120; id <= 18256; id++)
			// Sepulchers monsters
			addKillId(id);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31538-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31538-09.htm"))
		{
			if (st.getQuestItemsCount(RELICS) >= 1000)
			{
				htmltext = "31538-09.htm";
				st.takeItems(RELICS, 1000);
				st.giveItems(RCP_REWARDS[Rnd.get(RCP_REWARDS.length)], 1);
			}
			else
				htmltext = "31538-06.htm";
		}
		else if (event.equalsIgnoreCase("31538-10.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 74) ? "31538-02.htm" : "31538-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(RELICS) >= 1000)
					htmltext = "31538-04.htm";
				else if (st.hasQuestItems(ENTRANCE))
					htmltext = "31538-06.htm";
				else
					htmltext = "31538-07.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		Player partyMember = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		st.dropItemsAlways(RELICS, 1, 0);
		st.dropItems(ENTRANCE, 1, 0, 50000);
		
		return null;
	}
}