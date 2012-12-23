package me.kitskub.myhungergames.commands.admin.add;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.ItemConfig;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;
import org.spout.api.inventory.ItemStack;
import org.spout.vanilla.component.inventory.PlayerInventory;

public class AddChestLootCommand extends PlayerCommand {

	public AddChestLootCommand() {
		super(Perm.ADMIN_ADD_CHEST_LOOT, Commands.ADMIN_ADD_HELP.getCommand(), "chestloot", 1, 2, "<chance> [itemset]", "adds the itemstack in hand to the specified itemset or global if no itemset is specified");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		float chance = 0;
		try {
			chance = (float) args.getDouble(0);
		}
		catch (NumberFormatException e) {
			ChatUtils.error(player, "{0} is not a valid number", args.getString(0));
		}
		ItemStack hand = player.get(PlayerInventory.class).getQuickbar().getCurrentItem();
		if (args.getRawArgs().size() < 2) {
			if (hand == null) {
				ChatUtils.error(player,"There is no item in hand. Perhaps you used the command wrong?");
				return;
			}
			ItemConfig.addChestLoot(null, hand, chance);
		}
		else {
			ItemConfig.addChestLoot(args.getString(1), hand, chance);
		}
		ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "Item in hand added to chest loot");
	}
}
