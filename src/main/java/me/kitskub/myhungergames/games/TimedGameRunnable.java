package me.kitskub.myhungergames.games;

import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Logging;
import me.kitskub.myhungergames.api.Game;
import me.kitskub.myhungergames.api.event.GameEndEvent;
import me.kitskub.myhungergames.api.event.GamePauseEvent;
import me.kitskub.myhungergames.api.event.GameStartEvent;
import me.kitskub.myhungergames.utils.ChatUtils;
import me.kitskub.myhungergames.utils.EquatableWeakReference;

import java.util.WeakHashMap;

import org.spout.api.Spout;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.scheduler.TaskPriority;

public class TimedGameRunnable implements Runnable, Listener{
	private static WeakHashMap<EquatableWeakReference<Game>, TimedGameRunnable> runnables = new WeakHashMap<EquatableWeakReference<Game>, TimedGameRunnable>();
	private EquatableWeakReference<Game> game;
	private int taskId;
	private long timeLeft;
	
	private TimedGameRunnable setGame(Game game) {
		this.game = new EquatableWeakReference<Game>(game);
		return this;
	}

	@EventHandler(order = Order.MONITOR)
	public static void onGamePause(GamePauseEvent event) {
		if (event.isCancelled()) return;
		TimedGameRunnable get = runnables.get(new EquatableWeakReference<Game>(event.getGame()));
		if (get != null) {
			get.pause();
		}
	}

	@EventHandler(order = Order.MONITOR)
	public static void onGameStart(GameStartEvent event) {
		if (event.isCancelled()) return;
		if (!event.isResuming()) {
			new TimedGameRunnable().setGame(event.getGame()).start();
		}
		else {
			TimedGameRunnable get = runnables.get(new EquatableWeakReference<Game>(event.getGame()));
			if (get != null) {
				get.resume();
			}
		}
	}

	@EventHandler(order = Order.MONITOR)
	public static void onGameEnd(GameEndEvent event) {
		if (event.isCancelled()) return;
		TimedGameRunnable get = runnables.get(new EquatableWeakReference<Game>(event.getGame()));
		if (get != null) {
			get.stop();
		}
	}

	public void run() {
		if (game.get() == null) stop();
		game.get().stopGame(false);
		ChatUtils.broadcast(game.get(), "Game %s has ended because it ran out of time!", game.get().getName());
		stop();
	}
	
	private void resume() {
		runnables.put(game, this);
		if (timeLeft <= 0) {
			Spout.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), this, 5 * 20, TaskPriority.NORMAL);
			return;
		}
		Spout.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), this, timeLeft * 20, TaskPriority.NORMAL);
	}
	
	private void pause() {
		if (game.get() == null) stop();
		long startTime = game.get().getStartTimes().get(game.get().getStartTimes().size() - 1);
		long endTime = game.get().getEndTimes().get(game.get().getEndTimes().size() - 1);
		long elapsed = (endTime - startTime) / 1000;
		timeLeft -= elapsed;
		Spout.getScheduler().cancelTask(taskId);
	}
	
	private void stop() {
		Spout.getScheduler().cancelTask(taskId);
		runnables.put(game, null);
	}
	
	private void start() {
		if (game.get() == null) return;
		timeLeft = Config.MAX_GAME_DURATION.getInt(game.get().getSetup()) * 1000;
		if (timeLeft <= 0) return;
		runnables.put(game, this);
		Logging.debug("Scheduled TimedGameRunnable for "  + timeLeft * 20);
		Spout.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), this, timeLeft * 20, TaskPriority.NORMAL);
	}

}
