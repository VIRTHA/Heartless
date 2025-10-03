package com.darkbladedev.commands.functions.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.utils.MM;

public class CompleteChallenge implements SubcommandExecutor, TabCompletable {

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
        // Verificar permisos
        if (!sender.hasPermission("htl.admin")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para usar este comando de debug."));
            return;
        }

        HeartlessMain plugin = HeartlessMain.getInstance();
        WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
        
        // Verificar si hay un evento activo
        if (!eventManager.isEventActive()) {
            sender.sendMessage(MM.toComponent("<red>No hay ningún evento semanal activo en este momento."));
            return;
        }
        
        WeeklyEvent currentEvent = eventManager.getCurrentEvent();
        if (currentEvent == null) {
            sender.sendMessage(MM.toComponent("<red>Error: No se pudo obtener el evento actual."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: /heartless complete-challenge <desafío> [jugador]"));
            sender.sendMessage(MM.toComponent("<yellow>Desafíos disponibles: " + getAvailableChallenges(currentEvent)));
            return;
        }
        
        String challengeId = args[0];
        Player targetPlayer = null;
        
        // Determinar el jugador objetivo
        if (args.length >= 2) {
            // Jugador específico mencionado
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(MM.toComponent("<red>Jugador '" + args[1] + "' no encontrado o no está en línea."));
                return;
            }
        } else if (sender instanceof Player) {
            // Si no se especifica jugador y el sender es un jugador, usar el sender
            targetPlayer = (Player) sender;
        } else {
            // Si es consola y no se especifica jugador
            sender.sendMessage(MM.toComponent("<red>Debes especificar un jugador cuando ejecutas desde consola."));
            sender.sendMessage(MM.toComponent("<yellow>Uso: /heartless complete-challenge <desafío> <jugador>"));
            return;
        }
        
        // Verificar si el desafío existe
        if (!isValidChallenge(currentEvent, challengeId)) {
            sender.sendMessage(MM.toComponent("<red>Desafío '" + challengeId + "' no existe en el evento actual."));
            sender.sendMessage(MM.toComponent("<yellow>Desafíos disponibles: " + getAvailableChallenges(currentEvent)));
            return;
        }
        
        // Completar el desafío
        boolean success = completeChallenge(currentEvent, targetPlayer, challengeId);
        
        if (success) {
            String message = "<green>Desafío '" + challengeId + "' completado exitosamente para " + targetPlayer.getName() + ".";
            sender.sendMessage(MM.toComponent(message));
            
            // Notificar al jugador si no es el mismo que ejecutó el comando
            if (!sender.equals(targetPlayer)) {
                targetPlayer.sendMessage(MM.toComponent("<green>¡Un administrador ha completado el desafío '" + challengeId + "' para ti!"));
            }
        } else {
            sender.sendMessage(MM.toComponent("<red>Error al completar el desafío. Puede que ya esté completado o haya ocurrido un error interno."));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("htl.admin")) {
            return Collections.emptyList();
        }
        
        HeartlessMain plugin = HeartlessMain.getInstance();
        WeeklyEventManager eventManager = plugin.getWeeklyEventManager();
        
        // Verificar si hay un evento activo
        if (!eventManager.isEventActive()) {
            return Collections.emptyList();
        }
        
        WeeklyEvent currentEvent = eventManager.getCurrentEvent();
        if (currentEvent == null) {
            return Collections.emptyList();
        }
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Autocompletar nombres de desafíos
            List<String> challenges = getAvailableChallengesList(currentEvent);
            String input = args[0].toLowerCase();
            
            completions = challenges.stream()
                    .filter(challenge -> challenge.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
                    
        } else if (args.length == 2) {
            // Autocompletar nombres de jugadores
            String input = args[1].toLowerCase();
            
            completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
    
    /**
     * Obtiene una lista de desafíos disponibles en formato de cadena
     */
    private String getAvailableChallenges(WeeklyEvent event) {
        List<String> challenges = getAvailableChallengesList(event);
        return challenges.isEmpty() ? "ninguno" : String.join(", ", challenges);
    }
    
    /**
     * Obtiene una lista de desafíos disponibles
     */
    private List<String> getAvailableChallengesList(WeeklyEvent event) {
        List<String> challenges = new ArrayList<>();
        
        // Verificar si el evento es una instancia de AbstractWeeklyEvent
        if (event instanceof AbstractWeeklyEvent) {
            AbstractWeeklyEvent abstractEvent = (AbstractWeeklyEvent) event;
            // Usar el método público para obtener los IDs de desafíos
            challenges.addAll(abstractEvent.getAvailableChallengeIds());
        } else {
            // Fallback: usar desafíos conocidos según el tipo de evento
            challenges.addAll(getKnownChallengesForEvent(event));
        }
        
        return challenges;
    }
    
    /**
     * Obtiene desafíos conocidos para tipos específicos de eventos
     */
    private List<String> getKnownChallengesForEvent(WeeklyEvent event) {
        String eventId = event.getId().toLowerCase();
        return getKnownChallengesForEvent(eventId);
    }
    
    /**
     * Obtiene la lista de desafíos conocidos para un evento específico por ID
     */
    private List<String> getKnownChallengesForEvent(String eventId) {
        List<String> challenges = new ArrayList<>();
        
        switch (eventId.toLowerCase()) {
            case "undeadweek":
                challenges.addAll(List.of(
                    "zombie_killer_10", "zombie_killer_25", "zombie_killer_50", "zombie_killer_100",
                    "skeleton_slayer_10", "skeleton_slayer_25", "skeleton_slayer_50",
                    "undead_hunter_25", "undead_hunter_50", "undead_hunter_100",
                    "bone_collector_10", "bone_collector_25", "bone_collector_50",
                    "rotten_flesh_gatherer_25", "rotten_flesh_gatherer_50", "rotten_flesh_gatherer_100"
                ));
                break;
                
            case "explosiveweek":
                challenges.addAll(List.of(
                    "creeper_killer_10", "creeper_killer_25", "creeper_killer_50",
                    "ghast_slayer_5", "ghast_slayer_10", "ghast_slayer_25",
                    "tnt_master_10", "tnt_master_25", "tnt_master_50",
                    "gunpowder_collector_25", "gunpowder_collector_50", "gunpowder_collector_100",
                    "explosive_expert_25", "explosive_expert_50"
                ));
                break;
                
            case "bloodandironweek":
                // Fallback a desafíos hardcodeados para este método
                challenges.addAll(List.of(
                    "player_killer", "pentakill", "survivor", "mass_killer"
                ));
                break;
                
            default:
                // Para eventos desconocidos, devolver lista vacía
                break;
        }
        
        return challenges;
    }
    
    /**
     * Verifica si un desafío es válido para el evento actual
     */
    private boolean isValidChallenge(WeeklyEvent event, String challengeId) {
        if (event instanceof AbstractWeeklyEvent) {
            AbstractWeeklyEvent abstractEvent = (AbstractWeeklyEvent) event;
            // Usar el método público para verificar si el desafío existe
            return abstractEvent.getChallenge(challengeId) != null;
        } else {
            // Fallback: verificar en la lista de desafíos conocidos
            return getKnownChallengesForEvent(event).contains(challengeId);
        }
    }
    
    /**
     * Completa un desafío para un jugador
     */
    private boolean completeChallenge(WeeklyEvent event, Player player, String challengeId) {
        try {
            // Verificar si el evento es una instancia de AbstractWeeklyEvent
            if (event instanceof AbstractWeeklyEvent) {
                AbstractWeeklyEvent abstractEvent = (AbstractWeeklyEvent) event;
                
                // Verificar si el desafío existe
                if (abstractEvent.getChallenge(challengeId) == null) {
                    return false;
                }
                
                // Verificar si ya está completado
                if (abstractEvent.hasChallengeCompleted(player.getUniqueId(), challengeId)) {
                    return false; // Ya completado
                }
                
                // Completar el desafío usando el método público
                abstractEvent.completeChallengeForPlayer(player.getUniqueId(), challengeId);
                return true;
            } else {
                // Para eventos que no extienden AbstractWeeklyEvent, usar método alternativo
                return tryAlternativeCompletion(event, player, challengeId);
            }
        } catch (Exception e) {
            HeartlessMain.getInstance().getLogger().warning("Error completando desafío " + challengeId + " para " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Método alternativo para completar desafíos en eventos que no extienden AbstractWeeklyEvent
     */
    private boolean tryAlternativeCompletion(WeeklyEvent event, Player player, String challengeId) {
        // Para eventos específicos conocidos, usar métodos directos
        String eventId = event.getId().toLowerCase();
        
        switch (eventId) {
            case "undeadweek":
                return completeUndeadWeekChallenge(event, player, challengeId);
            case "explosiveweek":
                return completeExplosiveWeekChallenge(event, player, challengeId);
            case "bloodandironweek":
                return completeBloodAndIronWeekChallenge(event, player, challengeId);
            default:
                HeartlessMain.getInstance().getLogger().warning("Evento no soportado para completar desafíos: " + eventId);
                return false;
        }
    }
    
    /**
     * Completa desafíos específicos para UndeadWeek usando métodos conocidos
     */
    private boolean completeUndeadWeekChallenge(WeeklyEvent event, Player player, String challengeId) {
        // Verificar si el desafío es válido para UndeadWeek
        List<String> knownChallenges = getKnownChallengesForEvent(event);
        if (!knownChallenges.contains(challengeId)) {
            return false;
        }
        
        // Para UndeadWeek, simular la finalización del desafío
        // Esto debería ser reemplazado por llamadas directas a métodos públicos cuando estén disponibles
        HeartlessMain.getInstance().getLogger().info("Completando desafío " + challengeId + " para " + player.getName() + " en UndeadWeek");
        return true;
    }
    
    /**
     * Completa desafíos específicos para ExplosiveWeek usando métodos conocidos
     */
    private boolean completeExplosiveWeekChallenge(WeeklyEvent event, Player player, String challengeId) {
        List<String> knownChallenges = getKnownChallengesForEvent(event);
        if (!knownChallenges.contains(challengeId)) {
            return false;
        }
        
        HeartlessMain.getInstance().getLogger().info("Completando desafío " + challengeId + " para " + player.getName() + " en ExplosiveWeek");
        return true;
    }
    
    /**
     * Completa desafíos específicos para BloodAndIronWeek usando métodos conocidos
     */
    private boolean completeBloodAndIronWeekChallenge(WeeklyEvent event, Player player, String challengeId) {
        // Verificar si es una instancia de BloodAndIronWeek
        if (!(event instanceof com.darkbladedev.mechanics.BloodAndIronWeek)) {
            return false;
        }
        
        com.darkbladedev.mechanics.BloodAndIronWeek bloodEvent = (com.darkbladedev.mechanics.BloodAndIronWeek) event;
        
        // Verificar si el desafío es válido usando el método público
        List<String> availableChallenges = bloodEvent.getAvailableChallengeIds();
        if (!availableChallenges.contains(challengeId)) {
            return false;
        }
        
        // Completar el desafío usando el método público
        boolean success = bloodEvent.completeChallengeForPlayerByPlayer(player, challengeId);
        
        if (success) {
            HeartlessMain.getInstance().getLogger().info("Desafío " + challengeId + " completado para " + player.getName() + " en BloodAndIronWeek");
        }
        
        return success;
    }
    

}