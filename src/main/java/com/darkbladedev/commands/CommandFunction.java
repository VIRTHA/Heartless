package com.darkbladedev.commands;

import java.util.Map;

public interface CommandFunction {
    Map<String, SubcommandExecutor> getSubcommands();
    //boolean isEnabled();
    //void setEnabled(boolean enabled);
}
