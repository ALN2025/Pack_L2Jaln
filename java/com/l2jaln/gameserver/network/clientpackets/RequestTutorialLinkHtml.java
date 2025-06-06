package com.l2jaln.gameserver.network.clientpackets;

import com.l2jaln.Config;
import com.l2jaln.events.TvT;
import com.l2jaln.gameserver.handler.admincommandhandlers.AdminCustom;
import com.l2jaln.gameserver.handler.voicedcommandhandlers.VoicedMission;
import com.l2jaln.gameserver.instancemanager.BotsPreventionManager;
import com.l2jaln.gameserver.instancemanager.BotsPvPPreventionManager;
import com.l2jaln.gameserver.instancemanager.VoteZoneCommands;
import com.l2jaln.gameserver.model.actor.instance.ClassMaster;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.QuestState;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	String _bypass;
	
	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		BotsPreventionManager.Link(player, _bypass);
		
		BotsPvPPreventionManager.Link(player, _bypass);
	    VoteZoneCommands.Link(player, this._bypass);
		TvT.Link(player, _bypass);

		AdminCustom.NewsLink(player, _bypass);
		AdminCustom.NewsLink2(player, _bypass);
		AdminCustom.NewsLink3(player, _bypass);
		AdminCustom.NewsLink4(player, _bypass);
		AdminCustom.NewsLink5(player, _bypass);
		ClassMaster.onTutorialLink(player, _bypass);
	    AdminCustom.onVIPLink(player, this._bypass);
		
	    if (Config.ACTIVE_MISSION)
	    	VoicedMission.linkMission(player, this._bypass); 
		
		QuestState qs = player.getQuestState("Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent(_bypass, null, player);
	}
}