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

public class AddItemSetCommand extends PlayerCommand {

	public AddItemSetCommand() {
		super(Perm.ADMIN_ADD_ITEMSET, Commands.ADMIN_ADD_HELP.getCommand(), "itemset", 2, 2, "<game name> <itemset name>", "add an itemset");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {	    
	    game = GameManager.INSTANCE.getRawGame(args.getString(0));
	    
	    if (game == null) {
		    ChatUtils.error(player, Lang.getNotExist().replace("<item>", args.getString(0)));
		    return;
	    }
	    game.addItemSet(args.getString(1));
	    ChatUtils.send(player, "Itemset created!");
	}
}
