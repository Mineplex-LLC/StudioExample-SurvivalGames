package com.mineplex.studio.example.survivalgames.modules.prefix.commands;

import com.mineplex.studio.example.survivalgames.modules.prefix.ChatPrefixModule;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link org.bukkit.command.Command} for setting or removing a {@link Player} prefix.
 */
public class PrefixCommand extends Command {
    /**
     * The name of the command.
     */
    private static final String COMMAND_NAME = "prefix";
    /**
     * The syntax of the command.
     */
    private static final String COMMAND_SYNTAX = "<prefix> or empty to reset";

    // Modules
    /**
     * Prefix module.
     */
    private final ChatPrefixModule module;

    public PrefixCommand(final ChatPrefixModule module) {
        super(COMMAND_NAME, "", String.format("/%s %s", COMMAND_NAME, COMMAND_SYNTAX), List.of());

        this.module = module;
    }

    /**
     * Sets the prefix for a {@link Player}.
     */
    @Override
    public boolean execute(
            @NotNull final CommandSender commandSender, @NotNull final String s, @NotNull final String[] args) {
        if (!(commandSender instanceof final Player player)) {
            return false;
        }

        // Reset the prefix
        if (args.length == 0) {
            this.module.removePrefix(player);
            PrefixCommandMessageComponent.PREFIX_REMOVE.send(player);
            return true;
        }

        // Set new prefix
        final String prefix = String.join(" ", args);
        this.module.updatePrefix(player, prefix);
        PrefixCommandMessageComponent.PREFIX_SET.send(player);
        return true;
    }
}
