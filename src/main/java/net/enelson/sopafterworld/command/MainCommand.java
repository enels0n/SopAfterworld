package net.enelson.sopafterworld.command;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("reload") && (sender.hasPermission("sopafterworld.reload") || sender.isOp())) {
			SopAfterworld.reloadRuntimeConfig();
			sender.sendMessage(Utils.color(SopAfterworld.config.getString("messages.reload")));
		} else {
			sender.sendMessage(Utils.color("&3SopAfterworld &fv" + SopAfterworld.pluginVersion + " by E.NeLsOn"));
		}
		return false;
	}
}
