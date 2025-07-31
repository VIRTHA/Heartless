package com.darkbladedev.commands.functions.enchantments;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.utils.MM;

public class List implements SubcommandExecutor, TabCompletable {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(MM.toComponent("<gold>Lista de encantamientos disponibles:</gold>"));
        
        // Obtener todos los encantamientos y ordenarlos alfabéticamente
        @SuppressWarnings("deprecation")
        java.util.List<Enchantment> enchantments = Arrays.stream(Enchantment.values())
                .sorted(Comparator.comparing(e -> e.getKey().getKey()))
                .collect(Collectors.toList());
        
        for (Enchantment enchant : enchantments) {
            String enchantName = enchant.getKey().getKey();
            int maxLevel = enchant.getMaxLevel();
            @SuppressWarnings("deprecation")
            boolean isTreasure = enchant.isTreasure();
            boolean isCursed = enchant.isCursed();
            
            StringBuilder info = new StringBuilder();
            info.append("<yellow>• ").append(enchantName).append("</yellow>");
            info.append(" <gray>(Nivel máximo: ").append(maxLevel).append(")</gray>");
            
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
    public java.util.List<String> onTabComplete(CommandSender sender, String[] args) {
        // No hay argumentos para este comando
        return Collections.emptyList();
    }
}