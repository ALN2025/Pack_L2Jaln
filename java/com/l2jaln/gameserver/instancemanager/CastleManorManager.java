package com.l2jaln.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.l2jaln.commons.concurrent.ThreadPool;
import com.l2jaln.commons.random.Rnd;

import com.l2jaln.Config;
import com.l2jaln.L2DatabaseFactory;
import com.l2jaln.gameserver.data.sql.ClanTable;
import com.l2jaln.gameserver.model.entity.Castle;
import com.l2jaln.gameserver.model.itemcontainer.ItemContainer;
import com.l2jaln.gameserver.model.manor.CropProcure;
import com.l2jaln.gameserver.model.manor.Seed;
import com.l2jaln.gameserver.model.manor.SeedProduction;
import com.l2jaln.gameserver.model.pledge.Clan;
import com.l2jaln.gameserver.model.pledge.ClanMember;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.templates.StatsSet;
import com.l2jaln.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CastleManorManager
{
	public enum ManorMode
	{
		DISABLED,
		MODIFIABLE,
		MAINTENANCE,
		APPROVED
	}
	
	protected static Logger _log = Logger.getLogger(CastleManorManager.class.getName());
	
	private static final String LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
	private static final String LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
	
	private static final String INSERT_PRODUCT = "INSERT INTO castle_manor_production VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT_CROP = "INSERT INTO castle_manor_procure VALUES (?, ?, ?, ?, ?, ?, ?)";
	
	private ManorMode _mode = ManorMode.APPROVED;
	
	private Calendar _nextModeChange = null;
	
	private final Map<Integer, Seed> _seeds = new HashMap<>();
	
	private final Map<Integer, List<CropProcure>> _procure = new HashMap<>();
	private final Map<Integer, List<CropProcure>> _procureNext = new HashMap<>();
	private final Map<Integer, List<SeedProduction>> _production = new HashMap<>();
	private final Map<Integer, List<SeedProduction>> _productionNext = new HashMap<>();
	
	protected CastleManorManager()
	{
		if (!Config.ALLOW_MANOR)
		{
			_mode = ManorMode.DISABLED;
			_log.info("CastleManorManager: Manor system is deactivated.");
			return;
		}
		
		// Load static data.
		try
		{
			File f = new File("./data/xml/seeds.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			final StatsSet set = new StatsSet();
			
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if ("seed".equalsIgnoreCase(d.getNodeName()))
				{
					final NamedNodeMap attrs = d.getAttributes();
					for (int i = 0; i < attrs.getLength(); i++)
					{
						final Node att = attrs.item(i);
						set.set(att.getNodeName(), att.getNodeValue());
					}
					
					_seeds.put(set.getInteger("id"), new Seed(set));
					set.clear();
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("CastleManorManager: Error while creating table: " + e);
		}
		_log.info("CastleManorManager: Loaded " + _seeds.size() + " seeds.");
		
		// Load dynamic data.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement stProduction = con.prepareStatement(LOAD_PRODUCTION); PreparedStatement stProcure = con.prepareStatement(LOAD_PROCURE))
		{
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				final int castleId = castle.getCastleId();
				
				// Seed production
				final List<SeedProduction> pCurrent = new ArrayList<>();
				final List<SeedProduction> pNext = new ArrayList<>();
				
				stProduction.clearParameters();
				stProduction.setInt(1, castleId);
				
				try (ResultSet rs = stProduction.executeQuery())
				{
					while (rs.next())
					{
						final SeedProduction sp = new SeedProduction(rs.getInt("seed_id"), rs.getInt("amount"), rs.getInt("price"), rs.getInt("start_amount"));
						if (rs.getBoolean("next_period"))
							pNext.add(sp);
						else
							pCurrent.add(sp);
					}
				}
				_production.put(castleId, pCurrent);
				_productionNext.put(castleId, pNext);
				
				// Seed procure
				final List<CropProcure> current = new ArrayList<>();
				final List<CropProcure> next = new ArrayList<>();
				
				stProcure.clearParameters();
				stProcure.setInt(1, castleId);
				
				try (ResultSet rs = stProcure.executeQuery())
				{
					while (rs.next())
					{
						final CropProcure cp = new CropProcure(rs.getInt("crop_id"), rs.getInt("amount"), rs.getInt("reward_type"), rs.getInt("start_amount"), rs.getInt("price"));
						if (rs.getBoolean("next_period"))
							next.add(cp);
						else
							current.add(cp);
					}
				}
				_procure.put(castleId, current);
				_procureNext.put(castleId, next);
			}
		}
		catch (Exception e)
		{
			_log.info("Error restoring manor data: " + e.getMessage());
		}
		
		// Set mode and start timer
		final Calendar currentTime = Calendar.getInstance();
		final int hour = currentTime.get(Calendar.HOUR_OF_DAY);
		final int min = currentTime.get(Calendar.MINUTE);
		final int maintenanceMin = Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN;
		
		if ((hour >= Config.ALT_MANOR_REFRESH_TIME && min >= maintenanceMin) || hour < Config.ALT_MANOR_APPROVE_TIME || (hour == Config.ALT_MANOR_APPROVE_TIME && min <= Config.ALT_MANOR_APPROVE_MIN))
			_mode = ManorMode.MODIFIABLE;
		else if (hour == Config.ALT_MANOR_REFRESH_TIME && (min >= Config.ALT_MANOR_REFRESH_MIN && min < maintenanceMin))
			_mode = ManorMode.MAINTENANCE;
		
		// Schedule mode change
		scheduleModeChange();
		
		// Schedule autosave
		ThreadPool.scheduleAtFixedRate(this::storeMe, Config.ALT_MANOR_SAVE_PERIOD_RATE, Config.ALT_MANOR_SAVE_PERIOD_RATE);
		
		// Send debug message
		if (Config.DEBUG)
			_log.info("CastleManorManager: Current mode " + _mode.toString());
	}
	
	// -------------------------------------------------------
	// Manor methods
	// -------------------------------------------------------
	private final void scheduleModeChange()
	{
		// Calculate next mode change
		_nextModeChange = Calendar.getInstance();
		_nextModeChange.set(Calendar.SECOND, 0);
		
		switch (_mode)
		{
			case MODIFIABLE:
				_nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_APPROVE_TIME);
				_nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_APPROVE_MIN);
				if (_nextModeChange.before(Calendar.getInstance()))
				{
					_nextModeChange.add(Calendar.DATE, 1);
				}
				break;
			
			case MAINTENANCE:
				_nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_REFRESH_TIME);
				_nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN);
				break;
			
			case APPROVED:
				_nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_REFRESH_TIME);
				_nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_REFRESH_MIN);
				break;
		}
		
		// Schedule mode change
		ThreadPool.schedule(this::changeMode, (_nextModeChange.getTimeInMillis() - System.currentTimeMillis()));
	}
	
	public final void changeMode()
	{
		switch (_mode)
		{
			case APPROVED:
				// Change mode
				_mode = ManorMode.MAINTENANCE;
				
				// Update manor period
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					final Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
					if (owner == null)
						continue;
					
					final int castleId = castle.getCastleId();
					final ItemContainer cwh = owner.getWarehouse();
					
					for (CropProcure crop : _procure.get(castleId))
					{
						if (crop.getStartAmount() > 0)
						{
							// Adding bought crops to clan warehouse
							if (crop.getStartAmount() != crop.getAmount())
							{
								int count = (int) ((crop.getStartAmount() - crop.getAmount()) * 0.9);
								if (count < 1 && Rnd.nextInt(99) < 90)
									count = 1;
								
								if (count > 0)
									cwh.addItem("Manor", getSeedByCrop(crop.getId()).getMatureId(), count, null, null);
							}
							
							// Reserved and not used money giving back to treasury
							if (crop.getAmount() > 0)
								castle.addToTreasuryNoTax(crop.getAmount() * crop.getPrice());
						}
					}
					
					// Change next period to current and prepare next period data
					final List<SeedProduction> _nextProduction = _productionNext.get(castleId);
					final List<CropProcure> _nextProcure = _procureNext.get(castleId);
					
					_production.put(castleId, _nextProduction);
					_procure.put(castleId, _nextProcure);
					
					if (castle.getTreasury() < getManorCost(castleId, false))
					{
						_productionNext.put(castleId, Collections.emptyList());
						_procureNext.put(castleId, Collections.emptyList());
					}
					else
					{
						final List<SeedProduction> production = new ArrayList<>(_nextProduction);
						for (SeedProduction s : production)
							s.setAmount(s.getStartAmount());
						
						_productionNext.put(castleId, production);
						
						final List<CropProcure> procure = new ArrayList<>(_nextProcure);
						for (CropProcure cr : procure)
							cr.setAmount(cr.getStartAmount());
						
						_procureNext.put(castleId, procure);
					}
				}
				
				// Save changes
				storeMe();
				break;
			
			case MAINTENANCE:
				// Notify clan leader about manor mode change
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					final Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
					if (owner != null)
					{
						final ClanMember clanLeader = owner.getLeader();
						if (clanLeader != null && clanLeader.isOnline())
							clanLeader.getPlayerInstance().sendPacket(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
					}
				}
				_mode = ManorMode.MODIFIABLE;
				break;
			
			case MODIFIABLE:
				_mode = ManorMode.APPROVED;
				
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					final Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
					if (owner == null)
						continue;
					
					int slots = 0;
					final int castleId = castle.getCastleId();
					final ItemContainer cwh = owner.getWarehouse();
					
					for (CropProcure crop : _procureNext.get(castleId))
					{
						if (crop.getStartAmount() > 0 && cwh.getItemsByItemId(getSeedByCrop(crop.getId()).getMatureId()) == null)
							slots++;
					}
					
					final long manorCost = getManorCost(castleId, true);
					if (!cwh.validateCapacity(slots) && (castle.getTreasury() < manorCost))
					{
						_productionNext.get(castleId).clear();
						_procureNext.get(castleId).clear();
						
						// Notify clan leader
						final ClanMember clanLeader = owner.getLeader();
						if (clanLeader != null && clanLeader.isOnline())
							clanLeader.getPlayerInstance().sendPacket(SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
					}
					else
						castle.addToTreasuryNoTax(-manorCost);
				}
				break;
		}
		scheduleModeChange();
		
		if (Config.DEBUG)
			_log.info("CastleManorManager: Manor mode changed to " + _mode);
	}
	
	public final void setNextSeedProduction(List<SeedProduction> list, int castleId)
	{
		_productionNext.put(castleId, list);
	}
	
	public final void setNextCropProcure(List<CropProcure> list, int castleId)
	{
		_procureNext.put(castleId, list);
	}
	
	public final static void updateCurrentProduction(int castleId, Collection<SeedProduction> items)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle_manor_production SET amount = ? WHERE castle_id = ? AND seed_id = ? AND next_period = 0"))
		{
			for (SeedProduction sp : items)
			{
				ps.setLong(1, sp.getAmount());
				ps.setInt(2, castleId);
				ps.setInt(3, sp.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e)
		{
			_log.warning("CastleManorManager: Unable to store manor data" + e);
		}
	}
	
	public final static void updateCurrentProcure(int castleId, Collection<CropProcure> items)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement ps = con.prepareStatement("UPDATE castle_manor_procure SET amount = ? WHERE castle_id = ? AND crop_id = ? AND next_period = 0"))
		{
			for (CropProcure sp : items)
			{
				ps.setLong(1, sp.getAmount());
				ps.setInt(2, castleId);
				ps.setInt(3, sp.getId());
				ps.addBatch();
			}
			ps.executeBatch();
		}
		catch (Exception e)
		{
			_log.warning("CastleManorManager: Unable to store manor data" + e);
		}
	}
	
	public final List<SeedProduction> getSeedProduction(int castleId, boolean nextPeriod)
	{
		return (nextPeriod) ? _productionNext.get(castleId) : _production.get(castleId);
	}
	
	public final SeedProduction getSeedProduct(int castleId, int seedId, boolean nextPeriod)
	{
		for (SeedProduction sp : getSeedProduction(castleId, nextPeriod))
		{
			if (sp.getId() == seedId)
				return sp;
		}
		return null;
	}
	
	public final List<CropProcure> getCropProcure(int castleId, boolean nextPeriod)
	{
		return (nextPeriod) ? _procureNext.get(castleId) : _procure.get(castleId);
	}
	
	public final CropProcure getCropProcure(int castleId, int cropId, boolean nextPeriod)
	{
		for (CropProcure cp : getCropProcure(castleId, nextPeriod))
		{
			if (cp.getId() == cropId)
				return cp;
		}
		return null;
	}
	
	public final long getManorCost(int castleId, boolean nextPeriod)
	{
		final List<CropProcure> procure = getCropProcure(castleId, nextPeriod);
		final List<SeedProduction> production = getSeedProduction(castleId, nextPeriod);
		
		long total = 0;
		for (SeedProduction seed : production)
		{
			final Seed s = getSeed(seed.getId());
			total += (s == null) ? 1 : (s.getSeedReferencePrice() * seed.getStartAmount());
		}
		for (CropProcure crop : procure)
		{
			total += (crop.getPrice() * crop.getStartAmount());
		}
		return total;
	}
	
	public final boolean storeMe()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ds = con.prepareStatement("DELETE FROM castle_manor_production");
			PreparedStatement is = con.prepareStatement(INSERT_PRODUCT);
			PreparedStatement dp = con.prepareStatement("DELETE FROM castle_manor_procure");
			PreparedStatement ip = con.prepareStatement(INSERT_CROP))
		{
			// Delete old seeds
			ds.executeUpdate();
			
			// Current production
			for (Map.Entry<Integer, List<SeedProduction>> entry : _production.entrySet())
			{
				for (SeedProduction sp : entry.getValue())
				{
					is.setInt(1, entry.getKey());
					is.setInt(2, sp.getId());
					is.setLong(3, sp.getAmount());
					is.setLong(4, sp.getStartAmount());
					is.setLong(5, sp.getPrice());
					is.setBoolean(6, false);
					is.addBatch();
				}
			}
			
			// Next production
			for (Map.Entry<Integer, List<SeedProduction>> entry : _productionNext.entrySet())
			{
				for (SeedProduction sp : entry.getValue())
				{
					is.setInt(1, entry.getKey());
					is.setInt(2, sp.getId());
					is.setLong(3, sp.getAmount());
					is.setLong(4, sp.getStartAmount());
					is.setLong(5, sp.getPrice());
					is.setBoolean(6, true);
					is.addBatch();
				}
			}
			
			// Execute production batch
			is.executeBatch();
			
			// Delete old procure
			dp.executeUpdate();
			
			// Current procure
			for (Map.Entry<Integer, List<CropProcure>> entry : _procure.entrySet())
			{
				for (CropProcure cp : entry.getValue())
				{
					ip.setInt(1, entry.getKey());
					ip.setInt(2, cp.getId());
					ip.setLong(3, cp.getAmount());
					ip.setLong(4, cp.getStartAmount());
					ip.setLong(5, cp.getPrice());
					ip.setInt(6, cp.getReward());
					ip.setBoolean(7, false);
					ip.addBatch();
				}
			}
			
			// Next procure
			for (Map.Entry<Integer, List<CropProcure>> entry : _procureNext.entrySet())
			{
				for (CropProcure cp : entry.getValue())
				{
					ip.setInt(1, entry.getKey());
					ip.setInt(2, cp.getId());
					ip.setLong(3, cp.getAmount());
					ip.setLong(4, cp.getStartAmount());
					ip.setLong(5, cp.getPrice());
					ip.setInt(6, cp.getReward());
					ip.setBoolean(7, true);
					ip.addBatch();
				}
			}
			
			// Execute procure batch
			ip.executeBatch();
			
			return true;
		}
		catch (Exception e)
		{
			_log.warning("CastleManorManager: Unable to store manor data" + e);
			return false;
		}
	}
	
	public final void resetManorData(int castleId)
	{
		_procure.get(castleId).clear();
		_procureNext.get(castleId).clear();
		_production.get(castleId).clear();
		_productionNext.get(castleId).clear();
	}
	
	public final boolean isUnderMaintenance()
	{
		return _mode == ManorMode.MAINTENANCE;
	}
	
	public final boolean isManorApproved()
	{
		return _mode == ManorMode.APPROVED;
	}
	
	public final boolean isModifiablePeriod()
	{
		return _mode == ManorMode.MODIFIABLE;
	}
	
	public final String getCurrentModeName()
	{
		return _mode.toString();
	}
	
	public final String getNextModeChange()
	{
		return new SimpleDateFormat("dd/MM HH:mm:ss").format(_nextModeChange.getTime());
	}
	
	// -------------------------------------------------------
	// Seed methods
	// -------------------------------------------------------
	public final List<Seed> getCrops()
	{
		final List<Seed> seeds = new ArrayList<>();
		final List<Integer> cropIds = new ArrayList<>();
		for (Seed seed : _seeds.values())
		{
			if (!cropIds.contains(seed.getCropId()))
			{
				seeds.add(seed);
				cropIds.add(seed.getCropId());
			}
		}
		cropIds.clear();
		return seeds;
	}
	
	public final Set<Seed> getSeedsForCastle(int castleId)
	{
		return _seeds.values().stream().filter(s -> s.getCastleId() == castleId).collect(Collectors.toSet());
	}
	
	public final Set<Integer> getSeedIds()
	{
		return _seeds.keySet();
	}
	
	public final Set<Integer> getCropIds()
	{
		return _seeds.values().stream().map(Seed::getCropId).collect(Collectors.toSet());
	}
	
	public final Seed getSeed(int seedId)
	{
		return _seeds.get(seedId);
	}
	
	public final Seed getSeedByCrop(int cropId, int castleId)
	{
		for (Seed s : getSeedsForCastle(castleId))
		{
			if (s.getCropId() == cropId)
				return s;
		}
		return null;
	}
	
	public final Seed getSeedByCrop(int cropId)
	{
		for (Seed s : _seeds.values())
		{
			if (s.getCropId() == cropId)
				return s;
		}
		return null;
	}
	
	// -------------------------------------------------------
	// Static methods
	// -------------------------------------------------------
	public static final CastleManorManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CastleManorManager INSTANCE = new CastleManorManager();
	}
}