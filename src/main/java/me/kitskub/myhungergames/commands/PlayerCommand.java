package me.kitskub.myhungergames.commands;

import me.kitskub.myhungergames.Defaults.Perm;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;

/**
 *
 * Same as command except it checks if CommandSender is player. If it is, passes it to handlePlayer
 */
public abstract class PlayerCommand extends HGCommand {

	public PlayerCommand(Perm perm, String name, String type, int min, int max, String usage, String desc) {
		super(perm, name, type, min, max, usage, desc);
	}

	public PlayerCommand(Perm perm, HGCommand parent, String name, int min, int max, String usage, String desc) {
		super(perm, parent, name, min, max, usage, desc);
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		if (!(source instanceof Player)) {
			source.sendMessage("In-game use only.");
			return;
		}
		handlePlayer((Player) source, command, args);
	}
	
	public abstract void handlePlayer(Player player, Command command, CommandContext args);
}
