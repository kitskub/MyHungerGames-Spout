package me.kitskub.myhungergames.api.event;

import me.kitskub.myhungergames.games.HungerGame;
import org.spout.api.entity.Player;
import org.spout.api.event.HandlerList;


// called when a player leaves a game
public class PlayerLeaveGameEvent extends GameEvent {
	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private Type type;
	
	public PlayerLeaveGameEvent(final HungerGame game, final Player player, Type type) {
		super(game);
		this.player = player;
		this.type = type;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Type getType() {
		return type;
	}

	public static enum Type {
		LEAVE,
		QUIT,
		KICK;
	}
}
