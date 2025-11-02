package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.mechanics.BloodAndIronWeek;
import com.darkbladedev.mechanics.ExplosiveWeek;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.AcidWeek;
import com.darkbladedev.utils.EventType;
import com.darkbladedev.utils.RewardPool;
import com.darkbladedev.utils.MM;

/**
 * Comando para mostrar los desafíos del evento semanal activo
 */
public class Challenges implements SubcommandExecutor, TabCompletable {
    
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
        if (!sender.hasPermission("htl.challenges")) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>No tienes permisos para usar este comando."));
            return;
        }
        
        // Solo los jugadores pueden usar este comando ya que necesitamos verificar su mundo
        if (!(sender instanceof Player)) {
            sender.sendMessage(MM.toComponent(HeartlessMain.getInstance().getPrefix() + " <red>Este comando solo puede ser usado por jugadores."));
            return;
        }
        
        Player player = (Player) sender;
        HeartlessMain plugin = HeartlessMain.getInstance();
        
        // Verificar si hay un evento activo
        WeeklyEvent currentEvent = plugin.getWeeklyEventManager().getCurrentEvent();
        
        if (currentEvent == null || !currentEvent.isActive()) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <red>No hay ningún evento semanal activo en este momento."));
            return;
        }
        
        // Verificar si el evento está activo en el mundo del jugador
        String playerWorldName = player.getWorld().getName();
        if (!isEventActiveInPlayerWorld((AbstractWeeklyEvent) currentEvent, playerWorldName)) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <red>No hay ningún evento activo en tu mundo actual (" + playerWorldName + ")."));
            return;
        }
        
        // Mostrar los desafíos según el tipo de evento
        showEventChallenges(sender, currentEvent);
    }
    
    /**
     * Muestra los desafíos específicos del evento activo
     * @param sender El CommandSender que ejecutó el comando
     * @param event El evento activo
     */
    private void showEventChallenges(CommandSender sender, WeeklyEvent event) {
        String eventName = event.getId();
        EventType eventType = EventType.getByName(eventName);
        
        // Encabezado común
        sender.sendMessage(MM.toComponent("<gray><b>=== <gold>DESAFÍOS DE " + eventName.toUpperCase() + "</gold> <gray><b>==="));
        
        // Verificar si el evento tiene recompensas definidas
        if (eventType == null || !RewardPool.hasRewards(eventType)) {
            sender.sendMessage(MM.toComponent("<yellow>Este evento no tiene desafíos específicos definidos."));
            return;
        }
        
        // Obtener las recompensas del pool
        List<String> rewards = RewardPool.getRewardsForEvent(eventType);
        
        // Mostrar desafíos específicos según el tipo de evento
        if (event instanceof BloodAndIronWeek) {
            showBloodAndIronChallenges(sender, rewards, (BloodAndIronWeek) event);
        } else if (event instanceof ExplosiveWeek) {
            showExplosiveWeekChallenges(sender, rewards, (ExplosiveWeek) event);
        } else if (event instanceof UndeadWeek) {
            showUndeadWeekChallenges(sender, rewards, (UndeadWeek) event);
        } else if (event instanceof AcidWeek) {
            showAcidWeekChallenges(sender, rewards, (AcidWeek) event);
        } else {
            sender.sendMessage(MM.toComponent("<yellow>Este evento no tiene desafíos específicos definidos."));
        }
    }
    
    /**
     * Muestra los desafíos de BloodAndIronWeek
     */
    private void showBloodAndIronChallenges(CommandSender sender, List<String> rewards, BloodAndIronWeek event) {
        UUID playerId = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        
        // Desafío 1: Asesino de Jugadores - Mata a un jugador durante el evento
        Component challenge1 = MM.toComponent("<yellow>1. <gold>Asesino de Jugadores</gold> - <red>Mata</red> a <white>un jugador</white> durante el evento</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "player_killer", 1);
            challenge1 = challenge1.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge1);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(0) + "<white>"));
        
        // Desafío 2: Pentakill - Consigue 5 kills consecutivos sin morir
        Component challenge2 = MM.toComponent("<yellow>2. <gold>Pentakill</gold> - <red>Consigue</red> <white>5 kills consecutivos</white> sin morir</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "pentakill", 5);
            challenge2 = challenge2.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge2);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(1) + "</white>"));
        
        // Desafío 3: Superviviente - Sobrevive hasta el final del evento sin morir
        Component challenge3 = MM.toComponent("<yellow>3. <gold>Superviviente</gold> - <green>Sobrevive</green> hasta el final del evento sin morir</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "survivor", 1);
            challenge3 = challenge3.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge3);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(2) + "</white>"));
        
        // Desafío 4: Asesino en Masa - Consigue 10 o más kills durante el evento
        Component challenge4 = MM.toComponent("<yellow>4. <gold>Asesino en Masa</gold> - <red>Consigue</red> <white>10 o más kills</white> durante el evento</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "mass_killer", 10);
            challenge4 = challenge4.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge4);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(3) + "</white>"));
    }
    
    /**
     * Muestra los desafíos de ExplosiveWeek
     */
    private void showExplosiveWeekChallenges(CommandSender sender, List<String> rewards, ExplosiveWeek event) {
        UUID playerId = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        
        // Desafío 1: Asesino de Ghast - Mata un Ghast durante una tormenta
        Component challenge1 = MM.toComponent("<yellow>1. <gold>Asesino de Ghast</gold>: Mata un <white>Ghast</white> durante una <blue>tormenta</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "ghast_killer", 1);
            challenge1 = challenge1.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge1);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(0) + "</white>"));
        
        // Desafío 2: Coleccionista de Cabezas - Consigue cabezas de Zombie, Skeleton y Creeper
        Component challenge2 = MM.toComponent("<yellow>2. <gold>Coleccionista de Cabezas</gold>: Consigue cabezas de <white>Zombie</white>, <white>Skeleton</white> y <white>Creeper</white></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "mob_head_collector", 3);
            challenge2 = challenge2.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge2);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(1) + "</white>"));
        
        // Desafío 3: Asesino de Tormenta - Mata a un jugador con una explosión durante una tormenta
        Component challenge3 = MM.toComponent("<yellow>3. <gold>Asesino de Tormenta</gold>: Mata a un jugador con una <red>explosión</red> durante una <blue>tormenta</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "storm_killer", 1);
            challenge3 = challenge3.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge3);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(2) + "</white>"));
        
        // Desafío 4: Asesino de Warden en Tormenta - Mata a un Warden durante una tormenta
        Component challenge4 = MM.toComponent("<yellow>4. <gold>Asesino de Warden en Tormenta</gold>: Mata a un <dark_red>Warden</dark_red> durante una <blue>tormenta</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "warden_storm_killer", 1);
            challenge4 = challenge4.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge4);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(3) + "</white>"));
    }
    
    /**
     * Muestra los desafíos de UndeadWeek
     */
    private void showUndeadWeekChallenges(CommandSender sender, List<String> rewards, UndeadWeek event) {
        UUID playerId = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        
        // Desafío 1: Dr. Zomboss - Evita morir durante la Noche Roja
        Component challenge1 = MM.toComponent("<yellow>1. <gold>Dr. Zomboss</gold>: Evita morir durante la <red>Noche Roja</red></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "dr_zomboss", 1);
            challenge1 = challenge1.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge1);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(0) + "</white>"));
        
        // Desafío 2: Superviviente de Infección - Sobrevive 30 minutos infectado
        Component challenge2 = MM.toComponent("<yellow>2. <gold>Superviviente de Infección</gold>: Sobrevive <white>30 minutos</white> <green>infectado</green></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "infection_survivor", 1800);
            challenge2 = challenge2.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge2);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(1) + "</white>"));
        
        // Desafío 3: Cazador de Luna Roja - Mata 50 no-muertos en Noche Roja
        Component challenge3 = MM.toComponent("<yellow>3. <gold>Cazador de Luna Roja</gold>: Mata <white>50 no-muertos</white> en <red>Noche Roja</red></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "red_moon_hunter", 50);
            challenge3 = challenge3.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge3);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(2) + "</white>"));
        
        // Desafío 4: Asesino de Wither - Derrota al Wither en Noche Roja
        Component challenge4 = MM.toComponent("<yellow>4. <gold>Asesino de Wither</gold>: Derrota al <dark_red>Wither</dark_red> en <red>Noche Roja</red></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "wither_slayer", 1);
            challenge4 = challenge4.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge4);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(3) + "</white>"));
    }
    
    /**
     * Muestra los desafíos de AcidWeek
     */
    private void showAcidWeekChallenges(CommandSender sender, List<String> rewards, AcidWeek event) {
        UUID playerId = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        
        // Desafío 1: Coleccionista de Peces - Consigue los 4 tipos de pescados en cubetas
        Component challenge1 = MM.toComponent("<yellow>1. <gold>Coleccionista de Peces</gold>: Consigue los <white>4 tipos</white> de pescados en <blue>cubetas</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "fish_collector", 4);
            challenge1 = challenge1.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge1);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(0) + "</white>"));
        
        // Desafío 2: Superviviente de Lluvia Ácida - Sobrevive 90 segundos bajo lluvia ácida
        Component challenge2 = MM.toComponent("<yellow>2. <gold>Superviviente de Lluvia Ácida</gold>: <green>Sobrevive</green> <white>90 segundos</white> bajo lluvia ácida sin equipo especial</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "acid_rain_survivor", 90);
            challenge2 = challenge2.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge2);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(1) + "</white>"));
        
        // Desafío 3: Asesino Químico - Mata a un jugador con botella de agua arrojadiza
        Component challenge3 = MM.toComponent("<yellow>3. <gold>Asesino Químico</gold>: Mata a un jugador con <blue>botella de agua</blue> arrojadiza</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "chemical_killer", 1);
            challenge3 = challenge3.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge3);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(2) + "</white>"));
        
        // Desafío 4: Ajolote Azul - Consigue un ajolote azul
        Component challenge4 = MM.toComponent("<yellow>4. <gold>Ajolote Azul</gold>: Consigue un <blue>ajolote azul</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "blue_axolotl", 1);
            challenge4 = challenge4.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge4);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(3) + "</white>"));
    }
    
    /**
     * Obtiene el texto de hover con el progreso del desafío
     * @param event El evento activo
     * @param playerId El UUID del jugador
     * @param challengeId El ID del desafío
     * @param targetProgress El progreso objetivo del desafío
     * @return El texto de hover formateado
     */
    private String getProgressHover(Object event, UUID playerId, String challengeId, int targetProgress) {
        int currentProgress = 0;
        boolean isCompleted = false;
        
        // Obtener el progreso actual del desafío desde el evento
        if (event instanceof AbstractWeeklyEvent) {
            AbstractWeeklyEvent weeklyEvent = (AbstractWeeklyEvent) event;
            
            // Verificar si el desafío está completado
            isCompleted = weeklyEvent.hasChallengeCompleted(playerId, challengeId);
            
            // SIEMPRE buscar el progreso real almacenado, independientemente del estado de completado
            Map<String, Object> playerProgress = weeklyEvent.getChallengeProgress(playerId);
            Object progressObj = playerProgress.get(challengeId + "_current");
            
            if (progressObj instanceof Integer) {
                currentProgress = (Integer) progressObj;
            } else if (progressObj instanceof Number) {
                currentProgress = ((Number) progressObj).intValue();
            } else if (isCompleted && currentProgress == 0) {
                // Solo como fallback si está completado pero no hay progreso registrado
                currentProgress = targetProgress;
            }
        }
        
        // Colorear el progreso basado en el estado
        String progressColor = "<red>";
        if (isCompleted || currentProgress >= targetProgress) {
            progressColor = "<green>";
        } else if (currentProgress > 0) {
            progressColor = "<yellow>";
        }
        
        // Formatear el texto de hover con el color apropiado
        String status = isCompleted ? "<green>✓ Completado</green>" : "<yellow>En progreso</yellow>";
        return progressColor + "Progreso: " + currentProgress + "/" + targetProgress + "</white>\n" + status;
    }
    
    /**
     * Verifica si un evento está activo en el mundo específico del jugador
     * usando reflexión para acceder al campo protegido worldEventStatus
     */
    private boolean isEventActiveInPlayerWorld(AbstractWeeklyEvent event, String worldName) {
        try {
            java.lang.reflect.Field worldEventStatusField = AbstractWeeklyEvent.class.getDeclaredField("worldEventStatus");
            worldEventStatusField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            java.util.Map<String, java.util.concurrent.atomic.AtomicBoolean> worldEventStatus = 
                (java.util.Map<String, java.util.concurrent.atomic.AtomicBoolean>) worldEventStatusField.get(event);
            
            // Si el mundo no está en el mapa, el evento no está activo
            if (worldEventStatus == null || !worldEventStatus.containsKey(worldName)) {
                return false;
            }
            
            // Obtener el AtomicBoolean y verificar su valor
            java.util.concurrent.atomic.AtomicBoolean atomicStatus = worldEventStatus.get(worldName);
            return atomicStatus != null && atomicStatus.get();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // En caso de error, asumir que el evento no está activo en el mundo
            HeartlessMain.getInstance().getLogger().warning("Error al acceder al estado del evento por mundo: " + e.getMessage());
            return false;
        }
    }
}