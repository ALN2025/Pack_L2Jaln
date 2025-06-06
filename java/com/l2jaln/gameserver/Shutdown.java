package com.l2jaln.gameserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jaln.commons.concurrent.ThreadPool;
import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.Config;
import com.l2jaln.L2DatabaseFactory;
import com.l2jaln.gameserver.data.BufferTable;
import com.l2jaln.gameserver.data.sql.ServerMemoTable;
import com.l2jaln.gameserver.instancemanager.CastleManorManager;
import com.l2jaln.gameserver.instancemanager.CoupleManager;
import com.l2jaln.gameserver.instancemanager.FishingChampionshipManager;
import com.l2jaln.gameserver.instancemanager.FourSepulchersManager;
import com.l2jaln.gameserver.instancemanager.GrandBossManager;
import com.l2jaln.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jaln.gameserver.instancemanager.SevenSigns;
import com.l2jaln.gameserver.instancemanager.SevenSignsFestival;
import com.l2jaln.gameserver.instancemanager.ZoneManager;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.entity.Hero;
import com.l2jaln.gameserver.model.olympiad.Olympiad;
import com.l2jaln.gameserver.network.L2GameClient;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jaln.gameserver.network.serverpackets.ServerClose;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;
import com.l2jaln.gameserver.taskmanager.ItemsOnGroundTaskManager;
import com.l2jaln.gameserver.util.Broadcast;

/**
 * This class provides functions for shutting down and restarting the server. It closes all client connections and saves data.
 */
public class Shutdown extends Thread
{
	private static Logger _log = Logger.getLogger(Shutdown.class.getName());
	private static Shutdown _counterInstance = null;
	
	private int _secondsShut;
	private int _shutdownMode;
	
	public static final int SIGTERM = 0;
	public static final int GM_SHUTDOWN = 1;
	public static final int GM_RESTART = 2;
	public static final int ABORT = 3;
	private static final String[] MODE_TEXT =
	{
		"SIGTERM",
		"shutting down",
		"restarting",
		"aborting"
	};
	
	private static void SendServerQuit(int seconds)
	{
		SystemMessage sysm = SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
		sysm.addNumber(seconds);
		Broadcast.toAllOnlinePlayers(sysm);
	}
	
	/**
	 * Default constucter is only used internal to create the shutdown-hook instance
	 */
	protected Shutdown()
	{
		_secondsShut = -1;
		_shutdownMode = SIGTERM;
	}
	
	/**
	 * This creates a countdown instance of Shutdown.
	 * @param seconds how many seconds until shutdown
	 * @param restart true is the server shall restart after shutdown
	 */
	public Shutdown(int seconds, boolean restart)
	{
		if (seconds < 0)
			seconds = 0;
		
		_secondsShut = seconds;
		
		if (restart)
			_shutdownMode = GM_RESTART;
		else
			_shutdownMode = GM_SHUTDOWN;
	}
	
