package com.l2jaln.gameserver.skills.effects;

import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.templates.skills.L2EffectFlag;
import com.l2jaln.gameserver.templates.skills.L2EffectType;

public class EffectMute extends L2Effect
{
	public EffectMute(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MUTE;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startMuted();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		// Simply stop the effect
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopMuted(false);
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.MUTED.getMask();
	}
}