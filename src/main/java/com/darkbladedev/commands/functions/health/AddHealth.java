package com.darkbladedev.commands.functions.health;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.utils.MM;

public class AddHealth implements SubcommandExecutor, TabCompletable {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        Double health = 0.0;
        Player target = null;

        try {
            health = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.toComponent("<red>'" + args[0] + "'debe ser un número."));
            return;
        } catch (NullPointerException e) {
            sender.sendMessage(MM.toComponent("<red>El primer argumento no puede ser nulo."));
            return;
        }

        try {
            target = Bukkit.getPlayer(args[1]);
        } catch (NullPointerException e) {
            sender.sendMessage(MM.toComponent("<red>El segundo argumento no puede ser nulo."));
            return;
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Ha ocurrido un error indefinido al ejecutar este comando. <gray>(Vea consola)"));
            
        }
        
        if (target == null) {
            sender.sendMessage("§cJugador no encontrado.");
            return;
        }

        try {
            target.setHealth(target.getHealth() + health);
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<dark_red>Error: <red>" + e.getMessage()));
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return List.of("<amount>");
            
            case 2:
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());

            default:
                return Collections.emptyList();
        }
    }
    

}
