package com.l2jaln.gameserver.skills.conditions;

import com.l2jaln.gameserver.model.pledge.Clan;
import com.l2jaln.gameserver.skills.Env;

/**
 * The Class ConditionPlayerHasCastle.
 * @author MrPoke
 */
public final class ConditionPlayerHasCastle extends Condition
{
	private final int _castle;
	
	/**
	 * Instantiates a new condition player has castle.
	 * @param castle the castle
	 */
	public ConditionPlayerHasCastle(int castle)
	{
		_castle = castle;
	}
	
	/**
	 * @param env the env
	 * @return true, if successful
	 * @see com.l2jaln.gameserver.skills.conditions.Condition#testImpl(com.l2jaln.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
			return false;
		
		Clan clan = env.getPlayer().getClan();
		if (clan == null)
			return _castle == 0;
		
		// Any castle
		if (_castle == -1)
			return clan.hasCastle();
		
		return clan.getCastleId() == _castle;
	}
}