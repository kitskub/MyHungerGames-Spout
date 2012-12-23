package me.kitskub.myhungergames.commands.admin.add;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.listeners.SessionListener;
import me.kitskub.myhungergames.listeners.SessionListener.SessionType;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class AddInfoWallCommand extends PlayerCommand {

	public AddInfoWallCommand() {
		super(Perm.ADMIN_ADD_INFO_WALL, Commands.ADMIN_ADD_HELP.getCommand(), "infowall", 1, 1, "<game name>", "add an infowall");
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
	    
	    ChatUtils.send(player, "Click the two corners add an infowall.");
	    SessionListener.addSession(SessionType.INFO_WALL_ADDER, player, game.getName(), "game", game.getName());
	}
}
