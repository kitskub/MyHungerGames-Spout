package me.kitskub.myhungergames.listeners;

import java.util.HashSet;
import java.util.Set;

import org.spout.api.entity.Player;
import org.spout.api.event.Listener;

public class TeleportListener implements Listener {
	private static Set<String> playerWhiteList = new HashSet<String>();
	
	public static void allowTeleport(Player player) {
		playerWhiteList.add(player.getName());
	}
	/*//TODO add back in teleport listener
	@EventHandler()
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		boolean isWhiteListed = playerWhiteList.remove(player.getName());
		HungerGame session = GameManager.INSTANCE.getRawPlayingSession(player);
		if (session == null) return;
		if (Config.CAN_TELEPORT.getBoolean(session.getSetup()) && !isWhiteListed && (event.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN) || event.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND))) {
			ChatUtils.error(player, "You cannot teleport while in-game!");
			//Logging.debug("Cancelling a teleport.");
			event.setCancelled(true);
		}
		
	}
*/
}
