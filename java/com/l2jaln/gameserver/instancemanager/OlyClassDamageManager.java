 /*
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  * 02111-1307, USA.
  *
  * http://www.gnu.org/copyleft/gpl.html
  */
 package com.l2jaln.gameserver.instancemanager;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import com.l2jaln.Config;
 import com.l2jaln.gameserver.data.CharTemplateTable;
 import com.l2jaln.gameserver.model.actor.instance.Player;

 public class OlyClassDamageManager
 {
 	private static final Logger _log = Logger.getLogger(OlyClassDamageManager.class.getName());
 	
 	private static Hashtable<Integer, Double> damage_to_mage = new Hashtable<>();
 	private static Hashtable<Integer, Double> damage_to_fighter = new Hashtable<>();
 	private static Hashtable<Integer, Double> damage_by_mage = new Hashtable<>();
 	private static Hashtable<Integer, Double> damage_by_fighter = new Hashtable<>();
 
 	private static Hashtable<Integer, String> id_to_name = new Hashtable<>();
 	private static Hashtable<String, Integer> name_to_id = new Hashtable<>();
 	
 	public static void loadConfig()
 	{
 		final String SCRIPT = Config.OLY_CLASS_DAMAGES_FILE;
 		InputStream is = null;
 		File file = null;
 		try
 		{
 			Properties scriptSetting = new Properties();
 			file = new File(SCRIPT);
 			is = new FileInputStream(file);
 			scriptSetting.load(is);
 			
 			Set<Object> key_set = scriptSetting.keySet();
 			
 			for(Object key : key_set)
 			{
 				String key_string = (String) key;
 				
 				String[] class_and_type = key_string.split("__");
 				
 				String class_name = class_and_type[0].replace("_", " ");
 				
 				if (class_name.equals("Eva s Saint"))
 					class_name = "Eva's Saint";
 				
 				String type = class_and_type[1];
 				
 				Integer class_id = CharTemplateTable.getClassIdByName(class_name)-1;
 				
 				id_to_name.put(class_id, class_name);
 				name_to_id.put(class_name, class_id);
 				
 				if(type.equals("ToFighter"))
 				{
 					damage_to_fighter.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string)));
 				}
 				else if(type.equals("ToMage"))
 				{
 					damage_to_mage.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string)));
 				}
 				else if(type.equals("ByFighter"))
 				{
 					damage_by_fighter.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string)));
 				}
 				else if(type.equals("ByMage"))
 				{
 					damage_by_mage.put(class_id, Double.parseDouble(scriptSetting.getProperty(key_string)));
 				}
 			}
 			
 			_log.info("Loaded "+id_to_name.size()+" oly classes Damages configurations");
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			if(is != null)
 			{
 				try
 				{
 					is.close();
 				}
 				catch(IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public static double getClassDamageToMage(int id)
 	{
 		Double multiplier = damage_to_mage.get(id);
 		
 		if(multiplier!=null)
 			return multiplier;
 		return 1; 
 	}
 	
 	public static double getClassDamageToFighter(int id)
 	{
 		Double multiplier = damage_to_fighter.get(id);
 		if(multiplier!=null)
 			return multiplier;
 		return 1; 
 	}
 	
 	public static double getClassDamageByMage(int id)
 	{
 		Double multiplier = damage_by_mage.get(id);
 		if(multiplier!=null)
 			return multiplier;
 		return 1; 
 	}
 	
 	public static double getClassDamageByFighter(int id)
 	{
 		Double multiplier = damage_by_fighter.get(id);
 		if(multiplier!=null)
 			return multiplier;
 		return 1; 
 	}
 	
 	public static int getIdByName(String name)
 	{
 		Integer id = name_to_id.get(name);
 		if(id!=null)
 			return id;
 		return 0;
 	}
 	
 	public static String getNameById(int id)
 	{
 		String name = id_to_name.get(id);
 		if(name!=null)
 			return name;
 		return "";
 	}
 	
 	/**
 	 * return the product between the attackerMultiplier and attackedMultiplier
 	 * configured into the classDamages.properties
 	 * @param attacker
 	 * @param attacked
 	 * @return output = attackerMulti*attackedMulti
 	 */
 	public static double getDamageMultiplier(Player attacker, Player attacked)
 	{
 		if(attacker==null || attacked==null)
 			return 1;
 		
 		double attackerMulti = 1;
 		
 		if(attacked.isMageClass())
 			attackerMulti = getClassDamageToMage(attacker.getClassId().getId());
 		else
 			attackerMulti = getClassDamageToFighter(attacker.getClassId().getId());
 		
 		double attackedMulti = 1;
 		
 		if(attacker.isMageClass())
 			attackedMulti = getClassDamageByMage(attacked.getClassId().getId());
 		else
 			attackedMulti = getClassDamageByFighter(attacked.getClassId().getId());
 		
 		double output = attackerMulti*attackedMulti;
 		
 		return output;
 	}
 }