package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class QuitCommand extends PlayerCommand {

	public QuitCommand() {
		super(Perm.USER_QUIT, "quit", USER_COMMAND, 0, 0, "", "quit the current game indefinitely");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		game = GameManager.INSTANCE.getRawSession(player);
		if (game == null) {
			ChatUtils.error(player, "You are currently not in a game.");
			return;
		}

		game.quit(player, true);
	}
}
