package com.l2jaln.gameserver.model.pledge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.l2jaln.L2DatabaseFactory;
import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.base.Sex;

public class ClanMember
{
	private final Clan _clan;
	private int _objectId;
	private String _name;
	private String _title;
	private int _powerGrade;
	private int _level;
	private int _classId;
	private Sex _sex;
	private int _raceOrdinal;
	private Player _player;
	private int _pledgeType;
	private int _apprentice;
	private int _sponsor;
	
	/**
	 * Used to restore a clan member from the database.
	 * @param clan the clan where the clan member belongs.
	 * @param clanMember the clan member result set
	 * @throws SQLException if the columnLabel is not valid or a database error occurs
	 */
	public ClanMember(Clan clan, ResultSet clanMember) throws SQLException
	{
		if (clan == null)
			throw new IllegalArgumentException("Cannot create a clan member with a null clan.");
		
		_clan = clan;
		_name = clanMember.getString("char_name");
		_level = clanMember.getInt("level");
		_classId = clanMember.getInt("classid");
		_objectId = clanMember.getInt("obj_Id");
		_pledgeType = clanMember.getInt("subpledge");
		_title = clanMember.getString("title");
		_powerGrade = clanMember.getInt("power_grade");
		_apprentice = clanMember.getInt("apprentice");
		_sponsor = clanMember.getInt("sponsor");
		_sex = Sex.values()[clanMember.getInt("sex")];
		_raceOrdinal = clanMember.getInt("race");
	}
	
	/**
	 * Creates a clan member from a player instance.
	 * @param clan the clan where the player belongs
	 * @param player the player from which the clan member will be created
	 */
	public ClanMember(Clan clan, Player player)
	{
		if (clan == null)
			throw new IllegalArgumentException("Cannot create a clan member with a null clan.");
		
		_player = player;
		_clan = clan;
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		_objectId = player.getObjectId();
		_pledgeType = player.getPledgeType();
		_powerGrade = player.getPowerGrade();
		_title = player.getTitle();
		_sponsor = 0;
		_apprentice = 0;
		_sex = player.getAppearance().getSex();
		_raceOrdinal = player.getRace().ordinal();
	}
	
	public void setPlayerInstance(Player player)
	{
		if (player == null && _player != null)
		{
			_name = _player.getName();
			_level = _player.getLevel();
			_classId = _player.getClassId().getId();
			_objectId = _player.getObjectId();
			_powerGrade = _player.getPowerGrade();
			_pledgeType = _player.getPledgeType();
			_title = _player.getTitle();
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
			_sex = _player.getAppearance().getSex();
			_raceOrdinal = _player.getRace().ordinal();
		}
		
		if (player != null)
		{
			if (_clan.getLevel() > 3 && player.isClanLeader())
				player.addSiegeSkills();
			
			if (_clan.getReputationScore() >= 0)
			{
				for (L2Skill sk : _clan.getClanSkills())
				{
					if (sk.getMinPledgeClass() <= player.getPledgeClass())
						player.addSkill(sk, false);
				}
			}
		}
		_player = player;
	}
	
	public Player getPlayerInstance()
	{
		return _player;
	}
	
	public boolean isOnline()
	{
		return _player != null && _player.getClient() != null && (!_player.getClient().isDetached() && !_player.isPhantom());
	}
	
	public int getClassId()
	{
		return (_player != null) ? _player.getClassId().getId() : _classId;
	}
	
	public int getLevel()
	{
		return (_player != null) ? _player.getLevel() : _level;
	}
	
	public String getName()
	{
		return (_player != null) ? _player.getName() : _name;
	}
	
	public int getObjectId()
	{
		return (_player != null) ? _player.getObjectId() : _objectId;
	}
	
	public String getTitle()
	{
		return (_player != null) ? _player.getTitle() : _title;
	}
	
	public int getPledgeType()
	{
		return (_player != null) ? _player.getPledgeType() : _pledgeType;
	}
	
