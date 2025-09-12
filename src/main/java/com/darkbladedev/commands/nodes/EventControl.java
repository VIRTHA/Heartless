package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.functions.events.Challenges;
import com.darkbladedev.commands.functions.events.DebugRedMoon;
import com.darkbladedev.commands.functions.events.Pause;
import com.darkbladedev.commands.functions.events.Reset;
import com.darkbladedev.commands.functions.events.Resume;
import com.darkbladedev.commands.functions.events.Schedule;
import com.darkbladedev.commands.functions.events.Start;
import com.darkbladedev.commands.functions.events.Status;
import com.darkbladedev.commands.functions.events.Stop;

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
        subcommands.put("debug-redmoon", new DebugRedMoon()); // Comando temporal de debug
    }

	@Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }
}
