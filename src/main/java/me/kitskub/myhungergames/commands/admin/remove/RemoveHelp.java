package me.kitskub.myhungergames.commands.admin.remove;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;

public class RemoveHelp extends HGCommand {

	public RemoveHelp() {
		super(Perm.ADMIN_REMOVE_HELP, "remove", ADMIN_COMMAND, 0, 0, "", "remove items");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		for (Command c : children.values()) {
			ChatUtils.helpCommand(source, c.getUsage(), ADMIN_COMMAND);
		}
	}
}
