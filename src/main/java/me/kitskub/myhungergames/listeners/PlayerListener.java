package me.kitskub.myhungergames.listeners;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.games.PlayerQueueHandler;
import org.spout.api.entity.Player;

import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.player.PlayerJoinEvent;
import org.spout.api.event.player.PlayerKickEvent;
import org.spout.api.event.player.PlayerLeaveEvent;
import org.spout.vanilla.event.player.PlayerDeathEvent;
import org.spout.vanilla.event.player.PlayerToggleSneakingEvent;


public class PlayerListener implements Listener {

	@EventHandler(order = Order.LATEST)
	public static void playerKilled(PlayerDeathEvent event) {
		Player killed = event.getPlayer();
		HungerGame gameOfKilled = GameManager.INSTANCE.getRawPlayingSession(killed);
		if (gameOfKilled == null) return;
		//Player killer = killed.getKiller();//TODO add back in game of killer
		//if (killer != null) {
		//	HungerGame gameOfKiller = GameManager.INSTANCE.getRawPlayingSession(killer);
		//	if (gameOfKiller == null) return;
		//	if (gameOfKilled.compareTo(gameOfKiller) == 0) {
		//		gameOfKiller.killed(killer, killed, event);
		//	}
		//}
		//else {
			gameOfKilled.killed(null, killed, event);
		//}
	}

	@EventHandler(order = Order.MONITOR)
	public static void playerQuit(PlayerLeaveEvent event) {
		GameManager.INSTANCE.playerLeftServer(event.getPlayer());
		HungerGames.playerLeftServer(event.getPlayer());
	}

	@EventHandler(order = Order.MONITOR)
	public static void playerKick(PlayerKickEvent event) {
		GameManager.INSTANCE.playerLeftServer(event.getPlayer());
		HungerGames.playerLeftServer(event.getPlayer());
	}
	
	/*@EventHandler//TODO add back in player move
	public void playerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		Location frozenLoc = GameManager.INSTANCE.getFrozenLocation(player);
		if (frozenLoc == null) {
			return;
		}
		int px = player.getLocation().getBlockX();
		int pz = player.getLocation().getBlockZ();
		int fx = frozenLoc.getBlockX();
		int fz = frozenLoc.getBlockZ();
		if ((px != fx) || (pz != fz)) {
			TeleportListener.allowTeleport(player);
			player.teleport(frozenLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
		}
	}*/

	@EventHandler(order = Order.MONITOR)
	public void playerJoin(PlayerJoinEvent event) {
		if (event.isCancelled()) return;
		PlayerQueueHandler.addPlayer(event.getPlayer());
	}
	
	@EventHandler(order = Order.MONITOR)
	public void playerSneak(PlayerToggleSneakingEvent event) {
		HungerGame game;
		if ((game = GameManager.INSTANCE.getRawPlayingSession(event.getPlayer())) == null) return;
		if (!Defaults.Config.HIDE_PLAYERS.getBoolean(game.getSetup())) return;
		event.setCancelled(true);
	}
	/*//TODO add back in pickup item
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)	
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (GameManager.INSTANCE.getSpectating(event.getPlayer()) != null) {
			event.setCancelled(true);
		}
	}*/
}
