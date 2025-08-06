package com.darkbladedev.commands;

import java.util.Map;

public interface CommandFunction {
    Map<String, SubcommandExecutor> getSubcommands();
}
