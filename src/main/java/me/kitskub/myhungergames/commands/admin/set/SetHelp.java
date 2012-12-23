package me.kitskub.myhungergames.commands.admin.set;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;



public class SetHelp extends HGCommand {

	public SetHelp() {
		super(Perm.ADMIN_SET_HELP, "set", ADMIN_COMMAND, 0, 0, "", "set items");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		for (Command c : children.values()) {
			ChatUtils.helpCommand(source, c.getUsage(), ADMIN_COMMAND);
		}
	}
}
