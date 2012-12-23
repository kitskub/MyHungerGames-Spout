package me.kitskub.myhungergames.commands.admin.remove;

import me.kitskub.myhungergames.Defaults.Commands;
import me.kitskub.myhungergames.Defaults.Perm;
import me.kitskub.myhungergames.commands.PlayerCommand;
import me.kitskub.myhungergames.listeners.SessionListener;
import me.kitskub.myhungergames.utils.ChatUtils;

import org.spout.api.chat.style.ChatStyle;
import org.spout.api.command.Command;
import org.spout.api.command.CommandContext;
import org.spout.api.entity.Player;

public class RemoveSignCommand extends PlayerCommand {

	public RemoveSignCommand() {
		super(Perm.ADMIN_REMOVE_SIGN, Commands.ADMIN_REMOVE_HELP.getCommand(), "sign", 0, 0, "", "remove a sign or an info wall that contains the sign");
	}

	@Override
	public void handlePlayer(Player player, Command command, CommandContext args) {
		SessionListener.addSession(SessionListener.SessionType.SIGN_REMOVER, player, "");
		ChatUtils.send(player, ChatStyle.BRIGHT_GREEN, "Hit a sign to remove it. If you do not hit a sign, nothing will happen.");
	}
}
