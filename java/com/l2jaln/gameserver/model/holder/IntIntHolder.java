package com.l2jaln.gameserver.model.holder;

import com.l2jaln.gameserver.data.SkillTable;
import com.l2jaln.gameserver.model.L2Skill;

/**
 * A generic int/int container.
 */
public class IntIntHolder
{
	private int _id;
	private int _value;
	
	public IntIntHolder(int id, int value)
	{
		_id = id;
		_value = value;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getValue()
	{
		return _value;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public void setValue(int value)
	{
		_value = value;
	}
	
	/**
	 * @return the L2Skill associated to the id/value.
	 */
	public final L2Skill getSkill()
	{
		return SkillTable.getInstance().getInfo(_id, _value);
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": Id: " + _id + ", Value: " + _value;
	}
}