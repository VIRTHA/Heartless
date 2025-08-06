package com.darkbladedev.utils;

import net.minecraft.resources.ResourceLocation;

public class RegistryUtils {

    private final static String NAMESPACE = "heartless";

    public static ResourceLocation createKey(String key) {
        return ResourceLocation.tryBuild(NAMESPACE, key);
    }

}
