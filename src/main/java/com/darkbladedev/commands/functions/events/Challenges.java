package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.mechanics.WeeklyEvent;
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
            showBloodAndIronChallenges(sender, rewards);
        } else if (event instanceof ExplosiveWeek) {
            showExplosiveWeekChallenges(sender, rewards);
        } else if (event instanceof UndeadWeek) {
            showUndeadWeekChallenges(sender, rewards);
        } else if (event instanceof AcidWeek) {
            showAcidWeekChallenges(sender, rewards);
        } else {
            sender.sendMessage(MM.toComponent("<yellow>Este evento no tiene desafíos específicos definidos."));
        }
    }
    
    /**
     * Muestra los desafíos de la Semana de Sangre y Hierro
     */
    private void showBloodAndIronChallenges(CommandSender sender, List<String> rewards) {
        sender.sendMessage(MM.toComponent("<yellow>1. <red>Mata</red> a <white>3</white> jugadores</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(0) + "<gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>2. <green>No mueras</green> en toda la semana</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(1) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>3. <red>Mata</red> a 5 jugadores seguidos sin morir</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(2) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>4. <red>Haz</red> más de <white>10 kills</white> durante la semana</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(3) + "</gray>"));
    }
    
    /**
     * Muestra los desafíos de la Semana Explosiva
     */
    private void showExplosiveWeekChallenges(CommandSender sender, List<String> rewards) {
        sender.sendMessage(MM.toComponent("<yellow>1. Mata a un <red>ghast</red> durante una <blue>tormenta</blue></yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(0) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>2. Consigue <white>cabezas</white> de mobs clásicos</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   (Zombie, Esqueleto, Creeper)</gray>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(1) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>3. Mata a un jugador en <red>PvP</red> durante una <blue>tormenta</blue></yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(2) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>4. Mata a un <dark_red>Warden</dark_red> durante una <blue>tormenta</blue></yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(3) + "</gray>"));
    }
    
    /**
     * Muestra los desafíos de la Semana de No-Muertos
     */
    private void showUndeadWeekChallenges(CommandSender sender, List<String> rewards) {
        sender.sendMessage(MM.toComponent("<yellow>1. <green>Cura</green> a <white>5</white> aldeanos zombificados</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(0) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>2. <green>Cúrate</green> la infección zombie <white>10 veces</white></yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(1) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>3. Mata <white>50 no-muertos</white> en <red>Noche Roja</red></yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(2) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>4. Derrota al <dark_red>Wither</dark_red> en <red>Noche Roja</red></yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(3) + "</gray>"));
    }
    
    /**
     * Muestra los desafíos de la Semana Ácida
     */
    private void showAcidWeekChallenges(CommandSender sender, List<String> rewards) {
        sender.sendMessage(MM.toComponent("<yellow>1. Consigue los <white>4 tipos</white> de pescados en <blue>cubetas</blue></yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(0) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>2. <green>Sobrevive</green> <white>1.5 minutos</white> bajo lluvia ácida sin pociones ni armadura especial</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(1) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>3. Mata a un jugador con <blue>botella de agua</blue> arrojadiza</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(2) + "</gray>"));
        
        sender.sendMessage(MM.toComponent("<yellow>4. Consigue un <blue>ajolote azul</blue></yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> " + rewards.get(3) + "</gray>"));
    }
}