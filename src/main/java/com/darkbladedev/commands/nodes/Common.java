package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.functions.common.AutoSave;
import com.darkbladedev.commands.functions.common.Diagnostic;
import com.darkbladedev.commands.functions.common.Reload;
import com.darkbladedev.commands.functions.common.SystemManager;
import com.darkbladedev.commands.functions.common.TestPersistence;

public class Common implements CommandFunction {

    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();

    public Common() {
        subcommands.put("reload", new Reload());
        subcommands.put("diagnostic", new Diagnostic());
        subcommands.put("test-persistence", new TestPersistence());
        subcommands.put("autosave", new AutoSave());
        subcommands.put("systems", new SystemManager());
    }

    @Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }

}
