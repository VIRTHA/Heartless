package com.darkbladedev.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.Bukkit;

public class WorldGuardUtils {
    public static WorldGuard getWorldGuard() {
        return WorldGuard.getInstance();
    }

    public static ProtectedRegion getRegion(String id, World world) {
        var regionManager = getWorldGuard().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) return null;
        return regionManager.getRegion(id);
    }

    /**
     * Checks if PvP is denied at the given Bukkit location using WorldGuard flags.
     * Returns false if WorldGuard is not present or the region query cannot be performed.
     */
    public static boolean isPvPDeniedAt(Location bukkitLocation) {
        try {
            // If WorldGuard plugin is not loaded, treat as not denied
            if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null ||
                !Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                return false;
            }

            RegionContainer container = getWorldGuard().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            boolean allowed = query.testState(BukkitAdapter.adapt(bukkitLocation), null, Flags.PVP);
            return !allowed; // denied if not allowed
        } catch (NoClassDefFoundError | Exception e) {
            // If WorldGuard classes are not found at runtime or any error occurs, default to not denied
            return false;
        }
    }
}
