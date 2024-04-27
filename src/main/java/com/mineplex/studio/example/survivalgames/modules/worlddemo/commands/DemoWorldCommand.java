package com.mineplex.studio.example.survivalgames.modules.worlddemo.commands;

import com.mineplex.studio.example.survivalgames.modules.worlddemo.WorldDemoModule;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@link org.bukkit.command.Command} for controlling the {@link WorldDemoModule}.
 */
public class DemoWorldCommand extends Command {
    /**
     * The name of the command.
     */
    private static final String COMMAND_NAME = "demoworld";
    /**
     * The syntax of the command.
     */
    private static final String COMMAND_SYNTAX = "<load|unload|delete> <worldID>";

    // Modules
    private final WorldDemoModule worldDemoModule;

    public DemoWorldCommand(final WorldDemoModule worldDemoModule) {
        super(COMMAND_NAME, "", String.format("/%s %s", COMMAND_NAME, COMMAND_SYNTAX), List.of());

        this.worldDemoModule = worldDemoModule;
    }

    public boolean onLoad(final Player player, final String worldID) {
        this.worldDemoModule
                .loadDemoWorld(worldID)
                .thenAccept(world -> DemoWorldCommandMessageComponent.WORLD_LOAD.send(
                        player, Component.text(world.getMinecraftWorld().getName())));
        return true;
    }

    public boolean onUnload(final Player player, final String worldID) {
        if (this.worldDemoModule.unloadDemoWorld(worldID)) {
            DemoWorldCommandMessageComponent.WORLD_UNLOAD.send(player, Component.text(worldID));
        } else {
            DemoWorldCommandMessageComponent.INVALID_ID.send(player, Component.text(worldID));
        }

        return true;
    }

    public boolean onDelete(final Player player, final String worldID) {
        this.worldDemoModule.unloadDemoWorld(worldID);
        this.worldDemoModule
                .deleteDemoWorld(worldID)
                .thenAccept(v -> DemoWorldCommandMessageComponent.WORLD_DELETE.send(player, Component.text(worldID)));

        return true;
    }

    @Override
    public boolean execute(
            @NotNull final CommandSender commandSender, @NotNull final String s, @NotNull final String[] strings) {
        if (!(commandSender instanceof final Player player)) {
            return false;
        }

        if (strings.length < 2) {
            DemoWorldCommandMessageComponent.MISSING_ARGUMENTS.send(player, Component.text(COMMAND_SYNTAX));
            return false;
        }

        final String worldID = strings[1];
        return switch (strings[0].toLowerCase(Locale.ENGLISH)) {
            case "load" -> this.onLoad(player, worldID);
            case "unload" -> this.onUnload(player, worldID);
            case "delete" -> this.onDelete(player, worldID);
            default -> false;
        };
    }
}
