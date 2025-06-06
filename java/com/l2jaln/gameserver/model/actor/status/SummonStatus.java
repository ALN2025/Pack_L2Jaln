package com.l2jaln.gameserver.model.actor.status;

import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.Summon;
import com.l2jaln.gameserver.model.actor.ai.CtrlEvent;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.actor.instance.Servitor;
import com.l2jaln.gameserver.model.entity.Duel.DuelState;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;

public class SummonStatus extends PlayableStatus
{
	public SummonStatus(Summon activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	@Override
	public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		if (getActiveChar().isDead())
			return;
		
		final Player owner = getActiveChar().getOwner();
		
		// We deny the duel, no matter if damage has been done or not.
		if (attacker != null)
		{
			final Player attackerPlayer = attacker.getActingPlayer();
			if (attackerPlayer != null && (owner == null || owner.getDuelId() != attackerPlayer.getDuelId()))
				attackerPlayer.setDuelState(DuelState.INTERRUPTED);
		}
		
		super.reduceHp(value, attacker, awake, isDOT, isHPConsumption);
		
		// Since damages have been done, we can send damage message and EVT_ATTACKED notification.
		if (attacker != null)
		{
			if (!isDOT && owner != null)
				owner.sendPacket(SystemMessage.getSystemMessage((getActiveChar() instanceof Servitor) ? SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1 : SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1).addCharName(attacker).addNumber((int) value));
			
			getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
		}
	}
	
	@Override
	public Summon getActiveChar()
	{
		return (Summon) super.getActiveChar();
	}
}