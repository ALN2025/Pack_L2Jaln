package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q638_SeekersOfTheHolyGrail extends Quest
{
	private static final String qn = "Q638_SeekersOfTheHolyGrail";
	
	// NPC
	private static final int INNOCENTIN = 31328;
	
	// Item
	private static final int PAGAN_TOTEM = 8068;
	
	public Q638_SeekersOfTheHolyGrail()
	{
		super(638, "Seekers of the Holy Grail");
		
		setItemsIds(PAGAN_TOTEM);
		
		addStartNpc(INNOCENTIN);
		addTalkId(INNOCENTIN);
		
		for (int i = 22138; i < 22175; i++)
			addKillId(i);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31328-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31328-06.htm"))
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
				htmltext = (player.getLevel() < 73) ? "31328-00.htm" : "31328-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(PAGAN_TOTEM) >= 2000)
				{
					htmltext = "31328-03.htm";
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(PAGAN_TOTEM, 2000);
					
					int chance = Rnd.get(3);
					if (chance == 0)
						st.rewardItems(959, 1);
					else if (chance == 1)
						st.rewardItems(960, 1);
					else
						st.rewardItems(57, 3576000);
				}
				else
					htmltext = "31328-04.htm";
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
		
		partyMember.getQuestState(qn).dropItemsAlways(PAGAN_TOTEM, 1, 0);
		
		return null;
	}
}