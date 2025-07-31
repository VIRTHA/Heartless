package com.darkbladedev.commands.functions.enchantments;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.utils.MM;

public class Compare implements SubcommandExecutor, TabCompletable {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MM.toComponent("<red>Uso: /enchantments compare <jugador1> <jugador2>"));
            return;
        }

        Player player1 = Bukkit.getPlayer(args[0]);
        if (player1 == null) {
            sender.sendMessage(MM.toComponent("<red>El jugador '" + args[0] + "' no está en línea."));
            return;
        }

        Player player2 = Bukkit.getPlayer(args[1]);
        if (player2 == null) {
            sender.sendMessage(MM.toComponent("<red>El jugador '" + args[1] + "' no está en línea."));
            return;
        }

        ItemStack item1 = player1.getInventory().getItemInMainHand();
        if (item1 == null || item1.getType().isAir()) {
            sender.sendMessage(MM.toComponent("<red>" + player1.getName() + " no tiene un ítem en la mano principal."));
            return;
        }

        ItemStack item2 = player2.getInventory().getItemInMainHand();
        if (item2 == null || item2.getType().isAir()) {
            sender.sendMessage(MM.toComponent("<red>" + player2.getName() + " no tiene un ítem en la mano principal."));
            return;
        }

        Map<Enchantment, Integer> enchants1 = item1.getEnchantments();
        Map<Enchantment, Integer> enchants2 = item2.getEnchantments();

        if (enchants1.isEmpty() && enchants2.isEmpty()) {
            sender.sendMessage(MM.toComponent("<yellow>Ninguno de los ítems tiene encantamientos.</yellow>"));
            return;
        }

        // Obtener todos los encantamientos únicos entre ambos ítems
        Set<Enchantment> allEnchants = new HashSet<>();
        allEnchants.addAll(enchants1.keySet());
        allEnchants.addAll(enchants2.keySet());

        sender.sendMessage(MM.toComponent("<gold>Comparación de encantamientos:</gold>"));
        sender.sendMessage(MM.toComponent("<gray>Ítem de " + player1.getName() + ": " + item1.getType() + "</gray>"));
        sender.sendMessage(MM.toComponent("<gray>Ítem de " + player2.getName() + ": " + item2.getType() + "</gray>"));
        sender.sendMessage(MM.toComponent("<gray>--------------------------</gray>"));

        for (Enchantment enchant : allEnchants) {
            String enchantName = enchant.getKey().getKey();
            int level1 = enchants1.getOrDefault(enchant, 0);
            int level2 = enchants2.getOrDefault(enchant, 0);

            StringBuilder info = new StringBuilder();
            info.append("<yellow>" + enchantName + ":</yellow> ");

            if (level1 > 0 && level2 > 0) {
                if (level1 > level2) {
                    info.append("<green>" + player1.getName() + " (" + level1 + ")</green> vs <red>" + player2.getName() + " (" + level2 + ")</red>");
                } else if (level2 > level1) {
                    info.append("<red>" + player1.getName() + " (" + level1 + ")</red> vs <green>" + player2.getName() + " (" + level2 + ")</green>");
                } else {
                    info.append("<aqua>" + player1.getName() + " (" + level1 + ")</aqua> = <aqua>" + player2.getName() + " (" + level2 + ")</aqua>");
                }
            } else if (level1 > 0) {
                info.append("<green>" + player1.getName() + " (" + level1 + ")</green> vs <red>" + player2.getName() + " (No tiene)</red>");
            } else {
                info.append("<red>" + player1.getName() + " (No tiene)</red> vs <green>" + player2.getName() + " (" + level2 + ")</green>");
            }

            sender.sendMessage(MM.toComponent(info.toString()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length - 2 == 1 || args.length - 2 == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}