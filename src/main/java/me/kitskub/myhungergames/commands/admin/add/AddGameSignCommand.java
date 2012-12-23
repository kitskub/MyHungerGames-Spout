package me.kitskub.myhungergames.commands.admin.add;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.listeners.SessionListener;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class AddGameSignCommand extends PlayerCommand {

	public AddGameSignCommand() {
		super(Perm.ADMIN_ADD_GAME_SIGN, Commands.ADMIN_ADD_HELP.getCommand(), "gamesign", 1, 1, "<game name>", "add a game sign");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		if (args.getRawArgs().isEmpty()) {
			ChatUtils.send(player, getUsage(), HungerGames.CMD_ADMIN);
			return;
		}
		game = GameManager.INSTANCE.getRawGame(args.getString(0));

		if (game == null) {
			ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
			return;
		}

		SessionListener.addSession(SessionListener.SessionType.GAME_SIGN_ADDER, player, game.getName(), "game", game.getName());
		ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "Left-click the sign to add it as a game sign.");
	}
}
