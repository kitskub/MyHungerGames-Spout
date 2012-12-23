package me.kitskub.myhungergames;

import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.utils.ChatUtils;
import me.kitskub.myhungergames.utils.GeneralUtils;

import org.spout.api.Spout;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;
import org.spout.api.scheduler.Task;
import org.spout.api.scheduler.TaskPriority;

public class GameCountdown implements Runnable {
	private final HungerGame game;
	private int countdown;
	private Task task;
	private CommandSource starter;
	private boolean isResuming;
	
	public GameCountdown(final HungerGame game, int num, boolean isResuming) {
		this.game = game;
		countdown = num;
		task = Spout.getScheduler().scheduleSyncRepeatingTask(HungerGames.getInstance(), this, 20L, 20L, TaskPriority.NORMAL);
		this.isResuming = isResuming;
		if(isResuming) {
			ChatUtils.broadcast(game, "Resuming %s in %s...",
					game.getName(), GeneralUtils.formatTime(countdown));
		}
		else {
			ChatUtils.broadcast(game, "Starting %s in %s...",
					game.getName(), GeneralUtils.formatTime(countdown));
		}

	}
	
	public GameCountdown(final HungerGame game, int num, boolean isResuming, Player starter) {
		this(game, num, isResuming);
		this.starter = starter;
	}

	public GameCountdown(final HungerGame game, int num) {
		this(game, num, false);
	}

	public GameCountdown(final HungerGame game, int num, Player starter) {
		this(game, num, false, starter);
	}
	
	public GameCountdown(final HungerGame game, boolean isResuming) {
		this(game, Defaults.Config.DEFAULT_TIME.getInt(game.getSetup()), isResuming);
	}
	
	public GameCountdown(final HungerGame game) {
		this(game, Defaults.Config.DEFAULT_TIME.getInt(game.getSetup()), false);
	}
	
	public void cancel() {
		Spout.getScheduler().cancelTask(task);
	}

	public int getTimeLeft() {
		return countdown;
	}

	public void run() {
		if (countdown <= 1) {
			cancel();
			game.setDoneCounting();
			if (isResuming) {
				game.resumeGame(starter, 0);
			}
			else {
				game.startGame(starter, 0);
			}
			return;
		}
		countdown--;
		ChatStyle color = ChatStyle.BRIGHT_GREEN;
		if(countdown <= 5) color = ChatStyle.GOLD;
		if(countdown <= 3) color = ChatStyle.RED;
		ChatUtils.broadcastRaw(game, color, "%s...", GeneralUtils.formatTime(countdown));
	}
	
	public void setStarter(CommandSource starter) {
		this.starter = starter;
	}

}
