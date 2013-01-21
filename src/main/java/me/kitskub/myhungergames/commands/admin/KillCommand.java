package me.kitskub.myhungergames.commands.admin;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.Spout;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;
import org.spout.vanilla.plugin.component.misc.HealthComponent;
import org.spout.vanilla.plugin.event.cause.HealthChangeCause;


public class KillCommand extends HGCommand {

	public KillCommand() {
		super(Perm.ADMIN_KILL, "kill", ADMIN_COMMAND, 1, 1, "<player>", "kills a player in a game");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		Player kill = Spout.getEngine().getPlayer(args.getString(0), false);
		if (kill == null) {
		    ChatUtils.error(source, "%s is not online.", args.get(0));
		    return;
		}
		game = GameManager.INSTANCE.getRawSession(kill);
		if (game == null) {
		    ChatUtils.error(source, "%s is currently not in a game.", kill.getName());
		    return;
		}
		ChatUtils.broadcast(game, "%s has been killed by an admin.", kill.getName());
		kill.get(HealthComponent.class).setHealth(0, HealthChangeCause.PLUGIN);
	}
}
