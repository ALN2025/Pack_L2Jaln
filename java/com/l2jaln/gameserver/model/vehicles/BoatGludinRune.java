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

public class BoatGludinRune implements Runnable
{
	private static final Logger _log = Logger.getLogger(BoatGludinRune.class.getName());
	
	private static final Location OUST_LOC_1 = new Location(-90015, 150422, -3610);
	private static final Location OUST_LOC_2 = new Location(34513, -38009, -3640);
	
	// Time: 1151s
	private static final VehicleLocation[] GLUDIN_TO_RUNE =
	{
		new VehicleLocation(-95686, 155514, -3610, 150, 800),
		new VehicleLocation(-98112, 159040, -3610, 150, 800),
		new VehicleLocation(-104192, 160608, -3610, 200, 1800),
		new VehicleLocation(-109952, 159616, -3610, 250, 1800),
		new VehicleLocation(-112768, 154784, -3610, 290, 1800),
		new VehicleLocation(-114688, 139040, -3610, 290, 1800),
		new VehicleLocation(-115232, 134368, -3610, 290, 1800),
		new VehicleLocation(-113888, 121696, -3610, 290, 1800),
		new VehicleLocation(-107808, 104928, -3610, 290, 1800),
		new VehicleLocation(-97152, 75520, -3610, 290, 800),
		new VehicleLocation(-85536, 67264, -3610, 290, 1800),
		new VehicleLocation(-64640, 55840, -3610, 290, 1800),
		new VehicleLocation(-60096, 44672, -3610, 290, 1800),
		new VehicleLocation(-52672, 37440, -3610, 290, 1800),
		new VehicleLocation(-46144, 33184, -3610, 290, 1800),
		new VehicleLocation(-36096, 24928, -3610, 290, 1800),
		new VehicleLocation(-33792, 8448, -3610, 290, 1800),
		new VehicleLocation(-23776, 3424, -3610, 290, 1000),
		new VehicleLocation(-12000, -1760, -3610, 290, 1000),
		new VehicleLocation(672, 480, -3610, 290, 1800),
		new VehicleLocation(15488, 200, -3610, 290, 1000),
		new VehicleLocation(24736, 164, -3610, 290, 1000),
		new VehicleLocation(32192, -1156, -3610, 290, 1000),
		new VehicleLocation(39200, -8032, -3610, 270, 1000),
		new VehicleLocation(44320, -25152, -3610, 270, 1000),
		new VehicleLocation(40576, -31616, -3610, 250, 800),
		new VehicleLocation(36819, -35315, -3610, 220, 800)
	};
	
	private static final VehicleLocation[] RUNE_DOCK =
	{
		new VehicleLocation(34381, -37680, -3610, 200, 800)
	};
	
	// Time: 967s
	private static final VehicleLocation[] RUNE_TO_GLUDIN =
	{
		new VehicleLocation(32750, -39300, -3610, 150, 800),
		new VehicleLocation(27440, -39328, -3610, 180, 1000),
		new VehicleLocation(21456, -34272, -3610, 200, 1000),
		new VehicleLocation(6608, -29520, -3610, 250, 800),
		new VehicleLocation(4160, -27828, -3610, 270, 800),
		new VehicleLocation(2432, -25472, -3610, 270, 1000),
		new VehicleLocation(-8000, -16272, -3610, 220, 1000),
		new VehicleLocation(-18976, -9760, -3610, 290, 800),
		new VehicleLocation(-23776, 3408, -3610, 290, 800),
		new VehicleLocation(-33792, 8432, -3610, 290, 800),
		new VehicleLocation(-36096, 24912, -3610, 290, 800),
		new VehicleLocation(-46144, 33184, -3610, 290, 800),
		new VehicleLocation(-52688, 37440, -3610, 290, 800),
		new VehicleLocation(-60096, 44672, -3610, 290, 800),
		new VehicleLocation(-64640, 55840, -3610, 290, 800),
		new VehicleLocation(-85552, 67248, -3610, 290, 800),
		new VehicleLocation(-97168, 85264, -3610, 290, 800),
		new VehicleLocation(-107824, 104912, -3610, 290, 800),
		new VehicleLocation(-102151, 135704, -3610, 290, 800),
		new VehicleLocation(-96686, 140595, -3610, 290, 800),
		new VehicleLocation(-95686, 147717, -3610, 250, 800),
		new VehicleLocation(-95686, 148218, -3610, 200, 800)
	};
	
