package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.commands.PlayerCommand;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class SponsorCommand extends PlayerCommand {

	public SponsorCommand() {
		super(Perm.USER_SPONSOR, "sponsor", USER_COMMAND, 1, 1, "<player>", "sponsor a player an item");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		/*if (args.getRawArgs().isEmpty()) {//TODO add back in
			ChatUtils.send(player, getUsage(), HungerGames.CMD_USER);
			return;
		}

		Player p = Spout.getServer().getPlayer(args.getString(0));
		if (p == null) {
			ChatUtils.error(player, "%s is not online.", args.getString(0));
			return;
		}
		if (GameManager.INSTANCE.getPlayingSession(p) == null) {
			ChatUtils.error(player, "%s is not playing in a game.", p.getName());
			return;
		}
		GameManager.INSTANCE.addSponsor(player, p);*/
	}
}
