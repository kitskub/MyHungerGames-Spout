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

public class AddCuboidCommand extends PlayerCommand {

	public AddCuboidCommand() {
		super(Perm.ADMIN_ADD_CUBOID, Commands.ADMIN_ADD_HELP.getCommand(), "cuboid", 1, 1, "<game name>", "add a cuboid");
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
	    
	    ChatUtils.send(player, "Click the two corners add a cuboid.");
	    SessionListener.addSession(SessionType.CUBOID_ADDER, player, game.getName());
	}
}
