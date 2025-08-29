package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.functions.common.*;

public class Common implements CommandFunction {

    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();

    public Common() {
        subcommands.put("reload", new Reload());
        subcommands.put("diagnostic", new Diagnostic());
        subcommands.put("test-persistence", new TestPersistence());
    }

    @Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }

}
