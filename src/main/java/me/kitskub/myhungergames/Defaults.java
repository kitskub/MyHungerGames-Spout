package me.kitskub.myhungergames;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.kitskub.myhungergames.commands.*;
import me.kitskub.myhungergames.commands.admin.*;
import me.kitskub.myhungergames.commands.admin.add.*;
import me.kitskub.myhungergames.commands.admin.remove.*;
import me.kitskub.myhungergames.commands.admin.set.*;
import me.kitskub.myhungergames.commands.user.*;


public class Defaults {
	
    public enum Lang {
	
	JOIN("<player> has joined the game <game>."),
	REJOIN("<player> has rejoined the game <gam"),
	LEAVE("<player> has left the game <game>."),
	QUIT("<player> has quit the game <game>."),
	VOTE("<player> is ready to play <game>. Type /hg vote when you are ready to play."),
	KILL("<killer> killed <killed> in game <game>."),
	DEATH("<player> died in <game>"),
	NO_PERM("You do not have permission."),
	NO_WINNER("You do not have permission."),
	WIN("You do not have permission."),
	ALREADY_COUNTING_DOWN("<game> is already counting down."),
	NOT_ENABLED("<game> is currently not enabled."),
	NOT_RUNNING("<game> is not running."),
	NOT_EXIST("<item> does not exist."),
	RUNNING("<game> is an already running game."),
	IN_GAME("You are in <game>."),
	NOT_IN_GAME("You are not in a game.");
	
	
	private String value;
	
	private Lang(String message) {
	    this.value = message;
	}
	
	public String getMessage(){
	    return value;
	}
    }
    public enum ItemConfig {
	MAX_RANDOM_ITEMS(5);
	    
	private Object value;

	private ItemConfig(Object message) {
	    this.value = message;
	}
	
	public boolean getBoolean(){
	    return (Boolean) value;
	}
	
	public int getInt(){
	    return (Integer) value;
	}
	
	public double getDouble(){
	    return (Double) value;
	}
	
	public String getString(){
	    return (String) value;	    
	}
    }

    public enum Config {
	
	MIN_VOTE(5, "min-vote"),
	MIN_PLAYERS(2, "min-players"),
	UPDATE_DELAY(30, "update-delay"),
	AUTO_JOIN(false, "auto-join"),
	AUTO_JOIN_ALLOWED(true, "auto-join-allowed"),
	DEFAULT_TIME(10, "default-time"),
	ALLOW_REJOIN(true, "allow-rejoin"),
	ALLOW_JOIN_DURING_GAME(false, "allow-join-during-game"),
	WINNER_KEEPS_ITEMS(true, "winner-keeps-items"),
	SPAWNPOINT_ON_DEATH(false, "spawnpoint-on-death"),
	DEFAULT_GAME("Test", "default-game"),
	LIVES(1, "lives"),
	CLEAR_INV(true, "clear-inv"),
	REQUIRE_INV_CLEAR(false, "require-inv-clear"),
	ALL_VOTE(false, "all-vote"),
	AUTO_VOTE(false, "auto-vote"),
	CAN_PLACE_BLOCK(false, "can-place-block"),
	CAN_BREAK_BLOCK(false, "can-break-block"),
	CAN_INTERACT_BLOCK(false, "can-interact-block"),
	SPECIAL_BLOCKS_PLACE(new ArrayList<String>(), "special-blocks-place"),
	SPECIAL_BLOCKS_BREAK(new ArrayList<String>(), "special-blocks-break"),
	SPECIAL_BLOCKS_INTERACT(new ArrayList<String>(), "special-blocks-interact"),
	CAN_TELEPORT(false, "can-teleport"),
	USE_COMMAND(false, "use-command"),
	SPECIAL_COMMANDS(new ArrayList<String>(), "special-commands"),
	AUTO_ADD(true, "auto-add"),
	RESET_CHANGES(true, "reset-changes"),
	FORCE_SURVIVAL(true, "force-survival"),
	FREEZE_PLAYERS(true, "freeze-players"),
	FORCE_DAMAGE(false, "force-damage"),
	FORCE_INTERNAL(false, "force-internal"),
	ISOLATE_PLAYER_CHAT(true, "isolate-player-chat"),
	CHAT_DISTANCE(15, "chat-distance"),
	ALLOW_MINIMAL_MESSAGES(true, "allow-minimal-messages"),
	REMOVE_ITEMS(true, "remove-items"),
	SPECTATOR_SPONSOR_PERIOD(0, "spectator-sponsor-period"),
	WEBSTATS_IP("http://myhungergames.fragzone.org/dbproxy.php", "webstats-ip"),
	DEATH_CANNON(1, "death-cannon"),
	MAX_GAME_DURATION(0, "max-game-duration"),
	USE_SPAWN(true, "use-spawn"),
	GRACE_PERIOD(0d, "grace-period"),
	TIMEOUT(300, "timeout"),
	TAKE_LIFE_ON_LEAVE(true, "take-life-on-leave"),
	START_TIMER(0, "start-timer"),
	STOP_TARGETTING(true, "stop-targetting"),
	HIDE_PLAYERS(true, "hide-players"),
	SHOW_DEATH_MESSAGES(1, "show-death-messages"),
	TEAMS_ALLOW_TEAMS(true, "teams.allow-teams"),
	TEAMS_ALLOW_FRIENDLY_DAMAGE(false, "teams.allow-friendly-damage"),
	DISABLE_FLY(true, "disable-fly");
	
	
	private Object value;
	private String option;

