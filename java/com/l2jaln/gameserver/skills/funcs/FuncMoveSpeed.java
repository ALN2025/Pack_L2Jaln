package com.l2jaln.gameserver.skills.funcs;

import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.skills.Stats;
import com.l2jaln.gameserver.skills.basefuncs.Func;

public class FuncMoveSpeed extends Func
{
	static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();
	
	public static Func getInstance()
	{
		return _fms_instance;
	}
	
	private FuncMoveSpeed()
	{
		super(Stats.RUN_SPEED, 0x30, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.DEX_BONUS[env.getCharacter().getDEX()]);
	}
}