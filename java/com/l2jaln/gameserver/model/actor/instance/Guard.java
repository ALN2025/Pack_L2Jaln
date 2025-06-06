package com.l2jaln.gameserver.model.actor.instance;

import java.util.List;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.gameserver.model.actor.Attackable;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.ai.CtrlIntention;
import com.l2jaln.gameserver.model.actor.template.NpcTemplate;
import com.l2jaln.gameserver.network.serverpackets.ActionFailed;
import com.l2jaln.gameserver.network.serverpackets.MoveToPawn;
import com.l2jaln.gameserver.scripting.EventType;
import com.l2jaln.gameserver.scripting.Quest;

/**
 * This class manages all Guards in the world.<br>
 * It inherits all methods from L2Attackable and adds some more such as:
 * <ul>
 * <li>tracking PK</li>
 * <li>aggressive L2MonsterInstance.</li>
 * </ul>
 */
public final class Guard extends Attackable
{
	public Guard(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker instanceof Monster;
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/guard/" + filename + ".htm";
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Calculate the distance between the Player and the L2Npc
			if (!canInteract(player))
			{
				// Set the Player Intention to INTERACT
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				// Some guards have no HTMs on retail. Bypass the chat window if such guard is met.
				switch (getNpcId())
				{
					case 30733: // Guards in start villages
					case 31032:
					case 31033:
					case 31034:
					case 31035:
					case 31036:
					case 31671: // Patrols
					case 31672:
					case 31673:
					case 31674:
						return;
				}
				
				if (hasRandomAnimation())
					onRandomAnimation(Rnd.get(8));
				
				List<Quest> qlsa = getTemplate().getEventQuests(EventType.QUEST_START);
				if (qlsa != null && !qlsa.isEmpty())
					player.setLastQuestNpcObject(getObjectId());
				
				List<Quest> qlst = getTemplate().getEventQuests(EventType.ON_FIRST_TALK);
				if (qlst != null && qlst.size() == 1)
					qlst.get(0).notifyFirstTalk(this, player);
				else
					showChatWindow(player);
			}
		}
	}
	
	@Override
	public boolean isGuard()
	{
		return true;
	}
	
	@Override
	public int getDriftRange()
	{
		return 20;
	}
}