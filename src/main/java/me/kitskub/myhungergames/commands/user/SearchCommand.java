package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.stats.SQLStat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;

public class SearchCommand extends HGCommand {
	private final List<FutureTask<SQLStat>> searches = new ArrayList<FutureTask<SQLStat>>();

	public SearchCommand() {
		super(Perm.USER_SEARCH, "search", USER_COMMAND, 1, 1, "<player>", "searches for a player's stat and prints out the info");
	}

	@Override
	public void handle(final CommandSource source, final Command command, final CommandContext args) {
		/*if (args.getRawArgs().isEmpty()) {//TODO add back in search command
			ChatUtils.error(cs, "Must have a player to search for!");
			return;
		}
		FutureTask<SQLStat> f = new FutureTask<SQLStat>(new Callable<SQLStat>() {
			public SQLStat call() throws Exception {
				return StatHandler.getStat(args.getString(0));
			}
		});
		synchronized(searches) {
			searches.add(f);
			Spout.getScheduler().runTaskAsynchronously(HungerGames.getInstance(), f);
			Spout.getScheduler().runTaskTimer(HungerGames.getInstance(), new SpoutRunnable() {
				public void run() {
					synchronized(searches) {
						if (searches.isEmpty()) {
							cancel();
							return;
						}
						Iterator<FutureTask<SQLStat>> iterator = searches.iterator();
						while (iterator.hasNext()) {
							FutureTask<SQLStat> next = iterator.next();
							if (next.isCancelled()) {
								cancel();
								return;
							}
							if (!next.isDone()) continue;
							iterator.remove();
							SQLStat stat;
							try {
								stat = next.get();
							} catch (Exception ex) {
								Logging.debug("Exception in search runnable");
								cancel();
								return;
							}
							if (stat == null) {
								ChatUtils.send(cs, "Could not find stat for: %s", args.getString(0));
							}
							ChatUtils.send(cs, ChatUtils.getHeadLiner());
							ChatUtils.send(cs, "Player stat for: %s", args.getString(0));
							ChatUtils.send(cs, "%s has a global rank of %s",args.getString(0), stat.rank);
							ChatUtils.send(cs, "%s has played %s games for a total of %s", args.getString(0), stat.totalGames, stat.totalTime);
							ChatUtils.send(cs, "%s has had %s wins, %s deaths, and %s kills", args.getString(0), stat.wins, stat.deaths, stat.kills);
						}
					}
				}
			}, 0, 5);
			
		}*/
	}
}
