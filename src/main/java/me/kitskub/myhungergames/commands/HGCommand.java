package me.kitskub.myhungergames.commands;

import me.kitskub.myhungergames.games.HungerGame;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.HungerGames;

import org.spout.api.command.*;
import org.spout.api.exception.CommandException;


/**
 * Represents a subcommand
 *
 */
public abstract class HGCommand extends SimpleCommand implements CommandExecutor {
	protected HungerGame game = null;
	protected final Perm perm;
	protected final String type;
	protected String desc;
	
	public static final String ADMIN_COMMAND = HungerGames.CMD_ADMIN;
	public static final String USER_COMMAND = HungerGames.CMD_USER;
	
	public HGCommand(Perm perm, HGCommand parent, String name, int min, int max, String usage, String desc) {
		super(HungerGames.getInstance(), name);
		setParent(parent);
		this.perm = perm;
		type = parent.type;
		this.desc = desc;
		set(min, max, usage);
		setExecutor(this);
	}	
	
	public HGCommand(Perm perm, String name, String type, int min, int max, String usage, String desc) {
		super(HungerGames.getInstance(), name);
		this.perm = perm;
		this.type = type;
		this.desc = desc;
		set(min, max, usage);
		if (type.equalsIgnoreCase(ADMIN_COMMAND)) {
			setParent(HungerGames.ADMIN);
		}
		else {
			setParent(HungerGames.USER);
		}
		setExecutor(this);
	}
	
	public final void set(int min, int max, String usage) {
		this.minArgLength = min;
		this.maxArgLength = max;
		this.usage = usage;
	}

	public abstract void handle(CommandSource source, Command command, CommandContext args);

	@Override
	public void processCommand(CommandSource source, Command command, CommandContext args) throws CommandException {
		if (!HungerGames.checkPermission(source, perm)) return;
		handle(source, command, args);
		save();
	}

	public boolean save() {
		if(game != null) {
		    GameManager.INSTANCE.saveGame(game);
		    return true;
		}
		else{
		    GameManager.INSTANCE.saveGames();
		    return false;
		}
	}
	
	public Perm getPerm() {
		return perm;
	}
	
	public String getInfo() {
		return desc;
	}
}
