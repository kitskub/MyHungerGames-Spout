package me.kitskub.myhungergames.listeners;


import java.util.HashSet;
import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.games.HungerGame;

import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.player.PlayerChatEvent;


public class ChatListener implements Listener {
	/*//TODO add back in Chat
	@EventHandler(order = Order.LATEST_IGNORE_CANCELLED)
	public void playerChat(PlayerChatEvent event) {
		HungerGame chatterGame = GameManager.INSTANCE.getRawSession(event.getPlayer());
		for (Player p : new HashSet<Player>(event.getReceivers())) {
			HungerGame receipientGame = GameManager.INSTANCE.getRawSession(event.getPlayer());
			if (receipientGame != null && Config.ISOLATE_PLAYER_CHAT.getBoolean(receipientGame.getSetup())) {
				if (chatterGame != null) {
					if (chatterGame.compareTo(receipientGame) == 0 
						&& event.getPlayer().getLocation().getWorld() == p.getLocation().getWorld()) {
						float distanceRequired = Config.CHAT_DISTANCE.getInt(receipientGame.getSetup());
						if (distanceRequired != 0 && event.getPlayer().getLocation().distance(p.getLocation()) >= distanceRequired) {
							if (HungerGames.hasPermission(event.getPlayer(), Perm.ADMIN_CHAT)) {
								if (event.getMessage().startsWith("hg ")) {
									event.setMessage(event.getMessage().substring(3));
									return;
								}
								if (event.getMessage().startsWith("hg")) {
									event.setMessage(event.getMessage().substring(2));
									return;
								}
							}
							//Logging.debug("Cancelling chat because too far.");
							event.getRecipients().remove(p);
						}
					}
					else {
						//Logging.debug("Cancelling chat because games are not the same or different worlds.");
						event.getRecipients().remove(p);
					}
				}
				else {
					if (HungerGames.hasPermission(event.getPlayer(), Perm.ADMIN_CHAT)) {
						if (event.getMessage().startsWith("hg ")) {
							event.setMessage(event.getMessage().substring(3));
							return;
						}
						if (event.getMessage().startsWith("hg")) {
							event.setMessage(event.getMessage().substring(2));
							return;
						}
					}
					//Logging.debug("Cancelling chat because chatter was not in a game.");
					event.getRecipients().remove(p);
				}
			}
		}
	}*/
}
