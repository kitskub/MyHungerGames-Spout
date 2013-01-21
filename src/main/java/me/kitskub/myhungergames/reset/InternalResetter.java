package me.kitskub.myhungergames.reset;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Logging;
import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.utils.Cuboid;
import me.kitskub.myhungergames.utils.EquatableWeakReference;

import org.spout.api.Spout;
import org.spout.api.component.Component;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.block.BlockChangeEvent;
import org.spout.api.event.player.PlayerInteractEvent;
import org.spout.api.geo.cuboid.Block;
import org.spout.api.geo.discrete.Point;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.BlockMaterial;
import org.spout.api.material.Material;
import org.spout.api.scheduler.TaskPriority;
import org.spout.vanilla.plugin.event.entity.EntityExplodeEvent;
import org.spout.vanilla.plugin.inventory.Container;

public class InternalResetter extends Resetter implements Listener, Runnable {
	private static final Map<EquatableWeakReference<HungerGame>, Map<Point, BlockMaterial>> changedBlocks = Collections.synchronizedMap(new WeakHashMap<EquatableWeakReference<HungerGame>, Map<Point, BlockMaterial>>());//TODO make better deque?
	private static final Map<EquatableWeakReference<HungerGame>, Map<Point, ItemStack[]>> changedInvs = Collections.synchronizedMap(new WeakHashMap<EquatableWeakReference<HungerGame>, Map<Point, ItemStack[]>>());
	private static final Map<Point, BlockMaterial> toCheck = new ConcurrentHashMap<Point, BlockMaterial>();
	private static final Map<Point, ItemStack[]> toCheckInvs = new ConcurrentHashMap<Point, ItemStack[]>();


	@Override
	public void init() {
		Spout.getEventManager().registerEvents(this, HungerGames.getInstance());
		Spout.getScheduler().scheduleSyncRepeatingTask(HungerGames.getInstance(), this, 0, 20, TaskPriority.NORMAL);
	}

	@Override
	public void beginGame(HungerGame game) {
	}

	@Override
	public boolean resetChanges(HungerGame game) {
		EquatableWeakReference<HungerGame> eMap = new EquatableWeakReference<HungerGame>(game);
		if(!changedBlocks.containsKey(new EquatableWeakReference<HungerGame>(game))) return true;
		for(Point l : changedBlocks.get(eMap).keySet()) {
			BlockMaterial state = changedBlocks.get(eMap).get(l);
			l.getBlock().setMaterial(state);

		}
		int chests = 0;
		for(Point l : changedInvs.get(eMap).keySet()) {
			Component state = l.getBlock().getComponent();
			if (!(state instanceof Container)) throw new IllegalStateException("Error when resetting a game: inventory saved for non-Container");
			((Container) state).getInventory().clear();
			((Container) state).getInventory().addAll(Arrays.asList(changedInvs.get(eMap).get(l)));
			chests++;

		}
		Logging.debug("Reset " + chests + " chests");
		changedBlocks.get(eMap).clear();
		return true;
	}

	private static HungerGame insideGame(Point loc) {
		for (HungerGame game : GameManager.INSTANCE.getRawGames()) {
			if (game.getWorlds().size() <= 0 && game.getCuboids().size() <= 0) return null;
			if (game.getWorlds().contains(loc.getWorld())) return game;
			for (Cuboid c : game.getCuboids()) {
				if (c.isPointWithin(loc)) return game;
			}
		}
		return null;
	}

	public void run() {
	synchronized(toCheck) {
		for (Iterator<Point> it = toCheck.keySet().iterator(); it.hasNext();) {
			Point loc = it.next();
			HungerGame game = insideGame(loc);
			if (game != null) {
				addComponent(game, loc, toCheck.get(loc));
				addInv(game, loc, toCheckInvs.get(loc));
			}
			it.remove();
		}
	}
	}

	private static void addToCheck(Block block) {
		synchronized(toCheck) {
			if (block.getComponent() instanceof Container) {
				toCheckInvs.put(block.getPosition(), (ItemStack[]) ((Container) block.getComponent()).getInventory().toArray());
				return;
			}
			toCheck.put(block.getPosition(), block.getMaterial());
		}
	}

	private static synchronized void addComponent(HungerGame game, Point loc, BlockMaterial state) {
		EquatableWeakReference<HungerGame> eMap = new EquatableWeakReference<HungerGame>(game);
		if (!changedBlocks.containsKey(eMap)) changedBlocks.put(eMap, new HashMap<Point, BlockMaterial>());
		if (changedBlocks.get(eMap).containsKey(loc)) return; // Don't want to erase the original block
		changedBlocks.get(eMap).put(loc, state);
	}

	private static synchronized void addInv(HungerGame game, Point loc, ItemStack[] inv) {
		EquatableWeakReference<HungerGame> eMap = new EquatableWeakReference<HungerGame>(game);
		if (!changedInvs.containsKey(eMap)) changedInvs.put(eMap, new HashMap<Point, ItemStack[]>());
		if (changedInvs.get(eMap).containsKey(loc)) return; // Don't want to erase the original block
		changedInvs.get(eMap).put(loc, inv);
	}

	@EventHandler(order = Order.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (!event.isAir() && event.getInteractedPoint().getBlock().getComponent() instanceof Container) {
			addToCheck(event.getInteractedPoint().getBlock());
		}
	}

	@EventHandler(order = Order.MONITOR)
	public void onExplosion(EntityExplodeEvent event) {
		if (event.isCancelled()) return;
		for (Block b : event.getBlocks()) {
			addToCheck(b);
		}
	}

	@EventHandler(order = Order.MONITOR)
	public void onBlockChange(BlockChangeEvent event) {
		addToCheck(event.getBlock());
	}

	/*//TODO add back in block changes?
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBurn(BlockBurnEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockIgnite(BlockIgniteEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockFade(BlockFadeEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());	    
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLeavesDecay(LeavesDecayEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockSpread(BlockSpreadEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockForm(BlockFormEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent event) {
		addToCheck(event.getBlock(), event.getBlock().getState());
	}
	*/
	/*//TODO add in vehicles/creature resetting
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onVehicleDestroy(VehicleDestroyEvent event) {
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onVehicleMove(VehicleMoveEvent event) {
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
	}
 
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityTeleport(EntityTeleportEvent event) {
	}
	*/
}
