package com.darkbladedev.challenges.integration;

import com.darkbladedev.challenges.StandardizedChallenge;
import com.darkbladedev.challenges.types.ChallengeDifficulty;
import com.darkbladedev.challenges.types.ChallengeType;
import com.darkbladedev.challenges.validation.ChallengeValidator;
import com.darkbladedev.mechanics.AbstractWeeklyEvent;
import com.darkbladedev.mechanics.ChallengeDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper para integrar el nuevo sistema de desafíos estandarizados con el sistema existente.
 * 
 * Proporciona métodos para:
 * - Conversión bidireccional entre formatos
 * - Migración de desafíos existentes
 * - Validación de integridad
 * - Registro en eventos semanales
 * 
 * Formatos soportados:
 * - StandardizedChallenge ↔ ChallengeDefinition (via reflexión)
 * - Migración automática de datos existentes
 * - Validación de consistencia entre formatos
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2025
 */
public class ChallengeIntegrationHelper {
    
    // === CONVERSIÓN A SISTEMA EXISTENTE ===
    
    /**
     * Convierte un StandardizedChallenge al formato ChallengeDefinition del sistema existente.
     * 
     * @param challenge El desafío estandarizado a convertir
     * @return ChallengeDefinition compatible con AbstractWeeklyEvent
     * @throws IllegalArgumentException si el challenge es null o inválido
     */
    public static ChallengeDefinition toExistingFormat(StandardizedChallenge challenge) {
        if (challenge == null) {
            throw new IllegalArgumentException("El desafío no puede ser null");
        }
        
        // Validar el desafío antes de la conversión
        ChallengeValidator.validateChallenge(challenge);
        
        System.out.println("🔄 Convirtiendo desafío estandarizado a formato existente: " + challenge.getId());
        
        try {
            return new ChallengeDefinition(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                calculateRequiredProgress(challenge),
                challenge.getRewards()
            );
        } catch (Exception e) {
            System.err.println("❌ Error creando ChallengeDefinition: " + e.getMessage());
            throw new RuntimeException("No se pudo crear ChallengeDefinition para: " + challenge.getId(), e);
        }
    }
    
    /**
     * Convierte múltiples StandardizedChallenge al formato existente.
     * 
     * @param standardizedChallenges Lista de desafíos estandarizados
     * @return Lista de ChallengeDefinition compatibles
     */
    public static List<ChallengeDefinition> toExistingFormat(
            List<StandardizedChallenge> standardizedChallenges) {
        
        return standardizedChallenges.stream()
                .map(ChallengeIntegrationHelper::toExistingFormat)
                .collect(Collectors.toList());
    }
    
    // === CONVERSIÓN DESDE SISTEMA EXISTENTE ===
    
    /**
     * Convierte un ChallengeDefinition del sistema existente a StandardizedChallenge.
     * 
     * @param definition La definición del desafío existente
     * @return StandardizedChallenge equivalente
     * @throws IllegalArgumentException si definition es null
     */
    public static StandardizedChallenge fromExistingFormat(ChallengeDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("La definición del desafío no puede ser null");
        }
        
        System.out.println("🔄 Convirtiendo desde formato existente a StandardizedChallenge");
        
