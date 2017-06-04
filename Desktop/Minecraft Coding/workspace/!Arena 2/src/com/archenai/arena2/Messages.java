package com.archenai.arena2;import org.bukkit.ChatColor;

public enum Messages 
{
	//Miscellaneous
	BORDER(ChatColor.DARK_GREEN + "========================================"),
	//General
	ARENA(ChatColor.YELLOW + "Arena II: "),
	TIPS(ChatColor.RED + "" + ChatColor.BOLD + "TIP: "),
	ARENA_START(Messages.ARENA + "" + ChatColor.GRAY + "Arena II has begun!"),
	COUNTDOWN(Messages.ARENA + "" + ChatColor.GRAY + "The game will begin in "),
	COUNTDOWN_2(Messages.ARENA + "" + ChatColor.GRAY + "The game will start in "),
	COOLDOWN_1(Messages.ARENA + "" + ChatColor.GRAY + "You cannot use "),
	COOLDOWN_2(ChatColor.GRAY + " for "),
	COOLDOWN_3(ChatColor.GRAY + " seconds."),
	//Kit selection
	SPECTATOR(Messages.ARENA + "" + ChatColor.GRAY + "You are now a spectator!"), 
	CHEVALIER(Messages.ARENA + "" + ChatColor.GRAY + "You are now a chevalier!"),
	WARLORD(Messages.ARENA + "" + ChatColor.GRAY + "You are now a warlord!"),
	BARBARIAN(Messages.ARENA + "" + ChatColor.GRAY + "You are now a barbarian!"),
	CLERIC(Messages.ARENA + "" + ChatColor.GRAY + "You are now a cleric!"),
	//Barbarian Abilities
	BARBARIAN_ABILITY_NAME_1(ChatColor.GREEN + "Tackle"),
	BARBARIAN_ABILITY_1(Messages.ARENA + "" + ChatColor.GRAY + "You activated " + Messages.BARBARIAN_ABILITY_NAME_1 + ChatColor.GRAY + "."),
	BARBARIAN_ABILITY_1_WEAR_OFF(Messages.ARENA + "" + ChatColor.GRAY + "You missed " + Messages.BARBARIAN_ABILITY_NAME_1 + ChatColor.GRAY + "."),
	BARBARIAN_ABILITY_1_RECHARGE(Messages.ARENA + "" + ChatColor.GRAY + "You can now use " + Messages.BARBARIAN_ABILITY_NAME_1 + ChatColor.GRAY + "."),
	BARBARIAN_ABILITY_1_HIT_1(Messages.ARENA + "" + ChatColor.GRAY + "You hit "),
	BARBARIAN_ABILITY_1_HIT_2(ChatColor.GRAY + " with " + ChatColor.GREEN + "Tackle" + ChatColor.GRAY + "."),
	//Cleric Abilities
	CLERIC_ABILITY_NAME_1(ChatColor.GREEN + "Heal"),
	CLERIC_ABILITY_1(Messages.ARENA + "" + ChatColor.GRAY + "You activated " + Messages.CLERIC_ABILITY_NAME_1 + ChatColor.GRAY + "."), 
	CLERIC_ABILITY_1_WEAR_OFF(Messages.ARENA + "" + ChatColor.GRAY + "Your " + Messages.CLERIC_ABILITY_NAME_1 + "Heal" + ChatColor.GRAY + " spell has worn off."),
	CLERIC_ABILITY_1_RECHARGE(Messages.ARENA + "" + ChatColor.GRAY + "You can now use " + Messages.CLERIC_ABILITY_NAME_1 + ChatColor.GRAY + "."),
	//Paladin Abilities;
	CHEVALIER_ABILITY_NAME_1(ChatColor.GREEN + "Resistance"),
	CHEVALIER_ABILITY_1(Messages.ARENA + "" + ChatColor.GRAY + "You activated " + Messages.CHEVALIER_ABILITY_NAME_1 + ChatColor.GRAY + "."),
	CHEVALIER_ABILITY_1_WEAR_OFF(Messages.ARENA + "" + Messages.CHEVALIER_ABILITY_NAME_1 + ChatColor.GRAY + " has worn off."),
	CHEVALIER_ABILITY_1_RECHARGE(Messages.ARENA + "" + ChatColor.GRAY + "You can now use " + Messages.CHEVALIER_ABILITY_NAME_1 + ChatColor.GRAY + "."),
	//Warlord Abilities
	WARLORD_ABILITY_NAME_1(ChatColor.GREEN + "Berserk"),
	WARLORD_ABILITY_1(Messages.ARENA + "" + ChatColor.GRAY + "You activated " + Messages.WARLORD_ABILITY_NAME_1 + ChatColor.GRAY + "."), 
	WARLORD_ABILITY_1_WEAR_OFF(Messages.ARENA + "" + Messages.WARLORD_ABILITY_NAME_1 + ChatColor.GRAY + " has worn off."),
	WARLORD_ABILITY_1_RECHARGE(Messages.ARENA + "" + ChatColor.GRAY + "You can now use " + Messages.WARLORD_ABILITY_NAME_1 + ChatColor.GRAY + "."),
	WARLORD_ABILITY_NAME_2(ChatColor.GREEN + "Parry"),
	WARLORD_ABILITY_2(Messages.ARENA + "" + ChatColor.GRAY + "You activated " + Messages.WARLORD_ABILITY_NAME_2 + ChatColor.GRAY + "."), 
	WARLORD_ABILITY_2_WEAR_OFF(Messages.ARENA + "" + Messages.WARLORD_ABILITY_NAME_2 + ChatColor.GRAY + " has worn off."),
	WARLORD_ABILITY_2_RECHARGE(Messages.ARENA + "" + ChatColor.GRAY + "You can now use " + Messages.WARLORD_ABILITY_NAME_2 + ChatColor.GRAY + ".");
	
    private final String value;

    private Messages(final String value) 
    {
        this.value = value;
    }

    public String getValue() 
    {
        return value;
    }
}
