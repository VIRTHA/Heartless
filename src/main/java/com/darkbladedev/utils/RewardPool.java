package com.darkbladedev.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.darkbladedev.utils.EventType;

@SuppressWarnings("unused")
public class RewardPool {

    private static final Map<EventType, List<String>> EVENT_REWARDS = new HashMap<>();

    static {
        // Recompensas para Semana Explosiva
        EVENT_REWARDS.put(EventType.EXPLOSIVE_WEEK, Arrays.asList(
            "Encantamiento TicTac (por matar ghast en overworld)",
            "+1 corazón permanente (por conseguir cabezas de mobs hostiles)",
            "Tag 'TNTómano' (por matar jugador con explosión)",
            "+1 corazón permanente (por matar warden con creeper eléctrico)"
        ));
        
        // Recompensas para Semana de No-Muertos
        EVENT_REWARDS.put(EventType.UNDEAD_WEEK, Arrays.asList(
            "Encantamiento First Strike (por matar 50 zombies)",
            "+1 corazón permanente (por sobrevivir horda de 20+ zombies)",
            "Tag 'Dr. Zomboss' (por matar jugador usando zombies)",
            "+1 corazón permanente (por convertir 10 aldeanos en zombies)"
        ));
        
        // Recompensas para Semana de Sangre y Hierro
        EVENT_REWARDS.put(EventType.BLOOD_AND_IRON_WEEK, Arrays.asList(
            "Encantamiento Adrenaline (por matar 3 jugadores)",
            "+1 corazón permanente (por matar jugador con poción de daño)",
            "Tag 'Pentakill' (por 5 kills seguidos sin morir)",
            "+1 corazón permanente (por sobrevivir evento con +10 kills)"
        ));
        
        // Recompensas para Semana Ácida
        EVENT_REWARDS.put(EventType.ACID_WEEK, Arrays.asList(
            "Poción de resistencia permanente (por sobrevivir 10 min bajo lluvia ácida)",
            "+1 corazón permanente (por matar jugador en agua ácida)",
            "Kit de construcción especial (por construir base resistente al ácido)",
            "Tag 'Químico' (por ayudar a 5 jugadores a sobrevivir al ácido)"
        ));

        // El evento EMPTY no debería tener recompensas
        EVENT_REWARDS.put(EventType.EMPTY, Collections.emptyList());
    }

    public static List<String> getRewardsForEvent(EventType eventType) {
        return EVENT_REWARDS.getOrDefault(eventType, Collections.emptyList());
    }

    public static boolean hasRewards(EventType eventType) {
        return EVENT_REWARDS.containsKey(eventType);
    }

}
