package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.commons.lang.StringUtil;
import com.l2jaln.commons.random.Rnd;

import com.l2jaln.Config;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.pledge.Clan;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q508_AClansReputation extends Quest
{
	private static final String qn = "Q508_AClansReputation";
	
	// NPC
	private static final int SIR_ERIC_RODEMAI = 30868;
	
	// Items
	private static final int NUCLEUS_OF_FLAMESTONE_GIANT = 8494;
	private static final int THEMIS_SCALE = 8277;
	private static final int NUCLEUS_OF_HEKATON_PRIME = 8279;
	private static final int TIPHON_SHARD = 8280;
	private static final int GLAKI_NUCLEUS = 8281;
	private static final int RAHHA_FANG = 8282;
	
	// Min & Max
	private static final int MIN_FLAMESTONE_GIANT = Config.MIN_FLAMESTONE_GIANT;
	private static final int MAX_FLAMESTONE_GIANT = Config.MAX_FLAMESTONE_GIANT;
	
	private static final int MIN_THEMIS_SCALE = Config.MIN_THEMIS_SCALE;
	private static final int MAX_THEMIS_SCALE = Config.MAX_THEMIS_SCALE;
	
	private static final int MIN_HEKATON_PRIME = Config.MIN_HEKATON_PRIME;
	private static final int MAX_HEKATON_PRIME = Config.MAX_HEKATON_PRIME;
	
	private static final int MIN_GARGOYLE = Config.MIN_GARGOYLE;
	private static final int MAX_GARGOYLE = Config.MAX_GARGOYLE;
	
	private static final int MIN_GLAKI = Config.MIN_GLAKI;
	private static final int MAX_GLAKI = Config.MAX_GLAKI;
	
	private static final int MIN_RAHHA = Config.MIN_RAHHA;
	private static final int MAX_RAHHA = Config.MAX_RAHHA;
	
	// Raidbosses
	private static final int FLAMESTONE_GIANT = 25524;
	private static final int PALIBATI_QUEEN_THEMIS = 25252;
	private static final int HEKATON_PRIME = 25140;
	private static final int GARGOYLE_LORD_TIPHON = 25255;
	private static final int LAST_LESSER_GIANT_GLAKI = 25245;
	private static final int RAHHA = 25051;
	
	// Reward list (itemId, minClanPoints, maxClanPoints)
	private static final int reward_list[][] =
	{
		{
			PALIBATI_QUEEN_THEMIS,
			THEMIS_SCALE,
			MIN_THEMIS_SCALE,
			MAX_THEMIS_SCALE
		},
		{
			HEKATON_PRIME,
			NUCLEUS_OF_HEKATON_PRIME,
			MIN_HEKATON_PRIME,
			MAX_HEKATON_PRIME
		},
		{
			GARGOYLE_LORD_TIPHON,
			TIPHON_SHARD,
			MIN_GARGOYLE,
			MAX_GARGOYLE
		},
		{
			LAST_LESSER_GIANT_GLAKI,
			GLAKI_NUCLEUS,
			MIN_GLAKI,
			MAX_GLAKI
		},
		{
			RAHHA,
			RAHHA_FANG,
			MIN_RAHHA,
			MAX_RAHHA
		},
		{
			FLAMESTONE_GIANT,
			NUCLEUS_OF_FLAMESTONE_GIANT,
			MIN_FLAMESTONE_GIANT,
			MAX_FLAMESTONE_GIANT
		}
	};
	
	// Radar
	private static final int radar[][] =
	{
		{
			192346,
			21528,
			-3648
		},
		{
			191979,
			54902,
			-7658
		},
		{
			170038,
			-26236,
			-3824
		},
		{
			171762,
			55028,
			-5992
		},
		{
			117232,
			-9476,
			-3320
		},
		{
			144218,
			-5816,
			-4722
		}
	};
	
	public Q508_AClansReputation()
	{
		super(508, "A Clan's Reputation");
		
		setItemsIds(THEMIS_SCALE, NUCLEUS_OF_HEKATON_PRIME, TIPHON_SHARD, GLAKI_NUCLEUS, RAHHA_FANG, NUCLEUS_OF_FLAMESTONE_GIANT);
		
		addStartNpc(SIR_ERIC_RODEMAI);
		addTalkId(SIR_ERIC_RODEMAI);
		
		addKillId(FLAMESTONE_GIANT, PALIBATI_QUEEN_THEMIS, HEKATON_PRIME, GARGOYLE_LORD_TIPHON, LAST_LESSER_GIANT_GLAKI, RAHHA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (StringUtil.isDigit(event))
		{
			htmltext = "30868-" + event + ".htm";
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("raid", event);
			st.playSound(QuestState.SOUND_ACCEPT);
			
			int evt = Integer.parseInt(event);
			
			int x = radar[evt - 1][0];
			int y = radar[evt - 1][1];
			int z = radar[evt - 1][2];
			
			if (x + y + z > 0)
				st.addRadar(x, y, z);
		}
		else if (event.equalsIgnoreCase("30868-7.htm"))
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
		
		Clan clan = player.getClan();
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (!player.isClanLeader())
					htmltext = "30868-0a.htm";
				else if (clan.getLevel() < 5)
					htmltext = "30868-0b.htm";
				else
					htmltext = "30868-0c.htm";
				break;
			
			case STATE_STARTED:
				final int raid = st.getInt("raid");
				final int item = reward_list[raid - 1][1];
				
				if (!st.hasQuestItems(item))
					htmltext = "30868-" + raid + "a.htm";
				else
				{
					final int reward = Rnd.get(reward_list[raid - 1][2], reward_list[raid - 1][3]);
					
					htmltext = "30868-" + raid + "b.htm";
					st.takeItems(item, 1);
					clan.addReputationScore(reward);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(reward));
					clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		// Retrieve the qS of the clan leader.
		QuestState st = getClanLeaderQuestState(player, npc);
		if (st == null || !st.isStarted())
			return null;
		
		// Reward only if quest is setup on good index.
		final int raid = st.getInt("raid");
		if (reward_list[raid - 1][0] == npc.getNpcId())
			st.dropItemsAlways(reward_list[raid - 1][1], 1, 1);
		
		return null;
	}
}