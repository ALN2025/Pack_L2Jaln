package com.l2jaln.gameserver.handler.skillhandlers;

import com.l2jaln.commons.math.MathUtil;

import com.l2jaln.gameserver.handler.ISkillHandler;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.ai.CtrlIntention;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.FlyToLocation;
import com.l2jaln.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.network.serverpackets.ValidateLocation;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.templates.skills.L2SkillType;

/**
 * @author Didldak Some parts taken from EffectWarp, which cannot be used for this case.
 */
public class InstantJump implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.INSTANT_JUMP
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		Creature target = (Creature) targets[0];
		
		if (Formulas.calcPhysicalSkillEvasion(target, skill))
		{
			if (activeChar instanceof Player)
				((Player) activeChar).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
			
			return;
		}
		
		int x = 0, y = 0, z = 0;
		
		int px = target.getX();
		int py = target.getY();
		double ph = MathUtil.convertHeadingToDegree(target.getHeading());
		
		ph += 180;
		
		if (ph > 360)
			ph -= 360;
		
		ph = (Math.PI * ph) / 180;
		
		x = (int) (px + (25 * Math.cos(ph)));
		y = (int) (py + (25 * Math.sin(ph)));
		z = target.getZ();
		
		activeChar.getAI().setIntention(CtrlIntention.IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, x, y, z, FlyType.DUMMY));
		activeChar.abortAttack();
		activeChar.abortCast();
		
		activeChar.setXYZ(x, y, z);
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
	}
	
	/**
	 * @see com.l2jaln.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}