package com.archenai.arena2;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.archenai.util.NMS;

import net.md_5.bungee.api.ChatColor;

public class KitData 
{
	
	static public void EquipPlayer(Player p, Material[] armorSet, Material[] itemSet, int[] damages, int[] defenses, String[] itemNames, String[] armorNames)
	{
		for(int i = 0; i < 4; i++)
		{
			ItemStack armor = new ItemStack(armorSet[i]);
			ItemStack is2 = NMS.createCustomArmor(i < armorNames.length ? armorNames[i] : armor.getItemMeta().getDisplayName(), null, defenses[i], armorSet[i], i);
			ItemMeta im = is2.getItemMeta();
			im.spigot().setUnbreakable(true);
			is2.setItemMeta(im);
			switch(i)
			{
				case 0: p.getInventory().setBoots(is2);
						break;
				case 1: p.getInventory().setLeggings(is2);
						break;
				case 2: p.getInventory().setChestplate(is2);
						break;
				case 3: p.getInventory().setHelmet(is2);
						break;
				default: p.getInventory().setBoots(is2);
						break;
			}
		}
		for(int i = 0; i < itemSet.length; i++)
		{
			ItemStack is = new ItemStack(itemSet[i]);
			String defense = null;
			if(i + 4 < defenses.length)
				defense = String.valueOf(defenses[i + 4]);
			ItemStack is2 = NMS.createCustomWeapon(i < itemNames.length ? itemNames[i] : is.getItemMeta().getDisplayName(), null, 2048, damages[i], defense, itemSet[i]);
			ItemMeta im = is2.getItemMeta();
			im.spigot().setUnbreakable(true);
			is2.setItemMeta(im);
			if(i == 1)
				p.getInventory().setItemInOffHand(is2);
			else
				p.getInventory().addItem(is2);
		}
	}

	public static class Chevalier extends Kit
	{
		static private Material[] armorSet = {Material.IRON_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET};
		static private Material[] itemSet = {Material.IRON_SWORD};
		static private int[] damages = {6};
		static private int[] defenses = {2, 5, 5, 2};
		static private String[] itemNames = {"Longsword"};
		static private String[] armorNames = {"Plate Boots", "Mail Greaves", "Plate Mail", "Plate Helmet"};
		static private Messages[] messages = {Messages.CHEVALIER_ABILITY_1, Messages.CHEVALIER_ABILITY_1_WEAR_OFF, Messages.CHEVALIER_ABILITY_1_RECHARGE, Messages.CHEVALIER_ABILITY_NAME_1};

		static public void Ability1(Player p, PlayerData pd, Plugin plugin)
		{
			pd.setLastAbility(1);
			p.setWalkSpeed(0.15F);
			pd.setDamageResistance(2);
			pd.setCooldown(120);
			p.sendMessage(messages[0].getValue());
			new BukkitRunnable(){
				
				public void run()
				{
					if(pd.getAlive())
					{
						p.setWalkSpeed(0.2F);
						pd.setDamageResistance(1);
						p.sendMessage(messages[1].getValue());
					}
				}
				
			}.runTaskLater(plugin, 60);
		}
		
		static public Messages Ability1Cooldown()
		{
			return messages[2];
		}
		
		static public Messages getAbility1Name()
		{
			return messages[3];
		}
		
		static public Material[] getArmorSet()
		{
			return armorSet;
		}

		static public Material[] getItemSet()
		{
			return itemSet;
		}

		static public int[] getDamages()
		{
			return damages;
		}

		static public int[] getDefenses()
		{
			return defenses;
		}

		static public String[] getItemNames()
		{
			return itemNames;
		}

		static public String[] getArmorNames()
		{
			return armorNames;
		}
	}

	public static class Warlord extends Kit
	{
		static private Material[] armorSet = {Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.IRON_CHESTPLATE, Material.LEATHER_HELMET};
		static private Material[] itemSet = {Material.IRON_SWORD, Material.STONE_SWORD};
		static private int[] damages = {7, 4};
		static private int[] defenses = {1, 4, 4, 2, 4, 2};
		static private String[] itemNames = {"Heavy Rapier", "Gauche Blade"};
		static private String[] armorNames = {"High Boots", "Studded Leather Greaves", "Breastplate", "Studded Leather Cap"};
		static private Messages[] messages = {Messages.WARLORD_ABILITY_1, Messages.WARLORD_ABILITY_1_WEAR_OFF, Messages.WARLORD_ABILITY_1_RECHARGE, 
				Messages.WARLORD_ABILITY_NAME_1, Messages.WARLORD_ABILITY_2, Messages.WARLORD_ABILITY_2_WEAR_OFF, Messages.WARLORD_ABILITY_1_RECHARGE,
				Messages.WARLORD_ABILITY_NAME_2};

		static public void Ability1(Player p, PlayerData pd, Plugin plugin)
		{
			pd.setLastAbility(1);
			pd.setDamageModifier(1.33);
			p.setWalkSpeed(0.25f);
			pd.setCooldown(160);
			p.sendMessage(messages[0].getValue());
			new BukkitRunnable(){
				
				public void run()
				{
					if(pd.getAlive())
					{
						p.setWalkSpeed(0.2f);
						pd.setDamageModifier(1);
						p.sendMessage(messages[1].getValue());
					}
				}
				
			}.runTaskLater(plugin, 60);
		}
		
