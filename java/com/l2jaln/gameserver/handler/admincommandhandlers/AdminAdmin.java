package com.l2jaln.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.Config;
import com.l2jaln.gameserver.BalanceLoad;
import com.l2jaln.gameserver.data.DoorTable;
import com.l2jaln.gameserver.data.ItemTable;
import com.l2jaln.gameserver.data.NpcTable;
import com.l2jaln.gameserver.data.SkillTable;
import com.l2jaln.gameserver.data.cache.CrestCache;
import com.l2jaln.gameserver.data.cache.HtmCache;
import com.l2jaln.gameserver.data.manager.BuyListManager;
import com.l2jaln.gameserver.data.xml.AdminData;
import com.l2jaln.gameserver.data.xml.AnnouncementData;
import com.l2jaln.gameserver.data.xml.DressMeData;
import com.l2jaln.gameserver.data.xml.MultisellData;
import com.l2jaln.gameserver.data.xml.PkColorTable;
import com.l2jaln.gameserver.data.xml.PvpColorTable;
import com.l2jaln.gameserver.data.xml.TeleportLocationData;
import com.l2jaln.gameserver.data.xml.WalkerRouteData;
import com.l2jaln.gameserver.handler.IAdminCommandHandler;
import com.l2jaln.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jaln.gameserver.instancemanager.OlyClassDamageManager;
import com.l2jaln.gameserver.instancemanager.ZoneManager;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;

import phantom.PhantomNameManager;
import phantom.PhantomTitleManager;
import phantom.Phantom_Attack;
import phantom.Phantom_Town;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>admin/admin1/admin2/admin3/admin4 : the different admin menus.</li>
 * <li>gmlist : includes/excludes active character from /gmlist results.</li>
 * <li>kill : handles the kill command.</li>
 * <li>silence : toggles private messages acceptance mode.</li>
 * <li>tradeoff : toggles trade acceptance mode.</li>
 * <li>reload : reloads specified component.</li>
 * <li>script_load : loads following script. MUSTN'T be used instead of //reload quest !</li>
 * </ul>
 */
