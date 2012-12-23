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

public class ResumeCommand extends HGCommand {

	public ResumeCommand() {
		super(Perm.ADMIN_RESUME, "resume", ADMIN_COMMAND, 0, 2, "[game name [time]]", "resume a game");
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
		    ChatUtils.error(source, "%s does not exist.", name);
		    return;
		}

		if(args.getRawArgs().size() < 2) {
			if(!game.resumeGame(source, false)) {
				ChatUtils.error(source, "Failed to resume %s.", game.getName());
			}

		}

		else {
			int seconds;
			try {
				seconds = args.getInteger(1);
			} catch (Exception ex) {
				ChatUtils.error(source, "'%s' is not an integer.", args.getString(1));
				return;
			}
			if(!game.resumeGame(source, seconds)) {
				ChatUtils.error(source, "Failed to resume %s.", game.getName());
			}

		}
		return;
	}
}
