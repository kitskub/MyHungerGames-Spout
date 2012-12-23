package me.kitskub.myhungergames.listeners;


import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;

import me.kitskub.myhungergames.*;
import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.stats.PlayerStat;
import me.kitskub.myhungergames.utils.EquatableWeakReference;
import me.kitskub.myhungergames.utils.GeneralUtils;

import org.spout.api.Spout;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.block.BlockChangeEvent;
import org.spout.api.event.cause.PluginCause;
import org.spout.api.event.player.PlayerInteractEvent;
import org.spout.api.geo.World;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.geo.discrete.Point;
import org.spout.api.material.block.BlockFace;
import org.spout.api.scheduler.TaskPriority;
import org.spout.api.util.config.ConfigurationNode;
import org.spout.api.util.config.yaml.YamlConfiguration;
import org.spout.vanilla.chat.VanillaStyleHandler;
import org.spout.vanilla.component.substance.material.Sign;
import org.spout.vanilla.event.cause.PlayerBreakCause;

public class LobbyListener implements Listener, Runnable {
	private static Map<Point, WeakReference<HungerGame>> joinSigns = new HashMap<Point, WeakReference<HungerGame>>();
	private static Map<Point, WeakReference<HungerGame>> gameSigns = new HashMap<Point, WeakReference<HungerGame>>();
	private static List<InfoWall> infoWalls = new ArrayList<InfoWall>();
	private static int currentCheckPeriod = 0, maxCheckPeriod = 5;
	
	public LobbyListener() {
		Spout.getScheduler().scheduleSyncRepeatingTask(HungerGames.getInstance(), this, 0, 40, TaskPriority.NORMAL);
	}
	
	public static void removeSign(Point loc) {
		joinSigns.remove(loc);
		gameSigns.remove(loc);
		Block b = loc.getBlock();
		if (b.getComponent() instanceof Sign) {
			Sign sign = (Sign) b;
			String[] text = {"","","",""};
			sign.setText(text, new PluginCause(HungerGames.getInstance()));
		}
		for (Iterator<InfoWall> it = infoWalls.iterator(); it.hasNext();) {
			InfoWall w = it.next();
			if (w.signs.contains(loc)) {
				w.clearSigns();
				it.remove();
			}
		}
		save();
	}
	
	public static boolean addInfoWall(Point one, Point two, BlockFace clickedFace, String str) {
		if (one.getWorld() != two.getWorld()) return false;
		EquatableWeakReference<HungerGame> game = GameManager.INSTANCE.getGame(str);
		if (game == null) return false;
		World w = one.getWorld();
		int oneX = one.getBlockX();
		int oneY = one.getBlockY();
		int oneZ = one.getBlockZ();
		int twoX = two.getBlockX();
		int twoY = two.getBlockY();
		int twoZ = two.getBlockZ();
		int xSize = Math.abs(twoX - oneX);
		int zSize = Math.abs(twoZ - oneZ);
		if (xSize > 0 && zSize > 0) return false; // Sorry can't be a 3d wall
		int startY = Math.max(oneY, twoY);
		int endY = Math.min(oneY, twoY);
		List<Point> locs = new ArrayList<Point>();
		if (xSize > 0) {
			switch (clickedFace) {
				// S to N, + to - X
				case NORTH:
				case EAST:
					for (int y = startY; y >= endY; y--) {
						for (int x = Math.max(oneX, twoX); x >= Math.min(oneX, twoX); x--) {
							Point loc = new Point(w, x, y, oneZ);
							if (loc.getBlock().getComponent() instanceof Sign) locs.add(loc);
						}
					}
					break;
				// N to S, - to + X
				case WEST:
				case SOUTH:
				default:
					for (int y = startY; y >= endY; y--) {
						for (int x = Math.min(oneX, twoX); x <= Math.max(oneX, twoX); x++) {
							Point loc = new Point(w, x, y, oneZ);
							if (loc.getBlock().getComponent() instanceof Sign) locs.add(loc);
						}
					}
					break;
			}
		}
		else if (zSize > 0) {
			switch (clickedFace) {
				// W to E, + to - Z
				case SOUTH:
				case EAST:
					for (int y = startY; y >= endY; y--) {
						for (int z = Math.max(oneZ, twoZ); z >= Math.min(oneZ, twoZ); z--) {
							Point loc = new Point(w, oneX, y, z);
							if (loc.getBlock().getComponent() instanceof Sign) locs.add(loc);
						}
					}
					break;
				// E to W, - to + Z
				case NORTH:
				case WEST:
				default:
					for (int y = startY; y >= endY; y--) {
						for (int z = Math.min(oneZ, twoZ); z <= Math.max(oneZ, twoZ); z++) {
							Point loc = new Point(w, oneX, y, z);
							if (loc.getBlock().getComponent() instanceof Sign) locs.add(loc);
						}
					}
					break;
			}
		}
		infoWalls.add(new InfoWall(game, locs));
		save();
		return true;
	}

