package com.l2jaln.gameserver.model.actor.status;

import com.l2jaln.commons.math.MathUtil;
import com.l2jaln.commons.random.Rnd;

import com.l2jaln.Config;
import com.l2jaln.gameserver.instancemanager.DuelManager;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.Playable;
import com.l2jaln.gameserver.model.actor.Summon;
import com.l2jaln.gameserver.model.actor.ai.CtrlIntention;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.actor.instance.Servitor;
import com.l2jaln.gameserver.model.actor.stat.PlayerStat;
import com.l2jaln.gameserver.model.entity.Duel.DuelState;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.ActionFailed;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.scripting.QuestState;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.skills.Stats;

public class PlayerStatus extends PlayableStatus
{
	private double _currentCp = 0;
	
	public PlayerStatus(Player activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceCp(int value)
	{
		if (getCurrentCp() > value)
			setCurrentCp(getCurrentCp() - value);
		else
			setCurrentCp(0);
	}
	
	@Override
	public final void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true, false, false, false);
	}
	
	@Override
	public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		reduceHp(value, attacker, awake, isDOT, isHPConsumption, false);
	}
	
	public final void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption, boolean ignoreCP)
	{
		if (getActiveChar().isDead())
			return;
		
		// invul handling
		if (getActiveChar().isInvul())
		{
			// other chars can't damage
			if (attacker != getActiveChar())
				return;
			
			// only DOT and HP consumption allowed for damage self
			if (!isDOT && !isHPConsumption)
				return;
		}
		
		if (!isHPConsumption)
		{
			getActiveChar().stopEffectsOnDamage(awake);
			getActiveChar().forceStandUp();
			
			if (!isDOT)
			{
				if (getActiveChar().isStunned() && Rnd.get(10) == 0)
					getActiveChar().stopStunning(true);
			}
		}
		
		int fullValue = (int) value;
		int tDmg = 0;
		
		if (attacker != null && attacker != getActiveChar())
		{
			final Player attackerPlayer = attacker.getActingPlayer();
			if (attackerPlayer != null)
			{
				if (attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
					return;
				
				if (getActiveChar().isInDuel())
				{
					final DuelState playerState = getActiveChar().getDuelState();
					if (playerState == DuelState.DEAD || playerState == DuelState.WINNER)
						return;
					
					// Cancel duel if player got hit by another player that is not part of the duel or if player isn't in duel state.
					if (attackerPlayer.getDuelId() != getActiveChar().getDuelId() || playerState != DuelState.DUELLING)
						getActiveChar().setDuelState(DuelState.INTERRUPTED);
				}
			}
			
			// Check and calculate transfered damage
			final Summon summon = getActiveChar().getPet();
			if (summon != null && summon instanceof Servitor && MathUtil.checkIfInRange(900, getActiveChar(), summon, true))
			{
				tDmg = (int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;
				
				// Only transfer dmg up to current HP, it should not be killed
				tDmg = Math.min((int) summon.getCurrentHp() - 1, tDmg);
				if (tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker, null);
					value -= tDmg;
					fullValue = (int) value; // reduce the announced value here as player will get a message about summon damage
				}
			}
			
			if (!ignoreCP && attacker instanceof Playable)
			{
				if (getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value); // Set Cp to diff of Cp vs value
					value = 0; // No need to subtract anything from Hp
				}
				else
				{
					value -= getCurrentCp(); // Get diff from value vs Cp; will apply diff to Hp
					setCurrentCp(0, false); // Set Cp to 0
				}
			}
			
			if (fullValue > 0 && !isDOT)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
				smsg.addCharName(attacker);
				smsg.addNumber(fullValue);
				getActiveChar().sendPacket(smsg);
				
				if (tDmg > 0)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1);
					smsg.addCharName(attacker);
					smsg.addNumber(tDmg);
					getActiveChar().sendPacket(smsg);
					
					if (attackerPlayer != null)
					{
						smsg = SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR);
						smsg.addNumber(fullValue);
						smsg.addNumber(tDmg);
						attackerPlayer.sendPacket(smsg);
					}
				}
			}
		}
		
		if (value > 0)
		{
			value = getCurrentHp() - value;
			if (value <= 0)
			{
				if (getActiveChar().isInDuel())
				{
					if (getActiveChar().getDuelState() == DuelState.DUELLING)
					{
						getActiveChar().disableAllSkills();
						stopHpMpRegeneration();
						
						if (attacker != null)
						{
							attacker.getAI().setIntention(CtrlIntention.ACTIVE);
							attacker.sendPacket(ActionFailed.STATIC_PACKET);
						}
						
						// let the DuelManager know of his defeat
						DuelManager.getInstance().onPlayerDefeat(getActiveChar());
					}
					value = 1;
				}
				else
					value = 0;
			}
			setCurrentHp(value);
		}
		
		if (getActiveChar().getCurrentHp() < 0.5)
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();
			
			if (getActiveChar().isInOlympiadMode())
			{
				stopHpMpRegeneration();
				getActiveChar().setIsDead(true);
				
				if (getActiveChar().getPet() != null)
					getActiveChar().getPet().getAI().setIntention(CtrlIntention.IDLE, null);
				
				return;
			}
			
			getActiveChar().doDie(attacker);
			
			if (!Config.DISABLE_TUTORIAL)
			{
				QuestState qs = getActiveChar().getQuestState("Tutorial");
				if (qs != null)
					qs.getQuest().notifyEvent("CE30", null, getActiveChar());
			}
		}
	}
	
	@Override
	public final void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		super.setCurrentHp(newHp, broadcastPacket);
		
		if (!Config.DISABLE_TUTORIAL && getCurrentHp() <= getActiveChar().getStat().getMaxHp() * .3)
		{
			QuestState qs = getActiveChar().getQuestState("Tutorial");
			if (qs != null)
				qs.getQuest().notifyEvent("CE45", null, getActiveChar());
		}
	}
	
	@Override
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	@Override
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}
	
	public final void setCurrentCp(double newCp, boolean broadcastPacket)
	{
		int maxCp = getActiveChar().getStat().getMaxCp();
		
		synchronized (this)
		{
			if (getActiveChar().isDead())
				return;
			
			if (newCp < 0)
				newCp = 0;
			
			if (newCp >= maxCp)
			{
				// Set the RegenActive flag to false
				_currentCp = maxCp;
				_flagsRegenActive &= ~REGEN_FLAG_CP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentCp = newCp;
				_flagsRegenActive |= REGEN_FLAG_CP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}
	
	@Override
	protected void doRegeneration()
	{
		final PlayerStat pcStat = getActiveChar().getStat();
		
		// Modify the current CP of the Creature.
		if (getCurrentCp() < pcStat.getMaxCp())
			setCurrentCp(getCurrentCp() + Formulas.calcCpRegen(getActiveChar()), false);
		
		// Modify the current HP of the Creature.
		if (getCurrentHp() < pcStat.getMaxHp())
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		
		// Modify the current MP of the Creature.
		if (getCurrentMp() < pcStat.getMaxMp())
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		
		// Send the StatusUpdate packet.
		getActiveChar().broadcastStatusUpdate();
	}
	
	@Override
	public Player getActiveChar()
	{
		return (Player) super.getActiveChar();
	}
}