package com.l2jaln.gameserver.skills.conditions;

import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.skills.Env;

/**
 * @author mkizub
 */
public final class ConditionUsingItemType extends Condition
{
	private final int _mask;
	
	public ConditionUsingItemType(int mask)
	{
		_mask = mask;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.getCharacter() instanceof Player))
			return false;
		
		return (_mask & env.getPlayer().getInventory().getWornMask()) != 0;
	}
}