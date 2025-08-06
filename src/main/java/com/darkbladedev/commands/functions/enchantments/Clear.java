package com.darkbladedev.commands.functions.enchantments;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

public class Clear implements SubcommandExecutor, TabCompletable {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: /enchantments clear <jugador>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(MM.toComponent("<red>El jugador '" + args[0] + "' no está en línea."));
            return;
        }

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(MM.toComponent("<red>El jugador debe tener un ítem en la mano principal."));
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            sender.sendMessage(MM.toComponent("<red>Este ítem no puede ser modificado."));
            return;
        }

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (enchantments.isEmpty()) {
            sender.sendMessage(MM.toComponent("<red>El ítem no tiene encantamientos."));
            return;
        }

        int count = enchantments.size();
        
        try {
            for (Enchantment enchant : enchantments.keySet()) {
                item.removeEnchantment(enchant);
            }
            
            target.getInventory().setItemInMainHand(item);
            
            sender.sendMessage(MM.toComponent("<green>Se han eliminado " + count + " encantamiento(s) del ítem de " + target.getName() + "."));
            if (sender != target) {
                target.sendMessage(MM.toComponent("<green>Se han eliminado " + count + " encantamiento(s) de tu ítem."));
            }
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al eliminar los encantamientos: " + e.getMessage()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}