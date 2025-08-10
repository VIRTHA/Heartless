package com.darkbladedev.commands;

import org.bukkit.command.CommandSender;

public interface SubcommandExecutor {
    void execute(CommandSender sender, String[] args);
    boolean isEnabled();
    void setEnabled(boolean enabled);
}
