package com.l2jaln.gameserver.skills.conditions;

import com.l2jaln.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionPlayerLevel extends Condition
{
	private final int _level;
	
	public ConditionPlayerLevel(int level)
	{
		_level = level;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getLevel() >= _level;
	}
}