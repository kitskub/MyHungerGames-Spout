package me.kitskub.myhungergames.api.event;

import me.kitskub.myhungergames.games.HungerGame;
import org.spout.api.event.HandlerList;

// called when a Hunger Game saves
public class GameSaveEvent extends GameEvent {
	private static final HandlerList handlers = new HandlerList();
	
	public GameSaveEvent(final HungerGame game) {
		super(game);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
