package com.l2jaln.gameserver.scripting.scripts.ai.group;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.Config;
import com.l2jaln.gameserver.geoengine.GeoEngine;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.ai.CtrlIntention;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.location.Location;
import com.l2jaln.gameserver.scripting.scripts.ai.L2AttackableAIScript;

/**
 * Elpies and 4s victims behavior.<br>
 * Hitting such NPC will lead them to flee everytime.
 */
public class FleeingNPCs extends L2AttackableAIScript
{
	public FleeingNPCs()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157, 20432);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		// Calculate random coords.
		final int rndX = npc.getX() + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		final int rndY = npc.getY() + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		
		// Wait the NPC to be immobile to move him again. Also check destination point.
		if (!npc.isMoving() && GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), rndX, rndY, npc.getZ()))
			npc.getAI().setIntention(CtrlIntention.MOVE_TO, new Location(rndX, rndY, npc.getZ()));
		
		return null;
	}
}