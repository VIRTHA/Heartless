package com.darkbladedev.commands.functions.health;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.utils.MM;

public class RemoveMaxHealth implements SubcommandExecutor, TabCompletable {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        Double health = Double.parseDouble(args[0]);
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage("§cJugador no encontrado.");
            return;
        }
        
        try {
            target.getAttribute(Attribute.MAX_HEALTH).setBaseValue(target.getHealth() - health);
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<dark_red>Error: <red>" + e.getMessage()));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        // Los args aquí incluyen todos los argumentos del comando, incluyendo grupo y acción
        // Necesitamos ajustar el índice para que coincida con los argumentos específicos de este subcomando
        // args[0] y args[1] son el grupo y la acción, por lo que args[2] es el primer argumento real del subcomando
        
        // Calculamos el índice real restando 2 (grupo y acción)
        int adjustedIndex = args.length - 2;
        
        switch (adjustedIndex) {
            case 1: // Primer argumento del subcomando (cantidad)
                return List.of("<amount>");
            
            case 2: // Segundo argumento del subcomando (jugador)
                // Filtramos por el argumento actual (args[3])
                String currentArg = args.length > 3 ? args[3] : "";
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(currentArg.toLowerCase()))
                        .collect(Collectors.toList());

            default:
                return Collections.emptyList();
        }
    }
}
