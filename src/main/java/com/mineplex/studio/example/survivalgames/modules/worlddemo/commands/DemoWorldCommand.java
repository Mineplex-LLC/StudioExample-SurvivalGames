package com.mineplex.studio.example.survivalgames.modules.worlddemo.commands;

import com.mineplex.studio.example.survivalgames.SurvivalGamesI18nText;
import com.mineplex.studio.example.survivalgames.modules.worlddemo.WorldDemoModule;
import com.mineplex.studio.sdk.i18n.I18nText;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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

    // Messages
    /**
     * The {@link I18nText} for the invalid command syntax.
     * This message follows the {@link MiniMessage} format.
     */
    private static final I18nText MISSING_ARGUMENTS = new SurvivalGamesI18nText(
            "COMMAND_DEMO_WORLD_INVALID_SYNTAX", "<gray>Missing required arguments: <blue><arguments></blue></gray>");

    /**
     * The {@link I18nText} for a invalid {@link com.mineplex.studio.sdk.modules.world.MineplexWorld} id.
     * This message follows the {@link MiniMessage} format.
     */
    private static final I18nText WORLD_INVALID_ID = new SurvivalGamesI18nText(
            "COMMAND_DEMO_WORLD_INVALID_ID", "<red>The provided ID <yellow><world></yellow> was not found!</red>");
    /**
     * The {@link I18nText} for a successfully loaded {@link com.mineplex.studio.sdk.modules.world.MineplexWorld}.
     * This message follows the {@link MiniMessage} format.
     */
    private static final I18nText WORLD_LOAD = new SurvivalGamesI18nText(
            "COMMAND_DEMO_WORLD_LOAD", "<green>Loaded world <yellow><world></yellow>.</green>");
    /**
     * The {@link I18nText} for a successfully unloaded {@link com.mineplex.studio.sdk.modules.world.MineplexWorld}.
     * This message follows the {@link MiniMessage} format.
     */
    private static final I18nText WORLD_UNLOAD = new SurvivalGamesI18nText(
            "COMMAND_DEMO_WORLD_UNLOAD", "<green>Unloaded world <yellow><world></yellow>.</green>");
    /**
     * The {@link I18nText} for a successfully deleted {@link com.mineplex.studio.sdk.modules.world.MineplexWorld}.
     * This message follows the {@link MiniMessage} format.
     */
    private static final I18nText WORLD_DELETE = new SurvivalGamesI18nText(
            "COMMAND_DEMO_WORLD_DELETE", "<red>Deleted world <yellow><world></yellow>!</red>");

    // Modules
    private final WorldDemoModule worldDemoModule;

    public DemoWorldCommand(final WorldDemoModule worldDemoModule) {
        super(COMMAND_NAME, "", String.format("/%s %s", COMMAND_NAME, COMMAND_SYNTAX), List.of());

        this.worldDemoModule = worldDemoModule;
    }

    public boolean onLoad(final Player player, final String worldID) {
        this.worldDemoModule.loadDemoWorld(worldID).thenAccept(world -> {
            final Component message = MiniMessage.miniMessage()
                    .deserialize(
                            WORLD_LOAD.getText(player.locale()),
                            Placeholder.parsed(
                                    "world", world.getMinecraftWorld().getName()));
            player.sendMessage(message);
        });
        return true;
    }

    public boolean onUnload(final Player player, final String worldID) {
        final Component message;
        if (this.worldDemoModule.unloadDemoWorld(worldID)) {
            message = MiniMessage.miniMessage()
                    .deserialize(WORLD_UNLOAD.getText(player.locale()), Placeholder.parsed("world", worldID));
        } else {
            message = MiniMessage.miniMessage()
                    .deserialize(WORLD_INVALID_ID.getText(player.locale()), Placeholder.parsed("world", worldID));
        }

        player.sendMessage(message);
        return true;
    }

    public boolean onDelete(final Player player, final String worldID) {
        this.worldDemoModule.unloadDemoWorld(worldID);
        this.worldDemoModule.deleteDemoWorld(worldID).thenAccept(v -> {
            final Component message = MiniMessage.miniMessage()
                    .deserialize(WORLD_DELETE.getText(player.locale()), Placeholder.parsed("world", worldID));
            player.sendMessage(message);
        });

        return true;
    }

    @Override
    public boolean execute(
            @NotNull final CommandSender commandSender, @NotNull final String s, @NotNull final String[] strings) {
        if (!(commandSender instanceof final Player player)) {
            return false;
        }

        if (strings.length < 2) {
            player.sendMessage(MiniMessage.miniMessage()
                    .deserialize(
                            MISSING_ARGUMENTS.getText(player.locale()),
                            Placeholder.parsed("arguments", MISSING_ARGUMENTS.getText(player.locale()))));
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
