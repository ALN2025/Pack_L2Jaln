package com.l2jaln.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jaln.gameserver.data.DoorTable;
import com.l2jaln.gameserver.data.xml.TeleportLocationData;
import com.l2jaln.gameserver.model.actor.template.NpcTemplate;
import com.l2jaln.gameserver.model.location.TeleportLocation;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.ActionFailed;
import com.l2jaln.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * L2Doormen is the mother class of L2ClanHallDoormen and L2CastleDoormen.
 */
public class Doorman extends Folk
{
	public Doorman(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("open_doors"))
		{
			if (isOwnerClan(player))
			{
				if (isUnderSiege())
				{
					cannotManageDoors(player);
					player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
				}
				else
					openDoors(player, command);
			}
		}
		else if (command.startsWith("close_doors"))
		{
			if (isOwnerClan(player))
			{
				if (isUnderSiege())
				{
					cannotManageDoors(player);
					player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
				}
				else
					closeDoors(player, command);
			}
		}
		else if (command.startsWith("tele"))
		{
			if (isOwnerClan(player))
				doTeleport(player, command);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/doormen/" + getTemplate().getNpcId() + ((!isOwnerClan(player)) ? "-no.htm" : ".htm"));
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	protected void openDoors(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens())
			DoorTable.getInstance().getDoor(Integer.parseInt(st.nextToken())).openMe();
	}
	
	protected void closeDoors(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
		st.nextToken();
		
		while (st.hasMoreTokens())
			DoorTable.getInstance().getDoor(Integer.parseInt(st.nextToken())).closeMe();
	}
	
	protected void cannotManageDoors(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/doormen/busy.htm");
		player.sendPacket(html);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	protected void doTeleport(Player player, String command)
	{
		final int whereTo = Integer.parseInt(command.substring(5).trim());
		TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(whereTo);
		if (list != null)
		{
			if (!player.isAlikeDead())
				player.teleToLocation(list, 0);
		}
		else
			_log.warning("No teleport destination with id: " + whereTo);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	protected boolean isOwnerClan(Player player)
	{
		return true;
	}
	
	protected boolean isUnderSiege()
	{
		return false;
	}
}