	public static boolean addJoinSign(Point Point, String name) {
		if (Point == null) return false;
		Block block = Point.getBlock();
		if (!(block.getComponent() instanceof Sign)) return false;
		EquatableWeakReference<HungerGame> game = GameManager.INSTANCE.getGame(name);
		if (game == null) return false;
		joinSigns.put(Point, game);
		Sign sign = (Sign) block.getComponent();
		String[] text = {"[MyHungerGames]", "Click the sign", "to join", name}; 
		sign.setText(text, new PluginCause(HungerGames.getInstance()));
		save();
		return true;
	}

	public static boolean addGameSign(Point Point, String str) {
		if (Point == null) return false;
		Block block = Point.getBlock();
		if (!(block.getComponent() instanceof Sign)) return false;
		EquatableWeakReference<HungerGame> game = GameManager.INSTANCE.getGame(str);
		if (game == null) return false;
		gameSigns.put(Point, game);
		updateGameSigns();
		save();
		return true;
	}
	
	@EventHandler(order = Order.MONITOR)
	public static void playerClickedBlock(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (event.isAir()) return;
		if (!joinSigns.containsKey(event.getInteractedPoint())) return;
		WeakReference<HungerGame> game = joinSigns.get(event.getInteractedPoint());
		if (game == null || game.get() == null) {
			joinSigns.remove(event.getInteractedPoint());
			return;
		}
		game.get().join(event.getPlayer());
	}
	
	@EventHandler(order = Order.MONITOR)
	public static void onBlockBreak(BlockChangeEvent event) {
		if (event.isCancelled()) return;
		if (event.getCause() instanceof PlayerBreakCause == false) return;
		removeSign(event.getBlock().getPosition());		
	}

	public void run() {
		currentCheckPeriod++;
		if (currentCheckPeriod >= maxCheckPeriod) {
			for (Point l : joinSigns.keySet()) {
				WeakReference<HungerGame> game = joinSigns.get(l);
				if (game == null || game.get() == null) {
					joinSigns.remove(l);
				}
				if (game.get().getState() == HungerGame.GameState.DELETED) {
					joinSigns.remove(l);
				}
			}
			currentCheckPeriod = 0;
		}

		updateGameSigns();
		for (Iterator<InfoWall> it = infoWalls.iterator(); it.hasNext();) {
			InfoWall w = it.next();
			if (w.signs.isEmpty()) {
				it.remove();
				continue;
			}
			w.update();
		}
		save();
	}
	
