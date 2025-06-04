package com.l2jaln.gameserver.model.actor.instance;

import com.l2jaln.gameserver.model.actor.template.NpcTemplate;
import com.l2jaln.gameserver.model.base.ClassId;
import com.l2jaln.gameserver.model.base.ClassRace;
import com.l2jaln.gameserver.model.base.ClassType;

public final class VillageMasterFighter extends VillageMaster
{
	public VillageMasterFighter(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected final boolean checkVillageMasterRace(ClassId pclass)
	{
		if (pclass == null)
			return false;
		
		return pclass.getRace() == ClassRace.HUMAN || pclass.getRace() == ClassRace.ELF;
	}
	
	@Override
	protected final boolean checkVillageMasterTeachType(ClassId pclass)
	{
		if (pclass == null)
			return false;
		
		return pclass.getType() == ClassType.FIGHTER;
	}
}