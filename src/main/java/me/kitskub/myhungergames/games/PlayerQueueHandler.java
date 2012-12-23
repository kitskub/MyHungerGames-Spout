package me.kitskub.myhungergames.games;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.api.Game;
import me.kitskub.myhungergames.api.event.GameEndEvent;
import me.kitskub.myhungergames.api.Game.GameState;
import me.kitskub.myhungergames.utils.ChatUtils;
import me.kitskub.myhungergames.utils.EquatableWeakReference;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import org.spout.api.Spout;
import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.scheduler.TaskPriority;

public class PlayerQueueHandler implements Listener, Runnable {
	private static final Queue<String> queuedPlayers = new LinkedList<String>();
	private static final Queue<WeakReference<Game>> queuedGames = new LinkedList<WeakReference<Game>>();
	private static boolean enabled = false;

	public PlayerQueueHandler() {
		enabled = true;
	}

	public static void addPlayer(Player player) {
		if (!enabled) return;
		if (!HungerGames.hasPermission(player, Defaults.Perm.USER_AUTO_JOIN_ALLOWED)) return;
		queuedPlayers.offer(player.getName());
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onGameEnd(GameEndEvent event) {
		if (event.isCancelled()) return;
		if (!enabled) return;
		if (!Config.AUTO_JOIN_ALLOWED.getBoolean(event.getGame().getSetup())) return;
		Spout.getScheduler().scheduleAsyncDelayedTask(HungerGames.getInstance(), this, 20 * 10, TaskPriority.NORMAL);
		queuedGames.offer(new EquatableWeakReference<Game>(event.getGame()));
	}
	
	public void run() {
		WeakReference<Game> game = null;
		while ((game = queuedGames.poll()) != null && game.get() == null) {}
		if (game.get().getState() != GameState.STOPPED) return;
		for (int i = 0; i < Math.min(game.get().getSize(), queuedPlayers.size()); i++) {
			Player p = Spout.getEngine().getPlayer(queuedPlayers.poll(), false);
			if (p == null) continue;
			ChatUtils.send(p, "You have been selected to join the game %s", game.get().getName());
			game.get().join(p);
		}
	}

}
