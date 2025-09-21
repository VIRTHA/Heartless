package com.darkbladedev.challenges.types;

/**
 * Enumeración que define los tipos de desafíos disponibles
 * en el sistema de eventos semanales.
 * 
 * Cada tipo incluye:
 * - Nombre descriptivo
 * - Icono representativo
 * - Categoría de agrupación
 * - Descripción del comportamiento
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public enum ChallengeType {
    
    /**
     * Desafíos de combate - Eliminar entidades, PvP, etc.
     */
    COMBAT("Combate", "⚔", "Acción", 
           "Desafíos relacionados con combate, eliminación de mobs y PvP"),
    
    /**
     * Desafíos de construcción - Colocar bloques, construir estructuras
     */
    BUILDING("Construcción", "🏗", "Creatividad", 
             "Desafíos de construcción, colocación de bloques y arquitectura"),
    
    /**
     * Desafíos de minería - Extraer recursos, explorar cuevas
     */
    MINING("Minería", "⛏", "Recolección", 
           "Desafíos de extracción de recursos y exploración subterránea"),
    
    /**
     * Desafíos de agricultura - Cultivar, criar animales
     */
    FARMING("Agricultura", "🌾", "Supervivencia", 
            "Desafíos de cultivo, cría de animales y producción de alimentos"),
    
    /**
     * Desafíos de exploración - Viajar, descubrir biomas
     */
    EXPLORATION("Exploración", "🗺", "Aventura", 
                "Desafíos de viaje, descubrimiento y exploración del mundo"),
    
    /**
     * Desafíos de comercio - Intercambiar con aldeanos, economía
     */
    TRADING("Comercio", "💰", "Economía", 
            "Desafíos relacionados con comercio, intercambios y economía"),
    
    /**
     * Desafíos de crafteo - Crear objetos, usar mesas de trabajo
     */
    CRAFTING("Crafteo", "🔨", "Creación", 
             "Desafíos de creación de objetos y uso de estaciones de trabajo"),
    
    /**
     * Desafíos de supervivencia - Sobrevivir condiciones adversas
     */
    SURVIVAL("Supervivencia", "❤", "Resistencia", 
             "Desafíos de supervivencia en condiciones adversas"),
    
    /**
     * Desafíos sociales - Interactuar con otros jugadores
     */
    SOCIAL("Social", "👥", "Comunidad", 
           "Desafíos que requieren interacción con otros jugadores"),
    
    /**
     * Desafíos de colección - Recopilar objetos específicos
     */
    COLLECTION("Colección", "📦", "Recolección", 
               "Desafíos de recopilación y acumulación de objetos específicos"),
    
    /**
     * Desafíos de tiempo - Completar tareas en tiempo limitado
     */
    TIMED("Temporal", "⏰", "Velocidad", 
          "Desafíos que deben completarse dentro de un tiempo límite"),
    
    /**
     * Desafíos especiales - Eventos únicos o mecánicas especiales
     */
    SPECIAL("Especial", "✨", "Único", 
            "Desafíos únicos con mecánicas especiales del evento");
    
    private final String displayName;
    private final String icon;
    private final String category;
    private final String description;
    
    /**
     * Constructor del enum de tipo de desafío.
     * 
     * @param displayName Nombre para mostrar
     * @param icon Icono representativo
     * @param category Categoría de agrupación
     * @param description Descripción del tipo
     */
    ChallengeType(String displayName, String icon, String category, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.category = category;
        this.description = description;
    }
    
    /**
     * Obtiene el nombre para mostrar del tipo.
     * 
     * @return Nombre descriptivo
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Obtiene el icono del tipo.
     * 
     * @return Icono Unicode
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * Obtiene la categoría del tipo.
     * 
     * @return Categoría de agrupación
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Obtiene la descripción del tipo.
     * 
     * @return Descripción detallada
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Obtiene el nombre con icono.
     * 
     * @return Nombre formateado con icono
     */
    public String getFormattedName() {
        return icon + " " + displayName;
    }
    
    /**
     * Obtiene el nombre completo con categoría.
     * 
     * @return Nombre con categoría
     */
    public String getFullName() {
        return category + " - " + displayName;
    }
    
    /**
     * Convierte una cadena a tipo de desafío.
     * 
     * @param type Cadena del tipo
     * @return ChallengeType correspondiente
     * @throws IllegalArgumentException si el tipo no es válido
     */
    public static ChallengeType fromString(String type) {
        if (type == null) {
            throw new IllegalArgumentException("El tipo no puede ser nulo");
        }
        
        String normalized = type.toLowerCase().trim();
        for (ChallengeType challengeType : values()) {
            if (challengeType.name().toLowerCase().equals(normalized) ||
                challengeType.displayName.toLowerCase().equals(normalized) ||
                challengeType.category.toLowerCase().equals(normalized)) {
                return challengeType;
            }
        }
        
        throw new IllegalArgumentException("Tipo de desafío no válido: " + type);
    }
    
    /**
     * Obtiene todos los tipos por categoría.
     * 
     * @param category Categoría a filtrar
     * @return Array de tipos en la categoría
     */
    public static ChallengeType[] getByCategory(String category) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.category.equalsIgnoreCase(category))
                .toArray(ChallengeType[]::new);
    }
    
    /**
     * Obtiene todas las categorías disponibles.
     * 
     * @return Array de categorías únicas
     */
    public static String[] getCategories() {
        return java.util.Arrays.stream(values())
                .map(type -> type.category)
                .distinct()
                .toArray(String[]::new);
    }
    
    /**
     * Verifica si un tipo es válido.
     * 
     * @param type Cadena a verificar
     * @return true si es válido
     */
    public static boolean isValid(String type) {
        try {
            fromString(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Obtiene una representación formateada de todos los tipos.
     * 
     * @return Lista de tipos formateada
     */
    public static String getFormattedList() {
        StringBuilder sb = new StringBuilder();
        String currentCategory = "";
        
        for (ChallengeType type : values()) {
            if (!type.category.equals(currentCategory)) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append("§6").append(type.category).append(":§r\n");
                currentCategory = type.category;
            }
            sb.append("  ").append(type.getFormattedName()).append("\n");
        }
        
        return sb.toString();
    }
}