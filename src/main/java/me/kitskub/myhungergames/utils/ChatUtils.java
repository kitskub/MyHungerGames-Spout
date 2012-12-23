package me.kitskub.myhungergames.utils;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.Logging;
import me.kitskub.myhungergames.api.Game;
import me.kitskub.myhungergames.stats.PlayerStat;
import me.kitskub.myhungergames.stats.PlayerStat.Team;

import org.spout.api.Server;
import org.spout.api.Spout;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;

public class ChatUtils {

	// TODO convert to CommandSource
	public static String getPrefix() {
		return String.format("[%s] - ", HungerGames.getInstance().getName());
	}

	public static String getHeadLiner() {
		return String.format("-------------------[%s]--------------------", HungerGames.getInstance().getName());
	}
	
	public static void broadcast(Game game, ChatStyle color, String message) {
		broadcastRaw(game, color, getPrefix() + message);
	}

	public static void broadcast(Game game, String message) {
		broadcast(game, ChatStyle.BRIGHT_GREEN, message);
	}

	public static void broadcast(Game game, String format, Object... args) {
		broadcast(game, String.format(format, args));
	}

	public static void broadcast(Game game, ChatStyle color, String format, Object... args) {
		broadcast(game, color, String.format(format, args));
	}

	/**
	 * Same as broadcasted but with no prefix
	 * @param message
	 * @param color
	 * @param game 
	 */
	public static void broadcastRaw(Game game, ChatStyle color, String message) {
		for (Player player : ((Server) Spout.getEngine()).getOnlinePlayers()) {
			if (Defaults.Config.ALLOW_MINIMAL_MESSAGES.getGlobalBoolean() && !GameManager.INSTANCE.isPlayerSubscribed(player, game)) continue;
			player.sendMessage(color + message);
		}
		message = ChatStyle.strip(message);
		Logging.info(message);
	}
		
	public static void broadcastRaw(Game game, ChatStyle color, String format, Object... args) {
		broadcastRaw(game, color, String.format(format, args));
	}

	public static void broadcastRaw(Game game, String message) {
		broadcastRaw(game, ChatStyle.BRIGHT_GREEN, message);
	}


	public static void sendToTeam(PlayerStat player, String mess) {
		Team team = player.getTeam();
		if (team == null) {
			send(player.getPlayer(), mess);
			return;
		}
		for (PlayerStat p : team.getPlayers()) {
			send(p.getPlayer(), mess);
		}
	}

	public static void sendToTeam(Team team, String mess) {
		if (team == null) {
			return;
		}
		for (PlayerStat p : team.getPlayers()) {
			send(p.getPlayer(), mess);
		}
	}

	public static void send(CommandSource cs, ChatStyle color, String mess) {
		if (cs == null) return;
		cs.sendMessage(color + mess);
	}
	
	public static void send(CommandSource cs, ChatStyle color, String format, Object... args) {
		send(cs, color, String.format(format, args));
	}
			
	public static void send(CommandSource cs, String mess) {
		send(cs, ChatStyle.GRAY, mess);
	}
	
	public static void send(CommandSource cs, String format, Object... args) {
		send(cs, ChatStyle.GRAY, String.format(format, args));
	}
	

	public static void help(CommandSource cs, String mess) {
		send(cs, ChatStyle.GOLD, mess);
	}
	
	public static void help(CommandSource cs, String format, Object... args) {
		help(cs, String.format(format, args));
	}
	
	public static void helpCommand(CommandSource cs, String format, Object... args) {
		help(cs, String.format("- " + format, args));
	}
	
	
	public static void error(CommandSource cs, String mess) {
		send(cs, ChatStyle.RED, mess);
	}

	public static void error(CommandSource cs, String format, Object... args) {
		error(cs, String.format(format, args));
	}	
}
