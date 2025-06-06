package com.l2jaln.gameserver.model.actor.ai.type;

import java.util.List;
import java.util.concurrent.Future;

import com.l2jaln.commons.concurrent.ThreadPool;
import com.l2jaln.commons.math.MathUtil;
import com.l2jaln.commons.random.Rnd;
import com.l2jaln.commons.util.ArraysUtil;

import com.l2jaln.Config;
import com.l2jaln.gameserver.geoengine.GeoEngine;
import com.l2jaln.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.L2Skill.SkillTargetType;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Attackable;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.Playable;
import com.l2jaln.gameserver.model.actor.Summon;
import com.l2jaln.gameserver.model.actor.ai.CtrlIntention;
import com.l2jaln.gameserver.model.actor.instance.Door;
import com.l2jaln.gameserver.model.actor.instance.FestivalMonster;
import com.l2jaln.gameserver.model.actor.instance.FriendlyMonster;
import com.l2jaln.gameserver.model.actor.instance.GrandBoss;
import com.l2jaln.gameserver.model.actor.instance.Guard;
import com.l2jaln.gameserver.model.actor.instance.Monster;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.actor.instance.RaidBoss;
import com.l2jaln.gameserver.model.actor.instance.RiftInvader;
import com.l2jaln.gameserver.model.actor.template.NpcTemplate.AIType;
import com.l2jaln.gameserver.model.actor.template.NpcTemplate.SkillType;
import com.l2jaln.gameserver.model.location.Location;
import com.l2jaln.gameserver.model.zone.ZoneId;
import com.l2jaln.gameserver.scripting.EventType;
import com.l2jaln.gameserver.scripting.Quest;
import com.l2jaln.gameserver.templates.skills.L2EffectType;

public class AttackableAI extends CreatureAI implements Runnable
{
	protected static final int RANDOM_WALK_RATE = 30;
	protected static final int MAX_ATTACK_TIMEOUT = 90000; // 1m30
	
	/** The L2Attackable AI task executed every 1s (call onEvtThink method) */
	protected Future<?> _aiTask;
	
	/** The delay after wich the attacked is stopped */
	protected long _attackTimeout;
	
	/** The L2Attackable aggro counter */
	protected int _globalAggro;
	
	/** The flag used to indicate that a thinking action is in progress ; prevent recursive thinking */
	protected boolean _thinking;
	
	private int _chaostime = 0;
	
