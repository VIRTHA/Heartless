package com.darkbladedev.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardPool {

    private static final Map<EventType, List<String>> EVENT_REWARDS = new HashMap<>();

    static {
        // Recompensas para Semana Explosiva (según Heartless.md)
        EVENT_REWARDS.put(EventType.EXPLOSIVE_WEEK, Arrays.asList(
            "Encantamiento Tic Tac",           // Matar ghast durante tormenta
            "+20 Thalos",                      // Conseguir cabezas de mobs clásicos
            "Tag \"TNTómano\"",                // Matar jugador en PvP durante tormenta
            "+1 corazón máximo"                // Matar Warden durante tormenta
        ));
        
        // Recompensas para Semana de No-Muertos (según Heartless.md)
        EVENT_REWARDS.put(EventType.UNDEAD_WEEK, Arrays.asList(
            "Tag \"Dr. Zomboss\"",             // Curar 5 aldeanos zombificados
            "Encantamiento First Strike",      // Curarse infección zombie 10 veces
            "+1 corazón máximo",               // Matar 50 no-muertos en Noche Roja
            "+1 corazón máximo"                // Derrotar Wither en Noche Roja
        ));
        
        // Recompensas para Semana de Sangre y Hierro (según Heartless.md)
        EVENT_REWARDS.put(EventType.BLOOD_AND_IRON_WEEK, Arrays.asList(
            "Encantamiento Adrenaline",        // Matar 3 jugadores
            "+20 Thalos",                      // No morir en toda la semana
            "Tag \"Pentakill\"",               // Matar 5 jugadores seguidos sin morir
            "+1 corazón máximo"                // Hacer más de 10 kills durante la semana
        ));
        
        // Recompensas para Semana Ácida (según Heartless.md)
        EVENT_REWARDS.put(EventType.ACID_WEEK, Arrays.asList(
            "Encantamiento Contagion",         // Conseguir 4 tipos de pescados en cubetas
            "+20 Thalos",                      // Sobrevivir 1.5 min bajo lluvia ácida sin pociones/armadura
            "Tag \"Asesino Químico\"",         // Matar jugador con botella de agua arrojadiza
            "+1 corazón máximo"                // Conseguir ajolote azul
        ));

        // Evento vacío sin recompensas
        EVENT_REWARDS.put(EventType.EMPTY, Collections.emptyList());
    }

    public static List<String> getRewardsForEvent(EventType eventType) {
        return EVENT_REWARDS.getOrDefault(eventType, Collections.emptyList());
    }

    public static boolean hasRewards(EventType eventType) {
        return EVENT_REWARDS.containsKey(eventType);
    }

}
