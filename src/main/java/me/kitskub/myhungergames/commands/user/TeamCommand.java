package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.GameManager;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.stats.PlayerStat;
import me.kitskub.myhungergames.stats.PlayerStat.Team;
import me.kitskub.myhungergames.utils.ChatUtils;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class TeamCommand extends PlayerCommand {

	public TeamCommand() {
		super(Perm.USER_TEAM, "team", USER_COMMAND, 1, 1, "<team>", "joins the team specified (may create a new one if there is nobody in it) or leaves current team");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		game = GameManager.INSTANCE.getRawSession(player);
		if (game == null) {
			ChatUtils.error(player, "You are not in a game!");
			return;
		}
		if (!Defaults.Config.TEAMS_ALLOW_TEAMS.getBoolean(game.getSetup())) {
			ChatUtils.error(player, "Teams are not enabled for this game!");
			return;
		}
		String team = null;
		if (args.getRawArgs().size() >= 1) {
			team = args.getString(0);
		}
		PlayerStat stat = game.getPlayerStat(player);
		boolean require = false;
		if (stat.getTeam() != null) {
			stat.setTeam(null);
		} else {
			require = true;
		}
		if (team == null) {
			if (require) {
				ChatUtils.error(player, "Must specify a team!");
			}
		} else {
			game.getPlayerStat(player).setTeam(Team.get(team));
		}
	}
}
