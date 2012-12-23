package me.kitskub.myhungergames.commands.admin.remove;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.api.event.GameRemoveEvent;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.Spout;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class RemoveGameCommand extends PlayerCommand {

	public RemoveGameCommand() {
		super(Perm.ADMIN_REMOVE_GAME, Commands.ADMIN_REMOVE_HELP.getCommand(), "game", 1, 1, "<game name>", "remove a game");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
	    game = GameManager.INSTANCE.getRawGame(args.getString(0));
	    if(game == null){
		    ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
		    return;
	    }
	    
	    GameManager.INSTANCE.removeGame(args.getString(0));
	    Spout.getEventManager().callEvent(new GameRemoveEvent(game));
	    ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "%s has been removed.", args.getString(0));
	}
}
