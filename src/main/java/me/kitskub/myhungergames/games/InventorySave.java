package me.kitskub.myhungergames.games;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.spout.api.entity.Player;
import org.spout.api.inventory.ItemStack;
import org.spout.vanilla.component.inventory.PlayerInventory;

public class InventorySave {
	private static final Map<Player, InventorySave> savedInventories = new HashMap<Player, InventorySave>();
	private static final Map<Player, InventorySave> savedGameInventories = new HashMap<Player, InventorySave>();
	
	private ItemStack[] contents;
	private ItemStack[] armorContents;

	private InventorySave(Player player) {
		PlayerInventory inv = player.get(PlayerInventory.class);
		armorContents = (ItemStack[]) inv.getArmor().toArray();
		contents = (ItemStack[]) inv.getMain().toArray();
	}

	private void loadInventoryTo(Player player) {
		PlayerInventory inv = player.get(PlayerInventory.class);
		inv.getMain().addAll(Arrays.asList(contents));
		inv.getArmor().addAll(Arrays.asList(armorContents));
	}
	
	public static void saveAndClearInventory(Player player){
		PlayerInventory inv = player.get(PlayerInventory.class);
		savedInventories.put(player, new InventorySave(player));
		inv.getArmor().clear();
		inv.getMain().clear();
	}

	public static void loadInventory(Player player){
		if(!savedInventories.containsKey(player)) return;
		savedInventories.remove(player).loadInventoryTo(player);
	}
	
	public static void saveAndClearGameInventory(Player player){
		PlayerInventory inv = player.get(PlayerInventory.class);
		savedGameInventories.put(player, new InventorySave(player));
		inv.getArmor().clear();
		inv.getMain().clear();
	}

	public static void loadGameInventory(Player player){
		if(!savedGameInventories.containsKey(player)) return;
		savedGameInventories.remove(player).loadInventoryTo(player);
	}
}
