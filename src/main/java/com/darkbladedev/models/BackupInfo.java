package com.darkbladedev.models;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo de datos que representa información sobre un respaldo del sistema.
 * Contiene detalles sobre archivos respaldados, tiempos de creación,
 * duración del proceso y estado de completitud.
 * 
 * @author DarkBladeDev
 * @version 1.0
 */
public class BackupInfo {
    private final String id;
    private final String reason;
    private final long creationTime;
    private final Map<String, Path> files = new HashMap<>();
    private Path backupPath;
    private long duration;
    private boolean completed = false;
    
    /**
     * Constructor para crear información de respaldo.
     * 
     * @param id ID único del respaldo
     * @param reason Razón del respaldo
     * @param creationTime Timestamp de creación
     */
    public BackupInfo(String id, String reason, long creationTime) {
        this.id = id;
        this.reason = reason;
        this.creationTime = creationTime;
    }
    
    /**
     * Añade un archivo al respaldo.
     * 
     * @param type Tipo de archivo
     * @param path Ruta del archivo
     */
    public void addFile(String type, Path path) {
        files.put(type, path);
    }
    
    /**
     * Marca el respaldo como completado.
     * 
     * @param backupPath Ruta del archivo de respaldo
     * @param duration Duración del proceso en milisegundos
     */
    public void setCompleted(Path backupPath, long duration) {
        this.backupPath = backupPath;
        this.duration = duration;
        this.completed = true;
    }
    
    /**
     * Obtiene el ID del respaldo.
     * 
     * @return ID del respaldo
     */
    public String getId() { 
        return id; 
    }
    
    /**
     * Obtiene la razón del respaldo.
     * 
     * @return Razón del respaldo
     */
    public String getReason() { 
        return reason; 
    }
    
    /**
     * Obtiene el timestamp de creación.
     * 
     * @return Timestamp de creación
     */
    public long getCreationTime() { 
        return creationTime; 
    }
    
    /**
     * Obtiene el mapa de archivos respaldados.
     * 
     * @return Mapa de archivos por tipo
     */
    public Map<String, Path> getFiles() { 
        return new HashMap<>(files); 
    }
    
    /**
     * Obtiene la ruta del archivo de respaldo.
     * 
     * @return Ruta del respaldo
     */
    public Path getBackupPath() { 
        return backupPath; 
    }
    
    /**
     * Obtiene la duración del proceso de respaldo.
     * 
     * @return Duración en milisegundos
     */
    public long getDuration() { 
        return duration; 
    }
    
    /**
     * Verifica si el respaldo está completado.
     * 
     * @return true si está completado, false en caso contrario
     */
    public boolean isCompleted() { 
        return completed; 
    }
    
    /**
     * Obtiene el número de archivos en el respaldo.
     * 
     * @return Número de archivos
     */
    public int getFileCount() {
        return files.size();
    }
    
    /**
     * Verifica si el respaldo contiene un tipo específico de archivo.
     * 
     * @param type Tipo de archivo a verificar
     * @return true si contiene el tipo, false en caso contrario
     */
    public boolean hasFileType(String type) {
        return files.containsKey(type);
    }
    
    @Override
    public String toString() {
        return "BackupInfo{" +
                "id='" + id + '\'' +
                ", reason='" + reason + '\'' +
                ", creationTime=" + creationTime +
                ", fileCount=" + files.size() +
                ", completed=" + completed +
                ", duration=" + duration + "ms" +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BackupInfo that = (BackupInfo) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}