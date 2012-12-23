package me.kitskub.myhungergames.commands.admin;

import me.kitskub.myhungergames.*;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;



public class StartCommand extends HGCommand {

	public StartCommand() {
		super(Perm.ADMIN_START, "start", ADMIN_COMMAND, 0, 2, "[[game name] [seconds]]", "manually start a game");
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

		int seconds;

		if (args.getRawArgs().size() == 2) {
			try {
				seconds = args.getInteger(1);
			} catch (Exception ex) {
				ChatUtils.error(source, "'%s' is not an integer.", args.getString(1));
				return;
			}
		}

		else {
			seconds = Defaults.Config.DEFAULT_TIME.getInt(game.getSetup());
		}
		if (!game.startGame(source, seconds)) {
			ChatUtils.error(source, "Failed to start %s.", game.getName());
		}
	}
}
