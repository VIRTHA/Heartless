package com.darkbladedev.challenges.templates;

import com.darkbladedev.challenges.ChallengeBuilder;
import com.darkbladedev.challenges.StandardizedChallenge;
import com.darkbladedev.challenges.types.ChallengeDifficulty;
import com.darkbladedev.challenges.types.ChallengeType;

import java.util.*;

/**
 * Plantillas predefinidas para la creación rápida y consistente
 * de desafíos en eventos semanales.
 * 
 * Proporciona métodos estáticos para generar desafíos comunes
 * con configuraciones optimizadas y balanceadas según el tipo
 * y dificultad especificados.
 * 
 * Características:
 * - Plantillas para todos los tipos de desafíos
 * - Configuraciones balanceadas por dificultad
 * - Recompensas apropiadas y escalables
 * - Requisitos técnicos preconfigurados
 * - Fácil personalización posterior
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class ChallengeTemplates {
    
    // === PLANTILLAS DE COMBATE ===
    
    /**
     * Crea un desafío de eliminación de mobs.
     * 
     * @param difficulty Dificultad del desafío
     * @param mobType Tipo de mob a eliminar
     * @param count Cantidad a eliminar
     * @return Builder configurado
     */
    public static ChallengeBuilder createMobKillChallenge(ChallengeDifficulty difficulty, 
                                                         String mobType, int count) {
        return new ChallengeBuilder()
                .withId("kill_" + mobType.toLowerCase() + "_" + difficulty.name().toLowerCase())
                .withTitle("Cazador de " + capitalize(mobType))
                .withDescription("Elimina " + count + " " + mobType + " para completar este desafío")
                .withDifficulty(difficulty)
                .withType(ChallengeType.COMBAT)
                .withRequiredProgress(count)
                .addTechnicalRequirement("mobType", mobType)
                .addTechnicalRequirement("killMethod", "any")
                .addReward("money:" + (int)(count * 10 * difficulty.getRewardMultiplier()))
                .addReward("xp:" + (int)(count * 5 * difficulty.getRewardMultiplier()));
    }
    
    /**
     * Crea un desafío de PvP.
     * 
     * @param difficulty Dificultad del desafío
     * @param kills Número de kills requeridos
     * @return Builder configurado
     */
    public static ChallengeBuilder createPvPChallenge(ChallengeDifficulty difficulty, int kills) {
        return new ChallengeBuilder()
                .withId("pvp_kills_" + difficulty.name().toLowerCase())
                .withTitle("Guerrero PvP")
                .withDescription("Elimina " + kills + " jugadores en combate PvP")
                .withDifficulty(difficulty)
                .withType(ChallengeType.COMBAT)
                .withRequiredProgress(kills)
                .addTechnicalRequirement("pvpOnly", true)
                .addTechnicalRequirement("assistsCount", false)
                .addReward("money:" + (int)(kills * 50 * difficulty.getRewardMultiplier()))
                .addReward("title:Guerrero");
    }
    
    // === PLANTILLAS DE MINERÍA ===
    
    /**
     * Crea un desafío de minería de bloques específicos.
     * 
     * @param difficulty Dificultad del desafío
     * @param blockType Tipo de bloque a minar
     * @param count Cantidad a minar
     * @return Builder configurado
     */
    public static ChallengeBuilder createMiningChallenge(ChallengeDifficulty difficulty, 
                                                        String blockType, int count) {
        return new ChallengeBuilder()
                .withId("mine_" + blockType.toLowerCase() + "_" + difficulty.name().toLowerCase())
                .withTitle("Minero de " + capitalize(blockType))
                .withDescription("Extrae " + count + " bloques de " + blockType)
                .withDifficulty(difficulty)
                .withType(ChallengeType.MINING)
                .withRequiredProgress(count)
                .addTechnicalRequirement("blockType", blockType)
                .addTechnicalRequirement("toolRequired", getRequiredTool(blockType))
                .addReward("money:" + (int)(count * 2 * difficulty.getRewardMultiplier()))
                .addReward("item:" + blockType + ":" + (int)Math.max(1, count / 10));
    }
    
    /**
     * Crea un desafío de exploración de cuevas.
     * 
     * @param difficulty Dificultad del desafío
     * @param depth Profundidad mínima
     * @return Builder configurado
     */
    public static ChallengeBuilder createCaveExplorationChallenge(ChallengeDifficulty difficulty, 
                                                                 int depth) {
        return new ChallengeBuilder()
                .withId("cave_exploration_" + difficulty.name().toLowerCase())
                .withTitle("Explorador de Cuevas")
                .withDescription("Explora cuevas hasta la profundidad Y=" + depth)
                .withDifficulty(difficulty)
                .withType(ChallengeType.EXPLORATION)
                .withRequiredProgress(1)
                .addTechnicalRequirement("minDepth", depth)
                .addTechnicalRequirement("biomeType", "cave")
                .addReward("money:" + (int)(Math.abs(depth) * 5 * difficulty.getRewardMultiplier()))
                .addReward("item:torch:32");
    }
    
    // === PLANTILLAS DE CONSTRUCCIÓN ===
    
    /**
     * Crea un desafío de construcción con bloques específicos.
     * 
     * @param difficulty Dificultad del desafío
     * @param blockType Tipo de bloque a usar
     * @param count Cantidad de bloques a colocar
     * @return Builder configurado
     */
    public static ChallengeBuilder createBuildingChallenge(ChallengeDifficulty difficulty, 
                                                          String blockType, int count) {
        return new ChallengeBuilder()
                .withId("build_" + blockType.toLowerCase() + "_" + difficulty.name().toLowerCase())
                .withTitle("Constructor con " + capitalize(blockType))
                .withDescription("Coloca " + count + " bloques de " + blockType)
                .withDifficulty(difficulty)
                .withType(ChallengeType.BUILDING)
                .withRequiredProgress(count)
                .addTechnicalRequirement("blockType", blockType)
                .addTechnicalRequirement("mustSurvive", true)
                .addReward("money:" + (int)(count * 1 * difficulty.getRewardMultiplier()))
                .addReward("item:" + blockType + ":" + (int)Math.max(1, count / 4));
    }
    
    /**
     * Crea un desafío de construcción de estructura específica.
     * 
     * @param difficulty Dificultad del desafío
     * @param structureType Tipo de estructura
     * @return Builder configurado
     */
    public static ChallengeBuilder createStructureBuildingChallenge(ChallengeDifficulty difficulty, 
                                                                   String structureType) {
        return new ChallengeBuilder()
                .withId("structure_" + structureType.toLowerCase() + "_" + difficulty.name().toLowerCase())
                .withTitle("Arquitecto: " + capitalize(structureType))
                .withDescription("Construye una " + structureType + " completa")
                .withDifficulty(difficulty)
                .withType(ChallengeType.BUILDING)
                .withRequiredProgress(1)
                .addTechnicalRequirement("structureType", structureType)
                .addTechnicalRequirement("minSize", getMinStructureSize(difficulty))
                .addReward("money:" + (int)(500 * difficulty.getRewardMultiplier()))
                .addReward("title:Arquitecto");
    }
    
    // === PLANTILLAS DE AGRICULTURA ===
    
    /**
     * Crea un desafío de cultivo de plantas.
     * 
     * @param difficulty Dificultad del desafío
     * @param cropType Tipo de cultivo
     * @param count Cantidad a cosechar
     * @return Builder configurado
     */
    public static ChallengeBuilder createFarmingChallenge(ChallengeDifficulty difficulty, 
                                                         String cropType, int count) {
        return new ChallengeBuilder()
                .withId("farm_" + cropType.toLowerCase() + "_" + difficulty.name().toLowerCase())
                .withTitle("Granjero de " + capitalize(cropType))
                .withDescription("Cosecha " + count + " " + cropType)
                .withDifficulty(difficulty)
                .withType(ChallengeType.FARMING)
                .withRequiredProgress(count)
                .addTechnicalRequirement("cropType", cropType)
                .addTechnicalRequirement("mustGrow", true)
                .addReward("money:" + (int)(count * 3 * difficulty.getRewardMultiplier()))
                .addReward("item:" + cropType + ":" + (int)Math.max(1, count / 2));
    }
    
    /**
     * Crea un desafío de cría de animales.
     * 
     * @param difficulty Dificultad del desafío
     * @param animalType Tipo de animal
     * @param count Cantidad a criar
     * @return Builder configurado
     */
    public static ChallengeBuilder createAnimalBreedingChallenge(ChallengeDifficulty difficulty, 
                                                               String animalType, int count) {
        return new ChallengeBuilder()
                .withId("breed_" + animalType.toLowerCase() + "_" + difficulty.name().toLowerCase())
                .withTitle("Criador de " + capitalize(animalType))
                .withDescription("Cría " + count + " " + animalType)
                .withDifficulty(difficulty)
                .withType(ChallengeType.FARMING)
                .withRequiredProgress(count)
                .addTechnicalRequirement("animalType", animalType)
                .addTechnicalRequirement("breedingRequired", true)
                .addReward("money:" + (int)(count * 25 * difficulty.getRewardMultiplier()))
                .addReward("item:" + getAnimalProduct(animalType) + ":" + count);
    }
    
    // === PLANTILLAS DE COMERCIO ===
    
    /**
     * Crea un desafío de comercio con aldeanos.
     * 
     * @param difficulty Dificultad del desafío
     * @param trades Número de intercambios
     * @return Builder configurado
     */
    public static ChallengeBuilder createTradingChallenge(ChallengeDifficulty difficulty, int trades) {
        return new ChallengeBuilder()
                .withId("trading_" + difficulty.name().toLowerCase())
                .withTitle("Comerciante Experto")
                .withDescription("Realiza " + trades + " intercambios con aldeanos")
                .withDifficulty(difficulty)
                .withType(ChallengeType.TRADING)
                .withRequiredProgress(trades)
                .addTechnicalRequirement("villagerOnly", true)
                .addTechnicalRequirement("uniqueVillagers", true)
                .addReward("money:" + (int)(trades * 20 * difficulty.getRewardMultiplier()))
                .addReward("item:emerald:" + trades);
    }
    
    // === PLANTILLAS DE COLECCIÓN ===
    
    /**
     * Crea un desafío de colección de objetos diversos.
     * 
     * @param difficulty Dificultad del desafío
     * @param items Lista de objetos a coleccionar
     * @return Builder configurado
     */
    public static ChallengeBuilder createCollectionChallenge(ChallengeDifficulty difficulty, 
                                                           List<String> items) {
        return new ChallengeBuilder()
                .withId("collection_" + difficulty.name().toLowerCase())
                .withTitle("Coleccionista")
                .withDescription("Reúne todos los objetos de la lista: " + String.join(", ", items))
                .withDifficulty(difficulty)
                .withType(ChallengeType.COLLECTION)
                .withRequiredProgress(items.size())
                .addTechnicalRequirement("itemList", items)
                .addTechnicalRequirement("mustHaveAll", true)
                .addReward("money:" + (int)(items.size() * 50 * difficulty.getRewardMultiplier()))
                .addReward("title:Coleccionista");
    }
    
    // === PLANTILLAS TEMPORALES ===
    
    /**
     * Crea un desafío con límite de tiempo.
     * 
     * @param baseChallenge Desafío base a temporizar
     * @param timeLimit Tiempo límite en minutos
     * @return Builder configurado con tiempo
     */
    public static ChallengeBuilder createTimedChallenge(ChallengeBuilder baseChallenge, int timeLimit) {
        return baseChallenge
                .withType(ChallengeType.TIMED)
                .addTechnicalRequirement("timeLimit", timeLimit * 60000L) // Convertir a ms
                .addTechnicalRequirement("failOnTimeout", true)
                .addReward("money:" + (timeLimit * 10)); // Bonus por velocidad
    }
    
    // === PLANTILLAS ESPECIALES ===
    
    /**
     * Crea un desafío especial del evento.
     * 
     * @param eventName Nombre del evento
     * @param difficulty Dificultad
     * @param specialMechanic Mecánica especial
     * @return Builder configurado
     */
    public static ChallengeBuilder createSpecialEventChallenge(String eventName, 
                                                              ChallengeDifficulty difficulty, 
                                                              String specialMechanic) {
        // Limpiar el nombre del evento para usar en ID y título de recompensa
        String cleanEventName = eventName.replaceAll("[^a-zA-Z0-9_]", "_");
        // Limpiar también la mecánica especial para evitar caracteres problemáticos
        String cleanMechanic = specialMechanic.replaceAll("[^a-zA-Z0-9_\\s\\-]", "_");
        
        // Calcular progreso requerido basado en dificultad
        int requiredProgress = switch (difficulty) {
            case LOW -> 1;
            case MEDIUM -> 3;
            case HIGH -> 5;
        };
        
        ChallengeBuilder builder = new ChallengeBuilder()
                .withId("special_" + cleanEventName.toLowerCase() + "_" + difficulty.name().toLowerCase())
                .withTitle("Desafío Especial: " + eventName)
                .withDescription("Completa la mecánica especial: " + cleanMechanic)
                .withDifficulty(difficulty)
                .withType(ChallengeType.SPECIAL)
                .withRequiredProgress(requiredProgress)
                .addTechnicalRequirement("eventName", cleanEventName)
                .addTechnicalRequirement("specialMechanic", cleanMechanic)
                .addReward("money:" + (int)(1000 * difficulty.getRewardMultiplier()))
                .addReward("title:" + cleanEventName + "_Champion");
        
        // Agregar recompensas adicionales para dificultades altas
        if (difficulty == ChallengeDifficulty.HIGH) {
            builder.addReward("xp:" + (int)(500 * difficulty.getRewardMultiplier()));
        }
        
        return builder;
    }
    
    // === PLANTILLAS COMBINADAS ===
    
    /**
     * Crea un desafío que combina múltiples tipos.
     * 
     * @param difficulty Dificultad
     * @param primaryType Tipo principal
     * @param secondaryType Tipo secundario
     * @return Builder configurado
     */
    public static ChallengeBuilder createHybridChallenge(ChallengeDifficulty difficulty, 
                                                        ChallengeType primaryType, 
                                                        ChallengeType secondaryType) {
        return new ChallengeBuilder()
                .withId("hybrid_" + primaryType.name().toLowerCase() + "_" + 
                       secondaryType.name().toLowerCase() + "_" + difficulty.name().toLowerCase())
                .withTitle("Desafío Híbrido: " + primaryType.getDisplayName() + " & " + 
                          secondaryType.getDisplayName())
                .withDescription("Combina actividades de " + primaryType.getDisplayName() + 
                               " y " + secondaryType.getDisplayName())
                .withDifficulty(difficulty)
                .withType(primaryType)
                .addTechnicalRequirement("hybridType", secondaryType.name())
                .addTechnicalRequirement("requiresBoth", true)
                .addReward("money:" + (int)(750 * difficulty.getRewardMultiplier()))
                .addReward("title:Versátil");
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * Capitaliza la primera letra de una cadena.
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * Obtiene la herramienta requerida para un tipo de bloque.
     */
    private static String getRequiredTool(String blockType) {
        switch (blockType.toLowerCase()) {
            case "stone":
            case "cobblestone":
            case "iron_ore":
            case "gold_ore":
                return "pickaxe";
            case "wood":
            case "oak_log":
            case "birch_log":
                return "axe";
            case "dirt":
            case "grass_block":
                return "shovel";
            default:
                return "any";
        }
    }
    
    /**
     * Obtiene el tamaño mínimo para una estructura según la dificultad.
     */
    private static String getMinStructureSize(ChallengeDifficulty difficulty) {
        switch (difficulty) {
            case LOW: return "5x5x3";
            case MEDIUM: return "10x10x5";
            case HIGH: return "15x15x8";
            default: return "5x5x3";
        }
    }
    
    /**
     * Obtiene el producto asociado a un tipo de animal.
     */
    private static String getAnimalProduct(String animalType) {
        switch (animalType.toLowerCase()) {
            case "cow": return "leather";
            case "pig": return "porkchop";
            case "chicken": return "egg";
            case "sheep": return "wool";
            case "horse": return "saddle";
            default: return "bone";
        }
    }
    
    // === MÉTODOS DE CREACIÓN RÁPIDA ===
    
    /**
     * Crea un desafío de combate genérico.
     * 
     * @param id ID del desafío
     * @param title Título del desafío
     * @param difficulty Dificultad
     * @param killCount Número de eliminaciones requeridas
     * @return Desafío de combate configurado
     */
    public static StandardizedChallenge createCombatChallenge(String id, String title, 
                                                             ChallengeDifficulty difficulty, 
                                                             int killCount) {
        ChallengeBuilder builder = new ChallengeBuilder()
                .withId(id)
                .withTitle(title)
                .withDescription("Elimina " + killCount + " enemigos para completar este desafío")
                .withDifficulty(difficulty)
                .withType(ChallengeType.COMBAT)
                .withRequiredProgress(killCount)
                .addTechnicalRequirement("mobType", "any")
                .addTechnicalRequirement("killMethod", "any")
                .addReward("money:" + (int)(killCount * 15 * difficulty.getRewardMultiplier()))
                .addReward("xp:" + (int)(killCount * 8 * difficulty.getRewardMultiplier()));
        
        // Agregar tercera recompensa para dificultad HIGH
        if (difficulty == ChallengeDifficulty.HIGH) {
            builder.addReward("item:iron_sword:1");
        }
        
        return builder.buildStandardized();
    }
    
    /**
     * Crea un conjunto balanceado de desafíos semanales.
     * 
     * @param eventName Nombre del evento
     * @return Lista de desafíos balanceados para la semana
     */
    public static List<StandardizedChallenge> createBalancedWeeklySet(String eventName) {
        List<StandardizedChallenge> challenges = new ArrayList<>();
        
        // Desafíos fáciles (3)
        challenges.add(createMobKillChallenge(ChallengeDifficulty.LOW, "zombie", 10).buildStandardized());
        challenges.add(createMiningChallenge(ChallengeDifficulty.LOW, "stone", 50).buildStandardized());
        challenges.add(createFarmingChallenge(ChallengeDifficulty.LOW, "carrot", 20).buildStandardized());
        
        // Desafíos medios (2)
        challenges.add(createBuildingChallenge(ChallengeDifficulty.MEDIUM, "cobblestone", 100).buildStandardized());
        challenges.add(createTradingChallenge(ChallengeDifficulty.MEDIUM, 5).buildStandardized());
        
        // Desafío difícil (1)
        challenges.add(createSpecialEventChallenge(eventName, ChallengeDifficulty.HIGH, 
                                                 "Complete event boss").buildStandardized());
        
        return challenges;
    }
    
    /**
     * Crea un conjunto de desafíos balanceados para un evento.
     * 
     * @param eventName Nombre del evento
     * @return Lista de desafíos balanceados
     */
    public static List<StandardizedChallenge> createBalancedChallengeSet(String eventName) {

        List<StandardizedChallenge> challenges = new ArrayList<>();
        
        // Desafíos fáciles (3)
        challenges.add(createMobKillChallenge(ChallengeDifficulty.LOW, "zombie", 10).buildStandardized());
        challenges.add(createMiningChallenge(ChallengeDifficulty.LOW, "stone", 50).buildStandardized());
        challenges.add(createFarmingChallenge(ChallengeDifficulty.LOW, "wheat", 20).buildStandardized());
        
        // Desafíos medios (2)
        challenges.add(createBuildingChallenge(ChallengeDifficulty.MEDIUM, "cobblestone", 100).buildStandardized());
        challenges.add(createTradingChallenge(ChallengeDifficulty.MEDIUM, 5).buildStandardized());
        
        // Desafío difícil (1)
        challenges.add(createSpecialEventChallenge(eventName, ChallengeDifficulty.HIGH, 
                                                 "Complete event boss").buildStandardized());
        
        return challenges;
    }
    
    /**
     * Crea desafíos temáticos para un tipo específico.
     * 
     * @param type Tipo de desafío
     * @return Lista de desafíos del tipo especificado
     */
    public static List<StandardizedChallenge> createThematicChallenges(ChallengeType type) {
        List<StandardizedChallenge> challenges = new ArrayList<>();
        
        switch (type) {
            case COMBAT:
                challenges.add(createMobKillChallenge(ChallengeDifficulty.LOW, "skeleton", 15).buildStandardized());
                challenges.add(createMobKillChallenge(ChallengeDifficulty.MEDIUM, "creeper", 10).buildStandardized());
                challenges.add(createPvPChallenge(ChallengeDifficulty.HIGH, 3).buildStandardized());
                break;
                
            case MINING:
                challenges.add(createMiningChallenge(ChallengeDifficulty.LOW, "coal_ore", 30).buildStandardized());
                challenges.add(createMiningChallenge(ChallengeDifficulty.MEDIUM, "iron_ore", 20).buildStandardized());
                challenges.add(createCaveExplorationChallenge(ChallengeDifficulty.HIGH, -50).buildStandardized());
                break;
                
            case FARMING:
                challenges.add(createFarmingChallenge(ChallengeDifficulty.LOW, "carrot", 25).buildStandardized());
                challenges.add(createAnimalBreedingChallenge(ChallengeDifficulty.MEDIUM, "cow", 5).buildStandardized());
                challenges.add(createFarmingChallenge(ChallengeDifficulty.HIGH, "nether_wart", 50).buildStandardized());
                break;
                
            default:
                // Crear desafíos genéricos para otros tipos
                challenges.add(createSpecialEventChallenge("Generic", ChallengeDifficulty.MEDIUM, 
                                                         type.getDisplayName()).buildStandardized());
        }
        
        return challenges;
    }

    /**
     * Crea un conjunto temático de desafíos del mismo tipo.
     * 
     * @param theme Tema del conjunto
     * @param type Tipo de desafío
     * @param count Cantidad de desafíos a crear
     * @return Lista de desafíos temáticos
     */
    public static List<StandardizedChallenge> createThematicSet(String theme, ChallengeType type, int count) {
        List<StandardizedChallenge> challenges = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            StandardizedChallenge challenge = StandardizedChallenge.builder()
                .withId(theme.toLowerCase() + "_" + type.name().toLowerCase() + "_" + i)
                .withTitle(theme + " " + type.getDisplayName() + " #" + i)
                .withDescription("Desafío temático de " + theme.toLowerCase() + " - " + type.getDisplayName())
                .withType(type)
                .withDifficulty(i <= count/3 ? ChallengeDifficulty.LOW : 
                              i <= 2*count/3 ? ChallengeDifficulty.MEDIUM : ChallengeDifficulty.HIGH)
                .withRequiredProgress(i * 5)
                .withRewards(List.of("money:" + (i * 100), "xp:" + (i * 50)))
                .buildStandardized();
            
            challenges.add(challenge);
        }
        
        return challenges;
    }
}