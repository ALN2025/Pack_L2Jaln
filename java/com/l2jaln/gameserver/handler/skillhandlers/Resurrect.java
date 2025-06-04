package com.l2jaln.gameserver.handler.skillhandlers;

import com.l2jaln.Config;
import com.l2jaln.gameserver.handler.ISkillHandler;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.ShotType;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.instance.Pet;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.zone.ZoneId;
import com.l2jaln.gameserver.network.clientpackets.Say2;
import com.l2jaln.gameserver.network.serverpackets.CreatureSay;
import com.l2jaln.gameserver.skills.Formulas;
import com.l2jaln.gameserver.taskmanager.DecayTaskManager;
import com.l2jaln.gameserver.templates.skills.L2SkillType;

public class Resurrect implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.RESURRECT
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		for (WorldObject cha : targets)
		{
			final Creature target = (Creature) cha;
			

			if(Config.ANTIZERG_RES && (activeChar.isInsideZone(ZoneId.RAID) || activeChar.isInsideZone(ZoneId.RAID_NO_FLAG)) && (((Player) activeChar).getClanId() > 0 && ((Player) target).getClanId() > 0 && ((Player) activeChar).getClanId() != ((Player) target).getClanId()) || (((Player) activeChar).getAllyId() > 0 && ((Player) target).getAllyId() > 0 && ((Player) activeChar).getAllyId() != ((Player) target).getAllyId())) 
			 {
				activeChar.sendPacket(new CreatureSay(0, Say2.TELL, "Anti-Zerg:","You can't res in another clan's player!"));
				return;				
			 }
	
			if (activeChar instanceof Player)
			{
				if (cha instanceof Player)
					((Player) cha).reviveRequest((Player) activeChar, skill, false);
				else if (cha instanceof Pet)
				{
					if (((Pet) cha).getOwner() == activeChar)
						target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
					else
						((Pet) cha).getOwner().reviveRequest((Player) activeChar, skill, true);
				}
				else
					target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
			}
			else
			{
				DecayTaskManager.getInstance().cancel(target);
				target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
			}
		}
		activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT) ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}