package com.darkbladedev.commands.functions.enchantments;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.utils.MM;

public class ItemInfo implements SubcommandExecutor, TabCompletable {
    
    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player target;
        
        if (args.length < 1) {
            // Si no se especifica un jugador y el comando es ejecutado por un jugador
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(MM.toComponent("<red>Uso: /enchantments iteminfo <jugador>"));
                return;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(MM.toComponent("<red>El jugador '" + args[0] + "' no está en línea."));
                return;
            }
        }

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(MM.toComponent("<red>" + (sender == target ? "No tienes" : target.getName() + " no tiene") + " un ítem en la mano principal."));
            return;
        }

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (enchantments.isEmpty()) {
            sender.sendMessage(MM.toComponent("<yellow>El ítem no tiene encantamientos.</yellow>"));
            return;
        }

        sender.sendMessage(MM.toComponent("<gold>Encantamientos en el ítem de " + target.getName() + ":</gold>"));
        
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            
            String enchantName = enchant.getKey().getKey();
            boolean isCursed = enchant.isCursed();
            boolean isTreasure = enchant.isTreasure();
            
            StringBuilder info = new StringBuilder();
            info.append("<yellow>• ").append(enchantName).append("</yellow>");
            info.append(" <gray>Nivel: ").append(level).append("</gray>");
            
            if (isTreasure) {
                info.append(" <aqua>[Tesoro]</aqua>");
            }
            
            if (isCursed) {
                info.append(" <red>[Maldición]</red>");
            }
            
            sender.sendMessage(MM.toComponent(info.toString()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length - 2 == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}