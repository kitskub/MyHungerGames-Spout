package me.kitskub.myhungergames.games;

import me.kitskub.myhungergames.*;
import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.api.Game;
import static me.kitskub.myhungergames.api.Game.GameState.*;
import static me.kitskub.myhungergames.stats.PlayerStat.PlayerState;
import me.kitskub.myhungergames.reset.ResetHandler;
import me.kitskub.myhungergames.stats.PlayerStat;
import me.kitskub.myhungergames.api.event.*;
import me.kitskub.myhungergames.listeners.TeleportListener;
import me.kitskub.myhungergames.stats.PlayerStat.Team;
import me.kitskub.myhungergames.stats.StatHandler;
import me.kitskub.myhungergames.utils.ChatUtils;
import me.kitskub.myhungergames.utils.Cuboid;
import me.kitskub.myhungergames.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Set;

import org.spout.api.Spout;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Entity;
import org.spout.api.entity.Player;
import org.spout.api.event.cause.PluginCause;
import org.spout.api.geo.World;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.geo.discrete.Point;
import org.spout.api.geo.discrete.Transform;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.block.BlockFace;
import org.spout.api.material.block.BlockFaces;
import org.spout.api.scheduler.Task;
import org.spout.api.scheduler.TaskPriority;
import org.spout.api.util.config.ConfigurationNode;
import org.spout.vanilla.component.inventory.PlayerInventory;
import org.spout.vanilla.component.living.neutral.Human;
import org.spout.vanilla.component.misc.HungerComponent;
import org.spout.vanilla.component.substance.Item;
import org.spout.vanilla.component.substance.material.chest.Chest;
import org.spout.vanilla.component.world.VanillaSky;
import org.spout.vanilla.data.GameMode;
import org.spout.vanilla.event.cause.HealthChangeCause;
import org.spout.vanilla.event.player.PlayerDeathEvent;
import org.spout.vanilla.util.explosion.ExplosionModelSpherical;

	
public class HungerGame implements Comparable<HungerGame>, Runnable, Game {
	// Per game
	private final Map<String, PlayerStat> stats;
	private final Map<String, Transform> spawnsTaken;
	private final List<Transform> randomLocs;
	private final Map<String, List<String>> sponsors; // Just a list for info, <sponsor, sponsee>
	private final SpectatorSponsoringRunnable spectatorSponsoringRunnable;
	private final List<Long> startTimes;
	private final List<Long> endTimes;
	private final List<Team> teams;
	private long initialStartTime;

	// Persistent
	private final Map<Point, Float> chests;
	private final Map<Point, String> fixedChests;
	private final List<Point> blacklistedChests;
	private final List<Transform> spawnPoints;
	private final String name;
	private String setup;
	private final List<String> itemsets;
	private final Set<String> worlds;
	private final Set<Cuboid> cuboids;
	private Transform spawn;
	private GameState state;

	
	// Temporary
	private final Map<String, Transform> playerLocs;// For pausing
	private final Map<String, Transform> spectators;
	private final Map<String, Boolean> spectatorFlying; // If a spectator was flying
	private final Map<String, Boolean> spectatorFlightAllowed; // If a spectator's flight was allowed
	private final Map<String, GameMode> playerGameModes; // Whether a player was in survival when game started
	private final List<String> playersFlying; // Players that were flying when they joined
	private final List<String> playersCanFly; // Players that could fly when they joined
	private final List<String> readyToPlay;
	private GameCountdown countdown;
	private Task locTask;

	public HungerGame(String name) {
		this(name, null);
	}

	public HungerGame(final String name, final String setup) {
		stats = new TreeMap<String, PlayerStat>();
		spawnsTaken = new HashMap<String, Transform>();
		sponsors = new HashMap<String, List<String>>();
		spectatorSponsoringRunnable = new SpectatorSponsoringRunnable(this);
		randomLocs = new ArrayList<Transform>();
		startTimes = new ArrayList<Long>();
		endTimes = new ArrayList<Long>();
		teams = new ArrayList<Team>();
		initialStartTime = 0;
		
		chests = new HashMap<Point, Float>();
		fixedChests = new HashMap<Point, String>();
		blacklistedChests = new ArrayList<Point>();
		spawnPoints = new ArrayList<Transform>();
		this.name = name;
		this.setup = null;
		itemsets = new ArrayList<String>();
		worlds = new HashSet<String>();
		cuboids = new HashSet<Cuboid>();
		spawn = null;
		state = GameState.STOPPED;

		readyToPlay = new ArrayList<String>();
		playerLocs = new HashMap<String, Transform>();
		spectators = new HashMap<String, Transform>();
		spectatorFlying = new HashMap<String, Boolean>();
		spectatorFlightAllowed = new HashMap<String, Boolean>();
		playerGameModes = new HashMap<String, GameMode>();
		playersFlying = new ArrayList<String>();
		playersCanFly = new ArrayList<String>();
		countdown = null;
	}

	public void loadFrom(ConfigurationNode section) {
		spawnPoints.clear();
		chests.clear();
		fixedChests.clear();
		itemsets.clear();
		worlds.clear();
		cuboids.clear();
		if (section.getNode("spawn-points").getValue() != null) {
			ConfigurationNode spawnPointsSection = section.getNode("spawn-points");
			for (String key : spawnPointsSection.getKeys(false)) {
				String str = spawnPointsSection.getString(key);
				Transform loc = null;
				try {
					loc = GeneralUtils.parseToTransform(str);
				} catch (WorldNotFoundException ex) {
					Logging.warning(ex.getMessage());
					continue;
				} catch (NumberFormatException e) {
					Logging.debug(e.getMessage());
					continue;
				}
				spawnPoints.add(loc);
			}

		}

		if (section.getNode("chests").getValue() != null) {
			ConfigurationNode chestsSection = section.getNode("chests");
			for (String key : chestsSection.getKeys(false)) {
				String[] parts = chestsSection.getString(key).split(",");
				Point loc = null;
				float weight = 1f;
				try {
					loc = GeneralUtils.parseToPoint(parts[0]);
					weight = Float.parseFloat(parts[1]);
				} catch (WorldNotFoundException ex) {
					Logging.warning(ex.getMessage());
					continue;
				} catch (NumberFormatException e) {
					Logging.debug(e.getMessage());
					continue;
				}
				if (!(loc.getBlock().getComponent() instanceof Chest)) {
					Logging.warning("'%s' is no longer a chest.", parts[0]);
					continue;
				}
				chests.put(loc, weight);
			}

		}

		if (section.getNode("blacklistedchests").getValue() != null) {
			ConfigurationNode chestsSection = section.getNode("blacklistedchests");
			for (String key : chestsSection.getKeys(false)) {
				Point loc;
				try {
					loc = GeneralUtils.parseToPoint(chestsSection.getString(key));
				} catch (WorldNotFoundException ex) {
					Logging.warning(ex.getMessage());
					continue;
				} catch (NumberFormatException e) {
					Logging.debug(e.getMessage());
					continue;
				}
				if (!(loc.getBlock().getComponent() instanceof Chest)) {
					Logging.warning("'%s' is no longer a chest.", chestsSection.getString(key));
					continue;
				}
				blacklistedChests.add(loc);
			}

		}

		if (section.getNode("fixedchests").getValue() != null) {
			ConfigurationNode fixedChestsSection = section.getNode("fixedchests");
			for (String key : fixedChestsSection.getKeys(false)) {
				String str = fixedChestsSection.getString(key);
				String[] split = str.split(",");
				if (split.length != 2) continue;
				Point loc = null;
				try {
					loc = GeneralUtils.parseToPoint(split[0]);
				} catch (WorldNotFoundException ex) {
					Logging.warning(ex.getMessage());
					continue;
				} catch (NumberFormatException e) {
					Logging.debug(e.getMessage());
					continue;
				}
				if (!(loc.getBlock().getComponent() instanceof Chest)) {
					Logging.warning("'%s' is no longer a chest.", str);
					continue;
				}
				fixedChests.put(loc, split[1]);
			}

		}
                
                if(section.getNode("itemsets").getStringList(null) != null) {
			itemsets.addAll(section.getNode("itemsets").getStringList());
                }
		
                if(section.getNode("worlds").getStringList(null) != null) {
			worlds.addAll(section.getNode("worlds").getStringList());
                }
		if (section.getNode("cuboids").getStringList(null) != null) {
			List<Cuboid> cuboidList = new ArrayList<Cuboid>();
			for (String s : section.getNode("cuboids").getStringList()) {
				cuboidList.add(Cuboid.parseFromString(s));
			}
			cuboids.addAll(cuboidList);
		}
		setEnabled(section.getNode("enabled").getBoolean(true));
		if (section.getNode("setup").getValue() != null) setup = section.getString("setup");
		try {
			if (section.getNode("spawn").getValue() != null) spawn = GeneralUtils.parseToTransform(section.getString("spawn"));
		} catch (WorldNotFoundException ex) {
			Logging.warning(ex.getMessage());
		} catch (NumberFormatException e) {
			Logging.debug(e.getMessage());
		}
		 Spout.getEventManager().callEvent(new GameLoadEvent(this));
	}

