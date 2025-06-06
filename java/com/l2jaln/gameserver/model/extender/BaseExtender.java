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
package com.l2jaln.gameserver.model.extender;

import com.l2jaln.gameserver.model.WorldObject;

/**
 * @author Mayke
 *
 */
public class BaseExtender
{
	public enum EventType
	{
		LOAD("load"), // null
		STORE("store"), // null
		CAST("cast"), // L2Skill , L2Character, L2Character[]
		ATTACK("attack"), // L2Character
		CRAFT("craft"),
		ENCHANT("enchant"),
		SPAWN("spawn"), // null
		DELETE("delete"), // null
		SETOWNER("setwoner"), // int, String
		DROP("drop"), // null
		DIE("die"), // L2Character
		REVIVE("revive"), // null
		SETINTENTION("setintention"); // CtrlIntention
		public final String name;
		
		EventType(final String name)
		{
			this.name = name;
		}
	}
	
	/**
	 * @param object as L2Object<br>
	 * @return as boolean<br>
	 */
	public static boolean canCreateFor(final WorldObject object)
	{
		return true;
	}
	
	protected WorldObject _owner;
	private BaseExtender _next = null;
	
	/**
	 * @param owner - L2Object
	 */
	public BaseExtender(final WorldObject owner)
	{
		_owner = owner;
	}
	
	/**
	 * @return as Object
	 */
	public WorldObject getOwner()
	{
		return _owner;
	}
	
	/**
	 * onEvent - super.onEvent(event,params);<BR>
	 * <BR>
	 * @param event as String<br>
	 * @param params as Object[]<br>
	 * @return as Object
	 */
	public Object onEvent(final String event, final Object... params)
	{
		if (_next == null)
			return null;
		return _next.onEvent(event, params);
	}
	
	/**
	 * @param simpleClassName as String<br>
	 * @return as BaseExtender - null
	 */
	public BaseExtender getExtender(final String simpleClassName)
	{
		if (this.getClass().getSimpleName().compareTo(simpleClassName) == 0)
			return this;
		else if (_next != null)
			return _next.getExtender(simpleClassName);
		else
			return null;
	}
	
	public void removeExtender(final BaseExtender ext)
	{
		if (_next != null)
			if (_next == ext)
				_next = _next._next;
			else
				_next.removeExtender(ext);
	}
	
	public BaseExtender getNextExtender()
	{
		return _next;
	}
	
	/**
	 * @param newExtender as BaseExtender
	 */
	public void addExtender(final BaseExtender newExtender)
	{
		if (_next == null)
		{
			_next = newExtender;
		}
		else
		{
			_next.addExtender(newExtender);
		}
	}
}
