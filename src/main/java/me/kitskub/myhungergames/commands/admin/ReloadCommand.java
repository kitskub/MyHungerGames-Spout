package me.kitskub.myhungergames.commands.admin;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;

public class ReloadCommand extends HGCommand {

	public ReloadCommand() {
		super(Perm.ADMIN_RELOAD, "reload", ADMIN_COMMAND, 0, 0, "", "reload MyHungerGames");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		HungerGames.reload();
		ChatUtils.send(source, ChatUtils.getPrefix() + "Reloaded %s", HungerGames.getInstance().getDescription().getVersion());
	}
}
