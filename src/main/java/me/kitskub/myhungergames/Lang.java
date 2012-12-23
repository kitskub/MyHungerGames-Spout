package me.kitskub.myhungergames;

import static me.kitskub.myhungergames.Defaults.Lang.*;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Lang {
		
	private static String getGlobalOnly(String config, String def) {
		return Files.LANG.getConfig().getNode("global-only." + config).getString(def);
	}
		
	private static String getGlobal(String config, String def) {
		return Files.LANG.getConfig().getNode("global." + config).getString(def);
	}
	
	private static List<String> getGlobalStringList(String config, List<String> def) {
		if (Files.LANG.getConfig().hasNode("global." + config)) {
			return Files.LANG.getConfig().getNode("global." + config).getStringList();
		}
		return def;
	}
	
	/** 
	 * For safe recursiveness 
	 * return String if found, null if not
	 */	
	private static String getString(String config, String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.LANG.getConfig().hasNode("setups." + setup + "." + config)) {
			return Files.LANG.getConfig().getNode("setups." + setup + "." + config).getString();
		}
		checked.add(setup);
		for (String parent : Files.LANG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			String s = getString(config, parent, checked);
			if (s != null) return s;
		}
		return null;
	}
	private static String getString(String config, String setup, String def) {
		String s = getString(config, setup, new HashSet<String>());
		return s == null ? def : s;
	}
	
	/** 
	 * For safe recursiveness 
	 * return String if found, null if not
	 */	
	private static List<String> getStringList(String config, String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.LANG.getConfig().hasNode("setups." + setup + "." + config)) {
			return Files.LANG.getConfig().getNode("setups." + setup + "." + config).getStringList();
		}
		checked.add(setup);
		for (String parent : Files.LANG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			List<String> s = getStringList(config, parent, checked);
			if (s != null) return s;
		}
		return null;
	}

	private static List<String> getStringList(String config, String setup, List<String> def) {
		List<String> s = getStringList(config, setup, new HashSet<String>());
		return s == null ? def : s;
	}
	
	// Global only
	public static String getNoPerm() {
		return getGlobalOnly("no-perm", NO_PERM.getMessage());
	}

	public static String getNotExist() {
		return getGlobalOnly("not-exist", NOT_EXIST.getMessage());
	}
	
	// Global
	public static String getGlobalJoinMessage() {
		return getGlobal("join-message", JOIN.getMessage());
	}
	
	public static String getGlobalRejoinMessage() {
		return getGlobal("rejoin-message", REJOIN.getMessage());
	}
	
	public static String getGlobalLeaveMessage() {
		return getGlobal("leave-message", LEAVE.getMessage());
	}
	
	public static String getGlobalQuitMessage() {
		return getGlobal("quit-message", QUIT.getMessage());
	}
	
	public static String getGlobalKillMessage() {
		return getGlobal("kill-message", KILL.getMessage());
	}
	
	public static String getGlobalVoteMessage() {
		return getGlobal("vote-message", VOTE.getMessage());
	}
	
	public static String getGlobalNoWinner() {
		return getGlobal("no-winner", NO_WINNER.getMessage());
	}
	
	public static String getGlobalWin() {
		return getGlobal("win", WIN.getMessage());
	}
	
	public static String getGlobalAlreadyCountingDown() {
		return getGlobal("already-counting-down", ALREADY_COUNTING_DOWN.getMessage());
	}
	
	public static String getGlobalNotEnabled() {
		return getGlobal("not-enabled", NOT_ENABLED.getMessage());
	}
	
	public static String getGlobalNotRunning() {
		return getGlobal("not-running", NOT_RUNNING.getMessage());
	}
	
	public static String getGlobalRunning() {
		return getGlobal("running", RUNNING.getMessage());
	}
	
	public static String getGlobalInGame() {
		return getGlobal("in-game", IN_GAME.getMessage());
	}
	
	public static String getGlobalNotInGame() {
		return getGlobal("not-in-game", NOT_IN_GAME.getMessage());
	}
	
	public static List<String> getGlobalDeathMessages() {
		return getGlobalStringList("death-messages", new ArrayList<String>());
	}
	
	// Setups
	public static String getJoinMessage(String setup) {
		return getString("join-message", setup, getGlobalJoinMessage());
	}
	
	public static String getRejoinMessage(String setup) {
		return getString("rejoin-message", setup, getGlobalRejoinMessage());
	}
	
	public static String getLeaveMessage(String setup) {
		return getString("leave-message", setup, getGlobalLeaveMessage());
	}
	
	public static String getQuitMessage(String setup) {
		return getString("quit-message", setup, getGlobalQuitMessage());
	}
	
	public static String getKillMessage(String setup) {
		return getString("kill-message", setup, getGlobalKillMessage());
	}
	
	public static String getVoteMessage(String setup) {
		return getString("vote-message", setup, getGlobalVoteMessage());
	}
	
	public static String getNoWinner(String setup) {
		return getString("no-winner", setup, getGlobalNoWinner());
	}
	
	public static String getWin(String setup) {
		return getString("win", setup, getGlobalWin());
	}
	
	public static String getAlreadyCountingDown(String setup) {
		return getString("already-counting-down", setup, getGlobalAlreadyCountingDown());
	}
	
	public static String getNotEnabled(String setup) {
		return getString("not-enabled", setup, getGlobalNotEnabled());
	}
	
	public static String getNotRunning(String setup) {
		return getString("not-running", setup, getGlobalNotRunning());
	}
	
	public static String getRunning(String setup) {
		return getString("running", setup, getGlobalRunning());
	}
	
	public static String getInGame(String setup) {
		return getString("in-game", setup, getGlobalInGame());
	}
	
	public static String getNotInGame(String setup) {
		return getString("not-in-game", setup, getGlobalNotInGame());
	}
	
	public static List<String> getDeathMessages(String setup) {
		return getStringList("death-messages", setup, getGlobalDeathMessages());
	}
}
