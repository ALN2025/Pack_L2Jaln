package com.l2jaln.gameserver.skills.funcs;

import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.skills.Stats;
import com.l2jaln.gameserver.skills.basefuncs.Func;

public class FuncMaxMpMul extends Func
{
	static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();
	
	public static Func getInstance()
	{
		return _fmmm_instance;
	}
	
	private FuncMaxMpMul()
	{
		super(Stats.MAX_MP, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.MEN_BONUS[env.getCharacter().getMEN()]);
	}
}