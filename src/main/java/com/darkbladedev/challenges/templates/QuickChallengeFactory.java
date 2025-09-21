package com.darkbladedev.challenges.templates;

import com.darkbladedev.challenges.ChallengeBuilder;
import com.darkbladedev.challenges.StandardizedChallenge;
import com.darkbladedev.challenges.types.ChallengeDifficulty;
import com.darkbladedev.challenges.types.ChallengeType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Factory para la creación ultra-rápida de desafíos con configuraciones
 * automáticas y métodos simplificados.
 * 
 * Esta clase está diseñada para desarrolladores que necesitan crear
 * desafíos rápidamente sin preocuparse por los detalles de configuración.
 * Proporciona métodos de una línea para los casos más comunes.
 * 
 * Características:
 * - Métodos de una línea para casos comunes
 * - Configuración automática de recompensas
 * - Balanceo automático según dificultad
 * - Generación aleatoria de desafíos
 * - Validación automática
 * - Compatibilidad total con el sistema existente
 * 
 * @author DarkBladeDev
 * @version 1.0
 * @since 2.0
 */
public class QuickChallengeFactory {
    
    // === CREACIÓN ULTRA-RÁPIDA ===
    
    /**
     * Crea un desafío de eliminación de mobs con configuración automática.
     * 
     * @param mobType Tipo de mob
     * @param difficulty Dificultad
     * @return Desafío listo para usar
     */
    public static StandardizedChallenge killMobs(String mobType, ChallengeDifficulty difficulty) {
        int count = getAutoCount(difficulty, 10, 25, 50);
        return ChallengeTemplates.createMobKillChallenge(difficulty, mobType, count).buildStandardized();
    }
    
    /**
     * Crea un desafío de minería con configuración automática.
     * 
     * @param blockType Tipo de bloque
     * @param difficulty Dificultad
     * @return Desafío listo para usar
     */
    public static StandardizedChallenge mineBlocks(String blockType, ChallengeDifficulty difficulty) {
        int count = getAutoCount(difficulty, 25, 75, 150);
        return ChallengeTemplates.createMiningChallenge(difficulty, blockType, count).buildStandardized();
    }
    
    /**
     * Crea un desafío de construcción con configuración automática.
     * 
     * @param blockType Tipo de bloque
     * @param difficulty Dificultad
     * @return Desafío listo para usar
     */
    public static StandardizedChallenge buildWith(String blockType, ChallengeDifficulty difficulty) {
        int count = getAutoCount(difficulty, 50, 150, 300);
        return ChallengeTemplates.createBuildingChallenge(difficulty, blockType, count).buildStandardized();
    }
    
    /**
     * Crea un desafío de agricultura con configuración automática.
     * 
     * @param cropType Tipo de cultivo
     * @param difficulty Dificultad
     * @return Desafío listo para usar
     */
    public static StandardizedChallenge farmCrops(String cropType, ChallengeDifficulty difficulty) {
        int count = getAutoCount(difficulty, 15, 40, 80);
        return ChallengeTemplates.createFarmingChallenge(difficulty, cropType, count).buildStandardized();
    }
    
    /**
     * Crea un desafío de comercio con configuración automática.
     * 
     * @param difficulty Dificultad
     * @return Desafío listo para usar
     */
    public static StandardizedChallenge trade(ChallengeDifficulty difficulty) {
        int count = getAutoCount(difficulty, 3, 8, 15);
        return ChallengeTemplates.createTradingChallenge(difficulty, count).buildStandardized();
    }
    
    /**
     * Crea un desafío de PvP con configuración automática.
     * 
     * @param difficulty Dificultad
     * @return Desafío listo para usar
     */
    public static StandardizedChallenge pvpKills(ChallengeDifficulty difficulty) {
        int count = getAutoCount(difficulty, 1, 3, 5);
        return ChallengeTemplates.createPvPChallenge(difficulty, count).buildStandardized();
    }

    /**
     * Crea un desafío de combate genérico.
     * 
     * @return Desafío de combate configurado
     */
    public static StandardizedChallenge combat() {
        return new ChallengeBuilder()
                .withId("quick_combat_" + System.currentTimeMillis())
                .withTitle("Combate Rápido")
                .withDescription("Elimina 5 enemigos para completar este desafío")
                .withDifficulty(ChallengeDifficulty.LOW)
                .withType(ChallengeType.COMBAT)
                .withRequiredProgress(5)
                .addTechnicalRequirement("mobType", "any")
                .addTechnicalRequirement("killMethod", "any")
                .addReward("money:75")
                .addReward("xp:40")
                .buildStandardized();
    }