	public static void save() {
		YamlConfiguration config = Files.LOBBY_SIGNS.getConfig();
		ConfigurationNode joinSection = config.addNode("join-signs");
		ConfigurationNode gameSection = config.addNode("game-signs");
		ConfigurationNode infoSection = config.addNode("info-walls");

		int count = 0;
		for (Iterator<Entry<Point, WeakReference<HungerGame>>> it = joinSigns.entrySet().iterator(); it.hasNext();) {
			Entry<Point, WeakReference<HungerGame>> entry = it.next();
			if (entry.getValue().get() == null) {
				it.remove();
				continue;
			}
			count++;
			ConfigurationNode section = joinSection.getChild(String.valueOf(count));
			section.getChild("location").setValue(GeneralUtils.parseToString(entry.getKey()));
			section.getChild("game").setValue(entry.getValue().get().getName());
		}
		count = 0;
		for (Iterator<Entry<Point, WeakReference<HungerGame>>> it = gameSigns.entrySet().iterator(); it.hasNext();) {
			Entry<Point, WeakReference<HungerGame>> entry = it.next();
			if (entry.getValue().get() == null) {
				it.remove();
				continue;
			}
			count++;
			ConfigurationNode section = gameSection.getChild(String.valueOf(count));
			section.getChild("location").setValue(GeneralUtils.parseToString(entry.getKey()));
			section.getChild("game").setValue(entry.getValue().get().getName());
		}
		count = 0;
		for (InfoWall w : infoWalls) {
			if (w.game.get() == null) continue;
			count++;
			ConfigurationNode section = infoSection.getChild(String.valueOf(count));
			section.getChild("game").setValue(w.game.get().getName());
			List<String> strings = new ArrayList<String>();
			for (Point l : w.signs) {
				strings.add(GeneralUtils.parseToString(l));
			}
			section.getChild("signs").setValue(strings);
		}
		Files.LOBBY_SIGNS.save();
	}

	public static void load() {
		YamlConfiguration config = Files.LOBBY_SIGNS.getConfig();
		ConfigurationNode joinSection = config.getChild("join-signs");
		ConfigurationNode gameSection = config.getChild("game-signs");
		ConfigurationNode infoSection = config.getChild("info-walls");
		
		if (joinSection.isAttached()) {//TODO confirm
			joinSigns.clear();
			for (String key : joinSection.getKeys(false)) {
				ConfigurationNode section = joinSection.getChild(key);
				Point loc;
				try {
					loc = GeneralUtils.parseToPoint(section.getNode("location").getString(""));
				} catch (NumberFormatException ex) {
					Logging.debug(ex.getMessage());
					continue;
				} catch (WorldNotFoundException ex) {
					Logging.warning(ex.getMessage());
					continue;
				} catch (IllegalArgumentException ex) {
					continue;
				}
				EquatableWeakReference<HungerGame> game = GameManager.INSTANCE.getGame(section.getNode("game").getString(""));
				if (game == null) continue;
				joinSigns.put(loc, game);
			}
		}
		if (gameSection.isAttached()) {
			gameSigns.clear();
			for (String key : gameSection.getKeys(false)) {
				ConfigurationNode section = gameSection.getChild(key);
				Point loc;
				try {
					loc = GeneralUtils.parseToPoint(section.getNode("location").getString(""));
				} catch (NumberFormatException ex) {
					Logging.debug(ex.getMessage());
					continue;
				} catch (WorldNotFoundException ex) {
					Logging.warning(ex.getMessage());
					continue;
				} catch (IllegalArgumentException ex) {
					continue;
				}
				EquatableWeakReference<HungerGame> game = GameManager.INSTANCE.getGame(section.getNode("game").getString(""));
				if (loc == null || game == null) continue;
				gameSigns.put(loc, game);
			}
		}
		if (infoSection.isAttached()) {
			infoWalls.clear();
			for (String key : infoSection.getKeys(false)) {
				ConfigurationNode section = infoSection.getChild(key);
				EquatableWeakReference<HungerGame> game = GameManager.INSTANCE.getGame(section.getNode("game").getString(""));
				List<String> strings = section.getChild("signs").getStringList();
				List<Point> locs = new ArrayList<Point>();
				for (String s : strings) {
					Point loc;
					try {
						loc = GeneralUtils.parseToPoint(s);
					} catch (NumberFormatException ex) {
						Logging.debug(ex.getMessage());
						continue;
					} catch (WorldNotFoundException ex) {
						Logging.warning(ex.getMessage());
						continue;
					} catch (IllegalArgumentException ex) {
						continue;
					}
					locs.add(loc);
				}
				InfoWall w = new InfoWall(game, locs);
				infoWalls.add(w);
			}
		}
	}

