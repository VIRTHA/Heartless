package com.darkbladedev.commands.nodes;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.functions.health.AddHealth;
import com.darkbladedev.commands.functions.health.AddMaxHealth;
import com.darkbladedev.commands.functions.health.RemoveHealth;
import com.darkbladedev.commands.functions.health.RemoveMaxHealth;
import com.darkbladedev.commands.functions.health.SetHealth;
import com.darkbladedev.commands.functions.health.SetMaxHealth;

import java.util.HashMap;
import java.util.Map;

public class Health implements CommandFunction {

    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();

    public Health() {
        subcommands.put("add", new AddHealth());

        subcommands.put("remove", new RemoveHealth());

        subcommands.put("set", new SetHealth());

        subcommands.put("add-max", new AddMaxHealth());

        subcommands.put("remove-max", new RemoveMaxHealth());

        subcommands.put("set-max", new SetMaxHealth());
    }

    @Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }
}
