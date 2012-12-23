package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class SpectateCommand extends PlayerCommand {

	public SpectateCommand() {
		super(Perm.USER_SPECTATE, "spectate", USER_COMMAND, 0, 2, "[<game name> [player]]", "sets player to flying to spectate a game or cancels a spectation");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		if (GameManager.INSTANCE.removeSpectator(player)) return;
		String name = (args.getRawArgs().isEmpty()) ? Defaults.Config.DEFAULT_GAME.getGlobalString() : args.getString(0);
		if (name == null) {
			ChatUtils.helpCommand(player, getUsage(), HungerGames.CMD_USER);
			return;
		}
		game = GameManager.INSTANCE.getRawGame(name);
		if (game == null) {
			ChatUtils.error(player, Lang.getNotExist().replace("<item>", name));
			return;
		}
		Player spectated;
		if (GameManager.INSTANCE.getSpectating(player) != null) {
			ChatUtils.error(player, "You are already spectating a game.");
			return;
		}
		if (args.getRawArgs().size() < 2 || (spectated = args.getPlayer(1, false)) == null) {
			GameManager.INSTANCE.addSpectator(player, game, null);
		}
		else {
			GameManager.INSTANCE.addSpectator(player, game, spectated);
		}
	}
}
