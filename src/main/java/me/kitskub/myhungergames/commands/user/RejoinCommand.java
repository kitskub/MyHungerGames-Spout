package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class RejoinCommand extends PlayerCommand {

	public RejoinCommand() {
		super(Perm.USER_REJOIN, "rejoin", USER_COMMAND, 0, 0, "", "rejoin your current game");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		game = GameManager.INSTANCE.getRawSession(player);
		if (game == null) {
			ChatUtils.error(player, "You are currently not in a game.");
			return;
		}
		game.rejoin(player);
	}
}
