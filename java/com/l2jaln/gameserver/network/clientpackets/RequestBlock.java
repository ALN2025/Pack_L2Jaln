package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.gameserver.data.PlayerNameTable;
import com.l2jaln.gameserver.model.BlockList;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;

public final class RequestBlock extends L2GameClientPacket
{
	private static final int BLOCK = 0;
	private static final int UNBLOCK = 1;
	private static final int BLOCKLIST = 2;
	private static final int ALLBLOCK = 3;
	private static final int ALLUNBLOCK = 4;
	
	private String _name;
	private int _type;
	
	@Override
	protected void readImpl()
	{
		_type = readD(); // 0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock
		
		if (_type == BLOCK || _type == UNBLOCK)
			_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		switch (_type)
		{
			case BLOCK:
			case UNBLOCK:
				// Can't block/unblock inexisting or self.
				final int targetId = PlayerNameTable.getInstance().getPlayerObjectId(_name);
				if (targetId <= 0 || activeChar.getObjectId() == targetId)
				{
					activeChar.sendPacket(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST);
					return;
				}
				
				// Can't block a GM character.
				if (PlayerNameTable.getInstance().getPlayerAccessLevel(targetId) > 0)
				{
					activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
					return;
				}
				
				if (_type == BLOCK)
					BlockList.addToBlockList(activeChar, targetId);
				else
					BlockList.removeFromBlockList(activeChar, targetId);
				break;
			
			case BLOCKLIST:
				BlockList.sendListToOwner(activeChar);
				break;
			
			case ALLBLOCK:
				activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);// Update by rocknow
				BlockList.setBlockAll(activeChar, true);
				break;
			
			case ALLUNBLOCK:
				activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);// Update by rocknow
				BlockList.setBlockAll(activeChar, false);
				break;
			
			default:
				_log.info("Unknown 0x0a block type: " + _type);
		}
	}
}