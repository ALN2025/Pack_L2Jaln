package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q647_InfluxOfMachines extends Quest
{
	private static final String qn = "Q647_InfluxOfMachines";
	
	// Item
	private static final int DESTROYED_GOLEM_SHARD = 8100;
	
	// NPC
	private static final int GUTENHAGEN = 32069;
	
	public Q647_InfluxOfMachines()
	{
		super(647, "Influx of Machines");
		
		setItemsIds(DESTROYED_GOLEM_SHARD);
		
		addStartNpc(GUTENHAGEN);
		addTalkId(GUTENHAGEN);
		
		for (int i = 22052; i < 22079; i++)
			addKillId(i);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32069-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32069-06.htm"))
		{
			st.takeItems(DESTROYED_GOLEM_SHARD, -1);
			st.giveItems(Rnd.get(4963, 4972), 1);
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
				htmltext = (player.getLevel() < 46) ? "32069-03.htm" : "32069-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32069-04.htm";
				else if (cond == 2)
					htmltext = "32069-05.htm";
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
		
		if (st.dropItems(DESTROYED_GOLEM_SHARD, 1, 500, 300000))
			st.set("cond", "2");
		
		return null;
	}
}