	private static final VehicleLocation[] GLUDIN_DOCK =
	{
		new VehicleLocation(-95686, 150514, -3610, 150, 800)
	};
	
	private final Vehicle _boat;
	private int _cycle = 0;
	private int _shoutCount = 0;
	
	private final CreatureSay ARRIVED_AT_GLUDIN;
	private final CreatureSay ARRIVED_AT_GLUDIN_2;
	private final CreatureSay LEAVE_GLUDIN5;
	private final CreatureSay LEAVE_GLUDIN1;
	private final CreatureSay LEAVING_GLUDIN;
	private final CreatureSay ARRIVED_AT_RUNE;
	private final CreatureSay ARRIVED_AT_RUNE_2;
	private final CreatureSay LEAVE_RUNE5;
	private final CreatureSay LEAVE_RUNE1;
	private final CreatureSay LEAVE_RUNE0;
	private final CreatureSay LEAVING_RUNE;
	private final CreatureSay BUSY_GLUDIN;
	private final CreatureSay BUSY_RUNE;
	
	private final CreatureSay ARRIVAL_RUNE15;
	private final CreatureSay ARRIVAL_RUNE10;
	private final CreatureSay ARRIVAL_RUNE5;
	private final CreatureSay ARRIVAL_RUNE1;
	private final CreatureSay ARRIVAL_GLUDIN15;
	private final CreatureSay ARRIVAL_GLUDIN10;
	private final CreatureSay ARRIVAL_GLUDIN5;
	private final CreatureSay ARRIVAL_GLUDIN1;
	
	private final PlaySound GLUDIN_SOUND;
	private final PlaySound RUNE_SOUND;
	
	private final PlaySound GLUDIN_SOUND_LEAVE_5MIN;
	private final PlaySound GLUDIN_SOUND_LEAVE_1MIN;
	
	private final PlaySound RUNE_SOUND_LEAVE_5MIN;
	private final PlaySound RUNE_SOUND_LEAVE_1MIN;
	
