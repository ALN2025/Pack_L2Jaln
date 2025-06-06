package com.l2jaln.gameserver.scripting.scripts.ai.individual;

import java.util.ArrayList;
import java.util.List;

import com.l2jaln.commons.random.Rnd;

import com.l2jaln.Config;
import com.l2jaln.gameserver.instancemanager.GrandBossManager;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.actor.Attackable;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.GrandBoss;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.serverpackets.PlaySound;
import com.l2jaln.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import com.l2jaln.gameserver.templates.StatsSet;

public class Core extends L2AttackableAIScript
{
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	private static final int SUSCEPTOR = 29011;
	
	private static final byte ALIVE = 0; // Core is spawned.
	private static final byte DEAD = 1; // Core has been killed.
	
	private final List<Attackable> _minions = new ArrayList<>();
	
	public Core()
	{
		super("ai/individual");
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
		final int status = GrandBossManager.getInstance().getBossStatus(CORE);
		if (status == DEAD)
		{
			// load the unlock date and time for Core from DB
			final long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
			if (temp > 0)
			{
				// The time has not yet expired. Mark Core as currently locked (dead).
				startQuestTimer("core_unlock", temp, null, null, false);
			}
			else
			{
				// The time has expired while the server was offline. Spawn Core.
				final GrandBoss core = (GrandBoss) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0, false);
				GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
				spawnBoss(core);
			}
		}
		else
		{
			final int loc_x = info.getInteger("loc_x");
			final int loc_y = info.getInteger("loc_y");
			final int loc_z = info.getInteger("loc_z");
			final int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			
			final GrandBoss core = (GrandBoss) addSpawn(CORE, loc_x, loc_y, loc_z, heading, false, 0, false);
			core.setCurrentHpMp(hp, mp);
			spawnBoss(core);
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(CORE);
		addKillId(CORE, DEATH_KNIGHT, DOOM_WRAITH, SUSCEPTOR);
	}
	
	public void spawnBoss(GrandBoss npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", npc));
		
		// Spawn minions
		Attackable mob;
		for (int i = 0; i < 5; i++)
		{
			int x = 16800 + i * 360;
			mob = (Attackable) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
			mob = (Attackable) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
			int x2 = 16800 + i * 600;
			mob = (Attackable) addSpawn(DOOM_WRAITH, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
		}
		
		for (int i = 0; i < 4; i++)
		{
			int x = 16800 + i * 450;
			mob = (Attackable) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("core_unlock"))
		{
			final GrandBoss core = (GrandBoss) addSpawn(CORE, 17726, 108915, -6480, 0, false, 0, false);
			GrandBossManager.getInstance().setBossStatus(CORE, ALIVE);
			spawnBoss(core);
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			final Attackable mob = (Attackable) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (int i = 0; i < _minions.size(); i++)
			{
				final Attackable mob = _minions.get(i);
				if (mob != null)
					mob.decayMe();
			}
			_minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.isScriptValue(1))
		{
			if (Rnd.get(100) == 0)
				npc.broadcastNpcSay("Removing intruders.");
		}
		else
		{
			npc.setScriptValue(1);
			npc.broadcastNpcSay("A non-permitted target has been discovered.");
			npc.broadcastNpcSay("Starting intruder removal system.");
		}
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		if (npc.getNpcId() == CORE)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
			npc.broadcastNpcSay("A fatal error has occurred.");
			npc.broadcastNpcSay("System is being shut down...");
			npc.broadcastNpcSay("......");
			
			addSpawn(31842, 16502, 110165, -6394, 0, false, 900000, false);
			addSpawn(31842, 18948, 110166, -6397, 0, false, 900000, false);
			GrandBossManager.getInstance().setBossStatus(CORE, DEAD);
			
			long respawnTime;
            if(Config.CORE_CUSTOM_SPAWN_ENABLED && Config.FindNext(Config.CORE_CUSTOM_SPAWN_TIMES) != null)
            {
				respawnTime = Config.FindNext(Config.CORE_CUSTOM_SPAWN_TIMES).getTimeInMillis() - System.currentTimeMillis();
			}
            else
            {
    			respawnTime = (long) Config.SPAWN_INTERVAL_CORE + Rnd.get(-Config.RANDOM_SPAWN_TIME_CORE, Config.RANDOM_SPAWN_TIME_CORE);
    			respawnTime *= 3600000;
            }
			
			startQuestTimer("core_unlock", respawnTime, null, null, false);
			
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(CORE);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(CORE, info);
			startQuestTimer("despawn_minions", 20000, null, null, false);
			cancelQuestTimers("spawn_minion");
		}
		else if (GrandBossManager.getInstance().getBossStatus(CORE) == ALIVE && _minions != null && _minions.contains(npc))
		{
			_minions.remove(npc);
			startQuestTimer("spawn_minion", 60000, npc, null, false);
		}
		return super.onKill(npc, killer, isPet);
	}
}