package com.l2jaln.gameserver.skills.l2skills;

import java.util.logging.Level;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.gameserver.data.NpcTable;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.L2Spawn;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.template.NpcTemplate;
import com.l2jaln.gameserver.templates.StatsSet;

public class L2SkillSpawn extends L2Skill
{
	private final int _npcId;
	private final int _despawnDelay;
	private final boolean _summonSpawn;
	private final boolean _randomOffset;
	
	public L2SkillSpawn(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_despawnDelay = set.getInteger("despawnDelay", 0);
		_summonSpawn = set.getBool("isSummonSpawn", false);
		_randomOffset = set.getBool("randomOffset", true);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		if (_npcId == 0)
		{
			_log.warning("NPC ID not defined for skill ID: " + getId());
			return;
		}
		
		final NpcTemplate template = NpcTable.getInstance().getTemplate(_npcId);
		if (template == null)
		{
			_log.warning("Spawn of the nonexisting NPC ID: " + _npcId + ", skill ID: " + getId());
			return;
		}
		
		try
		{
			final L2Spawn spawn = new L2Spawn(template);
			
			int x = caster.getX();
			int y = caster.getY();
			if (_randomOffset)
			{
				x += Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20);
				y += Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20);
			}
			spawn.setLoc(x, y, caster.getZ() + 20, caster.getHeading());
			
			spawn.setRespawnState(false);
			Npc npc = spawn.doSpawn(_summonSpawn);
			
			if (_despawnDelay > 0)
				npc.scheduleDespawn(_despawnDelay);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception while spawning NPC ID: " + _npcId + ", skill ID: " + getId() + ", exception: " + e.getMessage(), e);
		}
	}
}