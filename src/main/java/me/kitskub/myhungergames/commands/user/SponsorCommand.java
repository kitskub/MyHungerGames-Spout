package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class SponsorCommand extends PlayerCommand {

	public SponsorCommand() {
		super(Perm.USER_SPONSOR, "sponsor", USER_COMMAND, 1, 1, "<player>", "sponsor a player an item");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		Player p = args.getPlayer(0, false);
		if (p == null) {
			ChatUtils.error(player, "%s is not online.", args.getString(0));
			return;
		}
		if (GameManager.INSTANCE.getPlayingSession(p) == null) {
			ChatUtils.error(player, "%s is not playing in a game.", p.getName());
			return;
		}
		GameManager.INSTANCE.addSponsor(player, p);
	}
}
