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
     * @param sender El remitente del comando
     * @param event El evento activo
     */
    private void showEventChallenges(CommandSender sender, WeeklyEvent event) {
        String eventName = event.getName();
        
        // Encabezado común
        sender.sendMessage(MM.toComponent("<gray><b>=== <gold>DESAFÍOS DE " + eventName.toUpperCase() + "</gold> <gray><b>==="));
        
        // Mostrar desafíos específicos según el tipo de evento
        if (event instanceof BloodAndIronWeek) {
            showBloodAndIronChallenges(sender);
        } else if (event instanceof ExplosiveWeek) {
            showExplosiveWeekChallenges(sender);
        } else if (event instanceof UndeadWeek) {
            showUndeadWeekChallenges(sender);
        } else if (event instanceof AcidWeek) {
            showAcidWeekChallenges(sender);
        } else {
            sender.sendMessage(MM.toComponent("<yellow>Este evento no tiene desafíos específicos definidos."));
        }
        
        // Información adicional
        sender.sendMessage(MM.toComponent("<gray>Usa </gray><white>/heartless event status</white><gray> para ver el estado del evento."));
    }
    
    /**
     * Muestra los desafíos de la Semana de Sangre y Hierro
     */
    private void showBloodAndIronChallenges(CommandSender sender) {
        sender.sendMessage(MM.toComponent("<yellow>1. <red>Mata</red> a <white>3</white> jugadores</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> Encantamiento <gold><u>Adrenaline</u></gold><gray>"));
        sender.sendMessage(MM.toComponent("<yellow>2. <red>Mata</red> a un jugador con poción de daño instantáneo</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> <u>+1</u> corazón permanente</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>3. <red>Mata</red> a 5 jugadores seguidos sin morir</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> <u>Tag</u> \"Pentakill\"</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>4. <green>Sobrevive</green> sin morir en todo el evento (con más de 10 kills)</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   <white>Recompensa:</white> <u>+1</u> corazón permanente</gray>"));
    }
    
    /**
     * Muestra los desafíos de la Semana Explosiva
     */
    private void showExplosiveWeekChallenges(CommandSender sender) {
        sender.sendMessage(MM.toComponent("<yellow>1. Mata a un ghast en el overworld</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: Encantamiento Carve</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>2. Consigue la cabeza de todos los mobs hostiles posibles</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   (Zombie, Esqueleto, Creeper)</gray>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: +1 corazón</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>3. Mata a un jugador con una explosión</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: Tag \"TNTómano\"</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>4. Mata a un warden con la explosión de un creeper eléctrico</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: +1 corazón</gray>"));
    }
    
    /**
     * Muestra los desafíos de la Semana de No-Muertos
     */
    private void showUndeadWeekChallenges(CommandSender sender) {
        sender.sendMessage(MM.toComponent("<yellow>1. Mata a 50 zombies durante el evento</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: Encantamiento First Strike</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>2. Sobrevive a una horda de 20+ zombies</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: +1 corazón permanente</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>3. Mata a un jugador usando solo zombies</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: Tag \"Dr. Zomboss\"</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>4. Convierte a 10 aldeanos en zombies</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: +1 corazón permanente</gray>"));
    }
    
    /**
     * Muestra los desafíos de la Semana Ácida
     */
    private void showAcidWeekChallenges(CommandSender sender) {
        sender.sendMessage(MM.toComponent("<yellow>1. Sobrevive 10 minutos bajo la lluvia ácida</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: Poción de resistencia permanente</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>2. Mata a un jugador mientras estás en agua ácida</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: +1 corazón permanente</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>3. Construye una base completamente resistente al ácido</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: Kit de construcción especial</gray>"));
        sender.sendMessage(MM.toComponent("<yellow>4. Ayuda a 5 jugadores a sobrevivir al ácido</yellow>"));
        sender.sendMessage(MM.toComponent("<gray>   Recompensa: Tag \"Químico\"</gray>"));
    }
}