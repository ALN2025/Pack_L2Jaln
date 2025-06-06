package com.l2jaln.gameserver.scripting.quests;

import com.l2jaln.commons.random.Rnd;
import com.l2jaln.commons.util.ArraysUtil;

import com.l2jaln.gameserver.data.xml.SoulCrystalData;
import com.l2jaln.gameserver.model.AbsorbInfo;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Attackable;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.item.instance.ItemInstance;
import com.l2jaln.gameserver.model.soulcrystal.LevelingInfo;
import com.l2jaln.gameserver.model.soulcrystal.SoulCrystal;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.scripting.QuestState;

public class Q350_EnhanceYourWeapon extends Quest
{
	private static final String qn = "Q350_EnhanceYourWeapon";
	
	public Q350_EnhanceYourWeapon()
	{
		super(350, "Enhance Your Weapon");
		
		addStartNpc(30115, 30194, 30856);
		addTalkId(30115, 30194, 30856);
		
		for (int npcId : SoulCrystalData.getInstance().getLevelingInfos().keySet())
			addKillId(npcId);
		
		for (int crystalId : SoulCrystalData.getInstance().getSoulCrystals().keySet())
			addItemUse(crystalId);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Start the quest.
		if (event.endsWith("-04.htm"))
		{
			st.setState(Quest.STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		// Give Red Soul Crystal.
		else if (event.endsWith("-09.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(4629, 1);
		}
		// Give Green Soul Crystal.
		else if (event.endsWith("-10.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(4640, 1);
		}
		// Give Blue Soul Crystal.
		else if (event.endsWith("-11.htm"))
		{
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(4651, 1);
		}
		// Terminate the quest.
		else if (event.endsWith("-exit.htm"))
			st.exitQuest(true);
		
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
				if (player.getLevel() < 40)
					htmltext = npc.getNpcId() + "-lvl.htm";
				else
					htmltext = npc.getNpcId() + "-01.htm";
				break;
			
			case STATE_STARTED:
				// Check inventory for soul crystals.
				for (ItemInstance item : player.getInventory().getItems())
				{
					// Crystal found, show "how to" html.
					if (SoulCrystalData.getInstance().getSoulCrystals().get(item.getItemId()) != null)
						return npc.getNpcId() + "-03.htm";
				}
				// No crystal found, offer a new crystal.
				htmltext = npc.getNpcId() + "-21.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onItemUse(ItemInstance item, Player user, WorldObject target)
	{
		// Caster is dead.
		if (user.isDead())
			return null;
		
		// No target, or target isn't an L2Attackable.
		if (target == null || !(target instanceof Attackable))
			return null;
		
		final Attackable mob = ((Attackable) target);
		
		// Mob is dead or not registered in _npcInfos.
		if (mob.isDead() || !SoulCrystalData.getInstance().getLevelingInfos().containsKey(mob.getNpcId()))
			return null;
		
		// Add user to mob's absorber list.
		mob.addAbsorber(user, item);
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		// Retrieve individual mob informations.
		final LevelingInfo npcInfo = SoulCrystalData.getInstance().getLevelingInfos().get(npc.getNpcId());
		if (npcInfo == null)
			return null;
		
		final int chance = Rnd.get(1000);
		
		// Handle npc leveling info type.
		switch (npcInfo.getAbsorbCrystalType())
		{
			case FULL_PARTY:
				final Attackable mob = (Attackable) npc;
				
				for (Player player : getPartyMembersState(killer, npc, Quest.STATE_STARTED))
					tryToStageCrystal(player, mob, npcInfo, chance);
				break;
			
			case PARTY_ONE_RANDOM:
				final Player player = getRandomPartyMemberState(killer, npc, Quest.STATE_STARTED);
				if (player != null)
					tryToStageCrystal(player, (Attackable) npc, npcInfo, chance);
				break;
			
			case LAST_HIT:
				if (checkPlayerState(killer, npc, Quest.STATE_STARTED) != null)
					tryToStageCrystal(killer, (Attackable) npc, npcInfo, chance);
				break;
		}
		
		return null;
	}
	
	/**
	 * Define the Soul Crystal and try to stage it. Checks for quest enabled, crystal(s) in inventory, required usage of crystal, mob's ability to level crystal and mob vs player level gap.
	 * @param player : The player to make checks on.
	 * @param mob : The mob to make checks on.
	 * @param npcInfo : The mob's leveling informations.
	 * @param chance : Input variable used to determine keep/stage/break of the crystal.
	 */
	private static void tryToStageCrystal(Player player, Attackable mob, LevelingInfo npcInfo, int chance)
	{
		SoulCrystal crystalData = null;
		ItemInstance crystalItem = null;
		
		// Iterate through player's inventory to find crystal(s).
		for (ItemInstance item : player.getInventory().getItems())
		{
			SoulCrystal data = SoulCrystalData.getInstance().getSoulCrystals().get(item.getItemId());
			if (data == null)
				continue;
			
			// More crystals found.
			if (crystalData != null)
			{
				// Leveling requires soul crystal being used?
				if (npcInfo.isSkillRequired())
				{
					// Absorb list contains killer and his AbsorbInfo is registered.
					final AbsorbInfo ai = mob.getAbsorbInfo(player.getObjectId());
					if (ai != null && ai.isRegistered())
						player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
				}
				else
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
				
				return;
			}
			
			crystalData = data;
			crystalItem = item;
		}
		
		// No crystal found, return without any notification.
		if (crystalData == null || crystalItem == null)
			return;
		
		// Leveling requires soul crystal being used?
		if (npcInfo.isSkillRequired())
		{
			// Absorb list doesn't contain killer or his AbsorbInfo is not registered.
			final AbsorbInfo ai = mob.getAbsorbInfo(player.getObjectId());
			if (ai == null || !ai.isRegistered())
				return;
			
			// Check if Absorb list contains valid crystal and whether it was used properly.
			if (!ai.isValid(crystalItem.getObjectId()))
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
				return;
			}
		}
		
		// Check, if npc stages this type of crystal.
		if (!ArraysUtil.contains(npcInfo.getLevelList(), crystalData.getLevel()))
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
			return;
		}
		
		// Check level difference limitation, dark blue monsters does not stage.
		//if (player.getLevel() - mob.getLevel() > 8)
		//{
		//	player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
		//	return;
		//}
		
		// Lucky, crystal successfully stages.
		if (chance < npcInfo.getChanceStage())
			exchangeCrystal(player, crystalData, true);
		// Bad luck, crystal accidentally breaks.
		else if (chance < (npcInfo.getChanceStage() + npcInfo.getChanceBreak()))
			exchangeCrystal(player, crystalData, false);
		// Bad luck, crystal doesn't stage.
		else
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
	}
	
	/**
	 * Remove the old crystal and add new one if stage, broken crystal if break. Send messages in both cases.
	 * @param player : The player to check on (inventory and send messages).
	 * @param sc : SoulCrystal of to take information form.
	 * @param stage : Switch to determine success or fail.
	 */
	private static void exchangeCrystal(Player player, SoulCrystal sc, boolean stage)
	{
		QuestState st = player.getQuestState(qn);
		
		st.takeItems(sc.getInitialItemId(), 1);
		if (stage)
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
			st.giveItems(sc.getStagedItemId(), 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else
		{
			int broken = sc.getBrokenItemId();
			if (broken != 0)
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_BROKE);
				st.giveItems(broken, 1);
			}
		}
	}
}