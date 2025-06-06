package com.l2jaln.gameserver.skills.conditions;

import com.l2jaln.gameserver.skills.Env;

/**
 * The Class ConditionPlayerCharges.
 */
public class ConditionPlayerCharges extends Condition
{
	
	private final int _charges;
	
	/**
	 * Instantiates a new condition player charges.
	 * @param charges the charges
	 */
	public ConditionPlayerCharges(int charges)
	{
		_charges = charges;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.getPlayer() != null && env.getPlayer().getCharges() >= _charges;
	}
}