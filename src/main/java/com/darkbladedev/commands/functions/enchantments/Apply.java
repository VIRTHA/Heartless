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
import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.utils.MM;

public class Apply implements SubcommandExecutor, TabCompletable {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(MM.toComponent("<red>Uso: /enchantments apply <jugador> <encantamiento> <nivel>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(MM.toComponent("<red>El jugador '" + args[0] + "' no está en línea."));
            return;
        }

        Enchantment enchantment;
        try {
            enchantment = CustomEnchantments.ENCHANTMENTS.valueOf(args[1].toUpperCase()).toEnchantment();
            if (enchantment == null) {
                sender.sendMessage(MM.toComponent("<red>El encantamiento '" + args[1] + "' no existe."));
                return;
            }
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al obtener el encantamiento: " + e.getMessage()));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[2]);
            if (level <= 0) {
                sender.sendMessage(MM.toComponent("<red>El nivel debe ser mayor que 0."));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(MM.toComponent("<red>'" + args[2] + "' debe ser un número entero."));
            return;
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

        try {
            item.addUnsafeEnchantment(enchantment, level);
            target.getInventory().setItemInMainHand(item);
            
            sender.sendMessage(MM.toComponent("<green>Se ha aplicado " + enchantment.getKey().getKey() + " nivel " + level + " al ítem de " + target.getName() + "."));
            if (sender != target) {
                target.sendMessage(MM.toComponent("<green>Se ha aplicado " + enchantment.getKey().getKey() + " nivel " + level + " a tu ítem."));
            }
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al aplicar el encantamiento: " + e.getMessage()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return java.util.Arrays.stream(CustomEnchantments.ENCHANTMENTS.values())
                    .map(enchant -> enchant.getKey().value().toLowerCase())
                    .filter(name -> name.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            return List.of("1", "2", "3", "4", "5");
        }
        return Collections.emptyList();
    }
}