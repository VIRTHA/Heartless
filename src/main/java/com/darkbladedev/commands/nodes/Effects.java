package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;

public class Effects implements CommandFunction {
	
    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();

	@Override
	public Map<String, SubcommandExecutor> getSubcommands() {
		return subcommands;
	}

}
