package com.l2jaln.gameserver.skills.funcs;

import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.skills.Stats;
import com.l2jaln.gameserver.skills.basefuncs.Func;

public class FuncPAtkSpeed extends Func
{
	static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();
	
	public static Func getInstance()
	{
		return _fas_instance;
	}
	
	private FuncPAtkSpeed()
	{
		super(Stats.POWER_ATTACK_SPEED, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.DEX_BONUS[env.getCharacter().getDEX()]);
	}
}