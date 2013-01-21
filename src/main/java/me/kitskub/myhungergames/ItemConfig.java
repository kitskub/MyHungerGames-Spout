package me.kitskub.myhungergames;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.kitskub.myhungergames.utils.ConfigUtils;

import org.spout.api.inventory.ItemStack;
import org.spout.api.util.config.ConfigurationNode;
import org.spout.vanilla.plugin.material.enchantment.Enchantment;

public class ItemConfig {
	
	// Itemsets
	public static List<String> getItemSets(){
	    ConfigurationNode section = Files.ITEMCONFIG.getConfig().getNode("itemsets");
	    if(section == null) return Collections.emptyList();
	    List<String> list = new ArrayList<String>(section.getKeys(false));
	    return (list == null) ? new ArrayList<String>() : list;
	}
	
	public static Map<ItemStack, Float> getAllChestLootWithGlobal(List<String> itemsets){
	    Map<ItemStack, Float> toRet = new HashMap<ItemStack, Float>();
	    if(itemsets != null) {
		for(String s : itemsets){
			toRet.putAll(getChestLoot(s));
		}
	    }
	    toRet.putAll(getGlobalChestLoot());
	    return toRet;
	}
	
	public static Map<ItemStack, Double> getAllSponsorLootWithGlobal(List<String> itemsets){
	    Map<ItemStack, Double> toRet = new HashMap<ItemStack, Double>();
	    if(itemsets != null){
		for(String s : itemsets){
			toRet.putAll(getSponsorLoot(s));
		}
	    }
	    toRet.putAll(getGlobalSponsorLoot());
	    return toRet;
	}
	
	/** For safe recursiveness */
	private static Map<ItemStack, Float> getChestLoot(String itemset, Set<String> checked) {
		Map<ItemStack, Float> chestLoot = new HashMap<ItemStack, Float>();
		if (checked.contains(itemset)) return chestLoot;
		chestLoot.putAll(ConfigUtils.readItemSectionWithChance(Files.ITEMCONFIG.getConfig().getNode("itemsets." + itemset + ".chest-loot")));
		checked.add(itemset);
		for (String parent : Files.ITEMCONFIG.getConfig().getNode("itemsets." + itemset + ".inherits").getStringList()) {
			chestLoot.putAll(getChestLoot(parent, checked));
		}
		return chestLoot;
	}
	
	public static Map<ItemStack, Float> getChestLoot(String itemset){
		return getChestLoot(itemset, new HashSet<String>());
	}
	
	/** For safe recursiveness */
	private static Map<ItemStack, Double> getSponsorLoot(String itemset, Set<String> checked) {
		Map<ItemStack, Double> chestLoot = new HashMap<ItemStack, Double>();
		if (checked.contains(itemset)) return chestLoot;
		chestLoot.putAll(ConfigUtils.readItemSectionWithMoney(Files.ITEMCONFIG.getConfig().getNode("itemsets." + itemset + ".sponsor-loot")));
		checked.add(itemset);
		for (String parent : Files.ITEMCONFIG.getConfig().getNode("itemsets." + itemset + ".inherits").getStringList()) {
			checked.add(parent);
			chestLoot.putAll(getSponsorLoot(parent, checked));
		}
		return chestLoot;
	}
	public static Map<ItemStack, Double> getSponsorLoot(String itemset){
		return getSponsorLoot(itemset, new HashSet<String>());
	}
	
	
	/**
	 * Adds itemstack to chestLoot of the itemset provided, or global if itemset is empty or null
	 * @param itemset
	 * @param item
	 * @param chance
	 */
	public static void addChestLoot(String itemset, ItemStack item, float chance){
	    ConfigurationNode itemSection = null;
	    if (itemset == null || itemset.equalsIgnoreCase("")){
		    itemSection = ConfigUtils.getOrCreateSection(Files.ITEMCONFIG.getConfig(), "global.chest-loot");
	    }
	    else {
		    itemSection = ConfigUtils.getOrCreateSection(Files.ITEMCONFIG.getConfig(), "itemsets." + itemset + ".chest-loot");
	    }
	    StringBuilder builder = new StringBuilder();
	    builder.append(item.getMaterial().getName());
	    builder.append(item.getData());
	    builder.append(",");
	    builder.append(System.currentTimeMillis());
	    itemSection = itemSection.getChild(builder.toString());
	    itemSection.getNode("stack-size").setValue(item.getAmount());
	    itemSection.getNode("chance").setValue(chance);
	    Map<Enchantment, Integer> enchantments = Enchantment.getEnchantments(item);
	    for (Enchantment enchantment : enchantments.keySet()) {
		    itemSection.getNode(enchantment.getName()).setValue(enchantments.get(enchantment));
	    }
	}
	
