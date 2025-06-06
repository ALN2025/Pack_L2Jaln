/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jaln.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jaln.gameserver.model.actor.Attackable;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.CreatureSay;
import com.l2jaln.gameserver.network.serverpackets.ExCloseMPCC;
import com.l2jaln.gameserver.network.serverpackets.ExOpenMPCC;
import com.l2jaln.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jaln.gameserver.network.serverpackets.SystemMessage;

/**
 * @author chris_00
 */
public class L2CommandChannel
{
	private final List<L2Party> _partys = new CopyOnWriteArrayList<>();
	private Player _commandLeader;
	private int _channelLvl;
	
	/**
	 * Creates a New Command Channel and Add the Leaders party to the CC
	 * @param leader
	 */
	public L2CommandChannel(Player leader)
	{
		_commandLeader = leader;
		_partys.add(leader.getParty());
		_channelLvl = leader.getParty().getLevel();
		
		leader.getParty().setCommandChannel(this);
		leader.getParty().broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_FORMED));
		leader.getParty().broadcastToPartyMembers(ExOpenMPCC.STATIC_PACKET);
	}
	
	/**
	 * Adds a Party to the Command Channel
	 * @param party
	 */
	public void addParty(L2Party party)
	{
		if (party == null)
			return;
		
		_partys.add(party);
		
		if (party.getLevel() > _channelLvl)
			_channelLvl = party.getLevel();
		
		party.setCommandChannel(this);
		party.broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
		party.broadcastToPartyMembers(ExOpenMPCC.STATIC_PACKET);
	}
	
	/**
	 * Removes a Party from the Command Channel
	 * @param party
	 */
	public void removeParty(L2Party party)
	{
		if (party == null)
			return;
		
		_partys.remove(party);
		_channelLvl = 0;
		
		for (L2Party pty : _partys)
		{
			if (pty.getLevel() > _channelLvl)
				_channelLvl = pty.getLevel();
		}
		
		party.setCommandChannel(null);
		party.broadcastToPartyMembers(ExCloseMPCC.STATIC_PACKET);
		
		if (_partys.size() < 2)
		{
			broadcastToChannelMembers(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
			disbandChannel();
		}
	}
	
	/**
	 * disbands the whole Command Channel
	 */
	public void disbandChannel()
	{
		for (L2Party party : _partys)
		{
			if (party != null)
				removeParty(party);
		}
		_partys.clear();
	}
	
	/**
	 * @return overall membercount of the Command Channel
	 */
	public int getMemberCount()
	{
		int count = 0;
		for (L2Party party : _partys)
		{
			if (party != null)
				count += party.getMemberCount();
		}
		return count;
	}
	
	/**
	 * Broadcast packet to every channelmember
	 * @param gsp The packet to send.
	 */
	public void broadcastToChannelMembers(L2GameServerPacket gsp)
	{
		for (L2Party party : _partys)
		{
			if (party != null)
				party.broadcastToPartyMembers(gsp);
		}
	}
	
	public void broadcastCSToChannelMembers(CreatureSay gsp, Player broadcaster)
	{
		for (L2Party party : _partys)
		{
			if (party != null)
				party.broadcastCSToPartyMembers(gsp, broadcaster);
		}
	}
	
	/**
	 * @return list of Parties in Command Channel
	 */
	public List<L2Party> getPartys()
	{
		return _partys;
	}
	
	/**
	 * @return list of all Members in Command Channel
	 */
	public List<Player> getMembers()
	{
		List<Player> members = new ArrayList<>();
		for (L2Party party : _partys)
			members.addAll(party.getPartyMembers());
		
		return members;
	}
	
	/**
	 * Check if a given player is in this command channel.
	 * @param player the player to check
	 * @return {@code true} if he does, {@code false} otherwise
	 */
	public boolean containsPlayer(Player player)
	{
		for (L2Party party : _partys)
		{
			if (party.getPartyMembers().contains(player))
				return true;
		}
		return false;
	}
	
	/**
	 * @return Level of CC
	 */
	public int getLevel()
	{
		return _channelLvl;
	}
	
	/**
	 * @param leader sets the leader of the Command Channel
	 */
	public void setChannelLeader(Player leader)
	{
		_commandLeader = leader;
	}
	
	/**
	 * @return the leader of the Command Channel
	 */
	public Player getChannelLeader()
	{
		return _commandLeader;
	}
	
	/**
	 * Queen Ant, Core, Orfen, Zaken: MemberCount > 36<br>
	 * Baium: MemberCount > 56<br>
	 * Antharas: MemberCount > 225<br>
	 * Valakas: MemberCount > 99<br>
	 * normal RaidBoss: MemberCount > 18
	 * @param obj
	 * @return true if proper condition for RaidWar
	 */
	public boolean meetRaidWarCondition(Attackable obj)
	{
		switch (obj.getNpcId())
		{
			case 29001: // Queen Ant
			case 29006: // Core
			case 29014: // Orfen
			case 29022: // Zaken
				return (getMemberCount() > 36);
			case 29020: // Baium
				return (getMemberCount() > 56);
			case 29019: // Antharas
				return (getMemberCount() > 225);
			case 29028: // Valakas
				return (getMemberCount() > 99);
			default: // normal Raidboss
				return (getMemberCount() > 18);
		}
	}
}