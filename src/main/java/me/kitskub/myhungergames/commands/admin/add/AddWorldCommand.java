package me.kitskub.myhungergames.commands.admin.add;

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
import org.spout.api.geo.World;

public class AddWorldCommand extends PlayerCommand {

	public AddWorldCommand() {
		super(Perm.ADMIN_ADD_WORLD, Commands.ADMIN_ADD_HELP.getCommand(), "world", 1, 2, "<game name> [world]", "adds the world specified or you are currently in to the game");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		if(args.getRawArgs().isEmpty()){
			ChatUtils.helpCommand(player, getUsage(), HungerGames.CMD_ADMIN);
			return;
		}
		game = GameManager.INSTANCE.getRawGame(args.getString(0));

		if (game == null) {
			ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
			return;
		}
		if (args.getRawArgs().size() < 2) {
			game.addWorld(player.getWorld());
		}
		else {
			World world = args.getWorld(1);
			if (world == null) {
				ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(1)));
				return;
			}
			else {
				game.addWorld(player.getWorld());
			}
		}
		ChatUtils.send(player, "World added!");
	}
}