	public static void updateGameSigns() {
		for (Iterator<Point> it = gameSigns.keySet().iterator(); it.hasNext();) {
			Point l = it.next();
			WeakReference<HungerGame> gameRef = gameSigns.get(l);
			if (gameRef == null || gameRef.get() == null) {
				it.remove();
				continue;
			}
			HungerGame game = gameRef.get();
			if (game.getState() == HungerGame.GameState.DELETED || !(l.getBlock().getComponent() instanceof Sign)) {
				it.remove();
				continue;
			}

			Sign sign = (Sign) l.getBlock().getComponent();

			String[] text = new String[4];
			Arrays.fill(text, "");
			text[0] = (game.getState() != HungerGame.GameState.DISABLED ? toString(ChatStyle.BRIGHT_GREEN) : toString(ChatStyle.RED)) + game.getName();
			if (game.getState() == HungerGame.GameState.DISABLED) {
				text[1] = toString(ChatStyle.RED) + "Disabled";
			}
			else if (game.getState() == HungerGame.GameState.PAUSED) {
				text[1] = "Paused";
				text[2] = "In-game:" + game.getRemainingPlayers().size();
			}
			else if (game.getState() == HungerGame.GameState.STOPPED) {
				text[1] = "Stopped";
				text[2] ="Ready:" + game.getRemainingPlayers().size();
				text[3] = "Available:" + (game.getSize() - game.getRemainingPlayers().size());
			}
			else if (game.getState() == HungerGame.GameState.RUNNING || game.getState() == HungerGame.GameState.COUNTING_FOR_RESUME || game.getState() == HungerGame.GameState.COUNTING_FOR_START) {
				text[1] = "Running";
				text[2] = "Remaining:" + game.getRemainingPlayers().size();
			}
			sign.setText(text, new PluginCause(HungerGames.getInstance()));
		}
	}

	private static String toString(ChatStyle style) {
		VanillaStyleHandler styler = VanillaStyleHandler.INSTANCE;
		return styler.getFormatter(style).format("");
	}

	private static final class InfoWall {
		private final List<Point> signs;
		private final EquatableWeakReference<HungerGame> game;

		public InfoWall(EquatableWeakReference<HungerGame> game, List<Point> list) {
			this.signs = list;
			this.game = game;
			update();
		}
		public void update() {
			if (game.get() == null) return;
			List<Sign> signList = new ArrayList<Sign>();
			for (Iterator<Point> it = signs.iterator(); it.hasNext();) {
				Point l = it.next();
				if (!(l.getBlock().getComponent() instanceof Sign)) {
					it.remove();
					continue;
				}
				signList.add((Sign) l.getBlock().getComponent());
			}
			Iterator<Sign> signIt = signList.iterator();
			TreeSet<PlayerStat> stats = new TreeSet<PlayerStat>(new PlayerStat.StatComparator());
			stats.addAll(game.get().getStats());
			Iterator<PlayerStat> statIt = stats.iterator();
			
			while(signIt.hasNext()) {
				Sign nextSign = signIt.next();
				if (statIt.hasNext()) {
					PlayerStat nextStat = statIt.next();
					String[] text = {nextStat.getPlayer().getName(),
						"Kills:" + nextStat.getKills().size(),
						"Deaths:" + nextStat.getDesths().size(),
						"Lives:" + nextStat.getLivesLeft()};
					nextSign.setText(text, new PluginCause(HungerGames.getInstance()));
				}
				else {
					String[] text = {"", "", "", ""};
					nextSign.setText(text, new PluginCause(HungerGames.getInstance()));
				}
			}
		}

			
		public void clearSigns() {
			for (Iterator<Point> it = signs.iterator(); it.hasNext();) {
				Point l = it.next();
				if (!(l.getBlock().getComponent() instanceof Sign)) {
					it.remove();
					continue;
				}
				Sign sign = (Sign) l.getBlock().getComponent();
				String[] text = {"", "", "", ""};
				sign.setText(text, new PluginCause(HungerGames.getInstance()));
			}
		}
	
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final InfoWall other = (InfoWall) obj;
			if (this.signs != other.signs && (this.signs == null || !this.signs.equals(other.signs))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 61 * hash + (this.signs != null ? this.signs.hashCode() : 0);
			return hash;
		}	
	}
}
