package me.kitskub.myhungergames.listeners;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.kitskub.myhungergames.GameManager;

import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.player.PlayerInteractEvent;
import org.spout.api.event.player.PlayerInteractEvent.Action;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.geo.discrete.Point;
import org.spout.api.geo.discrete.Transform;
import org.spout.api.math.Quaternion;
import org.spout.api.math.Vector3;
import org.spout.vanilla.plugin.component.substance.material.chest.Chest;

public class SessionListener implements Listener {
	private static final Map<String, Session> sessions = new HashMap<String, Session>(); // <player, session>>

	@EventHandler(order = Order.LATEST)
	public void playerClickedBlock(PlayerInteractEvent event) {
	    Player player = event.getPlayer();
	    Action action = event.getAction();
	    if (event.isAir()) return;
	    Point loc = event.getInteractedPoint();
	    Block clickedBlock = loc.getBlock();
	    SessionType type = null;
	    HungerGame game = null;
	    Session session = null;
	    if (sessions.containsKey(player.getName())) {
		    session = sessions.get(player.getName());
		    type = session.getType();
		    game = session.getGame();
	    }
	    else {
		    return;
	    }
	    event.setCancelled(true); // Because if not sign interacting would be handled on monitor TODO: better?

	    if (game == null) {
		    if (type == SessionType.SIGN_REMOVER) {
			    LobbyListener.removeSign(loc);
			    ChatUtils.send(player, "Sign has been removed.");
			    sessions.remove(player.getName());
			    return;
		    }
		    return;
	    }
	    switch(type) {
		case CHEST_ADDER:
			if (action == Action.LEFT_CLICK) {
				if (!(clickedBlock.getComponent() instanceof Chest)) {
					ChatUtils.error(player, "Block is not a chest.");
					return;
				}
				float weight = session.getData().get("weight") == null ? 1f : (Float) session.getData().get("weight");
				if (game.addChest(loc, weight)) {
					ChatUtils.send(player, "Chest has been added to %s.", game.getName());
					session.clicked(clickedBlock);
				}
				else {
					ChatUtils.error(player, "Chest has already been added to game %s.",game.getName());
				}
			}
			else {
				ChatUtils.send(player, "You have added %d chests to the game %s.", session.getBlocks().size(), game.getName());
				sessions.remove(player.getName());
			}
			break;
		case CHEST_REMOVER:
			if (action == Action.LEFT_CLICK) {
				if (!(clickedBlock.getComponent() instanceof Chest)) {
					ChatUtils.error(player, "Block is not a chest.");
					return;
				}
				if (game.removeChest(loc)) {
					ChatUtils.send(player, "Chest has been removed from %s.", game.getName());
					session.clicked(clickedBlock);
				}
				else {
					ChatUtils.send(player, "Chest has been blacklisted from %s.", game.getName());
				}
			}
			else {
			ChatUtils.send(player, "You have removed %d chests from the game %s.", session.getBlocks().size(), game.getName());
			sessions.remove(player.getName());
			}
			break;
		case SPAWN_ADDER:
			loc.add(.5, 1, .5);
			if (action == Action.LEFT_CLICK) {
				if (game.addSpawnPoint(new Transform(loc, Quaternion.IDENTITY, Vector3.ONE))) {
					session.clicked(clickedBlock);
					ChatUtils.send(player, "Spawn point %s has been added to %s.", session.getBlocks().size(), game.getName());
				}
				else {
					ChatUtils.error(player, "%s already has this spawn point.", game.getName());
				}
			}
			else {
				ChatUtils.send(player, "You have added %d spawn points to the game %s.", session.getBlocks().size(), game.getName());
				sessions.remove(player.getName());
			}
			break;
		case SPAWN_REMOVER:
			loc.add(.5, 1, .5);
			if (action == Action.LEFT_CLICK) {
				if (game.removeSpawnPoint(loc)) {
					session.clicked(clickedBlock);
					ChatUtils.send(player, "Spawn point %s has been removed from %s.", session.getBlocks().size(), game.getName());
				}
				else {
					ChatUtils.error(player, "%s does not contain this spawn point.", game.getName());
				}
			}
			else {
				ChatUtils.send(player, "You have removed %d spawn points from the game %s.", session.getBlocks().size(), game.getName());
				sessions.remove(player.getName());
			}
			break;
		case CUBOID_ADDER:
			if (session.getBlocks().size() < 1) {
				session.clicked(clickedBlock);
				ChatUtils.send(player, "First corner set.");
			}
			else {
				game.addCuboid(session.getBlocks().get(0).getPosition(), loc);
				sessions.remove(player.getName());
				ChatUtils.send(player, "Second corner and cuboid set.");
			}
			break;
		case FIXED_CHEST_ADDER:
			if (game.addFixedChest(loc, session.getData().get("name").toString())) {
				sessions.remove(player.getName());
				ChatUtils.send(player, "Chest is now a fixed item chest.");
			}
			else {
				ChatUtils.error(player, "That is not a chest!");    
			}
			break;
		case FIXED_CHEST_REMOVER:
			if (game.removeFixedChest(loc)) {
				sessions.remove(player.getName());
				ChatUtils.send(player, "Chest is no longer a fixed item chest.");
			}
			else {
				ChatUtils.error(player, "That is not a chest! Try again!");    
			}
			break;
		case JOIN_SIGN_ADDER:
			if (LobbyListener.addJoinSign(loc, session.getData().get("game").toString())) {
				sessions.remove(player.getName());
				ChatUtils.send(player, "Join sign has been added successfully.");
			}
			else {
				ChatUtils.error(player, "Error when adding join sign!");    
			}
			break;
		case GAME_SIGN_ADDER:
			if (LobbyListener.addGameSign(loc, session.getData().get("game").toString())) {
				sessions.remove(player.getName());
				ChatUtils.send(player, "Game sign has been added successfully.");
			}
			else {
				ChatUtils.error(player, "Error when adding game sign! Try again!");    
			}
			break;
		case INFO_WALL_ADDER:
			if (session.getBlocks().size() < 1) {
				session.clicked(clickedBlock);
				ChatUtils.send(player, "First corner set.");
			}
			else {
				ChatUtils.send(player, "Second corner and info wall set.");
				LobbyListener.addInfoWall(session.getBlocks().get(0).getPosition(), loc, event.getClickedFace(), session.getData().get("game").toString());
				sessions.remove(player.getName());
			}
			break;
		default:
		//Logging.debug("Failed to get sessionlistener.");
		break;
	    }
	}
	
