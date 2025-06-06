package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q432_BirthdayPartySong extends Quest
{
	private static final String qn = "Q432_BirthdayPartySong";
	
	// NPC
	private static final int OCTAVIA = 31043;
	
	// Item
	private static final int RED_CRYSTAL = 7541;
	
	public Q432_BirthdayPartySong()
	{
		super(432, "Birthday Party Song");
		
		setItemsIds(RED_CRYSTAL);
		
		addStartNpc(OCTAVIA);
		addTalkId(OCTAVIA);
		
		addKillId(21103);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31043-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31043-06.htm"))
		{
			if (st.getQuestItemsCount(RED_CRYSTAL) == 50)
			{
				htmltext = "31043-05.htm";
				st.takeItems(RED_CRYSTAL, -1);
				st.rewardItems(7061, 25);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
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
				htmltext = (player.getLevel() < 31) ? "31043-00.htm" : "31043-01.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.getQuestItemsCount(RED_CRYSTAL) < 50) ? "31043-03.htm" : "31043-04.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		Player partyMember = getRandomPartyMember(player, npc, "1");
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st.dropItems(RED_CRYSTAL, 1, 50, 500000))
			st.set("cond", "2");
		
		return null;
	}
}