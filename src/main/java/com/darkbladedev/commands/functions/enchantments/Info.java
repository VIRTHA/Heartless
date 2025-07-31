package com.darkbladedev.commands.functions.enchantments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.content.custom.CustomEnchantments;
import com.darkbladedev.content.custom.CustomEnchantments.ENCHANTMENTS;
import com.darkbladedev.utils.MM;

public class Info implements SubcommandExecutor, TabCompletable {
    
    @SuppressWarnings({ "removal", "deprecation" })
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: /enchantments info <encantamiento>"));
            return;
        }

        String enchantName = args[0].toLowerCase();
        ENCHANTMENTS[] enchs = CustomEnchantments.ENCHANTMENTS.values();
        
        AtomicReference<Enchantment> enchantmentRef = new AtomicReference<>();

        for (ENCHANTMENTS ench : enchs) {
            if (ench.getKey().asString().toLowerCase().equals(enchantName)) {
                enchantmentRef.set(ench.toEnchantment());
                break;
            }
        }

        Enchantment enchantment = enchantmentRef.get();
        
        if (enchantment == null) {
            sender.sendMessage(MM.toComponent("<red>El encantamiento '" + enchantName + "' no existe."));
            return;
        }
        
        // Mostrar información detallada
        sender.sendMessage(MM.toComponent("<gold>Información sobre el encantamiento: <yellow>" + enchantment.getKey().getKey() + "</yellow></gold>"));
        sender.sendMessage(MM.toComponent("<gray>Nivel máximo:</gray> <white>" + enchantment.getMaxLevel() + "</white>"));
        sender.sendMessage(MM.toComponent("<gray>Nivel inicial:</gray> <white>" + enchantment.getStartLevel() + "</white>"));
        
        EnchantmentTarget target = enchantment.getItemTarget();
        sender.sendMessage(MM.toComponent("<gray>Aplicable a:</gray> <white>" + (target != null ? target.name() : "Varios") + "</white>"));
        
        sender.sendMessage(MM.toComponent("<gray>Es tesoro:</gray> <white>" + (enchantment.isTreasure() ? "Sí" : "No") + "</white>"));
        sender.sendMessage(MM.toComponent("<gray>Es maldición:</gray> <white>" + (enchantment.isCursed() ? "Sí" : "No") + "</white>"));
        sender.sendMessage(MM.toComponent("<gray>Es tradeable:</gray> <white>" + (enchantment.isTradeable() ? "Sí" : "No") + "</white>"));
        
        // Conflictos con otros encantamientos
        List<String> conflicts = Arrays.stream(Enchantment.values())
                .filter(e -> enchantment.conflictsWith(e) && !e.equals(enchantment))
                .map(e -> e.getKey().getKey())
                .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            sender.sendMessage(MM.toComponent("<gray>Conflictos con:</gray> <white>" + String.join(", ", conflicts) + "</white>"));
        } else {
            sender.sendMessage(MM.toComponent("<gray>Conflictos con:</gray> <white>Ninguno</white>"));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length - 2 == 1) {
            return Arrays.stream(Enchantment.values())
                    .map(enchant -> enchant.getKey().getKey().toLowerCase())
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}