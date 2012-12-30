package me.kitskub.myhungergames.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import me.kitskub.myhungergames.stats.PlayerStat;
import me.kitskub.myhungergames.utils.Cuboid;

import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;
import org.spout.api.geo.World;
import org.spout.api.geo.discrete.Point;
import org.spout.api.geo.discrete.Transform;
import org.spout.vanilla.component.substance.material.chest.Chest;

public interface Game {
	public boolean isSpectating(Player player);

	public boolean stopGame(CommandSource notifier, boolean isFinished);
	
	public String stopGame(boolean isFinished);
	
	/**
	 * Starts the game with the specified number of ticks
	 * 
	 * @param player
	 * @param ticks
	 * @return true if game or countdown was successfully started
	 */
	public boolean startGame(CommandSource player, int ticks);
	
	/**
	 * Starts this game with the default time if immediate is true. Otherwise, starts the game immediately.
	 * 
	 * @param notifier who to notify
	 * @param immediate
	 * @return
	 */	
	public boolean startGame(CommandSource notifier, boolean immediate);

	/**
	 * Starts this game with the default time if immediate is true. Otherwise, starts the game immediately.
	 * 
	 * @param immediate
	 * @return
	 */	
	public boolean startGame(boolean immediate);
	
		
	/**
	 * Starts the game
	 * 
	 * @param ticks 
	 * @return Null if game or countdown was successfully started. Otherwise, error message.
	 */
	public String startGame(int ticks);

	
 	public boolean resumeGame(CommandSource notifier, int ticks);	
	
	public boolean resumeGame(CommandSource notifier, boolean immediate);
	
	public boolean resumeGame(boolean immediate);
	
	/**
	 * Resumes the game
	 * 
	 * @param ticks 
	 * @return Null if game or countdown was not successfully started. Otherwise, error message.
	 */
	public String resumeGame(int ticks);
	
	public boolean pauseGame(CommandSource notifier);
	
	/**
	 * 
	 * @return null if successful, message if not
	 */
	public String pauseGame();
	
	public void addAndFillChest(Chest chest);
        
	public void fillInventories();
	
	/**
	 * Only used for players that have left the game, but not quitted. Only valid while game is running
	 * 
	 * @param player
	 * @return true if successful
	 */
	public boolean rejoin(Player player);

	public boolean join(Player player);
	
	public boolean leave(Player player, boolean callEvent);
	
	public boolean quit(Player player, boolean callEvent);
	
	/**
	 * Will be canceled if player is playing and teleporting is not allowed which should not ever happen
	 * @param player
	 */
	public void teleportPlayerToSpawn(Player player);
	
	/**
	 * 
	 * @param notifyOfRemaining
	 * @return true if is over, false if not
	 */
	public boolean checkForGameOver(boolean notifyOfRemaining);
	
	public String getInfo();
	
	/**
	 * Checks if players are in the game and have lives, regardless is game is running and if they are playing.
	 * @param players players to check
	 * @return
	 */
	public boolean contains(Player... players);
	
	/**
	 * 
	 * @param players players to check
	 * @return true if players are in the game, have lives, and are playing
	 */
	public boolean isPlaying(Player... players);

	/**
	 * Gets the players that have lives and are playing
	 * If game is not yet started remaining players are those that are waiting
	 * 
	 * @return the remaining players that have lives and are playing
	 */
	public List<Player> getRemainingPlayers();
	
	public PlayerStat getPlayerStat(Player player);
	
	public void listStats(CommandSource notifier);
	
	public String getName();

	public boolean addChest(Point loc, float weight);

	public boolean addFixedChest(Point loc, String fixedChest);

	public boolean addSpawnPoint(Transform loc);

	/**
	 * Removes chest from fixedChests and adds it to chests
	 * @param loc
	 * @return
	 */
	public boolean removeFixedChest(Point loc);

	public boolean removeChest(Point loc);

	public boolean removeSpawnPoint(Point loc);

	public void setEnabled(boolean flag);
	
	public void setSpawn(Transform newSpawn);

	public List<Player> getAllPlayers();

	public List<PlayerStat> getStats();
	
	public Transform getSpawn();

	public String getSetup();

	public List<String> getItemSets();

	public void addItemSet(String name);

	public void removeItemSet(String name);
		
	public void addWorld(World world);

	public void addCuboid(Point one, Point two);

	public Map<Player, List<Player>> getSponsors();
	
	public Set<World> getWorlds();
	
	public Set<Cuboid> getCuboids();
	
	public void removeItemsOnGround();
	
	public int getSize();

	public void playCannonBoom();

	public List<Long> getEndTimes();

	public long getInitialStartTime();

	public List<Long> getStartTimes();
	
	public GameState getState();
	
	public enum GameState {
		DISABLED,
		DELETED,
		STOPPED,
		RUNNING,
		PAUSED,
		COUNTING_FOR_START,
		COUNTING_FOR_RESUME,
		ABOUT_TO_START;
		
	}
}
