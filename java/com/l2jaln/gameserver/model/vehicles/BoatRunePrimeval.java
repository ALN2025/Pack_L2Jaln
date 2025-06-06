package com.l2jaln.gameserver.model.vehicles;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jaln.commons.concurrent.ThreadPool;

import com.l2jaln.gameserver.instancemanager.BoatManager;
import com.l2jaln.gameserver.model.actor.Vehicle;
import com.l2jaln.gameserver.model.location.Location;
import com.l2jaln.gameserver.model.location.VehicleLocation;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.clientpackets.Say2;
import com.l2jaln.gameserver.network.serverpackets.CreatureSay;
import com.l2jaln.gameserver.network.serverpackets.PlaySound;

public class BoatRunePrimeval implements Runnable
{
	private static final Logger _log = Logger.getLogger(BoatRunePrimeval.class.getName());
	
	private static final Location OUST_LOC_1 = new Location(34513, -38009, -3640);
	private static final Location OUST_LOC_2 = new Location(10447, -24982, -3664);
	
	// Time: 239s
	private static final VehicleLocation[] RUNE_TO_PRIMEVAL =
	{
		new VehicleLocation(32750, -39300, -3610, 180, 800),
		new VehicleLocation(27440, -39328, -3610, 250, 1000),
		new VehicleLocation(19616, -39360, -3610, 270, 1000),
		new VehicleLocation(3840, -38528, -3610, 270, 1000),
		new VehicleLocation(1664, -37120, -3610, 270, 1000),
		new VehicleLocation(896, -34560, -3610, 180, 1800),
		new VehicleLocation(832, -31104, -3610, 180, 180),
		new VehicleLocation(2240, -29132, -3610, 150, 1800),
		new VehicleLocation(4160, -27828, -3610, 150, 1800),
		new VehicleLocation(5888, -27279, -3610, 150, 1800),
		new VehicleLocation(7000, -27279, -3610, 150, 1800),
		new VehicleLocation(10342, -27279, -3610, 150, 1800)
	};
	
	// Time: 221s
	private static final VehicleLocation[] PRIMEVAL_TO_RUNE =
	{
		new VehicleLocation(15528, -27279, -3610, 180, 800),
		new VehicleLocation(22304, -29664, -3610, 290, 800),
		new VehicleLocation(33824, -26880, -3610, 290, 800),
		new VehicleLocation(38848, -21792, -3610, 240, 1200),
		new VehicleLocation(43424, -22080, -3610, 180, 1800),
		new VehicleLocation(44320, -25152, -3610, 180, 1800),
		new VehicleLocation(40576, -31616, -3610, 250, 800),
		new VehicleLocation(36819, -35315, -3610, 220, 800)
	};
	
	private static final VehicleLocation[] RUNE_DOCK =
	{
		new VehicleLocation(34381, -37680, -3610, 220, 800)
	};
	
	private static final VehicleLocation PRIMEVAL_DOCK = RUNE_TO_PRIMEVAL[RUNE_TO_PRIMEVAL.length - 1];
	
	private final Vehicle _boat;
	private int _cycle = 0;
	private int _shoutCount = 0;
	
	private final CreatureSay ARRIVED_AT_RUNE;
	private final CreatureSay ARRIVED_AT_RUNE_2;
	private final CreatureSay LEAVING_RUNE;
	private final CreatureSay ARRIVED_AT_PRIMEVAL;
	private final CreatureSay ARRIVED_AT_PRIMEVAL_2;
	private final CreatureSay LEAVING_PRIMEVAL;
	private final CreatureSay BUSY_RUNE;
	
	private final PlaySound RUNE_SOUND;
	private final PlaySound PRIMEVAL_SOUND;
	
	public BoatRunePrimeval(Vehicle boat)
	{
		_boat = boat;
		
		ARRIVED_AT_RUNE = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.ARRIVED_AT_RUNE);
		ARRIVED_AT_RUNE_2 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES);
		LEAVING_RUNE = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW);
		ARRIVED_AT_PRIMEVAL = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_PRIMEVAL);
		ARRIVED_AT_PRIMEVAL_2 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_RUNE_3_MINUTES);
		LEAVING_PRIMEVAL = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW);
		BUSY_RUNE = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED);
		
		RUNE_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		PRIMEVAL_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
	}
	
	@Override
	public void run()
	{
		try
		{
			switch (_cycle)
			{
				case 0:
					BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, false);
					BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, LEAVING_RUNE, RUNE_SOUND);
					_boat.payForRide(8925, 1, OUST_LOC_1);
					_boat.executePath(RUNE_TO_PRIMEVAL);
					break;
				case 1:
					BoatManager.getInstance().broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], ARRIVED_AT_PRIMEVAL, ARRIVED_AT_PRIMEVAL_2, PRIMEVAL_SOUND);
					ThreadPool.schedule(this, 180000);
					break;
				case 2:
					BoatManager.getInstance().broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], LEAVING_PRIMEVAL, PRIMEVAL_SOUND);
					_boat.payForRide(8924, 1, OUST_LOC_2);
					_boat.executePath(PRIMEVAL_TO_RUNE);
					break;
				case 3:
					if (BoatManager.getInstance().dockBusy(BoatManager.RUNE_HARBOR))
					{
						if (_shoutCount == 0)
							BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], PRIMEVAL_DOCK, BUSY_RUNE);
						
						_shoutCount++;
						if (_shoutCount > 35)
							_shoutCount = 0;
						
						ThreadPool.schedule(this, 5000);
						return;
					}
					BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, true);
					_boat.executePath(RUNE_DOCK);
					break;
				case 4:
					BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, ARRIVED_AT_RUNE, ARRIVED_AT_RUNE_2, RUNE_SOUND);
					ThreadPool.schedule(this, 180000);
					break;
			}
			_shoutCount = 0;
			_cycle++;
			if (_cycle > 4)
				_cycle = 0;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, e.getMessage());
		}
	}
	
	public static void load()
	{
		final Vehicle boat = BoatManager.getInstance().getNewBoat(5, 34381, -37680, -3610, 40785);
		if (boat != null)
		{
			boat.registerEngine(new BoatRunePrimeval(boat));
			boat.runEngine(180000);
			BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, true);
		}
	}
}