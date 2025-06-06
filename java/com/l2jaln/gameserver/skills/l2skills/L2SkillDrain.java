package com.l2jaln.gameserver.skills.l2skills;

import com.l2jaln.Config;
import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.ShotType;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.Playable;
import com.l2jaln.gameserver.model.actor.instance.Cubic;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.StatusUpdate;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.templates.StatsSet;

public class L2SkillDrain extends L2Skill
{
	private final float _absorbPart;
	private final int _absorbAbs;
	
	public L2SkillDrain(StatsSet set)
	{
		super(set);
		
		_absorbPart = set.getFloat("absorbPart", 0.f);
		_absorbAbs = set.getInteger("absorbAbs", 0);
	}
	
	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		final boolean isPlayable = activeChar instanceof Playable;
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			if (activeChar != target && target.isInvul())
				continue; // No effect on invulnerable chars unless they cast it themselves.
				
			boolean mcrit;
			if (Config.OLY_ENABLE_CUSTOM_CRIT && activeChar instanceof Player && activeChar.isInOlympiadMode())
			{
				if (((Player) activeChar).getClassId().getId() == 12 || ((Player) activeChar).getClassId().getId() == 94)
					mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Archmage);
				else if (((Player) activeChar).getClassId().getId() == 13 || ((Player) activeChar).getClassId().getId() == 95)
					mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Soultaker);
				else if (((Player) activeChar).getClassId().getId() == 27 || ((Player) activeChar).getClassId().getId() == 103)
					mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Mystic_Muse);
				else if (((Player) activeChar).getClassId().getId() == 40 || ((Player) activeChar).getClassId().getId() == 110)
					mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Storm_Screamer);
				else if (((Player) activeChar).getClassId().getId() == 51 || ((Player) activeChar).getClassId().getId() == 52 || ((Player) activeChar).getClassId().getId() == 115 || ((Player) activeChar).getClassId().getId() == 116)
					mcrit = Formulas.calcMCrit(Config.OLY_MCRIT_RATE_Dominator);
				else
					mcrit = Formulas.calcMCrit(Config.MAX_MCRIT_RATE);
			}
			else if (Config.ENABLE_CUSTOM_CRIT && activeChar instanceof Player)
			{
				if (((Player) activeChar).getClassId().getId() == 12 || ((Player) activeChar).getClassId().getId() == 94)
					mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Archmage);
				else if (((Player) activeChar).getClassId().getId() == 13 || ((Player) activeChar).getClassId().getId() == 95)
					mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Soultaker);
				else if (((Player) activeChar).getClassId().getId() == 27 || ((Player) activeChar).getClassId().getId() == 103)
					mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Mystic_Muse);
				else if (((Player) activeChar).getClassId().getId() == 40 || ((Player) activeChar).getClassId().getId() == 110)
					mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Storm_Screamer);
				else if (((Player) activeChar).getClassId().getId() == 51 || ((Player) activeChar).getClassId().getId() == 52 || ((Player) activeChar).getClassId().getId() == 115 || ((Player) activeChar).getClassId().getId() == 116)
					mcrit = Formulas.calcMCrit(Config.MCRIT_RATE_Dominator);
				else
					mcrit = Formulas.calcMCrit(Config.MAX_MCRIT_RATE);
			}
			else
				mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			
			final byte shld = Formulas.calcShldUse(activeChar, target, this);
			final int damage = (int) Formulas.calcMagicDam(activeChar, target, this, shld, sps, bsps, mcrit);
			
			if (damage > 0)
			{
				int _drain = 0;
				int _cp = (int) target.getCurrentCp();
				int _hp = (int) target.getCurrentHp();
				
				// Drain system is different for L2Playable and monsters.
				// When playables attack CP of enemies, monsters don't bother about it.
				if (isPlayable && _cp > 0)
				{
					if (damage < _cp)
						_drain = 0;
					else
						_drain = damage - _cp;
				}
				else if (damage > _hp)
					_drain = _hp;
				else
					_drain = damage;
				
				final double hpAdd = _absorbAbs + _absorbPart * _drain;
				if (hpAdd > 0)
				{
					final double hp = ((activeChar.getCurrentHp() + hpAdd) > activeChar.getMaxHp() ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd));
					
					activeChar.setCurrentHp(hp);
					
					StatusUpdate suhp = new StatusUpdate(activeChar);
					suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
					activeChar.sendPacket(suhp);
				}
				
				// That section is launched for drain skills made on ALIVE targets.
				if (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				{
					// Manage cast break of the target (calculating rate, sending message...)
					Formulas.calcCastBreak(target, damage);
					
					activeChar.sendDamageMessage(target, damage, mcrit, false, false);
					
					if (hasEffects() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
					{
						// ignoring vengance-like reflections
						if ((Formulas.calcSkillReflect(target, this) & Formulas.SKILL_REFLECT_SUCCEED) > 0)
						{
							activeChar.stopSkillEffects(getId());
							getEffects(target, activeChar);
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(getId()));
						}
						else
						{
							// activate attacked effects, if any
							target.stopSkillEffects(getId());
							if (Formulas.calcSkillSuccess(activeChar, target, this, shld, bsps))
								getEffects(activeChar, target);
							else
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(getId()));
						}
					}
					target.reduceCurrentHp(damage, activeChar, this);
				}
			}
		}
		
		if (hasSelfEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			getEffectsSelf(activeChar);
		}
		
		activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, isStaticReuse());
	}
	
	public void useCubicSkill(Cubic activeCubic, WorldObject[] targets)
	{
		if (Config.DEBUG)
			_log.info("L2SkillDrain: useCubicSkill()");
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			final boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, this));
			final byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, this);
			final int damage = (int) Formulas.calcMagicDam(activeCubic, target, this, mcrit, shld);
			
			// Check to see if we should damage the target
			if (damage > 0)
			{
				final Player owner = activeCubic.getOwner();
				final double hpAdd = _absorbAbs + _absorbPart * damage;
				if (hpAdd > 0)
				{
					final double hp = ((owner.getCurrentHp() + hpAdd) > owner.getMaxHp() ? owner.getMaxHp() : (owner.getCurrentHp() + hpAdd));
					
					owner.setCurrentHp(hp);
					
					StatusUpdate suhp = new StatusUpdate(owner);
					suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
					owner.sendPacket(suhp);
				}
				
				// That section is launched for drain skills made on ALIVE targets.
				if (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				{
					target.reduceCurrentHp(damage, activeCubic.getOwner(), this);
					
					// Manage cast break of the target (calculating rate, sending message...)
					Formulas.calcCastBreak(target, damage);
					
					owner.sendDamageMessage(target, damage, mcrit, false, false);
				}
			}
		}
	}
}