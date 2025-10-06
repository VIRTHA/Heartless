package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.exceptions.CustomException;
import com.darkbladedev.exceptions.ExceptionBuilder;
import com.darkbladedev.exceptions.NullEventException;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.utils.MM;

public class Status implements SubcommandExecutor, TabCompletable {
    
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
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        HeartlessMain plugin = HeartlessMain.getInstance();
        StringBuilder status = new StringBuilder();
        status.append("<gray>========== " + HeartlessMain.getInstance().getPrefix() + " ==========</gray>\n");

        // Get current event info
        WeeklyEvent currentEvent = null;
        try {
            currentEvent = plugin.getWeeklyEventManager().getCurrentEvent();
        } catch (Exception e) {
            CustomException ce = ExceptionBuilder.build(NullEventException.class, currentEvent, "El evento solicitado parece ser nulo.");
            ExceptionBuilder.sendToConsole(ce);

            // Informamos al usuario que no hay evento activo
            status.append("<white>Evento activo: <red>Ninguno");
            sender.sendMessage(MM.toComponent(status.toString()));
            return;
        }
        
        if (currentEvent == null) {
            status.append("<white>Evento activo: <red>Ninguno");
            sender.sendMessage(MM.toComponent(status.toString()));
            return;
        }

        // Build enhanced status message with world-specific information
        status.append("<white>Evento activo: <aqua>").append(currentEvent.getId()).append("\n");
        status.append("<white>Estado general: <aqua>").append(currentEvent.isPaused() ? "<red><b>Pausado" : "<green><b>Activo").append("\n");

        // Time remaining - obtener tiempo restante del WeeklyEventManager
        long remainingTimeMs = plugin.getWeeklyEventManager().getRemainingTime();
        if (remainingTimeMs > 0) {
            String formattedTime = formatDuration(remainingTimeMs);
            status.append("<white>Tiempo restante: <aqua>").append(formattedTime).append("\n");
        } else {
            status.append("<white>Tiempo restante: <red>Finalizando...\n");
        }

        // Total participants
        status.append("<white>Participantes totales: <aqua>").append(currentEvent.getActivePlayerCount()).append("\n");

        // World-specific information if it's an AbstractWeeklyEvent
        if (currentEvent instanceof AbstractWeeklyEvent) {
            AbstractWeeklyEvent abstractEvent = (AbstractWeeklyEvent) currentEvent;
            Set<String> activeWorlds = abstractEvent.getActiveWorlds();
            
            if (!activeWorlds.isEmpty()) {
                status.append("\n<yellow><b>═══ ESTADO POR MUNDO ═══</b></yellow>\n");
                
                for (String worldName : activeWorlds) {
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;
                    
                    status.append("\n<white><b>🌍 Mundo:</b> <aqua>").append(worldName).append("\n");
                    
                    // World event status
                    boolean isActiveInWorld = getWorldEventStatus(abstractEvent, worldName);
                    status.append("   <white>Estado: ").append(isActiveInWorld ? "<green>Activo" : "<red>Inactivo").append("\n");
                    
                    // Players in this world
                    Set<UUID> worldPlayers = getWorldActivePlayers(abstractEvent, worldName);
                    status.append("   <white>Participantes: <aqua>").append(worldPlayers.size()).append("\n");
                    
                    if (!worldPlayers.isEmpty() && worldPlayers.size() <= 10) {
                        status.append("   <white>Jugadores: <gray>");
                        boolean first = true;
                        for (UUID playerId : worldPlayers) {
                            Player player = Bukkit.getPlayer(playerId);
                            if (player != null && player.isOnline()) {
                                if (!first) status.append(", ");
                                status.append(player.getName());
                                first = false;
                            }
                        }
                        status.append("\n");
                    } else if (worldPlayers.size() > 10) {
                        status.append("   <white>Jugadores: <gray>").append(worldPlayers.size()).append(" jugadores (lista muy larga)\n");
                    }
                    
                    // World start time
                    long worldStartTime = getWorldStartTime(abstractEvent, worldName);
                    if (worldStartTime > 0) {
                        long worldDuration = System.currentTimeMillis() - worldStartTime;
                        status.append("   <white>Tiempo activo: <aqua>").append(formatDuration(worldDuration)).append("\n");
                    }
                }
            }
        }

