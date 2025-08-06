package com.darkbladedev.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardPool {

    private static final Map<EventType, List<String>> EVENT_REWARDS = new HashMap<>();

    static {
        EVENT_REWARDS.put(EventType.ACID_WEEK, Arrays.asList(
            ""
        ));

        EVENT_REWARDS.put(EventType.BLOOD_AND_IRON_WEEK, Arrays.asList(
            "<white>Encantamiento <gold><u>Adrenaline</u></gold><white>",
            "<white><u>Tag</u> \"Pentakill\"</white>",
            "<white><u>+2</u> corazones</white>"
        ));

        EVENT_REWARDS.put(EventType.EXPLOSIVE_WEEK, Arrays.asList(
            "<white>Encantamiento <gold><u>Carve</u></gold><white>",
            "<white><u>Tag</u> \"TNTómano\"</white>",
            "<white><u>+2</u> corazones</white>"
        ));

        EVENT_REWARDS.put(EventType.UNDEAD_WEEK, Arrays.asList(
            "<white>Encantamiento <gold><u>First Strike</u></gold><white>",
            "<white><u>Tag</u> \"Dr. Zomboss\"</white>",
            "<white><u>+2</u> corazones</white>"
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
