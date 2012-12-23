package me.kitskub.myhungergames.commands.user;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.HungerGames;
import me.kitskub.myhungergames.commands.HGCommand;
import me.kitskub.myhungergames.utils.ChatUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.command.CommandSource;



public class AboutCommand extends HGCommand {

	public AboutCommand() {
		super(Perm.USER_ABOUT, "about", HGCommand.USER_COMMAND, 0, 0, "", "gives basic info about MyHungerGames");
	}

	@Override
	public void handle(CommandSource source, Command command, CommandContext args) {
		ChatUtils.send(source, ChatUtils.getHeadLiner());
		ChatUtils.send(source, "Original Author - Randude14");
		ChatUtils.send(source, "Active Developer - kitskub");
		ChatUtils.send(source, "Sponsor - http://treepuncher.com");
		if (source.getName().equals("kitskub") && args.getRawArgs().size() == 1) {
			if (!args.getString(0).equals("true")) return;
			File commandperms = new File(HungerGames.getInstance().getDataFolder(), "commandperms.txt");
			try {
				commandperms.createNewFile();
				FileWriter writer;
				writer = new FileWriter(commandperms);
				writer.write("== <<color red>>Commands<</color>> ==\n");
				Map<Perm, HGCommand> map = new EnumMap<Perm, HGCommand>(Perm.class);
				for (Commands c : Commands.values()) {
					HGCommand hgC = c.getCommand();
					map.put(hgC.getPerm(), hgC);
					StringBuilder builder = new StringBuilder();
					builder.append("* **");
					builder.append(hgC.getUsage());
					builder.append("** - ");
					builder.append(hgC.getInfo());
					builder.append("\n");
					writer.write(builder.toString());
				}
				writer.write("== <<color red>>Permissions<</color>> ==\n");
				for (Perm permission : Perm.values()) {
					StringBuilder builder = new StringBuilder();
					builder.append("* **");
					builder.append(permission.get());
					builder.append("**");
					String info = "";
					if (map.containsKey(permission)) {
						info = " - Allows " + map.get(permission).getUsage();
					} else if (permission.getInfo() != null) {
						info = " - " + permission.getInfo();
					}
					builder.append(info);
					builder.append("\n");
					writer.write(builder.toString());
				}
				writer.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex.getMessage());
			}
				
		}
	}
}
