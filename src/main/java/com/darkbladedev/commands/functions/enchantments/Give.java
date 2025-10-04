package com.darkbladedev.commands.functions.enchantments;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.enchantments.Enchantment;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.utils.MM;

public class Give implements SubcommandExecutor, TabCompletable {

    private boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MM.toComponent("<red>Uso: /enchantments give <jugador> <encantamiento> [nivel] [cantidad]"));
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

        int level = 1; // Nivel por defecto
        if (args.length >= 3) {
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
        }

        int amount = 1; // Cantidad por defecto
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0 || amount > 64) {
                    sender.sendMessage(MM.toComponent("<red>La cantidad debe estar entre 1 y 64."));
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(MM.toComponent("<red>'" + args[3] + "' debe ser un número entero."));
                return;
            }
        }

        try {
            // Crear el libro encantado
            ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK, amount);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();
            
            if (meta != null) {
                meta.addStoredEnchant(enchantment, level, true);
                enchantedBook.setItemMeta(meta);
            }

            // Verificar si el jugador tiene espacio en el inventario
            if (target.getInventory().firstEmpty() == -1 && amount > 0) {
                sender.sendMessage(MM.toComponent("<yellow>Advertencia: El inventario de " + target.getName() + " está lleno. Los libros se han dropeado en el suelo."));
                target.getWorld().dropItemNaturally(target.getLocation(), enchantedBook);
            } else {
                target.getInventory().addItem(enchantedBook);
            }

            String enchantName = enchantment.getKey().getKey();
            String amountText = amount > 1 ? " (x" + amount + ")" : "";
            
            sender.sendMessage(MM.toComponent("<green>Se ha dado " + enchantName + " nivel " + level + amountText + " a " + target.getName() + "."));
            if (sender != target) {
                target.sendMessage(MM.toComponent("<green>Has recibido un libro encantado con " + enchantName + " nivel " + level + amountText + "."));
            }
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al crear el libro encantado: " + e.getMessage()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Autocompletar nombres de jugadores
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Autocompletar nombres de encantamientos
            return java.util.Arrays.stream(CustomEnchantments.ENCHANTMENTS.values())
                    .map(enchant -> enchant.getKey().value().toLowerCase())
                    .filter(name -> name.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Sugerir niveles comunes
            return List.of("1", "2", "3", "4", "5");
        } else if (args.length == 4) {
            // Sugerir cantidades comunes
            return List.of("1", "2", "4", "8", "16", "32", "64");
        }
        return Collections.emptyList();
    }
}