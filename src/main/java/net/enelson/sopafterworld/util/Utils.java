package net.enelson.sopafterworld.util;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.data.PlayerData;
import net.enelson.sopli.lib.SopLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Map.Entry;
import java.util.Random;

public class Utils {
	private static final Random RANDOM = new Random();

	public static String color(String message) {
		return SopAfterworld.textUtils != null ? SopAfterworld.textUtils.color(message) : message;
	}

	public static String resolvePlaceholders(Player player, String text) {
		if (text == null) {
			return "";
		}
		String resolved = text.replace("{player}", player != null ? player.getName() : "");
		if (player != null) {
			resolved = applyPlaceholderApi(player, resolved);
		}
		return resolved;
	}

	public static String resolvePlaceholders(String playerName, String text) {
		if (text == null) {
			return "";
		}
		Player player = playerName != null && !playerName.trim().isEmpty() ? Bukkit.getPlayerExact(playerName) : null;
		String resolved = text.replace("{player}", playerName != null ? playerName : "");
		if (player != null) {
			resolved = applyPlaceholderApi(player, resolved);
		}
		return resolved;
	}

	public static String getCorpseDisplayName(Player player) {
		String format = SopAfterworld.config.getString("corpse.name-format", "&8Труп &f{player}");
		return color(resolvePlaceholders(player, format));
	}

	public static String getCorpseDisplayName(String playerName) {
		String format = SopAfterworld.config.getString("corpse.name-format", "&8Труп &f{player}");
		return color(resolvePlaceholders(playerName, format));
	}

	public static String getCorpseInventoryTitle(Player player) {
		String format = SopAfterworld.config.getString(
				"corpse.inventory-title-format",
				SopAfterworld.config.getString("corpse.name-format", "&8Труп &f{player}")
		);
		return color(resolvePlaceholders(player, format));
	}

	public static String getCorpseInventoryTitle(String playerName) {
		String format = SopAfterworld.config.getString(
				"corpse.inventory-title-format",
				SopAfterworld.config.getString("corpse.name-format", "&8Труп &f{player}")
		);
		return color(resolvePlaceholders(playerName, format));
	}

	public static String getCorpseInspectText(Player player, String corpsePlayerName) {
		String format = SopAfterworld.config.getString("corpse.inspect-format", "*&eОсматривает труп {player}&f*");
		String resolved = format.replace("{player}", corpsePlayerName != null ? corpsePlayerName : "");
		if (player != null) {
			resolved = applyPlaceholderApi(player, resolved);
		}
		return color(resolved);
	}

	public static Location getLocationInCircle(Location origin, Integer radius) {
		double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
		double distance = Math.sqrt(RANDOM.nextDouble()) * radius;
		double mineX = origin.getX() + distance * Math.cos(angle);
		double mineZ = origin.getZ() + distance * Math.sin(angle);

		Location location = new Location(origin.getWorld(), mineX, 0, mineZ);
		Block block = origin.getWorld().getHighestBlockAt(location);
		return block.getLocation();
	}

	public static Location[] createPortalLocations(Location location) {
		Location[] locs = new Location[17];
		for (int i = 0; i < locs.length; i++) {
			locs[i] = location.clone();
		}

		locs[1].add(0, 0.5, 0);
		locs[2].add(0, 1, 0);
		locs[3].add(0, 1.5, 0);
		locs[4].add(0, 2, 0);
		locs[5].add(0, 2.3, 0.25);
		locs[6].add(0, 2.6, 0.75);
		locs[7].add(0, 2.8, 1);
		locs[8].add(0, 2.8, 1.5);
		locs[9].add(0, 2.8, 2);
		locs[10].add(0, 2.6, 2.3);
		locs[11].add(0, 2.3, 2.7);
		locs[12].add(0, 2, 3);
		locs[13].add(0, 1.5, 3);
		locs[14].add(0, 1, 3);
		locs[15].add(0, 0.5, 3);
		locs[16].add(0, 0, 3);
		return locs;
	}

