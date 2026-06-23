package net.enelson.sopafterworld.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * WorldGuard integration kept in its own class so the WorldGuard/WorldEdit
 * classes are only loaded when WorldGuard is actually installed.
 */
public final class WorldGuardHook {

    private WorldGuardHook() {
    }

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    /**
     * Applies the configured flags to the world's {@code __global__} region.
     * State strings: "allow", "deny", or "none"/empty to clear.
     */
    public static void applyGlobalFlags(World world, String passthrough, String interact, List<String> denySpawn, Logger logger) {
        try {
            RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (regions == null) {
                logger.warning("WorldGuard region manager not available for world " + world.getName() + "; skipping flags.");
                return;
            }
            ProtectedRegion global = regions.getRegion("__global__");
            if (global == null) {
                global = new GlobalProtectedRegion("__global__");
                regions.addRegion(global);
            }

            applyState(global, Flags.PASSTHROUGH, passthrough);
            applyState(global, Flags.INTERACT, interact);

            if (denySpawn != null) {
                Set<EntityType> entities = new HashSet<EntityType>();
                for (String id : denySpawn) {
                    if (id == null || id.trim().isEmpty()) {
                        continue;
                    }
                    EntityType type = EntityTypes.get(id.trim().toLowerCase());
                    if (type != null) {
                        entities.add(type);
                    } else {
                        logger.warning("Unknown deny-spawn entity id: " + id);
                    }
                }
                global.setFlag(Flags.DENY_SPAWN, entities);
            }

            regions.save();
        } catch (Throwable throwable) {
            logger.warning("Failed to apply WorldGuard flags to " + world.getName() + ": " + throwable.getMessage());
        }
    }

    private static void applyState(ProtectedRegion region, StateFlag flag, String value) {
        if (value == null) {
            return;
        }
        String normalized = value.trim().toLowerCase();
        if (normalized.equals("allow")) {
            region.setFlag(flag, StateFlag.State.ALLOW);
        } else if (normalized.equals("deny")) {
            region.setFlag(flag, StateFlag.State.DENY);
        } else if (normalized.equals("none") || normalized.isEmpty()) {
            region.setFlag(flag, null);
        }
    }
}
