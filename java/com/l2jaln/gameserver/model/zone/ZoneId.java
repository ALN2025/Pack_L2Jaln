package com.l2jaln.gameserver.model.zone;

/**
 * Zone Ids.
 * @author Zoey76
 */
public enum ZoneId
{
	PVP(0),
	PEACE(1),
	SIEGE(2),
	MOTHER_TREE(3),
	CLAN_HALL(4),
	NO_LANDING(5),
	WATER(6),
	JAIL(7),
	MONSTER_TRACK(8),
	CASTLE(9),
	SWAMP(10),
	NO_SUMMON_FRIEND(11),
	NO_STORE(12),
	TOWN(13),
	HQ(14),
	DANGER_AREA(15),
	CAST_ON_ARTIFACT(16),
	NO_RESTART(17),
	SCRIPT(18),
	FLAG(19),
	RAID(20),
	CUSTOM(21),
	BOSS(22),
	ARENA_EVENT(23),
	PVP_CUSTOM(24),
	TOURNAMENT(25),
	OLYMPIAD(26),
	NO_MONSTER(27),
	NO_BISHOP(28),
	SOLO_CUSTOM(29),
	RAID_NO_FLAG(30);

	private final int _id;
	
	private ZoneId(int id)
	{
		_id = id;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public static int getZoneCount()
	{
		return values().length;
	}
}