	public AttackableAI(Attackable attackable)
	{
		super(attackable);
		
		_attackTimeout = Long.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}
	
	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * <B><U> Actor is a L2GuardInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The Player target has karma (=PK)</li>
	 * <li>The L2MonsterInstance target is aggressive</li>
	 * </ul>
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The Player target isn't a Defender</li>
	 * </ul>
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The Player target has karma (=PK)</li>
	 * </ul>
	 * <B><U> Actor is a L2MonsterInstance</U> :</B>
	 * <ul>
	 * <li>The target isn't a Folk, a Door or another L2Npc</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li>
	 * </ul>
	 * @param target The targeted Creature
	 * @return True if the target is autoattackable (depends on the actor type).
	 */
	protected boolean autoAttackCondition(Creature target)
	{
		// Check if the target isn't null, a Door or dead.
		if (target == null || target instanceof Door || target.isAlikeDead())
			return false;
		
		final Attackable me = getActiveChar();
		
		if (target instanceof Playable)
		{
			// Check if target is in the Aggro range
			if (!me.isInsideRadius(target, me.getTemplate().getAggroRange(), true, false))
				return false;
			
			// Check if the AI isn't a Raid Boss, can See Silent Moving players and the target isn't in silent move mode
			if (!(me.isRaid()) && !(me.canSeeThroughSilentMove()) && ((Playable) target).isSilentMoving())
				return false;
			
			// Check if the target is a Player
			Player targetPlayer = target.getActingPlayer();
			if (targetPlayer != null)
			{
				// GM checks ; check if the target is invisible or got access level
				if (targetPlayer.isGM() && (targetPlayer.getAppearance().getInvisible() || !targetPlayer.getAccessLevel().canTakeAggro()))
					return false;
				
				// Check if player is an allied Varka.
				if (ArraysUtil.contains(me.getTemplate().getClans(), "varka_silenos_clan") && targetPlayer.isAlliedWithVarka())
					return false;
				
				// Check if player is an allied Ketra.
				if (ArraysUtil.contains(me.getTemplate().getClans(), "ketra_orc_clan") && targetPlayer.isAlliedWithKetra())
					return false;
				
				// check if the target is within the grace period for JUST getting up from fake death
				if (targetPlayer.isRecentFakeDeath())
					return false;
				
				if (targetPlayer.isInParty() && targetPlayer.getParty().isInDimensionalRift())
				{
					byte riftType = targetPlayer.getParty().getDimensionalRift().getType();
					byte riftRoom = targetPlayer.getParty().getDimensionalRift().getCurrentRoom();
					
					if (me instanceof RiftInvader && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
						return false;
				}
			}
		}
		
		// Check if the actor is a L2GuardInstance
		if (me instanceof Guard)
		{
			// Check if the Player target has karma (=PK)
			if (target instanceof Player && ((Player) target).getKarma() > 0)
				return GeoEngine.getInstance().canSeeTarget(me, target);
			
			// Check if the L2MonsterInstance target is aggressive
			if (target instanceof Monster && Config.GUARD_ATTACK_AGGRO_MOB)
				return (((Monster) target).isAggressive() && GeoEngine.getInstance().canSeeTarget(me, target));
			
			return false;
		}
		// The actor is a L2FriendlyMobInstance
		else if (me instanceof FriendlyMonster)
		{
			// Check if the Player target has karma (=PK)
			if (target instanceof Player && ((Player) target).getKarma() > 0)
				return GeoEngine.getInstance().canSeeTarget(me, target); // Los Check
				
			return false;
		}
		// The actor is a L2Npc
		else
		{
			if (target instanceof Attackable && me.isConfused())
				return GeoEngine.getInstance().canSeeTarget(me, target);
			
			if (target instanceof Npc)
				return false;
			
			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneId.PEACE))
				return false;
			
			// Check if the actor is Aggressive
			return (me.isAggressive() && GeoEngine.getInstance().canSeeTarget(me, target));
		}
	}
	
	@Override
	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}
	
	/**
	 * Set the Intention of this CreatureAI and create an AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, IDLE will be change in ACTIVE</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (intention == CtrlIntention.IDLE || intention == CtrlIntention.ACTIVE)
		{
			// Check if actor is not dead
			Attackable npc = getActiveChar();
			if (!npc.isAlikeDead())
			{
				// If no players are around, set the Intention to ACTIVE
				if (!npc.getKnownType(Player.class).isEmpty())
					intention = CtrlIntention.ACTIVE;
				else
				{
					if (npc.getSpawn() != null)
					{
						final int range = Config.MAX_DRIFT_RANGE;
						if (!npc.isInsideRadius(npc.getSpawn().getLocX(), npc.getSpawn().getLocY(), npc.getSpawn().getLocZ(), range + range, true, false))
							intention = CtrlIntention.ACTIVE;
					}
				}
			}
			
			if (intention == CtrlIntention.IDLE)
			{
				// Set the Intention of this L2AttackableAI to IDLE
				super.changeIntention(CtrlIntention.IDLE, null, null);
				
				// Stop AI task and detach AI from NPC
				stopAITask();
				
				// Cancel the AI
				_actor.detachAI();
				return;
			}
		}
		
		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);
		
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (_aiTask == null)
			_aiTask = ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	/**
	 * Manage the Attack Intention :
	 * <ul>
	 * <li>Stop current Attack (if necessary).</li>
	 * <li>Calculate attack timeout.</li>
	 * <li>Start a new Attack and Launch Think Event.</li>
	 * </ul>
	 * @param target The Creature to attack
	 */
	@Override
	protected void onIntentionAttack(Creature target)
	{
		// Calculate the attack timeout
		_attackTimeout = System.currentTimeMillis() + MAX_ATTACK_TIMEOUT;
		
		// Check buff.
		checkBuffAndSetBackTarget(target);
		
		// Manage the attack intention : stop current attack (if necessary), start a new attack and launch Think event.
		super.onIntentionAttack(target);
	}
	
	private void thinkCast()
	{
		if (checkTargetLost(getTarget()))
		{
			setTarget(null);
			return;
		}
		
		if (maybeMoveToPawn(getTarget(), _skill.getCastRange()))
			return;
		
		clientStopMoving(null);
		setIntention(CtrlIntention.ACTIVE);
		_actor.doCast(_skill);
	}
	
	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).
	 * <ul>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable Creature in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
	 * <li>If the actor is a L2GuardInstance that can't attack, order to it to return to its home location</li>
	 * <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li>
	 * </ul>
	 */
	protected void thinkActive()
	{
		final Attackable npc = getActiveChar();
		
		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
				_globalAggro++;
			else
				_globalAggro--;
		}
		
		// Add all autoAttackable Creature in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			// Get all visible objects inside its Aggro Range
			for (Creature target : npc.getKnownType(Creature.class))
			{
				// Check to see if this is a festival mob spawn. If it is, then check to see if the aggro trigger is a festival participant...if so, move to attack it.
				if (npc instanceof FestivalMonster && target instanceof Player)
				{
					if (!((Player) target).isFestivalParticipant())
						continue;
				}
				
				// For each Creature check if the target is autoattackable
				if (autoAttackCondition(target)) // check aggression
				{
					// Add the attacker to the L2Attackable _aggroList
					if (npc.getHating(target) == 0)
						npc.addDamageHate(target, 0, 0);
				}
			}
			
			if (!npc.isCoreAIDisabled())
			{
				// Chose a target from its aggroList and order to attack the target
				final Creature hated = (Creature) ((npc.isConfused()) ? getTarget() : npc.getMostHated());
				if (hated != null)
				{
					// Get the hate level of the L2Attackable against this Creature target contained in _aggroList
					if (npc.getHating(hated) + _globalAggro > 0)
					{
						// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
						npc.setRunning();
						
						// Set the AI Intention to ATTACK
						setIntention(CtrlIntention.ATTACK, hated);
					}
					return;
				}
			}
		}
		
		// If this is a festival monster, then it remains in the same location.
		if (npc instanceof FestivalMonster)
			return;
		
		// Check buffs.
		if (checkBuffAndSetBackTarget(_actor.getTarget()))
			return;
		
		// Minions following leader
		final Attackable leader = npc.getLeader();
		if (leader != null && !leader.isAlikeDead())
		{
			final int offset = (int) (100 + npc.getCollisionRadius() + leader.getCollisionRadius());
			final int minRadius = (int) (leader.getCollisionRadius() + 30);
			
			if (leader.isRunning())
				npc.setRunning();
			else
				npc.setWalking();
			
			if (npc.getPlanDistanceSq(leader.getX(), leader.getY()) > offset * offset)
			{
				int x1 = Rnd.get(minRadius * 2, offset * 2); // x
				int y1 = Rnd.get(x1, offset * 2); // distance
				
				y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
				
				if (x1 > offset + minRadius)
					x1 = leader.getX() + x1 - offset;
				else
					x1 = leader.getX() - x1 + minRadius;
				
				if (y1 > offset + minRadius)
					y1 = leader.getY() + y1 - offset;
				else
					y1 = leader.getY() - y1 + minRadius;
				
				// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
				moveTo(x1, y1, leader.getZ());
				return;
			}
		}
		else
		{
			// Return to home if too far.
			if (npc.returnHome(false))
				return;
			
			// Random walk otherwise.
			if (npc.getSpawn() != null && !npc.isNoRndWalk() && Rnd.get(RANDOM_WALK_RATE) == 0)
			{
				int x1 = npc.getSpawn().getLocX();
				int y1 = npc.getSpawn().getLocY();
				int z1 = npc.getSpawn().getLocZ();
				
				final int range = Config.MAX_DRIFT_RANGE;
				
				x1 = Rnd.get(range * 2); // x
				y1 = Rnd.get(x1, range * 2); // distance
				y1 = (int) Math.sqrt(y1 * y1 - x1 * x1); // y
				x1 += npc.getSpawn().getLocX() - range;
				y1 += npc.getSpawn().getLocY() - range;
				z1 = npc.getZ();
				
				// Move the actor to Location (x,y,z)
				moveTo(x1, y1, z1);
			}
		}
	}
	
	/**
	 * Manage AI attack thoughts of a L2Attackable (called by onEvtThink).
	 * <ul>
	 * <li>Update the attack timeout if actor is running.</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to ACTIVE.</li>
	 * <li>Call all WorldObject of its Faction inside the Faction Range.</li>
	 * <li>Choose a target and order to attack it with magic skill or physical attack.</li>
	 * </ul>
	 */
	protected void thinkAttack()
	{
		final Attackable npc = getActiveChar();
		if (npc.isCastingNow())
			return;
		
		// Pickup most hated character.
		Creature attackTarget = npc.getMostHated();
		
		// If target doesn't exist, is too far or if timeout is expired.
		if (attackTarget == null || _attackTimeout < System.currentTimeMillis() || MathUtil.calculateDistance(npc, attackTarget, true) > 2000)
		{
			// Stop hating this target after the attack timeout or if target is dead
			npc.stopHating(attackTarget);
			setIntention(CtrlIntention.ACTIVE);
			npc.setWalking();
			return;
		}
		
		// Corpse AIs, as AI scripts, are stopped here.
		if (npc.isCoreAIDisabled())
			return;
		
		if ((npc instanceof Monster) && (attackTarget instanceof Player) && !(npc instanceof RaidBoss) && !(npc instanceof GrandBoss) && !npc.isDead() && !npc.isRaidMinion() && npc.isInsideZone(ZoneId.NO_MONSTER) && attackTarget.isInsideZone(ZoneId.NO_MONSTER))
		{
			setIntention(CtrlIntention.ACTIVE);
			npc.setWalking();
			npc.abortAttack();
			npc.abortCast();
			npc.setTarget(null);
			
			if (npc.getSpawn() != null)
				npc.teleToLocation(npc.getSpawn().getLoc(), 0);
			else if (npc.isMinion())
			{
				Creature leader = npc.getLeader();
				if (leader != null && !leader.isDead())
					npc.teleToLocation(leader.getX(), leader.getY(), leader.getZ(), 0);
			}
			System.out.println(npc.getName() + " Teleported");
			return;
		}
		if(Config.ENABLE_PROTECT_LIST_BOSS)
		{
			if ((npc instanceof RaidBoss) && (attackTarget instanceof Player) && !npc.isDead() && Config.LIST_RAID_BOSS_ZONE_IDS.contains(npc.getNpcId()) && (!npc.isInsideZone(ZoneId.RAID) || !attackTarget.isInsideZone(ZoneId.RAID)) && (!npc.isInsideZone(ZoneId.RAID_NO_FLAG) || !attackTarget.isInsideZone(ZoneId.RAID_NO_FLAG)) && !npc.getSpawn().is_customBossInstance())
			{
				setIntention(CtrlIntention.ACTIVE);
				npc.setWalking();
				npc.abortAttack();
				npc.abortCast();
				npc.setTarget(null);
				
				if (npc.getSpawn() != null)
					npc.teleToLocation(npc.getSpawn().getLoc(), 0);
				else if (npc.isMinion())
				{
					Creature leader = npc.getLeader();
					if (leader != null && !leader.isDead())
						npc.teleToLocation(leader.getX(), leader.getY(), leader.getZ(), 0);
				}
				return;
			}
		}
		else if ((npc instanceof GrandBoss) && (attackTarget instanceof Player) && !npc.isDead() && npc.getNpcId() == 29001 && (!npc.isInsideZone(ZoneId.RAID) || !attackTarget.isInsideZone(ZoneId.RAID)) && (!npc.isInsideZone(ZoneId.RAID_NO_FLAG) || !attackTarget.isInsideZone(ZoneId.RAID_NO_FLAG)))
		{
			if (!npc.isMinion() && !npc.isRaidMinion())
			{
				setIntention(CtrlIntention.ACTIVE);
				npc.setWalking();
				npc.abortAttack();
				npc.abortCast();
				npc.setTarget(null);
				
				if (npc.getSpawn() != null)
					npc.teleToLocation(npc.getSpawn().getLoc(), 0);
			}
			return;
		}
		
		/**
		 * TARGET CHECKS<br>
		 * Chaos time for RB/minions.
		 */
		
		if (npc.isRaid() || npc.isRaidMinion())
		{
			_chaostime++;
			if (npc instanceof RaidBoss && _chaostime > Config.RAID_CHAOS_TIME && (Rnd.get(100) <= 100 - (npc.getCurrentHp() * ((((Monster) npc).hasMinions()) ? 200 : 100) / npc.getMaxHp())))
			{
				attackTarget = aggroReconsider(attackTarget);
				_chaostime = 0;
			}
			else if (npc instanceof GrandBoss && _chaostime > Config.GRAND_CHAOS_TIME)
			{
				double chaosRate = 100 - (npc.getCurrentHp() * 300 / npc.getMaxHp());
				if ((chaosRate <= 10 && Rnd.get(100) <= 10) || (chaosRate > 10 && Rnd.get(100) <= chaosRate))
				{
					attackTarget = aggroReconsider(attackTarget);
					_chaostime = 0;
				}
			}
			else if (_chaostime > Config.MINION_CHAOS_TIME && (Rnd.get(100) <= 100 - (npc.getCurrentHp() * 200 / npc.getMaxHp())))
			{
				attackTarget = aggroReconsider(attackTarget);
				_chaostime = 0;
			}
		}
		
		setTarget(attackTarget);
		npc.setTarget(attackTarget);
		
		/**
		 * COMMON INFORMATIONS<br>
		 * Used for range and distance check.
		 */
		
		final int actorCollision = (int) npc.getCollisionRadius();
		final int combinedCollision = (int) (actorCollision + attackTarget.getCollisionRadius());
		final double dist = Math.sqrt(npc.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
		
		int range = combinedCollision;
		if (attackTarget.isMoving())
			range += 15;
		
		if (npc.isMoving())
			range += 15;
		
		/**
		 * CAST CHECK<br>
		 * The mob succeeds a skill check ; make all possible checks to define the skill to launch. If nothing is found, go in MELEE CHECK.<br>
		 * It will check skills arrays in that order :
		 * <ul>
		 * <li>suicide skill at 15% max HPs</li>
		 * <li>buff skill if such effect isn't existing</li>
		 * <li>heal skill if self or ally is under 75% HPs (priority to others healers and mages)</li>
		 * <li>debuff skill if such effect isn't existing</li>
		 * <li>damage skill, in that order : short range and long range</li>
		 * </ul>
		 */
		
		if (willCastASpell())
		{
			// This list is used in order to avoid multiple calls on skills lists. Tests are made one after the other, and content is replaced when needed.
			List<L2Skill> defaultList;
			
			// -------------------------------------------------------------------------------
			// Suicide possibility if HPs are < 15%.
			defaultList = npc.getTemplate().getSkills(SkillType.SUICIDE);
			if (!defaultList.isEmpty() && (npc.getCurrentHp() / npc.getMaxHp() < 0.15))
			{
				final L2Skill skill = Rnd.get(defaultList);
				if (cast(skill, dist, range + skill.getSkillRadius()))
					return;
			}
			
			// -------------------------------------------------------------------------------
			// Heal
			defaultList = npc.getTemplate().getSkills(SkillType.HEAL);
			if (!defaultList.isEmpty())
			{
				// First priority is to heal leader (if npc is a minion).
				if (npc.isMinion())
				{
					Creature leader = npc.getLeader();
					if (leader != null && !leader.isDead() && (leader.getCurrentHp() / leader.getMaxHp() < 0.75))
					{
						for (L2Skill sk : defaultList)
						{
							if (sk.getTargetType() == SkillTargetType.TARGET_SELF)
								continue;
							
							if (!checkSkillCastConditions(sk))
								continue;
							
							final int overallRange = (int) (sk.getCastRange() + actorCollision + leader.getCollisionRadius());
							if (!MathUtil.checkIfInRange(overallRange, npc, leader, false) && sk.getTargetType() != SkillTargetType.TARGET_PARTY && !npc.isMovementDisabled())
							{
								moveToPawn(leader, overallRange);
								return;
							}
							
							if (GeoEngine.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								npc.setTarget(leader);
								npc.doCast(sk);
								return;
							}
						}
					}
				}
				
				// Second priority is to heal himself.
				if (npc.getCurrentHp() / npc.getMaxHp() < 0.75)
				{
					for (L2Skill sk : defaultList)
					{
						if (!checkSkillCastConditions(sk))
							continue;
						
						clientStopMoving(null);
						npc.setTarget(npc);
						npc.doCast(sk);
						return;
					}
				}
				
				for (L2Skill sk : defaultList)
				{
					if (!checkSkillCastConditions(sk))
						continue;
					
					if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
					{
						final String[] actorClans = npc.getTemplate().getClans();
						for (Attackable obj : npc.getKnownTypeInRadius(Attackable.class, sk.getCastRange() + actorCollision))
						{
							if (obj.isDead())
								continue;
							
							if (!ArraysUtil.contains(actorClans, obj.getTemplate().getClans()))
								continue;
							
							if (obj.getCurrentHp() / obj.getMaxHp() < 0.75)
							{
								if (GeoEngine.getInstance().canSeeTarget(npc, obj))
								{
									clientStopMoving(null);
									npc.setTarget(obj);
									npc.doCast(sk);
									return;
								}
							}
						}
						
						if (sk.getTargetType() == SkillTargetType.TARGET_PARTY)
						{
							clientStopMoving(null);
							npc.doCast(sk);
							return;
						}
					}
				}
			}
			
			// -------------------------------------------------------------------------------
			// Buff
			defaultList = npc.getTemplate().getSkills(SkillType.BUFF);
			if (!defaultList.isEmpty())
			{
				for (L2Skill sk : defaultList)
				{
					if (!checkSkillCastConditions(sk))
						continue;
					
					if (npc.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(attackTarget);
						return;
					}
				}
			}
			
			// -------------------------------------------------------------------------------
			// Debuff - 10% luck to get debuffed.
			defaultList = npc.getTemplate().getSkills(SkillType.DEBUFF);
			if (Rnd.get(100) < 10 && !defaultList.isEmpty())
			{
				for (L2Skill sk : defaultList)
				{
					if (!checkSkillCastConditions(sk) || (sk.getCastRange() + npc.getCollisionRadius() + attackTarget.getCollisionRadius() <= dist && !canAura(sk)))
						continue;
					
					if (!GeoEngine.getInstance().canSeeTarget(npc, attackTarget))
						continue;
					
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			
			// -------------------------------------------------------------------------------
			// General attack skill - short range is checked, then long range.
			defaultList = npc.getTemplate().getSkills(SkillType.SHORT_RANGE);
			if (!defaultList.isEmpty() && dist <= 150)
			{
				final L2Skill skill = Rnd.get(defaultList);
				if (cast(skill, dist, skill.getCastRange()))
					return;
			}
			else
			{
				defaultList = npc.getTemplate().getSkills(SkillType.LONG_RANGE);
				if (!defaultList.isEmpty() && dist > 150)
				{
					final L2Skill skill = Rnd.get(defaultList);
					if (cast(skill, dist, skill.getCastRange()))
						return;
				}
			}
		}
		
		/**
		 * MELEE CHECK<br>
		 * The mob failed a skill check ; make him flee if AI authorizes it, else melee attack.
		 */
		
		// The range takes now in consideration physical attack range.
		range += npc.getPhysicalAttackRange();
		
		if (npc.isMovementDisabled())
		{
			// If distance is too big, choose another target.
			if (dist > range)
				attackTarget = targetReconsider(range, true);
			
			// Any AI type, even healer or mage, will try to melee attack if it can't do anything else (desesperate situation).
			if (attackTarget != null)
				_actor.doAttack(attackTarget);
			
			return;
		}
		
		/**
		 * MOVE AROUND CHECK<br>
		 * In case many mobs are trying to hit from same place, move a bit, circling around the target
		 */
		
		if (Rnd.get(100) <= 3)
		{
			for (Attackable nearby : npc.getKnownTypeInRadius(Attackable.class, actorCollision))
			{
				if (nearby != attackTarget)
				{
					int newX = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
						newX = attackTarget.getX() + newX;
					else
						newX = attackTarget.getX() - newX;
					
					int newY = combinedCollision + Rnd.get(40);
					if (Rnd.nextBoolean())
						newY = attackTarget.getY() + newY;
					else
						newY = attackTarget.getY() - newY;
					
					if (!npc.isInsideRadius(newX, newY, actorCollision, false))
					{
						int newZ = npc.getZ() + 30;
						if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ))
							moveTo(newX, newY, newZ);
					}
					return;
				}
			}
		}
		
		/**
		 * FLEE CHECK<br>
		 * Test the flee possibility. Archers got 25% chance to flee.
		 */
		
		if (npc.getTemplate().getAiType() == AIType.ARCHER && dist <= (60 + combinedCollision) && Rnd.get(4) > 1)
		{
			final int posX = npc.getX() + ((attackTarget.getX() < npc.getX()) ? 300 : -300);
			final int posY = npc.getY() + ((attackTarget.getY() < npc.getY()) ? 300 : -300);
			final int posZ = npc.getZ() + 30;
			
			if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ))
			{
				setIntention(CtrlIntention.MOVE_TO, new Location(posX, posY, posZ));
				return;
			}
		}
		
		/**
		 * BASIC MELEE ATTACK
		 */
		
		if (dist > range || !GeoEngine.getInstance().canSeeTarget(npc, attackTarget))
		{
			if (attackTarget.isMoving())
				range -= 30;
			
			if (range < 5)
				range = 5;
			
			moveToPawn(attackTarget, range);
			return;
		}
		
		_actor.doAttack((Creature) getTarget());
	}
	
	protected boolean cast(L2Skill sk, double distance, int range)
	{
		if (sk == null)
			return false;
		
		final Attackable caster = getActiveChar();
		
		if (caster.isCastingNow() && !sk.isSimultaneousCast())
			return false;
		
		if (!checkSkillCastConditions(sk))
			return false;
		
		Creature attackTarget = (Creature) getTarget();
		if (attackTarget == null)
			return false;
		
		switch (sk.getSkillType())
		{
			case BUFF:
			{
				if (caster.getFirstEffect(sk) == null)
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					return true;
				}
				
				// ----------------------------------------
				// If actor already have buff, start looking at others same faction mob to cast
				if (sk.getTargetType() == SkillTargetType.TARGET_SELF)
					return false;
				
				if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						WorldObject targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				
				if (canParty(sk))
				{
					clientStopMoving(null);
					WorldObject targets = attackTarget;
					caster.setTarget(caster);
					caster.doCast(sk);
					caster.setTarget(targets);
					return true;
				}
				break;
			}
			
			case HEAL:
			case HOT:
			case HEAL_PERCENT:
			case HEAL_STATIC:
			case BALANCE_LIFE:
			{
				// Minion case.
				if (caster.isMinion() && sk.getTargetType() != SkillTargetType.TARGET_SELF)
				{
					Creature leader = caster.getLeader();
					if (leader != null && !leader.isDead() && Rnd.get(100) > (leader.getCurrentHp() / leader.getMaxHp() * 100))
					{
						final int overallRange = (int) (sk.getCastRange() + caster.getCollisionRadius() + leader.getCollisionRadius());
						if (!MathUtil.checkIfInRange(overallRange, caster, leader, false) && sk.getTargetType() != SkillTargetType.TARGET_PARTY && !caster.isMovementDisabled())
							moveToPawn(leader, overallRange);
						
						if (GeoEngine.getInstance().canSeeTarget(caster, leader))
						{
							clientStopMoving(null);
							caster.setTarget(leader);
							caster.doCast(sk);
							return true;
						}
					}
				}
				
				// Personal case.
				double percentage = caster.getCurrentHp() / caster.getMaxHp() * 100;
				if (Rnd.get(100) < (100 - percentage) / 3)
				{
					clientStopMoving(null);
					caster.setTarget(caster);
					caster.doCast(sk);
					return true;
				}
				
				if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					for (Attackable obj : caster.getKnownTypeInRadius(Attackable.class, (int) (sk.getCastRange() + caster.getCollisionRadius())))
					{
						if (obj.isDead())
							continue;
						
						if (!ArraysUtil.contains(caster.getTemplate().getClans(), obj.getTemplate().getClans()))
							continue;
						
						percentage = obj.getCurrentHp() / obj.getMaxHp() * 100;
						if (Rnd.get(100) < (100 - percentage) / 10)
						{
							if (GeoEngine.getInstance().canSeeTarget(caster, obj))
							{
								clientStopMoving(null);
								caster.setTarget(obj);
								caster.doCast(sk);
								return true;
							}
						}
					}
				}
				
				if (sk.getTargetType() == SkillTargetType.TARGET_PARTY)
				{
					for (Attackable obj : caster.getKnownTypeInRadius(Attackable.class, (int) (sk.getSkillRadius() + caster.getCollisionRadius())))
					{
						if (!ArraysUtil.contains(caster.getTemplate().getClans(), obj.getTemplate().getClans()))
							continue;
						
						if (obj.getCurrentHp() < obj.getMaxHp() && Rnd.get(100) <= 20)
						{
							clientStopMoving(null);
							caster.setTarget(caster);
							caster.doCast(sk);
							return true;
						}
					}
				}
				break;
			}
			
			case DEBUFF:
			case POISON:
			case DOT:
			case MDOT:
			case BLEED:
			{
				if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && !attackTarget.isDead() && distance <= range)
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					if ((sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case SLEEP:
			{
				if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					if (!attackTarget.isDead() && distance <= range)
					{
						if (distance > range || attackTarget.isMoving())
						{
							if (attackTarget.getFirstEffect(sk) == null)
							{
								clientStopMoving(null);
								caster.doCast(sk);
								return true;
							}
						}
					}
					
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					if ((sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case ROOT:
			case STUN:
			case PARALYZE:
			{
				if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && distance <= range)
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					else if ((sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case MUTE:
			case FEAR:
			{
				if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !canAOE(sk) && distance <= range)
				{
					if (attackTarget.getFirstEffect(sk) == null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					if ((sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				else if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			case CANCEL:
			case NEGATE:
			{
				// decrease cancel probability
				if (Rnd.get(50) != 0)
					return true;
				
				if (sk.getTargetType() == SkillTargetType.TARGET_ONE)
				{
					if (attackTarget.getFirstEffect(L2EffectType.BUFF) != null && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						WorldObject targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				else if (canAOE(sk))
				{
					if ((sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget))
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					else if ((sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
				}
				break;
			}
			
			default:
			{
				if (!canAura(sk))
				{
					if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= range)
					{
						clientStopMoving(null);
						caster.doCast(sk);
						return true;
					}
					
					Creature target = targetReconsider(sk.getCastRange(), true);
					if (target != null)
					{
						clientStopMoving(null);
						WorldObject targets = attackTarget;
						caster.setTarget(target);
						caster.doCast(sk);
						caster.setTarget(targets);
						return true;
					}
				}
				else
				{
					clientStopMoving(null);
					caster.doCast(sk);
					return true;
				}
			}
				break;
		}
		
		return false;
	}
	
	/**
	 * @param skill the skill to check.
	 * @return {@code true} if the skill is available for casting {@code false} otherwise.
	 */
	protected boolean checkSkillCastConditions(L2Skill skill)
	{
		// Not enough MP.
		if (skill.getMpConsume() >= getActiveChar().getCurrentMp())
			return false;
		
		// Character is in "skill disabled" mode.
		if (getActiveChar().isSkillDisabled(skill))
			return false;
		
		// Is a magic skill and character is magically muted or is a physical skill and character is physically muted.
		if ((skill.isMagic() && getActiveChar().isMuted()) || getActiveChar().isPhysicalMuted())
			return false;
		
		return true;
	}
	
	/**
	 * This method checks if the actor will cast a skill or not.
	 * @return true if the actor will cast a spell, false otherwise.
	 */
	protected boolean willCastASpell()
	{
		switch (getActiveChar().getTemplate().getAiType())
		{
			case HEALER:
			case MAGE:
				return !getActiveChar().isMuted();
				
			default:
				if (getActiveChar().isPhysicalMuted())
					return false;
		}
		return Rnd.get(100) < 10;
	}
	
	/**
	 * Method used when the actor can't attack his current target (immobilize state, for exemple).
	 * <ul>
	 * <li>If the actor got an hate list, pickup a new target from it.</li>
	 * <li>If the actor didn't find a target on his hate list, check if he is aggro type and pickup a new target using his knownlist.</li>
	 * </ul>
	 * @param range The range to check (skill range for skill ; physical range for melee).
	 * @param rangeCheck That boolean is used to see if a check based on the distance must be made (skill check).
	 * @return The new Creature victim.
	 */
	protected Creature targetReconsider(int range, boolean rangeCheck)
	{
		final Attackable actor = getActiveChar();
		
		// Verify first if aggro list is empty, if not search a victim following his aggro position.
		if (!actor.getAggroList().isEmpty())
		{
			// Store aggro value && most hated, in order to add it to the random target we will choose.
			final Creature previousMostHated = actor.getMostHated();
			final int aggroMostHated = actor.getHating(previousMostHated);
			
			for (Creature obj : actor.getHateList())
			{
				if (!autoAttackCondition(obj))
					continue;
				
				if (rangeCheck)
				{
					// Verify the distance, -15 if the victim is moving, -15 if the npc is moving.
					double dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY())) - obj.getCollisionRadius();
					if (actor.isMoving())
						dist -= 15;
					
					if (obj.isMoving())
						dist -= 15;
					
					if (dist > range)
						continue;
				}
				
				// Stop to hate the most hated.
				actor.stopHating(previousMostHated);
				
				// Add previous most hated aggro to that new victim.
				actor.addDamageHate(obj, 0, (aggroMostHated > 0) ? aggroMostHated : 2000);
				return obj;
			}
		}
		
		// If hate list gave nothing, then verify first if the actor is aggressive, and then pickup a victim from his knownlist.
		if (actor.isAggressive())
		{
			for (Creature target : actor.getKnownTypeInRadius(Creature.class, actor.getTemplate().getAggroRange()))
			{
				if (!autoAttackCondition(target))
					continue;
				
				if (rangeCheck)
				{
					// Verify the distance, -15 if the victim is moving, -15 if the npc is moving.
					double dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY())) - target.getCollisionRadius();
					if (actor.isMoving())
						dist -= 15;
					
					if (target.isMoving())
						dist -= 15;
					
					if (dist > range)
						continue;
				}
				
				// Only 1 aggro, as the hate list is supposed to be cleaned. Simulate an aggro range entrance.
				actor.addDamageHate(target, 0, 1);
				return target;
			}
		}
		
		// Return null if no new victim has been found.
		return null;
	}
	
	/**
	 * Method used for chaotic mode (RBs / GBs and their minions).<br>
	 * @param oldTarget The previous target, reused if no available target is found.
	 * @return old target if none could fits or the new target.
	 */
	protected Creature aggroReconsider(Creature oldTarget)
	{
		final Attackable actor = getActiveChar();
		
		// Choose a new victim, and make checks to see if it fits.
		for (Creature victim : actor.getHateList())
		{
			if (!autoAttackCondition(victim))
				continue;
			
			// Add most hated aggro to the victim aggro.
			actor.addDamageHate(victim, 0, actor.getHating(actor.getMostHated()));
			return victim;
		}
		return oldTarget;
	}
	
	/**
	 * Manage AI thinking actions of a L2Attackable.
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the thinking action is already in progress.
		if (_thinking || _actor.isAllSkillsDisabled())
			return;
		
		// Start thinking action.
		_thinking = true;
		
		try
		{
			// Manage AI thoughts.
			switch (getIntention())
			{
				case ACTIVE:
					thinkActive();
					break;
				case ATTACK:
					thinkAttack();
					break;
				case CAST:
					thinkCast();
					break;
			}
		}
		finally
		{
			// Stop thinking action.
			_thinking = false;
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Attacked.
	 * <ul>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player</li>
	 * <li>Set the Intention to ATTACK</li>
	 * </ul>
	 * @param attacker The Creature that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		final Attackable me = getActiveChar();
		
		// Calculate the attack timeout
		_attackTimeout = System.currentTimeMillis() + MAX_ATTACK_TIMEOUT;
		
		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
			_globalAggro = 0;
		
		// Add the attacker to the _aggroList of the actor
		me.addDamageHate(attacker, 0, 1);
		
		// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
		me.setRunning();
		
		// Set the Intention to ATTACK
		if (getIntention() != CtrlIntention.ATTACK)
			setIntention(CtrlIntention.ATTACK, attacker);
		else if (me.getMostHated() != getTarget())
			setIntention(CtrlIntention.ATTACK, attacker);
		
		if (me instanceof Monster)
		{
			Monster master = (Monster) me;
			
			if (master.hasMinions())
				master.getMinionList().onAssist(me, attacker);
			else
			{
				master = master.getLeader();
				if (master != null && master.hasMinions())
					master.getMinionList().onAssist(me, attacker);
			}
		}
		
		if (attacker != null)
		{
			// Faction check.
			final String[] actorClans = me.getTemplate().getClans();
			if (actorClans != null && me.getAttackByList().contains(attacker))
			{
				for (Attackable called : me.getKnownTypeInRadius(Attackable.class, me.getTemplate().getClanRange()))
				{
					// Caller hasn't AI or is dead.
					if (!called.hasAI() || called.isDead())
						continue;
					
					// Caller clan doesn't correspond to the called clan.
					if (!ArraysUtil.contains(actorClans, called.getTemplate().getClans()))
						continue;
					
					// Called mob doesnt care about that type of caller id (the bitch !).
					if (ArraysUtil.contains(called.getTemplate().getIgnoredIds(), me.getNpcId()))
						continue;
					
					// Check if the WorldObject is inside the Faction Range of the actor
					final CtrlIntention calledIntention = called.getAI().getIntention();
					if ((calledIntention == CtrlIntention.IDLE || calledIntention == CtrlIntention.ACTIVE || (calledIntention == CtrlIntention.MOVE_TO && !called.isRunning())) && GeoEngine.getInstance().canSeeTarget(me, called))
					{
						if (attacker instanceof Playable)
						{
							List<Quest> quests = called.getTemplate().getEventQuests(EventType.ON_FACTION_CALL);
							if (quests != null)
							{
								Player player = attacker.getActingPlayer();
								boolean isSummon = attacker instanceof Summon;
								for (Quest quest : quests)
									quest.notifyFactionCall(called, me, player, isSummon);
							}
						}
						else
						{
							called.addDamageHate(attacker, 0, me.getHating(attacker));
							called.getAI().setIntention(CtrlIntention.ATTACK, attacker);
						}
					}
				}
			}
		}
		
		super.onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Aggression.
	 * <ul>
	 * <li>Add the target to the actor _aggroList or update hate if already present</li>
	 * <li>Set the actor Intention to ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li>
	 * </ul>
	 * @param target The Creature that attacks
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		final Attackable me = getActiveChar();
		
		// Add the target to the actor _aggroList or update hate if already present
		me.addDamageHate(target, 0, aggro);
		
		// Set the actor AI Intention to ATTACK
		if (getIntention() != CtrlIntention.ATTACK)
		{
			// Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
			me.setRunning();
			
			setIntention(CtrlIntention.ATTACK, target);
		}
		
		if (me instanceof Monster)
		{
			Monster master = (Monster) me;
			
			if (master.hasMinions())
				master.getMinionList().onAssist(me, target);
			else
			{
				master = master.getLeader();
				if (master != null && master.hasMinions())
					master.getMinionList().onAssist(me, target);
			}
		}
		
		if (target == null)
			return;
		
		// Faction check.
		final String[] actorClans = me.getTemplate().getClans();
		if (actorClans != null && me.getAttackByList().contains(target))
		{
			for (Attackable called : me.getKnownTypeInRadius(Attackable.class, me.getTemplate().getClanRange()))
			{
				// Caller hasn't AI or is dead.
				if (!called.hasAI() || called.isDead())
					continue;
				
				// Caller clan doesn't correspond to the called clan.
				if (!ArraysUtil.contains(actorClans, called.getTemplate().getClans()))
					continue;
				
				// Called mob doesnt care about that type of caller id (the bitch !).
				if (ArraysUtil.contains(called.getTemplate().getIgnoredIds(), me.getNpcId()))
					continue;
				
				// Check if the WorldObject is inside the Faction Range of the actor
				final CtrlIntention calledIntention = called.getAI().getIntention();
				if ((calledIntention == CtrlIntention.IDLE || calledIntention == CtrlIntention.ACTIVE || (calledIntention == CtrlIntention.MOVE_TO && !called.isRunning())) && GeoEngine.getInstance().canSeeTarget(me, called))
				{
					if (target instanceof Playable)
					{
						List<Quest> quests = called.getTemplate().getEventQuests(EventType.ON_FACTION_CALL);
						if (quests != null)
						{
							Player player = target.getActingPlayer();
							boolean isSummon = target instanceof Summon;
							for (Quest quest : quests)
								quest.notifyFactionCall(called, me, player, isSummon);
						}
					}
					else
					{
						called.addDamageHate(target, 0, me.getHating(target));
						called.getAI().setIntention(CtrlIntention.ATTACK, target);
					}
				}
			}
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		// Cancel attack timeout
		_attackTimeout = Long.MAX_VALUE;
		
		super.onIntentionActive();
	}
	
	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}
	
	private Attackable getActiveChar()
	{
		return (Attackable) _actor;
	}
	
	private boolean checkBuffAndSetBackTarget(WorldObject target)
	{
		if (Rnd.get(RANDOM_WALK_RATE) != 0)
			return false;
		
		for (L2Skill sk : getActiveChar().getTemplate().getSkills(SkillType.BUFF))
		{
			if (getActiveChar().getFirstEffect(sk) != null)
				continue;
			
			clientStopMoving(null);
			
			_actor.setTarget(_actor);
			_actor.doCast(sk);
			_actor.setTarget(target);
			return true;
		}
		return false;
	}
}