        try {
            return StandardizedChallenge.builder()
                .withId(definition.getId())
                .withTitle(definition.getDisplayName())
                .withDescription(definition.getDescription())
                .withType(ChallengeType.SPECIAL) // Inferir tipo por defecto
                .withDifficulty(inferDifficultyFromProgress(definition.getRequiredProgress()))
                .withRequiredProgress(definition.getRequiredProgress())
                .withRewards(definition.getRewards())
                .addMetadata("migratedFrom", "ChallengeDefinition")
                .buildStandardized();
        } catch (Exception e) {
            System.err.println("❌ Error convirtiendo desde formato existente: " + e.getMessage());
            throw new RuntimeException("No se pudo convertir ChallengeDefinition", e);
        }
    }
    
    /**
     * Convierte múltiples ChallengeDefinition al formato estandarizado.
     * 
     * @param existingChallenges Lista de desafíos existentes
     * @return Lista de StandardizedChallenge equivalentes
     */
    public static List<StandardizedChallenge> fromExistingFormat(
            List<ChallengeDefinition> existingChallenges) {
        if (existingChallenges == null) {
            return new ArrayList<>();
        }
        
        return existingChallenges.stream()
                .filter(definition -> definition != null)
                .map(ChallengeIntegrationHelper::fromExistingFormat)
                .collect(Collectors.toList());
    }
    
    // === MÉTODOS DE INTEGRACIÓN PARA EVENTOS ===
    
    /**
     * Registra desafíos estandarizados en un evento existente.
     * 
     * @param event El evento donde registrar los desafíos
     * @param challenges Lista de desafíos estandarizados
     */
    public static void registerStandardizedChallenges(AbstractWeeklyEvent event, List<StandardizedChallenge> challenges) {
        if (event == null || challenges == null) {
            throw new IllegalArgumentException("Event y challenges no pueden ser null");
        }
        
        System.out.println("📝 Registrando " + challenges.size() + " desafíos estandarizados en: " + 
                          event.getClass().getSimpleName());
        
        for (StandardizedChallenge challenge : challenges) {
            try {
                // Convertir al formato existente
                ChallengeDefinition definition = toExistingFormat(challenge);
                
                // Registrar en el evento (usando reflexión o método público si existe)
                registerChallengeInEvent(event, challenge.getId(), definition);
                
                System.out.println("✅ Desafío registrado: " + challenge.getId());
            } catch (Exception e) {
                System.err.println("❌ Error registrando desafío " + challenge.getId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("📊 Registro completado para evento: " + event.getClass().getSimpleName());
    }
    
    /**
     * Migra desafíos existentes de un evento al nuevo formato.
     * 
     * @param event El evento con desafíos existentes
     * @return Lista de desafíos estandarizados
     */
    public static List<StandardizedChallenge> migrateExistingChallenges(AbstractWeeklyEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("El evento no puede ser null");
        }
        
        System.out.println("🔄 Migrando desafíos existentes del evento: " + event.getClass().getSimpleName());
        
        List<StandardizedChallenge> migratedChallenges = new ArrayList<>();
        List<ChallengeDefinition> existingChallenges = getExistingChallenges(event);
        
        for (ChallengeDefinition existing : existingChallenges) {
            try {
                StandardizedChallenge migrated = fromExistingFormat(existing);
                migratedChallenges.add(migrated);
                System.out.println("✅ Desafío migrado: " + existing.getId());
            } catch (Exception e) {
                System.err.println("❌ Error migrando desafío: " + e.getMessage());
            }
        }
        
        System.out.println("📊 Migración completada: " + migratedChallenges.size() + " desafíos migrados");
        return migratedChallenges;
    }
    
    // === MÉTODOS DE VALIDACIÓN ===
    
    /**
     * Valida la compatibilidad entre un StandardizedChallenge y el sistema existente.
     * 
     * @param challenge Desafío a validar
     * @return true si es compatible, false en caso contrario
     */
    public static boolean isCompatibleWithExistingSystem(StandardizedChallenge challenge) {
        if (challenge == null) return false;
        
        try {
            // Intentar conversión
            ChallengeDefinition converted = toExistingFormat(challenge);
            
            // Usar métodos públicos en lugar de reflexión
            String id = converted.getId();
            String displayName = converted.getDisplayName();
            String description = converted.getDescription();
            int requiredProgress = converted.getRequiredProgress();
            List<String> rewards = converted.getRewards();
            
            return id != null && !id.isEmpty() &&
                   displayName != null && !displayName.isEmpty() &&
                   description != null && !description.isEmpty() &&
                   requiredProgress > 0 &&
                   rewards != null && !rewards.isEmpty();
                   
        } catch (Exception e) {
            System.err.println("❌ Error validando compatibilidad: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida múltiples desafíos para compatibilidad.
     * 
     * @param challenges Lista de desafíos a validar
     * @return Mapa con resultados de validación (ID -> compatible)
     */
    public static Map<String, Boolean> validateCompatibility(List<StandardizedChallenge> challenges) {
        return challenges.stream()
                .collect(Collectors.toMap(
                        StandardizedChallenge::getId,
                        ChallengeIntegrationHelper::isCompatibleWithExistingSystem
                ));
    }
    
    /**
     * Calcula el progreso requerido basado en la dificultad del desafío.
     * 
     * @param challenge El desafío estandarizado
     * @return Progreso requerido como entero
     */
    private static int calculateRequiredProgress(StandardizedChallenge challenge) {
        // Usar el progreso requerido original del desafío
        return challenge.getRequiredProgress();
    }
    
    
    /**
     * Infiere la dificultad basada en el progreso requerido.
     * 
     * @param requiredProgress Progreso requerido
     * @return Dificultad inferida
     */
    private static ChallengeDifficulty inferDifficultyFromProgress(int requiredProgress) {
        if (requiredProgress <= 15) {
            return ChallengeDifficulty.LOW;
        } else if (requiredProgress <= 35) {
            return ChallengeDifficulty.MEDIUM;
        } else {
            return ChallengeDifficulty.HIGH;
        }
    }
    
    
    
    /**
     * Registra un desafío específico en un evento existente.
     * 
     * @param event El evento donde registrar el desafío
     * @param challengeId ID del desafío a registrar
     * @param challengeDefinition La definición del desafío
     * @throws IllegalArgumentException si algún parámetro es null
     */
    public static void registerChallengeInEvent(AbstractWeeklyEvent event, String challengeId, ChallengeDefinition challengeDefinition) {
        if (event == null) {
            throw new IllegalArgumentException("El evento no puede ser null");
        }
        if (challengeId == null || challengeId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del desafío no puede ser null o vacío");
        }
        if (challengeDefinition == null) {
            throw new IllegalArgumentException("La definición del desafío no puede ser null");
        }
        
        System.out.println("📝 Registrando desafío en evento: " + challengeId);
        
        try {
            // Usar reflexión para llamar al método addChallenge
            Method addChallengeMethod = AbstractWeeklyEvent.class.getDeclaredMethod("addChallenge", ChallengeDefinition.class);
            addChallengeMethod.setAccessible(true);
            addChallengeMethod.invoke(event, challengeDefinition);
            
            System.out.println("✅ Desafío registrado exitosamente: " + challengeId);
        } catch (Exception e) {
            System.err.println("❌ Error registrando desafío: " + e.getMessage());
            throw new RuntimeException("No se pudo registrar el desafío: " + challengeId, e);
        }
    }
    
    /**
     * Obtiene desafíos existentes de un evento accediendo al campo availableChallenges.
     * Utiliza reflexión para acceder al campo protegido de AbstractWeeklyEvent.
     */
    private static List<ChallengeDefinition> getExistingChallenges(AbstractWeeklyEvent event) {
        List<ChallengeDefinition> challenges = new ArrayList<>();
        
        try {
            System.out.println("📋 Obteniendo desafíos existentes de: " + event.getClass().getSimpleName());
            
            // Acceder al campo availableChallenges usando reflexión
            Field challengesField = AbstractWeeklyEvent.class.getDeclaredField("availableChallenges");
            challengesField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> availableChallenges = (Map<String, Object>) challengesField.get(event);
            
            if (availableChallenges != null && !availableChallenges.isEmpty()) {
                System.out.println("✅ Encontrados " + availableChallenges.size() + " desafíos en el evento");
                
                // Convertir cada desafío interno a ChallengeDefinition
                for (Map.Entry<String, Object> entry : availableChallenges.entrySet()) {
                    Object challengeObj = entry.getValue();
                    
                    // El objeto puede ser AbstractWeeklyEvent.ChallengeDefinition
                    if (challengeObj != null) {
                        ChallengeDefinition converted = convertInternalChallenge(challengeObj);
                        if (converted != null) {
                            challenges.add(converted);
                        }
                    }
                }
                
                System.out.println("🔄 Convertidos " + challenges.size() + " desafíos exitosamente");
            } else {
                System.out.println("⚠️ No se encontraron desafíos en el evento");
            }
            
        } catch (NoSuchFieldException e) {
            System.err.println("❌ Campo availableChallenges no encontrado: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("❌ No se pudo acceder al campo availableChallenges: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado obteniendo desafíos: " + e.getMessage());
        }
        
        return challenges;
    }
    
    /**
     * Convierte un objeto de desafío interno de AbstractWeeklyEvent a ChallengeDefinition.
     */
    private static ChallengeDefinition convertInternalChallenge(Object internalChallenge) {
        try {
            // Usar reflexión para extraer los datos del desafío interno
            Class<?> challengeClass = internalChallenge.getClass();
            
            Method getIdMethod = challengeClass.getMethod("getId");
            Method getDisplayNameMethod = challengeClass.getMethod("getDisplayName");
            Method getDescriptionMethod = challengeClass.getMethod("getDescription");
            Method getRequiredProgressMethod = challengeClass.getMethod("getRequiredProgress");
            Method getRewardsMethod = challengeClass.getMethod("getRewards");
            
            String id = (String) getIdMethod.invoke(internalChallenge);
            String displayName = (String) getDisplayNameMethod.invoke(internalChallenge);
            String description = (String) getDescriptionMethod.invoke(internalChallenge);
            int requiredProgress = (Integer) getRequiredProgressMethod.invoke(internalChallenge);
            
            @SuppressWarnings("unchecked")
            List<String> rewards = (List<String>) getRewardsMethod.invoke(internalChallenge);
            
            return new ChallengeDefinition(id, displayName, description, requiredProgress, rewards);
            
        } catch (Exception e) {
            System.err.println("❌ Error convirtiendo desafío interno: " + e.getMessage());
            return null;
        }
    }
    
    // === MÉTODOS DE UTILIDAD PÚBLICA ===
    
    /**
     * Verifica la integridad de la conversión bidireccional.
     * 
     * @param original Desafío original
     * @return true si la conversión es íntegra
     */
    public static boolean verifyConversionIntegrity(ChallengeDefinition original) {
        try {
            // Convertir a estandarizado y de vuelta
            StandardizedChallenge standardized = fromExistingFormat(original);
            ChallengeDefinition converted = toExistingFormat(standardized);
            
            // Verificar que los datos esenciales se preserven
            boolean idMatch = Objects.equals(original.getId(), converted.getId());
            boolean titleMatch = Objects.equals(original.getDisplayName(), converted.getDisplayName());
            boolean descMatch = Objects.equals(original.getDescription(), converted.getDescription());
            boolean progressMatch = original.getRequiredProgress() == converted.getRequiredProgress();
            
            return idMatch && titleMatch && descMatch && progressMatch;
                   
        } catch (Exception e) {
            System.err.println("❌ Error verificando integridad: " + e.getMessage());
            return false;
        }
    }
}