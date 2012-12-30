package me.kitskub.myhungergames;

import java.util.Random;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.games.PlayerQueueHandler;
import me.kitskub.myhungergames.games.TimedGameRunnable;
import me.kitskub.myhungergames.listeners.*;
import me.kitskub.myhungergames.reset.ResetHandler;
import me.kitskub.myhungergames.stats.TimeListener;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.Spout;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandSource;
import org.spout.api.command.SimpleCommand;
import org.spout.api.entity.Player;
import org.spout.api.event.EventManager;
import org.spout.api.exception.ConfigurationException;
import org.spout.api.plugin.CommonPlugin;
import org.spout.api.plugin.services.EconomyService;

public class HungerGames extends CommonPlugin {
	public static final String CMD_ADMIN = "hga", CMD_USER = "hg";
	private static HungerGames instance;
	public static SimpleCommand ADMIN;
	public static SimpleCommand USER;
	public static final Random RANDOM = new Random();
	private static EconomyService econ = EconomyService.getEconomy();

	@Override
	public void onEnable() {
		instance = this;
		registerCommands();
		Files.loadAll();
		registerEvents();
		loadResetter();
		callTasks();
		GameManager.INSTANCE.loadGames();
		LobbyListener.load();
		Logging.info("%s games loaded.", GameManager.INSTANCE.getRawGames().size());
		Spout.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				try {
					SpoutMetrics metrics = new SpoutMetrics(instance);
					metrics.start();
				} catch (ConfigurationException e) {
					// Fail silently
				}
			}
		});
		Logging.info("Enabled.");
	}

	@Override
	public void onDisable() {
	}
	
	public static void reload() {
		Files.loadAll();
		GameManager.INSTANCE.loadGames();
		SignListener.loadSigns();
	}

	private static void registerEvents() {
		EventManager em = Spout.getEventManager();
		em.registerEvents(new ActivityListener(), instance);
		em.registerEvents(new BlockListener(), instance);
		em.registerEvents(new CommandListener(), instance);
		em.registerEvents(new PlayerListener(), instance);
		em.registerEvents(new EntityListener(), instance);
		em.registerEvents(new SignListener(), instance);
		em.registerEvents(new SessionListener(), instance);
		em.registerEvents(new ChatListener(), instance);
		em.registerEvents(new TeleportListener(), instance);
		em.registerEvents(new TimedGameRunnable(), instance);
		em.registerEvents(new TimeListener(), instance);
		em.registerEvents(new LobbyListener(), instance);
		if (Defaults.Config.AUTO_JOIN_ALLOWED.getGlobalBoolean()) em.registerEvents(new PlayerQueueHandler(), instance);
	}

	private static void loadResetter() {
	    if (Defaults.Config.FORCE_INTERNAL.getGlobalBoolean()) {
		    Logging.info("Forcing internal resetter.");
		    ResetHandler.setRessetter(ResetHandler.Resetters.INTERNAL);
		    return;
	    }
	    Logging.info("No logging plugins installed, using internal resetter.");
	    ResetHandler.setRessetter(ResetHandler.Resetters.INTERNAL);
	}

	private void callTasks() {
		/*Spout.getScheduler().scheduleSyncRepeatingTask(this,//TODO add back in updater
			new Runnable() {
			public void run() {
				Updater updater = new Updater(HungerGames.getInstance(), "myhungergames", HungerGames.getInstance().getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
				if (updater.getResult().equals(Updater.UpdateResult.UPDATE_AVAILABLE))
					Logging.warning("There is a new version: %s (You are running %s)", updater.getLatestVersionString(), getDescription().getVersion());
				}
		}, 0L, Defaults.Config.UPDATE_DELAY.getGlobalInt() * 20L * 60L, TaskPriority.NORMAL);*/
	}

	private void registerCommands() {
		ADMIN = Spout.getEngine().getRootCommand().addSubCommand(instance, CMD_ADMIN);
		USER = Spout.getEngine().getRootCommand().addSubCommand(instance, CMD_USER);
		Commands.init();
	}
	
	public static HungerGames getInstance() {
		return instance;
	}

	public static boolean checkPermission(CommandSource source, Perm perm) {
		if (!HungerGames.hasPermission(source, perm)) {
			source.sendMessage(ChatStyle.RED + Lang.getNoPerm());
			return false;
		}
		return true;
	}

	public static boolean hasPermission(CommandSource cs, Perm perm) {
		if (perm == null) return true;
		if (cs.hasPermission(perm.get())) {
			return true;
		}
		if (perm.getParent() != null) {
			return hasPermission(cs, perm.getParent());
		}
		return false;
	}

	public static void playerLeftServer(Player player) {
		SessionListener.removePlayer(player);
	}
	
		public static boolean isEconomyEnabled() {
		return econ != null;
	}

	public static void withdraw(Player player, double amount) {
		if (!isEconomyEnabled()) {
			ChatUtils.error(player, "Economy use has been disabled.");
			return;
		}
		econ.withdraw(player.getName(), amount);
	}

	public static void deposit(Player player, double amount) {
		if (!isEconomyEnabled()) {
			ChatUtils.error(player, "Economy use has been disabled.");
			return;
		}
		econ.deposit(player.getName(), amount);
	}

	public static boolean hasEnough(Player player, double amount) {
		if (!isEconomyEnabled()) {
			ChatUtils.error(player, "Economy use has been disabled.");
			return false;
		}
		return econ.has(player.getName(), amount);
	}
}
