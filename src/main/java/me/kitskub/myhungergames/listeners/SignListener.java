package me.kitskub.myhungergames.listeners;

import java.util.*;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.Files;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Logging;
import me.kitskub.myhungergames.WorldNotFoundException;
import me.kitskub.myhungergames.api.Game;
import me.kitskub.myhungergames.api.event.*;
import me.kitskub.myhungergames.utils.GeneralUtils;

import org.spout.api.Spout;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.cause.PluginCause;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.geo.discrete.Point;
import org.spout.api.material.Material;
import org.spout.api.scheduler.Task;
import org.spout.api.scheduler.TaskPriority;
import org.spout.api.util.config.ConfigurationNode;
import org.spout.api.util.config.yaml.YamlConfiguration;
import org.spout.vanilla.component.substance.material.Sign;
import org.spout.vanilla.material.VanillaMaterials;

public class SignListener implements Runnable, Listener {

	private static final Map<ListenerType, Map<String, List<Point>>> listeners = Collections.synchronizedMap(new EnumMap<ListenerType, Map<String, List<Point>>>(ListenerType.class));
	private static final Map<ListenerType, List<Point>> allGameListeners = Collections.synchronizedMap(new EnumMap<ListenerType, List<Point>>(ListenerType.class));
	private static List<OffQueue> queues = new ArrayList<OffQueue>();
	private static Task task;
	
	public SignListener() {
		task = Spout.getScheduler().scheduleSyncRepeatingTask(HungerGames.getInstance(), this, 0, 1, TaskPriority.NORMAL);
		loadSigns();
	}
	
	public static void loadSigns() {
		YamlConfiguration config = Files.SIGNS.getConfig();
		for (ListenerType type : ListenerType.values()) {
			ConfigurationNode section = config.getNode(type.name());
			if (!allGameListeners.containsKey(type)) allGameListeners.put(type, new ArrayList<Point>());
			if (!listeners.containsKey(type)) listeners.put(type, new HashMap<String, List<Point>>());
			if (section == null) continue;

			List<String> sList = section.getChild("gameLocs").getStringList();
			if (sList != null) {
				for (String s : sList) {
					try {
						allGameListeners.get(type).add(GeneralUtils.parseToPoint(s));
					} catch (NumberFormatException ex) {
						Logging.debug(ex.getMessage());
						continue;
					} catch (WorldNotFoundException ex) {
						Logging.warning(ex.getMessage());
						continue;
					}
				}
			}
			if (section == null) continue;
			for (String game : section.getKeys(false)) {
				if (!listeners.get(type).containsKey(game)) listeners.get(type).put(game, new ArrayList<Point>());
				ConfigurationNode gameSection = section.getNode(game);
				if (gameSection == null) continue;
				List<Point> list = new ArrayList<Point>();
				List<String> stringList = gameSection.getChild("gameLocs").getStringList();
				for (String s : stringList) {
					try {
						list.add(GeneralUtils.parseToPoint(s));
					} catch (NumberFormatException ex) {
						Logging.debug(ex.getMessage());
						continue;
					} catch (WorldNotFoundException ex) {
						Logging.warning(ex.getMessage());
						continue;
					}
				}
				listeners.get(type).get(game).addAll(list);
			}
		}
	}
	
	public static void saveSigns() {
		YamlConfiguration config = Files.SIGNS.getConfig();
		for (ListenerType type : ListenerType.values()) {
			ConfigurationNode section = config.getChild(type.name());
			List<String> allGamesList = new ArrayList<String>();
			for (Point l : allGameListeners.get(type)) {
				allGamesList.add(GeneralUtils.parseToString(l));
			}
			section.getChild("allGames").setValue(allGamesList);
			for (String game : listeners.get(type).keySet()) {
				ConfigurationNode gameSection = section.addNode(game);
				List<Point> list = listeners.get(type).get(game);
				List<String> stringList = new ArrayList<String>();
				for (Point l : list) {
					stringList.add(GeneralUtils.parseToString(l));
				}
				gameSection.getChild("gameLocs").setValue(stringList);
			}
		}
	}
	
