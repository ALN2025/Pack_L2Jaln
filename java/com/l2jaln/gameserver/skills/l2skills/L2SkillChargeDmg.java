package com.l2jaln.gameserver.skills.l2skills;

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
import com.l2jaln.gameserver.templates.StatsSet;

public class L2SkillChargeDmg extends L2Skill
{
	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		double modifier = 0;
		
		if (caster instanceof Player)
			modifier = 0.7 + 0.3 * (((Player) caster).getCharges() + getNumCharges());
		
		final boolean ss = caster.isChargedShot(ShotType.SOULSHOT);
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = ((Creature) obj);
			if (target.isAlikeDead())
				continue;
			
			// Calculate skill evasion
			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, this);
			if (skillIsEvaded)
			{
				if (caster instanceof Player)
					((Player) caster).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
				
				if (target instanceof Player)
					((Player) target).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(caster));
				
				// no futher calculations needed.
				continue;
			}
			
			byte shld = Formulas.calcShldUse(caster, target, this);
			boolean crit = false;
			
			if (getBaseCritRate() > 0)
				crit = Formulas.calcCrit(getBaseCritRate() * 10 * Formulas.getSTRBonus(caster));
			
			// damage calculation, crit is static 2x
			double damage = Formulas.calcPhysDam(caster, target, this, shld, false, ss);
			if (crit)
				damage *= 2;
			
			if (damage > 0)
			{
				byte reflect = Formulas.calcSkillReflect(target, this);
				if (hasEffects())
				{
					if ((reflect & Formulas.SKILL_REFLECT_SUCCEED) != 0)
					{
						caster.stopSkillEffects(getId());
						getEffects(target, caster);
						caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(this));
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(getId());
						if (Formulas.calcSkillSuccess(caster, target, this, shld, true))
						{
							getEffects(caster, target, new Env(shld, false, false, false));
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(this));
						}
						else
							caster.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(this));
					}
				}
				
				double finalDamage = damage * modifier;
				target.reduceCurrentHp(finalDamage, caster, this);
				
				// vengeance reflected damage
				if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
					caster.reduceCurrentHp(damage, target, this);
				
				caster.sendDamageMessage(target, (int) finalDamage, false, crit, false);
			}
			else
				caster.sendDamageMessage(target, 0, false, false, true);
		}
		
		if (hasSelfEffects())
		{
			final L2Effect effect = caster.getFirstEffect(getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			getEffectsSelf(caster);
		}
		
		caster.setChargedShot(ShotType.SOULSHOT, isStaticReuse());
	}
}