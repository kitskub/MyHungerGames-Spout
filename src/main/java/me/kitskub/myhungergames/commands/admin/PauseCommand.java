package me.kitskub.myhungergames.commands.admin;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;

public class PauseCommand extends HGCommand {

	public PauseCommand() {
		super(Perm.ADMIN_PAUSE, "pause", ADMIN_COMMAND, 0, 1, "[game name]", "pause a game");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		String name = args.getRawArgs().isEmpty() ? Defaults.Config.DEFAULT_GAME.getGlobalString() : args.getString(0);
		if (name == null) {
			ChatUtils.helpCommand(source, getUsage(), HungerGames.CMD_ADMIN);
			return;
		}

		game = GameManager.INSTANCE.getRawGame(name);
		if (game == null) {
		    ChatUtils.error(source, "%s does not exist.", name);
		    return;
		}

		if(game.pauseGame(source)) {
			ChatUtils.broadcast(game, "%s has been paused.", game.getName());
		}
	}
}