	public void saveTo(ConfigurationNode section) {
		ConfigurationNode spawnPointsSection = section.addNode("spawn-points");
		ConfigurationNode chestsSection = section.addNode("chests");
		ConfigurationNode blacklistedchestsSection = section.addNode("blacklistedchests");
		ConfigurationNode fixedChestsSection = section.addNode("fixedchests");
		int cntr;
		
		for (cntr = 0; cntr < spawnPoints.size(); cntr++) {
			Transform loc = spawnPoints.get(cntr);
			if (loc == null) continue;
			String parsed = GeneralUtils.parseToString(loc);
			//Logging.debug("Saving a spawnpoint. It's Point is: " + loc + "\n" + "Parsed as: " + parsed);
			spawnPointsSection.getNode("spawnpoint" + (cntr + 1)).setValue(parsed);
		}
		cntr = 1;
		for (Point loc : chests.keySet()) {
			chestsSection.getNode("chest" + cntr).setValue(GeneralUtils.parseToString(loc) + "," + chests.get(loc));
			cntr++;
		}
		cntr = 1;
		for (Point loc : blacklistedChests) {
			blacklistedchestsSection.getNode("chest" + cntr).setValue(GeneralUtils.parseToString(loc));
			cntr++;
		}
		
		cntr = 1;
		for (Point loc : fixedChests.keySet()) {
			fixedChestsSection.getNode("fixedchest" + cntr).setValue(GeneralUtils.parseToString(loc) + "," + fixedChests.get(loc));
			cntr++;
		}
		section.getNode("itemsets").setValue(itemsets);
		if (!worlds.isEmpty()) {
			section.getNode("worlds").setValue(new ArrayList<String>(worlds));
		}
		List<String> cuboidStringList = new ArrayList<String>();
		for (Cuboid c : cuboids) {
			cuboidStringList.add(c.parseToString());
		}
		if (!cuboidStringList.isEmpty()) {
			section.getNode("cuboids").setValue(cuboidStringList);
		}
		section.getNode("enabled").setValue(state != DISABLED);
		section.getNode("setup").setValue(setup);
		section.getNode("spawn").setValue(GeneralUtils.parseToString(spawn));
		
		 Spout.getEventManager().callEvent(new GameSaveEvent(this));
	}

	public void run() {
		if (state != RUNNING) return;
		Random rand = HungerGames.RANDOM;
		int size = getRemainingPlayers().size();
		if (size < 0) {
			Logging.debug("HungerGame.run(): Unexpected size:" + size);
			return;
		}
		Transform loc = getRemainingPlayers().get(rand.nextInt(size)).getTransform().getTransform();
		if (randomLocs.size() >= 15) randomLocs.remove(rand.nextInt(15));
		randomLocs.add(loc);
	}
	
	public int compareTo(HungerGame game) {
		return game.name.compareToIgnoreCase(name);
	}

	public boolean addReadyPlayer(Player player) {
		if (state == DELETED) {
			ChatUtils.error(player, "That game does not exist anymore.");
			return false;
		}
		if (readyToPlay.contains(player.getName())) {
			ChatUtils.error(player, "You have already cast your vote that you are ready to play.");
			return false;
		}
		if (state == COUNTING_FOR_RESUME || state == COUNTING_FOR_START) {
			ChatUtils.error(player, Lang.getAlreadyCountingDown(setup).replace("<game>", name));
			return false;
		}
		if (state == RUNNING) {
			ChatUtils.error(player, Lang.getRunning(setup).replace("<game>", name));
			return false;
		}
		if(state == PAUSED) {
			ChatUtils.error(player, "%s has been paused.", name);
			return false;
		}
		readyToPlay.add(player.getName());
		String mess = Lang.getVoteMessage(setup).replace("<player>", player.getName()).replace("<game>", this.name);
		ChatUtils.broadcast(this, mess);
		int minVote = Config.MIN_VOTE.getInt(setup);
		int minPlayers = Config.MIN_PLAYERS.getInt(setup);
		int startTimer = Config.START_TIMER.getInt(setup);
		int ready = readyToPlay.size();
		int joined = stats.size();
		boolean allVote = Config.ALL_VOTE.getBoolean(setup);
		boolean autoVote = Config.AUTO_VOTE.getBoolean(setup);
		if (joined >= minPlayers) {
			if ((ready >= minVote && !allVote) || (ready >= joined && allVote && !autoVote)) {
				ChatUtils.broadcast(this, "Enough players have voted that they are ready. Starting game...", this.name);
				startGame(false);
			}
			else if (startTimer > 0) {
				ChatUtils.broadcast(this, "The minimum amount of players for this game has been reached. Countdown has begun...", this.name);
				startGame(startTimer);
			}
		}
		return true;
	}
	
	public void clearWaitingPlayers() {
		for (Iterator<String> it = stats.keySet().iterator(); it.hasNext();) {
			String stat = it.next();
			if (!stats.get(stat).getState().equals(PlayerState.WAITING)) continue;
			stats.get(stat).setState(PlayerState.NOT_IN_GAME);
			Player player = Spout.getEngine().getPlayer(stat, false);
			ItemStack[] contents = (ItemStack[]) player.get(PlayerInventory.class).getMain().toArray();
			List<ItemStack> list = new ArrayList<ItemStack>();
			for (ItemStack i : contents) {
				if (i != null) list.add(i);
			}
			contents = list.toArray(new ItemStack[list.size()]);
			playerLeaving(player, false);
			for (ItemStack i : contents) Item.dropNaturally(player.getTransform().getPosition(), i);
			GameManager.INSTANCE.clearGamesForPlayer(stat, this);
			stats.remove(stat);
		}
	}

