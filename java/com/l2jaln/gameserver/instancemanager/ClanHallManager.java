package com.l2jaln.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jaln.L2DatabaseFactory;
import com.l2jaln.gameserver.data.sql.ClanTable;
import com.l2jaln.gameserver.model.entity.Auction;
import com.l2jaln.gameserver.model.entity.ClanHall;
import com.l2jaln.gameserver.model.pledge.Clan;
import com.l2jaln.gameserver.model.zone.type.L2ClanHallZone;

/**
 * @author Steuf
 */
public class ClanHallManager
{
	protected static final Logger _log = Logger.getLogger(ClanHallManager.class.getName());
	
	private final Map<String, List<ClanHall>> _allClanHalls;
	private final Map<Integer, ClanHall> _clanHall;
	private final Map<Integer, ClanHall> _freeClanHall;
	private boolean _loaded = false;
	
	public static ClanHallManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public boolean loaded()
	{
		return _loaded;
	}
	
	protected ClanHallManager()
	{
		_allClanHalls = new HashMap<>();
		_clanHall = new HashMap<>();
		_freeClanHall = new HashMap<>();
		load();
	}
	
	/** Load All Clan Hall */
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int id, ownerId, lease, grade = 0;
			String Name, Desc, Location;
			long paidUntil = 0;
			boolean paid = false;
			
			PreparedStatement statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				id = rs.getInt("id");
				Name = rs.getString("name");
				ownerId = rs.getInt("ownerId");
				lease = rs.getInt("lease");
				Desc = rs.getString("desc");
				Location = rs.getString("location");
				paidUntil = rs.getLong("paidUntil");
				grade = rs.getInt("Grade");
				paid = rs.getBoolean("paid");
				
				ClanHall ch = new ClanHall(id, Name, ownerId, lease, Desc, Location, paidUntil, grade, paid);
				
				if (!_allClanHalls.containsKey(Location))
					_allClanHalls.put(Location, new ArrayList<ClanHall>());
				
				_allClanHalls.get(Location).add(ch);
				
				if (ownerId > 0)
				{
					final Clan owner = ClanTable.getInstance().getClan(ownerId);
					if (owner != null)
					{
						_clanHall.put(id, ch);
						owner.setHideout(id);
						continue;
					}
					ch.free();
				}
				_freeClanHall.put(id, ch);
				
				Auction auc = AuctionManager.getInstance().getAuction(id);
				if (auc == null && lease > 0)
					AuctionManager.getInstance().initNPC(id);
			}
			rs.close();
			statement.close();
			
			_log.info("ClanHallManager: Loaded " + getClanHalls().size() + " clan halls.");
			_log.info("ClanHallManager: Loaded " + getFreeClanHalls().size() + " free clan halls.");
			_loaded = true;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception: ClanHallManager.load(): " + e.getMessage(), e);
		}
	}
	
	/**
	 * @return Map with all free ClanHalls
	 */
	public final Map<Integer, ClanHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}
	
	/**
	 * @return Map with all ClanHalls that have owner
	 */
	public final Map<Integer, ClanHall> getClanHalls()
	{
		return _clanHall;
	}
	
	/**
	 * @param location
	 * @return Map with all ClanHalls which are in location
	 */
	public final List<ClanHall> getClanHallsByLocation(String location)
	{
		if (!_allClanHalls.containsKey(location))
			return null;
		
		return _allClanHalls.get(location);
	}
	
	/**
	 * @param chId the clanHall id to check.
	 * @return true if the clanHall is free.
	 */
	public final boolean isFree(int chId)
	{
		return _freeClanHall.containsKey(chId);
	}
	
	/**
	 * Free a ClanHall
	 * @param chId the id of clanHall to release.
	 */
	public final synchronized void setFree(int chId)
	{
		_freeClanHall.put(chId, _clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHideout(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	
	/**
	 * Set owner status for a clan hall.
	 * @param chId the clanHall id to make checks on.
	 * @param clan the new clan owner.
	 */
	public final synchronized void setOwner(int chId, Clan clan)
	{
		if (!_clanHall.containsKey(chId))
		{
			_clanHall.put(chId, _freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}
		else
			_clanHall.get(chId).free();
		
		ClanTable.getInstance().getClan(clan.getClanId()).setHideout(chId);
		_clanHall.get(chId).setOwner(clan);
	}
	
	/**
	 * @param clanHallId the id to use.
	 * @return a clanHall by its id.
	 */
	public final ClanHall getClanHallById(int clanHallId)
	{
		if (_clanHall.containsKey(clanHallId))
			return _clanHall.get(clanHallId);
		
		if (_freeClanHall.containsKey(clanHallId))
			return _freeClanHall.get(clanHallId);
		
		_log.warning("ClanHall (id: " + clanHallId + ") isn't found in clanhall table.");
		return null;
	}
	
	public final ClanHall getNearbyClanHall(int x, int y, int maxDist)
	{
		L2ClanHallZone zone = null;
		
		for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if (zone != null && zone.getDistanceToZone(x, y) < maxDist)
				return ch.getValue();
		}
		for (Map.Entry<Integer, ClanHall> ch : _freeClanHall.entrySet())
		{
			zone = ch.getValue().getZone();
			if (zone != null && zone.getDistanceToZone(x, y) < maxDist)
				return ch.getValue();
		}
		return null;
	}
	
	/**
	 * @param clan the clan to use.
	 * @return a clanHall by its owner.
	 */
	public final ClanHall getClanHallByOwner(Clan clan)
	{
		for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
		{
			if (clan.getClanId() == ch.getValue().getOwnerId())
				return ch.getValue();
		}
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanHallManager _instance = new ClanHallManager();
	}
}