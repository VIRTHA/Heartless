package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.functions.events.*;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;

public class EventControl implements CommandFunction {

    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();


	public EventControl() {
        subcommands.put("start", new Start());
        subcommands.put("stop", new Stop());
        subcommands.put("pause", new Pause());
        subcommands.put("resume", new Resume());
        subcommands.put("schedule", new Schedule());
        subcommands.put("status", new Status());
        subcommands.put("reset", new Reset());
        subcommands.put("challenges", new Challenges());
        subcommands.put("complete-challenge", new CompleteChallenge());
        subcommands.put("stats", new Stats()); // Comando para ver estadísticas personales
        subcommands.put("debug-redmoon", new DebugRedMoon()); // Comando temporal de debug
    }

	@Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }
}
