package me.kitskub.myhungergames.commands.admin.set;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;



public class SetEnabledCommand extends HGCommand {

	public SetEnabledCommand() {
		super(Perm.ADMIN_SET_ENABLED, Commands.ADMIN_SET_HELP.getCommand(), "enabled", 1, 2, "<game name> [true/false]", "enable or disable a game");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		game = GameManager.INSTANCE.getRawGame(args.getString(0));
		if (game == null) {
			ChatUtils.error(source, Lang.getNotExist().replace("<item>", args.getString(0)));
			return;
		}

		boolean flag;
		if (args.getRawArgs().size() == 1) {
			flag = true;
		} else {
			flag = Boolean.valueOf(args.getString(1));
		}
		game.setEnabled(flag);
		if (flag) {
			ChatUtils.send(source, "%s has been enabled.", game.getName());
		} else {
			ChatUtils.send(source, "%s has been disabled and stopped if it was running.", game.getName());
		}
	}
}
