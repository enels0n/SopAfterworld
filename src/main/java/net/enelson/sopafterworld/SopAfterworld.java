package net.enelson.sopafterworld;

import com.google.common.reflect.ClassPath;
import net.enelson.sopafterworld.armorstand.ASManager;
import net.enelson.sopafterworld.command.MainCommand;
import net.enelson.sopafterworld.command.PortalCommand;
import net.enelson.sopafterworld.corpses.CorpseManager;
import net.enelson.sopafterworld.data.PlayerManager;
import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SopAfterworld extends org.bukkit.plugin.java.JavaPlugin implements Listener {

	public static Plugin plugin;
	public static File fileConfig;
	public static FileConfiguration config;
	public static String reincarnationPoint;
	public static String afterworld;
	public static String pluginVersion;
	public static List<String> worlds;
	public static PlayerManager playerManager;
	public static CorpseManager corpseManager;
	public static Map<Integer, Integer> spawns;
	public static int minX;
	public static int maxX;
	public static int minZ;
	public static int maxZ;
	public static ASManager am;
	public static TextUtils textUtils;

	@Override
	public void onEnable() {
		plugin = this;
		textUtils = SopLib.getInstance() != null ? SopLib.getInstance().getTextUtils() : new TextUtils();

		fileConfig = new File(getDataFolder(), "config.yml");
		if (!fileConfig.exists()) {
			saveResource("config.yml", true);
		}
		config = YamlConfiguration.loadConfiguration(fileConfig);

		createDataFolder("players");
		createDataFolder("corpses");

		spawns = new HashMap<Integer, Integer>();
		reloadRuntimeConfig();

		getCommand("sopafterworld").setExecutor(new MainCommand());
		getCommand("portal").setExecutor(new PortalCommand());

		playerManager = new PlayerManager(this);
		corpseManager = new CorpseManager();
		registerListeners();

		new BukkitRunnable() {
			@Override
			public void run() {
				pluginVersion = getDescription().getVersion();
			}
		}.runTaskLater(this, 1L);
		am = new ASManager();
	}

	@Override
	public void onDisable() {
		if (corpseManager != null) {
			corpseManager.deInit();
		}
	}

	public static void reloadRuntimeConfig() {
		config = YamlConfiguration.loadConfiguration(fileConfig);
		afterworld = config.getString("afterworld");
		worlds = config.getStringList("enabled-worlds");
		minX = config.getInt("portal.minX");
		maxX = config.getInt("portal.maxX");
		minZ = config.getInt("portal.minZ");
		maxZ = config.getInt("portal.maxZ");
		reincarnationPoint = config.getString("default-reincarnation-point");

		spawns.clear();
		if (config.getConfigurationSection("spawns") != null) {
			for (String key : config.getConfigurationSection("spawns").getKeys(false)) {
				spawns.put(Integer.parseInt(key), config.getInt("spawns." + key));
			}
		}
	}

	private void createDataFolder(String name) {
		File folder = new File(getDataFolder(), name);
		if (!folder.exists()) {
			folder.mkdir();
		}
	}

	private void registerListeners() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		try {
			for (ClassPath.ClassInfo clazzInfo : ClassPath.from(getClassLoader()).getTopLevelClasses("net.enelson.sopafterworld.listeners")) {
				Class<?> clazz = Class.forName(clazzInfo.getName());
				if (Listener.class.isAssignableFrom(clazz)) {
					pluginManager.registerEvents((Listener) clazz.getDeclaredConstructor().newInstance(), this);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
