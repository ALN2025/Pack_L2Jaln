package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q352_HelpRoodRaiseANewPet extends Quest
{
	private static final String qn = "Q352_HelpRoodRaiseANewPet";
	
	// Items
	private static final int LIENRIK_EGG_1 = 5860;
	private static final int LIENRIK_EGG_2 = 5861;
	
	public Q352_HelpRoodRaiseANewPet()
	{
		super(352, "Help Rood Raise A New Pet!");
		
		setItemsIds(LIENRIK_EGG_1, LIENRIK_EGG_2);
		
		addStartNpc(31067); // Rood
		addTalkId(31067);
		
		addKillId(20786, 20787, 21644, 21645);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31067-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31067-09.htm"))
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
				htmltext = (player.getLevel() < 39) ? "31067-00.htm" : "31067-01.htm";
				break;
			
			case STATE_STARTED:
				final int eggs1 = st.getQuestItemsCount(LIENRIK_EGG_1);
				final int eggs2 = st.getQuestItemsCount(LIENRIK_EGG_2);
				
				if (eggs1 + eggs2 == 0)
					htmltext = "31067-05.htm";
				else
				{
					int reward = 2000;
					if (eggs1 > 0 && eggs2 == 0)
					{
						htmltext = "31067-06.htm";
						reward += eggs1 * 34;
						
						st.takeItems(LIENRIK_EGG_1, -1);
						st.rewardItems(57, reward);
					}
					else if (eggs1 == 0 && eggs2 > 0)
					{
						htmltext = "31067-08.htm";
						reward += eggs2 * 1025;
						
						st.takeItems(LIENRIK_EGG_2, -1);
						st.rewardItems(57, reward);
					}
					else if (eggs1 > 0 && eggs2 > 0)
					{
						htmltext = "31067-08.htm";
						reward += (eggs1 * 34) + (eggs2 * 1025) + 2000;
						
						st.takeItems(LIENRIK_EGG_1, -1);
						st.takeItems(LIENRIK_EGG_2, -1);
						st.rewardItems(57, reward);
					}
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int npcId = npc.getNpcId();
		final int random = Rnd.get(100);
		final int chance = (npcId == 20786 || npcId == 21644) ? 44 : 58;
		
		if (random < chance)
			st.dropItemsAlways(LIENRIK_EGG_1, 1, 0);
		else if (random < (chance + 4))
			st.dropItemsAlways(LIENRIK_EGG_2, 1, 0);
		
		return null;
	}
}