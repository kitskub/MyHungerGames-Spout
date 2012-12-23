package me.kitskub.myhungergames.api.event;

import me.kitskub.myhungergames.api.Game;

import org.spout.api.event.Event;

public abstract class GameEvent extends Event {
	private final Game game;
	
	public GameEvent(final Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return game;
	}

}