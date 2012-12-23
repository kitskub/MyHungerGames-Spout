package me.kitskub.myhungergames.reset;

import me.kitskub.myhungergames.games.HungerGame;

public abstract class Resetter {
	public abstract void init();
	
	public abstract boolean resetChanges(HungerGame game);
	
	public abstract void beginGame(HungerGame game);
}
