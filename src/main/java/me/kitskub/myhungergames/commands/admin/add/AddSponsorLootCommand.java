package me.kitskub.myhungergames.commands.admin.add;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.ItemConfig;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;
import org.spout.api.inventory.ItemStack;
import org.spout.vanilla.plugin.component.inventory.PlayerInventory;

public class AddSponsorLootCommand extends PlayerCommand {
	
	public AddSponsorLootCommand() {
		super(Perm.ADMIN_ADD_SPONSOR_LOOT, Commands.ADMIN_ADD_HELP.getCommand(), "sponsorloot", 1, 2, "<money> [itemset]", "adds the itemstack in hand to the specified itemset or global if no itemset is specified");
	}
	
	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		if (args.getRawArgs().isEmpty()) {
			ChatUtils.helpCommand(player, getUsage(), HungerGames.CMD_ADMIN);
			return;
		}
		ItemStack hand = player.get(PlayerInventory.class).getQuickbar().getCurrentItem();
		float chance = (float) args.getDouble(0);
		if (args.getRawArgs().size() < 2) {
			ItemConfig.addSponsorLoot(null, hand, chance);
		}
		else {
			ItemConfig.addSponsorLoot(args.getString(1), hand, chance);
		}
		ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "Item in hand added to sponsor loot", game.getName());
	}
}
