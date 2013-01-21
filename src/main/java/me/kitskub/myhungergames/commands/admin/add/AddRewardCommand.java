package me.kitskub.myhungergames.commands.admin.add;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.ItemConfig;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;
import org.spout.api.inventory.ItemStack;
import org.spout.vanilla.plugin.component.inventory.PlayerInventory;

public class AddRewardCommand extends PlayerCommand {

	public AddRewardCommand() {
		super(Perm.ADMIN_ADD_REWARD, Commands.ADMIN_ADD_HELP.getCommand(), "reward", 0, 1, "[chance]", "add current item in hand to static rewards or as a random if chance is specified");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		ItemStack hand = player.get(PlayerInventory.class).getQuickbar().getCurrentItem();
		if (hand == null) {
			ChatUtils.error(player, "Cannot add no item to rewards!");
		}
		if (args.getRawArgs().isEmpty()) {
			ItemConfig.addStaticReward(hand);
		}
		else {
			float chance = 0;
			try {
				chance = (float) args.getDouble(0);
			}
			catch (NumberFormatException e) {
				ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "{0} is not a valid number. Defaulting to 0", args.getString(0));
			}
			ItemConfig.addRandomReward(hand, chance);
		}
		ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "Item in hand added to rewards");
	}
}
