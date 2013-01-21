package me.kitskub.myhungergames.listeners;

import me.kitskub.myhungergames.Defaults.Config;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.games.HungerGame;
import me.kitskub.myhungergames.stats.PlayerStat.Team;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.entity.Player;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.vanilla.plugin.event.cause.DamageCause;
import org.spout.vanilla.plugin.event.entity.EntityDamageEvent;

public class EntityListener implements Listener{
	
	@EventHandler(order = Order.LATEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		// Games
		HungerGame hurtGame = GameManager.INSTANCE.getRawSession(player);
		if (hurtGame != null) {
			if (Config.FORCE_DAMAGE.getBoolean(hurtGame.getSetup())) {
				event.setCancelled(false);
			}
			if (event.getDamageType() == DamageCause.DamageType.ATTACK) {
				double period = Config.GRACE_PERIOD.getDouble(hurtGame.getSetup());
				long startTime = hurtGame.getInitialStartTime();
				if (((System.currentTimeMillis() - startTime) / 1000) < period) {
					event.setCancelled(true);
					if (event.getDamager() instanceof Player) {
						ChatUtils.error((Player) event.getDamager(), "You can't hurt that player during the grace-period!");
					}
				}
				if (!Config.TEAMS_ALLOW_FRIENDLY_DAMAGE.getBoolean(hurtGame.getSetup())) {
					Team hurtTeam = hurtGame.getPlayerStat(player).getTeam();
					if (event.getDamager() instanceof Player && hurtTeam != null && hurtGame.contains((Player) event.getDamager())){
						Team hurterTeam = hurtGame.getPlayerStat((Player) event.getDamager()).getTeam();
						if (hurterTeam != null) {
							if (hurtTeam.getName().equals(hurterTeam.getName())) {
								event.setCancelled(true);
								ChatUtils.error((Player) event.getDamager(), "You can't hurt a player on your team!");
							}
						}
					}
				}
			}
		}

		// Spectators
		if (event.isCancelled()) return;
		if (GameManager.INSTANCE.getSpectating(player) != null) {
			event.setCancelled(true);
		}
	}
	/*//TODO add back in targetting
	@EventHandler(order = Order.LATEST)
	public void onEntityTargetEntity(EntityTargetEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		// Games
		HungerGame game = GameManager.INSTANCE.getRawSession(player);
		if (game != null) {
			if (game.getState() == GameState.STOPPED) {
				if (!Config.STOP_TARGETTING.getBoolean(game.getSetup())) return;
				PlayerStat stat = game.getPlayerStat(player);
				if (stat != null && stat.getState().equals(PlayerState.WAITING)) event.setCancelled(true); 
			}
		}
	}*/
}
