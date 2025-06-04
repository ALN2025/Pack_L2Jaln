package com.l2jaln.gameserver.skills.effects;

import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.templates.skills.L2EffectType;

class EffectManaDamOverTime extends L2Effect
{
	public EffectManaDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANA_DMG_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		double manaDam = calc();
		
		if (manaDam > getEffected().getCurrentMp())
		{
			if (getSkill().isToggle())
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				getEffected().sendPacket(sm);
				return false;
			}
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
}