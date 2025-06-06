package com.l2jaln.gameserver.scripting.scripts.custom;

import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class HeroCirclet extends Quest
{
	public HeroCirclet()
	{
		super(-1, "custom");
		
		addStartNpc(31690, 31769, 31770, 31771, 31772);
		addTalkId(31690, 31769, 31770, 31771, 31772);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
		if (player.isHero())
		{
			if (player.getInventory().getItemByItemId(6842) == null)
				st.giveItems(6842, 1);
			else
				htmltext = "already_have_circlet.htm";
		}
		else
			htmltext = "no_hero.htm";
		
		st.exitQuest(true);
		return htmltext;
	}
}