	private Config(Object message, String option) {
	    this.value = message;
	    this.option = option;
	}

	/*
	public boolean getBoolean(){
	    return (Boolean) value;
	}

	public int getInt(){
	    return (Integer) value;
	}

	public double getDouble(){
	    return (Double) value;
	}

	public String getString(){
	    return (String) value;	    
	}*/

	public boolean getGlobalBoolean(){
		return Files.CONFIG.getConfig().getNode("global." + option).getBoolean((Boolean) value);
	}

	public int getGlobalInt(){
		return Files.CONFIG.getConfig().getNode("global." + option).getInt((Integer) value);
	}

	public double getGlobalDouble(){
		return Files.CONFIG.getConfig().getNode("global." + option).getDouble((Double) value);
	}

	public String getGlobalString(){
		return Files.CONFIG.getConfig().getNode("global." + option).getString((String) value);
	}

	@SuppressWarnings("unchecked")
	public List<String> getGlobalStringList() {
		return Files.CONFIG.getConfig().getNode("global." + option).getStringList((List<String>) value);
	}
	/** 
	 * For safe recursiveness 
	 * return boolean if found, null if not
	 */
	private Boolean getBoolean(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.CONFIG.getConfig().hasChild("setups." + setup + "." + option)) {
			return Files.CONFIG.getConfig().getNode("setups." + setup + "." + option).getBoolean();
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			Boolean b = getBoolean(parent, checked);
			if (b != null) return b;
		}
		return null;
	}
	public boolean getBoolean(String setup) {
		Boolean b = getBoolean(setup, new HashSet<String>());
		return b == null ? getGlobalBoolean() : b;
	}
	
