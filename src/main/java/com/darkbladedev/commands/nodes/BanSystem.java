package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.functions.bansystem.Get;
import com.darkbladedev.commands.functions.bansystem.Unban;

public class BanSystem implements CommandFunction {

    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();

    public BanSystem() {
        subcommands.put("unban", new Unban());
        subcommands.put("list", new Get());
    }

    @Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }

}