	public static void addSession(SessionType type, Player player, String game) {
		sessions.put(player.getName(), new Session(type, game));
	}
	
	public static void addSession(SessionType type, Player player, String game, Object... data) {
		sessions.put(player.getName(),  new Session(type, game, data));
	}

	public static void removePlayer(Player player) {
		sessions.remove(player.getName());
	}
	
	public enum SessionType {
		FIXED_CHEST_ADDER,
		FIXED_CHEST_REMOVER,
		SPAWN_ADDER,
		SPAWN_REMOVER,
		CHEST_ADDER,
		CHEST_REMOVER,
		CUBOID_ADDER,
		JOIN_SIGN_ADDER,
		GAME_SIGN_ADDER,
		INFO_WALL_ADDER,
		SIGN_REMOVER;
	}
	
	private static class Session {
		private final SessionType type;
		private List<Block> blocks;
		private final String game;
		private final Map<Object, Object> data;

		public Session(SessionType type, String game) {
			this(type, game, "");
		}
		
		public Session(SessionType type, String game, Object... args) {
			this.game = game;
			this.type = type;
			this.blocks = new ArrayList<Block>();
			data = new HashMap<Object, Object>();
			if (args.length % 2 == 1) return;
			for (int i = 0; i < args.length; i += 2) {
				data.put(args[i], args[i + 1]);
			}
		}

		public HungerGame getGame() {
			return GameManager.INSTANCE.getRawGame(game);
		}

		public void clicked(Block block) {
			blocks.add(block);
		}

		public List<Block> getBlocks() {
			return blocks;
		}

		public SessionType getType() {
			return type;
		}

		public Map<Object, Object> getData() {
			return data;
		}
	}
}
