package com.l2jaln.gameserver.skills.effects;

import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.templates.skills.L2EffectType;

public class EffectBlockBuff extends L2Effect
{
	public EffectBlockBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLOCK_BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}