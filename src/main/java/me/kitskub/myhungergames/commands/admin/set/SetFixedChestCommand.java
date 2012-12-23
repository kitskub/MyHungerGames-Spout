package me.kitskub.myhungergames.commands.admin.set;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.ItemConfig;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.listeners.SessionListener;
import me.kitskub.myhungergames.listeners.SessionListener.SessionType;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class SetFixedChestCommand extends PlayerCommand {

	public SetFixedChestCommand() {
		super(Perm.ADMIN_SET_FIXED_CHEST, Commands.ADMIN_SET_HELP.getCommand(), "fixedchest", 2, 2, "<game name> <name|false>", "Sets a chest to a specific fixed chest itemset or removes it from being a fixed chest if name is false");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		game = GameManager.INSTANCE.getRawGame(args.getString(0));
		if (game == null) {
			ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
			return;
		}

		String name = args.getString(1);
		if (name.equalsIgnoreCase("false")) {
			SessionListener.addSession(SessionType.FIXED_CHEST_REMOVER, player, game.getName());
			ChatUtils.send(player, "Click chest to remove it from being a fixed item chest.");
			return;
		}
		if (!ItemConfig.getFixedChests().contains(name)) {
			ChatUtils.error(player, Lang.getNotExist().replace("<item>", name));
			return;
		}
		SessionListener.addSession(SessionType.FIXED_CHEST_ADDER, player, game.getName(), "name", name);
		ChatUtils.send(player, "Click chest to add it as a fixed item chest.");
	}
}
