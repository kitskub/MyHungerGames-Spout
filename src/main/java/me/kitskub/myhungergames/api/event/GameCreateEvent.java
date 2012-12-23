package me.kitskub.myhungergames.api.event;

import me.kitskub.myhungergames.games.HungerGame;
import org.spout.api.event.Cancellable;
import org.spout.api.event.HandlerList;

public class GameCreateEvent extends GameEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;

	public GameCreateEvent(final HungerGame game) {
		super(game);
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		cancelled = isCancelled;
	}
	
}
