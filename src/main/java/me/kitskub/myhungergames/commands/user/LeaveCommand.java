package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class LeaveCommand extends PlayerCommand {

	public LeaveCommand() {
		super(Perm.USER_LEAVE, "leave", USER_COMMAND, 0, 0, "", "leave current game temporarily (if enabled)");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		game = GameManager.INSTANCE.getRawPlayingSession(player);
		if (game == null) {
			ChatUtils.error(player, "You are currently not playing a game.");
			return;
		}

		game.leave(player, true);
	}
}