	/** 
	 * For safe recursiveness 
	 * return String if found, null if not
	 */	
	private String getString(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.CONFIG.getConfig().hasChild("setups." + setup + "." + option)) {
			return Files.CONFIG.getConfig().getNode("setups." + setup + "." + option).getString();
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			String s = getString(parent, checked);
			if (s != null) return s;
		}
		return null;
	}
	public String getString(String setup) {
		String s = getString(setup, new HashSet<String>());
		return s == null ? getGlobalString() : s;
	}
	
	/** 
	 * For safe recursiveness 
	 * return Integer if found, null if not
	 */
	private Integer getInt(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.CONFIG.getConfig().hasChild("setups." + setup + "." + option)) {
			return Files.CONFIG.getConfig().getNode("setups." + setup + "." + option).getInt();
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			Integer i = getInt(parent, checked);
			if (i != null) return i;
		}
		return null;
	}
	public int getInt(String setup) {
		Integer i = getInt(setup, new HashSet<String>());
		return i == null ? getGlobalInt() : i;
	}
	
	/** 
	 * For safe recursiveness 
	 * return Integer if found, null if not
	 */
	private Double getDouble(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		if (Files.CONFIG.getConfig().hasChild("setups." + setup + "." + option)) {
			return Files.CONFIG.getConfig().getNode("setups." + setup + "." + option).getDouble();
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			Double d = getDouble(parent, checked);
			if (d != null) return d;
		}
		return null;
	}
	public double getDouble(String setup) {
		Double d = getDouble(setup, new HashSet<String>());
		return d == null ? getGlobalDouble() : d;
	}
	
	/** 
	 * For safe recursiveness 
	 * return List if found, null if not
	 */
	private List<String> getStringList(String setup, Set<String> checked) {
		if (checked.contains(setup)) return null;
		List<String> strings = new ArrayList<String>();
		if (Files.CONFIG.getConfig().hasChild("setups." + setup + "." + option)) {
			strings.addAll(Files.CONFIG.getConfig().getNode("setups." + setup + "." + option).getStringList());
		}
		checked.add(setup);
		for (String parent : Files.CONFIG.getConfig().getNode("setups." + setup + ".inherits").getStringList()) {
			List<String> list = getStringList(parent, checked);
			if (list != null) strings.addAll(list);
		}
		return strings;
	}
	
	/** returns combination of all lists, including global
	 * @param setup
	 * @return  
	 */
	public List<String> getStringList(String setup) {
		List<String> list = getStringList(setup, new HashSet<String>());
		return list == null ? getGlobalStringList() : list;
	}
    }

    public enum Perm {
		ALL("hungergame.*", null, "gives the player all permissions"),
		ADMIN("hungergame.admin.*", ALL, "gives the player all admin permissions"),
		ADMIN_ALLOW_FLIGHT("hungergame.admin.allowflight", ADMIN, "allows the player to fly in game"),
		ADMIN_ADD_CUBOID("hungergame.add.cuboid", ADMIN),
		ADMIN_ADD_CHEST("hungergame.add.chest", ADMIN),
		ADMIN_ADD_CHEST_LOOT("hungergame.add.chestloot", ADMIN),
		ADMIN_ADD_GAME("hungergame.add.game", ADMIN),
		ADMIN_ADD_GAME_SIGN("hungergame.add.gamesign", ADMIN),
		ADMIN_ADD_HELP("hungergame.add.help", ADMIN, "allows the player to view add help page"),
		ADMIN_ADD_INFO_WALL("hungergame.add.infowall", ADMIN),
		ADMIN_ADD_ITEMSET("hungergame.add.itemset", ADMIN),
		ADMIN_ADD_JOIN_SIGN("hungergame.add.joinsign", ADMIN),
		ADMIN_ADD_REWARD("hungergame.add.reward", ADMIN),
		ADMIN_ADD_SPAWNPOINT("hungergame.add.spawnpoint", ADMIN),
		ADMIN_ADD_SPONSOR_LOOT("hungergame.add.sponsorloot", ADMIN),
		ADMIN_ADD_WORLD("hungergame.add.world", ADMIN),
		ADMIN_CHAT("hungergame.admin.chat", ADMIN, "Allows an admin to chat to a game by typing \"hg\" in front of their message"),
		ADMIN_CREATE_SIGN("hungergame.create.sign", ADMIN, "Allows player to create a sign listener"),
		ADMIN_CREATE_SIGN_GAMEEND("hungergame.create.sign.gameend", ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_GAMEPAUSE("hungergame.create.sign.gamepause", ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_GAMESTART("hungergame.create.sign.gamestart", ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERJOIN("hungergame.create.sign.playerjoin", ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERKICK("hungergame.create.sign.playerkick", ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERKILL("hungergame.create.sign.playerkill", ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERLEAVE("hungergame.create.sign.playerleave", ADMIN_CREATE_SIGN),
		ADMIN_CREATE_SIGN_PLAYERQUIT("hungergame.create.sign.playerquit", ADMIN_CREATE_SIGN),
		ADMIN_REMOVE_SPAWNPOINT("hungergame.remove.spawnpoint", ADMIN),
		ADMIN_REMOVE_CHEST("hungergame.remove.chest", ADMIN),
		ADMIN_REMOVE_GAME("hungergame.remove.game", ADMIN),
		ADMIN_REMOVE_HELP("hungergame.remove.help", ADMIN, "allows the player to view remove help page"),
		ADMIN_REMOVE_ITEMSET("hungergame.remove.itemset", ADMIN),
		ADMIN_REMOVE_SIGN("hungergame.remove.sign", ADMIN),
		ADMIN_SET_ENABLED("hungergame.set.enabled", ADMIN),
		ADMIN_SET_FIXED_CHEST("hungergame.set.fixedchest", ADMIN),
		ADMIN_SET_HELP("hungergame.set.help", ADMIN, "allows the player to view set help page"),
		ADMIN_SET_SPAWN("hungergame.set.spawn", ADMIN),
		ADMIN_FORCE_CLEAR("hungergame.game.forceclear", ADMIN),
		ADMIN_STOP("hungergame.game.stop", ADMIN),
		ADMIN_START("hungergame.game.start", ADMIN),
		ADMIN_PAUSE("hungergame.game.pause", ADMIN),
		ADMIN_RESUME("hungergame.game.resume", ADMIN),
		ADMIN_RELOAD("hungergame.admin.reload", ADMIN),
		ADMIN_KICK("hungergame.admin.kick", ADMIN),
		ADMIN_KILL("hungergame.admin.kill", ADMIN),
		ADMIN_HELP("hungergame.admin.help", ADMIN, "allows a player to view admin commands"),
		ADMIN_RESTOCK("hungergame.admin.restock", ADMIN),
		USER("hungergame.user.*", ALL),
		USER_ABOUT("hungergame.user.about", USER),
		USER_AUTO_SUBSCRIBE("hungergame.user.autosubscribe", null, "whether a user autosubscribes to a game or not; is not inherited from *"),
		USER_AUTO_JOIN_ALLOWED("hungergame.user.autojoinallowed", USER, "whether a user can autojoin games; can also have hungergame.user.autojoinallowed.<game>"),
		USER_BACK("hungergame.user.back", USER),
		USER_JOIN("hungergame.user.join", USER),
		USER_KIT("hungergame.user.kit", null, "whether a user gets all kits on start; can also add specific kits with hungergame.user.kit.<kit>"),
		USER_LEAVE("hungergame.user.leave", USER),
		USER_LIST("hungergame.user.list", USER),
		USER_REJOIN("hungergame.user.rejoin", USER),
		USER_SEARCH("hungergame.user.search", USER),
		USER_SPECTATE("hungergame.user.spectate", USER),
		USER_SPONSOR("hungergame.user.sponsor", USER),
		USER_SUBSCRIBE("hungergame.user.subscribe", USER),
		USER_TEAM("hungergame.user.team", USER),
		USER_VOTE("hungergame.user.vote", USER),
		USER_STAT("hungergame.user.stat", USER),
		USER_HELP("hungergame.user.help", USER, "allows a player to view user commands"),
		USER_QUIT("hungergame.user.quit", USER);

	public final String value;
	private Perm parent;
	private String info;
	
	private Perm(String permission, Perm parent) {
		this.value = permission;
		this.parent = parent;
	}
	
	private Perm(String permission, Perm parent, String info) {
		this.value = permission;
		this.parent = parent;
		this.info = info;
	}
	
	public final String get(){
		return value;
	}
	
	public Perm getParent() {
		return parent;
	}
	
	public String getInfo() {
		return info;
	}
    }
    
    public enum Commands {
	ADMIN_ADD_HELP(new AddHelp()),
	ADMIN_ADD_CUBOID(new AddCuboidCommand()),
	ADMIN_ADD_CHEST(new AddChestCommand()),
	ADMIN_ADD_CHEST_LOOT(new AddChestLootCommand()),
	ADMIN_ADD_GAME(new AddGameCommand()),
	ADMIN_ADD_GAME_SIGN(new AddGameSignCommand()),
	ADMIN_ADD_INFO_WALL(new AddInfoWallCommand()),
	ADMIN_ADD_ITEMSET(new AddItemSetCommand()),
	ADMIN_ADD_JOIN_SIGN(new AddJoinSignCommand()),
	ADMIN_ADD_REWARD(new AddRewardCommand()),
	ADMIN_ADD_SPAWNPOINT(new AddSpawnPointCommand()),
	ADMIN_ADD_SPONSOR_LOOT(new AddSponsorLootCommand()),
	ADMIN_ADD_WORLD(new AddWorldCommand()),
	ADMIN_REMOVE_HELP(new RemoveHelp()),
	ADMIN_REMOVE_CHEST(new RemoveChestCommand()),
	ADMIN_REMOVE_GAME(new RemoveGameCommand()),
	ADMIN_REMOVE_ITEMSET(new RemoveItemSetCommand()),
	ADMIN_REMOVE_SIGN(new RemoveSignCommand()),
	ADMIN_REMOVE_SPAWNPOINT(new RemoveSpawnPointCommand()),
	ADMIN_SET_HELP(new SetHelp()),
	ADMIN_SET_ENABLED(new SetEnabledCommand()),
	ADMIN_SET_FIXED_CHEST(new SetFixedChestCommand()),
	ADMIN_SET_SPAWN(new SetSpawnCommand()),
	ADMIN_FORCE_CLEAR(new ForceClearCommand()),
	ADMIN_START(new StartCommand()),
	ADMIN_STOP(new StopCommand()),
	ADMIN_PAUSE(new PauseCommand()),
	ADMIN_RESUME(new ResumeCommand()),
	ADMIN_RELOAD(new ReloadCommand()),
	ADMIN_KICK(new KickCommand()),
	ADMIN_KILL(new KillCommand()),
	ADMIN_RESTOCK(new RestockCommand()),
	USER_ABOUT(new AboutCommand()),
	USER_BACK(new BackCommand()),
	USER_JOIN(new JoinCommand()),
	USER_LEAVE(new LeaveCommand()),
	USER_LIST(new ListCommand()),
	USER_QUIT(new QuitCommand()),
	USER_REJOIN(new RejoinCommand()),
	USER_SEARCH(new SearchCommand()),
	USER_SPECTATE(new SpectateCommand()),
	USER_SPONSOR(new SponsorCommand()),
	USER_STAT(new StatCommand()),
	USER_SUBSCRIBE(new SubscribeCommand()),
	USER_TEAM(new TeamCommand()),
	USER_VOTE(new VoteCommand());
	
	private HGCommand command;
	
	private Commands(HGCommand command) {
		this.command = command;
	}
	
	public HGCommand getCommand() {
		return command;
	}
	
	public static void init() {} // Just so the class gets loaded
    }

}