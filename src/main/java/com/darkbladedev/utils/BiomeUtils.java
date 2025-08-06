package com.darkbladedev.utils;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BiomeUtils {

    // Conjunto de biomas donde **no puede llover**
    private static final Set<Biome> NON_RAINY_BIOMES = new HashSet<>(Arrays.asList(
        Biome.DESERT,
        Biome.DESERT,
        Biome.SAVANNA,
        Biome.SAVANNA_PLATEAU,
        Biome.BADLANDS,
        Biome.ERODED_BADLANDS,
        Biome.WOODED_BADLANDS,
        Biome.SNOWY_TAIGA,
        Biome.SNOWY_TAIGA,
        Biome.ICE_SPIKES,
        Biome.FROZEN_OCEAN,
        Biome.FROZEN_PEAKS,
        Biome.SNOWY_BEACH,
        Biome.SNOWY_SLOPES,
        Biome.GROVE,
        Biome.JAGGED_PEAKS,
        Biome.STONY_PEAKS,
        Biome.NETHER_WASTES,
        Biome.SOUL_SAND_VALLEY,
        Biome.WARPED_FOREST,
        Biome.CRIMSON_FOREST,
        Biome.BASALT_DELTAS,
        Biome.THE_END,
        Biome.END_HIGHLANDS,
        Biome.END_MIDLANDS,
        Biome.SMALL_END_ISLANDS,
        Biome.END_BARRENS
    ));

    /**
     * Devuelve true si el bioma permite lluvia (aunque el mundo tenga tormenta).
     */
    public static boolean canRain(Biome biome) {
        return !NON_RAINY_BIOMES.contains(biome);
    }

    /**
     * Devuelve true si puede llover en la ubicación especificada.
     */
    public static boolean canRain(Location location) {
        return canRain(location.getBlock().getBiome());
    }

    /**
     * Devuelve true si puede llover en el bloque.
     */
    public static boolean canRain(Block block) {
        return canRain(block.getBiome());
    }
}

