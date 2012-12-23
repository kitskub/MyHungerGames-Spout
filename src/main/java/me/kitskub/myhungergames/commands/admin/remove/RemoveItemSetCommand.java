package me.kitskub.myhungergames.commands.admin.remove;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class RemoveItemSetCommand extends PlayerCommand {

	public RemoveItemSetCommand() {
		super(Perm.ADMIN_REMOVE_ITEMSET, Commands.ADMIN_REMOVE_HELP.getCommand(), "itemset", 2, 2, "<game name> <itemset name>", "remove a game");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		String name = (args.getRawArgs().isEmpty()) ? Defaults.Config.DEFAULT_GAME.getGlobalString() : args.getString(0);
		if (name == null) {
			ChatUtils.helpCommand(player, getUsage(), HungerGames.CMD_ADMIN);
			return;
		}
		game = GameManager.INSTANCE.getRawGame(name);
		if(game == null){
			ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
			return;
		}
		game.removeItemSet(args.getString(1));
	}
}
