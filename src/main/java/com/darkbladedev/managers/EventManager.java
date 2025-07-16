package com.darkbladedev.managers;

import com.darkbladedev.HeartlessMain;

import com.darkbladedev.mechanics.AcidWeek;

public class EventManager {

    @SuppressWarnings("unused")
    private final HeartlessMain plugin;

    private AcidWeek acidWeek;
    
    public EventManager(HeartlessMain plugin) {
        this.plugin = plugin;
    }

    // STARTERS


    // GETTERS
    public AcidWeek getAcidWeek() {
        return  acidWeek;
    }
}
