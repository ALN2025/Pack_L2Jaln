package com.l2jaln.gameserver.skills.basefuncs;

import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.skills.Stats;

public class FuncSub extends Func
{
	public FuncSub(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner, lambda);
	}
	
	@Override
	public void calc(Env env)
	{
		if (cond == null || cond.test(env))
			env.subValue(_lambda.calc(env));
	}
}