package com.l2jaln.gameserver.scripting.scripts.ai.individual;


import com.l2jaln.commons.math.MathUtil;
import com.l2jaln.commons.random.Rnd;

import com.l2jaln.Config;
import com.l2jaln.gameserver.data.SkillTable;
import com.l2jaln.gameserver.geoengine.GeoEngine;
import com.l2jaln.gameserver.instancemanager.GrandBossManager;
import com.l2jaln.gameserver.instancemanager.ZoneManager;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.ai.CtrlIntention;
import com.l2jaln.gameserver.model.actor.instance.GrandBoss;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.location.Location;
import com.l2jaln.gameserver.model.location.SpawnLocation;
import com.l2jaln.gameserver.model.zone.ZoneId;
import com.l2jaln.gameserver.model.zone.type.L2BossZone;
import com.l2jaln.gameserver.network.serverpackets.PlaySound;
import com.l2jaln.gameserver.network.serverpackets.SocialAction;
import com.l2jaln.gameserver.network.serverpackets.SpecialCamera;
import com.l2jaln.gameserver.scripting.EventType;
import com.l2jaln.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import com.l2jaln.gameserver.templates.StatsSet;

public class Valakas extends L2AttackableAIScript
{
	private static final L2BossZone VALAKAS_LAIR = ZoneManager.getInstance().getZoneById(110010, L2BossZone.class);
	
	public static final byte DORMANT = 0; // Valakas is spawned and no one has entered yet. Entry is unlocked.
	public static final byte WAITING = 1; // Valakas is spawned and someone has entered, triggering a 30 minute window for additional people to enter. Entry is unlocked.
	public static final byte FIGHTING = 2; // Valakas is engaged in battle, annihilating his foes. Entry is locked.
	public static final byte DEAD = 3; // Valakas has been killed. Entry is locked.
	
	private static final int[] FRONT_SKILLS =
	{
		4681,
		4682,
		4683,
		4684
	};
	
	private static final int[] BEHIND_SKILLS =
	{
		4685,
		4686,
		4688
	};
	
	private static final int LAVA_SKIN = 4680;
	private static final int METEOR_SWARM = 4690;
	
	private static final SpawnLocation[] CUBE_LOC =
	{
		new SpawnLocation(213197, -114651, -1638, 0),
	};
	
	public static final int VALAKAS = 29028;
	
	private long _timeTracker = 0; // Time tracker for last attack on Valakas.
	private Player _actualVictim; // Actual target of Valakas.
	
