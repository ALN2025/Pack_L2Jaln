/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jaln.gameserver.model.zone.type;

import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.zone.L2SpawnZone;
import com.l2jaln.gameserver.model.zone.ZoneId;

/**
 * An arena
 * @author durgus
 */
public class L2NoMonsterZone extends L2SpawnZone
{
	public L2NoMonsterZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.NO_MONSTER, true);
	}
	
	@Override
	protected void onExit(Creature character)
	{		
		character.setInsideZone(ZoneId.NO_MONSTER, false);
	}
	
	@Override
	public void onDieInside(Creature character)
	{
	}
	
	@Override
	public void onReviveInside(Creature character)
	{
	}
	
}