	private class SignData {
		private Point loc;
		private Material type;
		private int data;
		private String[] lines;

		public SignData(Point loc, Material type, byte data, String[] lines) {
			this.loc = loc;
			this.type = type;
			this.data = data;
			this.lines = lines;
		}
	}

	/**
	 * Add a sign. Does not check
	 * @param type 
	 * @param game 
	 * @param sign
	 * @return  
	 */
	public static boolean addSign(ListenerType type, Game game, Sign sign) {
		if (game != null) {
			Map<String, List<Point>> gameMap = listeners.get(type);
			if (gameMap == null) {
				listeners.put(type, new HashMap<String, List<Point>>());
				gameMap = listeners.get(type);
			}
			List<Point> locs = gameMap.get(game.getName());
			if (locs == null) {
				gameMap.put(game.getName(), new ArrayList<Point>());
				locs = gameMap.get(game.getName());
			}
			locs.add(sign.getPosition());
			return true;
		}
		else {
			List<Point> locs = allGameListeners.get(type);
			if (locs == null) {
				allGameListeners.put(type, new ArrayList<Point>());
				locs = allGameListeners.get(type);
			}
			locs.add(sign.getPosition());
			return true;
		}
	}

	public void run() {
		if (queues.size() <= 0) return;
		List<OffQueue> toRemove = new ArrayList<OffQueue>();
		for (OffQueue queue : queues) {
			if (queue.removeTick()) {
				// Logging.debug("Removing sign from queue");
				for (SignData sign : queue.signs) {
					Block b = sign.loc.getBlock();
					b.setMaterial(sign.type, sign.data);
					if (sign.lines != null) {
						if (b.getComponent() instanceof Sign) {
							Sign signBlock = (Sign) b.getComponent();
							signBlock.setText(sign.lines, new PluginCause(HungerGames.getInstance()));
						}
					}
				}
				toRemove.add(queue);
			}
		}
		queues.removeAll(toRemove);
	}
	
