package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class SubscribeCommand extends PlayerCommand {

	public SubscribeCommand() {
		super(Perm.USER_SUBSCRIBE, "subscribe", USER_COMMAND, 0, 1, "[game]", "subscribe to game messages");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {		
		if (args.getRawArgs().size() > 0) {
			game = GameManager.INSTANCE.getRawGame(args.getString(0));
			if (game == null) {
				ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
				return;
			}
		}
		if (GameManager.INSTANCE.getSubscribedPlayers(game).contains(player.getName())) {
			GameManager.INSTANCE.removedSubscribedPlayer(player, game);
			ChatUtils.send(player, "You have been unsubscribed from those MyHungerGames messages.");
		}
		else {
			GameManager.INSTANCE.addSubscribedPlayer(player, game);
			ChatUtils.send(player, "You have been subscribed to those MyHungerGames messages.");
		}
	}
}
