package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class JoinCommand extends PlayerCommand {

	public JoinCommand() {
		super(Perm.USER_JOIN, "join", USER_COMMAND, 0, 1, "[game name]", "join a game");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		String name = (args.getRawArgs().isEmpty()) ? Defaults.Config.DEFAULT_GAME.getGlobalString() : args.getString(0);
		if (name == null) {
			ChatUtils.helpCommand(player, getUsage(), HungerGames.CMD_USER);
			return;
		}

		game = GameManager.INSTANCE.getRawGame(name);
		if (game == null) {
			ChatUtils.error(player, Lang.getNotExist().replace("<item>", name));
			return;
		}

		game.join(player);
	}
}
