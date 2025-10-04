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
        HeartlessMain plugin = HeartlessMain.getInstance();
        
        // Verificar si hay un evento activo
        WeeklyEvent currentEvent = plugin.getWeeklyEventManager().getCurrentEvent();
        
        if (currentEvent == null || !currentEvent.isActive()) {
            sender.sendMessage(MM.toComponent(plugin.getPrefix() + " <red>No hay ningún evento semanal activo en este momento."));
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
        
        // Desafío 1: Mata a 3 jugadores
        Component challenge1 = MM.toComponent("<yellow>1. <red>Mata</red> a <white>3</white> jugadores</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "blood_iron_killer", 3);
            challenge1 = challenge1.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge1);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(0) + "<white>"));
        
        // Desafío 2: No mueras en toda la semana
        Component challenge2 = MM.toComponent("<yellow>2. <green>No mueras</green> en toda la semana</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "blood_iron_survivor", 1);
            challenge2 = challenge2.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge2);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(1) + "</white>"));
        
        // Desafío 3: Mata a 5 jugadores seguidos sin morir
        Component challenge3 = MM.toComponent("<yellow>3. <red>Mata</red> a 5 jugadores seguidos sin morir</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "blood_iron_streak", 5);
            challenge3 = challenge3.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge3);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(2) + "</white>"));
        
        // Desafío 4: Haz más de 10 kills durante la semana
        Component challenge4 = MM.toComponent("<yellow>4. <red>Haz</red> más de <white>10 kills</white> durante la semana</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "blood_iron_massacre", 10);
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
        
        // Desafío 1: Mata un Ghast en el overworld
        Component challenge1 = MM.toComponent("<yellow>1. Mata un <white>Ghast</white> en el <green>overworld</green> durante una <blue>tormenta</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "ghast_killer", 1);
            challenge1 = challenge1.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge1);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(0) + "</white>"));
        
        // Desafío 2: Consigue cabezas de mobs clásicos
        Component challenge2 = MM.toComponent("<yellow>2. Consigue <white>cabezas</white> de mobs clásicos</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "mob_head_collector", 3);
            challenge2 = challenge2.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge2);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(1) + "</white>"));
        
        // Desafío 3: Mata a un jugador en PvP durante una tormenta
        Component challenge3 = MM.toComponent("<yellow>3. Mata a un jugador en <red>PvP</red> durante una <blue>tormenta</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "storm_killer", 1);
            challenge3 = challenge3.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge3);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(2) + "</white>"));
        
        // Desafío 4: Mata a un Warden durante una tormenta
        Component challenge4 = MM.toComponent("<yellow>4. Mata a un <dark_red>Warden</dark_red> durante una <blue>tormenta</blue></yellow>");
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
        
        // Desafío 1: Evita morir durante la Noche Roja
        Component challenge1 = MM.toComponent("<yellow>1. Evita morir durante la <red>Noche Roja</red></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "undead_red_night_survivor", 1);
            challenge1 = challenge1.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge1);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(0) + "</white>"));
        
        // Desafío 2: Cúrate de infección zombie 10 veces
        Component challenge2 = MM.toComponent("<yellow>2. Cúrate de <green>infección zombie</green> <white>10 veces</white></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "undead_infection_survivor", 10);
            challenge2 = challenge2.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge2);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(1) + "</white>"));
        
        // Desafío 3: Mata 50 no-muertos en Noche Roja
        Component challenge3 = MM.toComponent("<yellow>3. Mata <white>50 no-muertos</white> en <red>Noche Roja</red></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "undead_red_night_slayer", 50);
            challenge3 = challenge3.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge3);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(2) + "</white>"));
        
        // Desafío 4: Derrota al Wither en Noche Roja
        Component challenge4 = MM.toComponent("<yellow>4. Derrota al <dark_red>Wither</dark_red> en <red>Noche Roja</red></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "undead_wither_slayer", 1);
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
        
        // Desafío 1: Consigue los 4 tipos de pescados en cubetas
        Component challenge1 = MM.toComponent("<yellow>1. Consigue los <white>4 tipos</white> de pescados en <blue>cubetas</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "acid_fish_collector", 4);
            challenge1 = challenge1.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge1);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(0) + "</white>"));
        
        // Desafío 2: Sobrevive 90 segundos bajo lluvia ácida
        Component challenge2 = MM.toComponent("<yellow>2. <green>Sobrevive</green> <white>90 segundos</white> bajo lluvia ácida sin pociones ni armadura especial</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "acid_rain_survivor", 90);
            challenge2 = challenge2.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge2);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(1) + "</white>"));
        
        // Desafío 3: Mata a un jugador con botella de agua arrojadiza
        Component challenge3 = MM.toComponent("<yellow>3. Mata a un jugador con <blue>botella de agua</blue> arrojadiza</yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "acid_water_killer", 1);
            challenge3 = challenge3.hoverEvent(HoverEvent.showText(MM.toComponent(hoverText)));
        }
        sender.sendMessage(challenge3);
        sender.sendMessage(MM.toComponent("<white>   <white>Recompensa:</white> " + rewards.get(2) + "</white>"));
        
        // Desafío 4: Consigue un ajolote azul
        Component challenge4 = MM.toComponent("<yellow>4. Consigue un <blue>ajolote azul</blue></yellow>");
        if (playerId != null) {
            String hoverText = getProgressHover(event, playerId, "acid_blue_axolotl", 1);
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
        
        // Obtener el progreso actual del desafío desde el evento
        if (event instanceof AbstractWeeklyEvent) {
            AbstractWeeklyEvent weeklyEvent = (AbstractWeeklyEvent) event;
            Map<String, Object> playerProgress = weeklyEvent.getChallengeProgress(playerId);
            
            // Buscar el progreso actual del desafío específico
            Object progressObj = playerProgress.get(challengeId + "_current");
            if (progressObj instanceof Integer) {
                currentProgress = (Integer) progressObj;
            } else if (progressObj instanceof Number) {
                currentProgress = ((Number) progressObj).intValue();
            }
        }
        
        // Colorear el progreso basado en el estado
        String progressColor = "<red>";
        if (currentProgress >= targetProgress) {
            progressColor = "<green>";
        } else if (currentProgress > 0) {
            progressColor = "<yellow>";
        }
        
        return "<gray>Progreso: " + progressColor + currentProgress + "/" + targetProgress + "</gray>";
    }
}