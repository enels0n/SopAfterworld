package net.enelson.sopafterworld.util;

import me.clip.placeholderapi.PlaceholderAPI;
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

	public static Location getLocationInCircle(Location origin, Integer radius) {
		double angle = RANDOM.nextInt(360);
		double mineX = origin.getBlockX() + radius * Math.cos(angle);
		double mineZ = origin.getBlockZ() + radius * Math.sin(angle);

		Location location = new Location(origin.getWorld(), mineX, 0, mineZ);
		Block block = origin.getWorld().getHighestBlockAt(location);
		return block.getLocation();
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
		for (int i = 0; i < 1; i = 0) {
			int x = SopAfterworld.minX + (int) (Math.random() * ((SopAfterworld.maxX - SopAfterworld.minX) + 1));
			int z = SopAfterworld.minZ + (int) (Math.random() * ((SopAfterworld.maxZ - SopAfterworld.minZ) + 1));

			Block block = Bukkit.getWorld(SopAfterworld.afterworld).getHighestBlockAt(x, z);
			if (block.getType() != Material.LAVA && block.getType() != Material.FIRE && block.getType() != Material.MAGMA_BLOCK) {
				loc = block.getLocation().add(0, 1, 0);
				break;
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
			input = PlaceholderAPI.setPlaceholders(player, input);
		}
		if (output != null) {
			output = PlaceholderAPI.setPlaceholders(player, output);
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
}
