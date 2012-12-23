package me.kitskub.myhungergames.listeners;

import me.kitskub.myhungergames.Config;
import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.stats.PlayerStat.PlayerState;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.block.BlockChangeEvent;
import org.spout.api.event.block.BlockEvent;
import org.spout.api.event.cause.PlayerCause;
import org.spout.api.event.player.PlayerInteractEvent;
import org.spout.api.event.player.PlayerInteractEvent.Action;
import org.spout.vanilla.component.substance.material.Sign;
import org.spout.vanilla.component.substance.material.chest.Chest;
import org.spout.vanilla.event.block.SignUpdateEvent;
import org.spout.vanilla.event.cause.PlayerBreakCause;
import org.spout.vanilla.event.cause.PlayerPlacementCause;
import org.spout.vanilla.material.VanillaMaterials;

public class BlockListener implements Listener {
	
	@EventHandler(order = Order.MONITOR)
	public void onSignChange(SignUpdateEvent event) {
		if (event.isCancelled()) return;
		if(!event.getLines()[0].equalsIgnoreCase("[MyHungerGames]")) return;
		String[] lines = event.getLines();
		SignListener.ListenerType type = SignListener.ListenerType.byId(lines[1]);
		if (type == null) return;
		HungerGame game = null;
		if (lines[2] != null && !lines[2].equals("")) {
			game = GameManager.INSTANCE.getRawGame(lines[2]);
			if (game == null) {
				event.setLine(1, "");
				event.setLine(2, "BAD GAME NAME!");
				event.setLine(3, "");
				return;
			}
		}
		Sign sign = event.getSign();
		if (event.getSource() instanceof PlayerCause) {
			if (!HungerGames.checkPermission(((PlayerCause)event.getSource()).getSource(), type.getPerm())) {
				event.setLine(1, "");
				event.setLine(2, "NO PERMISSION!");
				event.setLine(3, "");
				return;
			}
		}
		if (SignListener.addSign(type, game, sign)) {
			if (event.getSource() instanceof PlayerCause) {
				ChatUtils.send(((PlayerCause)event.getSource()).getSource(), "Sign was created successfully.");
			}
		}
		else {
			if (event.getSource() instanceof PlayerCause) {
				ChatUtils.error(((PlayerCause)event.getSource()).getSource(), "Sign was not created.");
			}
		}
	}

	@EventHandler(order = Order.MONITOR)
	public void onchestBreak(BlockEvent event) {
		if (event.isCancelled()) return;
		if (!(event.getBlock().getComponent() instanceof Chest)) return;
		for (HungerGame game : GameManager.INSTANCE.getRawGames()) {
			game.chestBroken(event.getBlock().getPosition());
		}
	}
	
	
	@EventHandler(order = Order.DEFAULT)
	public void onBlockChange(BlockChangeEvent event) {
		if (event.getCause() instanceof PlayerBreakCause) {
			onBlockBreak(event, (PlayerBreakCause) event.getCause());
		}
		else if (event.getCause() instanceof PlayerPlacementCause) {
			onBlockPlace(event, (PlayerPlacementCause) event.getCause());
		}
	}
	
	
	public void onBlockPlace(BlockChangeEvent event, PlayerPlacementCause cause) {
		Player player = cause.getSource();
		HungerGame session = GameManager.INSTANCE.getRawPlayingSession(player);
		if(session != null) {
			if (session.getPlayerStat(player).getState().equals(PlayerState.WAITING)) {
				event.setCancelled(true);
				return;
			}
			String setup = session.getSetup();
			if(!Config.getCanPlaceBlock(setup, event.getBlock())) {
				ChatUtils.error(player, "You cannot place this block while in game %s.", session.getName());
				event.setCancelled(true);
			}
		}
		else if (GameManager.INSTANCE.getSpectating(player) != null) { // TODO configurable
			event.setCancelled(true);
			ChatUtils.error(player, "You cannot place this block while spectating %s.", GameManager.INSTANCE.getSpectating(player));
		}
	}

	public void onBlockBreak(BlockChangeEvent event, PlayerBreakCause cause) {
		Player player = cause.getSource();
		HungerGame session = GameManager.INSTANCE.getRawPlayingSession(player);
		if(session != null) {
			if (session.getPlayerStat(player).getState().equals(PlayerState.WAITING)) {
				event.setCancelled(true);
				return;
			}
			String setup = session.getSetup();
			if(!Config.getCanBreakBlock(setup, event.getBlock())) {
				ChatUtils.error(player, "You cannot break this block while in game %s.", session.getName());
				event.setCancelled(true);
			}
		}
		else if (GameManager.INSTANCE.getSpectating(player) != null) { // TODO configurable
			event.setCancelled(true);
			ChatUtils.error(player, "You cannot break this block while spectating %s.", GameManager.INSTANCE.getSpectating(player));
		}
	}
	
	@EventHandler(order = Order.MONITOR)
	public void onPlayerInteractMonitor(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (event.getAction() != Action.RIGHT_CLICK) return;
		if (event.isAir() || !(event.getInteractedPoint().getBlock().getComponent() instanceof Chest)) return;

                Player player = event.getPlayer();
                HungerGame game = GameManager.INSTANCE.getRawPlayingSession(player);
                if(game == null) return;
		if(!Defaults.Config.AUTO_ADD.getBoolean(game.getSetup())) return;
		
		// Logging.log(Level.FINEST, "Inventory opened and checking for fill. Player: {0}", player.getName());
                game.addAndFillChest((Chest) event.getInteractedPoint().getBlock().getComponent());
	}

	@EventHandler(order = Order.DEFAULT)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK || event.isAir()) return;
		Player player = event.getPlayer();
		HungerGame session = GameManager.INSTANCE.getRawPlayingSession(player);
		if(session != null) {
			if (session.getPlayerStat(player).getState().equals(PlayerState.WAITING)) {
				event.setCancelled(true);
				return;
			}
			String setup = session.getSetup();
			if(!Config.getCanInteractBlock(setup, event.getInteractedPoint().getBlock()) && !event.getInteractedPoint().getBlock().getMaterial().equals(VanillaMaterials.CHEST)) {
				ChatUtils.error(player, "You cannot interact with this block while in game %s.", session.getName());
				event.setCancelled(true);
			}
		}
		else if (GameManager.INSTANCE.getSpectating(player) != null) { // TODO configurable
			event.setCancelled(true);
			ChatUtils.error(player, "You cannot interact with this block while spectating %s.", GameManager.INSTANCE.getSpectating(player));
		}
	}

}