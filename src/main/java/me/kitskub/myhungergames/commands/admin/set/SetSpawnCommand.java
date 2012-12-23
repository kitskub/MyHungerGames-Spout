package me.kitskub.myhungergames.commands.admin.set;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class SetSpawnCommand extends PlayerCommand {
	public SetSpawnCommand() {
		super(Perm.ADMIN_SET_SPAWN, Commands.ADMIN_SET_HELP.getCommand(), "spawn", 1, 1, "<game name>", "set the spawnpoint for a game");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		if (args.getRawArgs().isEmpty()) {
			ChatUtils.helpCommand(player, getUsage(), HungerGames.CMD_ADMIN);
			return;
		}
		game = GameManager.INSTANCE.getRawGame(args.getString(0));
		if (game == null) {
			ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
			return;
		}
		game.setSpawn(player.getTransform().getTransform());
		ChatUtils.send(player, "Spawn has been set for %s.", game.getName());
	}
}
