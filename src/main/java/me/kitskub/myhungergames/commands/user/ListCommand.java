package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.utils.ChatUtils;

import java.util.Collection;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;


public class ListCommand extends HGCommand {

	public ListCommand() {
		super(Perm.USER_LIST, "list", USER_COMMAND, 0, 0, "", "list games");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		ChatUtils.send(source, ChatStyle.BRIGHT_GREEN, ChatUtils.getHeadLiner());
		Collection<HungerGame> games = GameManager.INSTANCE.getRawGames();
		if (games.isEmpty()) {
			ChatUtils.error(source, "No games have been created yet.");
			return;
		}

		for (HungerGame g : games) {
			ChatUtils.send(source, ChatStyle.GOLD, "- " + g.getInfo());
		}
	}
}
