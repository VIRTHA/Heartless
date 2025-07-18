package com.darkbladedev.commands.functions.events;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import com.darkbladedev.HeartlessMain;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.TabCompletable;
import com.darkbladedev.managers.WeeklyEventManager;
import com.darkbladedev.utils.EventType;
import com.darkbladedev.utils.MM;
import com.darkbladedev.utils.TimeConverter;

public class Start implements SubcommandExecutor, TabCompletable {

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return EventType.getEventNames();
            
            case 2:
                return List.of(TimeConverter.getTimeCompletions());

            default:
                return Collections.emptyList();
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length <= 2) {
            sender.sendMessage(MM.toComponent("<red>Usage: <gray>/heartless start <event-type> <duration>"));
            return;
        }
        
        String eventTypeName = args[0];
        long duration = TimeConverter.parseTimeToTicks(args[1]);
        
        EventType eventType = EventType.getByName(eventTypeName);
        
        
        if (eventType == null) {
            sender.sendMessage(MM.toComponent("<red>Tipo de evento desconocido: <yellow>" + eventTypeName));
            return;
        }
        
        // Get the WeeklyEventManager instance
        WeeklyEventManager eventManager = HeartlessMain.getWeeklyEventManager_();

        switch (eventType) {
            case ACID_WEEK:
                if (eventManager.getCurrentEvent().isActive() || 
                    eventManager.getCurrentEvent().isPaused()) {
                        sender.sendMessage(MM.toComponent("<red>Debes detener el evento actual para inciar otro."));
                        return;
                }
                eventManager.startEventFromCommand(eventType, duration);
                break;
            
            case BLOOD_AND_IRON_WEEK:
                if (eventManager.getCurrentEvent().isActive() || 
                    eventManager.getCurrentEvent().isPaused()) {
                        sender.sendMessage(MM.toComponent("<red>Debes detener el evento actual para inciar otro."));
                        return;
                }
                eventManager.startEventFromCommand(eventType, duration);
                break;
                
            case UNDEAD_WEEK:
                if (eventManager.getCurrentEvent().isActive() || 
                    eventManager.getCurrentEvent().isPaused()) {
                        sender.sendMessage(MM.toComponent("<red>Debes detener el evento actual para inciar otro."));
                        return;
                }
                eventManager.startEventFromCommand(eventType, duration);
                break;
                
            case TOXIC_FOG_WEEK:
                if (eventManager.getCurrentEvent().isActive() || 
                    eventManager.getCurrentEvent().isPaused()) {
                        sender.sendMessage(MM.toComponent("<red>Debes detener el evento actual para inciar otro."));
                        return;
                }
                eventManager.startEventFromCommand(eventType, duration);
                break;
                
            case EXPLOSIVE_WEEK:
                if (eventManager.getCurrentEvent().isActive() || 
                    eventManager.getCurrentEvent().isPaused()) {
                        sender.sendMessage(MM.toComponent("<red>Debes detener el evento actual para inciar otro."));
                        return;
                }
                eventManager.startEventFromCommand(eventType, duration);
                break;
        
            default:
                break;
        }
    }

}
