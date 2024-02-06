package com.mineplex.studio.example.survivalgames.util;

import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

/**
 * Utility methods to handle {@link Command}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandUtil {
    private static final String COMMAND_PREFIX = "sg";

    /**
     * Register a {@link Command} to the {@link CommandMap}.
     *
     * @param command the command
     */
    public static void register(final Command command) {
        Bukkit.getCommandMap().register(COMMAND_PREFIX, command);
    }

    /**
     * Unregister a {@link Command} to the {@link CommandMap}.
     *
     * @param command the command
     */
    public static void unRegister(final Command command) {
        final CommandMap commandMap = Bukkit.getCommandMap();
        command.unregister(commandMap);
        commandMap.getKnownCommands().remove(command.getName().toLowerCase(Locale.ROOT));
    }
}
