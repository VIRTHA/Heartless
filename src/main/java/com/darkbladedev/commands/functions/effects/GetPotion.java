package com.darkbladedev.commands.functions.effects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;

public class GetPotion implements SubcommandExecutor, TabCompletable {

    private boolean enabled = false;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("fury", "zombie_infection");
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList());

        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: /effects get-potion <potion> <player>");
            return;
        }
        var potion = args[1];
        var player = args[2];

        var playerObj = sender.getServer().getPlayer(player);
        if (playerObj == null) {
            sender.sendMessage("Jugador inválido");
            return;
        }
        var potionObj = HeartlessMain.getContentManager().getPotion(potion);
        if (potionObj == null) {
            sender.sendMessage("Efecto de poción inválido");

            return;
        }
        var item = playerObj.getInventory().addItem(potionObj);
        if (item.isEmpty()) {
            sender.sendMessage("Poción obtenida");
        } else {
            sender.sendMessage("No se pudo obtener la poción");
        }
    }

}
