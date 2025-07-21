package com.darkbladedev.commands.functions.bansystem;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.BanManager;
import com.darkbladedev.utils.MM;

public class Get implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("heartless.bansystem.unban")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para listar jugadores baneados."));
            return;
        }

        HeartlessMain plugin = HeartlessMain.getInstance();
        BanManager banManager = plugin.getBanManager();

        sender.sendMessage(MM.toComponent("<white>Jugadores baneados"));
        for (UUID id : banManager.getBanList()) {
            sender.sendMessage(MM.toComponent("   <green><b>|></b></green> " + Bukkit.getPlayer(id).getName()));
        }
    }

}
