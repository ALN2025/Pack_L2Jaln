package com.l2jaln.gameserver.communitybbs.Manager;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;

import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.gameserver.data.cache.HtmCache;
import com.l2jaln.gameserver.data.sql.ClanTable;
import com.l2jaln.gameserver.instancemanager.CastleManager;
import com.l2jaln.gameserver.instancemanager.ClanHallManager;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.entity.Castle;
import com.l2jaln.gameserver.model.entity.ClanHall;
import com.l2jaln.gameserver.model.pledge.Clan;

public class RegionBBSManager extends BaseBBSManager
{
	protected RegionBBSManager()
	{
	}
	
	public static RegionBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@Override
	public void parseCmd(String command, Player activeChar)
	{
		if (command.equals("_bbsloc"))
			showRegionsList(activeChar);
		else if (command.startsWith("_bbsloc"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			
			showRegion(activeChar, Integer.parseInt(st.nextToken()));
		}
		else
			super.parseCmd(command, activeChar);
	}
	
	@Override
	protected String getFolder()
	{
		return "region/";
	}
	
	private static void showRegionsList(Player activeChar)
	{
		final String content = HtmCache.getInstance().getHtm(CB_PATH + "region/castlelist.htm");
		
		final StringBuilder sb = new StringBuilder(500);
		for (Castle castle : CastleManager.getInstance().getCastles())
		{
			final Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
			
			StringUtil.append(sb, "<table><tr><td width=5></td><td width=160><a action=\"bypass _bbsloc;", castle.getCastleId(), "\">", castle.getName(), "</a></td><td width=160>", ((owner != null) ? "<a action=\"bypass _bbsclan;home;" + owner.getClanId() + "\">" + owner.getName() + "</a>" : "None"), "</td><td width=160>", ((owner != null && owner.getAllyId() > 0) ? owner.getAllyName() : "None"), "</td><td width=120>", ((owner != null) ? castle.getTaxPercent() : "0"), "</td><td width=5></td></tr></table><br1><img src=\"L2UI.Squaregray\" width=605 height=1><br1>");
		}
		separateAndSend(content.replace("%castleList%", sb.toString()), activeChar);
	}
	
	private static void showRegion(Player activeChar, int castleId)
	{
		final Castle castle = CastleManager.getInstance().getCastleById(castleId);
		final Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + "region/castle.htm");
		
		content = content.replace("%castleName%", castle.getName());
		content = content.replace("%tax%", Integer.toString(castle.getTaxPercent()));
		content = content.replace("%lord%", ((owner != null) ? owner.getLeaderName() : "None"));
		content = content.replace("%clanName%", ((owner != null) ? "<a action=\"bypass _bbsclan;home;" + owner.getClanId() + "\">" + owner.getName() + "</a>" : "None"));
		content = content.replace("%allyName%", ((owner != null && owner.getAllyId() > 0) ? owner.getAllyName() : "None"));
		content = content.replace("%siegeDate%", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(castle.getSiegeDate().getTimeInMillis()));
		
		final StringBuilder sb = new StringBuilder(200);
		
		final List<ClanHall> clanHalls = ClanHallManager.getInstance().getClanHallsByLocation(castle.getName());
		if (clanHalls != null && !clanHalls.isEmpty())
		{
			sb.append("<br><br><table width=610 bgcolor=A7A19A><tr><td width=5></td><td width=200>Clan Hall Name</td><td width=200>Owning Clan</td><td width=200>Clan Leader Name</td><td width=5></td></tr></table><br1>");
			
			for (ClanHall ch : clanHalls)
			{
				final Clan chOwner = ClanTable.getInstance().getClan(ch.getOwnerId());
				
				StringUtil.append(sb, "<table><tr><td width=5></td><td width=200>", ch.getName(), "</td><td width=200>", ((chOwner != null) ? "<a action=\"bypass _bbsclan;home;" + chOwner.getClanId() + "\">" + chOwner.getName() + "</a>" : "None"), "</td><td width=200>", ((chOwner != null) ? chOwner.getLeaderName() : "None"), "</td><td width=5></td></tr></table><br1><img src=\"L2UI.Squaregray\" width=605 height=1><br1>");
			}
		}
		separateAndSend(content.replace("%hallsList%", sb.toString()), activeChar);
	}
	
	private static class SingletonHolder
	{
		protected static final RegionBBSManager _instance = new RegionBBSManager();
	}
}