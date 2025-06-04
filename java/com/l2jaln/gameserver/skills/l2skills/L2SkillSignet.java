package com.l2jaln.gameserver.skills.l2skills;

import com.l2jaln.gameserver.data.NpcTable;
import com.l2jaln.gameserver.idfactory.IdFactory;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.instance.EffectPoint;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.actor.template.NpcTemplate;
import com.l2jaln.gameserver.model.location.Location;
import com.l2jaln.gameserver.templates.StatsSet;

public final class L2SkillSignet extends L2Skill
{
	private final int _effectNpcId;
	public int effectId;
	
	public L2SkillSignet(StatsSet set)
	{
		super(set);
		_effectNpcId = set.getInteger("effectNpcId", -1);
		effectId = set.getInteger("effectId", -1);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		NpcTemplate template = NpcTable.getInstance().getTemplate(_effectNpcId);
		EffectPoint effectPoint = new EffectPoint(IdFactory.getInstance().getNextId(), template, caster);
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		
		int x = caster.getX();
		int y = caster.getY();
		int z = caster.getZ();
		
		if (caster instanceof Player && getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
		{
			Location wordPosition = ((Player) caster).getCurrentSkillWorldPosition();
			
			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		getEffects(caster, effectPoint);
		
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);
	}
}