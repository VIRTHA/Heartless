package com.darkbladedev.commands.nodes;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.functions.bonus.EnableBonus;
import com.darkbladedev.commands.functions.bonus.DisableBonus;
import com.darkbladedev.commands.functions.bonus.AddBonus;
import com.darkbladedev.commands.functions.bonus.RemoveBonus;
import com.darkbladedev.commands.functions.bonus.ListBonus;
import com.darkbladedev.commands.functions.bonus.TestBonus;

import java.util.HashMap;
import java.util.Map;

/**
 * Comandos para gestionar el sistema de bonificación por permisos
 */
public class Bonus implements CommandFunction {

    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();

    public Bonus() {
        subcommands.put("enable", new EnableBonus());
        subcommands.put("disable", new DisableBonus());
        subcommands.put("add", new AddBonus());
        subcommands.put("remove", new RemoveBonus());
        subcommands.put("list", new ListBonus());
        subcommands.put("test", new TestBonus());
    }

    @Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }
}