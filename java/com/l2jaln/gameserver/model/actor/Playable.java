package com.l2jaln.gameserver.model.actor;

import com.l2jaln.Config;
import com.l2jaln.events.CTF;
import com.l2jaln.gameserver.model.Announcement;
import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.WorldRegion;
import com.l2jaln.gameserver.model.actor.ai.CtrlEvent;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.actor.stat.PlayableStat;
import com.l2jaln.gameserver.model.actor.status.PlayableStatus;
import com.l2jaln.gameserver.model.actor.template.CreatureTemplate;
import com.l2jaln.gameserver.model.zone.ZoneId;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.ActionFailed;
import com.l2jaln.gameserver.network.serverpackets.Revive;
import com.l2jaln.gameserver.scripting.QuestState;
import com.l2jaln.gameserver.templates.skills.L2EffectFlag;
import com.l2jaln.gameserver.templates.skills.L2EffectType;

/**
 * This class represents all Playable characters in the world.<BR>
 * <BR>
 * L2Playable :<BR>
 * <BR>
 * <li>Player</li> <li>L2Summon</li><BR>
 * <BR>
 */
public abstract class Playable extends Creature
{
	/**
	 * Constructor of L2Playable (use Creature constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the Creature constructor to create an empty _skills slot and link copy basic Calculator set to this L2Playable</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the L2Playable
	 */
	public Playable(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PlayableStat(this));
	}
	
	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new PlayableStatus(this));
	}
	
	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public void onActionShift(Player player)
	{
		if (player.getTarget() != this)
			player.setTarget(this);
		else
			player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
				return false;
			
			// now reset currentHp to zero
			setCurrentHp(0);
			
			setIsDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		
		final Player actingPlayer = getActingPlayer();
		
		// Stop all active skills effects in progress
		if (isPhoenixBlessed())
		{
			// remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
			if (getCharmOfLuck())
				stopCharmOfLuck(null);
			if (isNoblesseBlessed())
				stopNoblesseBlessing(null);
		}
		// Same thing if the Character isn't a Noblesse Blessed L2Playable
		else if (isNoblesseBlessed())
		{
			stopNoblesseBlessing(null);
			
			// remove Lucky Charm if player have Nobless blessing buff
			if (getCharmOfLuck())
				stopCharmOfLuck(null);
		}
		else if (Config.LEAVE_BUFFS_ON_DIE  && !isArena1x1() && !isArena2x2() && isArena9x9() && (isArena5x5() && Config.ALLOW_5X5_LOSTBUFF) && !_inEventTvT && !isInsideZone(ZoneId.PVP_CUSTOM) && !isInsideZone(ZoneId.CUSTOM) || (Config.LEAVE_BUFF_SOLOFARM && isInsideZone(ZoneId.SOLO_CUSTOM)) || (isInsideZone(ZoneId.FLAG) || isInsideZone(ZoneId.RAID) || isInsideZone(ZoneId.RAID_NO_FLAG)) || (CTF.is_started() && _inEventCTF && Config.CTF_REMOVE_BUFFS_ON_DIE))
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
			if(!isPhantom())
			{
				if (actingPlayer.getClan() != null) {
					// Announcement.SelfAnnounceScreen(getName() + " of Clan " + actingPlayer.getClan().getName() + " was SELF !");
					Announcement.SelfAnnounce(getName() + " of Clan " + actingPlayer.getClan().getName() + " was SELF !");
				} else {
					//Announcement.SelfAnnounceScreen(getName() + " was SELF !");        
					Announcement.SelfAnnounce(getName() + " was SELF !");  
				}
			}
		}
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		broadcastStatusUpdate();
		
		// Notify Creature AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		
		final WorldRegion region = getRegion();
		if (region != null)
			region.onDeath(this);
		
		// Notify Quest of L2Playable's death
		for (QuestState qs : actingPlayer.getNotifyQuestOfDeath())
			qs.getQuest().notifyDeath((killer == null ? this : killer), actingPlayer);
		
		if (killer != null)
		{
			final Player player = killer.getActingPlayer();
			if (player != null)
				player.onKillUpdatePvPKarma(this);
		}
		
		return true;
	}
	
	@Override
	public void doRevive()
	{
		if (!isDead() || isTeleporting())
			return;
		
		setIsDead(false);
		
		if (isPhoenixBlessed())
		{
			stopPhoenixBlessing(null);
			
			getStatus().setCurrentHp(getMaxHp());
			getStatus().setCurrentMp(getMaxMp());
		}
		else
		{
			getStatus().setCurrentHpMp(getMaxHp() * Config.RESPAWN_RESTORE_HP, getMaxMp() * Config.RESPAWN_RESTORE_HP);
			getStatus().setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
		}
		
		// Start broadcast status
		broadcastPacket(new Revive(this));
		
		final WorldRegion region = getRegion();
		if (region != null)
			region.onRevive(this);
	}
	
	public boolean checkIfPvP(Playable target)
	{
		if (target == null || target == this)
			return false;
		
		final Player player = getActingPlayer();
		if (player == null || player.getKarma() != 0)
			return false;
		
		final Player targetPlayer = target.getActingPlayer();
		if (targetPlayer == null || targetPlayer == this)
			return false;
		
		if (targetPlayer.getKarma() != 0 || targetPlayer.getPvpFlag() == 0)
			return false;
		
		return true;
	}
	
	/**
	 * Return True.
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	/**
	 * <B><U> Overridden in </U> :</B>
	 * <ul>
	 * <li>L2Summon</li>
	 * <li>Player</li>
	 * </ul>
	 * @param id The system message to send to player.
	 */
	public void sendPacket(SystemMessageId id)
	{
		// default implementation
	}
	
	// Support for Noblesse Blessing skill, where buffs are retained after resurrect
	public final boolean isNoblesseBlessed()
	{
		return _effects.isAffected(L2EffectFlag.NOBLESS_BLESSING);
	}
	
	public final void stopNoblesseBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
		else
			removeEffect(effect);
		updateAbnormalEffect();
	}
	
	// Support for Soul of the Phoenix and Salvation skills
	public final boolean isPhoenixBlessed()
	{
		return _effects.isAffected(L2EffectFlag.PHOENIX_BLESSING);
	}
	
	public final void stopPhoenixBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.PHOENIX_BLESSING);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	/**
	 * @return True if the Silent Moving mode is active.
	 */
	public boolean isSilentMoving()
	{
		return _effects.isAffected(L2EffectFlag.SILENT_MOVE);
	}
	
	// for Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you
	public final boolean getProtectionBlessing()
	{
		return _effects.isAffected(L2EffectFlag.PROTECTION_BLESSING);
	}
	
	public void stopProtectionBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.PROTECTION_BLESSING);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	// Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	public final boolean getCharmOfLuck()
	{
		return _effects.isAffected(L2EffectFlag.CHARM_OF_LUCK);
	}
	
	public final void stopCharmOfLuck(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.CHARM_OF_LUCK);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		_effects.updateEffectIcons(partyOnly);
	}
	
	/**
	 * This method allows to easily send relations. Overridden in L2Summon and Player.
	 */
	public void broadcastRelationsChanges()
	{
	}
	
	@Override
	public boolean isInArena()
	{
		return isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE);
	}
	
	public abstract void doPickupItem(WorldObject object);
	
	public abstract int getKarma();
	
	public abstract byte getPvpFlag();
	
	public abstract boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove);
}