package com.archenai.arena2;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.archenai.arena2.KitData.Barbarian;
import com.archenai.arena2.KitData.Chevalier;
import com.archenai.arena2.KitData.Cleric;
import com.archenai.arena2.KitData.Warlord;
import com.archenai.arena2.WorldData.WorldType;
import com.archenai.util.ZipUtils;

public class ArenaMain extends JavaPlugin implements Listener 
{
	private Plugin plugin;
	static boolean GameEnabled, GameActive, PvPEnabled, endTrigger;
	private static ArenaMain instance;
	private HashMap<UUID, PlayerData> playerData = new HashMap<UUID, PlayerData>();
	private HashMap<UUID, Location> startLocations = new HashMap<UUID, Location>();
	private ArrayList<UUID> spectators = new ArrayList<UUID>();
	private List<WorldData> worldData = new ArrayList<WorldData>();
	final int DEFAULT_START = 60;
	final int FILLED_START = 20;
	final int[] ALERT_TIMES = {30, 20, 10, 5, 4, 3, 2, 1};
	final int FILLED_PLAYERS = 1;
	final int MIN_PLAYERS = 1;
	final int PLAYERS_ALIVE_TO_END = 0;
	private String[] places = {null, null, null};
	final String[] TIPS = {ChatColor.GRAY + "Warlords can swap out their weapons (Default key: F) to have either better defense or damage.",
			ChatColor.GRAY + "Clerics have a regeneration ability that increases in-combat longevity.",
			ChatColor.GRAY + "Chevaliers can activate " + Messages.CHEVALIER_ABILITY_NAME_1.getValue() + ChatColor.GRAY + ", which grants temporary 50% damage resistance.",
			ChatColor.GRAY + "Barbarians can use " + Messages.BARBARIAN_ABILITY_NAME_1.getValue() + ChatColor.GRAY + " to escape combat quickly and do heavy damage.",
			ChatColor.GRAY + "Warlords can, depending on the weapon in their main hand, activate temporary defensive or damage boosts."};
	private int startTime;
	public String serverPath, pluginPath;
	private WorldData currentWorldData;
	private World currentWorld;
	private int currentData;
	
	BukkitRunnable gameEndRunnable = new BukkitRunnable(){
		
		public void run()
		{
			resetPluginInstance();
			for(Player p : Bukkit.getOnlinePlayers())
				setPlayerBase(p, false);
			startArena();
		}
		
	};

	public static ArenaMain getInstance()
	{
		return instance;
	}
	