    // === CREACIÓN CON PARÁMETROS MÍNIMOS ===
    
    /**
     * Crea un desafío básico con solo título y tipo.
     * El resto se configura automáticamente.
     * 
     * @param title Título del desafío
     * @param type Tipo de desafío
     * @return Desafío configurado automáticamente
     */
    public static StandardizedChallenge quick(String title, ChallengeType type) {
        return quick(title, type, ChallengeDifficulty.MEDIUM);
    }
    
    /**
     * Crea un desafío básico con título, tipo y dificultad.
     * El resto se configura automáticamente.
     * 
     * @param title Título del desafío
     * @param type Tipo de desafío
     * @param difficulty Dificultad
     * @return Desafío configurado automáticamente
     */
    public static StandardizedChallenge quick(String title, ChallengeType type, ChallengeDifficulty difficulty) {
        String id = generateAutoId(title, type, difficulty);
        String description = generateAutoDescription(title, type, difficulty);
        int progress = getAutoProgress(type, difficulty);
        
        return new ChallengeBuilder()
                .withId(id)
                .withTitle(title)
                .withDescription(description)
                .withType(type)
                .withDifficulty(difficulty)
                .withRequiredProgress(progress)
                .addReward("money:" + (progress * 10 * difficulty.getRewardMultiplier()))
                .addReward("xp:" + (progress * 5 * difficulty.getRewardMultiplier()))
                .addReward("xp:" + (progress * 3 * difficulty.getRewardMultiplier()))
                .buildStandardized();
    }
    
    // === GENERACIÓN ALEATORIA ===
    
    /**
     * Genera un desafío completamente aleatorio.
     * 
     * @return Desafío aleatorio balanceado
     */
    public static StandardizedChallenge random() {
        ChallengeType[] types = ChallengeType.values();
        ChallengeDifficulty[] difficulties = ChallengeDifficulty.values();
        
        ChallengeType randomType = types[ThreadLocalRandom.current().nextInt(types.length)];
        ChallengeDifficulty randomDifficulty = difficulties[ThreadLocalRandom.current().nextInt(difficulties.length)];
        
        return randomOfType(randomType, randomDifficulty);
    }
    
    /**
     * Genera un desafío aleatorio de un tipo específico.
     * 
     * @param type Tipo de desafío
     * @return Desafío aleatorio del tipo especificado
     */
    public static StandardizedChallenge randomOfType(ChallengeType type) {
        ChallengeDifficulty[] difficulties = ChallengeDifficulty.values();
        ChallengeDifficulty randomDifficulty = difficulties[ThreadLocalRandom.current().nextInt(difficulties.length)];
        
        return randomOfType(type, randomDifficulty);
    }
    
    /**
     * Genera un desafío aleatorio de un tipo y dificultad específicos.
     * 
     * @param type Tipo de desafío
     * @param difficulty Dificultad
     * @return Desafío aleatorio configurado
     */
    public static StandardizedChallenge randomOfType(ChallengeType type, ChallengeDifficulty difficulty) {
        switch (type) {
            case COMBAT:
                return randomCombatChallenge(difficulty);
            case MINING:
                return randomMiningChallenge(difficulty);
            case BUILDING:
                return randomBuildingChallenge(difficulty);
            case FARMING:
                return randomFarmingChallenge(difficulty);
            case TRADING:
                return trade(difficulty);
            case EXPLORATION:
                return randomExplorationChallenge(difficulty);
            case COLLECTION:
                return randomCollectionChallenge(difficulty);
            default:
                return quick("Desafío " + type.getDisplayName(), type, difficulty);
        }
    }
    
    // === SETS PREDEFINIDOS ===
    
    /**
     * Crea un set básico de desafíos para principiantes.
     * 
     * @return Lista de desafíos fáciles
     */
    public static List<StandardizedChallenge> beginnerSet() {
        return Arrays.asList(
                killMobs("zombie", ChallengeDifficulty.LOW),
                mineBlocks("stone", ChallengeDifficulty.LOW),
                farmCrops("wheat", ChallengeDifficulty.LOW),
                buildWith("cobblestone", ChallengeDifficulty.LOW)
        );
    }
    
    /**
     * Crea un set intermedio de desafíos.
     * 
     * @return Lista de desafíos medios
     */
    public static List<StandardizedChallenge> intermediateSet() {
        return Arrays.asList(
                killMobs("skeleton", ChallengeDifficulty.MEDIUM),
                mineBlocks("iron_ore", ChallengeDifficulty.MEDIUM),
                trade(ChallengeDifficulty.MEDIUM),
                buildWith("brick", ChallengeDifficulty.MEDIUM),
                farmCrops("carrot", ChallengeDifficulty.MEDIUM)
        );
    }
    
