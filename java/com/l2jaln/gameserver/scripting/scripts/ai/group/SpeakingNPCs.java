package com.l2jaln.gameserver.scripting.scripts.ai.group;

import com.l2jaln.gameserver.model.L2Skill;
import com.l2jaln.gameserver.model.actor.Npc;
import com.l2jaln.gameserver.model.actor.instance.Player;
import com.l2jaln.gameserver.scripting.EventType;
import com.l2jaln.gameserver.scripting.scripts.ai.L2AttackableAIScript;

/**
 * Speaking NPCs implementation.<br>
 * <br>
 * This AI leads the behavior of any speaking NPC.<br>
 * It sends back the good string following the action and the npcId.<br>
 * <br>
 * <font color="red"><b><u>TODO:</b></u> Replace the system of switch by an XML, once a decent amount of NPCs is mapped.</font>
 */
public class SpeakingNPCs extends L2AttackableAIScript
{
	private static final int[] NPC_IDS =
	{
		18212, //
		18213, //
		18214, //
		18215, //
		18216, // Archon of Halisha
		18217, //
		18218, //
		18219, //
		
		27016, // Nerkas
		27021, // Kirunak
		27022, // Merkenis
		
		27219, //
		27220, //
		27221, //
		27222, //
		27223, //
		27224, //
		27225, //
		27226, //
		27227, //
		27228, //
		27229, //
		27230, //
		27231, //
		27232, // Archon of Halisha
		27233, //
		27234, //
		27235, //
		27236, //
		27237, //
		27238, //
		27239, //
		27240, //
		27241, //
		27242, //
		27243, //
		27244, //
		27245, //
		27246, //
		27247, //
		27249
	};
	
	public SpeakingNPCs()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(NPC_IDS, EventType.ON_ATTACK, EventType.ON_KILL);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.isScriptValue(1))
			return super.onAttack(npc, attacker, damage, isPet, skill);
		
		String message = "";
		
		switch (npc.getNpcId())
		{
			case 18212:
			case 18213:
			case 18214:
			case 18215:
			case 18216:
			case 18217:
			case 18218:
			case 18219:
			case 27219:
			case 27220:
			case 27221:
			case 27222:
			case 27223:
			case 27224:
			case 27225:
			case 27226:
			case 27227:
			case 27228:
			case 27229:
			case 27230:
			case 27231:
			case 27232:
			case 27233:
			case 27234:
			case 27235:
			case 27236:
			case 27237:
			case 27238:
			case 27239:
			case 27240:
			case 27241:
			case 27242:
			case 27243:
			case 27244:
			case 27245:
			case 27246:
			case 27247:
			case 27249:
				message = "You dare to disturb the order of the shrine! Die!";
				break;
			
			case 27016:
				message = "...How dare you challenge me!";
				break;
			
			case 27021:
				message = "I will taste your blood!";
				break;
			
			case 27022:
				message = "I shall put you in a never-ending nightmare!";
				break;
		}
		
		npc.broadcastNpcSay(message);
		npc.setScriptValue(1); // Make the mob speaks only once, else he will spam.
		
		return super.onAttack(npc, attacker, damage, isPet, skill);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		String message = "";
		
		switch (npc.getNpcId())
		{
			case 18212:
			case 18213:
			case 18214:
			case 18215:
			case 18216:
			case 18217:
			case 18218:
			case 18219:
			case 27219:
			case 27220:
			case 27221:
			case 27222:
			case 27223:
			case 27224:
			case 27225:
			case 27226:
			case 27227:
			case 27228:
			case 27229:
			case 27230:
			case 27231:
			case 27232:
			case 27233:
			case 27234:
			case 27235:
			case 27236:
			case 27237:
			case 27238:
			case 27239:
			case 27240:
			case 27241:
			case 27242:
			case 27243:
			case 27244:
			case 27245:
			case 27246:
			case 27247:
			case 27249:
				message = "My spirit is releasing from this shell. I'm getting close to Halisha...";
				break;
			
			case 27016:
				message = "May Beleth's power be spread on the whole world...!";
				break;
			
			case 27021:
				message = "I have fulfilled my contract with Trader Creamees.";
				break;
			
			case 27022:
				message = "My soul belongs to Icarus...";
				break;
		}
		
		npc.broadcastNpcSay(message);
		
		return super.onKill(npc, player, isPet);
	}
}