	@Override
	public void onEnable()
	{
		endTrigger = false;
		instance = this;
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		plugin = this;
		serverPath = Bukkit.getServer().getWorldContainer().getAbsolutePath();
		for(int i = serverPath.length() - 1; i >= 0; i--)
		{
			if(serverPath.charAt(i) == File.separatorChar)
			{
				serverPath = serverPath.substring(0, i + 1);
				break;
			}
		}
		pluginPath = serverPath + "plugins" + File.separator;
		if(!(new File(pluginPath + "ArenaWorldData")).exists())
		{
			new File(pluginPath + "ArenaWorldData").mkdirs();
		}
		if(!(new File(serverPath + "gameworld")).exists())
		{
			new File(serverPath + "gameworld").mkdirs();
		}
		if(!(new File(serverPath + "spawn")).exists())
		{
			new File(serverPath + "spawn").mkdirs();
		}
		Bukkit.getLogger().info("Server path: " + serverPath);
		Bukkit.getLogger().info("Plugin path: " + pluginPath);
		Bukkit.getLogger().info("Attempting to load " + pluginPath + "ArenaWorldData" + File.separator + "spawn.dat");
		worldData.add(new WorldData(new File(pluginPath + "ArenaWorldData" + File.separator + "spawn.dat"), true));
		for(File f : (new File(pluginPath + "ArenaWorldData")).listFiles())
		{
			if(!f.getName().equalsIgnoreCase("spawn.dat"))
			{
				Bukkit.getLogger().info("Attempting to load " + f.getPath());
				worldData.add(new WorldData(f, false));
			}
		}
		//Ability timeout
		new BukkitRunnable()
		{
			public void run()
			{
				for(PlayerData pd : playerData.values())
				{
					if(pd.getKit().equals(Barbarian.class))
						if(pd.getPlayer().isOnGround())
						{
							if(pd.getActive())
							{
								try
								{
									pd.setActive(false);
									pd.getPlayer().sendMessage((String) Messages.class.getMethod("getValue").invoke(pd.getKit().getMethod("Ability1Expired").invoke(null)));
								} catch(Exception e)
								{
									e.printStackTrace();
								}
							}
						}
				}
			}
		}.runTaskTimer(plugin, 5, 5);
		
		//Ability cooldown
		new BukkitRunnable()
		{
			public void run()
			{
				if(GameActive)
				{
					for(PlayerData pd : playerData.values())
						pd.incrementCooldown();
				}
			}
		}.runTaskTimer(plugin, 20, 2);
		
		//Game start code
		new BukkitRunnable(){
			
			public void run()
			{
				if(GameEnabled)
					if(Bukkit.getOnlinePlayers().size() > MIN_PLAYERS)
						if(Bukkit.getOnlinePlayers().size() > FILLED_PLAYERS)
							startCountdown(FILLED_START);
						else
							startCountdown(DEFAULT_START);
			}
			
		}.runTaskTimer(plugin, 0, 20);
		
		new BukkitRunnable(){
			
			public void run()
			{
				if(GameActive)
					if(getAlivePlayers() <= PLAYERS_ALIVE_TO_END && !endTrigger)
					{
						endTrigger = true;
						broadcastEndgame();
						if(getAlivePlayers() == 1)
							for(PlayerData pd : playerData.values())
								if(pd.getAlive())
								{
									places[0] = pd.getPlayer().getName();
									setSpectator(pd.getPlayer(), true);
								}
						gameEndRunnable.runTaskLater(plugin, 100);
					}
			}
			
		}.runTaskTimer(plugin, 0, 40);
		loadWorld(0);
	}
	
	public void setWorld(String name, int index)
	{
		currentWorld = Bukkit.getServer().getWorld(name);
		currentWorldData = worldData.get(index);
		if(currentWorldData.getWorldType().equals(WorldType.SPAWN))
		{
			CoordSet cs = currentWorldData.getStartLocations().get(0);
			for(Player p : Bukkit.getOnlinePlayers())
			{
				p.teleport(new Location(currentWorld, cs.getX(), cs.getY(), cs.getZ()), TeleportCause.PLUGIN);
				new BukkitRunnable(){ //Extra delayed teleport to teleport player to the correct world location 
					
					public void run()
					{
						p.teleport(new Location(currentWorld, cs.getX(), cs.getY(), cs.getZ()), TeleportCause.PLUGIN);
					}
					
				}.runTaskLater(plugin, 2);
			}
		} else if(currentWorldData.getWorldType().equals(WorldType.GAMEWORLD))
		{
			int[] usedLocs = new int[currentWorldData.getStartLocations().size()];
			for(int i = 0; i < usedLocs.length; i++)
				usedLocs[i] = -1;
			for(Player p : Bukkit.getOnlinePlayers())
			{
				if(playerData.keySet().contains(p.getUniqueId()))
				{
					if(usedLocs[currentWorldData.getStartLocations().size() - 1] > -1)
					{
						for(int i = 0; i < usedLocs.length; i++)
							usedLocs[i] = -1;
					}
						int i = getSemiRandomInt(usedLocs, 0, currentWorldData.getStartLocations().size() - 1);
						CoordSet cs = currentWorldData.getStartLocations().get(i);
						p.teleport(new Location(currentWorld, cs.getX(), cs.getY(), cs.getZ()), TeleportCause.PLUGIN);
						new BukkitRunnable(){ //Extra delayed teleport to teleport player to the correct world location 
							
							public void run()
							{
								p.teleport(new Location(currentWorld, cs.getX(), cs.getY(), cs.getZ()), TeleportCause.PLUGIN);
							}
							
						}.runTaskLater(plugin, 2);
						for(int i2 = 0; i2 < usedLocs.length; i2++)
						{
							if(usedLocs[i2] == -1)
							{
								usedLocs[i2] = i;
								break;
							}
						}
				}
			}
		}
	}
	
