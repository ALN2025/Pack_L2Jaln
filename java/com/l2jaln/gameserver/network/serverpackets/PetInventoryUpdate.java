package com.l2jaln.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jaln.gameserver.model.item.instance.ItemInfo;
import com.l2jaln.gameserver.model.item.instance.ItemInstance;
import com.l2jaln.gameserver.model.item.instance.ItemInstance.ItemState;
import com.l2jaln.gameserver.model.item.kind.Item;

/**
 * @author Yme, Advi
 */
public class PetInventoryUpdate extends L2GameServerPacket
{
	private final List<ItemInfo> _items;
	
	public PetInventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
	}
	
	public PetInventoryUpdate()
	{
		this(new ArrayList<ItemInfo>());
	}
	
	public void addItem(ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item));
	}
	
	public void addNewItem(ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, ItemState.ADDED));
	}
	
	public void addModifiedItem(ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, ItemState.MODIFIED));
	}
	
	public void addRemovedItem(ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, ItemState.REMOVED));
	}
	
	public void addItems(List<ItemInstance> items)
	{
		if (items != null)
			for (ItemInstance item : items)
				if (item != null)
					_items.add(new ItemInfo(item));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb3);
		writeH(_items.size());
		
		for (ItemInfo temp : _items)
		{
			Item item = temp.getItem();
			
			writeH(temp.getChange().ordinal());
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(item.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeH(temp.getEquipped());
			writeD(item.getBodyPart());
			writeH(temp.getEnchant());
			writeH(temp.getCustomType2());
		}
	}
}