		static public Messages Ability1Cooldown()
		{
			return messages[2];
		}
		
		static public Messages getAbility1Name()
		{
			return messages[3];
		}
		static public void Ability2(Player p, PlayerData pd, Plugin plugin)
		{
			pd.setLastAbility(2);
			pd.setDamageResistance(1.33);
			pd.setCooldown(160);
			p.sendMessage(messages[4].getValue());
			new BukkitRunnable(){
				
				public void run()
				{
					if(pd.getAlive())
					{
						pd.setDamageResistance(1);
						p.sendMessage(messages[5].getValue());
					}
				}
				
			}.runTaskLater(plugin, 60);
		}
		
		static public Messages Ability2Cooldown()
		{
			return messages[6];
		}
		
		static public Messages getAbility2Name()
		{
			return messages[7];
		}
		
		static public Material[] getArmorSet()
		{
			return armorSet;
		}

		static public Material[] getItemSet()
		{
			return itemSet;
		}

		static public int[] getDamages()
		{
			return damages;
		}

		static public int[] getDefenses()
		{
			return defenses;
		}

		static public String[] getItemNames()
		{
			return itemNames;
		}

		static public String[] getArmorNames()
		{
			return armorNames;
		}
	}

	public static class Barbarian extends Kit
	{
		static private Material[] armorSet = {Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET};
		static private Material[] itemSet = {Material.GOLD_AXE};
		static private int[] damages = {7};
		static private int[] defenses = {1, 3, 3, 1};
		static private String[] itemNames = {"Greataxe"};
		static private String[] armorNames = {"Leather Shoes", "Leather Greaves", "Boiled Leather Cuirass", "Leather Cap"};
		static private Messages[] messages = {Messages.BARBARIAN_ABILITY_1, Messages.BARBARIAN_ABILITY_1_WEAR_OFF, Messages.BARBARIAN_ABILITY_1_RECHARGE, 
				Messages.BARBARIAN_ABILITY_1_HIT_1, Messages.BARBARIAN_ABILITY_1_HIT_2, Messages.BARBARIAN_ABILITY_NAME_1};

		static public void Ability1(Player p, PlayerData pd, Plugin plugin)
		{
			pd.setLastAbility(1);
			Vector v = p.getLocation().getDirection().multiply(1.5);
			p.setVelocity(v.setY(0.7));
			p.sendMessage(messages[0].getValue());
			pd.setActive(true);
		}
		
		static public Messages Ability1Cooldown()
		{
			return messages[2];
		}
		
		static public Messages Ability1Expired()
		{
			return messages[1];
		}
		
		static public double Ability1Action(Player p, PlayerData pd, Plugin plugin, List<LivingEntity> affected)
		{
			for(LivingEntity le : affected)
			{
				p.sendMessage(messages[3].getValue() + ChatColor.GREEN + le.getName() + messages[4].getValue());
				le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 3));
			}
			return 14;
		}
		
		static public Messages getAbility1Name()
		{
			return messages[5];
		}
		
		static public Material[] getArmorSet()
		{
			return armorSet;
		}

		static public Material[] getItemSet()
		{
			return itemSet;
		}

		static public int[] getDamages()
		{
			return damages;
		}

		static public int[] getDefenses()
		{
			return defenses;
		}

		static public String[] getItemNames()
		{
			return itemNames;
		}

		static public String[] getArmorNames()
		{
			return armorNames;
		}
	}

	public static class Cleric extends Kit
	{
		static private Material[] armorSet = {Material.GOLD_BOOTS, Material.GOLD_LEGGINGS, Material.GOLD_CHESTPLATE, Material.GOLD_HELMET};
		static private Material[] itemSet = {Material.GOLD_SPADE};
		static private int[] damages = {5};
		static private int[] defenses = {2, 4, 4, 2};
		static private String[] itemNames = {"Golden Mace"};
		static private String[] armorNames = {"Gilded Boots", "Gilded Greaves", "Gilded Mail", "Gilded Helmet"};
		static private Messages[] messages = {Messages.CLERIC_ABILITY_1, Messages.CLERIC_ABILITY_1_WEAR_OFF, Messages.CLERIC_ABILITY_1_RECHARGE,
				Messages.CLERIC_ABILITY_NAME_1};

		static public void Ability1(Player p, PlayerData pd, Plugin plugin)
		{
			pd.setLastAbility(1);
			if(p.getHealth() <= 16)
				p.setHealth(p.getHealth() + 4);
			else
				p.setHealth(20);
			pd.setCooldown(160);
			p.sendMessage(messages[0].getValue());
			p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3, 2));
			new BukkitRunnable(){
				
				public void run()
				{
					p.sendMessage(messages[1].getValue());
				}
				
			}.runTaskLater(plugin, 60);
		}
		
		static public Messages getAbility1Name()
		{
			return messages[3];
		}
		
		static public Messages Ability1Cooldown()
		{
			return messages[2];
		}
		
		static public Material[] getArmorSet()
		{
			return armorSet;
		}

		static public Material[] getItemSet()
		{
			return itemSet;
		}

		static public int[] getDamages()
		{
			return damages;
		}

		static public int[] getDefenses()
		{
			return defenses;
		}

		static public String[] getItemNames()
		{
			return itemNames;
		}

		static public String[] getArmorNames()
		{
			return armorNames;
		}
	}
}
