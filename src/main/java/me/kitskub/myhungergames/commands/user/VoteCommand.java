package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class VoteCommand extends PlayerCommand {

	public VoteCommand() {
		super(Perm.USER_VOTE, "vote", USER_COMMAND, 0, 0, "", "cast your vote that you are ready to play");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		game = GameManager.INSTANCE.getRawSession(player);
		if (game == null) {
			ChatUtils.error(player, "You must be in a game to vote. You can a game join by '" + Commands.USER_JOIN.getCommand().getUsage() + "'", HungerGames.CMD_USER);
			return;
		}
		game.addReadyPlayer(player);
		ChatUtils.send(player, "You have voted that you are ready.");
	}
}