	/**
	 * this function is called, when a new thread starts if this thread is the thread of getInstance, then this is the shutdown hook and we save all data and disconnect all clients. after this thread ends, the server will completely exit if this is not the thread of getInstance, then this is a
	 * countdown thread. we start the countdown, and when we finished it, and it was not aborted, we tell the shutdown-hook why we call exit, and then call exit when the exit status of the server is 1, startServer.sh / startServer.bat will restart the server.
	 */
	@Override
	public void run()
	{
		if (this == SingletonHolder._instance)
		{
			StringUtil.printSection("Under " + MODE_TEXT[_shutdownMode] + " process");
			
			// disconnect players
			try
			{
				disconnectAllCharacters();
				_log.info("All players have been disconnected.");
			}
			catch (Throwable t)
			{
			}
						
			// stop all threadpolls
			ThreadPool.shutdown();
			
			try
			{
				LoginServerThread.getInstance().interrupt();
			}
			catch (Throwable t)
			{
			}
			
			// Seven Signs data is now saved along with Festival data.
			if (!SevenSigns.getInstance().isSealValidationPeriod())
				SevenSignsFestival.getInstance().saveFestivalData(false);
			
			// Save Seven Signs data && status.
			SevenSigns.getInstance().saveSevenSignsData();
			SevenSigns.getInstance().saveSevenSignsStatus();
			_log.info("Seven Signs Festival, general data && status have been saved.");
			
			// Four Sepulchers, stop any working task.
			FourSepulchersManager.getInstance().stop();
			
			// Save zones (grandbosses status)
			ZoneManager.getInstance().save();
			
			// Save raidbosses status
			RaidBossSpawnManager.getInstance().cleanUp();
			_log.info("Raid Bosses data has been saved.");
			
			// Save grandbosses status
			GrandBossManager.getInstance().cleanUp();
			_log.info("World Bosses data has been saved.");
			
			// Save olympiads
			Olympiad.getInstance().saveOlympiadStatus();
			_log.info("Olympiad data has been saved.");
			
			// Save Hero data
			Hero.getInstance().shutdown();
			_log.info("Hero data has been saved.");
			
			// Save all manor data
			CastleManorManager.getInstance().storeMe();
			_log.info("Manors data has been saved.");
			
			// Save Fishing tournament data
			FishingChampionshipManager.getInstance().shutdown();
			_log.info("Fishing Championship data has been saved.");
			
			// Schemes save.
			BufferTable.getInstance().saveSchemes();
			_log.info("BufferTable data has been saved.");
			
			// Couples save.
			if (Config.ALLOW_WEDDING)
			{
				CoupleManager.getInstance().save();
				_log.info("CoupleManager data has been saved.");
			}
			
			// Save server memos.
			ServerMemoTable.getInstance().storeMe();
			_log.info("ServerMemo data has been saved.");
			
			// Save items on ground before closing
			ItemsOnGroundTaskManager.getInstance().save();
			
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
			}
			
			try
			{
				GameServer.getInstance().getSelectorThread().shutdown();
			}
			catch (Throwable t)
			{
			}
			
			try
			{
				L2DatabaseFactory.getInstance().shutdown();
			}
			catch (Throwable t)
			{
			}
			
			// server will quit, when this function ends.
			if (SingletonHolder._instance._shutdownMode == GM_RESTART)
				Runtime.getRuntime().halt(2);
			else
				Runtime.getRuntime().halt(0);
		}
		else
		{
			// shutdown: send warnings and then call exit to start shutdown sequence
			countdown();
			
			switch (_shutdownMode)
			{
				case GM_SHUTDOWN:
					SingletonHolder._instance.setMode(GM_SHUTDOWN);
					SingletonHolder._instance.run();
					System.exit(0);
					break;
				case GM_RESTART:
					SingletonHolder._instance.setMode(GM_RESTART);
					SingletonHolder._instance.run();
					System.exit(2);
					break;
			}
		}
	}
	
	/**
	 * This functions starts a shutdown countdown.<br>
	 * A choice must be made between activeChar or ghostEntity.
	 * @param activeChar GM who issued the shutdown command
	 * @param ghostEntity the entity who issued the shutdown command
	 * @param seconds seconds until shutdown
	 * @param restart true if the server will restart after shutdown
	 */
	public void startShutdown(Player activeChar, String ghostEntity, int seconds, boolean restart)
	{
		if (restart)
			_shutdownMode = GM_RESTART;
		else
			_shutdownMode = GM_SHUTDOWN;
		
		if (activeChar != null)
			_log.warning("GM: " + activeChar.getName() + " (" + activeChar.getObjectId() + ") issued shutdown command, " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds.");
		else if (!ghostEntity.isEmpty())
			_log.warning("Entity: " + ghostEntity + " issued shutdown command, " + MODE_TEXT[_shutdownMode] + " in " + seconds + " seconds.");
		
		if (_shutdownMode > 0)
		{
			switch (seconds)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					SendServerQuit(seconds);
			}
		}
		
		if (_counterInstance != null)
			_counterInstance._abort();
		
		// the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new Shutdown(seconds, restart);
		_counterInstance.start();
	}
	
	/**
	 * This function aborts a running countdown
	 * @param activeChar GM who issued the abort command
	 */
	public void abort(Player activeChar)
	{
		if (_counterInstance != null)
		{
			_log.warning("GM: " + activeChar.getName() + " (" + activeChar.getObjectId() + ") issued shutdown abort, " + MODE_TEXT[_shutdownMode] + " has been stopped.");
			_counterInstance._abort();
			
			Broadcast.announceToOnlinePlayers("Server aborts " + MODE_TEXT[_shutdownMode] + " and continues normal operation.");
		}
	}
	
	/**
	 * set the shutdown mode
	 * @param mode what mode shall be set
	 */
	private void setMode(int mode)
	{
		_shutdownMode = mode;
	}
	
	/**
	 * set shutdown mode to ABORT
	 */
	private void _abort()
	{
		_shutdownMode = ABORT;
	}
	
	/**
	 * this counts the countdown and reports it to all players countdown is aborted if mode changes to ABORT
	 */
	private void countdown()
	{
		try
		{
			while (_secondsShut > 0)
			{
				switch (_secondsShut)
				{
					case 540:
						SendServerQuit(540);
						break;
					case 480:
						SendServerQuit(480);
						break;
					case 420:
						SendServerQuit(420);
						break;
					case 360:
						SendServerQuit(360);
						break;
					case 300:
						SendServerQuit(300);
						break;
					case 240:
						SendServerQuit(240);
						break;
					case 180:
						SendServerQuit(180);
						break;
					case 120:
						SendServerQuit(120);
						break;
					case 60:
						// avoids new players from logging in
						LoginServerThread.getInstance().setServerStatus(ServerStatus.STATUS_DOWN);
						SendServerQuit(60);
						break;
					case 30:
						SendServerQuit(30);
						break;
					case 10:
						SendServerQuit(10);
						break;
					case 5:
						SendServerQuit(5);
						break;
					case 4:
						SendServerQuit(4);
						break;
					case 3:
						SendServerQuit(3);
						break;
					case 2:
						SendServerQuit(2);
						break;
					case 1:
						SendServerQuit(1);
						break;
				}
				
				_secondsShut--;
				
				Thread.sleep(1000);
				
				if (_shutdownMode == ABORT)
				{
					// Rehabilitate previous server status if shutdown is aborted.
					if (LoginServerThread.getInstance().getServerStatus() == ServerStatus.STATUS_DOWN)
						LoginServerThread.getInstance().setServerStatus((Config.SERVER_GMONLY) ? ServerStatus.STATUS_GM_ONLY : ServerStatus.STATUS_AUTO);
					
					break;
				}
			}
		}
		catch (InterruptedException e)
		{
		}
	}
	
	/**
	 * Disconnects all clients from the server
	 */
	private static void disconnectAllCharacters()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			try
			{
				L2GameClient client = player.getClient();
				if (client != null && !client.isDetached())
				{
					client.close(ServerClose.STATIC_PACKET);
					client.setActiveChar(null);
					player.setClient(null);
				}
				player.deleteMe();
			}
			catch (Throwable t)
			{
				_log.log(Level.WARNING, "Failed to logout chararacter: " + player, t);
			}
		}
	}
	
	/**
	 * get the shutdown-hook instance the shutdown-hook instance is created by the first call of this function, but it has to be registrered externaly.
	 * @return instance of Shutdown, to be used as shutdown hook
	 */
	public static Shutdown getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final Shutdown _instance = new Shutdown();
	}
	
	public void autoRestart(int time)
	{
		_shutdownMode = GM_RESTART;
		
		if (_shutdownMode > 0)
		{
			switch (time)
			{
				case 540:
				case 480:
				case 420:
				case 360:
				case 300:
				case 240:
				case 180:
				case 120:
				case 60:
				case 30:
				case 10:
				case 5:
				case 4:
				case 3:
				case 2:
				case 1:
					break;
				default:
					SendServerQuit(time);
			}
		}
		
		if (_counterInstance != null)
			_counterInstance._abort();
		
		// the main instance should only run for shutdown hook, so we start a new instance
		_counterInstance = new Shutdown(time, true);
		_counterInstance.start();
		
	}
	
}