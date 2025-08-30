package com.darkbladedev.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.darkbladedev.events.PlayerInfectedEvent;
import com.darkbladedev.utils.MM;
import com.darkbladedev.events.PlayerCuredEvent;
import com.darkbladedev.HeartlessMain;

/**
 * Listener que maneja los eventos personalizados de infección y curación
 * para proporcionar estadísticas adicionales y funcionalidades extendidas.
 */
public class UndeadWeekStatsListener implements Listener {
    
    @SuppressWarnings("unused")
    private final HeartlessMain plugin;
    
    public UndeadWeekStatsListener(HeartlessMain plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Maneja el evento cuando un jugador es infectado
     * @param event El evento de infección
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInfected(PlayerInfectedEvent event) {
        Player player = event.getPlayer();
        String source = event.getInfectionSource();
        
        // Log detallado para estadísticas
        Bukkit.getLogger().info("[Stats] Infección registrada - Jugador: " + player.getName() + 
                               ", Fuente: " + source + 
                               ", Tiempo: " + System.currentTimeMillis());
        
        // Aquí se pueden agregar más funcionalidades como:
        // - Guardar estadísticas en base de datos
        // - Enviar notificaciones a otros jugadores
        // - Activar efectos especiales
        // - Integración con sistemas de logros
        
        // Ejemplo: Notificar a jugadores cercanos (opcional)
        /*
        for (Player nearbyPlayer : player.getWorld().getPlayers()) {
            if (nearbyPlayer.getLocation().distance(player.getLocation()) <= 50) {
                nearbyPlayer.sendMessage(MM.toComponent("<dark_red>" + player.getName() + " ha sido infectado cerca de ti!"));
            }
        }
        */
    }
    
    /**
     * Maneja el evento cuando un jugador se cura
     * @param event El evento de curación
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCured(PlayerCuredEvent event) {
        Player player = event.getPlayer();
        String cureItem = event.getCureItem().getType().name();
        int totalCures = event.getTotalCuresCount();
        
        // Log detallado para estadísticas
        Bukkit.getLogger().info("[Stats] Curación registrada - Jugador: " + player.getName() + 
                               ", Item: " + cureItem + 
                               ", Total curaciones: " + totalCures + 
                               ", Tiempo: " + System.currentTimeMillis());
        
        // Aquí se pueden agregar más funcionalidades como:
        // - Guardar estadísticas en base de datos
        // - Calcular ratios de supervivencia
        // - Otorgar logros especiales
        // - Activar efectos de celebración
        
        // Ejemplo: Mensaje especial cada 5 curaciones
        if (totalCures > 0 && totalCures % 5 == 0) {
            Bukkit.broadcast(MM.toComponent("<gold>¡" + player.getName() + " ha sobrevivido a " + totalCures + " infecciones!"));
        }
        
        // Ejemplo: Logro especial por primera curación
        if (totalCures == 1) {
            player.sendMessage(MM.toComponent("<green>¡Primera curación!"));
        }
    }
}