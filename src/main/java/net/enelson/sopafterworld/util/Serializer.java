package net.enelson.sopafterworld.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Serializer {

	public static List<String> getSerializedLocation(Location[] locs) {
		List<String> list = new ArrayList<>();
		for(Location loc : locs) {
			list.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch());
		}
		return list;
	}
	public static String getSerializedLocation(Location loc) {
		return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
	}
	
	public static Location getDeserializedLocation(String s) {
		String[] split = s.split(",");
		return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]),
				Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
	}

	public static Location[] getDeserializedLocation(List<String> s) {
		Location[] locs = new Location[s.size()];
		int i = 0;

		for (String line : s) {
			String[] split = line.split(",");
			locs[i] = new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]),
					Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]),
					Float.parseFloat(split[5]));
			i++;
		}
		return locs;
	}
}

