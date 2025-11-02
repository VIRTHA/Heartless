package com.darkbladedev.commands.functions.events;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.mechanics.UndeadWeek;
import com.darkbladedev.mechanics.WeeklyEvent;
import com.darkbladedev.utils.MM;

/**
 * Comando de debug para probar el sistema de conteo de noches cíclico
 * Permite simular transiciones día/noche y verificar el funcionamiento del contador
 * Solo funciona cuando el evento UndeadWeek está activo
 */
public class DebugNightCycle implements SubcommandExecutor, TabCompletable {
    
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
        if (args.length == 1) {
            return Arrays.asList("status", "simulate-night", "reset-counter", "set-time", "force-third");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set-time")) {
            return Arrays.asList("day", "night", "0", "6000", "13000", "18000");
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("htl.admin")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permiso para usar este comando de debug."));
            return;
        }
        
        if (args.length == 0) {
            showUsage(sender);
            return;
        }
        
        try {
            // Obtener el evento actual
            WeeklyEvent currentEvent = HeartlessMain.getInstance().getWeeklyEventManager().getCurrentEvent();
            
            if (currentEvent == null) {
                sender.sendMessage(MM.toComponent("<red>No hay ningún evento activo."));
                return;
            }
            
            // Verificar si es UndeadWeek
            if (!(currentEvent instanceof UndeadWeek)) {
                sender.sendMessage(MM.toComponent("<red>El evento actual no es UndeadWeek. Evento actual: " + currentEvent.getId()));
                return;
            }
            
            UndeadWeek undeadWeek = (UndeadWeek) currentEvent;
            String subcommand = args[0].toLowerCase();
            
            switch (subcommand) {
                case "status":
                    showNightCycleStatus(sender, undeadWeek);
                    break;
                case "simulate-night":
                    simulateNightTransition(sender);
                    break;
                case "reset-counter":
                    resetNightCounter(sender, undeadWeek);
                    break;
                case "set-time":
                    if (args.length < 2) {
                        sender.sendMessage(MM.toComponent("<red>Uso: /htl event debug-nightcycle set-time <day|night|tiempo>"));
                        return;
                    }
                    setWorldTime(sender, args[1]);
                    break;
                case "force-third":
                    forceThirdNight(sender, undeadWeek);
                    break;
                default:
                    showUsage(sender);
                    break;
            }
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error al ejecutar comando de debug: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
    private void showUsage(CommandSender sender) {
        sender.sendMessage(MM.toComponent("<yellow>Uso del comando debug-nightcycle:"));
        sender.sendMessage(MM.toComponent("<gray>• <white>/htl event debug-nightcycle status</white> - Mostrar estado del sistema"));
        sender.sendMessage(MM.toComponent("<gray>• <white>/htl event debug-nightcycle simulate-night</white> - Simular transición a noche"));
        sender.sendMessage(MM.toComponent("<gray>• <white>/htl event debug-nightcycle reset-counter</white> - Resetear contador de noches"));
        sender.sendMessage(MM.toComponent("<gray>• <white>/htl event debug-nightcycle set-time <day|night|tiempo></white> - Cambiar tiempo del mundo"));
        sender.sendMessage(MM.toComponent("<gray>• <white>/htl event debug-nightcycle force-third</white> - Forzar tercera noche"));
    }
    
    private void showNightCycleStatus(CommandSender sender, UndeadWeek undeadWeek) {
        World world = Bukkit.getWorlds().get(0);
        long currentTime = world.getTime();
        boolean isNight = currentTime >= 13000 && currentTime <= 23000;
        
        sender.sendMessage(MM.toComponent("<green><bold>═══ ESTADO DEL SISTEMA DE NOCHES ═══</bold></green>"));
        sender.sendMessage(MM.toComponent("<yellow>Tiempo actual del mundo: <white>" + currentTime + "</white>"));
        sender.sendMessage(MM.toComponent("<yellow>Es de noche: <white>" + (isNight ? "Sí" : "No") + "</white>"));
        sender.sendMessage(MM.toComponent("<yellow>Luna Roja activa: <white>" + (undeadWeek.isRedMoonActive() ? "Sí" : "No") + "</white>"));
        
        // Usar reflexión para acceder a campos privados (solo para debug)
        try {
            java.lang.reflect.Field nightCounterField = UndeadWeek.class.getDeclaredField("nightCounter");
            nightCounterField.setAccessible(true);
            java.util.concurrent.atomic.AtomicInteger nightCounter = 
                (java.util.concurrent.atomic.AtomicInteger) nightCounterField.get(undeadWeek);
            
            java.lang.reflect.Field lastNightTimeField = UndeadWeek.class.getDeclaredField("lastNightTime");
            lastNightTimeField.setAccessible(true);
            java.util.concurrent.atomic.AtomicLong lastNightTime = 
                (java.util.concurrent.atomic.AtomicLong) lastNightTimeField.get(undeadWeek);
            
            java.lang.reflect.Field isNightEventScheduledField = UndeadWeek.class.getDeclaredField("isNightEventScheduled");
            isNightEventScheduledField.setAccessible(true);
            java.util.concurrent.atomic.AtomicBoolean isNightEventScheduled = 
                (java.util.concurrent.atomic.AtomicBoolean) isNightEventScheduledField.get(undeadWeek);
            
            sender.sendMessage(MM.toComponent("<yellow>Contador de noches: <white>" + nightCounter.get() + "</white>"));
            sender.sendMessage(MM.toComponent("<yellow>Última noche detectada: <white>" + 
                (lastNightTime.get() > 0 ? new java.util.Date(lastNightTime.get()).toString() : "Nunca") + "</white>"));
            sender.sendMessage(MM.toComponent("<yellow>Evento nocturno programado: <white>" + 
                (isNightEventScheduled.get() ? "Sí" : "No") + "</white>"));
            sender.sendMessage(MM.toComponent("<yellow>Próxima Luna Roja en: <white>" + 
                (3 - (nightCounter.get() % 3)) + " noches</white>"));
                
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error accediendo a campos internos: " + e.getMessage()));
        }
        
        sender.sendMessage(MM.toComponent("<green><bold>═══════════════════════════════════</bold></green>"));
    }
    
    private void simulateNightTransition(CommandSender sender) {
        World world = Bukkit.getWorlds().get(0);
        
        // Cambiar a día primero, luego a noche para simular transición
        world.setTime(6000); // Día
        sender.sendMessage(MM.toComponent("<yellow>Cambiando a día..."));
        
        // Esperar un momento y cambiar a noche
        Bukkit.getScheduler().runTaskLater(HeartlessMain.getInstance(), () -> {
            world.setTime(14000); // Noche
            sender.sendMessage(MM.toComponent("<blue>Cambiando a noche - Transición simulada"));
            sender.sendMessage(MM.toComponent("<green>El sistema debería detectar la nueva noche en los próximos 10 segundos."));
        }, 20L); // 1 segundo de retraso
    }
    
    private void resetNightCounter(CommandSender sender, UndeadWeek undeadWeek) {
        try {
            java.lang.reflect.Field nightCounterField = UndeadWeek.class.getDeclaredField("nightCounter");
            nightCounterField.setAccessible(true);
            java.util.concurrent.atomic.AtomicInteger nightCounter = 
                (java.util.concurrent.atomic.AtomicInteger) nightCounterField.get(undeadWeek);
            
            java.lang.reflect.Field isNightEventScheduledField = UndeadWeek.class.getDeclaredField("isNightEventScheduled");
            isNightEventScheduledField.setAccessible(true);
            java.util.concurrent.atomic.AtomicBoolean isNightEventScheduled = 
                (java.util.concurrent.atomic.AtomicBoolean) isNightEventScheduledField.get(undeadWeek);
            
            nightCounter.set(0);
            isNightEventScheduled.set(false);
            
            sender.sendMessage(MM.toComponent("<green>Contador de noches reseteado a 0."));
            sender.sendMessage(MM.toComponent("<yellow>La próxima Luna Roja ocurrirá en 3 noches."));
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error reseteando contador: " + e.getMessage()));
        }
    }
    
    private void setWorldTime(CommandSender sender, String timeArg) {
        World world = Bukkit.getWorlds().get(0);
        long time;
        
        switch (timeArg.toLowerCase()) {
            case "day":
                time = 6000;
                break;
            case "night":
                time = 14000;
                break;
            default:
                try {
                    time = Long.parseLong(timeArg);
                    if (time < 0 || time > 24000) {
                        sender.sendMessage(MM.toComponent("<red>El tiempo debe estar entre 0 y 24000."));
                        return;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(MM.toComponent("<red>Tiempo inválido. Usa 'day', 'night' o un número entre 0-24000."));
                    return;
                }
                break;
        }
        
        world.setTime(time);
        boolean isNight = time >= 13000 && time <= 23000;
        sender.sendMessage(MM.toComponent("<green>Tiempo del mundo cambiado a: <white>" + time + 
            "</white> (" + (isNight ? "Noche" : "Día") + ")"));
    }
    
    private void forceThirdNight(CommandSender sender, UndeadWeek undeadWeek) {
        try {
            java.lang.reflect.Field nightCounterField = UndeadWeek.class.getDeclaredField("nightCounter");
            nightCounterField.setAccessible(true);
            java.util.concurrent.atomic.AtomicInteger nightCounter = 
                (java.util.concurrent.atomic.AtomicInteger) nightCounterField.get(undeadWeek);
            
            // Establecer contador a 2, para que la próxima noche sea la tercera
            nightCounter.set(2);
            
            sender.sendMessage(MM.toComponent("<yellow>Contador establecido a 2."));
            sender.sendMessage(MM.toComponent("<green>La próxima transición a noche activará la Luna Roja."));
            sender.sendMessage(MM.toComponent("<gray>Usa 'simulate-night' para probar inmediatamente."));
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error forzando tercera noche: " + e.getMessage()));
        }
    }
}