	public boolean addSpectator(Player player, Player spectated) {
		if (GameManager.INSTANCE.getSpectating(player) != null) {
			ChatUtils.error(player, "You cannot spectate while in a game.");
			return false;
		}
		if (state != RUNNING) {
			ChatUtils.error(player, Lang.getNotRunning(setup).replace("<game>", name));
			return false;
		}
		spectators.put(player.getName(), player.getTransform().getTransform());
		if (Config.SPECTATOR_SPONSOR_PERIOD.getInt(setup) != 0) {
			 spectatorSponsoringRunnable.addSpectator(player);
		}
		Random rand = HungerGames.RANDOM;
		Transform loc = randomLocs.get(rand.nextInt(randomLocs.size()));
		if (spectated != null) loc = spectated.getTransform().getTransform();
		player.teleport(loc);
		spectatorFlying.put(player.getName(), player.get(Human.class).isFlying());
		spectatorFlightAllowed.put(player.getName(), player.get(Human.class).canFly());
		player.get(Human.class).setCanFly(true);
		player.get(Human.class).setFlying(true);
		for (Player p : getRemainingPlayers()) {
			p.setVisible(player, false);
		}
		ChatUtils.send(player, "You are now spectating %s", name);
		return true;
	}

	@Override
	public boolean isSpectating(Player player) {
		return spectators.containsKey(player.getName());
	}

	public void removeSpectator(Player player) {
		if (!spectators.containsKey(player.getName())) {
			ChatUtils.error(player, "You are not spectating that game.");
			return;
		}
		spectatorSponsoringRunnable.removeSpectator(player);
		player.get(Human.class).setFlying(spectatorFlying.get(player.getName()));
		player.get(Human.class).setCanFly(spectatorFlightAllowed.get(player.getName()));
		player.teleport(spectators.remove(player.getName()));
		for (Player p : getRemainingPlayers()) {
			p.setVisible(player, true);
		}
	}
	
	@Override
	public boolean stopGame(CommandSource cs, boolean isFinished) {
		String result = stopGame(isFinished);
		if (result != null && cs != null) {
			ChatUtils.error(cs, result);
			return false;
		}
		return true;
	}
	
	@Override
	public String stopGame(boolean isFinished) {
		if (state == DELETED) return "That game does not exist anymore.";
		clearWaitingPlayers();
		if (state != RUNNING && state != PAUSED && state != COUNTING_FOR_RESUME && state != COUNTING_FOR_START) return "Game is not started";
		
		endTimes.add(System.currentTimeMillis());
		if (countdown != null) countdown.cancel();
		if (state == PAUSED) { // Needed for inventory stuff
			for(String playerName : playerLocs.keySet()) {
				Player p = Spout.getEngine().getPlayer(playerName, false);
				if (p == null) continue;
				playerEntering(p, true);
				InventorySave.loadGameInventory(p);
			}
		}
		StatHandler.updateGame(this);
		for (Player player : getRemainingPlayers()) {
			stats.get(player.getName()).setState(PlayerState.NOT_IN_GAME);
			ItemStack[] contents = (ItemStack[]) player.get(PlayerInventory.class).getMain().toArray();
			List<ItemStack> list = new ArrayList<ItemStack>();
			for (ItemStack i : contents) {
				if (i != null) list.add(i); // Remove all null elements
			}
			contents = list.toArray(new ItemStack[list.size()]);
			playerLeaving(player, false);
			if (isFinished && Config.WINNER_KEEPS_ITEMS.getBoolean(setup)) {
				player.get(PlayerInventory.class).getMain().addAll(Arrays.asList(contents));
			}
			else {
				for (ItemStack i : contents) Item.dropNaturally(player.getTransform().getPosition(), i);
			}
			teleportPlayerToSpawn(player);
			if (isFinished) GeneralUtils.rewardPlayer(player);
		}
		for (String stat : stats.keySet()) {
			StatHandler.updateStat(stats.get(stat));// TODO: this might be a little slow to do it this way. Thread?
			GameManager.INSTANCE.clearGamesForPlayer(stat, this);
		}
		stats.clear();
		for (String spectatorName : spectators.keySet()) {
			Player spectator = Spout.getEngine().getPlayer(spectatorName, false);
			if (spectator == null) continue;
			removeSpectator(spectator);
		}
		//spectatorSponsoringRunnable.cancel();//TODO add back in sponsoring
		Spout.getScheduler().cancelTask(locTask);
		if (Config.REMOVE_ITEMS.getBoolean(setup)) removeItemsOnGround();
		state = STOPPED;
		if (!isFinished) {
			GameEndEvent event = new GameEndEvent(this, false);
			Spout.getEventManager().callEvent(event);
		}
		clear();
		ResetHandler.resetChanges(this);
		return null;
	}

	@Override
	public boolean startGame(CommandSource cs, int ticks) {
		String result = startGame(ticks);
		if (countdown != null) countdown.setStarter(cs);
		if (result != null) {
			ChatUtils.error(cs, result);
			return false;
		}
		return true;
	}

	@Override
	public boolean startGame(CommandSource cs, boolean immediate) {
		if(!immediate) return startGame(cs, Config.DEFAULT_TIME.getInt(setup));
		return startGame(cs, 0);
	}

	@Override
	public boolean startGame(boolean immediate) {
		if(!immediate) return startGame(Config.DEFAULT_TIME.getInt(setup)) == null;
		return startGame(0) == null;
	}

	@Override
	public String startGame(int ticks) {
		if (state == DELETED) return "Game no longer exists.";
		if (state == DISABLED) return Lang.getNotEnabled(setup).replace("<game>", name);
		if (state == RUNNING) return Lang.getRunning(setup).replace("<game>", name);
		if (stats.size() < Config.MIN_PLAYERS.getInt(setup)) return String.format("There are not enough players in %s", name);
		if (countdown != null) {
			if (ticks < countdown.getTimeLeft()) {
				countdown.cancel();
				countdown = null;
			}
			else {
				return Lang.getAlreadyCountingDown(setup).replace("<game>", name);
			}
		}
		if (ticks > 0) {
			countdown = new GameCountdown(this, ticks);
			state = COUNTING_FOR_START;
			return null;
		}
		GameStartEvent event = new GameStartEvent(this);
		Spout.getEventManager().callEvent(event);
		if (event.isCancelled()) {
			return "Start was cancelled.";
		}
		if (stats.size() < 2) ChatUtils.broadcast(this, "%s is being started with only one player. This has a high potential to lead to errors.", name);
		initialStartTime = System.currentTimeMillis();
		startTimes.add(System.currentTimeMillis());
		locTask = Spout.getScheduler().scheduleSyncRepeatingTask(HungerGames.getInstance(), this, 20 * 120, 20 * 10, TaskPriority.NORMAL);
		//spectatorSponsoringRunnable.setTask(Spout.getScheduler().scheduleSyncRepeatingTask(HungerGames.getInstance(), spectatorSponsoringRunnable, 0, SpectatorSponsoringRunnable.pollEveryInTicks, TaskPriority.NORMAL));//TODO add back in sponsoring
		ResetHandler.gameStarting(this);
		releasePlayers();
		fillInventories();
		for (String playerName : stats.keySet()) {
			Player p = Spout.getEngine().getPlayer(playerName, false);
			if (p == null) continue;
			World world = p.getWorld();
			world.getComponentHolder().get(VanillaSky.class).setTime(0L);
			p.get(HungerComponent.class).setHunger(20);
			stats.get(playerName).setState(PlayerStat.PlayerState.PLAYING);
		}
		state = RUNNING;
		run(); // Add at least one randomLoc
		readyToPlay.clear();
		ChatUtils.broadcast(this, "Starting %s. Go!!", name);
		return null;
	}

