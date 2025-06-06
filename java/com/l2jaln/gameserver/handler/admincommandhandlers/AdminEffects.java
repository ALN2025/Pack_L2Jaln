package com.l2jaln.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jaln.gameserver.data.SkillTable;
import com.l2jaln.gameserver.handler.IAdminCommandHandler;
import com.l2jaln.gameserver.model.World;
import com.l2jaln.gameserver.model.WorldObject;
import com.l2jaln.gameserver.model.actor.Creature;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.Summon;
import com.l2jaln.gameserver.model.actor.instance.Chest;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.network.SystemMessageId;
import com.l2jaln.gameserver.network.serverpackets.Earthquake;
import com.l2jaln.gameserver.network.serverpackets.ExRedSky;
import com.l2jaln.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jaln.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jaln.gameserver.network.serverpackets.PlaySound;
import com.l2jaln.gameserver.network.serverpackets.SSQInfo;
import com.l2jaln.gameserver.network.serverpackets.SocialAction;
import com.l2jaln.gameserver.network.serverpackets.StopMove;
import com.l2jaln.gameserver.network.serverpackets.SunRise;
import com.l2jaln.gameserver.network.serverpackets.SunSet;
import com.l2jaln.gameserver.util.Broadcast;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>hide = makes yourself invisible or visible.</li>
 * <li>earthquake = causes an earthquake of a given intensity and duration around you.</li>
 * <li>gmspeed = temporary Super Haste effect.</li>
 * <li>para/unpara = paralyze/remove paralysis from target.</li>
 * <li>para_all/unpara_all = same as para/unpara, affects the whole world.</li>
 * <li>polyself/unpolyself = makes you look as a specified mob.</li>
 * <li>social = forces an Creature instance to broadcast social action packets.</li>
 * <li>effect = forces an Creature instance to broadcast MSU packets.</li>
 * <li>abnormal = force changes over an Creature instance's abnormal state.</li>
 * <li>play_sound/jukebox = Music broadcasting related commands.</li>
 * <li>atmosphere = sky change related commands.</li>
 * </ul>
 */
