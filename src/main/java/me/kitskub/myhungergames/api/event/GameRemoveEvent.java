package me.kitskub.myhungergames.api.event;

import me.kitskub.myhungergames.games.HungerGame;
import org.spout.api.event.HandlerList;

public class GameRemoveEvent extends GameEvent {
	private static final HandlerList handlers = new HandlerList();

	public GameRemoveEvent(final HungerGame game) {
		super(game);
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