	public static void addSponsorLoot(String itemset, ItemStack item, double cost){
	    ConfigurationNode itemSection = null;
	    if (itemset == null || itemset.equalsIgnoreCase("")){
		    itemSection = ConfigUtils.getOrCreateSection(Files.ITEMCONFIG.getConfig(), "global.sponsor-loot");
	    }
	    else {
		    itemSection = ConfigUtils.getOrCreateSection(Files.ITEMCONFIG.getConfig(), "itemsets." + itemset + ".sponsor-loot");
	    }
	    StringBuilder builder = new StringBuilder();
	    builder.append(item.getMaterial().getName());
	    builder.append(item.getData());
	    itemSection = itemSection.getChild(builder.toString());
	    itemSection.getNode("stack-size").setValue(item.getAmount());
	    itemSection.getNode("money").setValue(cost);
	    Map<Enchantment, Integer> enchantments = Enchantment.getEnchantments(item);
	    for (Enchantment enchantment : enchantments.keySet()) {
		    itemSection.getNode(enchantment.getName()).setValue(enchantments.get(enchantment));
	    }
	}
	
	public static Map<ItemStack, Float> getGlobalChestLoot() {
		Map<ItemStack, Float> chestLoot = new HashMap<ItemStack, Float>();
		ConfigurationNode itemSection = Files.ITEMCONFIG.getConfig().getNode("global.chest-loot");
		if(itemSection == null) return chestLoot;
		
		return ConfigUtils.readItemSectionWithChance(itemSection);
	}
	
	public static Map<ItemStack, Double> getGlobalSponsorLoot() {
		Map<ItemStack, Double> sponsorLoot = new HashMap<ItemStack, Double>();
		ConfigurationNode itemSection = Files.ITEMCONFIG.getConfig().getNode("global.sponsor-loot");
		if(itemSection == null) return sponsorLoot;
		
		return ConfigUtils.readItemSectionWithMoney(itemSection);
	}

	public static Set<String> getFixedChests() {
		ConfigurationNode chestSection = Files.ITEMCONFIG.getConfig().getNode("chests");
		if (chestSection == null) return new HashSet<String>();
		return chestSection.getKeys(false);
	}

	public static Set<String> getKits() {
		ConfigurationNode chestSection = Files.ITEMCONFIG.getConfig().getNode("kits");
		if (chestSection == null) return new HashSet<String>();
		return chestSection.getKeys(false);
	}
	
	public static List<ItemStack> getKit(String kit) {
		return ConfigUtils.readItemSection(Files.ITEMCONFIG.getConfig().getNode("kits." + kit));
	}
	
	private static List<ItemStack> getFixedChest(String chest, Set<String> checked) {
		List<ItemStack> fixedChests = new ArrayList<ItemStack>();
		if (checked.contains(chest)) return fixedChests;
		fixedChests.addAll(ConfigUtils.readItemSection(Files.ITEMCONFIG.getConfig().getNode("chests." + chest)));
		checked.add(chest);
		for (String parent : Files.ITEMCONFIG.getConfig().getNode("chests." + chest + ".inherits").getStringList()) {
			fixedChests.addAll(getFixedChest(parent, checked));
		}
		return fixedChests;
	}
	
	public static List<ItemStack> getFixedChest(String section) {
		return getFixedChest(section, new HashSet<String>());
	}
	
	public static List<ItemStack> getStaticRewards() {
		return ConfigUtils.readItemSection(Files.ITEMCONFIG.getConfig().getNode("rewards.static"));
			
	}
	
	public static Map<ItemStack, Float> getRandomRewards() {
		return ConfigUtils.readItemSectionWithChance(Files.ITEMCONFIG.getConfig().getNode("rewards.random"));
			
	}
	
	public static void addStaticReward(ItemStack item) {
		ConfigurationNode itemSection = ConfigUtils.getOrCreateSection(Files.ITEMCONFIG.getConfig(), "rewards.static");
		StringBuilder builder = new StringBuilder();
		builder.append(item.getMaterial().getName());
		builder.append(item.getData());
		builder.append(",");
		builder.append(System.currentTimeMillis());
		itemSection = itemSection.getChild(builder.toString());
		itemSection.getNode("stack-size").setValue(item.getAmount());
		Map<Enchantment, Integer> enchantments = Enchantment.getEnchantments(item);
		for (Enchantment enchantment : enchantments.keySet()) {
			itemSection.getNode(enchantment.getName()).setValue(enchantments.get(enchantment));
		}
	}
	
	public static void addRandomReward(ItemStack item, float chance) {
		ConfigurationNode itemSection = ConfigUtils.getOrCreateSection(Files.ITEMCONFIG.getConfig(), "rewards.random");
		StringBuilder builder = new StringBuilder();
		builder.append(item.getMaterial().getName());
		builder.append(item.getData());
		builder.append(",");
		builder.append(System.currentTimeMillis());
		itemSection = itemSection.getChild(builder.toString());
		itemSection.getNode("stack-size").setValue(item.getAmount());
		itemSection.getNode("chance").setValue(chance);
		Map<Enchantment, Integer> enchantments = Enchantment.getEnchantments(item);
		for (Enchantment enchantment : enchantments.keySet()) {
			itemSection.getNode(enchantment.getName()).setValue(enchantments.get(enchantment));
		}
	}
	
	public static int getMaxRandomItems() {
		return Files.ITEMCONFIG.getConfig().getNode("rewards.max-random").getInt(Defaults.ItemConfig.MAX_RANDOM_ITEMS.getInt());
	}
}
