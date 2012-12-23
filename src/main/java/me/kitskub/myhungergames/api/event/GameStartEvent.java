package me.kitskub.myhungergames.api.event;

import me.kitskub.myhungergames.games.HungerGame;
import org.spout.api.event.Cancellable;
import org.spout.api.event.HandlerList;

// called when a Hunger Game starts
public class GameStartEvent extends GameEvent implements Cancellable  {
	private static final HandlerList handlers = new HandlerList();
	private final boolean isResuming;
	private boolean cancelled;
	
	public GameStartEvent(final HungerGame game, final boolean isResuming) {
		super(game);
		cancelled = false;
		this.isResuming = isResuming;
	}
	
	public GameStartEvent(final HungerGame game) {
		this(game, false);
	}
	
	public boolean isResuming() {
		return isResuming;
	}

	@Override
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
