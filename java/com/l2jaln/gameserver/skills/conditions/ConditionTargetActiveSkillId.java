package com.l2jaln.gameserver.skills.conditions;

import com.l2jaln.gameserver.skills.Env;

/**
 * The Class ConditionTargetActiveSkillId.
 */
public class ConditionTargetActiveSkillId extends Condition
{
	private final int _skillId;
	
	/**
	 * Instantiates a new condition target active skill id.
	 * @param skillId the skill id
	 */
	public ConditionTargetActiveSkillId(int skillId)
	{
		_skillId = skillId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.getTarget().getSkill(_skillId) != null;
	}
}