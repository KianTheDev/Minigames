package com.archenai.arena2;

import org.bukkit.entity.Player;

import com.archenai.arena2.KitData.Barbarian;

public class PlayerData 
{
	private Player player;
	private Class<?> kit;
	private int cooldown, lastAbility, kills;
	private boolean alive, activeAbility;
	private double damageResistance, damageModifier;

	public PlayerData(Player player, Class<?> kit, boolean isAlive)
	{
		this.player = player;
		this.kit = kit;
		this.activeAbility = false;
		this.cooldown = 0;
		this.damageResistance = 1;
		this.damageModifier = 1;
		this.alive = isAlive;
		this.lastAbility = 1;
		this.kills = 0;
	}
	
	public void incrementCooldown()
	{
		if(cooldown <= 0)
		{
			cooldown = 0;
			return;
		}
		else
			cooldown--;
		if(cooldown <= 0)
		{
			cooldown = 0;
			try
			{
				player.sendMessage((String) kit.getClass().getMethod("Ability" + lastAbility + "Cooldown").invoke(null));
			} catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void addKill()
	{
		kills++;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public Class<?> getKit()
	{
		return kit;
	}
	
	public int getCooldown()
	{
		return cooldown;
	}
	
	public boolean getAlive()
	{
		return alive;
	}
	
	public boolean getActive()
	{
		return activeAbility;
	}
	
	public double getDamageResistance()
	{
		return damageResistance;
	}
	
	public double getDamageModifier()
	{
		return damageModifier;
	}
	
	public int getLastAbility()
	{
		return lastAbility;
	}
	
	public int getKills()
	{
		return kills;
	}
	
	public void setCooldown(int i)
	{
		cooldown = i;
	}
	
	public void setAlive(boolean b)
	{
		alive = b;
	}
	
	public void setActive(boolean b)
	{
		activeAbility = b;
	}
	
	public void setDamageResistance(double d)
	{
		damageResistance = d;
	}
	
	public void setDamageModifier(double d)
	{
		damageModifier = d;
	}
	
	public void setKit(Class<?> k)
	{
		kit = k;
	}
	
	public void setLastAbility(int i)
	{
		lastAbility = i;
	}
}
