package me.kitskub.myhungergames.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.kitskub.myhungergames.Logging;

import org.spout.api.geo.cuboid.Block;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.Material;
import org.spout.api.util.config.ConfigurationNode;
import org.spout.api.util.config.ConfigurationNodeSource;
import org.spout.vanilla.plugin.material.VanillaMaterial;
import org.spout.vanilla.plugin.material.enchantment.Enchantment;
import org.spout.vanilla.plugin.material.enchantment.Enchantments;

public class ConfigUtils {
	
	public static ConfigurationNode getOrCreateSection(ConfigurationNodeSource section, String string) {
		ConfigurationNode config = section.addNode(string);
		return config;
	}
	
	public static ItemStack getItemStack(Block block) {
		return new ItemStack(block.getMaterial(), 1, block.getData());
	}
	
	public static ItemStack getItemStack(String s, int stackSize){
		s = s.split(",")[0];
		String[] keyParts = s.split(":");
		Material mat = Material.get(keyParts[0]);
		if(mat == null) {
			Logging.debug("Material with name {0} could not be loaded.", keyParts[0]);
			return null;
		}
		ItemStack item = new ItemStack(mat, stackSize);
		if(keyParts.length == 2){
			try{
				item.setData(Integer.getInteger(keyParts[1]));
			}
			catch(NumberFormatException e){
				Logging.debug("Can't convert {0} to short", keyParts[1]);
			}
		}
		return item;
	}
		
	public static List<ItemStack> readItemSection(ConfigurationNode chestSection) {
		List<ItemStack> toRet = new ArrayList<ItemStack>();
		if(chestSection == null) return toRet;

		for(String key : chestSection.getKeys(false)) {
		    ConfigurationNode section = chestSection.getNode(key);
		    ItemStack item = getItemStack(section);
		    if(item == null) continue;
		    toRet.add(item);
		}
		return toRet;
	}
	
	public static Map<ItemStack, Float> readItemSectionWithChance(ConfigurationNode itemSection){
	    Map<ItemStack, Float> toRet = new HashMap<ItemStack, Float>();
	    if(itemSection == null) return toRet;
	    
	    for(String key : itemSection.getKeys(false)) {
		    ConfigurationNode section = itemSection.getNode(key);
		    ItemStack item = getItemStack(section);
		    if (item == null) continue;
		    float chance = new Double(section.getChild("chance").getDouble(0.3333337)).floatValue();
		    toRet.put(item, chance);
	    }
	    return toRet;
	}
		
	public static Map<ItemStack, Double> readItemSectionWithMoney(ConfigurationNode itemSection){
	    Map<ItemStack, Double> toRet = new HashMap<ItemStack, Double>();
	    if(itemSection == null) return toRet;
	    
	    for(String key : itemSection.getKeys(false)) {
		    ConfigurationNode section = itemSection.getNode(key);
		    ItemStack item = getItemStack(section);
		    if(item == null) continue;

		    double money = section.getChild("money").getDouble(10.00);
		    toRet.put(item, money);
	    }
	    return toRet;
	}
	
	private static ItemStack getItemStack(ConfigurationNode section) {
	    if (section == null) return null;
	    int stackSize = section.getChild("stack-size").getInt(1);
	    ItemStack item = getItemStack(section.getPathElements()[section.getPathElements().length - 1], stackSize);
	    if(item == null) return null;

	    for(String str : section.getKeys(false)) {
		    Enchantment enchant = Enchantments.getByName(str);
		    if(enchant == null || item.getMaterial() instanceof VanillaMaterial == false) {
			    continue;
		    }
		    int level = section.getNode(str).getInt(1);
		    try {
			    Enchantment.addEnchantment(item, enchant, level, true);
		    } catch (Exception ex) {
		    }
	    }
	    return item;
	}
}
