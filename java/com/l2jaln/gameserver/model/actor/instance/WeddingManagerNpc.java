package com.l2jaln.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jaln.commons.lang.StringUtil;

import com.l2jaln.Config;
import com.l2jaln.gameserver.data.SkillTable.FrequentSkill;
import com.l2jaln.gameserver.instancemanager.CastleManager;
import com.l2jaln.gameserver.instancemanager.CoupleManager;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.ai.CtrlIntention;
import com.l2jaln.gameserver.model.actor.template.NpcTemplate;
import com.l2jaln.gameserver.model.item.instance.ItemInstance;
import com.l2jaln.gameserver.model.itemcontainer.Inventory;
import com.l2jaln.gameserver.network.serverpackets.ActionFailed;
import com.l2jaln.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jaln.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jaln.gameserver.network.serverpackets.MoveToPawn;
import com.l2jaln.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jaln.gameserver.util.Broadcast;

public class WeddingManagerNpc extends Folk
{
	public WeddingManagerNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Calculate the distance between the Player and the L2Npc
			if (!canInteract(player))
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				// Shouldn't be able to see wedding content if the mod isn't activated on configs
				if (!Config.ALLOW_WEDDING)
					sendHtmlMessage(player, "data/html/mods/wedding/disabled.htm");
				else
				{
					// Married people got access to another menu
					if (player.getCoupleId() > 0)
						sendHtmlMessage(player, "data/html/mods/wedding/start2.htm");
					// "Under marriage acceptance" people go to this one
					else if (player.isUnderMarryRequest())
						sendHtmlMessage(player, "data/html/mods/wedding/waitforpartner.htm");
					// And normal players go here :)
					else
						sendHtmlMessage(player, "data/html/mods/wedding/start.htm");
				}
			}
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("AskWedding"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			if (st.hasMoreTokens())
			{
				final Player partner = World.getInstance().getPlayer(st.nextToken());
				if (partner == null)
				{
					sendHtmlMessage(player, "data/html/mods/wedding/notfound.htm");
					return;
				}
				
				// check conditions
				if (!weddingConditions(player, partner))
					return;
				
				// block the wedding manager until an answer is given.
				player.setUnderMarryRequest(true);
				partner.setUnderMarryRequest(true);
				
				// memorize the requesterId for future use, and send a popup to the target
				partner.setRequesterId(player.getObjectId());
				partner.sendPacket(new ConfirmDlg(1983).addString(player.getName() + " asked you to marry. Do you want to start a new relationship ?"));
			}
			else
				sendHtmlMessage(player, "data/html/mods/wedding/notfound.htm");
		}
		else if (command.startsWith("Divorce"))
			CoupleManager.getInstance().deleteCouple(player.getCoupleId());
		else if (command.startsWith("GoToLove"))
		{
			// Find the partner using the couple id.
			final int partnerId = CoupleManager.getInstance().getPartnerId(player.getCoupleId(), player.getObjectId());
			if (partnerId == 0)
			{
				player.sendMessage("Your partner can't be found.");
				return;
			}
			
			final Player partner = World.getInstance().getPlayer(partnerId);
			if (partner == null)
			{
				player.sendMessage("Your partner is not online.");
				return;
			}
			
			// Simple checks to avoid exploits
			if (partner.isInJail() || partner.isInOlympiadMode() || partner.isInDuel() || partner.isFestivalParticipant() || (partner.isInParty() && partner.getParty().isInDimensionalRift()) || partner.isInObserverMode())
			{
				player.sendMessage("Due to the current partner's status, the teleportation failed.");
				return;
			}
			
			if (partner.getClan() != null && CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null && CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().isInProgress())
			{
				player.sendMessage("As your partner is in siege, you can't go to him/her.");
				return;
			}
			
			// If all checks are successfully passed, teleport the player to the partner
			player.teleToLocation(partner.getX(), partner.getY(), partner.getZ(), 20);
		}
	}
	
	/**
	 * Are both partners wearing formal wear ? If Formal Wear check is disabled, returns True in any case.<BR>
	 * @param p1 Player
	 * @param p2 Player
	 * @return boolean
	 */
	private static boolean wearsFormalWear(Player p1, Player p2)
	{
		ItemInstance fw1 = p1.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if (fw1 == null || fw1.getItemId() != 6408)
			return false;
		
		ItemInstance fw2 = p2.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if (fw2 == null || fw2.getItemId() != 6408)
			return false;
		
		return true;
	}
	
	private boolean weddingConditions(Player requester, Player partner)
	{
		// Check if player target himself
		if (partner.getObjectId() == requester.getObjectId())
		{
			sendHtmlMessage(requester, "data/html/mods/wedding/error_wrongtarget.htm");
			return false;
		}
		
		// Sex check
		if (!Config.WEDDING_SAMESEX && partner.getAppearance().getSex() == requester.getAppearance().getSex())
		{
			sendHtmlMessage(requester, "data/html/mods/wedding/error_sex.htm");
			return false;
		}
		
		// Check if player has the target on friendlist
		if (!requester.getFriendList().contains(partner.getObjectId()))
		{
			sendHtmlMessage(requester, "data/html/mods/wedding/error_friendlist.htm");
			return false;
		}
		
		// Target mustn't be already married
		if (partner.getCoupleId() > 0)
		{
			sendHtmlMessage(requester, "data/html/mods/wedding/error_alreadymarried.htm");
			return false;
		}
		
		// Check for Formal Wear
		if (Config.WEDDING_FORMALWEAR && !wearsFormalWear(requester, partner))
		{
			sendHtmlMessage(requester, "data/html/mods/wedding/error_noformal.htm");
			return false;
		}
		
		// Check and reduce wedding price
		if (requester.getAdena() < Config.WEDDING_PRICE || partner.getAdena() < Config.WEDDING_PRICE)
		{
			sendHtmlMessage(requester, "data/html/mods/wedding/error_adena.htm");
			return false;
		}
		
		return true;
	}
	
	public static void justMarried(Player requester, Player partner)
	{
		// Unlock the wedding manager for both users, and set them as married
		requester.setUnderMarryRequest(false);
		partner.setUnderMarryRequest(false);
		
		// reduce adenas amount according to configs
		requester.reduceAdena("Wedding", Config.WEDDING_PRICE, requester.getCurrentFolkNPC(), true);
		partner.reduceAdena("Wedding", Config.WEDDING_PRICE, requester.getCurrentFolkNPC(), true);
		
		// Messages to the couple
		requester.sendMessage("Congratulations, you are now married with " + partner.getName() + " !");
		partner.sendMessage("Congratulations, you are now married with " + requester.getName() + " !");
		
		// Wedding march
		requester.broadcastPacket(new MagicSkillUse(requester, requester, 2230, 1, 1, 0));
		partner.broadcastPacket(new MagicSkillUse(partner, partner, 2230, 1, 1, 0));
		
		// Fireworks
		requester.doCast(FrequentSkill.LARGE_FIREWORK.getSkill());
		partner.doCast(FrequentSkill.LARGE_FIREWORK.getSkill());
		
		Broadcast.announceToOnlinePlayers("Congratulations to " + requester.getName() + " and " + partner.getName() + "! They have been married.");
	}
	
	private void sendHtmlMessage(Player player, String file)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(file);
		html.replace("%objectId%", getObjectId());
		html.replace("%adenasCost%", StringUtil.formatNumber(Config.WEDDING_PRICE));
		html.replace("%needOrNot%", Config.WEDDING_FORMALWEAR ? "will" : "won't");
		player.sendPacket(html);
	}
}