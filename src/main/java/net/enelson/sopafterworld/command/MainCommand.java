package net.enelson.sopafterworld.command;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
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
		} else if (args.length > 0 && args[0].equalsIgnoreCase("deaths") && (sender.hasPermission("sopafterworld.deaths") || sender.isOp())) {
			handleDeaths(sender, args);
		} else {
			sender.sendMessage(Utils.color("&3SopAfterworld &fv" + SopAfterworld.pluginVersion + " by E.NeLsOn"));
		}
		return true;
	}

	private void handleDeaths(CommandSender sender, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(message("messages.deaths-usage", null, 0));
			return;
		}

		String target = args[1];
		Integer current = SopAfterworld.playerManager.getDeaths(target);
		if (current == null) {
			sender.sendMessage(message("messages.deaths-player-not-found", target, 0));
			return;
		}

		if (args.length == 2) {
			sender.sendMessage(message("messages.deaths-current", target, current));
			return;
		}

		String action = args[2].toLowerCase();
		int newValue;
		if (action.equals("clear")) {
			newValue = 0;
		} else if (action.equals("set") || action.equals("add") || action.equals("remove")) {
			if (args.length < 4) {
				sender.sendMessage(message("messages.deaths-usage", null, 0));
				return;
			}
			int amount;
			try {
				amount = Integer.parseInt(args[3]);
			} catch (NumberFormatException exception) {
				sender.sendMessage(message("messages.deaths-invalid-number", null, 0));
				return;
			}
			if (action.equals("set")) {
				newValue = amount;
			} else if (action.equals("add")) {
				newValue = current + amount;
			} else {
				newValue = current - amount;
			}
		} else {
			sender.sendMessage(message("messages.deaths-usage", null, 0));
			return;
		}

		if (newValue < 0) {
			newValue = 0;
		}
		if (!SopAfterworld.playerManager.setDeaths(target, newValue)) {
			sender.sendMessage(message("messages.deaths-player-not-found", target, 0));
			return;
		}
		sender.sendMessage(message("messages.deaths-updated", target, newValue));
	}

	private String message(String path, String player, int deaths) {
		String raw = SopAfterworld.config.getString(path, path);
		if (player != null) {
			raw = raw.replace("%player%", player);
		}
		raw = raw.replace("%deaths%", String.valueOf(deaths));
		return Utils.color(raw);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		boolean deathsPerm = sender.hasPermission("sopafterworld.deaths") || sender.isOp();

		if (args.length == 1) {
			List<String> completions = new ArrayList<String>();
			if (sender.hasPermission("sopafterworld.reload") || sender.isOp()) {
				completions.add("reload");
			}
			if (sender.hasPermission("sopafterworld.regenerate") || sender.isOp()) {
				completions.add("regenerate");
			}
			if (deathsPerm) {
				completions.add("deaths");
			}
			return filter(completions, args[0]);
		}

		if (args.length >= 2 && args[0].equalsIgnoreCase("deaths") && deathsPerm) {
			if (args.length == 2) {
				List<String> names = new ArrayList<String>();
				for (Player online : Bukkit.getOnlinePlayers()) {
					names.add(online.getName());
				}
				return filter(names, args[1]);
			}
			if (args.length == 3) {
				return filter(Arrays.asList("set", "add", "remove", "clear"), args[2]);
			}
			if (args.length == 4 && !args[2].equalsIgnoreCase("clear")) {
				return filter(Arrays.asList("1", "5", "10"), args[3]);
			}
		}

		return Collections.emptyList();
	}

	private List<String> filter(List<String> options, String prefix) {
		if (prefix == null || prefix.isEmpty()) {
			return options;
		}
		String lower = prefix.toLowerCase();
		List<String> result = new ArrayList<String>();
		for (String option : options) {
			if (option.toLowerCase().startsWith(lower)) {
				result.add(option);
			}
		}
		return result;
	}
}
