package com.l2jaln.gameserver.skills.funcs;

import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.skills.Stats;
import com.l2jaln.gameserver.skills.basefuncs.Func;

public class FuncPAtkMod extends Func
{
	static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();
	
	public static Func getInstance()
	{
		return _fpa_instance;
	}
	
	private FuncPAtkMod()
	{
		super(Stats.POWER_ATTACK, 0x30, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.STR_BONUS[env.getCharacter().getSTR()] * env.getCharacter().getLevelMod());
	}
}