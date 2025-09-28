package com.darkbladedev.commands.functions.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.BanManager;
import com.darkbladedev.managers.EventManager;
import com.darkbladedev.mechanics.HealthRewards;
import com.darkbladedev.mechanics.HealthSteal;
import com.darkbladedev.utils.MM;

/**
 * Comando para gestionar la activación/desactivación de sistemas del plugin
 */
public class SystemManager implements SubcommandExecutor, TabCompletable {

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
        if (!isEnabled()) {
            sender.sendMessage(MM.toComponent("<red>Este comando está deshabilitado."));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(MM.toComponent("<red>Uso: /heartless commons systems <sistema> <enable|disable|status>"));
            sender.sendMessage(MM.toComponent("<yellow>Sistemas disponibles: healthsteal, healthrewards, banmanager"));
            return;
        }

        String system = args[0].toLowerCase();
        String action = args[1].toLowerCase();

        switch (system) {
            case "healthsteal":
                handleHealthStealSystem(sender, action);
                break;
            case "healthrewards":
                handleHealthRewardsSystem(sender, action);
                break;
            case "banmanager":
                handleBanManagerSystem(sender, action);
                break;
            default:
                sender.sendMessage(MM.toComponent("<red>Sistema desconocido: " + system));
                sender.sendMessage(MM.toComponent("<yellow>Sistemas disponibles: healthsteal, healthrewards, banmanager"));
                break;
        }
    }

    private void handleHealthStealSystem(CommandSender sender, String action) {
        EventManager eventManager = HeartlessMain.getInstance().getEventManager();
        HealthSteal healthSteal = eventManager.getHealthStealSystem();

        if (healthSteal == null) {
            sender.sendMessage(MM.toComponent("<red>Error: Sistema HealthSteal no encontrado."));
            return;
        }

        switch (action) {
            case "enable":
                healthSteal.setEnabled(true);
                sender.sendMessage(MM.toComponent("<green>Sistema HealthSteal habilitado."));
                break;
            case "disable":
                healthSteal.setEnabled(false);
                sender.sendMessage(MM.toComponent("<yellow>Sistema HealthSteal deshabilitado."));
                break;
            case "status":
                String status = healthSteal.isEnabled() ? "<green>habilitado" : "<red>deshabilitado";
                sender.sendMessage(MM.toComponent("<gray>Estado del sistema HealthSteal: " + status));
                break;
            default:
                sender.sendMessage(MM.toComponent("<red>Acción no válida. Usa: enable, disable, status"));
                break;
        }
    }

    private void handleHealthRewardsSystem(CommandSender sender, String action) {
        EventManager eventManager = HeartlessMain.getInstance().getEventManager();
        HealthRewards healthRewards = eventManager.getHealthRewardsSystem();

        if (healthRewards == null) {
            sender.sendMessage(MM.toComponent("<red>Error: Sistema HealthRewards no encontrado."));
            return;
        }

        switch (action) {
            case "enable":
                healthRewards.setEnabled(true);
                sender.sendMessage(MM.toComponent("<green>Sistema HealthRewards habilitado."));
                break;
            case "disable":
                healthRewards.setEnabled(false);
                sender.sendMessage(MM.toComponent("<yellow>Sistema HealthRewards deshabilitado."));
                break;
            case "status":
                String status = healthRewards.isEnabled() ? "<green>habilitado" : "<red>deshabilitado";
                sender.sendMessage(MM.toComponent("<gray>Estado del sistema HealthRewards: " + status));
                break;
            default:
                sender.sendMessage(MM.toComponent("<red>Acción no válida. Usa: enable, disable, status"));
                break;
        }
    }

    private void handleBanManagerSystem(CommandSender sender, String action) {
        BanManager banManager = HeartlessMain.getInstance().getBanManager();

        if (banManager == null) {
            sender.sendMessage(MM.toComponent("<red>Error: Sistema BanManager no encontrado."));
            return;
        }

        switch (action) {
            case "enable":
                banManager.setEnabled(true);
                sender.sendMessage(MM.toComponent("<green>Sistema BanManager habilitado."));
                break;
            case "disable":
                banManager.setEnabled(false);
                sender.sendMessage(MM.toComponent("<yellow>Sistema BanManager deshabilitado."));
                break;
            case "status":
                String status = banManager.isEnabled() ? "<green>habilitado" : "<red>deshabilitado";
                sender.sendMessage(MM.toComponent("<gray>Estado del sistema BanManager: " + status));
                break;
            default:
                sender.sendMessage(MM.toComponent("<red>Acción no válida. Usa: enable, disable, status"));
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Autocompletar sistemas disponibles
            return Arrays.asList("healthsteal", "healthrewards", "banmanager")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            // Autocompletar acciones
            return Arrays.asList("enable", "disable", "status")
                    .stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}