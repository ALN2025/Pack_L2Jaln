package com.l2jaln.gameserver.scripting.tasks;

import com.l2jaln.gameserver.Shutdown;
import com.l2jaln.gameserver.scripting.ScheduledQuest;

public final class ServerShutdown extends ScheduledQuest
{
	private static final int PERIOD = 600; // 10 minutes
	
	public ServerShutdown()
	{
		super(-1, "tasks");
	}
	
	@Override
	public final void onStart()
	{
		new Shutdown(PERIOD, false).start();
	}
	
	@Override
	public final void onEnd()
	{
	}
}