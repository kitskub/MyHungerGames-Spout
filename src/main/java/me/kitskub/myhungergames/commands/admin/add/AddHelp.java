package me.kitskub.myhungergames.commands.admin.add;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;

public class AddHelp extends HGCommand {

	public AddHelp() {
		super(Perm.ADMIN_ADD_HELP, "add", ADMIN_COMMAND, 0, 0, "", "add items");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		for (Command c : children.values()) {
			ChatUtils.helpCommand(source, c.getUsage(), ADMIN_COMMAND);
		}
	}
}
