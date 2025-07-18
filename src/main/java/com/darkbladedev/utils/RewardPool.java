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
    }

    public static List<String> getRewardsForEvent(EventType eventType) {
        return EVENT_REWARDS.getOrDefault(eventType, Collections.emptyList());
    }

    public static boolean hasRewards(EventType eventType) {
        return EVENT_REWARDS.containsKey(eventType);
    }

}
