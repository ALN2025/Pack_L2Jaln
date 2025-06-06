package com.l2jaln.gameserver.handler.skillhandlers;

import java.util.ArrayList;

import com.l2jaln.commons.concurrent.ThreadPool;
import com.l2jaln.commons.random.Rnd;

import com.l2jaln.Config;
import com.l2jaln.gameserver.handler.ISkillHandler;
import com.l2jaln.gameserver.instancemanager.CustomCancelTaskManager;
import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.ShotType;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.templates.skills.L2SkillType;

/**
 * @author DS
 */
public class Cancel implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CANCEL,
		L2SkillType.MAGE_BANE,
		L2SkillType.WARRIOR_BANE
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		// Delimit min/max % success.
		final int minRate = (skill.getSkillType() == L2SkillType.CANCEL) ? 25 : 40;
		final int maxRate = (skill.getSkillType() == L2SkillType.CANCEL) ? 75 : 95;
		
		// Get skill power (which is used as baseRate).
		final double skillPower = skill.getPower();
		
		for (WorldObject obj : targets)
		{
			if (!(obj instanceof Creature))
				continue;
			
			final Creature target = (Creature) obj;
			if (target.isDead())
				continue;
		
			ArrayList<L2Skill> cancelledBuffs = new ArrayList<>();
	
			int lastCanceledSkillId = 0;
			int count = skill.getMaxNegatedEffects();
			
			// Calculate the difference of level between skill level and victim, and retrieve the vuln/prof.
			final int diffLevel = skill.getMagicLevel() - target.getLevel();
			final double skillVuln = Formulas.calcSkillVulnerability(activeChar, target, skill, skill.getSkillType());
			
			for (L2Effect effect : target.getAllEffects())
			{
				// Don't cancel null effects or toggles.
				if (effect == null || effect.getSkill().isToggle() || effect.getSkill().getId() == 5104 || effect.getSkill().getId() == 5105 || (effect.getPeriod() - effect.getTime() < 1200))
					continue;
				
				// Mage && Warrior Bane drop only particular stacktypes.
				switch (skill.getSkillType())
				{
					case MAGE_BANE:
						if ("casting_time_down".equalsIgnoreCase(effect.getStackType()))
							break;
						
						if ("ma_up".equalsIgnoreCase(effect.getStackType()))
							break;
						
						continue;
					
					case WARRIOR_BANE:
						if ("attack_time_down".equalsIgnoreCase(effect.getStackType()))
							break;
						
						if ("speed_up".equalsIgnoreCase(effect.getStackType()))
							break;
						
						continue;
				}
				
				// If that skill effect was already canceled, continue.
				if (effect.getSkill().getId() == lastCanceledSkillId)
					continue;
				
				// Calculate the success chance following previous variables.
				if (calcCancelSuccess(effect.getPeriod(), diffLevel, skillPower, skillVuln, minRate, maxRate))
				{
					if (target instanceof Player)
					{
						if (!cancelledBuffs.contains(effect.getSkill()) && !((Player) target).isOlympiadProtection())
							cancelledBuffs.add(effect.getSkill());
					}

					// Stores the last canceled skill for further use.
					lastCanceledSkillId = effect.getSkill().getId();
					
					// Exit the effect.
					effect.exit();
				}
				
				if (cancelledBuffs.size() > 0 && target instanceof Player)
				{
					((Player) target).setCancel(true);
					ThreadPool.schedule(new CustomCancelTaskManager((Player) target, cancelledBuffs), 10 * 1000);
				}
		
				// Remove 1 to the stack of buffs to remove.
				count--;
				
				// If the stack goes to 0, then break the loop.
				if (count == 0)
					break;
			}
			
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		
		if (skill.hasSelfEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			skill.getEffectsSelf(activeChar);
		}
		activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT) ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
	}
	
	private static boolean calcCancelSuccess(int effectPeriod, int diffLevel, double baseRate, double vuln, int minRate, int maxRate)
	{
		double rate = (2 * diffLevel + baseRate + effectPeriod / 120) * vuln;
		
		if (Config.DEVELOPER)
			_log.info("calcCancelSuccess(): diffLevel:" + diffLevel + ", baseRate:" + baseRate + ", vuln:" + vuln + ", total:" + rate);
		
		if (rate < minRate)
			rate = minRate;
		else if (rate > maxRate)
			rate = maxRate;
		
		return Rnd.get(100) < rate;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}