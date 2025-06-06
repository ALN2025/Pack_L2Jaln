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

public class BoatInnadrilTour implements Runnable
{
	private static final Logger _log = Logger.getLogger(BoatInnadrilTour.class.getName());
	
	private static final Location OUST_LOC = new Location(107092, 219098, -3952);
	
	// Time: 1867s
	private static final VehicleLocation[] TOUR =
	{
		new VehicleLocation(105129, 226240, -3610, 150, 800),
		new VehicleLocation(90604, 238797, -3610, 150, 800),
		new VehicleLocation(74853, 237943, -3610, 150, 800),
		new VehicleLocation(68207, 235399, -3610, 150, 800),
		new VehicleLocation(63226, 230487, -3610, 150, 800),
		new VehicleLocation(61843, 224797, -3610, 150, 800),
		new VehicleLocation(61822, 203066, -3610, 150, 800),
		new VehicleLocation(59051, 197685, -3610, 150, 800),
		new VehicleLocation(54048, 195298, -3610, 150, 800),
		new VehicleLocation(41609, 195687, -3610, 150, 800),
		new VehicleLocation(35821, 200284, -3610, 150, 800),
		new VehicleLocation(35567, 205265, -3610, 150, 800),
		new VehicleLocation(35617, 222471, -3610, 150, 800),
		new VehicleLocation(37932, 226588, -3610, 150, 800),
		new VehicleLocation(42932, 229394, -3610, 150, 800),
		new VehicleLocation(74324, 245231, -3610, 150, 800),
		new VehicleLocation(81872, 250314, -3610, 150, 800),
		new VehicleLocation(101692, 249882, -3610, 150, 800),
		new VehicleLocation(107907, 256073, -3610, 150, 800),
		new VehicleLocation(112317, 257133, -3610, 150, 800),
		new VehicleLocation(126273, 255313, -3610, 150, 800),
		new VehicleLocation(128067, 250961, -3610, 150, 800),
		new VehicleLocation(128520, 238249, -3610, 150, 800),
		new VehicleLocation(126428, 235072, -3610, 150, 800),
		new VehicleLocation(121843, 234656, -3610, 150, 800),
		new VehicleLocation(120096, 234268, -3610, 150, 800),
		new VehicleLocation(118572, 233046, -3610, 150, 800),
		new VehicleLocation(117671, 228951, -3610, 150, 800),
		new VehicleLocation(115936, 226540, -3610, 150, 800),
		new VehicleLocation(113628, 226240, -3610, 150, 800),
		new VehicleLocation(111300, 226240, -3610, 150, 800),
		new VehicleLocation(111264, 226240, -3610, 150, 800)
	};
	
	private static final VehicleLocation DOCK = TOUR[TOUR.length - 1];
	
	private final Vehicle _boat;
	private int _cycle = 0;
	
	private final CreatureSay ARRIVED_AT_INNADRIL;
	private final CreatureSay LEAVE_INNADRIL5;
	private final CreatureSay LEAVE_INNADRIL1;
	private final CreatureSay LEAVE_INNADRIL0;
	private final CreatureSay LEAVING_INNADRIL;
	
	private final CreatureSay ARRIVAL20;
	private final CreatureSay ARRIVAL15;
	private final CreatureSay ARRIVAL10;
	private final CreatureSay ARRIVAL5;
	private final CreatureSay ARRIVAL1;
	
	private final PlaySound INNADRIL_SOUND;
	
	private final PlaySound INNADRIL_SOUND_LEAVE_5MIN;
	private final PlaySound INNADRIL_SOUND_LEAVE_1MIN;
	
	public BoatInnadrilTour(Vehicle boat)
	{
		_boat = boat;
		
		ARRIVED_AT_INNADRIL = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ANCHOR_10_MINUTES);
		LEAVE_INNADRIL5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_IN_5_MINUTES);
		LEAVE_INNADRIL1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_IN_1_MINUTE);
		LEAVE_INNADRIL0 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_SOON);
		LEAVING_INNADRIL = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_LEAVING);
		
		ARRIVAL20 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_20_MINUTES);
		ARRIVAL15 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_15_MINUTES);
		ARRIVAL10 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_10_MINUTES);
		ARRIVAL5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_5_MINUTES);
		ARRIVAL1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_1_MINUTE);
		
		INNADRIL_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		
		INNADRIL_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		INNADRIL_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
	}
	
	@Override
	public void run()
	{
		try
		{
			switch (_cycle)
			{
				case 0:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL5);
					_boat.broadcastPacket(INNADRIL_SOUND_LEAVE_5MIN);
					ThreadPool.schedule(this, 240000);
					break;
				case 1:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL1);
					_boat.broadcastPacket(INNADRIL_SOUND_LEAVE_1MIN);
					ThreadPool.schedule(this, 40000);
					break;
				case 2:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL0);
					_boat.broadcastPacket(INNADRIL_SOUND_LEAVE_1MIN);
					ThreadPool.schedule(this, 20000);
					break;
				case 3:
					BoatManager.getInstance().broadcastPackets(DOCK, DOCK, LEAVING_INNADRIL, INNADRIL_SOUND);
					_boat.payForRide(0, 1, OUST_LOC);
					_boat.executePath(TOUR);
					ThreadPool.schedule(this, 650000);
					break;
				case 4:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL20);
					ThreadPool.schedule(this, 300000);
					break;
				case 5:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL15);
					ThreadPool.schedule(this, 300000);
					break;
				case 6:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL10);
					ThreadPool.schedule(this, 300000);
					break;
				case 7:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL5);
					ThreadPool.schedule(this, 240000);
					break;
				case 8:
					BoatManager.getInstance().broadcastPacket(DOCK, DOCK, ARRIVAL1);
					break;
				case 9:
					BoatManager.getInstance().broadcastPackets(DOCK, DOCK, ARRIVED_AT_INNADRIL, INNADRIL_SOUND);
					ThreadPool.schedule(this, 300000);
					break;
			}
			_cycle++;
			if (_cycle > 9)
				_cycle = 0;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, e.getMessage());
		}
	}
	
	public static void load()
	{
		final Vehicle boat = BoatManager.getInstance().getNewBoat(4, 111264, 226240, -3610, 32768);
		if (boat != null)
		{
			boat.registerEngine(new BoatInnadrilTour(boat));
			boat.runEngine(180000);
		}
	}
}