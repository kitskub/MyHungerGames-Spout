package me.kitskub.myhungergames.utils;

import com.google.common.base.Strings;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.ItemConfig;
import me.kitskub.myhungergames.Logging;
import me.kitskub.myhungergames.WorldNotFoundException;
import org.apache.commons.lang3.ArrayUtils;
import org.spout.api.Spout;
import org.spout.api.entity.Player;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;
import org.spout.api.geo.discrete.Transform;
import org.spout.api.inventory.ItemStack;
import org.spout.api.math.MathHelper;
import org.spout.api.math.Vector3;
import org.spout.vanilla.plugin.component.inventory.PlayerInventory;
import org.spout.vanilla.plugin.component.substance.material.chest.Chest;

public class GeneralUtils {
	public static boolean equals(Point loc1, Point loc2) {
		return loc1.getWorld() == loc2.getWorld()
			&& loc1.getBlockX() == loc2.getBlockX()
			&& loc1.getBlockY() == loc2.getBlockY()
			&& loc1.getBlockZ() == loc2.getBlockZ();
	}
	
	public static String parseToString(Transform loc) {
		if (loc == null) return "";
		DecimalFormat df = new DecimalFormat();
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(symbols);
		df.setGroupingUsed(false);
		return String.format("%s %s %s %s %s %s", df.format(loc.getPosition().getX()), df.format(loc.getPosition().getY()), df.format(loc.getPosition().getZ()), df.format(loc.getRotation().getYaw()), 
			df.format(loc.getRotation().getPitch()), loc.getPosition().getWorld().getName());
	}

	public static Transform parseToTransform(String str) throws NumberFormatException, WorldNotFoundException, IllegalArgumentException {
		Strings.emptyToNull(str);
		if (str == null) {
			throw new IllegalArgumentException("Point can not be null.");
		}
		String[] strs = str.split(" ");
		float x = Float.parseFloat(strs[0]);
		float y = Float.parseFloat(strs[1]);
		float z = Float.parseFloat(strs[2]);
		float yaw = Float.parseFloat(strs[3]);
		float pitch = Float.parseFloat(strs[4]);
		World world = Spout.getEngine().getWorld(strs[5]);
		if (world == null) throw new WorldNotFoundException("Could not load world \"" + strs[5] + "\" when loading Point \"" + str);
		return new Transform(new Point(world, x, y, z), MathHelper.rotation(pitch, yaw, 0), Vector3.ONE);
	}
	
	public static String parseToString(Point loc) {
		if (loc == null) return "";
		DecimalFormat df = new DecimalFormat();
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(symbols);
		df.setGroupingUsed(false);
		return String.format("%s %s %s %s", df.format(loc.getX()), df.format(loc.getY()), df.format(loc.getZ()), loc.getWorld().getName());
	}

	public static Point parseToPoint(String str) throws NumberFormatException, WorldNotFoundException, IllegalArgumentException {
		Strings.emptyToNull(str);
		if (str == null) {
			throw new IllegalArgumentException("Point can not be null.");
		}
		String[] strs = str.split(" ");
		float x = Float.parseFloat(strs[0]);
		float y = Float.parseFloat(strs[1]);
		float z = Float.parseFloat(strs[2]);
		World world = Spout.getEngine().getWorld(strs[3]);
		if (world == null) throw new WorldNotFoundException("Could not load world \"" + strs[5] + "\" when loading Point \"" + str);
		return new Point(world, x, y, z);
	}
	
	public static String formatTime(int time) {

		List<String> strs = new ArrayList<String>();
		if(time > 3600) {
			strs.add(String.format("%d hour(s)", (time / 3600) % 24));
		}
		if(time > 60) {
			strs.add(String.format("%d minute(s)", (time / 60) % 60));
		}
		strs.add(String.format("%d second(s)", time % 60));
		StringBuilder buff = new StringBuilder();
		String sep = "";
		for (String str : strs) {
			buff.append(sep);
			buff.append(str);
			sep = ", ";
		}
		return buff.toString();
	}
	
	public static boolean hasInventoryBeenCleared(Player player) {
		PlayerInventory inventory = player.get(PlayerInventory.class);
		for (ItemStack item : inventory.getMain()) {
			if (item != null) {
				return false;
			}

		}
		for (ItemStack item : inventory.getArmor()) {
			if (item != null) {
				return false;
			}

		}
		return true;
	}

	public static void fillFixedChest(Chest chest, String name) {
		chest.getInventory().clear();
		List<ItemStack> items = ItemConfig.getFixedChest(name);
		for (ItemStack stack : items) {
			int index = 0;
			do {
				index = HungerGames.RANDOM.nextInt(chest.getInventory().size());
			} while (chest.getInventory().get(index) != null);
			
			chest.getInventory().set(index, stack);
		}
	}
	
	public static void fillChest(Chest chest, float weight, List<String> itemsets) {
		if (ItemConfig.getGlobalChestLoot().isEmpty() && (itemsets == null || itemsets.isEmpty())) {
			return;
		}
		chest.getInventory().clear();
		Map<ItemStack, Float> itemMap = ItemConfig.getAllChestLootWithGlobal(itemsets);
		List<ItemStack> items = new ArrayList<ItemStack>(itemMap.keySet());
		int size = chest.getInventory().size();
		final int maxItemSize = 100;
		int numItems = items.size() >= maxItemSize ? size : (int) Math.ceil((size * Math.sqrt(items.size()))/Math.sqrt(maxItemSize));
		int minItems = (int) Math.floor(numItems/2);
		int itemsIn = 0;
		for (int cntr = 0; cntr < numItems || itemsIn < minItems; cntr++) {
			int index = 0;
			do {
				index = HungerGames.RANDOM.nextInt(chest.getInventory().size());
			} while (chest.getInventory().get(index) != null);
			
			ItemStack item = items.get(HungerGames.RANDOM.nextInt(items.size()));
			if (weight * itemMap.get(item) >= HungerGames.RANDOM.nextFloat()) {
				chest.getInventory().set(index, item);
				itemsIn++;
			}

		}
	}

	public static void rewardPlayer(Player player) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		items.addAll(ItemConfig.getStaticRewards());
		Logging.debug("rewardPlayer: items after static: " + ArrayUtils.toString(items));
		Map<ItemStack, Float> itemMap = ItemConfig.getRandomRewards();

		int size = ItemConfig.getMaxRandomItems();
		final int maxItemSize = 25;
		int numItems = items.size() >= maxItemSize ? size : (int) Math.ceil((size * Math.sqrt(items.size()))/Math.sqrt(maxItemSize));
		Logging.debug("rewardPlayer: items after random: " + ArrayUtils.toString(items));
		for (int cntr = 0; cntr < numItems; cntr++) {			
			ItemStack item = null;
			while (item == null) { // TODO items should not have any null elements, but do.
				item = items.get(HungerGames.RANDOM.nextInt(items.size()));
			}
			if (itemMap.get(item) >= HungerGames.RANDOM.nextFloat()) {
				items.add(item);
			}

		}
		player.get(PlayerInventory.class).getMain().addAll(Arrays.asList(items.toArray(new ItemStack[0])));
	}
}
