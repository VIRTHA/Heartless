package com.darkbladedev.commands.nodes;

import java.util.HashMap;
import java.util.Map;

import com.darkbladedev.commands.CommandFunction;
import com.darkbladedev.commands.SubcommandExecutor;
import com.darkbladedev.commands.functions.enchantments.Apply;
import com.darkbladedev.commands.functions.enchantments.Clear;
import com.darkbladedev.commands.functions.enchantments.Compare;
import com.darkbladedev.commands.functions.enchantments.Info;
import com.darkbladedev.commands.functions.enchantments.ItemInfo;
import com.darkbladedev.commands.functions.enchantments.List;
import com.darkbladedev.commands.functions.enchantments.Remove;
import com.darkbladedev.commands.functions.enchantments.Upgrade;

public class Enchantments implements CommandFunction {

    private final Map<String, SubcommandExecutor> subcommands = new HashMap<>();
    
    public Enchantments() {
        subcommands.put("apply", new Apply());
        subcommands.put("remove", new Remove());
        subcommands.put("clear", new Clear());
        subcommands.put("list", new List());
        subcommands.put("info", new Info());
        subcommands.put("iteminfo", new ItemInfo());
        subcommands.put("upgrade", new Upgrade());
        subcommands.put("compare", new Compare());
    }

    @Override
    public Map<String, SubcommandExecutor> getSubcommands() {
        return subcommands;
    }
}
