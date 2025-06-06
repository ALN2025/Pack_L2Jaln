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
import com.l2jaln.gameserver.skills.basefuncs.Func;
import com.l2jaln.gameserver.templates.skills.L2SkillType;

/**
 * @author Steuf
 */
public class Blow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BLOW
	};
	
	public static final int FRONT = 50;
	public static final int SIDE = 60;
	public static final int BEHIND = 70;
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		final boolean ss = activeChar.isChargedShot(ShotType.SOULSHOT);
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isAlikeDead())
				continue;
			
			byte _successChance = SIDE;
			
			if (activeChar.isBehindTarget())
				_successChance = BEHIND;
			else if (activeChar.isInFrontOfTarget())
				_successChance = FRONT;
			
			if (skill.getName().equals("Backstab"))
			{
				if (activeChar.isBehindTarget())
					_successChance = (byte) Config.BACKSTAB_ATTACK_BEHIND;
				else if (activeChar.isInFrontOfTarget())
					_successChance = (byte) Config.BACKSTAB_ATTACK_FRONT;
				else
					_successChance = (byte) Config.BACKSTAB_ATTACK_SIDE;
			}
			else
			{
				if (activeChar.isBehindTarget())
					_successChance = (byte) Config.BLOW_ATTACK_BEHIND;
				else if (activeChar.isInFrontOfTarget())
					_successChance = (byte) Config.BLOW_ATTACK_FRONT;
				else
					_successChance = (byte) Config.BLOW_ATTACK_SIDE;
			}
			
			// If skill requires Crit or skill requires behind,
			// calculate chance based on DEX, Position and on self BUFF
			/*
			 * if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0) { if (skill.getName().equals("Backstab")) { _successChance = (byte) Config.BACKSTAB_ATTACK_BEHIND; } else { _successChance = (byte) Config.BLOW_ATTACK_BEHIND; } }
			 */
			boolean success = true;
			
			if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
				success = (success && Formulas.calcBlow(activeChar, target, _successChance));
			
			if (success)
			{
				// Calculate skill evasion
				boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
				if (skillIsEvaded)
				{
					if (activeChar instanceof Player)
						((Player) activeChar).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
					
					if (target instanceof Player)
						((Player) target).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(activeChar));
					
					// no futher calculations needed.
					continue;
				}
				
				// Calculate skill reflect
				final byte reflect = Formulas.calcSkillReflect(target, skill);
				if (skill.hasEffects())
				{
					if (reflect == Formulas.SKILL_REFLECT_SUCCEED)
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
					else
					{
						final byte shld = Formulas.calcShldUse(activeChar, target, skill);
						target.stopSkillEffects(skill.getId());
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, true))
						{
							skill.getEffects(activeChar, target, new Env(shld, false, false, false));
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
						}
						else
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					}
				}
				
				byte shld = Formulas.calcShldUse(activeChar, target, skill);
				
				// Crit rate base crit rate for skill, modified with STR bonus
				boolean crit = false;
				if (Formulas.calcCrit(skill.getBaseCritRate() * 10 * Formulas.getSTRBonus(activeChar)))
					crit = true;
				
				double damage = (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, ss);
				if (crit)
				{
					damage *= 2;
					
					// Vicious Stance is special after C5, and only for BLOW skills
					L2Effect vicious = activeChar.getFirstEffect(312);
					if (vicious != null && damage > 1)
					{
						for (Func func : vicious.getStatFuncs())
						{
							final Env env = new Env();
							env.setCharacter(activeChar);
							env.setTarget(target);
							env.setSkill(skill);
							env.setValue(damage);
							
							func.calc(env);
							damage = (int) env.getValue();
						}
					}
				}
				
				target.reduceCurrentHp(damage, activeChar, skill);
				
				// vengeance reflected damage
				if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
				{
					if (target instanceof Player)
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addCharName(activeChar));
					
					if (activeChar instanceof Player)
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PERFORMING_COUNTERATTACK).addCharName(target));
					
					// Formula from Diego post, 700 from rpg tests
					double vegdamage = (700 * target.getPAtk(activeChar) / activeChar.getPDef(target));
					activeChar.reduceCurrentHp(vegdamage, target, skill);
				}
				
				// Manage cast break of the target (calculating rate, sending message...)
				Formulas.calcCastBreak(target, damage);
				
				if (activeChar instanceof Player)
					((Player) activeChar).sendDamageMessage(target, (int) damage, false, true, false);
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
			
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
			
			if (skill.hasSelfEffects())
			{
				final L2Effect effect = activeChar.getFirstEffect(skill.getId());
				if (effect != null && effect.isSelfEffect())
					effect.exit();
				
				skill.getEffectsSelf(activeChar);
			}
			activeChar.setChargedShot(ShotType.SOULSHOT, skill.isStaticReuse());
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}