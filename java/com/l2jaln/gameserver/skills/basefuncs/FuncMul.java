package com.l2jaln.gameserver.skills.basefuncs;

import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.skills.Stats;

public class FuncMul extends Func
{
	public FuncMul(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner, lambda);
	}
	
	@Override
	public void calc(Env env)
	{
		if (cond == null || cond.test(env))
			env.mulValue(_lambda.calc(env));
	}
}