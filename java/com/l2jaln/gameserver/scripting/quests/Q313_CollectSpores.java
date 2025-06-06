package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q313_CollectSpores extends Quest
{
	private static final String qn = "Q313_CollectSpores";
	
	// Item
	private static final int SPORE_SAC = 1118;
	
	public Q313_CollectSpores()
	{
		super(313, "Collect Spores");
		
		setItemsIds(SPORE_SAC);
		
		addStartNpc(30150); // Herbiel
		addTalkId(30150);
		
		addKillId(20509); // Spore Fungus
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30150-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				htmltext = (player.getLevel() < 8) ? "30150-02.htm" : "30150-03.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "30150-06.htm";
				else
				{
					htmltext = "30150-07.htm";
					st.takeItems(SPORE_SAC, -1);
					st.rewardItems(57, 3500);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(SPORE_SAC, 1, 10, 400000))
			st.set("cond", "2");
		
		return null;
	}
}