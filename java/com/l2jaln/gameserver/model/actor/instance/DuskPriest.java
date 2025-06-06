package com.l2jaln.gameserver.model.actor.instance;

import com.l2jaln.gameserver.instancemanager.SevenSigns;
import com.l2jaln.gameserver.instancemanager.SevenSigns.CabalType;
import com.l2jaln.gameserver.instancemanager.SevenSigns.SealType;
import com.l2jaln.gameserver.model.actor.template.NpcTemplate;
import com.l2jaln.gameserver.network.serverpackets.ActionFailed;
import com.l2jaln.gameserver.network.serverpackets.NpcHtmlMessage;

public class DuskPriest extends SignsPriest
{
	public DuskPriest(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("Chat"))
			showChatWindow(player);
		else
			super.onBypassFeedback(player, command);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		
		final CabalType winningCabal = SevenSigns.getInstance().getCabalHighestScore();
		
		switch (SevenSigns.getInstance().getPlayerCabal(player.getObjectId()))
		{
			case DUSK:
				if (SevenSigns.getInstance().isCompResultsPeriod())
					filename += "dusk_priest_5.htm";
				else if (SevenSigns.getInstance().isRecruitingPeriod())
					filename += "dusk_priest_6.htm";
				else if (SevenSigns.getInstance().isSealValidationPeriod())
				{
					if (winningCabal == CabalType.DUSK)
					{
						if (winningCabal != SevenSigns.getInstance().getSealOwner(SealType.GNOSIS))
							filename += "dusk_priest_2c.htm";
						else
							filename += "dusk_priest_2a.htm";
					}
					else if (winningCabal == CabalType.NORMAL)
						filename += "dusk_priest_2d.htm";
					else
						filename += "dusk_priest_2b.htm";
				}
				else
					filename += "dusk_priest_1b.htm";
				break;
			
			case DAWN:
				if (SevenSigns.getInstance().isSealValidationPeriod())
					filename += "dusk_priest_3a.htm";
				else
					filename += "dusk_priest_3b.htm";
				break;
			
			default:
				if (SevenSigns.getInstance().isCompResultsPeriod())
					filename += "dusk_priest_5.htm";
				else if (SevenSigns.getInstance().isRecruitingPeriod())
					filename += "dusk_priest_6.htm";
				else if (SevenSigns.getInstance().isSealValidationPeriod())
				{
					if (winningCabal == CabalType.DUSK)
						filename += "dusk_priest_4.htm";
					else if (winningCabal == CabalType.NORMAL)
						filename += "dusk_priest_2d.htm";
					else
						filename += "dusk_priest_2b.htm";
				}
				else
					filename += "dusk_priest_1a.htm";
				break;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
	}
}