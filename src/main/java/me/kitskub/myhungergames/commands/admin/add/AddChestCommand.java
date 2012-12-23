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

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;


public class AddChestCommand extends PlayerCommand {

	public AddChestCommand() {
		super(Perm.ADMIN_ADD_CHEST, Commands.ADMIN_ADD_HELP.getCommand(), "chest", 1, 2, "<game name> [weight]", "add a chest with optional weight");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
	    game = GameManager.INSTANCE.getRawGame(args.getString(0));
	    
	    if (game == null) {
		    ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
		    return;
	    }
	    ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "Hit a chest to add it to %s.", game.getName());
	    if (args.getRawArgs().size() == 2){
		    try {
			    float weight = (float) args.getDouble(1);
			    SessionListener.addSession(SessionType.CHEST_ADDER, player, args.getString(0), "weight", weight);
			    return;
		    } catch (NumberFormatException numberFormatException) {}
	    }
	    SessionListener.addSession(SessionType.CHEST_ADDER, player, args.getString(0));
	}
}
