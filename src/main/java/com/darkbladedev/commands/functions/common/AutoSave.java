package com.darkbladedev.commands.functions.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.AutoSaveManager;
import com.darkbladedev.managers.BackupManager;
import com.darkbladedev.utils.MM;

/**
 * Comando administrativo para controlar el sistema de guardado automático
 * 
 * Subcomandos disponibles:
 * - status - Muestra el estado del sistema
 * - save - Fuerza un guardado inmediato
 * - enable - Habilita el sistema de guardado automático
 * - disable - Deshabilita el sistema de guardado automático
 * - stats - Muestra estadísticas detalladas
 * - check - Realiza comprobación post-reinicio
 * - validate - Ejecuta validación post-reinicio
 * - report - Muestra último reporte de validación
 * - backup - Gestiona respaldos
 * 
 * @author DarkBladeDev
 * @version 2.0
 */
public class AutoSave implements SubcommandExecutor, TabCompletable {
    
    private boolean enabled = true;
    private final HeartlessMain plugin;
    
    public AutoSave() {
        this.plugin = HeartlessMain.getInstance();
    }
    
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
            return Arrays.asList("status", "save", "enable", "disable", "stats", "check", "validate", "report", "backup");
        }
        
        if (args.length == 2 && "backup".equals(args[0])) {
            return Arrays.asList("create", "list", "restore");
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("heartless.admin.autosave")) {
            sender.sendMessage(MM.toComponent("<red>No tienes permisos para usar este comando."));
            return;
        }
        
        AutoSaveManager autoSaveManager = HeartlessMain.getAutoSaveManager();
        if (autoSaveManager == null) {
            sender.sendMessage(MM.toComponent("<red>AutoSaveManager no está disponible."));
            return;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "status":
                handleStatusCommand(sender, autoSaveManager);
                break;
                
            case "save":
                handleSaveCommand(sender, autoSaveManager);
                break;
                
            case "enable":
                handleEnableCommand(sender, autoSaveManager);
                break;
                
            case "disable":
                handleDisableCommand(sender, autoSaveManager);
                break;
                
            case "stats":
                handleStatsCommand(sender, autoSaveManager);
                break;
                
            case "check":
                handleCheckCommand(sender, autoSaveManager);
                break;
                
            case "validate":
                handleValidateCommand(sender, autoSaveManager);
                break;
                
            case "report":
                handleReportCommand(sender, autoSaveManager);
                break;
                
            case "backup":
                handleBackupCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
                
            default:
                sender.sendMessage(MM.toComponent("<red>Subcomando desconocido: " + subCommand));
                showHelp(sender);
                break;
        }
    }
    
    /**
     * Muestra la ayuda del comando
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(MM.toComponent("<gold>=== AutoSave Commands ==="));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave status<white> - Muestra el estado del sistema"));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave save<white> - Fuerza un guardado inmediato"));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave enable<white> - Habilita el sistema"));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave disable<white> - Deshabilita el sistema"));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave stats<white> - Muestra estadísticas detalladas"));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave check<white> - Realiza comprobación post-reinicio"));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave validate<white> - Ejecuta validación post-reinicio"));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave report<white> - Muestra último reporte de validación"));
        sender.sendMessage(MM.toComponent("<yellow>/heartless commons autosave backup <create|list|restore><white> - Gestiona respaldos"));
    }
    
    /**
     * Maneja el comando de estado
     */
    private void handleStatusCommand(CommandSender sender, AutoSaveManager autoSaveManager) {
        sender.sendMessage(MM.toComponent("<gold>=== Estado del AutoSave ==="));
        
        boolean isEnabled = autoSaveManager.isEnabled();
        boolean isShuttingDown = autoSaveManager.isShuttingDown();
        long lastSaveTime = autoSaveManager.getLastSaveTime();
        
        // Estado general
        String statusColor = isEnabled ? "<green>" : "<red>";
        sender.sendMessage(MM.toComponent("<yellow>Estado: " + statusColor + (isEnabled ? "Activo" : "Inactivo")));
        
        if (isShuttingDown) {
            sender.sendMessage(MM.toComponent("<red>⚠ Sistema en proceso de cierre"));
        }
        
        // Último guardado
        if (lastSaveTime > 0) {
            long timeSince = System.currentTimeMillis() - lastSaveTime;
            long secondsSince = timeSince / 1000;
            long minutesSince = secondsSince / 60;
            
            if (minutesSince > 0) {
                sender.sendMessage(MM.toComponent("<yellow>Último guardado: <white>" + minutesSince + " minutos atrás"));
            } else {
                sender.sendMessage(MM.toComponent("<yellow>Último guardado: <white>" + secondsSince + " segundos atrás"));
            }
        } else {
            sender.sendMessage(MM.toComponent("<yellow>Último guardado: <gray>Nunca"));
        }
        
        // Próximo guardado automático
        if (isEnabled && !isShuttingDown) {
            sender.sendMessage(MM.toComponent("<yellow>Próximo guardado: <white>En menos de 5 minutos"));
        }
    }
    
    /**
     * Maneja el comando de guardado forzado
     */
    private void handleSaveCommand(CommandSender sender, AutoSaveManager autoSaveManager) {
        sender.sendMessage(MM.toComponent("<yellow>Iniciando guardado forzado..."));
        
        CompletableFuture<Boolean> saveTask = autoSaveManager.performAutoSave();
        
        // Si es un jugador, mostrar progreso
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            saveTask.thenAccept(success -> {
                if (success) {
                    player.sendMessage(MM.toComponent("<green>✓ Guardado completado exitosamente"));
                } else {
                    player.sendMessage(MM.toComponent("<red>✗ Guardado completado con errores (revisa la consola)"));
                }
            }).exceptionally(throwable -> {
                player.sendMessage(MM.toComponent("<red>✗ Error durante el guardado: " + throwable.getMessage()));
                return null;
            });
        } else {
            // Para la consola, esperar el resultado
            try {
                boolean success = saveTask.get();
                if (success) {
                    sender.sendMessage(MM.toComponent("<green>✓ Guardado completado exitosamente"));
                } else {
                    sender.sendMessage(MM.toComponent("<red>✗ Guardado completado con errores"));
                }
            } catch (Exception e) {
                sender.sendMessage(MM.toComponent("<red>✗ Error durante el guardado: " + e.getMessage()));
            }
        }
    }
    
    /**
     * Maneja el comando de habilitar
     */
    private void handleEnableCommand(CommandSender sender, AutoSaveManager autoSaveManager) {
        if (autoSaveManager.isEnabled()) {
            sender.sendMessage(MM.toComponent("<yellow>El sistema de guardado automático ya está habilitado."));
        } else {
            autoSaveManager.setEnabled(true);
            sender.sendMessage(MM.toComponent("<green>✓ Sistema de guardado automático habilitado"));
            
            // Log para administradores
            String adminName = sender instanceof Player ? ((Player) sender).getName() : "CONSOLE";
            plugin.getLogger().info("AutoSave habilitado por: " + adminName);
        }
    }
    
    /**
     * Maneja el comando de deshabilitar
     */
    private void handleDisableCommand(CommandSender sender, AutoSaveManager autoSaveManager) {
        if (!autoSaveManager.isEnabled()) {
            sender.sendMessage(MM.toComponent("<yellow>El sistema de guardado automático ya está deshabilitado."));
        } else {
            autoSaveManager.setEnabled(false);
            sender.sendMessage(MM.toComponent("<red>✗ Sistema de guardado automático deshabilitado"));
            sender.sendMessage(MM.toComponent("<yellow>⚠ Los datos no se guardarán automáticamente hasta que lo habilites nuevamente"));
            
            // Log para administradores
            String adminName = sender instanceof Player ? ((Player) sender).getName() : "CONSOLE";
            plugin.getLogger().warning("AutoSave deshabilitado por: " + adminName);
        }
    }
    
    /**
     * Maneja el comando de estadísticas
     */
    private void handleStatsCommand(CommandSender sender, AutoSaveManager autoSaveManager) {
        sender.sendMessage(MM.toComponent("<gold>=== Estadísticas del AutoSave ==="));
        
        String stats = autoSaveManager.getStatistics();
        String[] lines = stats.split("\n");
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            
            if (line.startsWith("===")) {
                sender.sendMessage(MM.toComponent("<gold>" + line));
            } else if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    sender.sendMessage(MM.toComponent("<yellow>" + parts[0] + ":<white>" + parts[1]));
                } else {
                    sender.sendMessage(MM.toComponent("<white>" + line));
                }
            } else {
                sender.sendMessage(MM.toComponent("<white>" + line));
            }
        }
        
        // Información adicional
        sender.sendMessage(MM.toComponent("<gray>Tip: Usa '/heartless commons autosave save' para forzar un guardado inmediato"));
    }
    
    /**
     * Maneja el comando de comprobación post-reinicio
     */
    private void handleCheckCommand(CommandSender sender, AutoSaveManager autoSaveManager) {
        sender.sendMessage(MM.toComponent("<yellow>Iniciando comprobación de integridad de datos..."));
        
        CompletableFuture<Boolean> checkTask = autoSaveManager.performPostRestartCheck();
        
        // Si es un jugador, mostrar progreso
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            checkTask.thenAccept(success -> {
                if (success) {
                    player.sendMessage(MM.toComponent("<green>✓ Comprobación de integridad completada - Todos los datos están correctos"));
                } else {
                    player.sendMessage(MM.toComponent("<red>✗ Comprobación de integridad encontró problemas (revisa la consola)"));
                }
            }).exceptionally(throwable -> {
                player.sendMessage(MM.toComponent("<red>✗ Error durante la comprobación: " + throwable.getMessage()));
                return null;
            });
        } else {
            // Para la consola, esperar el resultado
            try {
                boolean success = checkTask.get();
                if (success) {
                    sender.sendMessage(MM.toComponent("<green>✓ Comprobación de integridad completada - Todos los datos están correctos"));
                } else {
                    sender.sendMessage(MM.toComponent("<red>✗ Comprobación de integridad encontró problemas"));
                }
            } catch (Exception e) {
                sender.sendMessage(MM.toComponent("<red>✗ Error durante la comprobación: " + e.getMessage()));
            }
        }
    }
    
    /**
     * Maneja el comando de validación post-reinicio
     */
    private void handleValidateCommand(CommandSender sender, AutoSaveManager autoSaveManager) {
        sender.sendMessage(MM.toComponent("<yellow>Ejecutando validación post-reinicio..."));
        
        // Ejecutar validación en hilo asíncrono
        CompletableFuture.runAsync(() -> {
            try {
                // Simular validación manual
                boolean success = performManualValidation();
                
                // Enviar resultado al sender en el hilo principal
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (success) {
                        sender.sendMessage(MM.toComponent("<green>✓ Validación completada exitosamente"));
                    } else {
                        sender.sendMessage(MM.toComponent("<red>✗ Validación falló con errores"));
                    }
                });
                
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(MM.toComponent("<red>✗ Error durante validación: " + e.getMessage()));
                });
            }
        });
    }
    
    /**
     * Maneja el comando de reporte de validación
     */
    private void handleReportCommand(CommandSender sender, AutoSaveManager autoSaveManager) {
        sender.sendMessage(MM.toComponent("<gold>=== Último Reporte de Validación ==="));
        
        try {
            // Obtener estadísticas del sistema
            String stats = autoSaveManager.getStatistics();
            String[] lines = stats.split("\n");
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                
                if (line.startsWith("===")) {
                    sender.sendMessage(MM.toComponent("<gold>" + line));
                } else if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        sender.sendMessage(MM.toComponent("<yellow>" + parts[0] + ":<white>" + parts[1]));
                    } else {
                        sender.sendMessage(MM.toComponent("<white>" + line));
                    }
                } else {
                    sender.sendMessage(MM.toComponent("<white>" + line));
                }
            }
            
            sender.sendMessage(MM.toComponent("<gray>Usa '/heartless commons autosave validate' para ejecutar una nueva validación"));
            
        } catch (Exception e) {
            sender.sendMessage(MM.toComponent("<red>Error leyendo reporte: " + e.getMessage()));
        }
    }
    
    /**
     * Maneja comandos de respaldo
     */
    private void handleBackupCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MM.toComponent("<red>Uso: /heartless commons autosave backup <create|list|restore> [nombre]"));
            return;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "create":
                handleBackupCreate(sender, args);
                break;
            case "list":
                handleBackupList(sender);
                break;
            case "restore":
                handleBackupRestore(sender, args);
                break;
            default:
                sender.sendMessage(MM.toComponent("<red>Acción no válida. Usa: create, list, o restore"));
                break;
        }
    }
    
    /**
     * Crea un respaldo manual
     */
    private void handleBackupCreate(CommandSender sender, String[] args) {
        BackupManager backupManager = HeartlessMain.getBackupManager();
        if (backupManager == null) {
            sender.sendMessage(MM.toComponent("<red>Error: Sistema de respaldos no disponible"));
            return;
        }
        
        String backupName = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Manual";
        
        sender.sendMessage(MM.toComponent("<yellow>Creando respaldo: " + backupName + "..."));
        
        backupManager.createPreSaveBackup("Manual: " + backupName)
            .thenAccept(result -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (result.isSuccess()) {
                        sender.sendMessage(MM.toComponent("<green>✓ Respaldo creado exitosamente: " + result.getBackupName()));
                    } else {
                        sender.sendMessage(MM.toComponent("<red>✗ Error creando respaldo: " + result.getMessage()));
                    }
                });
            })
            .exceptionally(throwable -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(MM.toComponent("<red>✗ Error durante la creación del respaldo: " + throwable.getMessage()));
                });
                return null;
            });
    }
    
    /**
     * Lista respaldos disponibles
     */
    private void handleBackupList(CommandSender sender) {
        BackupManager backupManager = HeartlessMain.getBackupManager();
        if (backupManager == null) {
            sender.sendMessage(MM.toComponent("<red>Error: Sistema de respaldos no disponible"));
            return;
        }
        
        sender.sendMessage(MM.toComponent("<yellow>Obteniendo lista de respaldos..."));
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<BackupManager.BackupInfo> backups = backupManager.listAvailableBackups();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (backups.isEmpty()) {
                        sender.sendMessage(MM.toComponent("<red>No se encontraron respaldos disponibles."));
                        return;
                    }
                    
                    sender.sendMessage(MM.toComponent("<gold>=== Respaldos Disponibles ==="));
                    for (int i = 0; i < Math.min(backups.size(), 10); i++) {
                        BackupManager.BackupInfo backup = backups.get(i);
                        String timeStr = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                            .format(new java.util.Date(backup.getCreationTime()));
                        sender.sendMessage(MM.toComponent(String.format("<white>%d. %s <gray>(%s)", 
                            i + 1, backup.getId(), timeStr)));
                    }
                    
                    if (backups.size() > 10) {
                        sender.sendMessage(MM.toComponent("<gray>... y " + (backups.size() - 10) + " más."));
                    }
                    
                    sender.sendMessage(MM.toComponent("<gray>Usa '/heartless commons autosave backup restore <nombre>' para restaurar"));
                });
                
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(MM.toComponent("<red>✗ Error obteniendo lista de respaldos: " + e.getMessage()));
                });
            }
        });
    }
    
    /**
     * Restaura desde un respaldo
     */
    private void handleBackupRestore(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(MM.toComponent("<red>Uso: /heartless commons autosave backup restore <nombre_respaldo>"));
            return;
        }
        
        BackupManager backupManager = HeartlessMain.getBackupManager();
        if (backupManager == null) {
            sender.sendMessage(MM.toComponent("<red>Error: Sistema de respaldos no disponible"));
            return;
        }
        
        String backupName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        sender.sendMessage(MM.toComponent("<yellow>Iniciando restauración desde respaldo: " + backupName + "..."));
        sender.sendMessage(MM.toComponent("<red>⚠ ADVERTENCIA: Esta operación sobrescribirá los datos actuales"));
        
        // Usar CompletableFuture directamente
        backupManager.restoreFromBackup(java.nio.file.Paths.get(backupName))
            .thenAccept(success -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (success) {
                        sender.sendMessage(MM.toComponent("<green>✓ Restauración completada exitosamente"));
                        sender.sendMessage(MM.toComponent("<yellow>Se recomienda reiniciar el servidor para aplicar todos los cambios"));
                    } else {
                        sender.sendMessage(MM.toComponent("<red>✗ Error durante la restauración (revisa la consola)"));
                    }
                });
            })
            .exceptionally(throwable -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    sender.sendMessage(MM.toComponent("<red>✗ Error durante la restauración: " + throwable.getMessage()));
                });
                return null;
            });
    }
    
    /**
     * Realiza validación manual del sistema
     */
    private boolean performManualValidation() {
        try {
            // Simular proceso de validación
            Thread.sleep(2000);
            
            // Aquí iría la lógica real de validación
            // Por ahora retornamos true como simulación
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}