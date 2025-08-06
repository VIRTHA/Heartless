package com.darkbladedev.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class MM {

    public static Component toComponent(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static MiniMessage getMiniMessage() {
        return MiniMessage.miniMessage();
    }
}
