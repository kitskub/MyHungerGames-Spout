package me.kitskub.myhungergames.commands.admin.add;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.api.event.GameCreateEvent;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class AddGameCommand extends PlayerCommand {

	public AddGameCommand() {
		super(Perm.ADMIN_ADD_GAME, Commands.ADMIN_ADD_HELP.getCommand(), "game", 1, 2, "<game name> [setup]", "add a game");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
	    game = GameManager.INSTANCE.getRawGame(args.getString(0));

	    if (game != null) {
		    ChatUtils.error(player, "%s already exists.", args.getString(0));
		    return;
	    }
	    if(args.getRawArgs().size() == 1){
		    GameManager.INSTANCE.createGame(args.getString(0));
	    }
	    else{
		    GameManager.INSTANCE.createGame(args.getString(0), args.getString(1));
	    }
	    GameCreateEvent event = new GameCreateEvent(GameManager.INSTANCE.getRawGame(args.getString(0)));
	    if(event.isCancelled()) {
	    	GameManager.INSTANCE.removeGame(args.getString(0));
	    	ChatUtils.error(player, "Creation of game %s was cancelled.", args.getString(0));
	    }
	    else {
	    	ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "%s has been created. To add spawn points, simply", args.getString(0));
	    	ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "type the command 'add spawnpoint <game name>'", HungerGames.CMD_ADMIN);
	    }
	}
}
