package me.kitskub.myhungergames.commands.admin.remove;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.listeners.SessionListener;
import me.kitskub.myhungergames.listeners.SessionListener.SessionType;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class RemoveChestCommand extends PlayerCommand {

	public RemoveChestCommand() {
		super(Perm.ADMIN_REMOVE_CHEST, Commands.ADMIN_REMOVE_HELP.getCommand(), "chest", 1, 1, "<game name>", "remove a chest if it added to the game or blacklists it if it isn't");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
	    game = GameManager.INSTANCE.getRawGame(args.getString(0));
	    if(game == null){
		    ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
		    return;
	    }

	    SessionListener.addSession(SessionType.CHEST_REMOVER, player, args.getString(0));
	    ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "Hit a chest to remove it from %s.", game.getName());
	}
}
