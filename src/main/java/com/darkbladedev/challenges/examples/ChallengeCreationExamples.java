package com.darkbladedev.challenges.examples;

import com.darkbladedev.challenges.ChallengeBuilder;
import com.darkbladedev.challenges.StandardizedChallenge;
import com.darkbladedev.challenges.templates.ChallengeTemplates;
import com.darkbladedev.challenges.templates.QuickChallengeFactory;
import com.darkbladedev.challenges.types.ChallengeDifficulty;
import com.darkbladedev.challenges.types.ChallengeType;
import com.darkbladedev.challenges.validation.ChallengeValidator;

import java.util.Arrays;
import java.util.List;

/**
 * Ejemplos prácticos de uso del sistema de creación de desafíos.
 * 
 * Esta clase demuestra diferentes formas de crear desafíos utilizando
 * el sistema estandarizado, desde métodos básicos hasta configuraciones
 * avanzadas y personalizadas.
 * 
 * Incluye:
 * - Ejemplos básicos con QuickChallengeFactory
 * - Uso de plantillas predefinidas
 * - Creación manual con ChallengeBuilder
 * - Casos de uso comunes en eventos
 * - Mejores prácticas y patrones recomendados
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class ChallengeCreationExamples {
    
    /**
     * Ejemplo 1: Creación ultra-rápida con QuickChallengeFactory
     * 
     * Ideal para: Creación rápida de desafíos comunes
     * Tiempo estimado: 1 línea de código
     */
    public static void example1_QuickCreation() {
        System.out.println("=== EJEMPLO 1: CREACIÓN ULTRA-RÁPIDA ===");
        
        // Crear desafíos con una sola línea
        StandardizedChallenge zombieKill = QuickChallengeFactory.killMobs("zombie", ChallengeDifficulty.LOW);
        StandardizedChallenge stoneMining = QuickChallengeFactory.mineBlocks("stone", ChallengeDifficulty.MEDIUM);
        StandardizedChallenge wheatFarming = QuickChallengeFactory.farmCrops("wheat", ChallengeDifficulty.LOW);
        
        // Desafío completamente aleatorio
        StandardizedChallenge randomChallenge = QuickChallengeFactory.random();
        
        System.out.println("✓ Desafío de zombies: " + zombieKill.getTitle());
        System.out.println("✓ Desafío de minería: " + stoneMining.getTitle());
        System.out.println("✓ Desafío de agricultura: " + wheatFarming.getTitle());
        System.out.println("✓ Desafío aleatorio: " + randomChallenge.getTitle());
    }
    
    /**
     * Ejemplo 2: Uso de plantillas predefinidas
     * 
     * Ideal para: Desafíos con configuraciones específicas
     * Tiempo estimado: 2-3 líneas de código
     */
    public static void example2_TemplateUsage() {
        System.out.println("\n=== EJEMPLO 2: USO DE PLANTILLAS ===");
        
        // Usar plantillas para mayor control
        StandardizedChallenge skeletonHunt = ChallengeTemplates
                .createMobKillChallenge(ChallengeDifficulty.MEDIUM, "skeleton", 20)
                .buildStandardized();
        
        StandardizedChallenge ironMining = ChallengeTemplates
                .createMiningChallenge(ChallengeDifficulty.LOW, "iron_ore", 50)
                .buildStandardized();
        
        StandardizedChallenge pvpChallenge = ChallengeTemplates
                .createPvPChallenge(ChallengeDifficulty.HIGH, 2)
                .addPrerequisite("level:30") // Requisito adicional
                .buildStandardized();
        
        System.out.println("✓ Caza de esqueletos: " + skeletonHunt.getFormattedTitle());
        System.out.println("✓ Minería de hierro: " + ironMining.getFormattedTitle());
        System.out.println("✓ Desafío PvP: " + pvpChallenge.getFormattedTitle());
    }
    
    /**
     * Ejemplo 3: Creación manual con ChallengeBuilder
     * 
     * Ideal para: Desafíos completamente personalizados
     * Tiempo estimado: 5-10 líneas de código
     */
    public static void example3_ManualCreation() {
        System.out.println("\n=== EJEMPLO 3: CREACIÓN MANUAL ===");
        
        // Desafío completamente personalizado
        StandardizedChallenge customChallenge = new ChallengeBuilder()
                .withId("custom_dragon_slayer")
                .withTitle("§6§lMatador de Dragones")
                .withDescription("Derrota al Dragón del End para demostrar tu valentía")
                .withDifficulty(ChallengeDifficulty.HIGH)
                .withType(ChallengeType.COMBAT)
                .withRequiredProgress(1)
                .addTechnicalRequirement("bossType", "ender_dragon")
                .addTechnicalRequirement("mustBeSolo", true)
                .addPrerequisite("level:50")
                .addPrerequisite("item:ender_pearl:16")
                .addReward("money:5000")
                .addReward("item:dragon_egg:1")
                .addReward("title:Dragonslayer")
                .addReward("permission:fly")
                .addMetadata("eventSpecial", "true")
                .addMetadata("broadcastCompletion", "true")
                .buildStandardized();
        
        System.out.println("✓ Desafío personalizado: " + customChallenge.getTitle());
        System.out.println("  Recompensas: " + customChallenge.getRewards().size());
        System.out.println("  Requisitos: " + customChallenge.getPrerequisites().size());
    }
    
    /**
     * Ejemplo 4: Creación de sets balanceados para eventos
     * 
     * Ideal para: Eventos semanales completos
     * Tiempo estimado: 1-2 líneas de código
     */
    public static void example4_EventSets() {
        System.out.println("\n=== EJEMPLO 4: SETS PARA EVENTOS ===");
        
        // Set balanceado automático
        List<StandardizedChallenge> balancedSet = QuickChallengeFactory.balancedSet();
        
        // Set temático de combate
        List<StandardizedChallenge> combatSet = QuickChallengeFactory.createVariedSet(ChallengeType.COMBAT, 4);
        
        // Set personalizado para evento específico
        List<StandardizedChallenge> explosiveWeekSet = ChallengeTemplates.createBalancedChallengeSet("ExplosiveWeek");
        
        System.out.println("✓ Set balanceado: " + balancedSet.size() + " desafíos");
        System.out.println("✓ Set de combate: " + combatSet.size() + " desafíos");
        System.out.println("✓ Set Explosive Week: " + explosiveWeekSet.size() + " desafíos");
        
        // Mostrar detalles del set balanceado
        balancedSet.forEach(challenge -> 
                System.out.println("  - " + challenge.getTitle() + " (" + 
                        challenge.getDifficulty().getDisplayName() + ")"));
    }
    
    /**
     * Ejemplo 5: Desafíos con límite de tiempo
     * 
     * Ideal para: Eventos especiales y competencias
     * Tiempo estimado: 2-3 líneas de código
     */
    public static void example5_TimedChallenges() {
        System.out.println("\n=== EJEMPLO 5: DESAFÍOS TEMPORALES ===");
        
        // Crear desafío base
        ChallengeBuilder baseChallenge = ChallengeTemplates
                .createMobKillChallenge(ChallengeDifficulty.MEDIUM, "creeper", 15);
        
        // Convertir a desafío temporal (30 minutos)
        StandardizedChallenge timedChallenge = ChallengeTemplates
                .createTimedChallenge(baseChallenge, 30)
                .buildStandardized();
        
        // Desafío de velocidad personalizado
        StandardizedChallenge speedChallenge = new ChallengeBuilder()
                .withId("speed_builder")
                .withTitle("Constructor Veloz")
                .withDescription("Construye una casa completa en menos de 10 minutos")
                .withDifficulty(ChallengeDifficulty.MEDIUM)
                .withType(ChallengeType.BUILDING)
                .withRequiredProgress(1)
                .addReward("money:2000")
                .addReward("title:Speed Builder")
                .addTechnicalRequirement("timeLimit", 600) // 10 minutos en segundos
                .addTechnicalRequirement("minBlocks", 50)
                .addTechnicalRequirement("mustInclude", Arrays.asList("door", "window", "roof"))
                .buildStandardized();
        
        System.out.println("✓ Desafío temporal: " + timedChallenge.getTitle());
        System.out.println("✓ Desafío de velocidad: " + speedChallenge.getTitle());
    }
    
    /**
     * Ejemplo 6: Desafíos híbridos y especiales
     * 
     * Ideal para: Mecánicas únicas y eventos especiales
     * Tiempo estimado: 3-5 líneas de código
     */
    public static void example6_SpecialChallenges() {
        System.out.println("\n=== EJEMPLO 6: DESAFÍOS ESPECIALES ===");
        
        // Desafío híbrido (combina dos tipos)
        StandardizedChallenge hybridChallenge = ChallengeTemplates
                .createHybridChallenge(ChallengeDifficulty.HIGH, ChallengeType.COMBAT, ChallengeType.BUILDING)
                .withTitle("§c⚔§6🏗 Guerrero Constructor")
                .withDescription("Elimina 20 mobs Y construye una fortaleza de 15x15")
                .addTechnicalRequirement("mobKills", 20)
                .addTechnicalRequirement("buildingSize", "15x15x8")
                .buildStandardized();
        
        // Desafío especial de evento
        StandardizedChallenge eventSpecial = ChallengeTemplates
                .createSpecialEventChallenge("Halloween", ChallengeDifficulty.HIGH, "Collect 50 pumpkins at night")
                .addTechnicalRequirement("timeRestriction", "night_only")
                .addTechnicalRequirement("biomeRestriction", "plains,forest")
                .buildStandardized();
        
        // Desafío de colección compleja
        List<String> rareItems = Arrays.asList("nether_star", "dragon_egg", "elytra", "totem_of_undying");
        StandardizedChallenge collectionChallenge = ChallengeTemplates
                .createCollectionChallenge(ChallengeDifficulty.HIGH, rareItems)
                .withTitle("§5§lColeccionista Legendario")
                .addReward("money:10000")
                .addReward("title:Legendary Collector")
                .buildStandardized();
        
        System.out.println("✓ Desafío híbrido: " + hybridChallenge.getTitle());
        System.out.println("✓ Desafío especial: " + eventSpecial.getTitle());
        System.out.println("✓ Desafío de colección: " + collectionChallenge.getTitle());
    }
    
    /**
     * Ejemplo 7: Validación y mejores prácticas
     * 
     * Ideal para: Asegurar calidad y consistencia
     * Tiempo estimado: 2-3 líneas adicionales
     */
    public static void example7_ValidationAndBestPractices() {
        System.out.println("\n=== EJEMPLO 7: VALIDACIÓN Y MEJORES PRÁCTICAS ===");
        
        try {
            // Crear desafío con validación
            StandardizedChallenge challenge = new ChallengeBuilder()
                    .withId("validated_challenge")
                    .withTitle("Desafío Validado")
                    .withDescription("Este desafío será validado automáticamente")
                    .withDifficulty(ChallengeDifficulty.MEDIUM)
                    .withType(ChallengeType.MINING)
                    .withRequiredProgress(50)
                    .addReward("money:500")
                    .buildStandardized();
            
            // Validar el desafío
            ChallengeValidator.validateChallenge(challenge);
            System.out.println("✓ Desafío validado correctamente: " + challenge.getTitle());
            
            // Mejores prácticas demostradas:
            System.out.println("\n📋 MEJORES PRÁCTICAS APLICADAS:");
            System.out.println("  ✓ ID único y descriptivo");
            System.out.println("  ✓ Título claro y atractivo");
            System.out.println("  ✓ Descripción informativa");
            System.out.println("  ✓ Dificultad apropiada");
            System.out.println("  ✓ Progreso realista");
            System.out.println("  ✓ Recompensas balanceadas");
            System.out.println("  ✓ Validación automática");
            
        } catch (Exception e) {
            System.err.println("❌ Error en validación: " + e.getMessage());
        }
    }
    
    /**
     * Ejemplo 8: Integración con eventos existentes
     * 
     * Ideal para: Adaptar el sistema a eventos actuales
     * Tiempo estimado: Variable según complejidad
     */
    public static void example8_EventIntegration() {
        System.out.println("\n=== EJEMPLO 8: INTEGRACIÓN CON EVENTOS ===");
        
        // Simular integración con AbstractWeeklyEvent
        System.out.println("📝 PASOS PARA INTEGRACIÓN:");
        System.out.println("1. Crear desafíos con el nuevo sistema");
        System.out.println("2. Convertir a ChallengeDefinition existente");
        System.out.println("3. Registrar en el evento");
        
        // Ejemplo de conversión (pseudocódigo)
        StandardizedChallenge modernChallenge = QuickChallengeFactory.killMobs("zombie", ChallengeDifficulty.MEDIUM);
        
        System.out.println("\n🔄 CONVERSIÓN A SISTEMA EXISTENTE:");
        System.out.println("ID: " + modernChallenge.getId());
        System.out.println("Título: " + modernChallenge.getTitle());
        System.out.println("Descripción: " + modernChallenge.getDescription());
        System.out.println("Progreso requerido: " + modernChallenge.getRequiredProgress());
        System.out.println("Recompensas: " + modernChallenge.getRewards());
        
        // Nota: La conversión real se haría en el método de integración
        System.out.println("\n💡 TIP: Usar ChallengeIntegrationHelper para conversión automática");
    }
    
    /**
     * Método principal para ejecutar todos los ejemplos
     */
    public static void runAllExamples() {
        System.out.println("🚀 SISTEMA DE CREACIÓN DE DESAFÍOS - EJEMPLOS PRÁCTICOS");
        System.out.println("=" .repeat(60));
        
        example1_QuickCreation();
        example2_TemplateUsage();
        example3_ManualCreation();
        example4_EventSets();
        example5_TimedChallenges();
        example6_SpecialChallenges();
        example7_ValidationAndBestPractices();
        example8_EventIntegration();
        
        System.out.println("\n" + "=" .repeat(60));
        System.out.println("✅ TODOS LOS EJEMPLOS EJECUTADOS CORRECTAMENTE");
        System.out.println("📚 Consulta la documentación para más detalles");
    }
    
    /**
     * Ejemplo de uso rápido para desarrolladores
     */
    public static List<StandardizedChallenge> createQuickEventChallenges(String eventName) {
        System.out.println("⚡ CREACIÓN RÁPIDA PARA EVENTO: " + eventName);
        
        // Crear 5 desafíos balanceados en segundos
        return Arrays.asList(
                QuickChallengeFactory.killMobs("zombie", ChallengeDifficulty.LOW),
                QuickChallengeFactory.mineBlocks("stone", ChallengeDifficulty.LOW),
                QuickChallengeFactory.buildWith("cobblestone", ChallengeDifficulty.MEDIUM),
                QuickChallengeFactory.trade(ChallengeDifficulty.MEDIUM),
                ChallengeTemplates.createSpecialEventChallenge(eventName, ChallengeDifficulty.HIGH, 
                        "Complete the " + eventName + " boss fight").buildStandardized()
        );
    }
}