package com.l2jaln.gameserver.skills.effects;

import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.templates.skills.L2EffectFlag;
import com.l2jaln.gameserver.templates.skills.L2EffectType;

/**
 * @author mkizub
 */
final class EffectRoot extends L2Effect
{
	public EffectRoot(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.ROOT;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startRooted();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopRooting(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public boolean onSameEffect(L2Effect effect)
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.ROOTED.getMask();
	}
}