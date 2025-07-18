package com.darkbladedev.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface TabCompletable {
    List<String> onTabComplete(CommandSender sender, String[] args);
}
