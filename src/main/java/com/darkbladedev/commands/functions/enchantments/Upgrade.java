package com.darkbladedev.commands.functions.enchantments;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.utils.MM;

public class Upgrade implements SubcommandExecutor, TabCompletable {
    
    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MM.toComponent("<red>Uso: /enchantments upgrade <jugador> <encantamiento> [niveles]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(MM.toComponent("<red>El jugador '" + args[0] + "' no está en línea."));
            return;
        }

        Enchantment enchantment;
        try {
            enchantment = Enchantment.getByName(args[1].toUpperCase());
            if (enchantment == null) {
                sender.sendMessage(MM.toComponent("<red>El encantamiento '" + args[1] + "' no existe."));
                return;
            }
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al obtener el encantamiento: " + e.getMessage()));
            return;
        }

        int levelsToAdd = 1; // Por defecto, añadir 1 nivel
        if (args.length >= 3) {
            try {
                levelsToAdd = Integer.parseInt(args[2]);
                if (levelsToAdd <= 0) {
                    sender.sendMessage(MM.toComponent("<red>El número de niveles debe ser mayor que 0."));
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(MM.toComponent("<red>'" + args[2] + "' debe ser un número entero."));
                return;
            }
        }

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(MM.toComponent("<red>El jugador debe tener un ítem en la mano principal."));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            sender.sendMessage(MM.toComponent("<red>Este ítem no puede ser encantado."));
            return;
        }

        if (!item.containsEnchantment(enchantment)) {
            sender.sendMessage(MM.toComponent("<red>El ítem no tiene el encantamiento " + enchantment.getKey().getKey() + "."));
            return;
        }

        int currentLevel = item.getEnchantmentLevel(enchantment);
        int newLevel = currentLevel + levelsToAdd;

        try {
            item.addUnsafeEnchantment(enchantment, newLevel);
            target.getInventory().setItemInMainHand(item);
            
            sender.sendMessage(MM.toComponent("<green>Se ha mejorado " + enchantment.getKey().getKey() + " de nivel " + 
                    currentLevel + " a nivel " + newLevel + " en el ítem de " + target.getName() + "."));
            if (sender != target) {
                target.sendMessage(MM.toComponent("<green>Se ha mejorado " + enchantment.getKey().getKey() + " de nivel " + 
                        currentLevel + " a nivel " + newLevel + " en tu ítem."));
            }
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al mejorar el encantamiento: " + e.getMessage()));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length - 2 == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length - 2 == 2) {
            return java.util.Arrays.stream(Enchantment.values())
                    .map(enchant -> enchant.getKey().getKey().toLowerCase())
                    .filter(name -> name.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length - 2 == 3) {
            return List.of("1", "2", "3", "5", "10");
        }
        return Collections.emptyList();
    }
}