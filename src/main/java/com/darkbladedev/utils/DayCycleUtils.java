package com.darkbladedev.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.darkbladedev.events.NightPassedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DayCycleUtils {
    
    private static final Map<String, Long> lastCycle = new HashMap<>();
    private static final Map<String, Long> nightCounter = new HashMap<>();
    private static Logger logger;

    private DayCycleUtils() {
        // Evitar instanciación
    }


    public static void init(ConfigurationSection section, Logger loggerInstance) {
        logger = loggerInstance;
        if (section != null) {
            for (String world : section.getKeys(false)) {
                lastCycle.put(world, section.getLong(world + ".lastCycle", 0L));
                nightCounter.put(world, section.getLong(world + ".counter", 0L));
            }
        }
    }

    public static void ensureWorld(World world) {
        String name = world.getName();
        lastCycle.putIfAbsent(name, world.getFullTime() / 24000L);
        nightCounter.putIfAbsent(name, 0L);
    }

    public static void checkWorld(World world) {
        String name = world.getName();
        long currentCycle = world.getFullTime() / 24000L;
        long last = lastCycle.getOrDefault(name, currentCycle);

        if (currentCycle > last) {
            long delta = currentCycle - last;
            long total = nightCounter.getOrDefault(name, 0L) + delta;
            nightCounter.put(name, total);
            lastCycle.put(name, currentCycle);

            if (logger != null) {
                logger.info("[NightCounter] Mundo '" + name + "' ha pasado " + delta +
                        " noche(s). Total: " + total);
            }

            // 🔥 Dispara el evento custom
            Bukkit.getPluginManager().callEvent(new NightPassedEvent(world, delta, total));

        } else if (currentCycle < last) {
            if (logger != null) {
                logger.warning("[NightCounter] El ciclo de tiempo retrocedió en '" + name +
                        "'. Ajustando lastCycle a " + currentCycle);
            }
            lastCycle.put(name, currentCycle);
        }
    }

    public static void save(ConfigurationSection section) {
        for (String world : lastCycle.keySet()) {
            section.set(world + ".lastCycle", lastCycle.get(world));
            section.set(world + ".counter", nightCounter.getOrDefault(world, 0L));
        }
    }

    public static long getNightCount(World world) {
        return nightCounter.getOrDefault(world.getName(), 0L);
    }

    public static void resetNightCount(World world) {
        nightCounter.put(world.getName(), 0L);
        lastCycle.put(world.getName(), world.getFullTime() / 24000L);
    }
}