	private void callListeners(ListenerType type, Game game) {
		Map<String, List<Point>> gameMap = listeners.get(type);
		if (gameMap == null) {
			listeners.put(type, new HashMap<String, List<Point>>());
			gameMap = listeners.get(type);
		}
		List<Point> locs = gameMap.get(game.getName());
		if (locs == null) {
			gameMap.put(game.getName(), new ArrayList<Point>());
			locs = gameMap.get(game.getName());
		}
		List<Point> toRemove = new ArrayList<Point>();
		List<SignData> signs = new ArrayList<SignData>();
		locs.addAll(allGameListeners.get(type));
		for (Point loc : locs) {
			String[] lines = null;
			Block block = loc.getBlock();
			if (block.getComponent() instanceof Sign) {
				Sign sign = (Sign) block.getComponent();
				lines = sign.getText();
			}
			if (loc.getBlock().getMaterial().equals(VanillaMaterials.SIGN_POST)) {
				loc.getBlock().setMaterial(VanillaMaterials.REDSTONE_TORCH_ON, 0x5);
				signs.add(new SignData(loc, VanillaMaterials.SIGN_POST, (byte) 0x1, lines));
			}
			else if (loc.getBlock().getMaterial().equals(VanillaMaterials.WALL_SIGN)) {
				short data = loc.getBlock().getData(); // Correspond to the direction of the wall sign
				if (data == 0x2) { // South
					loc.getBlock().setMaterial(VanillaMaterials.REDSTONE_TORCH_ON);
					signs.add(new SignData(loc, VanillaMaterials.WALL_SIGN, (byte) 0x2, lines));
				}
				else if (data == 0x3) { // North
					loc.getBlock().setMaterial(VanillaMaterials.REDSTONE_TORCH_ON,0x3);
					signs.add(new SignData(loc, VanillaMaterials.WALL_SIGN, (byte) 0x3, lines));
				}
				else if (data == 0x4) { // East
					loc.getBlock().setMaterial(VanillaMaterials.REDSTONE_TORCH_ON,0x2);
					signs.add(new SignData(loc, VanillaMaterials.WALL_SIGN, (byte) 0x4, lines));
				}
				else if (data == 0x5) { // West
					loc.getBlock().setMaterial(VanillaMaterials.REDSTONE_TORCH_ON,0x1);
					signs.add(new SignData(loc, VanillaMaterials.WALL_SIGN, (byte) 0x5, lines));
				}
			}
			else {
				Logging.warning("Point is no longer a sign");
				toRemove.add(loc);
				continue;
			}
		}
		listeners.get(type).get(game.getName()).removeAll(toRemove);
		allGameListeners.get(type).removeAll(toRemove);
		queues.add(new OffQueue(signs));
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onGameEnd(GameEndEvent event) {
		if (event.isCancelled()) return;
		callListeners(ListenerType.GAME_END, event.getGame());
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onGamePause(GamePauseEvent event) {
		if (event.isCancelled()) return;
		callListeners(ListenerType.GAME_PAUSE, event.getGame());
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onGameStart(GameStartEvent event) {
		if (event.isCancelled()) return;
		callListeners(ListenerType.GAME_START, event.getGame());
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		if (event.isCancelled()) return;
		callListeners(ListenerType.PLAYER_JOIN, event.getGame());
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onPlayerKill(PlayerKillEvent event) {
		if (event.isCancelled()) return;
		callListeners(ListenerType.PLAYER_KILL, event.getGame());
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		if (event.isCancelled()) return;
		if (event.getType().equals(PlayerLeaveGameEvent.Type.LEAVE)) {
			callListeners(ListenerType.PLAYER_LEAVE, event.getGame());
		}
		else if (event.getType().equals(PlayerLeaveGameEvent.Type.QUIT)) {
			callListeners(ListenerType.PLAYER_QUIT, event.getGame());
		}
		else if (event.getType().equals(PlayerLeaveGameEvent.Type.KICK)) {
			callListeners(ListenerType.PLAYER_KICK, event.getGame());
		}
	}
	
	public enum ListenerType {
		GAME_END("gameend", Perm.ADMIN_CREATE_SIGN_GAMEEND),
		GAME_PAUSE("gamepause", Perm.ADMIN_CREATE_SIGN_GAMEPAUSE),
		GAME_START("gamestart", Perm.ADMIN_CREATE_SIGN_GAMESTART),
		PLAYER_JOIN("playerjoin", Perm.ADMIN_CREATE_SIGN_PLAYERJOIN),
		PLAYER_KICK("playerkick", Perm.ADMIN_CREATE_SIGN_PLAYERKICK),
		PLAYER_KILL("playerkill", Perm.ADMIN_CREATE_SIGN_PLAYERKILL),
		PLAYER_LEAVE("playerleave", Perm.ADMIN_CREATE_SIGN_PLAYERLEAVE),
		PLAYER_QUIT("playerquit", Perm.ADMIN_CREATE_SIGN_PLAYERQUIT);
		
		private String id;
		private Perm perm;
		private static final Map<String, ListenerType> map = new HashMap<String, ListenerType>();
		
		private ListenerType(String id, Perm perm) {
			this.id = id;
			this.perm = perm;
		}
		
		public String getId() {
			return id;
		}
		
		public Perm getPerm() {
			return perm;
		}
		
		public static ListenerType byId(String string) {
			if (string == null) return null;
			return map.get(string);
		}
		
		static {
			for (ListenerType value : values()) {
				map.put(value.id, value);
			}
		}
	}
	
	private class OffQueue {
		private int ticksLeft;
		private List<SignData> signs = new ArrayList<SignData>();

		public OffQueue(List<SignData> signs) {
			this.signs.addAll(signs);
			ticksLeft = 20;
		}
		
		public boolean removeTick() {
			ticksLeft--;
			return ticksLeft <= 0;
		}
		
	}

}
