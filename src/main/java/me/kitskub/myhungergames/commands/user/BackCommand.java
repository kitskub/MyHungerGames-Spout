package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;
import org.spout.api.geo.discrete.Point;

public class BackCommand extends PlayerCommand {

	public BackCommand() {
		super(Perm.USER_BACK, "back", USER_COMMAND, 0, 0, "", "returns a player to where they were before they joined");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {		
		if (GameManager.INSTANCE.getSession(player) != null) {
			ChatUtils.send(player, "You cannot use that command while you are in-game.");
			return;
		}
		Point loc = GameManager.INSTANCE.getAndRemoveBackPoint(player);
		if (loc != null) {
			ChatUtils.send(player, "Teleporting you to your back Point.");
			player.teleport(loc);
		}
		else {
			ChatUtils.error(player, "For some reason, there was no back Point set. Did you already teleport back?");
		}
	}
}
