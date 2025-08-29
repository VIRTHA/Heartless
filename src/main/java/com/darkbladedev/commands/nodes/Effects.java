package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.functions.effects.*;

public class Effects implements CommandFunction {
	
    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();
    
    public Effects() {
        subcommands.put("apply", new Apply());
        subcommands.put("remove", new Clear());
        subcommands.put("info", new Info());
        subcommands.put("list", new List());
        subcommands.put("activate", new Activate());
        subcommands.put("deactivate", new Deactivate());
        subcommands.put("reload", new Reload());
    }

	@Override
	public Map<String, SubcommandExecutor> getSubcommands() {
		return subcommands;
	}

}