	@Override
 	public boolean resumeGame(CommandSource cs, int ticks) {		
		if (ticks <= 0) {
			String result = resumeGame(0);
			if (result != null) {
				ChatUtils.error(cs, result);
				return false;
			}
		} else {
			countdown = new GameCountdown(this, ticks, true);
			state = COUNTING_FOR_RESUME;
		}
		return true;
	}
	
	@Override
	public boolean resumeGame(CommandSource cs, boolean immediate) {
		if (!immediate) return resumeGame(cs, Config.DEFAULT_TIME.getInt(setup));
		return resumeGame(cs, 0);
	}
	
	@Override
	public boolean resumeGame(boolean immediate) {
		if (!immediate) return resumeGame(Config.DEFAULT_TIME.getInt(setup)) == null;
		return resumeGame(0) == null;
	}

	@Override
	public String resumeGame(int ticks) {
		if (state == DELETED) return "That game does not exist anymore.";
		if(state != PAUSED && state != ABOUT_TO_START) return "Cannot resume a game that has not been paused.";
		if (ticks > 0) {
			countdown = new GameCountdown(this, ticks, true);
			state = COUNTING_FOR_RESUME;
			return null;
		}
		startTimes.add(System.currentTimeMillis());
		GameStartEvent event = new GameStartEvent(this, true);
		Spout.getEventManager().callEvent(event);
		if (event.isCancelled()) {
			return "Start was cancelled.";
		}
		for(String playerName : playerLocs.keySet()) {
			Player p = Spout.getEngine().getPlayer(playerName, false);
			if (p == null) continue;
			stats.get(p.getName()).setState(PlayerState.PLAYING);
			playerEntering(p, true);
			InventorySave.loadGameInventory(p);
			World world = p.getWorld();
			world.getComponentHolder().get(VanillaSky.class).setTime(0L);
			p.get(HungerComponent.class).setHunger(20);
			stats.get(playerName).setState(PlayerStat.PlayerState.PLAYING);
		}
		state = RUNNING;
		countdown = null;
		ChatUtils.broadcast(this, "Resuming %s. Go!!", name);
		return null;
	}
	
