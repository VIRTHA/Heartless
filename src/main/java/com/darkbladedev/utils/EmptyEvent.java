package com.darkbladedev.utils;

import com.darkbladedev.HeartlessMain;
import com.darkbladedev.mechanics.WeeklyEvent;

public class EmptyEvent extends WeeklyEvent {

    public EmptyEvent(HeartlessMain plugin, long duration) {
        super(plugin, duration);
        this.prefix = "<b><gradient:#808080:#a0a0a0:#c0c0c0:#e0e0e0:#ffffff:#ffffff:#ffffff:#ffffff:#ffffff:#e0e0e0:#c0c0c0:#a0a0a0:#808080>Evento Vacío</gradient></b>";
    }

    @Override
    public String getId() {
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
