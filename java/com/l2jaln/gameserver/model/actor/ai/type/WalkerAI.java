package com.l2jaln.gameserver.model.actor.ai.type;

import java.util.List;

import com.l2jaln.gameserver.data.xml.WalkerRouteData;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.ai.CtrlIntention;
import com.l2jaln.gameserver.model.actor.instance.Walker;
import com.l2jaln.gameserver.model.location.WalkerLocation;
import com.l2jaln.gameserver.taskmanager.WalkerTaskManager;

/**
 * This AI is used by {@link Walker}s.<br>
 * <br>
 * We use basic functionalities of AI, notably {@link CtrlIntention} MOVE_TO and onEvtArrived() to motion it.<br>
 * It retains a List of {@link WalkerLocation}s which, together, creates a route, and we save the current node index to find the next WalkerLocation. Once the path is complete, we return to index 0.<br>
 * <br>
 * It is associated to a global task named {@link WalkerTaskManager} to handle individual WalkerLocation delays.
 */
public class WalkerAI extends CreatureAI
{
	private final List<WalkerLocation> _route;
	
	private int _index = 1;
	
	public WalkerAI(Creature creature)
	{
		super(creature);
		
		_route = WalkerRouteData.getInstance().getWalkerRoute(getActor().getNpcId());
		if (!_route.isEmpty())
			setIntention(CtrlIntention.MOVE_TO, _route.get(_index));
	}
	
	@Override
	public Walker getActor()
	{
		return (Walker) super.getActor();
	}
	
	@Override
	protected void onEvtArrived()
	{
		// Retrieve current node.
		WalkerLocation node = _route.get(_index);
		
		if (node.getChat() != null)
			getActor().broadcastNpcSay(node.getChat());
		
		// We freeze the NPC and store it on WalkerTaskManager, which will release it in the future.
		if (node.getDelay() > 0)
			WalkerTaskManager.getInstance().add(getActor(), node.getDelay());
		else
			moveToNextPoint();
	}
	
	/**
	 * Move the {@link Walker} to the next {@link WalkerLocation} of his route.
	 */
	public void moveToNextPoint()
	{
		// Set the next node value.
		if (_index < _route.size() - 1)
			_index++;
		else
			_index = 0;
		
		// Retrieve next node.
		WalkerLocation node = _route.get(_index);
		
		// Running state.
		if (node.doesNpcMustRun())
			getActor().setRunning();
		else
			getActor().setWalking();
		
		setIntention(CtrlIntention.MOVE_TO, node);
	}
}