        // Event rewards
        status.append("\n<yellow><b>═══ RECOMPENSAS ═══</b></yellow>\n");
        for (String reward : currentEvent.getRewards()) {
            status.append("   <green><b>|></b></green> ").append(reward + "\n");
        }

        status.append("<gray>==============================");
        sender.sendMessage(MM.toComponent(status.toString()));
    }
    
    /**
     * Formatea una duración en milisegundos a texto legible
     * @param duration Duración en milisegundos
     * @return Texto formateado de la duración
     */
    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        
        if (weeks > 0) {
            long remainingDays = days % 7;
            if (remainingDays > 0) {
                return weeks + "w " + remainingDays + "d " + (hours % 24) + "h";
            } else {
                return weeks + "w " + (hours % 24) + "h " + (minutes % 60) + "m";
            }
        } else if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    /**
     * Obtiene los jugadores activos en un mundo específico usando reflexión
     * para acceder a los datos protegidos de AbstractWeeklyEvent
     * @param abstractEvent El evento abstracto
     * @param worldName Nombre del mundo
     * @return Set de UUIDs de jugadores activos en ese mundo
     */
    private Set<UUID> getWorldActivePlayers(AbstractWeeklyEvent abstractEvent, String worldName) {
        try {
            // Usar reflexión para acceder al campo protegido worldActivePlayers
            java.lang.reflect.Field field = AbstractWeeklyEvent.class.getDeclaredField("worldActivePlayers");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Set<UUID>> worldActivePlayers = (Map<String, Set<UUID>>) field.get(abstractEvent);
            
            Set<UUID> players = worldActivePlayers.get(worldName);
            return players != null ? players : Collections.emptySet();
        } catch (Exception e) {
            // Si falla la reflexión, devolver conjunto vacío
            return Collections.emptySet();
        }
    }
    
    /**
     * Obtiene el tiempo de inicio de un mundo específico usando reflexión
     * @param abstractEvent El evento abstracto
     * @param worldName Nombre del mundo
     * @return Tiempo de inicio en milisegundos, o 0 si no se encuentra
     */
    private long getWorldStartTime(AbstractWeeklyEvent abstractEvent, String worldName) {
        try {
            // Usar reflexión para acceder al campo protegido worldStartTimes
            java.lang.reflect.Field field = AbstractWeeklyEvent.class.getDeclaredField("worldStartTimes");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Long> worldStartTimes = (Map<String, Long>) field.get(abstractEvent);
            
            Long startTime = worldStartTimes.get(worldName);
            return startTime != null ? startTime : 0L;
        } catch (Exception e) {
            // Si falla la reflexión, devolver 0
            return 0L;
        }
    }
    
    /**
     * Obtiene el estado del evento en un mundo específico usando reflexión
     * @param abstractEvent El evento abstracto
     * @param worldName Nombre del mundo
     * @return true si el evento está activo en ese mundo
     */
    private boolean getWorldEventStatus(AbstractWeeklyEvent abstractEvent, String worldName) {
        try {
            // Usar reflexión para acceder al campo protegido worldEventStatus
            java.lang.reflect.Field field = AbstractWeeklyEvent.class.getDeclaredField("worldEventStatus");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, java.util.concurrent.atomic.AtomicBoolean> worldEventStatus = 
                (Map<String, java.util.concurrent.atomic.AtomicBoolean>) field.get(abstractEvent);
            
            java.util.concurrent.atomic.AtomicBoolean status = worldEventStatus.get(worldName);
            return status != null ? status.get() : false;
        } catch (Exception e) {
            // Si falla la reflexión, devolver false
            return false;
        }
    }
    
}
