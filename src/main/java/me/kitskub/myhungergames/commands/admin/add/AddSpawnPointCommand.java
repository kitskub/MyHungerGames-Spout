package me.kitskub.myhungergames.commands.admin.add;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.Lang;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.listeners.SessionListener;
import me.kitskub.myhungergames.listeners.SessionListener.SessionType;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class AddSpawnPointCommand extends PlayerCommand {

	public AddSpawnPointCommand() {
		super(Perm.ADMIN_ADD_SPAWNPOINT, Commands.ADMIN_ADD_HELP.getCommand(), "spawnpoint", 1, 1, "<game name>", "add a spawnpoint");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
	    game = GameManager.INSTANCE.getRawGame(args.getString(0));

	    if (game == null) {
		    ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
		    return;
	    }
	    
	    SessionListener.addSession(SessionType.SPAWN_ADDER, player, game.getName());
	    ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "Left-click blocks to add them as spawn points for %s. Right-click to finish.", game.getName());
	}
}
