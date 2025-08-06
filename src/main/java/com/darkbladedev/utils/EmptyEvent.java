package com.darkbladedev.utils;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.WeeklyEvent;

public class EmptyEvent extends WeeklyEvent {

    public EmptyEvent(HeartlessMain plugin, long duration) {
        super(plugin, duration);
    }

    @Override
    public String getName() {
        return "empty";
    }

    @Override
    protected void startEventTasks() {
        return;
    }

    @Override
    protected void stopEventTasks() {
        return;
    }

    @Override
    protected void cleanupEventData() {
        return;
    }

}
