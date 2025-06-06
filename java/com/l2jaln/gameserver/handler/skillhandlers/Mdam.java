package com.l2jaln.gameserver.handler.skillhandlers;

import com.l2jaln.Config;
import com.l2jaln.gameserver.handler.ISkillHandler;
import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.ShotType;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.templates.skills.L2EffectType;
import com.l2jaln.gameserver.templates.skills.L2SkillType;

public class Mdam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MDAM,
		L2SkillType.DEATHLINK
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		final boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
		final boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (activeChar instanceof Player && target instanceof Player && ((Player) target).isFakeDeath())
				target.stopFakeDeath(true);
			else if (target.isDead())
				continue;
			
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
					mcrit = Formulas.calcMCrit(Config.OLY_MAX_MCRIT_RATE);
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
				mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
			
			final byte shld = Formulas.calcShldUse(activeChar, target, skill);
			final byte reflect = Formulas.calcSkillReflect(target, skill);
			
			int damage = (int) Formulas.calcMagicDam(activeChar, target, skill, shld, sps, bsps, mcrit);
			if (damage > 0)
			{
				// Manage cast break of the target (calculating rate, sending message...)
				Formulas.calcCastBreak(target, damage);
				
				// vengeance reflected damage
				if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
					activeChar.reduceCurrentHp(damage, target, skill);
				else
				{
					activeChar.sendDamageMessage(target, damage, mcrit, false, false);
					target.reduceCurrentHp(damage, activeChar, skill);
				}
				
				if (skill.hasEffects() && target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) == null)
				{
					if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0) // reflect skill effects
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(skill.getId());
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps))
							skill.getEffects(activeChar, target, new Env(shld, sps, false, bsps));
						else
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
					}
				}
			}
		}
		
		if (skill.hasSelfEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			skill.getEffectsSelf(activeChar);
		}
		
		if (skill.isSuicideAttack())
			activeChar.doDie(null);
		
		activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}