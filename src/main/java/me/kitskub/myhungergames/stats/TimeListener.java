package me.kitskub.myhungergames.stats;

import me.kitskub.myhungergames.api.event.PlayerJoinGameEvent;
import me.kitskub.myhungergames.api.event.GamePauseEvent;
import me.kitskub.myhungergames.api.event.PlayerKillEvent;
import me.kitskub.myhungergames.api.event.GameEndEvent;
import me.kitskub.myhungergames.api.event.GameStartEvent;
import me.kitskub.myhungergames.api.event.PlayerLeaveGameEvent;
import me.kitskub.myhungergames.api.Game;
import me.kitskub.myhungergames.api.Game.GameState;
import me.kitskub.myhungergames.stats.PlayerStat.PlayerState;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;

/**
 * Used for stats per-player
 */
public class TimeListener implements Listener {
	private Map<String, Long> startTimes = new HashMap<String, Long>();

	@EventHandler(order = Order.MONITOR)
	public void onGameEnd(GameEndEvent event) {
		if (!event.isCancelled()) return;
		for (Player p : event.getGame().getRemainingPlayers()) {
			playerStopped(event.getGame(), p);
		}
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onGamePause(GamePauseEvent event) {
		if (!event.isCancelled()) return;
		for (Player p : event.getGame().getRemainingPlayers()) {
			playerStopped(event.getGame(), p);
		}
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onGameStart(GameStartEvent event) {
		if (!event.isCancelled()) return;
		for (Player p : event.getGame().getRemainingPlayers()) {
			playerStarted(p);
		}
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		if (!event.isCancelled()) return;
		if (event.getGame().getState() == GameState.RUNNING) playerStarted(event.getPlayer());
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onPlayerKill(PlayerKillEvent event) {
		if (!event.isCancelled()) return;
		if (event.getGame().getPlayerStat(event.getKilled()).getState() == PlayerState.DEAD) {
			playerStopped(event.getGame(), event.getKilled());
		}
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		if (!event.isCancelled()) return;
		playerStopped(event.getGame(), event.getPlayer());
	}
	
	private void playerStarted(Player p) {
		startTimes.put(p.getName(), new Date().getTime());
	}
	
	private void playerStopped(Game game, Player p) {
		Long l = startTimes.get(p.getName());
		if (l != null) {
			game.getPlayerStat(p).addTime(new Date().getTime() - l);
			startTimes.remove(p.getName());
		}
	}
}
