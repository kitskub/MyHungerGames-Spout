package me.kitskub.myhungergames.games;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.GameManager;
import org.spout.api.chat.channel.ChatChannel;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;

public class HungerGameChatChannel extends ChatChannel {
	private final Player player;

	public HungerGameChatChannel(String name, Player player) {
		super("MyHungerGames - " + name + " - " + player.getDisplayName());
		this.player = player;
	}

	@Override
	public Set<CommandSource> getReceivers() {
		HungerGame chatterGame = GameManager.INSTANCE.getRawSession(player);
		List<Player> nearbyPlayers = player.getTransform().getPosition().getWorld().getNearbyPlayers(player, Config.CHAT_DISTANCE.getInt(chatterGame.getSetup()));
		Set<CommandSource> ret = new HashSet<CommandSource>();
		for (Player nearby : nearbyPlayers) {
			HungerGame receipientGame = GameManager.INSTANCE.getRawSession(nearby);
			if (receipientGame != null && chatterGame.compareTo(receipientGame) == 0) {
				ret.add(nearby);
			}
		}
		return ret;
	}
}
/*//TODO add back in admin speaking to game
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
}*/
