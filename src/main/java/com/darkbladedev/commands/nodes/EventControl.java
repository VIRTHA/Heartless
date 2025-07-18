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
	}

	@Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }
}