public class AdminEffects implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_hide",
		"admin_earthquake",
		"admin_earthquake_menu",
		"admin_gmspeed",
		"admin_gmspeed_menu",
		"admin_unpara_all",
		"admin_para_all",
		"admin_unpara",
		"admin_para",
		"admin_unpara_all_menu",
		"admin_para_all_menu",
		"admin_unpara_menu",
		"admin_para_menu",
		"admin_social",
		"admin_social_menu",
		"admin_effect",
		"admin_effect_menu",
		"admin_abnormal",
		"admin_abnormal_menu",
		"admin_jukebox",
		"admin_play_sound",
		"admin_atmosphere",
		"admin_atmosphere_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_hide"))
		{
			if (!activeChar.getAppearance().getInvisible())
			{
				activeChar.getAppearance().setInvisible();
				activeChar.decayMe();
				activeChar.broadcastUserInfo();
				activeChar.spawnMe();
			}
			else
			{
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
			}
		}
		else if (command.startsWith("admin_earthquake"))
		{
			try
			{
				activeChar.broadcastPacket(new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use: //earthquake <intensity> <duration>");
			}
		}
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				String type = st.nextToken();
				String state = st.nextToken();
				
				L2GameServerPacket packet = null;
				
				if (type.equals("ssqinfo"))
				{
					if (state.equals("dawn"))
						packet = SSQInfo.DAWN_SKY_PACKET;
					else if (state.equals("dusk"))
						packet = SSQInfo.DUSK_SKY_PACKET;
					else if (state.equals("red"))
						packet = SSQInfo.RED_SKY_PACKET;
					else if (state.equals("regular"))
						packet = SSQInfo.REGULAR_SKY_PACKET;
				}
				else if (type.equals("sky"))
				{
					if (state.equals("night"))
						packet = SunSet.STATIC_PACKET;
					else if (state.equals("day"))
						packet = SunRise.STATIC_PACKET;
					else if (state.equals("red"))
						packet = new ExRedSky(10);
				}
				else
				{
					activeChar.sendMessage("Usage: //atmosphere <ssqinfo dawn|dusk|red|regular>");
					activeChar.sendMessage("Usage: //atmosphere <sky day|night|red>");
				}
				
				if (packet != null)
					Broadcast.toAllOnlinePlayers(packet);
			}
			catch (Exception ex)
			{
				activeChar.sendMessage("Usage: //atmosphere <ssqinfo dawn|dusk|red|regular>");
				activeChar.sendMessage("Usage: //atmosphere <sky day|night|red>");
			}
		}
		else if (command.startsWith("admin_jukebox"))
		{
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				final String sound = command.substring(17);
				final PlaySound snd = (sound.contains(".")) ? new PlaySound(sound) : new PlaySound(1, sound);
				
				activeChar.broadcastPacket(snd);
				activeChar.sendMessage("Playing " + sound + ".");
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_para_all"))
		{
			for (Player player : activeChar.getKnownType(Player.class))
			{
				if (!player.isGM())
				{
					player.startAbnormalEffect(0x0800);
					player.setIsParalyzed(true);
					player.broadcastPacket(new StopMove(player));
				}
			}
		}
		else if (command.startsWith("admin_unpara_all"))
		{
			for (Player player : activeChar.getKnownType(Player.class))
			{
				player.stopAbnormalEffect(0x0800);
				player.setIsParalyzed(false);
			}
		}
		else if (command.startsWith("admin_para"))
		{
			final WorldObject target = activeChar.getTarget();
			if (target instanceof Creature)
			{
				final Creature player = (Creature) target;
				
				player.startAbnormalEffect(0x0800);
				player.setIsParalyzed(true);
				player.broadcastPacket(new StopMove(player));
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if (command.startsWith("admin_unpara"))
		{
			final WorldObject target = activeChar.getTarget();
			if (target instanceof Creature)
			{
				final Creature player = (Creature) target;
				
				player.stopAbnormalEffect(0x0800);
				player.setIsParalyzed(false);
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if (command.startsWith("admin_gmspeed"))
		{
			try
			{
				activeChar.stopSkillEffects(7029);
				
				final int val = Integer.parseInt(st.nextToken());
				if (val > 0 && val < 5)
					activeChar.doCast(SkillTable.getInstance().getInfo(7029, val));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Use: //gmspeed value (0-4).");
			}
			finally
			{
				activeChar.updateEffectIcons();
			}
		}
		else if (command.startsWith("admin_social"))
		{
			try
			{
				final int social = Integer.parseInt(st.nextToken());
				
				if (st.hasMoreTokens())
				{
					final String targetOrRadius = st.nextToken();
					if (targetOrRadius != null)
					{
						Player player = World.getInstance().getPlayer(targetOrRadius);
						if (player != null)
						{
							if (performSocial(social, player))
								activeChar.sendMessage(player.getName() + " was affected by your social request.");
							else
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
						}
						else
						{
							final int radius = Integer.parseInt(targetOrRadius);
							
							for (Creature object : activeChar.getKnownTypeInRadius(Creature.class, radius))
								performSocial(social, object);
							
							activeChar.sendMessage(radius + " units radius was affected by your social request.");
						}
					}
				}
				else
				{
					WorldObject obj = activeChar.getTarget();
					if (obj == null)
						obj = activeChar;
					
					if (performSocial(social, obj))
						activeChar.sendMessage(obj.getName() + " was affected by your social request.");
					else
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
			}
		}
		else if (command.startsWith("admin_abnormal"))
		{
			try
			{
				final int abnormal = Integer.decode("0x" + st.nextToken());
				
				if (st.hasMoreTokens())
				{
					final String targetOrRadius = st.nextToken();
					if (targetOrRadius != null)
					{
						Player player = World.getInstance().getPlayer(targetOrRadius);
						if (player != null)
						{
							if (performAbnormal(abnormal, player))
								activeChar.sendMessage(player.getName() + " was affected by your abnormal request.");
							else
								activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
						}
						else
						{
							final int radius = Integer.parseInt(targetOrRadius);
							
							for (Creature object : activeChar.getKnownTypeInRadius(Creature.class, radius))
								performAbnormal(abnormal, object);
							
							activeChar.sendMessage(radius + " units radius was affected by your abnormal request.");
						}
					}
				}
				else
				{
					WorldObject obj = activeChar.getTarget();
					if (obj == null)
						obj = activeChar;
					
					if (performAbnormal(abnormal, obj))
						activeChar.sendMessage(obj.getName() + " was affected by your abnormal request.");
					else
						activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
			}
		}
		else if (command.startsWith("admin_effect"))
		{
			try
			{
				WorldObject obj = activeChar.getTarget();
				int level = 1, hittime = 1;
				int skill = Integer.parseInt(st.nextToken());
				
				if (st.hasMoreTokens())
					level = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
					hittime = Integer.parseInt(st.nextToken());
				
				if (obj == null)
					obj = activeChar;
				
				if (!(obj instanceof Creature))
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				else
				{
					Creature target = (Creature) obj;
					target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
					activeChar.sendMessage(obj.getName() + " performs MSU " + skill + "/" + level + " by your request.");
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
			}
		}
		
		if (command.contains("menu"))
		{
			String filename = "effects_menu.htm";
			if (command.contains("abnormal"))
				filename = "abnormal.htm";
			else if (command.contains("social"))
				filename = "social.htm";
			
			AdminHelpPage.showHelpPage(activeChar, filename);
		}
		
		return true;
	}
	
	private static boolean performAbnormal(int action, WorldObject target)
	{
		if (target instanceof Creature)
		{
			final Creature character = (Creature) target;
			if ((character.getAbnormalEffect() & action) == action)
				character.stopAbnormalEffect(action);
			else
				character.startAbnormalEffect(action);
			
			return true;
		}
		return false;
	}
	
	private static boolean performSocial(int action, WorldObject target)
	{
		if (target instanceof Creature)
		{
			if (target instanceof Summon || target instanceof Chest || (target instanceof Npc && (action < 1 || action > 3)) || (target instanceof Player && (action < 2 || action > 16)))
				return false;
			
			final Creature character = (Creature) target;
			character.broadcastPacket(new SocialAction(character, action));
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}