package net.enelson.sopafterworld.command;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.util.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainCommand implements TabExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("reload") && (sender.hasPermission("sopafterworld.reload") || sender.isOp())) {
			SopAfterworld.reloadRuntimeConfig();
			sender.sendMessage(Utils.color(SopAfterworld.config.getString("messages.reload")));
		} else if (args.length > 0 && args[0].equalsIgnoreCase("regenerate") && (sender.hasPermission("sopafterworld.regenerate") || sender.isOp())) {
			SopAfterworld.plugin.requestRegeneration(sender);
		} else {
			sender.sendMessage(Utils.color("&3SopAfterworld &fv" + SopAfterworld.pluginVersion + " by E.NeLsOn"));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			List<String> completions = new ArrayList<String>();
			if (sender.hasPermission("sopafterworld.reload") || sender.isOp()) {
				completions.add("reload");
			}
			if (sender.hasPermission("sopafterworld.regenerate") || sender.isOp()) {
				completions.add("regenerate");
			}
			String input = args[0].toLowerCase();
			completions.removeIf(value -> !value.startsWith(input));
			return completions;
		}
		return Collections.emptyList();
	}
}
