package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.functions.common.Reload;

public class Common implements CommandFunction {

    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();

    public Common() {
        subcommands.put("reload", new Reload());
    }

    @Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }

}