	public BoatGludinRune(Vehicle boat)
	{
		_boat = boat;
		
		ARRIVED_AT_GLUDIN = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_GLUDIN);
		ARRIVED_AT_GLUDIN_2 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_RUNE_10_MINUTES);
		LEAVE_GLUDIN5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_RUNE_5_MINUTES);
		LEAVE_GLUDIN1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_RUNE_1_MINUTE);
		LEAVING_GLUDIN = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_RUNE_NOW);
		
		ARRIVED_AT_RUNE = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.ARRIVED_AT_RUNE);
		ARRIVED_AT_RUNE_2 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES);
		LEAVE_RUNE5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_5_MINUTES);
		LEAVE_RUNE1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_1_MINUTE);
		LEAVE_RUNE0 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_SHORTLY);
		LEAVING_RUNE = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_NOW);
		BUSY_GLUDIN = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_RUNE_GLUDIN_DELAYED);
		BUSY_RUNE = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_GLUDIN_RUNE_DELAYED);
		
		ARRIVAL_RUNE15 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES);
		ARRIVAL_RUNE10 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES);
		ARRIVAL_RUNE5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES);
		ARRIVAL_RUNE1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE);
		ARRIVAL_GLUDIN15 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES);
		ARRIVAL_GLUDIN10 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES);
		ARRIVAL_GLUDIN5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES);
		ARRIVAL_GLUDIN1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE);
		
		GLUDIN_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		RUNE_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", _boat);
		
		GLUDIN_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		GLUDIN_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
		
		RUNE_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", _boat);
		RUNE_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", _boat);
	}
	
	@Override
	public void run()
	{
		try
		{
			switch (_cycle)
			{
				case 0:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVE_GLUDIN5);
					_boat.broadcastPacket(GLUDIN_SOUND_LEAVE_5MIN);
					ThreadPool.schedule(this, 240000);
					break;
				case 1:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVE_GLUDIN1);
					_boat.broadcastPacket(GLUDIN_SOUND_LEAVE_1MIN);
					ThreadPool.schedule(this, 60000);
					break;
				case 2:
					BoatManager.getInstance().dockShip(BoatManager.GLUDIN_HARBOR, false);
					BoatManager.getInstance().broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVING_GLUDIN);
					_boat.broadcastPacket(GLUDIN_SOUND);
					_boat.payForRide(7905, 1, OUST_LOC_1);
					_boat.executePath(GLUDIN_TO_RUNE);
					ThreadPool.schedule(this, 250000);
					break;
				case 3:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE15);
					ThreadPool.schedule(this, 300000);
					break;
				case 4:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE10);
					ThreadPool.schedule(this, 300000);
					break;
				case 5:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE5);
					ThreadPool.schedule(this, 240000);
					break;
				case 6:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE1);
					break;
				case 7:
					if (BoatManager.getInstance().dockBusy(BoatManager.RUNE_HARBOR))
					{
						if (_shoutCount == 0)
							BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], BUSY_RUNE);
						
						_shoutCount++;
						if (_shoutCount > 35)
							_shoutCount = 0;
						
						ThreadPool.schedule(this, 5000);
						return;
					}
					BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, true);
					_boat.executePath(RUNE_DOCK);
					break;
				case 8:
					BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVED_AT_RUNE, ARRIVED_AT_RUNE_2);
					_boat.broadcastPacket(RUNE_SOUND);
					ThreadPool.schedule(this, 300000);
					break;
				case 9:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE5);
					_boat.broadcastPacket(RUNE_SOUND_LEAVE_5MIN);
					ThreadPool.schedule(this, 240000);
					break;
				case 10:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE1);
					_boat.broadcastPacket(RUNE_SOUND_LEAVE_1MIN);
					ThreadPool.schedule(this, 40000);
					break;
				case 11:
					BoatManager.getInstance().broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE0);
					_boat.broadcastPacket(RUNE_SOUND_LEAVE_1MIN);
					ThreadPool.schedule(this, 20000);
					break;
				case 12:
					BoatManager.getInstance().dockShip(BoatManager.RUNE_HARBOR, false);
					BoatManager.getInstance().broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVING_RUNE);
					_boat.broadcastPacket(RUNE_SOUND);
					_boat.payForRide(7904, 1, OUST_LOC_2);
					_boat.executePath(RUNE_TO_GLUDIN);
					ThreadPool.schedule(this, 60000);
					break;
				case 13:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN15);
					ThreadPool.schedule(this, 300000);
					break;
				case 14:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN10);
					ThreadPool.schedule(this, 300000);
					break;
				case 15:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN5);
					ThreadPool.schedule(this, 240000);
					break;
				case 16:
					BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN1);
					break;
				case 17:
					if (BoatManager.getInstance().dockBusy(BoatManager.GLUDIN_HARBOR))
					{
						if (_shoutCount == 0)
							BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], BUSY_GLUDIN);
						
						_shoutCount++;
						if (_shoutCount > 35)
							_shoutCount = 0;
						
						ThreadPool.schedule(this, 5000);
						return;
					}
					BoatManager.getInstance().dockShip(BoatManager.GLUDIN_HARBOR, true);
					_boat.executePath(GLUDIN_DOCK);
					break;
				case 18:
					BoatManager.getInstance().broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVED_AT_GLUDIN, ARRIVED_AT_GLUDIN_2);
					_boat.broadcastPacket(GLUDIN_SOUND);
					ThreadPool.schedule(this, 300000);
					break;
			}
			_shoutCount = 0;
			_cycle++;
			if (_cycle > 18)
				_cycle = 0;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, e.getMessage());
		}
	}
	
	public static void load()
	{
		final Vehicle boat = BoatManager.getInstance().getNewBoat(3, -95686, 150514, -3610, 16723);
		if (boat != null)
		{
			boat.registerEngine(new BoatGludinRune(boat));
			boat.runEngine(180000);
			BoatManager.getInstance().dockShip(BoatManager.GLUDIN_HARBOR, true);
		}
	}
}