	public static Location findAfterworldSpawnLocation(Location portalLocation, Integer radius) {
		if (portalLocation == null || portalLocation.getWorld() == null) {
			return portalLocation;
		}
		for (int attempt = 0; attempt < 32; attempt++) {
			Location candidate = getLocationInCircle(portalLocation, radius);
			Location safe = findSafeStandingLocation(candidate);
			if (safe != null && isInsideWorldBorder(safe)) {
				return safe;
			}
		}
		Location fallback = findSafeStandingLocation(portalLocation);
		return isInsideWorldBorder(fallback) ? fallback : portalLocation;
	}

	public static Location findSafeStandingLocation(Location baseLocation) {
		if (baseLocation == null || baseLocation.getWorld() == null) {
			return baseLocation;
		}

		Location cursor = baseLocation.getBlock().getLocation();
		for (int radius = 0; radius <= 8; radius++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (radius != 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
						continue;
					}

					Location safe = findSafeStandingLocationInColumn(cursor.clone().add(x, 0, z));
					if (safe != null && isInsideWorldBorder(safe)) {
						return safe;
					}
				}
			}
		}

		return cursor.add(0.5D, 1.0D, 0.5D);
	}

	private static Location findSafeStandingLocationInColumn(Location columnBase) {
		Block highest = columnBase.getWorld().getHighestBlockAt(columnBase);
		int startY = Math.max(columnBase.getBlockY(), highest.getY() + 1);
		for (int y = startY + 6; y >= Math.max(2, startY - 24); y--) {
			Location check = new Location(columnBase.getWorld(), columnBase.getBlockX(), y, columnBase.getBlockZ());
			if (isSafeStandingLocation(check)) {
				return check.add(0.5D, 0.0D, 0.5D);
			}
		}
		return null;
	}

	public static boolean isInsideWorldBorder(Location location) {
		if (location == null || location.getWorld() == null) {
			return false;
		}
		if (!SopAfterworld.generatorWorldBorderEnabled) {
			return true;
		}
		org.bukkit.WorldBorder border = location.getWorld().getWorldBorder();
		if (border == null) {
			return true;
		}
		double halfSize = border.getSize() / 2.0D;
		Location center = border.getCenter();
		return location.getX() >= center.getX() - halfSize
				&& location.getX() <= center.getX() + halfSize
				&& location.getZ() >= center.getZ() - halfSize
				&& location.getZ() <= center.getZ() + halfSize;
	}

	private static boolean isSafeStandingLocation(Location location) {
		Block feet = location.getBlock();
		Block head = location.clone().add(0, 1, 0).getBlock();
		Block below = location.clone().add(0, -1, 0).getBlock();

		if (!below.getType().isSolid()) {
			return false;
		}
		if (isHazardous(below.getType()) || isHazardous(feet.getType()) || isHazardous(head.getType())) {
			return false;
		}
		return feet.getType().isAir() && head.getType().isAir();
	}

	private static boolean isHazardous(Material material) {
		return material == Material.LAVA || material == Material.MAGMA_BLOCK || material == Material.FIRE
				|| material == Material.CAMPFIRE || material == Material.SOUL_CAMPFIRE;
	}

	public static Integer getRadius(int deaths) {
		int radius = 10;
		for (Entry<Integer, Integer> point : SopAfterworld.spawns.entrySet()) {
			if (point.getKey() > deaths) {
				break;
			}
			radius = point.getValue();
		}
		return radius;
	}

	public static Location searchPortalPoint() {
		Location loc = null;
		for (int tries = 0; tries < 256; tries++) {
			int x;
			int z;
			if ("bounds".equalsIgnoreCase(SopAfterworld.portalMode)) {
				x = SopAfterworld.portalMinX + RANDOM.nextInt((SopAfterworld.portalMaxX - SopAfterworld.portalMinX) + 1);
				z = SopAfterworld.portalMinZ + RANDOM.nextInt((SopAfterworld.portalMaxZ - SopAfterworld.portalMinZ) + 1);
			} else {
				double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
				double distance = Math.sqrt(RANDOM.nextDouble()) * SopAfterworld.portalRadius;
				x = (int) Math.round(distance * Math.cos(angle));
				z = (int) Math.round(distance * Math.sin(angle));
			}

			Block block = Bukkit.getWorld(SopAfterworld.afterworld).getHighestBlockAt(x, z);
			if (!isHazardous(block.getType())) {
				loc = findSafeStandingLocation(block.getLocation().add(0, 1, 0));
				if (loc != null && isInsideWorldBorder(loc) && !isHazardous(loc.clone().add(0, -1, 0).getBlock().getType())) {
					break;
				}
			}
		}
		return loc;
	}

	public static Location getNavigatorLocation(Player player) {
		PlayerData pd = SopAfterworld.playerManager.getPlayerData(player);
		Vector vector = pd.getPortalLocation().clone().toVector().subtract(player.getLocation().toVector()).normalize().multiply(7);
		Location loc = player.getLocation().clone().add(vector);
		loc.setY(player.getLocation().getY() + 1.4);
		return loc;
	}

	public static boolean hasPermit(Player player) {
		for (String st : SopAfterworld.config.getConfigurationSection("requirements").getKeys(false)) {
			if (!Utils.checkRequire(player,
					SopAfterworld.config.getString("requirements." + st + ".type"),
					SopAfterworld.config.getString("requirements." + st + ".input"),
					SopAfterworld.config.getString("requirements." + st + ".output"))) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkRequire(Player player, String type, String input, String output) {
		if (input != null) {
			input = applyPlaceholderApi(player, input);
		}
		if (output != null) {
			output = applyPlaceholderApi(player, output);
		}

		switch (type) {
			case "==":
				return input.equals(output);
			case "!=":
				return !input.equals(output);
			case ">":
				return Double.parseDouble(input) > Double.parseDouble(output);
			case ">=":
				return Double.parseDouble(input) >= Double.parseDouble(output);
			case "<":
				return Double.parseDouble(input) < Double.parseDouble(output);
			case "<=":
				return Double.parseDouble(input) <= Double.parseDouble(output);
			case "has perm":
				return player.hasPermission(input);
			case "!has perm":
				return !player.hasPermission(input);
			default:
				return false;
		}
	}

	public static boolean checkNotDropping(ItemStack item) {
		if (item == null || SopLib.getInstance() == null || SopLib.getInstance().getItemUtils() == null) {
			return false;
		}
		return SopLib.getInstance().getItemUtils().getNBT(item, "MMOITEMS_DISABLE_DROPING", Byte.class) != null
				|| SopLib.getInstance().getItemUtils().getNBT(item, "MMOITEMS_DISABLE_DROPING", Integer.class) != null
				|| SopLib.getInstance().getItemUtils().getNBT(item, "MMOITEMS_DISABLE_DROPING", String.class) != null;
	}

	private static String applyPlaceholderApi(Player player, String text) {
		if (text == null || player == null) {
			return text;
		}
		String resolved = applyKnownPlaceholderFallbacks(player, text);
		if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			return resolved;
		}
		try {
			return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, resolved);
		} catch (Throwable ignored) {
			return resolved;
		}
	}

	private static String applyKnownPlaceholderFallbacks(Player player, String text) {
		String resolved = text;
		resolved = resolved.replace("%player_name%", player.getName());
		resolved = resolved.replace("%player_level%", String.valueOf(player.getLevel()));
		resolved = resolved.replace("%vault_eco_balance%", resolveVaultBalance(player));
		return resolved;
	}

	private static String resolveVaultBalance(Player player) {
		try {
			Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
			org.bukkit.plugin.RegisteredServiceProvider<?> registration = Bukkit.getServicesManager().getRegistration(economyClass);
			if (registration == null || registration.getProvider() == null) {
				return "0";
			}
			Object economy = registration.getProvider();
			Object result = economyClass.getMethod("getBalance", org.bukkit.OfflinePlayer.class).invoke(economy, player);
			if (result instanceof Number) {
				return String.valueOf(((Number) result).doubleValue());
			}
		} catch (Throwable ignored) {
		}
		return "0";
	}
}
