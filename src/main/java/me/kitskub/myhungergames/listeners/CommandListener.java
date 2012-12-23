package me.kitskub.myhungergames.listeners;

import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.server.PreCommandEvent;

public class CommandListener implements Listener {
	
	@EventHandler(order = Order.DEFAULT)
	public void onCommand(PreCommandEvent event) {
		if (event.getCommandSource() instanceof Player == false) return;
		Player player = (Player) event.getCommandSource();
		String message = event.getCommand();
		if(message.startsWith(HungerGames.CMD_ADMIN) || message.startsWith( HungerGames.CMD_USER)) return;
		HungerGame session = GameManager.INSTANCE.getRawPlayingSession(player);
		if(session == null) return;
		message = message.split(" ")[0];
		if(Config.USE_COMMAND.getBoolean(session.getSetup()) ^ Config.SPECIAL_COMMANDS.getStringList(session.getSetup()).contains("/" + message)) {
			ChatUtils.error(player, "Cannot use that command while in game %s.", session.getName());
			event.setCancelled(true);
		}
		
	}

}
