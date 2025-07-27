package com.darkbladedev.managers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.content.semi_custom.CustomEffects;
import com.darkbladedev.content.semi_custom.effects.*;
import com.darkbladedev.utils.MM;

/**
 * Gestor para inicializar y administrar todos los efectos personalizados del plugin.
 * Se encarga de registrar, activar, desactivar y gestionar el ciclo de vida de los efectos.
 */
public class CustomEffectsManager implements Listener {

    private static final String DATA_FILE = "custom_effects_data.json";
    
    private final HeartlessMain plugin;
    private final File dataFile;
    private final Map<String, CustomEffects> registeredEffects;
    private final Set<String> activeEffects;
    
    // Mapa para almacenar jugadores afectados por cada efecto
    private final Map<String, Set<UUID>> affectedPlayers;
    
    /**
     * Constructor del gestor de efectos personalizados
     * @param plugin Instancia principal del plugin
     */
    public CustomEffectsManager(HeartlessMain plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), DATA_FILE);
        this.registeredEffects = new HashMap<>();
        this.activeEffects = new HashSet<>();
        this.affectedPlayers = new HashMap<>();
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Inicializa el gestor de efectos personalizados
     * Registra todos los efectos disponibles y carga los datos guardados
     */
    public void initialize() {
        // Registrar todos los efectos disponibles
        registerAllEffects();
        
        // Cargar datos guardados
        loadSavedData();
        
        Bukkit.getConsoleSender().sendMessage(
            MM.toComponent(plugin.getPrefix() + "<gray>Sistema de efectos personalizados <green>inicializado</green>.")
        );
    }
    
    /**
     * Registra todos los efectos personalizados disponibles
     */
    private void registerAllEffects() {
        // Registrar efecto de infección zombie
        registerEffect(new ZombieInfection(plugin));
        
        // Aquí se pueden registrar más efectos personalizados
        
        Bukkit.getConsoleSender().sendMessage(
            MM.toComponent(plugin.getPrefix() + "<gray>Se han registrado <green>" + registeredEffects.size() + "</green> efectos personalizados.")
        );
    }
    
    /**
     * Registra un efecto personalizado en el gestor
     * @param effect El efecto a registrar
     * @return true si se registró correctamente, false si ya existía
     */
    public boolean registerEffect(CustomEffects effect) {
        String effectName = effect.getid().toLowerCase();
        
        if (registeredEffects.containsKey(effectName)) {
            return false; // Ya existe un efecto con ese nombre
        }
        
        registeredEffects.put(effectName, effect);
        affectedPlayers.put(effectName, new HashSet<>());
        
        return true;
    }
    
    /**
     * Activa un efecto personalizado
     * @param effectName Nombre del efecto a activar
     * @return true si se activó correctamente, false si no existe o ya estaba activo
     */
    public boolean activateEffect(String effectName) {
        effectName = effectName.toLowerCase();
        
        if (!registeredEffects.containsKey(effectName)) {
            return false; // No existe un efecto con ese nombre
        }
        
        if (activeEffects.contains(effectName)) {
            return false; // El efecto ya está activo
        }
        
        CustomEffects effect = registeredEffects.get(effectName);
        effect.setEnabled(true);
        activeEffects.add(effectName);
        
        // Guardar datos
        saveData();
        
        Bukkit.getConsoleSender().sendMessage(
            MM.toComponent(plugin.getPrefix() + "<gray>Efecto <green>" + effect.getid() + "</green> activado.")
        );
        
        return true;
    }
    
    /**
     * Desactiva un efecto personalizado
     * @param effectName Nombre del efecto a desactivar
     * @return true si se desactivó correctamente, false si no existe o ya estaba inactivo
     */
    public boolean deactivateEffect(String effectName) {
        effectName = effectName.toLowerCase();
        
        if (!registeredEffects.containsKey(effectName)) {
            return false; // No existe un efecto con ese nombre
        }
        
        if (!activeEffects.contains(effectName)) {
            return false; // El efecto ya está inactivo
        }
        
        CustomEffects effect = registeredEffects.get(effectName);
        effect.setEnabled(false);
        activeEffects.remove(effectName);
        
        // Guardar datos
        saveData();
        
        Bukkit.getConsoleSender().sendMessage(
            MM.toComponent(plugin.getPrefix() + "<gray>Efecto <red>" + effect.getid() + "</red> desactivado.")
        );
        
        return true;
    }
    
    /**
     * Aplica un efecto a un jugador específico
     * @param effectName Nombre del efecto a aplicar
     * @param player Jugador al que aplicar el efecto
     * @return true si se aplicó correctamente, false si no existe o no está activo
     */
    public boolean applyEffectToPlayer(String effectName, Player player) {
        effectName = effectName.toLowerCase();
        
        if (!registeredEffects.containsKey(effectName)) {
            return false; // No existe un efecto con ese nombre
        }
        
        if (!activeEffects.contains(effectName)) {
            return false; // El efecto no está activo
        }
        
        CustomEffects effect = registeredEffects.get(effectName);
        effect.applyEffect(player);
        
        // Registrar jugador afectado
        affectedPlayers.get(effectName).add(player.getUniqueId());
        
        return true;
    }
    
    /**
     * Elimina un efecto de un jugador específico
     * @param effectName Nombre del efecto a eliminar
     * @param player Jugador del que eliminar el efecto
     * @return true si se eliminó correctamente, false si no existe o no estaba afectado
     */
    public boolean removeEffectFromPlayer(String effectName, Player player) {
        effectName = effectName.toLowerCase();
        
        if (!registeredEffects.containsKey(effectName)) {
            return false; // No existe un efecto con ese nombre
        }
        
        CustomEffects effect = registeredEffects.get(effectName);
        
        if (!effect.isAffected(player)) {
            return false; // El jugador no está afectado por este efecto
        }
        
        effect.removeEffect(player);
        
        // Eliminar jugador de la lista de afectados
        affectedPlayers.get(effectName).remove(player.getUniqueId());
        
        return true;
    }
    
    /**
     * Verifica si un jugador está afectado por un efecto específico
     * @param effectName Nombre del efecto a verificar
     * @param player Jugador a verificar
     * @return true si está afectado, false si no
     */
    public boolean isPlayerAffected(String effectName, Player player) {
        effectName = effectName.toLowerCase();
        
        if (!registeredEffects.containsKey(effectName)) {
            return false; // No existe un efecto con ese nombre
        }
        
        CustomEffects effect = registeredEffects.get(effectName);
        return effect.isAffected(player);
    }
    
    /**
     * Obtiene un efecto personalizado por su nombre
     * @param effectName Nombre del efecto
     * @return El efecto personalizado o null si no existe
     */
    public CustomEffects getEffect(String effectName) {
        return registeredEffects.get(effectName.toLowerCase());
    }
    
    /**
     * Obtiene todos los efectos registrados
     * @return Mapa con todos los efectos registrados
     */
    public Map<String, CustomEffects> getAllEffects() {
        return new HashMap<>(registeredEffects);
    }
    
    /**
     * Obtiene todos los efectos activos
     * @return Conjunto con los nombres de todos los efectos activos
     */
    public Set<String> getActiveEffects() {
        return new HashSet<>(activeEffects);
    }
    
    /**
     * Obtiene todos los jugadores afectados por un efecto específico
     * @param effectName Nombre del efecto
     * @return Conjunto con los UUIDs de todos los jugadores afectados
     */
    public Set<UUID> getAffectedPlayers(String effectName) {
        effectName = effectName.toLowerCase();
        
        if (!affectedPlayers.containsKey(effectName)) {
            return new HashSet<>(); // No existe un efecto con ese nombre
        }
        
        return new HashSet<>(affectedPlayers.get(effectName));
    }
    
    /**
     * Guarda los datos de efectos activos y jugadores afectados
     */
    @SuppressWarnings("unchecked")
    private void saveData() {
        JSONObject data = new JSONObject();
        
        // Guardar efectos activos
        JSONArray activeEffectsArray = new JSONArray();
        for (String effectName : activeEffects) {
            activeEffectsArray.add(effectName);
        }
        data.put("activeEffects", activeEffectsArray);
        
        // Guardar jugadores afectados por cada efecto
        JSONObject affectedPlayersObj = new JSONObject();
        for (Map.Entry<String, Set<UUID>> entry : affectedPlayers.entrySet()) {
            JSONArray playersArray = new JSONArray();
            for (UUID playerId : entry.getValue()) {
                playersArray.add(playerId.toString());
            }
            affectedPlayersObj.put(entry.getKey(), playersArray);
        }
        data.put("affectedPlayers", affectedPlayersObj);
        
        // Guardar datos en archivo
        try (FileWriter writer = new FileWriter(dataFile)) {
            writer.write(data.toJSONString());
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(
                MM.toComponent(plugin.getPrefix() + "<red>Error al guardar datos de efectos personalizados: " + e.getMessage())
            );
        }
    }
    
    /**
     * Carga los datos guardados de efectos activos y jugadores afectados
     */
    private void loadSavedData() {
        if (!dataFile.exists()) {
            return; // No hay datos guardados
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(reader);
            
            // Cargar efectos activos
            JSONArray activeEffectsArray = (JSONArray) data.get("activeEffects");
            if (activeEffectsArray != null) {
                for (Object obj : activeEffectsArray) {
                    String effectName = (String) obj;
                    if (registeredEffects.containsKey(effectName)) {
                        activateEffect(effectName);
                    }
                }
            }
            
            // Cargar jugadores afectados por cada efecto
            JSONObject affectedPlayersObj = (JSONObject) data.get("affectedPlayers");
            if (affectedPlayersObj != null) {
                for (Object key : affectedPlayersObj.keySet()) {
                    String effectName = (String) key;
                    if (registeredEffects.containsKey(effectName)) {
                        JSONArray playersArray = (JSONArray) affectedPlayersObj.get(effectName);
                        for (Object playerObj : playersArray) {
                            String playerIdStr = (String) playerObj;
                            UUID playerId = UUID.fromString(playerIdStr);
                            Player player = Bukkit.getPlayer(playerId);
                            if (player != null && player.isOnline()) {
                                applyEffectToPlayer(effectName, player);
                            }
                        }
                    }
                }
            }
            
        } catch (IOException | ParseException e) {
            Bukkit.getConsoleSender().sendMessage(
                MM.toComponent(plugin.getPrefix() + "<red>Error al cargar datos de efectos personalizados: " + e.getMessage())
            );
        }
    }
    
    /**
     * Limpia todos los recursos utilizados por los efectos
     * Se debe llamar cuando se desactiva el plugin
     */
    public void cleanup() {
        // Guardar datos antes de limpiar
        saveData();
        
        // Limpiar todos los efectos
        for (CustomEffects effect : registeredEffects.values()) {
            effect.cleanup();
        }
        
        // Limpiar colecciones
        registeredEffects.clear();
        activeEffects.clear();
        affectedPlayers.clear();
    }
    
    /**
     * Recarga los datos de efectos personalizados
     */
    public void reload() {
        // Limpiar datos actuales
        for (CustomEffects effect : registeredEffects.values()) {
            effect.setEnabled(false);
        }
        activeEffects.clear();
        
        // Cargar datos guardados
        loadSavedData();
        
        Bukkit.getConsoleSender().sendMessage(
            MM.toComponent(plugin.getPrefix() + "<gray>Sistema de efectos personalizados <green>recargado</green>.")
        );
    }
    
    // Eventos
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Verificar si el jugador estaba afectado por algún efecto al desconectarse
        for (Map.Entry<String, CustomEffects> entry : registeredEffects.entrySet()) {
            String effectName = entry.getKey();
            CustomEffects effect = entry.getValue();
            
            if (effect.isAffected(player)) {
                // Registrar jugador afectado
                affectedPlayers.get(effectName).add(player.getUniqueId());
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Guardar datos al desconectarse
        saveData();
        
        // Eliminar jugador de las listas de afectados en memoria
        for (Set<UUID> players : affectedPlayers.values()) {
            players.remove(playerId);
        }
    }
}