	public Valakas()
	{
		super("ai/individual");
		
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(VALAKAS);
		
		switch (GrandBossManager.getInstance().getBossStatus(VALAKAS))
		{
			case DEAD: // Launch the timer to set DORMANT, or set DORMANT directly if timer expired while offline.
				long temp = (info.getLong("respawn_time") - System.currentTimeMillis());
				if (temp > 0)
					startQuestTimer("valakas_unlock", temp, null, null, false);
				else
					GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
				break;
			
			case WAITING:
				startQuestTimer("beginning", Config.WAIT_TIME_VALAKAS, null, null, false);
				break;
			
			case FIGHTING:
				final int loc_x = info.getInteger("loc_x");
				final int loc_y = info.getInteger("loc_y");
				final int loc_z = info.getInteger("loc_z");
				final int heading = info.getInteger("heading");
				final int hp = info.getInteger("currentHP");
				final int mp = info.getInteger("currentMP");
				
				final Npc valakas = addSpawn(VALAKAS, loc_x, loc_y, loc_z, heading, false, 0, false);
				GrandBossManager.getInstance().addBoss((GrandBoss) valakas);
				
				valakas.setCurrentHpMp(hp, mp);
				valakas.setRunning();
				
				// stores current time for inactivity task.
				_timeTracker = System.currentTimeMillis();
				
				// Start timers.
				startQuestTimer("regen_task", 60000, valakas, null, true);
				startQuestTimer("skill_task", 2000, valakas, null, true);
				break;
		}
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(VALAKAS, EventType.ON_ATTACK, EventType.ON_KILL, EventType.ON_SPAWN, EventType.ON_AGGRO);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("beginning"))
		{
			// Stores current time
			_timeTracker = System.currentTimeMillis();
			
			// Spawn Valakas and set him invul.
			npc = addSpawn(VALAKAS, 212852, -114842, -1632, 0, false, 0, false);
			GrandBossManager.getInstance().addBoss((GrandBoss) npc);
			npc.setIsInvul(true);
			
			// Sound + socialAction.
			for (Player plyr : VALAKAS_LAIR.getKnownTypeInside(Player.class))
			{
				plyr.sendPacket(new PlaySound(1, "B03_A", npc));
				plyr.sendPacket(new SocialAction(npc, 3));
			}
			
			// Launch the cinematic, and tasks (regen + skill).
			startQuestTimer("spawn_1", 2000, npc, null, false); // 2000
			startQuestTimer("spawn_2", 3500, npc, null, false); // 1500
			startQuestTimer("spawn_3", 6800, npc, null, false); // 3300
			startQuestTimer("spawn_4", 9700, npc, null, false); // 2900
			startQuestTimer("spawn_5", 12400, npc, null, false); // 2700
			startQuestTimer("spawn_6", 12401, npc, null, false); // 1
			startQuestTimer("spawn_7", 15601, npc, null, false); // 3200
			startQuestTimer("spawn_8", 17001, npc, null, false); // 1400
			startQuestTimer("spawn_9", 23701, npc, null, false); // 6700 - end of cinematic
			startQuestTimer("spawn_10", 29401, npc, null, false); // 5700 - AI + unlock
		}
		// Regeneration && inactivity task
		else if (event.equalsIgnoreCase("regen_task"))
		{
			// Inactivity task - 15min
			if (GrandBossManager.getInstance().getBossStatus(VALAKAS) == FIGHTING)
			{
				if (_timeTracker + 900000 < System.currentTimeMillis())
				{
					// Set it dormant.
					GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
					
					// Drop all players from the zone.
					VALAKAS_LAIR.oustAllPlayers();
					
					// Cancel skill_task and regen_task.
					cancelQuestTimer("regen_task", npc, null);
					cancelQuestTimer("skill_task", npc, null);
					
					// Delete current instance of Valakas.
					npc.deleteMe();
					
					return null;
				}
			}
			
			// Regeneration buff.
			if (Rnd.get(30) == 0)
			{
				L2Skill skillRegen;
				final double hpRatio = npc.getCurrentHp() / npc.getMaxHp();
				
				// Current HPs are inferior to 25% ; apply lvl 4 of regen skill.
				if (hpRatio < 0.25)
					skillRegen = SkillTable.getInstance().getInfo(4691, 4);
				// Current HPs are inferior to 50% ; apply lvl 3 of regen skill.
				else if (hpRatio < 0.5)
					skillRegen = SkillTable.getInstance().getInfo(4691, 3);
				// Current HPs are inferior to 75% ; apply lvl 2 of regen skill.
				else if (hpRatio < 0.75)
					skillRegen = SkillTable.getInstance().getInfo(4691, 2);
				else
					skillRegen = SkillTable.getInstance().getInfo(4691, 1);
				
				skillRegen.getEffects(npc, npc);
			}
		}
		// Spawn cinematic, regen_task and choose of skill.
		else if (event.equalsIgnoreCase("spawn_1"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1800, 180, -1, 1500, 10000, 0, 0, 1, 0));
		else if (event.equalsIgnoreCase("spawn_2"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1300, 180, -5, 3000, 10000, 0, -5, 1, 0));
		else if (event.equalsIgnoreCase("spawn_3"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 500, 180, -8, 600, 10000, 0, 60, 1, 0));
		else if (event.equalsIgnoreCase("spawn_4"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 800, 180, -8, 2700, 10000, 0, 30, 1, 0));
		else if (event.equalsIgnoreCase("spawn_5"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 200, 250, 70, 0, 10000, 30, 80, 1, 0));
		else if (event.equalsIgnoreCase("spawn_6"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 250, 70, 2500, 10000, 30, 80, 1, 0));
		else if (event.equalsIgnoreCase("spawn_7"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 700, 150, 30, 0, 10000, -10, 60, 1, 0));
		else if (event.equalsIgnoreCase("spawn_8"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1200, 150, 20, 2900, 10000, -10, 30, 1, 0));
		else if (event.equalsIgnoreCase("spawn_9"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 750, 170, -10, 3400, 4000, 10, -15, 1, 0));
		else if (event.equalsIgnoreCase("spawn_10"))
		{
			GrandBossManager.getInstance().setBossStatus(VALAKAS, FIGHTING);
			npc.setIsInvul(false);
			
			startQuestTimer("regen_task", 60000, npc, null, true);
			startQuestTimer("skill_task", 2000, npc, null, true);
		}
		// Death cinematic, spawn of Teleport Cubes.
		else if (event.equalsIgnoreCase("die_1"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 2000, 130, -1, 0, 10000, 0, 0, 1, 1));
		else if (event.equalsIgnoreCase("die_2"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1100, 210, -5, 3000, 10000, -13, 0, 1, 1));
		else if (event.equalsIgnoreCase("die_3"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1300, 200, -8, 3000, 10000, 0, 15, 1, 1));
		else if (event.equalsIgnoreCase("die_4"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1000, 190, 0, 500, 10000, 0, 10, 1, 1));
		else if (event.equalsIgnoreCase("die_5"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 120, 0, 2500, 10000, 12, 40, 1, 1));
		else if (event.equalsIgnoreCase("die_6"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 20, 0, 700, 10000, 10, 10, 1, 1));
		else if (event.equalsIgnoreCase("die_7"))
			VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 10, 0, 1000, 10000, 20, 70, 1, 1));
		else if (event.equalsIgnoreCase("die_8"))
		{
			//VALAKAS_LAIR.broadcastPacket(new SpecialCamera(npc.getObjectId(), 1700, 10, 0, 300, 250, 20, -20, 1, 1));
			
			for (SpawnLocation loc : CUBE_LOC)
				addSpawn(31759, loc, false, 900000, false);
			
			startQuestTimer("remove_players", 900000, null, null, false);
		}
		else if (event.equalsIgnoreCase("skill_task"))
			callSkillAI(npc);
		else if (event.equalsIgnoreCase("valakas_unlock"))
			GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT);
		else if (event.equalsIgnoreCase("remove_players"))
			VALAKAS_LAIR.oustAllPlayers();
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.disableCoreAI(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.isInvul())
			return null;
		
		// Debuff strider-mounted players.
		if (attacker.getMountType() == 1)
		{
			final L2Skill debuff = SkillTable.getInstance().getInfo(4258, 1);
			if (attacker.getFirstEffect(debuff) == null)
			{
				npc.setTarget(attacker);
				npc.doCast(debuff);
			}
		}
		_timeTracker = System.currentTimeMillis();
		
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		// Cancel skill_task and regen_task.
		cancelQuestTimer("regen_task", npc, null);
		cancelQuestTimer("skill_task", npc, null);
		
		// Launch death animation.
		VALAKAS_LAIR.broadcastPacket(new PlaySound(1, "B03_D", npc));
		
		//startQuestTimer("die_1", 300, npc, null, false); // 300
		//startQuestTimer("die_2", 600, npc, null, false); // 300
		//startQuestTimer("die_3", 3800, npc, null, false); // 3200
		//startQuestTimer("die_4", 8200, npc, null, false); // 4400
		//startQuestTimer("die_5", 8700, npc, null, false); // 500
		//startQuestTimer("die_6", 13300, npc, null, false); // 4600
		//startQuestTimer("die_7", 14000, npc, null, false); // 700
		startQuestTimer("die_8", 16500, npc, null, false); // 2500
		
		
		GrandBossManager.getInstance().setBossStatus(VALAKAS, DEAD);
		
		long respawnTime;
		if(Config.VALAKAS_CUSTOM_SPAWN_ENABLED && Config.FindNext(Config.VALAKAS_CUSTOM_SPAWN_TIMES) != null)
        {
			respawnTime = Config.FindNext(Config.VALAKAS_CUSTOM_SPAWN_TIMES).getTimeInMillis() - System.currentTimeMillis();
		}
        else
        {
    		respawnTime = (long) Config.SPAWN_INTERVAL_VALAKAS + Rnd.get(-Config.RANDOM_SPAWN_TIME_VALAKAS, Config.RANDOM_SPAWN_TIME_VALAKAS);
    		respawnTime *= 3600000;
        }
		
		startQuestTimer("valakas_unlock", respawnTime, null, null, false);
		
		// also save the respawn time so that the info is maintained past reboots
		StatsSet info = GrandBossManager.getInstance().getStatsSet(VALAKAS);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatsSet(VALAKAS, info);
		
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		return null;
	}
	
	private void callSkillAI(Npc npc)
	{
		if (npc.isInvul() || npc.isCastingNow())
			return;
		
		// Pickup a target if no or dead victim. 10% luck he decides to reconsiders his target.
		if (_actualVictim == null || _actualVictim.isDead() || !(npc.getKnownType(Player.class).contains(_actualVictim)) || Rnd.get(10) == 0 || !(_actualVictim.isInsideZone(ZoneId.RAID) || _actualVictim.isInsideZone(ZoneId.RAID_NO_FLAG)) || !GeoEngine.getInstance().canSeeTarget(npc, _actualVictim))
			_actualVictim = getRandomPlayer(npc);
		
		// If result is still null, Valakas will roam. Don't go deeper in skill AI.
		if (_actualVictim == null)
		{
			if (Rnd.get(10) == 0)
			{
				int x = npc.getX();
				int y = npc.getY();
				int z = npc.getZ();
				
				int posX = x + Rnd.get(-1400, 1400);
				int posY = y + Rnd.get(-1400, 1400);
				
				if (GeoEngine.getInstance().canMoveToTarget(x, y, z, posX, posY, z))
					npc.getAI().setIntention(CtrlIntention.MOVE_TO, new Location(posX, posY, z));
			}
			return;
		}
		
		if (!npc.isDead() && (!npc.isInsideZone(ZoneId.RAID) || !_actualVictim.isInsideZone(ZoneId.RAID)) && (!npc.isInsideZone(ZoneId.RAID_NO_FLAG) || !_actualVictim.isInsideZone(ZoneId.RAID_NO_FLAG)))
		{
			npc.abortAttack();
			npc.abortCast();
			npc.setTarget(null);
			_actualVictim = null;		
			npc.getAI().setIntention(CtrlIntention.MOVE_TO, new Location(npc.getSpawn().getLoc()));			
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
		
		// Cast the skill or follow the target.
		if (MathUtil.checkIfInRange((skill.getCastRange() < 600) ? 600 : skill.getCastRange(), npc, _actualVictim, true))
		{
			npc.getAI().setIntention(CtrlIntention.IDLE);
			npc.setTarget(_actualVictim);
			npc.doCast(skill);
		}
		else
			npc.getAI().setIntention(CtrlIntention.FOLLOW, _actualVictim, null);
	}
	
	/**
	 * Pick a random skill.<br>
	 * Valakas will mostly use utility skills. If Valakas feels surrounded, he will use AoE skills.<br>
	 * Lower than 50% HPs, he will begin to use Meteor skill.
	 * @param npc valakas
	 * @return a usable skillId
	 */
	private static int getRandomSkill(Npc npc)
	{
		final double hpRatio = npc.getCurrentHp() / npc.getMaxHp();
		
		// Valakas Lava Skin is prioritary.
		if (hpRatio < 0.25 && Rnd.get(1500) == 0 && npc.getFirstEffect(4680) == null)
			return LAVA_SKIN;
		
		if (hpRatio < 0.5 && Rnd.get(60) == 0)
			return METEOR_SWARM;
		
		// Find enemies surrounding Valakas.
		final int[] playersAround = getPlayersCountInPositions(1200, npc, false);
		
		// Behind position got more ppl than front position, use behind aura skill.
		if (playersAround[1] > playersAround[0])
			return BEHIND_SKILLS[Rnd.get(BEHIND_SKILLS.length)];
		
		// Use front aura skill.
		return FRONT_SKILLS[Rnd.get(FRONT_SKILLS.length)];
	}
	
	public static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		while (startWaiterTime + interval > System.currentTimeMillis() && GrandBossManager._announce)
		{
			seconds--; // Here because we don't want to see two time announce at the same time
			
			switch (seconds)
			{
				case 3600: // 1 hour left
					GrandBossManager.AnnounceGrandBoss("Spawn Valakas in " + seconds / 60 / 60 + " hour(s)!");
					break;
				case 1799: // 10 minutes left
					GrandBossManager.AnnounceGrandBoss("Spawn Valakas in 30 minute(s) !");
					break;
				case 599: // 10 minutes left
					GrandBossManager.AnnounceGrandBoss("Spawn Valakas in 10 minute(s) !");
					break;
				case 299: // 10 minutes left
					GrandBossManager.AnnounceGrandBoss("Spawn Valakas in 5 minute(s) !");
					break;
				
				case 1500: // 25 minutes left
				case 1200: // 20 minutes left
				case 900: // 15 minutes left
				case 540: // 9 minutes left
				case 480: // 8 minutes left
				case 420: // 7 minutes left
				case 360: // 6 minutes left
				case 240: // 4 minutes left
				case 180: // 3 minutes left
				case 120: // 2 minutes left
				case 60: // 1 minute left
					GrandBossManager.AnnounceGrandBoss("Spawn Valakas in " + seconds / 60 + " minute(s) !");
					break;
				case 30: // 30 seconds left
				case 15: // 15 seconds left
					GrandBossManager.AnnounceGrandBoss("Spawn Valakas in " + seconds + " second(s) !");
					break;
				
				case 6: // 3 seconds left
				case 5: // 3 seconds left
				case 4: // 3 seconds left
				case 3: // 2 seconds left
				case 2: // 1 seconds left
					GrandBossManager.AnnounceGrandBoss("Spawn Valakas in " + (seconds - 1) + " second(s) !");
					break;
				
				case 1: // 1 seconds left
				{
					if (GrandBossManager._announce)
						GrandBossManager.AnnounceGrandBoss("Valakas Is alive, teleport to boss closed !");
					GrandBossManager._announce = false;
				}
					break;
			}
			
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			
			// Only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{
				}
			}
		}
	}
}