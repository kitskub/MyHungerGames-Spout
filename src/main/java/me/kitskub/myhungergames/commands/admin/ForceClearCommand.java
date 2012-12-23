package me.kitskub.myhungergames.commands.admin;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;

public class ForceClearCommand extends HGCommand {

	public ForceClearCommand() {
		super(Perm.ADMIN_FORCE_CLEAR, "forceclear", ADMIN_COMMAND, 0, 1, "<game name>", "force clear a game. NOT RECOMMENDED");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		String name = (args.getRawArgs().isEmpty()) ? Defaults.Config.DEFAULT_GAME.getGlobalString() : args.getString(0);
		if (name == null) {
			ChatUtils.helpCommand(source, getUsage(), HungerGames.CMD_ADMIN);
			return;
		}
		game = GameManager.INSTANCE.getRawGame(name);
		if (game == null) {
			ChatUtils.error(source, Lang.getNotExist().replace("<item>", name));
			return;
		}
		ChatUtils.send(source, "Clearing game.");
		game.clear();
	}
}