public class AdminAdmin implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_admin1",
		"admin_admin2",
		"admin_admin3",
		"admin_admin4",
		"admin_gmlist",
		"admin_kill",
		"admin_silence",
		"admin_tradeoff",
		"admin_reload"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_admin"))
			showMainPage(activeChar, command);
		else if (command.startsWith("admin_gmlist"))
			activeChar.sendMessage((AdminData.getInstance().showOrHideGm(activeChar)) ? "Removed from GMList." : "Registered into GMList.");
		else if (command.startsWith("admin_kill"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if (!st.hasMoreTokens())
			{
				final WorldObject obj = activeChar.getTarget();
				if (!(obj instanceof Creature))
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				else
					kill(activeChar, (Creature) obj);
				
				return true;
			}
			
			String firstParam = st.nextToken();
			Player player = World.getInstance().getPlayer(firstParam);
			if (player != null)
			{
				if (st.hasMoreTokens())
				{
					String secondParam = st.nextToken();
					if (StringUtil.isDigit(secondParam))
					{
						int radius = Integer.parseInt(secondParam);
						for (Creature knownChar : player.getKnownTypeInRadius(Creature.class, radius))
						{
							if (knownChar.equals(activeChar))
								continue;
							
							kill(activeChar, knownChar);
						}
						activeChar.sendMessage("Killed all characters within a " + radius + " unit radius around " + player.getName() + ".");
					}
					else
						activeChar.sendMessage("Invalid radius.");
				}
				else
					kill(activeChar, player);
			}
			else if (StringUtil.isDigit(firstParam))
			{
				int radius = Integer.parseInt(firstParam);
				for (Creature knownChar : activeChar.getKnownTypeInRadius(Creature.class, radius))
					kill(activeChar, knownChar);
				
				activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
			}
		}
		else if (command.startsWith("admin_silence"))
		{
			if (activeChar.isInRefusalMode()) // already in message refusal mode
			{
				activeChar.setInRefusalMode(false);
				activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
			}
			else
			{
				activeChar.setInRefusalMode(true);
				activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
			}
		}
		else if (command.startsWith("admin_tradeoff"))
		{
			try
			{
				String mode = command.substring(15);
				if (mode.equalsIgnoreCase("on"))
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Trade refusal enabled");
				}
				else if (mode.equalsIgnoreCase("off"))
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Trade refusal disabled");
				}
			}
			catch (Exception e)
			{
				if (activeChar.getTradeRefusal())
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Trade refusal disabled");
				}
				else
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Trade refusal enabled");
				}
			}
		}
		else if (command.startsWith("admin_reload"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				do
				{
					String type = st.nextToken();
					if (type.startsWith("admin"))
					{
						AdminData.getInstance().reload();
						activeChar.sendMessage("Admin data has been reloaded.");
					}
					else if (type.startsWith("announcement"))
					{
						AnnouncementData.getInstance().reload();
						activeChar.sendMessage("The content of announcements.xml has been reloaded.");
					}
					else if (type.startsWith("config"))
					{
						Config.loadGameServer();
						activeChar.sendMessage("Configs files have been reloaded.");
						
						if (Config.ENABLE_PVP_COLOR)
							PvpColorTable.getInstance().reload();
						if (Config.ENABLE_PK_COLOR)
							PkColorTable.getInstance().reload();
						Phantom_Town.getInstance().reload();
						Phantom_Attack.getInstance().reload();
						PhantomNameManager.INSTANCE.initialise();
						PhantomTitleManager.INSTANCE.initialise();
					}
					else if (type.startsWith("dress"))
					{
						DressMeData.getInstance().reload();
						activeChar.sendMessage("Dress me have been reloaded.");
					}
					else if (type.startsWith("olly"))
					{
						OlyClassDamageManager.loadConfig();
						activeChar.sendMessage("Class Damages olly has been reloaded.");
					}
					else if (type.equals("balancer"))
					{
						BalanceLoad.LoadEm();
						activeChar.sendMessage("Balance stats for classes has been reloaded.");
					}
					else if (type.startsWith("crest"))
					{
						CrestCache.getInstance().reload();
						activeChar.sendMessage("Crests have been reloaded.");
					}
					else if (type.startsWith("cw"))
					{
						CursedWeaponsManager.getInstance().reload();
						activeChar.sendMessage("Cursed weapons have been reloaded.");
					}
					else if (type.startsWith("door"))
					{
						DoorTable.getInstance().reload();
						activeChar.sendMessage("Doors instance has been reloaded.");
					}
					else if (type.startsWith("htm"))
					{
						HtmCache.getInstance().reload();
						activeChar.sendMessage("The HTM cache has been reloaded.");
					}
					else if (type.startsWith("item"))
					{
						ItemTable.getInstance().reload();
						activeChar.sendMessage("Items' templates have been reloaded.");
					}
					else if (type.equals("multisell"))
					{
						MultisellData.getInstance().reload();
						activeChar.sendMessage("The multisell instance has been reloaded.");
					}
					else if (type.equals("npc"))
					{
						NpcTable.getInstance().reloadAllNpc();
						activeChar.sendMessage("NPCs templates have been reloaded.");
					}
					else if (type.startsWith("npcwalker"))
					{
						WalkerRouteData.getInstance().reload();
						activeChar.sendMessage("Walker routes have been reloaded.");
					}
					else if (type.startsWith("skill"))
					{
						SkillTable.getInstance().reload();
						activeChar.sendMessage("Skills' XMLs have been reloaded.");
					}
					else if (type.startsWith("teleport"))
					{
						TeleportLocationData.getInstance().reload();
						activeChar.sendMessage("Teleport locations have been reloaded.");
					}
					else if (type.startsWith("zone"))
					{
						ZoneManager.getInstance().reload();
						activeChar.sendMessage("Zones have been reloaded.");
					}
					else if (type.startsWith("buylist"))
					{
						BuyListManager.getInstance().load();
						activeChar.sendMessage("Buylist have been reloaded.");
					}
					else
					{
						activeChar.sendMessage("Usage : //reload <acar|announcement|config|crest|door>");
						activeChar.sendMessage("Usage : //reload <htm|item|multisell|npc|npcwalker>");
						activeChar.sendMessage("Usage : //reload <skill|teleport|zone>");
					}
				}
				while (st.hasMoreTokens());
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage : //reload <acar|announcement|config|crest|door>");
				activeChar.sendMessage("Usage : //reload <htm|item|multisell|npc|npcwalker>");
				activeChar.sendMessage("Usage : //reload <skill|teleport|zone>");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void kill(Player activeChar, Creature target)
	{
		if (target instanceof Player)
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar, null);
		else if (target.isChampion())
			target.reduceCurrentHp(target.getMaxHp() * Config.CHAMPION_HP + 1, activeChar, null);
		else
			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null);
	}
	
	static void showMainPage(Player activeChar, String command)
	{
		int mode = 0;
		String filename = null;
		try
		{
			mode = Integer.parseInt(command.substring(11));
		}
		catch (Exception e)
		{
		}
		
		switch (mode)
		{
			case 1:
				filename = "main";
				break;
			case 2:
				filename = "game";
				break;
			case 3:
				filename = "effects";
				break;
			case 4:
				filename = "server";
				break;
			default:
				filename = "main";
				break;
		}
		AdminHelpPage.showHelpPage(activeChar, filename + "_menu.htm");
	}
}