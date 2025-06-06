package com.l2jaln.gameserver.skills.effects;

import com.l2jaln.gameserver.model.L2Effect;
import com.l2jaln.gameserver.model.actor.Playable;
import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.templates.skills.L2EffectFlag;
import com.l2jaln.gameserver.templates.skills.L2EffectType;

/**
 * @author kerberos_20
 */
public class EffectProtectionBlessing extends L2Effect
{
	public EffectProtectionBlessing(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PROTECTION_BLESSING;
	}
	
	@Override
	public boolean onStart()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		((Playable) getEffected()).stopProtectionBlessing(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.PROTECTION_BLESSING.getMask();
	}
}