package me.kitskub.myhungergames;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.kitskub.myhungergames.utils.ConfigUtils.*;

import org.spout.api.geo.cuboid.Block;
import org.spout.api.inventory.ItemStack;
import org.spout.api.util.config.ConfigurationNode;


public class Config {

	public static List<ItemStack> getSpecialBlocksPlace(String setup) {
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s : Defaults.Config.SPECIAL_BLOCKS_PLACE.getStringList(setup)){
			list.add(getItemStack(s, 1));
		}
		return list;
	}
	
	public static List<ItemStack> getSpecialBlocksBreak(String setup) {
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s :Defaults.Config.SPECIAL_BLOCKS_BREAK.getStringList(setup)){
			list.add(getItemStack(s, 1));
		}
		return list;
	}
	
	public static List<ItemStack> getSpecialBlocksInteract(String setup) {
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s : Defaults.Config.SPECIAL_BLOCKS_INTERACT.getStringList(setup)){
			list.add(getItemStack(s, 1));
		}
		return list;
	}

	private static boolean getCanPlaceBlock(String setup, Block block, Set<String> checked) {
		boolean can = false;
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s : Files.CONFIG.getConfig().getNode("setups." + setup + "." + "special-blocks-place").getStringList()) {
			list.add(getItemStack(s, 1));
		}
		if (Files.CONFIG.getConfig().hasChild("setups." + setup + "." + "can-place-block")) {
			can |= Files.CONFIG.getConfig().getNode("setups." + setup + "." + "can-place-block").getBoolean() ^ list.contains(getItemStack(block));
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			can |= getCanPlaceBlock(parent, block, checked);
		}
		return can;
	}
	public static boolean getCanPlaceBlock(String setup, Block block) {
		boolean can = false;
		can |= getCanPlaceBlock(setup, block, new HashSet<String>());
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s : Files.CONFIG.getConfig().getNode("global.special-blocks-place").getStringList()){
			list.add(getItemStack(s, 1));
		}
		can |= Defaults.Config.CAN_PLACE_BLOCK.getGlobalBoolean() ^ list.contains(getItemStack(block));
		return can;
	}

	private static boolean getCanBreakBlock(String setup, Block block, Set<String> checked) {
		boolean can = false;
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s : Files.CONFIG.getConfig().getNode("setups." + setup + "." + "special-blocks-break").getStringList()){
			list.add(getItemStack(s, 1));
		}
		if (Files.CONFIG.getConfig().hasChild("setups." + setup + "." + "can-break-block")) {
			can |= Files.CONFIG.getConfig().getNode("setups." + setup + "." + "can-break-block").getBoolean() ^ list.contains(getItemStack(block));
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			can |= getCanBreakBlock(parent, block, checked);
		}
		return can;
	}
	public static boolean getCanBreakBlock(String setup, Block block) {
		boolean can = false;
		can |= getCanBreakBlock(setup, block, new HashSet<String>());
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s : Files.CONFIG.getConfig().getNode("global.special-blocks-break").getStringList()){
			list.add(getItemStack(s, 1));
		}
		can |= Defaults.Config.CAN_BREAK_BLOCK.getGlobalBoolean() ^ list.contains(getItemStack(block));
		return can;
	}

	private static boolean getCanInteractBlock(String setup, Block block, Set<String> checked) {
		boolean can = false;
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s : Files.CONFIG.getConfig().getNode("setups." + setup + "." + "special-blocks-interact").getStringList()){
			list.add(getItemStack(s, 1));
		}
		if (Files.CONFIG.getConfig().hasChild("setups." + setup + "." + "can-interact-block")) {
			can |= Files.CONFIG.getConfig().getNode("setups." + setup + "." + "can-interact-block").getBoolean() ^ list.contains(getItemStack(block));
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			can |= getCanInteractBlock(parent, block, checked);
		}
		return can;
	}
	public static boolean getCanInteractBlock(String setup, Block block) {
		boolean can = false;
		can |= getCanInteractBlock(setup, block, new HashSet<String>());
		List<ItemStack> list = new ArrayList<ItemStack>();
		for (String s : Files.CONFIG.getConfig().getNode("global.special-blocks-interact").getStringList()){
			list.add(getItemStack(s, 1));
		}
		can |= Defaults.Config.CAN_INTERACT_BLOCK.getGlobalBoolean() ^ list.contains(getItemStack(block));
		return can;
	}

	public static List<String> getSetups(){
		ConfigurationNode section = Files.CONFIG.getConfig().getNode("setups");
		if(section == null) return Collections.emptyList();
		List<String> list = (List<String>) section.getKeys(false);
		return (list == null) ? new ArrayList<String>() : list;
	}
	
}