    /**
     * Crea un set avanzado de desafíos.
     * 
     * @return Lista de desafíos difíciles
     */
    public static List<StandardizedChallenge> expertSet() {
        return Arrays.asList(
                pvpKills(ChallengeDifficulty.HIGH),
                mineBlocks("diamond_ore", ChallengeDifficulty.HIGH),
                ChallengeTemplates.createCaveExplorationChallenge(ChallengeDifficulty.HIGH, -60).buildStandardized(),
                ChallengeTemplates.createStructureBuildingChallenge(ChallengeDifficulty.HIGH, "castle").buildStandardized()
        );
    }
    
    /**
     * Crea un set mixto balanceado.
     * 
     * @return Lista de desafíos balanceados
     */
    public static List<StandardizedChallenge> balancedSet() {
        List<StandardizedChallenge> challenges = new ArrayList<>();
        challenges.addAll(beginnerSet().subList(0, 2));
        challenges.addAll(intermediateSet().subList(0, 2));
        challenges.addAll(expertSet().subList(0, 1));
        return challenges;
    }
    
    // === MÉTODOS DE UTILIDAD PRIVADOS ===
    
    /**
     * Obtiene un conteo automático basado en la dificultad.
     */
    private static int getAutoCount(ChallengeDifficulty difficulty, int low, int medium, int high) {
        switch (difficulty) {
            case LOW: return low;
            case MEDIUM: return medium;
            case HIGH: return high;
            default: return medium;
        }
    }
    
    /**
     * Genera un ID automático para el desafío.
     */
    private static String generateAutoId(String title, ChallengeType type, ChallengeDifficulty difficulty) {
        String cleanTitle = title.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        
        return String.format("auto_%s_%s_%s_%d", 
                cleanTitle, 
                type.name().toLowerCase(), 
                difficulty.name().toLowerCase(),
                System.currentTimeMillis() % 10000);
    }
    
    /**
     * Genera una descripción automática.
     */
    private static String generateAutoDescription(String title, ChallengeType type, ChallengeDifficulty difficulty) {
        String difficultyText = difficulty.getDisplayName().toLowerCase();
        String typeText = type.getDisplayName().toLowerCase();
        
        return String.format("Completa este desafío de %s con dificultad %s. %s", 
                typeText, difficultyText, type.getDescription());
    }
    
    /**
     * Obtiene el progreso automático según el tipo y dificultad.
     */
    private static int getAutoProgress(ChallengeType type, ChallengeDifficulty difficulty) {
        int base;
        switch (type) {
            case COMBAT: base = 15; break;
            case MINING: base = 50; break;
            case BUILDING: base = 100; break;
            case FARMING: base = 30; break;
            case TRADING: base = 5; break;
            case EXPLORATION: base = 3; break;
            case COLLECTION: base = 8; break;
            default: base = 10; break;
        }
        
        return (int) (base * difficulty.getRewardMultiplier());
    }
    
    // === GENERADORES ALEATORIOS ESPECÍFICOS ===
    
    private static StandardizedChallenge randomCombatChallenge(ChallengeDifficulty difficulty) {
        String[] mobs = {"zombie", "skeleton", "creeper", "spider", "enderman", "witch"};
        String randomMob = mobs[ThreadLocalRandom.current().nextInt(mobs.length)];
        return killMobs(randomMob, difficulty);
    }
    
    private static StandardizedChallenge randomMiningChallenge(ChallengeDifficulty difficulty) {
        String[] blocks = {"stone", "coal_ore", "iron_ore", "gold_ore", "diamond_ore", "cobblestone"};
        String randomBlock = blocks[ThreadLocalRandom.current().nextInt(blocks.length)];
        return mineBlocks(randomBlock, difficulty);
    }
    
    private static StandardizedChallenge randomBuildingChallenge(ChallengeDifficulty difficulty) {
        String[] blocks = {"cobblestone", "stone_bricks", "oak_planks", "brick", "quartz_block"};
        String randomBlock = blocks[ThreadLocalRandom.current().nextInt(blocks.length)];
        return buildWith(randomBlock, difficulty);
    }
    
