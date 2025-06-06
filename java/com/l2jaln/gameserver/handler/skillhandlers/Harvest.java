package com.l2jaln.gameserver.handler.skillhandlers;

import java.util.List;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.gameserver.handler.ISkillHandler;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.instance.Monster;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.holder.IntIntHolder;
import com.l2jaln.gameserver.model.item.instance.ItemInstance;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.templates.skills.L2SkillType;

/**
 * @author l3x
 */
public class Harvest implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HARVEST
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		final WorldObject object = targets[0];
		if (!(object instanceof Monster))
			return;
		
		final Player player = (Player) activeChar;
		final Monster target = (Monster) object;
		
		if (player.getObjectId() != target.getSeederId())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
			return;
		}
		
		boolean send = false;
		int total = 0;
		int cropId = 0;
		
		if (target.isSeeded())
		{
			if (calcSuccess(player, target))
			{
				final List<IntIntHolder> items = target.getHarvestItems();
				if (!items.isEmpty())
				{
					InventoryUpdate iu = new InventoryUpdate();
					for (IntIntHolder ritem : items)
					{
						cropId = ritem.getId(); // always got 1 type of crop as reward
						
						if (player.isInParty())
							player.getParty().distributeItem(player, ritem, true, target);
						else
						{
							ItemInstance item = player.getInventory().addItem("Manor", ritem.getId(), ritem.getValue(), player, target);
							iu.addItem(item);
							
							send = true;
							total += ritem.getValue();
						}
					}
					
					if (send)
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(cropId).addNumber(total));
						
						if (player.isInParty())
							player.getParty().broadcastToPartyMembers(player, SystemMessage.getSystemMessage(SystemMessageId.S1_HARVESTED_S3_S2S).addCharName(player).addItemName(cropId).addNumber(total));
						
						player.sendPacket(iu);
					}
					items.clear();
				}
			}
			else
				player.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED);
		}
		else
			player.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
	}
	
	private static boolean calcSuccess(Creature activeChar, Creature target)
	{
		int basicSuccess = 100;
		final int levelPlayer = activeChar.getLevel();
		final int levelTarget = target.getLevel();
		
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
			diff = -diff;
		
		// apply penalty, target <=> player levels, 5% penalty for each level
		if (diff > 5)
			basicSuccess -= (diff - 5) * 5;
		
		// success rate cant be less than 1%
		if (basicSuccess < 1)
			basicSuccess = 1;
		
		return Rnd.get(99) < basicSuccess;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}