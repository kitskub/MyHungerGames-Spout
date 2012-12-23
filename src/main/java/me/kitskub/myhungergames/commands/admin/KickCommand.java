package me.kitskub.myhungergames.commands.admin;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.api.event.PlayerLeaveGameEvent;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.Spout;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;
import org.spout.api.entity.Player;

public class KickCommand extends HGCommand {

	public KickCommand() {
		super(Perm.ADMIN_KICK, "kick", ADMIN_COMMAND, 1, 1, "<player>", "kick a player from a game");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		Player kick = Spout.getEngine().getPlayer(args.getString(0), false);
		if (kick == null) {
		    ChatUtils.error(source, "%s is not online.", args.getString(0));
		    return;
		}
		game = GameManager.INSTANCE.getRawSession(kick);
		if (game == null) {
		    ChatUtils.error(source, "%s is currently not in a game.", kick.getName());
		    return;
		}
		ChatUtils.broadcast(game, "%s has been kicked from the game %s.", source.getName(), game.getName());
		Spout.getEventManager().callEvent(new PlayerLeaveGameEvent(game, kick, PlayerLeaveGameEvent.Type.KICK));
		game.leave(kick, false);
	}
}