	public void setPledgeType(int pledgeType)
	{
		_pledgeType = pledgeType;
		
		if (_player != null)
			_player.setPledgeType(pledgeType);
		else
			updatePledgeType();
	}
	
	public void updatePledgeType()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET subpledge=? WHERE obj_id=?");
			statement.setInt(1, _pledgeType);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
		}
	}
	
	public int getPowerGrade()
	{
		return (_player != null) ? _player.getPowerGrade() : _powerGrade;
	}
	
	public void setPowerGrade(int powerGrade)
	{
		_powerGrade = powerGrade;
		
		if (_player != null)
			_player.setPowerGrade(powerGrade);
		else
			updatePowerGrade();
	}
	
	/**
	 * Update the characters table of the database with power grade.
	 */
	public void updatePowerGrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET power_grade=? WHERE obj_id=?");
			statement.setInt(1, _powerGrade);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
		}
	}
	
	public void setApprenticeAndSponsor(int apprenticeId, int sponsorId)
	{
		_apprentice = apprenticeId;
		_sponsor = sponsorId;
	}
	
	public int getRaceOrdinal()
	{
		return (_player != null) ? _player.getRace().ordinal() : _raceOrdinal;
	}
	
	public Sex getSex()
	{
		return (_player != null) ? _player.getAppearance().getSex() : _sex;
	}
	
	public int getSponsor()
	{
		return (_player != null) ? _player.getSponsor() : _sponsor;
	}
	
	public int getApprentice()
	{
		return (_player != null) ? _player.getApprentice() : _apprentice;
	}
	
	public String getApprenticeOrSponsorName()
	{
		if (_player != null)
		{
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
		}
		
		if (_apprentice != 0)
		{
			ClanMember apprentice = _clan.getClanMember(_apprentice);
			if (apprentice != null)
				return apprentice.getName();
			
			return "Error";
		}
		
		if (_sponsor != 0)
		{
			ClanMember sponsor = _clan.getClanMember(_sponsor);
			if (sponsor != null)
				return sponsor.getName();
			
			return "Error";
		}
		return "";
	}
	
	public Clan getClan()
	{
		return _clan;
	}
	
	/**
	 * Calculate player's pledge class.
	 * @param player The player to test.
	 * @return the pledge class, under an int.
	 */
	public static int calculatePledgeClass(Player player)
	{
		int pledgeClass = 0;
		
		final Clan clan = player.getClan();
		if (clan != null)
		{
			switch (clan.getLevel())
			{
				case 4:
					if (player.isClanLeader())
						pledgeClass = 3;
					break;
				
				case 5:
					if (player.isClanLeader())
						pledgeClass = 4;
					else
						pledgeClass = 2;
					break;
				
				case 6:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						
						case 100:
						case 200:
							pledgeClass = 2;
							break;
						
						case 0:
							if (player.isClanLeader())
								pledgeClass = 5;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 4;
										break;
									
									case -1:
									default:
										pledgeClass = 3;
										break;
								}
							break;
					}
					break;
				
				case 7:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						
						case 100:
						case 200:
							pledgeClass = 3;
							break;
						
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 2;
							break;
						
						case 0:
							if (player.isClanLeader())
								pledgeClass = 7;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 6;
										break;
									
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 5;
										break;
									
									case -1:
									default:
										pledgeClass = 4;
										break;
								}
							break;
					}
					break;
				
				case 8:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						
						case 100:
						case 200:
							pledgeClass = 4;
							break;
						
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 3;
							break;
						
						case 0:
							if (player.isClanLeader())
								pledgeClass = 8;
							else
								switch (clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 7;
										break;
									
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 6;
										break;
									
									case -1:
									default:
										pledgeClass = 5;
										break;
								}
							break;
					}
					break;
				
				default:
					pledgeClass = 1;
					break;
			}
		}

		return pledgeClass;
	}
	
	public void saveApprenticeAndSponsor(int apprentice, int sponsor)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET apprentice=?,sponsor=? WHERE obj_Id=?");
			statement.setInt(1, apprentice);
			statement.setInt(2, sponsor);
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
		}
	}
}