package com.l2jaln.gameserver.skills.funcs;

import com.l2jaln.gameserver.model.actor.Summon;
import com.l2jaln.gameserver.skills.Env;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.skills.Stats;
import com.l2jaln.gameserver.skills.basefuncs.Func;

public class FuncAtkAccuracy extends Func
{
	static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();
	
	public static Func getInstance()
	{
		return _faa_instance;
	}
	
	private FuncAtkAccuracy()
	{
		super(Stats.ACCURACY_COMBAT, 0x10, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		final int level = env.getCharacter().getLevel();
		
		env.addValue(Formulas.BASE_EVASION_ACCURACY[env.getCharacter().getDEX()] + level);
		
		if (env.getCharacter() instanceof Summon)
			env.addValue((level < 60) ? 4 : 5);
	}
}