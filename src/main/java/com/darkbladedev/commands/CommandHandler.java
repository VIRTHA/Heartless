package com.darkbladedev.commands;

import com.darkbladedev.commands.nodes.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final Map<String, CommandFunction> commands = new HashMap<>();
    @SuppressWarnings("unused")
    private final Map<String, CommandFunction> subcommands = new HashMap<>();

    public CommandHandler() {
        register("bansystem", new BanSystem());
        register("effects", new Effects());
        register("enchantments", new Enchantments());
        register("event", new EventControl());
        register("health", new Health());
    }

    private void register(String name, CommandFunction function) {
        commands.put(name.toLowerCase(), function);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Uso: /" + label + " <grupo> <acción> [args...]");
            return true;
        }

        String group = args[0].toLowerCase();
        String action = args[1].toLowerCase();

        CommandFunction function = commands.get(group);
        if (function == null) {
            sender.sendMessage("Comando desconocido: " + group);
            return true;
        }

        SubcommandExecutor executor = function.getSubcommands().get(action);
        if (executor == null) {
            sender.sendMessage("Acción desconocida: " + action);
            return true;
        }

        // Pasa los args a partir del índice 2
        String[] remainingArgs = new String[args.length - 2];
        System.arraycopy(args, 2, remainingArgs, 0, remainingArgs.length);

        executor.execute(sender, remainingArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Autocompletar grupo
            return commands.keySet().stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            // Autocompletar acción dentro del grupo
            String group = args[0].toLowerCase();
            CommandFunction func = commands.get(group);
            if (func == null) return Collections.emptyList();

            return func.getSubcommands().keySet().stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length >= 3) {
            String group = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            CommandFunction func = commands.get(group);
            if (func == null) return Collections.emptyList();

            SubcommandExecutor executor = func.getSubcommands().get(action);
            if (executor instanceof TabCompletable) {
                // Si el executor soporta autocompletado custom, se lo delegamos
                return ((TabCompletable) executor).onTabComplete(sender, args);
            }
        }

        return Collections.emptyList();
    }

}