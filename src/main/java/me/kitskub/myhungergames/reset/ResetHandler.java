package me.kitskub.myhungergames.reset;

import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.games.HungerGame;

public class ResetHandler {
    
    public enum Resetters {
	    INTERNAL;
    }

    private static Resetter resetter;
    
    
    public static void setRessetter(Resetters r) {
	    switch (r) {
		    default:
			    resetter = new InternalResetter();
	    }	
	    resetter.init();
    }
    
    public static void gameStarting(HungerGame game) {
	    resetter.beginGame(game);
    }
    
    private static boolean resetBlockChanges(HungerGame game) {
	    if (!Config.RESET_CHANGES.getBoolean(game.getSetup())) return true;
	    return resetter.resetChanges(game);
    }
    
    public static boolean resetChanges(HungerGame game) {
	return resetBlockChanges(game);
    }
}