    private static StandardizedChallenge randomFarmingChallenge(ChallengeDifficulty difficulty) {
        String[] crops = {"wheat", "carrot", "potato", "beetroot", "pumpkin", "melon"};
        String randomCrop = crops[ThreadLocalRandom.current().nextInt(crops.length)];
        return farmCrops(randomCrop, difficulty);
    }
    
    private static StandardizedChallenge randomExplorationChallenge(ChallengeDifficulty difficulty) {
        int depth = getAutoCount(difficulty, -20, -40, -60);
        return ChallengeTemplates.createCaveExplorationChallenge(difficulty, depth).buildStandardized();
    }
    
    private static StandardizedChallenge randomCollectionChallenge(ChallengeDifficulty difficulty) {
        List<String> items = Arrays.asList("apple", "bread", "iron_ingot", "gold_ingot", "diamond");
        int itemCount = getAutoCount(difficulty, 3, 4, 5);
        Collections.shuffle(items);
        
        return ChallengeTemplates.createCollectionChallenge(difficulty, 
                items.subList(0, Math.min(itemCount, items.size()))).buildStandardized();
    }
    
    // === MÉTODOS DE CONVENIENCIA ===
    
    /**
     * Crea múltiples desafíos del mismo tipo con diferentes dificultades.
     * 
     * @param type Tipo de desafío
     * @param count Número de desafíos a crear
     * @return Lista de desafíos variados
     */
    public static List<StandardizedChallenge> createVariedSet(ChallengeType type, int count) {
        List<StandardizedChallenge> challenges = new ArrayList<>();
        ChallengeDifficulty[] difficulties = ChallengeDifficulty.values();
        
        for (int i = 0; i < count; i++) {
            ChallengeDifficulty difficulty = difficulties[i % difficulties.length];
            challenges.add(randomOfType(type, difficulty));
        }
        
        return challenges;
    }
    
    /**
     * Crea un desafío personalizado con validación automática.
     * 
     * @param title Título
     * @param description Descripción
     * @param type Tipo
     * @param difficulty Dificultad
     * @param progress Progreso requerido
     * @return Desafío validado
     */
    public static StandardizedChallenge custom(String title, String description, 
                                             ChallengeType type, ChallengeDifficulty difficulty, 
                                             int progress) {
        String id = generateAutoId(title, type, difficulty);
        
        ChallengeBuilder builder = new ChallengeBuilder()
                .withId(id)
                .withTitle(title)
                .withDescription(description)
                .withType(type)
                .withDifficulty(difficulty)
                .withRequiredProgress(progress);
        
        // Agregar recompensas automáticas
        builder.addReward("money:" + (progress * 8 * difficulty.getRewardMultiplier()));
        builder.addReward("xp:" + (progress * 3 * difficulty.getRewardMultiplier()));
        
        return builder.buildStandardized();
    }

    /**
     * Crea un desafío de minería rápido.
     * 
     * @param blockType Tipo de bloque a minar
     * @param count Cantidad a minar
     * @return Desafío de minería configurado
     */
    public static StandardizedChallenge mining(String blockType, int count) {
        return StandardizedChallenge.builder()
                .withId("quick_mining_" + blockType.toLowerCase())
                .withTitle("Minería Rápida: " + capitalize(blockType))
                .withDescription("Mina " + count + " bloques de " + blockType)
                .withType(ChallengeType.MINING)
                .withDifficulty(ChallengeDifficulty.LOW)
                .withRequiredProgress(count)
                .withRewards(List.of("money:" + (count * 5), "xp:" + (count * 2)))
                .buildStandardized();
    }

    /**
     * Capitaliza la primera letra de una cadena.
     * 
     * @param str Cadena a capitalizar
     * @return Cadena capitalizada
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Crea múltiples desafíos basados en un desafío base.
     * 
     * @param baseChallenge Desafío base
     * @param count Cantidad de desafíos a crear
     * @return Lista de desafíos
     */
    public static List<StandardizedChallenge> createMultiple(StandardizedChallenge baseChallenge, int count) {
        List<StandardizedChallenge> challenges = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            StandardizedChallenge challenge = StandardizedChallenge.builder()
                    .withId(baseChallenge.getId() + "_" + i)
                    .withTitle(baseChallenge.getTitle() + " #" + i)
                    .withDescription(baseChallenge.getDescription())
                    .withType(baseChallenge.getType())
                    .withDifficulty(baseChallenge.getDifficulty())
                    .withRequiredProgress(baseChallenge.getRequiredProgress())
                    .withRewards(new ArrayList<>(baseChallenge.getRewards()))
                    .buildStandardized();
            
            challenges.add(challenge);
        }
        
        return challenges;
    }
}