	@Override
	public boolean pauseGame(CommandSource cs) {
		String result = pauseGame();
		if (result != null) {
			ChatUtils.error(cs, "Cannot pause a game that has been paused.");
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @return null if successful, message if not
	 */
	@Override
	public String pauseGame() {
		if (state == DELETED) return "That game does not exist anymore.";
		if(state == PAUSED) return "Cannot pause a game that has been paused.";
		
		state = PAUSED;
		endTimes.add(System.currentTimeMillis());
		if(countdown != null) {
			countdown.cancel();
			countdown = null;
		}
		for(Player p : getRemainingPlayers()) {
			if (p == null) continue;
			stats.get(p.getName()).setState(PlayerState.GAME_PAUSED);
			playerLocs.put(p.getName(), p.getTransform().getTransform());
			InventorySave.saveAndClearGameInventory(p);
			playerLeaving(p, true);
			teleportPlayerToSpawn(p);
		}
		for (String spectatorName : spectators.keySet()) {
			Player spectator = Spout.getEngine().getPlayer(spectatorName, false);
			removeSpectator(spectator);
		}
		Spout.getEventManager().callEvent(new GamePauseEvent(this));
		return null;
	}
	
	private void releasePlayers() {
		for (String playerName : stats.keySet()) {
			Player p = Spout.getEngine().getPlayer(playerName, false);
			if (p == null) continue;
			GameManager.INSTANCE.unfreezePlayer(p);
		}

	}

	@Override
	public void addAndFillChest(Chest chest) {
		if (fixedChests.containsKey(chest.getBlock().getPosition())) return;
		if(!chests.keySet().contains(chest.getBlock().getPosition()) && !blacklistedChests.contains(chest.getBlock().getPosition())) {
			//Logging.debug("Inventory Point was not in randomInvs.");
			GeneralUtils.fillChest(chest, 0, itemsets);
			addChest(chest.getBlock().getPosition(), 1f);
		}
	}
        
	@Override
	public void fillInventories() {
	    Point prev = null;
	    // Logging.debug("Filling inventories. Chests size: %s fixedChests size: %s", chests.size(), fixedChests.size());
	    for (Point loc : chests.keySet()) {
		    if (prev != null) {
			for (BlockFace f : BlockFaces.NESW) {
						if (prev.getBlock().translate(f).getPosition().equals(loc)) {
							//Logging.debug("Cancelling a fill because previous was a chest");
							continue;
						}
			}
		    }
		    if (!(loc.getBlock().getComponent() instanceof Chest)) {
			    //Logging.debug("Cancelling a fill because not a chest");
			    continue;
		    }
		    prev = loc;
		    Chest chest = (Chest) loc.getBlock().getComponent();
		    GeneralUtils.fillChest(chest, chests.get(loc), itemsets);
	    }
	    for (Point loc : fixedChests.keySet()) {
		    if (prev != null) {
			for (BlockFace f : BlockFaces.NESW) {
						if (prev.getBlock().translate(f).getPosition().equals(loc)) {
							//Logging.debug("Cancelling a fill because previous was a chest");
							continue;
						}
			}
		    }
		    if (!(loc.getBlock().getComponent() instanceof Chest)) {
			    //Logging.debug("Cancelling a fill because not a chest");
			    continue;
		    }
		    prev = loc;
		    Chest chest = (Chest) loc.getBlock().getComponent();
		    GeneralUtils.fillFixedChest(chest, fixedChests.get(loc));   
	    }

	}

	@Override
	public synchronized boolean rejoin(Player player) {
		if (state != RUNNING) {
			ChatUtils.error(player, Lang.getNotRunning(setup).replace("<game>", name));
			return false;
		}
		if(!playerEnteringPreCheck(player)) return false;
		if (!Config.ALLOW_REJOIN.getBoolean(setup)) {
			ChatUtils.error(player, "You are not allowed to rejoin a game.");
			return false;
		}
		if (stats.get(player.getName()).getState() == PlayerState.PLAYING){
			ChatUtils.error(player, "You can't rejoin a game while you are in it.");
			return false;
		}
		if (!stats.containsKey(player.getName()) || stats.get(player.getName()).getState() != PlayerState.NOT_PLAYING) {
			ChatUtils.error(player, Lang.getNotInGame(setup).replace("<game>", name));
			return false;
		}
		PlayerJoinGameEvent event = new PlayerJoinGameEvent(this, player, true);
		Spout.getEventManager().callEvent(event);
		if (event.isCancelled()) return false;
		if (!playerEntering(player, false)) return false;
		stats.get(player.getName()).setState(PlayerState.PLAYING);
		
		String mess = Lang.getRejoinMessage(setup);
		mess = mess.replace("<player>", player.getName()).replace("<game>", name);
		ChatUtils.broadcast(this, mess);
		return true;
	}

	@Override
	public synchronized boolean join(Player player) {
	    if (GameManager.INSTANCE.getSession(player) != null) {
		    ChatUtils.error(player, "You are already in a game. Leave that game before joining another.");
		    return false;
	    }
	    if (stats.containsKey(player.getName())) {
		    ChatUtils.error(player, Lang.getInGame(setup).replace("<game>", name));
		    return false;
	    }
	    if (!playerEnteringPreCheck(player)) return false;
	    if (state == RUNNING && !Config.ALLOW_JOIN_DURING_GAME.getBoolean(setup)) {
		    ChatUtils.error(player, Lang.getRunning(setup).replace("<game>", name));
		    return false;
	    }
	    if(state == PAUSED) {
		    ChatUtils.error(player, "%s has been paused.", name);
		    return false;
	    }
	    PlayerJoinGameEvent event = new PlayerJoinGameEvent(this, player);
	    Spout.getEventManager().callEvent(event);
	    if (event.isCancelled()) return false;
	    if(!playerEntering(player, false)) return false;
	    stats.put(player.getName(), GameManager.INSTANCE.createStat(this, player));
	    String mess = Lang.getJoinMessage(setup);
	    mess = mess.replace("<player>", player.getName()).replace("<game>", name);
	    ChatUtils.broadcast(this, mess);
	    if (state == RUNNING) {
		    stats.get(player.getName()).setState(PlayerState.PLAYING);
	    }
	    else {
		    stats.get(player.getName()).setState(PlayerState.WAITING);
		    if (Config.AUTO_VOTE.getBoolean(setup)) addReadyPlayer(player);
	    }
	    return true;
	}

	private synchronized boolean playerEnteringPreCheck(Player player) {
	    if (state == DELETED) {
		    ChatUtils.error(player, "That game does not exist anymore.");
		    return false;
	    }
	    if (state == DISABLED) {
		    ChatUtils.error(player, Lang.getNotEnabled(setup).replace("<game>", name));
		    return false;
	    }

	    if (spawnsTaken.size() >= spawnPoints.size()) {
		    ChatUtils.error(player, "%s is already full.", name);
		    return false;
	    }

	    if (Config.REQUIRE_INV_CLEAR.getBoolean(setup)) {
		    if(!GeneralUtils.hasInventoryBeenCleared(player)) {
			    ChatUtils.error(player, "You must clear your inventory first (Be sure you're not wearing armor either).");
			    return false;
		    }
	    }
	    return true;
	}

	/**
	 * When a player enters the game. Does not handle stats.
	 * This handles the teleporting.
	 * @param player
	 * @param fromTemporary if the player leaving was temporary. Leave is not temporary.
	 * @return
	 */
	 private synchronized boolean playerEntering(Player player, boolean fromTemporary) {
	    Transform loc;
	    if (!fromTemporary) {
		    loc = getNextOpenSpawnPoint();
		    spawnsTaken.put(player.getName(), loc);
	    }
	    else {
		    loc = spawnsTaken.get(player.getName());
	    }
	    GameManager.INSTANCE.addSubscribedPlayer(player, this);
	    GameManager.INSTANCE.addBackPoint(player);
	    TeleportListener.allowTeleport(player);
	    player.teleport(loc);
	    if (state != RUNNING && Config.FREEZE_PLAYERS.getBoolean(setup)) GameManager.INSTANCE.freezePlayer(player);
	    if (Config.FORCE_SURVIVAL.getBoolean(setup)) {
		    playerGameModes.put(player.getName(), player.get(Human.class).getGameMode());
		    player.get(Human.class).setGamemode(GameMode.SURVIVAL);
	    }
	    if (Config.DISABLE_FLY.getBoolean(setup)) {
		    if (!HungerGames.hasPermission(player, Perm.ADMIN_ALLOW_FLIGHT)) {
			    if (player.get(Human.class).canFly()) {
				playersCanFly.add(player.getName());
				player.get(Human.class).setCanFly(false);
			    }
			    if (player.get(Human.class).isFlying()) {
				    playersFlying.add(player.getName());
				    player.get(Human.class).setFlying(false);
			    }
			    
		    }	
	    }
	    if (Config.HIDE_PLAYERS.getBoolean(setup)) player.get(Human.class).setSneaking(true);
	    if (Config.CLEAR_INV.getBoolean(setup)) InventorySave.saveAndClearInventory(player);
	    for (String kit : ItemConfig.getKits()) {
		    if (HungerGames.hasPermission(player, Perm.USER_KIT) || player.hasPermission(Perm.USER_KIT.get() + "." + kit)) {
			    player.get(PlayerInventory.class).getMain().addAll(Arrays.asList((ItemStack[]) ItemConfig.getKit(kit).toArray()));
		    }
	    }
	    for (String string : spectators.keySet()) {
		    Player spectator = Spout.getEngine().getPlayer(string, false);
		    if (spectator == null) continue;
		    player.setVisible(spectator, false);
	    }
	    return true;
	}
	
	public Transform getNextOpenSpawnPoint() {
		Random rand = HungerGames.RANDOM;
		Transform loc;
		do {
			loc = spawnPoints.get(rand.nextInt(spawnPoints.size()));
			if (loc == null) spawnPoints.remove(loc);
			
		} while (loc == null || spawnsTaken.containsValue(loc));
		return loc;
	}
	
	@Override
	public synchronized boolean leave(Player player, boolean callEvent) {
		if (state != RUNNING && state != PAUSED) return quit(player, true);
		
		if (!isPlaying(player)) {
			ChatUtils.error(player, "You are not playing the game %s.", name);
			return false;
		}

		if (!Config.ALLOW_REJOIN.getBoolean(setup)) {
			stats.get(player.getName()).die();
		}
		else {
			stats.get(player.getName()).setState(PlayerState.NOT_PLAYING);
			stats.get(player.getName()).death(PlayerStat.NODODY);
		}
		if (callEvent) Spout.getEventManager().callEvent(new PlayerLeaveGameEvent(this, player, PlayerLeaveGameEvent.Type.LEAVE));
		if (state == PAUSED) playerEntering(player, true);
		InventorySave.loadGameInventory(player);
		dropInventory(player);
		playerLeaving(player, false);
		teleportPlayerToSpawn(player);
		String mess = Lang.getLeaveMessage(setup);
		mess = mess.replace("<player>", player.getName()).replace("<game>", name);
		ChatUtils.broadcast(this,mess);
		checkForGameOver(false);

		return true;
	}
	
	@Override
	public synchronized boolean quit(Player player, boolean callEvent) {
	    if (!contains(player)) {
		    ChatUtils.error(player, Lang.getNotInGame(setup).replace("<game>", name));
		    return false;
	    }
	    if (callEvent)  Spout.getEventManager().callEvent(new PlayerLeaveGameEvent(this, player, PlayerLeaveGameEvent.Type.QUIT));
	    boolean wasPlaying = stats.get(player.getName()).getState() == PlayerState.PLAYING || stats.get(player.getName()).getState() == PlayerState.WAITING;
	    if (wasPlaying) {
		    dropInventory(player);
	    }
	    if(state == RUNNING) {
		    stats.get(player.getName()).die();
	    }
	    else {
		    stats.remove(player.getName());
		    GameManager.INSTANCE.clearGamesForPlayer(player.getName(), this);
	    }
	    playerLeaving(player, false);
	    if (wasPlaying || state != RUNNING) {
		    teleportPlayerToSpawn(player);
	    }
	    
	    String mess = Lang.getQuitMessage(setup);
	    mess = mess.replace("<player>", player.getName()).replace("<game>", name);
	    ChatUtils.broadcast(this, mess);
	    checkForGameOver(false);
	    return true;
	}
	
	/**
	 * Used when a player is exiting.
	 * This does not handle teleporting and should be used before the teleport.
	 * @param player
	 */
	private synchronized void playerLeaving(Player player, boolean temporary) {
		for (String string : spectators.keySet()) {
		    Player spectator = Spout.getEngine().getPlayer(string, false);
		    if (spectator == null) continue;
		    player.setVisible(spectator, true);
		}
		GameManager.INSTANCE.unfreezePlayer(player);
		InventorySave.loadInventory(player);
		if (playerGameModes.containsKey(player.getName())) {
			player.get(Human.class).setGamemode(playerGameModes.remove(player.getName()));
		}
		if (Config.DISABLE_FLY.getBoolean(setup)) {
			if (!HungerGames.hasPermission(player, Perm.ADMIN_ALLOW_FLIGHT)) {
				player.get(Human.class).setCanFly(playersCanFly.remove(player.getName()));
				player.get(Human.class).setFlying(playersFlying.remove(player.getName()));
			}
		}
		if (Config.HIDE_PLAYERS.getBoolean(setup)) player.get(Human.class).setSneaking(false);
		readyToPlay.remove(player.getName());
		if (!temporary) {
			spawnsTaken.remove(player.getName());
			PlayerQueueHandler.addPlayer(player);
			GameManager.INSTANCE.removedSubscribedPlayer(player, this);
		}
	}

	// Complete clear just to be sure
	public void clear() {
		releasePlayers();
		stats.clear();
		spawnsTaken.clear();
		spectators.clear();
		sponsors.clear();
		randomLocs.clear();
		
		readyToPlay.clear();
		playerLocs.clear();
		spectatorFlying.clear();
		spectatorFlightAllowed.clear();
		playerGameModes.clear();
		playersCanFly.clear();
		playersFlying.clear();
		if (countdown != null) countdown.cancel(); 
		countdown = null;
	}

	@Override
	public void teleportPlayerToSpawn(Player player) {
		if (player == null) {
			return;
		}
		if (Config.USE_SPAWN.getBoolean(setup)) {
			if (spawn != null) {
				player.teleport(spawn);
				return;
			}
			else {
				ChatUtils.error(player, "There was no spawn set for %s. Teleporting to back Point.", name);
			}
		}
		Point loc = GameManager.INSTANCE.getAndRemoveBackPoint(player);
		if (loc != null) {
			player.teleport(loc);
		}
		else {
			ChatUtils.error(player, "For some reason, there was no back Point. Please contact an admin for help.", name);
			player.teleport(player.getWorld().getSpawnPoint());
		}
	}

	@Override
	public boolean checkForGameOver(boolean notifyOfRemaining) {// TODO config option
		if (state != RUNNING) return false;
		List<Player> remaining = getRemainingPlayers();
		List<Team> teamsLeft = new ArrayList<Team>();
		int left = 0;
		for (Player p : remaining) {
			Team team = stats.get(p.getName()).getTeam();
			if (team == null) {
				left++;
			}
			else if (!teamsLeft.contains(team)) {
				teamsLeft.add(team);
				left++;
			}
		}
		if (left < 2) {
			GameEndEvent event = null;
			if (teamsLeft.size() > 0) {
				ChatUtils.sendToTeam(teamsLeft.get(0), "Congratulations! Your team won!");
				event = new GameEndEvent(this, teamsLeft.get(0));
			}
			else {
				Player winner = null;
				if (!remaining.isEmpty()) {
					winner = remaining.get(0);
				}
				if (winner == null) {
					ChatUtils.broadcast(this, Lang.getNoWinner(setup));
					event = new GameEndEvent(this, true);
				} else {
					ChatUtils.broadcast(this, Lang.getWin(setup).replace("<player>", winner.getName()).replace("<game>", name));
					ChatUtils.send(winner, "Congratulations! You won!");// TODO message
					event = new GameEndEvent(this, winner);
				}
			}
			Spout.getEventManager().callEvent(event);
			stopGame(true);
			return true;
		}

		if (!notifyOfRemaining) return false;
		String mess = "Remaining players: ";
		for (int cntr = 0; cntr < remaining.size(); cntr++) {
			mess += remaining.get(cntr).getName();
			if (cntr < remaining.size() - 1) {
				mess += ", ";
			}

		}
		ChatUtils.broadcastRaw(this, ChatStyle.WHITE, mess);
		return false;
	}

	@Override
	public String getInfo() {
		return String.format("%s[%d/%d] Enabled: %b", name, spawnsTaken.size(), spawnPoints.size(), state != DISABLED);
	}

	@Override
	public boolean contains(Player... players) {
	    if (state == DELETED) return false;
	    for (Player player : players) {
		if (!stats.containsKey(player.getName())) return false;
		PlayerState pState = stats.get(player.getName()).getState();
		if (pState == PlayerState.NOT_IN_GAME || pState == PlayerState.DEAD) return false;
	    }
	    return true;
	}
	
	@Override
	public boolean isPlaying(Player... players) {
	    for (Player player : players) {
		if (state != RUNNING || !stats.containsKey(player.getName()) 
			|| stats.get(player.getName()).getState() != PlayerState.PLAYING ){
		    return false;
		}
	    }
	    return true;
	}

	
	public void killed(final Player killer, final Player killed, PlayerDeathEvent deathEvent) {
		if (state == DELETED || state != RUNNING || stats.get(killed.getName()).getState() != PlayerState.PLAYING) return;
		killed.get(Human.class).getHealth().setHealth(20, HealthChangeCause.PLUGIN);
		killed.get(HungerComponent.class).setHunger(20);
		PlayerStat killedStat = stats.get(killed.getName());
		PlayerKillEvent event;
		if (killer != null) {
			PlayerStat killerStat = stats.get(killer.getName());
			killerStat.kill(killed.getName());
			String message = Lang.getKillMessage(setup).replace("<killer>", killer.getName()).replace("<killed>", killed.getName()).replace("<game>", name);
			event = new PlayerKillEvent(this, killer, killed, message);
			ChatUtils.broadcast(this, message);
			killedStat.death(killer.getName());
		}
		else {
			event = new PlayerKillEvent(this, killed);
			killedStat.death(PlayerStat.NODODY);
		}
		Spout.getEventManager().callEvent(event);
		if (killedStat.getState() == PlayerState.DEAD) {
			for (ItemStack i : deathEvent.getDrops()) {
				Item.dropNaturally(killed.getTransform().getPosition(), i);
			}
			deathEvent.getDrops().clear();
			playerLeaving(killed, false);
			final ItemStack[] armor = (ItemStack[]) killed.get(PlayerInventory.class).getArmor().toArray();
			final ItemStack[] inventory = (ItemStack[]) killed.get(PlayerInventory.class).getMain().toArray();
			Spout.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), new Runnable() {
				@Override
				public void run() {
					killed.get(PlayerInventory.class).getArmor().addAll(Arrays.asList(armor));
					killed.get(PlayerInventory.class).getMain().addAll(Arrays.asList(inventory));
				}

			});
			teleportPlayerToSpawn(killed);
			checkForGameOver(false);
			int deathCannon = Config.DEATH_CANNON.getInt(setup);
			int deathMessages = Config.SHOW_DEATH_MESSAGES.getInt(setup);
			if (deathCannon == 1 || deathCannon == 2) playCannonBoom();
			if (deathMessages == 1 || deathMessages == 2) {
				List<String> messages = Lang.getDeathMessages(setup);
				ChatUtils.broadcast(this, messages.get((new Random()).nextInt(messages.size()))
					.replace("<killed>", killed.getDisplayName()
					.replace("<killer>", killer.getDisplayName())
					.replace("<game>", name)));
			}
		}
		else {
			if (Config.SPAWNPOINT_ON_DEATH.getBoolean(setup)) {
				Transform respawn = spawnsTaken.get(killed.getName());
				TeleportListener.allowTeleport(killed);
				killed.teleport(respawn);
			}
			else {
				Transform respawn = randomLocs.get(HungerGames.RANDOM.nextInt(randomLocs.size()));
				TeleportListener.allowTeleport(killed);
				killed.teleport(respawn);
			}
			if (Config.DEATH_CANNON.getInt(setup) == 1) playCannonBoom();
			if (Config.SHOW_DEATH_MESSAGES.getInt(setup) == 1) {
				List<String> deathMessages = Lang.getDeathMessages(setup);
				ChatUtils.broadcast(this, deathMessages.get((new Random()).nextInt(deathMessages.size()))
					.replace("<killed>", killed.getDisplayName()
					.replace("<killer>", killer.getDisplayName())
					.replace("<game>", name)));
			}
			ChatUtils.send(killed, "You have " + killedStat.getLivesLeft() + " lives left.");
		}
	}

