package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.Config;
import com.l2jaln.gameserver.data.cache.HtmCache;
import com.l2jaln.gameserver.data.manager.BuyListManager;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Fisherman;
import com.l2jaln.gameserver.model.actor.instance.Merchant;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.model.buylist.NpcBuyList;
import com.l2jaln.gameserver.model.buylist.Product;
import com.l2jaln.gameserver.model.holder.IntIntHolder;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.ItemList;
import com.l2jaln.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jaln.gameserver.network.serverpackets.StatusUpdate;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;

public final class RequestBuyItem extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 8; // length of the one item
	
	private int _listId;
	private IntIntHolder[] _items = null;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
			return;
		
		_items = new IntIntHolder[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			int cnt = readD();
			
			if (itemId < 1 || cnt < 1)
			{
				_items = null;
				return;
			}
			
			_items[i] = new IntIntHolder(itemId, cnt);
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (_items == null)
			return;
		
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isProcessingTransaction() || player.isInStoreMode())
        {
            player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
            return;
        }
		
		// We retrieve the buylist.
		final NpcBuyList buyList = BuyListManager.getInstance().getBuyList(_listId);
		if (buyList == null)
			return;
		
		double castleTaxRate = 0;
		Npc merchant = null;
		
		// If buylist is associated to a NPC, we retrieve the target.
		if (buyList.getNpcId() > 0)
		{
			final WorldObject target = player.getTarget();
			if (target instanceof Merchant)
				merchant = (Npc) target;
			
			if (merchant == null || !buyList.isNpcAllowed(merchant.getNpcId()) || !merchant.canInteract(player))
				return;
			
			if (merchant.getCastle() != null)
				castleTaxRate = merchant.getCastle().getTaxRate();
		}
		
		long totalPrice = 0;
		int slots = 0;
		long weight = 0;
		
		for (IntIntHolder i : _items)
		{
			int price = -1;
			
			final Product product = buyList.getProductByItemId(i.getId());
			if (product == null)
				return;
			
			if (!product.getItem().isStackable() && i.getValue() > 1)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			price = product.getPrice();
			if (i.getId() >= 3960 && i.getId() <= 4026)
				price *= Config.RATE_SIEGE_GUARDS_PRICE;
			
			if (price < 0)
				return;
			
			if (price == 0 && !player.isGM())
				return;
			
			if (product.hasLimitedStock())
			{
				// trying to buy more then available
				if (i.getValue() > product.getCount())
					return;
			}
						
			// first calculate price per item with tax, then multiply by count
			price = (int) (price * (1 + castleTaxRate));
			totalPrice += (long)i.getValue() * price;
			// Check for overflow
			if (Integer.MAX_VALUE / i.getValue() < price || totalPrice > Integer.MAX_VALUE || totalPrice < 0)
				return;
			
			weight += (long)i.getValue() * product.getItem().getWeight();
			if (!product.getItem().isStackable())
				slots += i.getValue();
			else if (player.getInventory().getItemByItemId(i.getId()) == null)
				slots++;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		
		if (weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight((int)weight))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		// Charge buyer and add tax to castle treasury if not owned by npc clan
		if (!player.reduceAdena("Buy", (int)totalPrice, player.getCurrentFolkNPC(), false))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		// Proceed the purchase
		for (IntIntHolder i : _items)
		{
			final Product product = buyList.getProductByItemId(i.getId());
			if (product == null)
				continue;
			
			if (product.hasLimitedStock())
			{
				if (product.decreaseCount(i.getValue()))
					player.getInventory().addItem("Buy", i.getId(), i.getValue(), player, merchant);
			}
			else
				player.getInventory().addItem("Buy", i.getId(), i.getValue(), player, merchant);
		}
		
		// Add to castle treasury and send the htm, if existing.
		if (merchant != null)
		{
			if (merchant.getCastle() != null)
				merchant.getCastle().addToTreasury((int) (totalPrice * castleTaxRate));
			
			String htmlFolder = "";
			if (merchant instanceof Fisherman)
				htmlFolder = "fisherman";
			else if (merchant instanceof Merchant)
				htmlFolder = "merchant";
			
			if (!htmlFolder.isEmpty())
			{
				final String content = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-bought.htm");
				if (content != null)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(merchant.getObjectId());
					html.setHtml(content);
					html.replace("%objectId%", merchant.getObjectId());
					player.sendPacket(html);
				}
			}
		}
		
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
	}
}