	private int getSemiRandomInt(int[] forbidden, int min, int max)
	{
		int i = (int) Math.round(Math.random() * (max + 1)) + min;
		boolean b = false;
		for(int integer : forbidden)
		{
			if(i == integer)
				b = true;
		}
		if(b)
			return getSemiRandomInt(forbidden, min, max);
		return i;
	}
	
	public void loadWorld(int i)
	{
		try
		{
			if(worldData.get(i).getWorldType().equals(WorldType.SPAWN))
			{
				if(Bukkit.getServer().getWorld("spawn") != null)
					Bukkit.getServer().unloadWorld("spawn", true);
				File f = new File(serverPath + "spawn");
				for(File sub : f.listFiles())
					sub.delete();
				ZipUtils.unzip(serverPath + worldData.get(i).getFileName(), serverPath + "spawn");
				Bukkit.getServer().createWorld(new WorldCreator("spawn"));
				CoordSet cs = worldData.get(i).getSpectatorStartLocation();
				Villager kitSelector = (Villager) Bukkit.getWorld("spawn").spawnEntity(new Location(Bukkit.getWorld("spawn"), cs.getX(), cs.getY(), cs.getZ()), EntityType.VILLAGER);
				kitSelector.setAdult();
				kitSelector.setAI(false);
				kitSelector.setGravity(false);
				kitSelector.setCollidable(false);
				kitSelector.setInvulnerable(true);
				kitSelector.setSilent(true);
				ArmorStand hologram = (ArmorStand) Bukkit.getWorld("spawn").spawnEntity(new Location(Bukkit.getWorld("spawn"), 0, 0, 0), EntityType.ARMOR_STAND);
				hologram.setSmall(true);
				hologram.setVisible(false);
				hologram.setCollidable(false);
				hologram.setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Kit Selector");
				hologram.setCustomNameVisible(true);
				hologram.setGravity(false);
				kitSelector.setPassenger(hologram);
			} else if(worldData.get(i).getWorldType().equals(WorldType.GAMEWORLD))
			{
				if(Bukkit.getServer().getWorld("gameworld") != null)
					Bukkit.getServer().unloadWorld("gameworld", true);
				File f = new File(serverPath + "gameworld");
				for(File sub : f.listFiles())
					sub.delete();
				ZipUtils.unzip(serverPath + worldData.get(i).getFileName(), serverPath + "gameworld");
				Bukkit.getServer().createWorld(new WorldCreator("gameworld"));
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean getGameEnabled()
	{
		return GameEnabled;
	}
	
	@EventHandler
	public void onItemSwitch(PlayerSwapHandItemsEvent e)
	{
		UUID id = e.getPlayer().getUniqueId();
		if(!(GameActive && playerData.keySet().contains(id) && playerData.get(id).getAlive() && playerData.get(id).getKit().equals(Warlord.class)))
			e.setCancelled(true);
	}

	@EventHandler
	public void onItemPickUp(PlayerPickupItemEvent e)
	{
		if(GameEnabled)
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e)
	{
		if(GameEnabled)
		{
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e)
	{
		if(GameEnabled)
		{
			e.setDroppedExp(0);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e)
	{
		e.setQuitMessage(null);
		if(GameActive)
		{
			Player player = e.getPlayer();
			if(playerData.containsKey(player.getUniqueId()))
				playerData.remove(player.getUniqueId());
			if(spectators.contains(player.getUniqueId()))
				spectators.remove(player.getUniqueId());
		}
	}
	

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		e.setJoinMessage(null);
		if(GameActive)
		{
			setSpectator(e.getPlayer(), true);
			for(UUID u : spectators)
				e.getPlayer().hidePlayer(Bukkit.getPlayer(u));
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e)
	{
		if(GameActive && playerData.keySet().contains(e.getEntity().getUniqueId()))
		{
			Player p = e.getEntity();
			//"Die"
			e.setDeathMessage(null);
			e.getEntity().setHealth(20);
			e.setDroppedExp(0);
			e.setKeepInventory(true);
			p.getInventory().clear();
			playerData.get(p.getUniqueId()).setAlive(false);
			//Death message
			String m = "";
			if(p.getKiller() != null)
			{
				m = Messages.ARENA.getValue() + ChatColor.GREEN + p.getName() + ChatColor.GRAY + " was killed by " + ChatColor.GREEN + p.getKiller().getName() + ChatColor.GRAY + ".";
				if(playerData.keySet().contains(p.getKiller().getUniqueId()))
					playerData.get(p.getKiller().getUniqueId()).addKill();
			}
			else
				m = Messages.ARENA.getValue() + ChatColor.GREEN + p.getName() + ChatColor.GRAY + " died.";
			for(Player p2 : Bukkit.getOnlinePlayers())
			{
				p2.sendMessage(m);
			}
			sendPlayerStats(p);
			if(getAlivePlayers() < 3)
			{
				places[Math.max(0, getAlivePlayers())] = p.getName();
			}
			if(getAlivePlayers() <= PLAYERS_ALIVE_TO_END && !endTrigger)
			{
				endTrigger = true;
				broadcastEndgame();
				if(getAlivePlayers() == 1)
					for(PlayerData pd : playerData.values())
						if(pd.getAlive())
						{
							places[0] = pd.getPlayer().getName();
							setSpectator(pd.getPlayer(), true);
						}
				gameEndRunnable.runTaskLater(plugin, 100);
			}
			//Set spectator
			setSpectator(p, true);
		}
	}

	@EventHandler
	public void onFood(FoodLevelChangeEvent e)
	{
		if(GameEnabled)
		{
			if(e.getEntity() instanceof Player)
			{
				Player p = (Player) e.getEntity();
				new BukkitRunnable(){
					
					public void run()
					{
						p.setFoodLevel(20);
					}
					
				}.runTaskLater(plugin, 1);
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		if(GameEnabled)
			e.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e)
	{
		if(GameActive)
		{
			if(e.getEntity() instanceof Player)
			{
				Player p = (Player) e.getEntity();
				if(e.getCause().equals(DamageCause.VOID) || spectators.contains(p.getUniqueId()))
				{
					CoordSet cs = currentWorldData.getSpectatorStartLocation();
					p.teleport(new Location(currentWorld, cs.getX(), cs.getY(), cs.getZ()));
				}
				else if(e.getCause().equals(DamageCause.VOID))
					e.setDamage(9001);
				if(!PvPEnabled)
				{
					e.setCancelled(true);
					return;
				} else if(spectators.contains(p.getUniqueId()))
				{
					e.setCancelled(true);
					return;
				} else if(!playerData.containsKey(p.getUniqueId()))
				{
					e.setCancelled(true);
					return;
				} else if(!playerData.get(p.getUniqueId()).getAlive())
				{
					e.setCancelled(true);
					return;
				}
			}
		} else if(GameEnabled)
		{
			e.setCancelled(true);
			if(e.getCause().equals(DamageCause.VOID) && e.getEntity() instanceof Player) //Teleports the player back to spawn if he jumps off the edge
			{
				Player p = (Player) e.getEntity();
				CoordSet cs = currentWorldData.getStartLocations().get(0);
				p.teleport(new Location(currentWorld, cs.getX(), cs.getY(), cs.getZ()), TeleportCause.PLUGIN);
				new BukkitRunnable(){ //Extra delayed teleport to teleport player to the correct world location 

					public void run()
					{
						p.teleport(new Location(currentWorld, cs.getX(), cs.getY(), cs.getZ()), TeleportCause.PLUGIN);
					}

				}.runTaskLater(plugin, 1);
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e)
	{
		if(GameEnabled)
			if(e.getRightClicked() instanceof Villager)
			{
				e.setCancelled(true);
				kitSelectionMenu(e.getPlayer());
			}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e)
	{
		if(e.getDamager() instanceof Player && spectators.contains(((Player) e.getDamager()).getUniqueId()))
		{
			e.setCancelled(true);
			return;
		}
		if(e.getDamager() instanceof Player && playerData.containsKey(((Player) e.getDamager()).getUniqueId()) && e.getEntity() instanceof LivingEntity)
		{
			Player p = (Player) e.getDamager();
			if(!playerData.get(p.getUniqueId()).getAlive())
			{
				e.setCancelled(true);
			} else
			{
				PlayerData pd = playerData.get(p.getUniqueId());
				e.setDamage((e.getDamage() * pd.getDamageModifier()));
				if(playerData.get(p.getUniqueId()).getActive())
				{
					try
					{
						List<LivingEntity> affected = new ArrayList<LivingEntity>();
						affected.add((LivingEntity) e.getEntity());
						double newDamage = (double) pd.getKit().getClass().getMethod("Ability1Action", Player.class, PlayerData.class, Plugin.class, List.class).invoke(null, p, pd, plugin, affected);
						e.setDamage(newDamage);
					} catch(Exception exc)
					{
						exc.printStackTrace();
					}
				}
				p.setLevel((int)e.getDamage());
				p.setExp(0);
			}
		}
		if(e.getEntity() instanceof Player)
		{
			Player p = (Player) e.getEntity();
			if(playerData.get(p.getUniqueId()) != null)
			{
				e.setDamage(e.getDamage() / playerData.get(p.getUniqueId()).getDamageResistance());
			}
		}
	}

	@EventHandler
	public void ItemClickEvent(PlayerInteractEvent e)
	{
		if(GameActive)
		{
			Action a = e.getAction();
			Player p = e.getPlayer();
			if(!playerData.containsKey(p.getUniqueId()))
				return;
			else if(playerData.get(p.getUniqueId()).getAlive())
				return;
			if(a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK))
			{
				PlayerData pd = playerData.get(p.getUniqueId());
				if(p.getInventory().getItemInMainHand() != null && p.getInventory().getItemInMainHand().getType() != Material.AIR)
				{	
					try
					{
						if(pd.getCooldown() <= 0)
							if(pd.getKit().equals(Warlord.class))
							{
								if(p.getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD))
									pd.getKit().getClass().getMethod("Ability1", Player.class, PlayerData.class, Plugin.class).invoke(null, p, pd, plugin);
								else
									pd.getKit().getClass().getMethod("Ability2", Player.class, PlayerData.class, Plugin.class).invoke(null, p, pd, plugin);
							} else
								pd.getKit().getClass().getMethod("Ability1", Player.class, PlayerData.class, Plugin.class).invoke(null, p, pd, plugin);
						else
							p.sendMessage(Messages.COOLDOWN_1.getValue() + pd.getKit().getClass().getMethod("getAbility1Name").invoke(null) + Messages.COOLDOWN_2.getValue() + ChatColor.GREEN + new DecimalFormat("#.0").format(((double) pd.getCooldown())/10D) + Messages.COOLDOWN_3.getValue());
					} catch(Exception exc)
					{
						exc.printStackTrace();
					}
				}
			}
		}
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent e)
	{
		if(GameEnabled)
		{
			if(e.getInventory().getName().equalsIgnoreCase("Class Chooser"))
			{
				if(e.getCurrentItem() == null)
					return;
				else if(e.getCurrentItem().getType().equals(Material.AIR))
					return;
				Player player = (Player) e.getWhoClicked();
				if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.BLUE + "Spectator"))
				{
					if(!spectators.contains(player.getUniqueId()))
						spectators.add(player.getUniqueId());
					player.sendMessage(Messages.SPECTATOR.getValue());
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.BLUE + "Barbarian"))
				{
					player.sendMessage(Messages.BARBARIAN.getValue());
					if(playerData.containsKey(player.getUniqueId()))
					{
						PlayerData pd = playerData.get(player.getUniqueId());
						pd.setKit(Barbarian.class);
					} else
						playerData.put(player.getUniqueId(), new PlayerData(player, Barbarian.class, true));
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.BLUE + "Chevalier"))
				{
					player.sendMessage(Messages.CHEVALIER.getValue());
					if(playerData.containsKey(player.getUniqueId()))
					{
						PlayerData pd = playerData.get(player.getUniqueId());
						pd.setKit(Chevalier.class);
					} else
						playerData.put(player.getUniqueId(), new PlayerData(player, Chevalier.class, true));
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.BLUE + "Warlord"))
				{
					player.sendMessage(Messages.WARLORD.getValue());
					if(playerData.containsKey(player.getUniqueId()))
					{
						PlayerData pd = playerData.get(player.getUniqueId());
						pd.setKit(Warlord.class);
					} else
						playerData.put(player.getUniqueId(), new PlayerData(player, Warlord.class, true));
				} else if(e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.BLUE + "Cleric"))
				{
					player.sendMessage(Messages.CLERIC.getValue());
					if(playerData.containsKey(player.getUniqueId()))
					{
						PlayerData pd = playerData.get(player.getUniqueId());
						pd.setKit(Cleric.class);
					} else
						playerData.put(player.getUniqueId(), new PlayerData(player, Cleric.class, true));
				}
				e.setCancelled(true);
			}
		}
	}
	
	/***
	 * Initalizes the player for the game. Freezes him, outfits him, and sets his proper statistics.
	 * @param player - Player to be initialized.
	 */
	public void initPlayer(Player player)
	{
		if(spectators.contains(player.getUniqueId()))
		{
			setSpectator(player, false);
			return;
		}
		setPlayerBase(player, false);
		outfitPlayer(player);
		freezePlayer(player);
	}
	
	public void kitSelectionMenu(Player p)
	{
		Inventory inv = Bukkit.getServer().createInventory(null, 27, "Class Chooser");
		ItemStack trim = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta trimIM = trim.getItemMeta();
		trim.setDurability((short) 15);
		trimIM.setDisplayName("");
		trim.setItemMeta(trimIM);
		for(int i = 0; i <= 8; i++)
		{
			inv.setItem(i, trim);
			inv.setItem(i + 18, trim);
		}
		inv.setItem(9, trim);
		inv.setItem(17, trim);
		ItemStack is = new ItemStack(Material.STONE_AXE);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "Barbarian");
		im.setLore(Arrays.asList(ChatColor.BLUE + "Destroy your enemies and hear the lamentations of their women.", ChatColor.DARK_GREEN + "Leap attack ability.", ChatColor.DARK_GREEN + "Leather armor.", ChatColor.DARK_GREEN + "Golden axe."));
		is.setItemMeta(im);
		inv.setItem(10, is);
		is.setType(Material.IRON_SWORD);
		im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "Warlord");
		im.setLore(Arrays.asList(ChatColor.BLUE + "A mighty warrior as of ancient times.", ChatColor.DARK_GREEN + "Weakened iron and chain armor.", ChatColor.DARK_GREEN + "Swappable main hand and offhand swords."));
		is.setItemMeta(im);
		inv.setItem(11, is);
		is.setType(Material.SKULL_ITEM);
		im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "Spectator");
		im.setLore(Arrays.asList(ChatColor.BLUE + "Just fly around...", ChatColor.BLUE + "Cannot interact or be harmed."));
		is.setItemMeta(im);
		inv.setItem(13, is);
		is.setType(Material.GOLD_SPADE);
		im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "Cleric");
		im.setLore(Arrays.asList(ChatColor.BLUE + "Heal yourself with the power of cheese.", ChatColor.DARK_GREEN + "Golden armor.", ChatColor.DARK_GREEN + "Gold mace."));
		is.setItemMeta(im);
		inv.setItem(15, is);
		is.setType(Material.IRON_CHESTPLATE);
		im = is.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "Chevalier");
		im.setLore(Arrays.asList(ChatColor.BLUE + "A stalwart defender with powerful defensive abilities.", ChatColor.DARK_GREEN + "Iron armor.", ChatColor.DARK_GREEN + "Iron sword."));
		is.setItemMeta(im);
		inv.setItem(16, is);
		p.openInventory(inv);
	}
	
	public void gameCountdown(int i)
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(i > 0)
				p.sendMessage(Messages.COUNTDOWN.getValue() + ChatColor.GREEN + i + Messages.COOLDOWN_3.getValue());
			else
			{
				p.sendMessage(Messages.ARENA_START.getValue());
				unfreezePlayer(p);
			}
		}
		new BukkitRunnable(){
			
			public void run()
			{
				if(i > 0)
					gameCountdown(i - 1);
			}
			
		}.runTaskLater(plugin, 20);
	}
	
	public void startCountdown(int i)
	{
		if(Bukkit.getOnlinePlayers().size() < MIN_PLAYERS)
		{
			for(Player p : Bukkit.getOnlinePlayers())
				p.sendMessage(Messages.ARENA.getValue() + ChatColor.GRAY + "Countdown cancelled. Too few players!");
			return;
		}
		if(i <= 0)
		{
			for(Player p : Bukkit.getOnlinePlayers())
			{
				p.closeInventory();
				p.sendMessage(Messages.ARENA_START.getValue());
				initPlayer(p);
			}
			GameActive = true;
			setWorld("gameworld", currentData);
		}
		for(int i2 : ALERT_TIMES)
			if(i2 == i)
				for(Player p : Bukkit.getOnlinePlayers())
					p.sendMessage(Messages.COUNTDOWN_2.getValue() + ChatColor.GREEN + i + Messages.COOLDOWN_3.getValue());
		if(Bukkit.getOnlinePlayers().size() >= FILLED_PLAYERS && i > FILLED_START)
			startCountdown(FILLED_START);
		new BukkitRunnable(){
			
			public void run()
			{
				if(i > 0)
					startCountdown(i - 1);
			}
			
		}.runTaskLater(plugin, 20);
	}
	
	public void startArena()
	{
		GameActive = false;
		endTrigger = false;
		setWorld("spawn", 0);
		for(Player p : Bukkit.getOnlinePlayers())
		{
			kitSelectionMenu(p);
		}
		if(Bukkit.getOnlinePlayers().size() >= FILLED_PLAYERS)
			startCountdown(FILLED_START);
		else
			startCountdown(DEFAULT_START);
		enableArena();
		new BukkitRunnable()
		{
			
			public void run()
			{
				if(GameEnabled && !GameActive)
				{
					for(Player p : Bukkit.getOnlinePlayers())
					{
						p.sendMessage(Messages.TIPS.getValue() + TIPS[(int) (Math.random() * TIPS.length)]);	
					}
				} else
					this.cancel();
			}
			
		}.runTaskLater(plugin, (int) (Math.random() * 8 + 8));
		currentData = (int) (Math.random() * (worldData.size() - 1) + 1);
		loadWorld(currentData);
	}
	
	public void stopArena()
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.setLevel(0);
			p.setExp(0.0F);
		}
		for(Player p : Bukkit.getOnlinePlayers())
		{
			p.setAllowFlight(false);
			for(Player p2 : Bukkit.getOnlinePlayers())
			{
				p.showPlayer(p2);
			}
		}
		playerData.clear();
		GameActive = false;
		GameEnabled = true;
		setWorld("spawn", 0);
	}
	
	public void disableArena()
	{
		GameEnabled = false;
	}
	
	public void enableArena()
	{
		GameEnabled = true;
	}
	
	public void setPlayerBase(Player player, boolean canFly)
	{
		player.setWalkSpeed(0.2F);
		player.setFoodLevel(20);
		player.setHealth(20);
		player.setCanPickupItems(false);
		player.setAllowFlight(canFly);
		player.getInventory().clear();
	}
	
	private void freezePlayer(Player p)
	{
		p.setWalkSpeed(0.0F); //Disables walking
		p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 32767, 255, true, false)); //Disables jumping
	}
	
	private void unfreezePlayer(Player p)
	{
		p.setWalkSpeed(0.2F); //Default speed
		p.removePotionEffect(PotionEffectType.JUMP);
	}
	
	/***
	 * Completely freezes players who are on the ground.
	 * @param players - List of players to be frozen.
	 */
	public void freezePlayers(List<Player> players)
	{
		for(Player p : players)
		{
			freezePlayer(p);
		}
	}
	
	/***
	 * Unfreezes players frozen by freezePlayers().
	 * @param players - List of players to be unfrozen.
	 */
	public void unfreezePlayers(List<Player> players)
	{
		for(Player p : players)
		{
			unfreezePlayer(p);
		}
	}

	public void outfitPlayer(Player p)
	{
		if(!playerData.containsKey(p.getUniqueId()))
			return;
		try
		{
			PlayerData pd = playerData.get(p.getUniqueId());
			KitData.EquipPlayer(p, 
				(Material[]) pd.getKit().getMethod("getArmorSet").invoke(null), 
				(Material[]) pd.getKit().getMethod("getItemSet").invoke(null), 
				(int[]) pd.getKit().getMethod("getDamages").invoke(null), 
				(int[]) pd.getKit().getMethod("getDefenses").invoke(null), 
				(String[]) pd.getKit().getMethod("getItemNames").invoke(null), 
				(String[]) pd.getKit().getMethod("getArmorNames").invoke(null));
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setSpectator(Player p, boolean mess)
	{
		p.setCollidable(false);
		p.setInvulnerable(true);
		setPlayerBase(p, true);
		if(mess)
			p.sendMessage(Messages.SPECTATOR.getValue());
		spectators.add(p.getUniqueId());
		p.setGameMode(GameMode.SURVIVAL);
		for(Player p2 : Bukkit.getOnlinePlayers())
			p2.hidePlayer(p);
	}
	
	public int getAlivePlayers()
	{
		int i = 0;
		for(PlayerData pd : playerData.values())
			if(pd.getAlive())
				i++;
		return i;
	}
	
	public void resetPluginInstance()
	{
		places[0] = null;
		places[1] = null;
		places[2] = null;
		spectators.clear();
		playerData.clear();
		GameActive = false;
		endTrigger = false;
	}
	
	public void sendPlayerStats(Player p)
	{
		if(playerData.keySet().contains(p.getUniqueId()))
		{
			p.sendMessage(Messages.BORDER.getValue());
			p.sendMessage(" ");
			p.sendMessage(ChatColor.YELLOW + "        Your kills:" + ChatColor.GRAY + playerData.get(p.getUniqueId()).getKills());
			p.sendMessage(" ");
			p.sendMessage(Messages.BORDER.getValue());
		}
	}
	
	public void broadcastEndgame()
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			p.sendMessage(Messages.BORDER.getValue());
			p.sendMessage(" ");
			p.sendMessage((places[0] == null) ? ChatColor.YELLOW + "        First place: " + ChatColor.GRAY + "Nobody" : ChatColor.YELLOW + "        First place:" + ChatColor.GREEN + places[0]);
			p.sendMessage((places[1] == null) ? ChatColor.YELLOW + "       Second place: " + ChatColor.GRAY + "Nobody" : ChatColor.YELLOW + "       Second place:" + ChatColor.GREEN + places[1]);
			p.sendMessage((places[2] == null) ? ChatColor.YELLOW + "        Third place: " + ChatColor.GRAY + "Nobody" : ChatColor.YELLOW + "        Third place:" + ChatColor.GREEN + places[2]);
			p.sendMessage(" ");
			p.sendMessage(Messages.BORDER.getValue());
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("kittest"))
		{
			Player p = (Player) sender;
			KitData.EquipPlayer(p, Warlord.getArmorSet(), Warlord.getItemSet(), Warlord.getDamages(), Warlord.getDefenses(), Warlord.getItemNames(), Warlord.getArmorNames());
		} else if(cmd.getName().equalsIgnoreCase("arena-start"))
			startArena();
		else if(cmd.getName().equalsIgnoreCase("arena-disable"))
			disableArena();
		else if(cmd.getName().equalsIgnoreCase("arena-enable"))
			enableArena();
		else if(cmd.getName().equalsIgnoreCase("freeze-test"))
		{
			ArrayList<Player> playerList = new ArrayList<Player>();
			for(Player p : Bukkit.getOnlinePlayers())
				playerList.add(p);
			freezePlayers(playerList);
			new BukkitRunnable(){
			
				public void run()
				{
					unfreezePlayers(playerList);
				}
				
			}.runTaskLater(plugin, 100);
		}
		return true;
	}
}