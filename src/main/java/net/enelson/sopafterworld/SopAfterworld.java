package net.enelson.sopafterworld;

import com.google.common.reflect.ClassPath;
import net.enelson.sopafterworld.armorstand.ASManager;
import net.enelson.sopafterworld.command.MainCommand;
import net.enelson.sopafterworld.command.PortalCommand;
import net.enelson.sopafterworld.corpses.CorpseManager;
import net.enelson.sopafterworld.corpses.PlayerCorpse;
import net.enelson.sopafterworld.data.PlayerManager;
import net.enelson.sopafterworld.util.Serializer;
import net.enelson.sopafterworld.world.AfterworldChunkGenerator;
import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SopAfterworld extends org.bukkit.plugin.java.JavaPlugin implements Listener {

	public static SopAfterworld plugin;
	public static File fileConfig;
	public static FileConfiguration config;
	public static String reincarnationPoint;
	public static String afterworld;
	public static String pluginVersion;
	public static List<String> worlds;
	public static PlayerManager playerManager;
	public static CorpseManager corpseManager;
	public static Map<Integer, Integer> spawns;
	public static String portalMode;
	public static int portalRadius;
	public static int portalMinX;
	public static int portalMaxX;
	public static int portalMinZ;
	public static int portalMaxZ;
	public static ASManager am;
	public static TextUtils textUtils;
	public static boolean generateAfterworld;
	public static int generatorBaseHeight;
	public static int generatorHeightVariation;
	public static int generatorDetailHeight;
	public static int generatorLavaLevel;
	public static int generatorBedrockLayers;
	public static double generatorPrimaryScale;
	public static double generatorDetailScale;
	public static double generatorRidgeScale;
	public static boolean generatorWorldBorderEnabled;
	public static String generatorSeed;
	public static double portalVisibilityDistance;
	public static Map<String, Long> pendingRegenerations = new HashMap<String, Long>();
	public static final int WORLD_BORDER_PADDING = 200;

	private final AfterworldChunkGenerator afterworldGenerator = new AfterworldChunkGenerator();

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
		ensureAfterworldLoaded();

		MainCommand mainCommand = new MainCommand();
		getCommand("sopafterworld").setExecutor(mainCommand);
		getCommand("sopafterworld").setTabCompleter(mainCommand);
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
		portalMode = config.getString("portal.mode", "radius").trim().toLowerCase();
		portalRadius = resolvePortalRadius(config);
		portalMinX = config.getInt("portal.minX");
		portalMaxX = config.getInt("portal.maxX");
		portalMinZ = config.getInt("portal.minZ");
		portalMaxZ = config.getInt("portal.maxZ");
		reincarnationPoint = config.getString("default-reincarnation-point");
		generateAfterworld = config.getBoolean("world-generator.enabled", true);
		generatorSeed = config.getString("world-generator.seed", "");
		generatorBaseHeight = config.getInt("world-generator.base-height", 63);
		generatorHeightVariation = config.getInt("world-generator.height-variation", 6);
		generatorDetailHeight = config.getInt("world-generator.detail-height", 5);
		generatorLavaLevel = config.getInt("world-generator.lava-level", 67);
		generatorBedrockLayers = config.getInt("world-generator.bedrock-layers", 3);
		generatorPrimaryScale = config.getDouble("world-generator.primary-scale", 0.0105D);
		generatorDetailScale = config.getDouble("world-generator.detail-scale", 0.040D);
		generatorRidgeScale = config.getDouble("world-generator.ridge-scale", 0.026D);
		generatorWorldBorderEnabled = config.getBoolean("world-generator.worldborder-enabled", false);
		portalVisibilityDistance = config.getDouble("portal.visibility-distance", 64.0D);

		spawns.clear();
		if (config.getConfigurationSection("spawns") != null) {
			for (String key : config.getConfigurationSection("spawns").getKeys(false)) {
				spawns.put(Integer.parseInt(key), config.getInt("spawns." + key));
			}
		}

		World loadedAfterworld = afterworld == null ? null : Bukkit.getWorld(afterworld);
		if (loadedAfterworld != null) {
			applyWorldBorder(loadedAfterworld);
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
		registerPaperCorpseUseListener(pluginManager);
	}

	private void registerPaperCorpseUseListener(PluginManager pluginManager) {
		try {
			final Class<?> unknownEntityEventClass = Class.forName("com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent");
			pluginManager.registerEvent((Class<? extends org.bukkit.event.Event>) unknownEntityEventClass, this,
					org.bukkit.event.EventPriority.NORMAL, new EventExecutor() {
						@Override
						public void execute(Listener listener, org.bukkit.event.Event event) {
							try {
								Player player = (Player) unknownEntityEventClass.getMethod("getPlayer").invoke(event);
								int entityId = ((Number) unknownEntityEventClass.getMethod("getEntityId").invoke(event)).intValue();
								PlayerCorpse corpse = corpseManager.getCorpse(entityId);
								if (corpse == null) {
									return;
								}
								net.enelson.sopafterworld.listeners.CorpseInteractHandler.openCorpseInventory(player, corpse, event);
							} catch (Throwable throwable) {
								throwable.printStackTrace();
							}
						}
					}, this, true);
		} catch (ClassNotFoundException ignored) {
		}
	}

	private void ensureAfterworldLoaded() {
		if (afterworld == null || afterworld.trim().isEmpty()) {
			return;
		}

		World world = Bukkit.getWorld(afterworld);
		if (world != null) {
			return;
		}

		WorldCreator creator = new WorldCreator(afterworld);
		creator.environment(World.Environment.NETHER);
		creator.generateStructures(false);

		if (generateAfterworld) {
			creator.seed(resolveConfiguredSeed());
			creator.generator(afterworldGenerator);
		}

		World createdWorld = Bukkit.createWorld(creator);
		if (createdWorld != null) {
			Location spawn = createdWorld.getHighestBlockAt(0, 0).getLocation().add(0.5D, 1.0D, 0.5D);
			createdWorld.setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
			applyWorldBorder(createdWorld);
		}
	}

	private long resolveConfiguredSeed() {
		if (generatorSeed != null && !generatorSeed.trim().isEmpty()) {
			try {
				return Long.parseLong(generatorSeed.trim());
			} catch (NumberFormatException ignored) {
				return generatorSeed.hashCode() * 341873128712L;
			}
		}
		return afterworld.hashCode() * 341873128712L;
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		if (generateAfterworld && worldName != null && worldName.equalsIgnoreCase(afterworld)) {
			return afterworldGenerator;
		}
		return super.getDefaultWorldGenerator(worldName, id);
	}

	public boolean regenerateAfterworld(CommandSender sender) {
		if (afterworld == null || afterworld.trim().isEmpty()) {
			sender.sendMessage(textUtils.color(config.getString("messages.regenerate-failed")));
			return false;
		}

		World world = Bukkit.getWorld(afterworld);
		Location fallbackLocation = resolveFallbackLocation();
		if (world != null) {
			for (Player player : world.getPlayers()) {
				player.teleport(fallbackLocation);
			}
			if (!Bukkit.unloadWorld(world, false)) {
				sender.sendMessage(textUtils.color(config.getString("messages.regenerate-failed")));
				return false;
			}
		}

		File worldFolder = new File(Bukkit.getWorldContainer(), afterworld);
		if (worldFolder.exists() && !deleteRecursively(worldFolder, worldFolder)) {
			sender.sendMessage(textUtils.color(config.getString("messages.regenerate-failed")));
			return false;
		}

		ensureAfterworldLoaded();
		sender.sendMessage(textUtils.color(config.getString("messages.regenerate-success")));
		return true;
	}

	public boolean requestRegeneration(CommandSender sender) {
		long now = System.currentTimeMillis();
		String key = sender.getName().toLowerCase();
		Long expiresAt = pendingRegenerations.get(key);
		if (expiresAt != null && expiresAt >= now) {
			pendingRegenerations.remove(key);
			return regenerateAfterworld(sender);
		}

		pendingRegenerations.put(key, now + 15000L);
		sender.sendMessage(textUtils.color(config.getString("messages.regenerate-confirm")));
		return true;
	}

	private Location resolveFallbackLocation() {
		Location location = Serializer.getDeserializedLocation(reincarnationPoint);
		if (location != null && location.getWorld() != null) {
			return location;
		}

		World firstWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
		if (firstWorld != null) {
			return firstWorld.getSpawnLocation();
		}
		return new Location(null, 0, 100, 0);
	}

	private static void applyWorldBorder(World world) {
		if (world == null) {
			return;
		}

		WorldBorder border = world.getWorldBorder();
		border.setCenter(0.0D, 0.0D);

		double size = Math.max(16.0D, getConfiguredWorldBorderDiameter());
		if (generatorWorldBorderEnabled) {
			border.setSize(size);
		} else {
			border.setSize(60000000.0D);
		}
	}

	public static double getConfiguredWorldBorderDiameter() {
		return (Math.max(1, getEffectivePortalExtent()) + WORLD_BORDER_PADDING) * 2.0D;
	}

	public static int getEffectivePortalExtent() {
		if ("bounds".equalsIgnoreCase(portalMode)) {
			return Math.max(1, Math.max(Math.max(Math.abs(portalMinX), Math.abs(portalMaxX)), Math.max(Math.abs(portalMinZ), Math.abs(portalMaxZ))));
		}
		return Math.max(1, portalRadius);
	}

	private static int resolvePortalRadius(FileConfiguration config) {
		if (config.contains("portal.radius")) {
			return Math.max(1, config.getInt("portal.radius"));
		}
		int minX = config.getInt("portal.minX");
		int maxX = config.getInt("portal.maxX");
		int minZ = config.getInt("portal.minZ");
		int maxZ = config.getInt("portal.maxZ");
		return Math.max(1, Math.max(Math.max(Math.abs(minX), Math.abs(maxX)), Math.max(Math.abs(minZ), Math.abs(maxZ))));
	}

	private boolean deleteRecursively(File file, File root) {
		if (file == null || !file.exists()) {
			return true;
		}
		File target = file.getAbsoluteFile();
		File allowedRoot = root.getAbsoluteFile();
		if (!target.toPath().normalize().startsWith(allowedRoot.toPath().normalize())) {
			return false;
		}
		if (target.isDirectory()) {
			File[] children = target.listFiles();
			if (children != null) {
				for (File child : children) {
					if (!deleteRecursively(child, allowedRoot)) {
						return false;
					}
				}
			}
		}
		return target.delete();
	}
}