	@Override
	public List<Player> getRemainingPlayers() {
	    List<Player> remaining = new ArrayList<Player>();
	    for (String playerName : stats.keySet()) {
		Player player = Spout.getEngine().getPlayer(playerName, false);
		if (player == null) continue;
		PlayerStat stat = stats.get(playerName);
		if (stat.getState() == PlayerState.PLAYING || stat.getState() == PlayerState.GAME_PAUSED || stat.getState() == PlayerState.WAITING) {
		    remaining.add(player);
		}
	    }
	    return remaining;
	}

	@Override
	public PlayerStat getPlayerStat(Player player) {
		return stats.get(player.getName());
	}

	@Override
	public void listStats(CommandSource cs) {
		int living = 0, dead = 0;
		List<String> players = new ArrayList<String>(stats.keySet());
		String mess = "";
		for (int cntr = 0; cntr < players.size(); cntr++) {
			PlayerStat stat = stats.get(players.get(cntr));
			Player p = stat.getPlayer();
			if (p == null) continue;
			String statName;
			if (stat.getState() == PlayerState.DEAD) {
				statName = ChatStyle.RED.toString() + p.getName() + ChatStyle.GRAY.toString();
				dead++;
			}
			else if (stat.getState() == PlayerState.NOT_PLAYING) {
				statName = ChatStyle.YELLOW.toString() + p.getName() + ChatStyle.GRAY.toString();
				dead++;
			}
			else {
				statName = ChatStyle.BRIGHT_GREEN.toString() + p.getName() + ChatStyle.GRAY.toString();
				living++;
			}
			mess += String.format("%s [%d/%d]", statName, stat.getLivesLeft(), stat.getKills().size());
			if (players.size() >= cntr + 1) {
				mess += ", ";
			}
		}
		ChatUtils.send(cs, "<name>[lives/kills]");
		ChatUtils.send(cs, "Total Players: %s Total Living: %s Total Dead or Not Playing: %s", stats.size(), living, dead);
		ChatUtils.send(cs, "");
		ChatUtils.send(cs, mess);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean addChest(Point loc, float weight) {
		if (chests.keySet().contains(loc) || fixedChests.containsKey(loc)) return false;
		blacklistedChests.remove(loc);
		chests.put(loc, weight);
		Block b = loc.getBlock();
		if (b.translate(BlockFace.NORTH).getComponent() instanceof Chest) chests.put(b.translate(BlockFace.NORTH).getPosition(), weight);
		else if (b.translate(BlockFace.SOUTH).getComponent() instanceof Chest) chests.put(b.translate(BlockFace.SOUTH).getPosition(), weight);
		else if (b.translate(BlockFace.EAST).getComponent() instanceof Chest) chests.put(b.translate(BlockFace.EAST).getPosition(), weight);
		else if (b.translate(BlockFace.WEST).getComponent() instanceof Chest) chests.put(b.translate(BlockFace.WEST).getPosition(), weight);
		return true;
	}

	@Override
	public boolean addFixedChest(Point loc, String fixedChest) {
		if (loc == null || fixedChest == null || fixedChest.equalsIgnoreCase("")) return false;
		if (fixedChests.keySet().contains(loc)) return false;
		blacklistedChests.remove(loc);
		if (!(loc.getBlock().getComponent() instanceof Chest)) return false;
		removeChest(loc);
		fixedChests.put(loc, fixedChest);
		Block b = loc.getBlock();
		if (b.translate(BlockFace.NORTH).getComponent() instanceof Chest) fixedChests.put(b.translate(BlockFace.NORTH).getPosition(), fixedChest);
		else if (b.translate(BlockFace.SOUTH).getComponent() instanceof Chest) fixedChests.put(b.translate(BlockFace.SOUTH).getPosition(), fixedChest);
		else if (b.translate(BlockFace.EAST).getComponent() instanceof Chest) fixedChests.put(b.translate(BlockFace.EAST).getPosition(), fixedChest);
		else if (b.translate(BlockFace.WEST).getComponent() instanceof Chest) fixedChests.put(b.translate(BlockFace.WEST).getPosition(), fixedChest);
		return true;
	}

	@Override
	public boolean addSpawnPoint(Transform loc) {
		if (loc == null) return false;
		if (spawnPoints.contains(loc)) return false;
		spawnPoints.add(loc);
		return true;
	}

	/**
	 * Removes chest from fixedChests and adds it to chests
	 * @param loc
	 * @return
	 */
	@Override
	public boolean removeFixedChest(Point loc) {
		if (loc == null) return false;
		if (!(loc.getBlock().getComponent() instanceof Chest)) return false;
		fixedChests.remove(loc);
		Block b = loc.getBlock();
		if (b.translate(BlockFace.NORTH).getComponent() instanceof Chest) fixedChests.remove(b.translate(BlockFace.NORTH).getPosition());
		else if (b.translate(BlockFace.SOUTH).getComponent() instanceof Chest) fixedChests.remove(b.translate(BlockFace.SOUTH).getPosition());
		else if (b.translate(BlockFace.EAST).getComponent() instanceof Chest) fixedChests.remove(b.translate(BlockFace.EAST).getPosition());
		else if (b.translate(BlockFace.WEST).getComponent() instanceof Chest) fixedChests.remove(b.translate(BlockFace.WEST).getPosition());
		return addChest(loc, 1f);
	}

	@Override
	public boolean removeChest(Point loc) {
		Block b = loc.getBlock();
		Point ad = null;
		if (b.translate(BlockFace.NORTH).getComponent() instanceof Chest) ad = b.translate(BlockFace.NORTH).getPosition();
		else if (b.translate(BlockFace.SOUTH).getComponent() instanceof Chest) ad = b.translate(BlockFace.SOUTH).getPosition();
		else if (b.translate(BlockFace.EAST).getComponent() instanceof Chest) ad = b.translate(BlockFace.EAST).getPosition();
		else if (b.translate(BlockFace.WEST).getComponent() instanceof Chest) ad = b.translate(BlockFace.WEST).getPosition();
		if (ad != null) {
			if (chests.remove(ad) == null & fixedChests.remove(ad) == null) {
				blacklistedChests.add(ad);
			}
		}
		if (chests.remove(loc) == null & fixedChests.remove(loc) == null) {
			blacklistedChests.add(loc);
			return false;
		}
		return true;
	}

	public void chestBroken(Point loc) {
		Block b = loc.getBlock();
		Point ad = null;
		if (b.translate(BlockFace.NORTH).getComponent() instanceof Chest) ad = b.translate(BlockFace.NORTH).getPosition();
		else if (b.translate(BlockFace.SOUTH).getComponent() instanceof Chest) ad = b.translate(BlockFace.SOUTH).getPosition();
		else if (b.translate(BlockFace.EAST).getComponent() instanceof Chest) ad = b.translate(BlockFace.EAST).getPosition();
		else if (b.translate(BlockFace.WEST).getComponent() instanceof Chest) ad = b.translate(BlockFace.WEST).getPosition();
		if (ad != null) {
			chests.remove(ad);
			fixedChests.remove(ad);
		}
		chests.remove(loc);
		fixedChests.remove(loc);
	}
	@Override
	public boolean removeSpawnPoint(Point loc) {
		if (loc == null) return false;
		Iterator<Transform> iterator = spawnPoints.iterator();
		Point l;
		while (iterator.hasNext()) {
			if (GeneralUtils.equals(loc, l = iterator.next().getPosition())) {
				iterator.remove();
				for (String playerName : spawnsTaken.keySet()) {
					Transform comp = spawnsTaken.get(playerName);
					if (GeneralUtils.equals(l, comp.getPosition())) {
						spawnsTaken.remove(playerName);
						if (Spout.getEngine().getPlayer(playerName, false) == null) continue;
						ChatUtils.error(Spout.getEngine().getPlayer(playerName, false),
							"Your spawn point has been recently removed. Try rejoining by typing '/hg rejoin %s'", name);
						leave(Spout.getEngine().getPlayer(playerName, false), true);
					}
				}
				return true;
			}
		}
		return false;
	}

	private static void dropInventory(Player player) {
		for (ItemStack i : player.get(PlayerInventory.class).getMain()) {
			if (i == null) continue;
			Item.dropNaturally(player.getTransform().getPosition(), i);
		}
		player.get(PlayerInventory.class).getMain().clear();
	}

	@Override
	public void setEnabled(boolean flag) {
		if (state == DELETED) return;
		if (!flag) {
			if (!flag) stopGame(false);
			state = DISABLED; // TODO do this better
			for (String s : stats.keySet()) {
				Player p = Spout.getEngine().getPlayer(s, false);
				if (p == null) continue;
				playerLeaving(p, false);
				teleportPlayerToSpawn(p);
			}
			clear();
			state = DISABLED;
		}
		if (flag && state == DISABLED) state = STOPPED;
	}

	@Override
	public void setSpawn(Transform newSpawn) {
		spawn = newSpawn;
	}

	@Override
	public List<String> getAllPlayers() {
		return new ArrayList<String>(stats.keySet());
	}

	@Override
	public List<PlayerStat> getStats() {
		return new ArrayList<PlayerStat>(stats.values());
	}
	
	@Override
	public Transform getSpawn() {
		return spawn;
	}

	@Override
	public String getSetup() {
		return (setup == null || "".equals(setup)) ? null : setup;
	}

	@Override
	public List<String> getItemSets() {
		return itemsets;
	}

	@Override
	public void addItemSet(String name) {
		itemsets.add(name);
	}

	@Override
	public void removeItemSet(String name) {
		itemsets.remove(name);
	}
	
	public void setDoneCounting() {
		state = ABOUT_TO_START;
	}
	
	@Override
	public void addWorld(World world) {
		worlds.add(world.getName());
	}

	@Override
	public void addCuboid(Point one, Point two) {
		cuboids.add(new Cuboid(one, two));
	}

	@Override
	public Map<String, List<String>> getSponsors() {
		return Collections.unmodifiableMap(sponsors);
	}

	public void addSponsor(String player, String playerToBeSponsored) {
		if (sponsors.get(player) == null) sponsors.put(player, new ArrayList<String>());
		sponsors.get(player).add(playerToBeSponsored);
	}
	
	@Override
	public Set<World> getWorlds() {
		if (worlds.size() <= 0) return Collections.emptySet();
		Set<World> list = new HashSet<World>();
		for (String s : worlds) {
			if (Spout.getEngine().getWorld(s) == null) continue;
			list.add(Spout.getEngine().getWorld(s));
		}
	return list;
	}
	
	@Override
	public Set<Cuboid> getCuboids() {
		return Collections.unmodifiableSet(cuboids);
	}
	
	@Override
	public void removeItemsOnGround() {
		Logging.debug("Aboout the check items on the ground for %s worlds.", worlds.size());
		for (String s : worlds) {
			World w = Spout.getEngine().getWorld(s);
			if (w == null) continue;
			Logging.debug("Checking world for items.");
			int count = 0;
			for (Entity e : w.getAll()) {
				count++;
				if (!(e instanceof Item)) continue;
				e.remove();
			}
			Logging.debug("Checked: ", count);
		}
		for (Cuboid c : cuboids) {
			if (worlds.contains(c.getLower().getWorld().getName())) continue;
			for (Entity e : c.getLower().getWorld().getAll()) {
				if (!(e instanceof Item)) continue;
				if (!c.isPointWithin(e.getTransform().getPosition())) continue;
				e.remove();
			}
		}
	}
	
	@Override
	public int getSize() {
		return spawnPoints.size();
	}

	@Override
	public void playCannonBoom() {
		for (Player p : getRemainingPlayers()) {
			ExplosionModelSpherical ex = new ExplosionModelSpherical();
			
			ex.execute(p.getTransform().getPosition(), 0, false, new PluginCause(HungerGames.getInstance()));
		}
	}

	@Override
	public List<Long> getEndTimes() {
		return endTimes;
	}

	@Override
	public long getInitialStartTime() {
		return initialStartTime;
	}

	@Override
	public List<Long> getStartTimes() {
		return startTimes;
	}
	
	@Override
	public GameState getState() {
		return state;
	}

	public void delete() {
		clear();
		state = DELETED;
		chests.clear();
		fixedChests.clear();
		setup = null;
		itemsets.clear();
		worlds.clear();
		cuboids.clear();
		spawn = null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final HungerGame other = (HungerGame) obj;
		return this.compareTo(other) == 0;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + (this.name != null ? this.name.toLowerCase().hashCode() : 0);
		return hash;
	}
	
	

	// sorts players by name ignoring case
	private class PlayerComparator implements Comparator<Player> {

		public PlayerComparator() {
		}

		public int compare(Player p1, Player p2) {
			String name1 = p1.getName();
			String name2 = p2.getName();
			return name1.compareToIgnoreCase(name2);
		}

	}
}