package com.mineplex.studio.example.survivalgames.modules.worlddemo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.mineplex.studio.example.survivalgames.SurvivalGamesI18nText;
import com.mineplex.studio.example.survivalgames.modules.worlddemo.WorldDemoModule;
import com.mineplex.studio.sdk.i18n.I18nText;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

/**
 * Represents a {@link org.bukkit.command.Command} for controlling the {@link WorldDemoModule}.
 */
@RequiredArgsConstructor
@CommandAlias("demoworld")
public class DemoWorldCommand extends BaseCommand {
    // Messages
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

    @Subcommand("load")
    @Syntax("<worldID>")
    public void onLoad(final Player player, final String worldID) {
        this.worldDemoModule.loadDemoWorld(worldID).thenAccept(world -> {
            final Component message = MiniMessage.miniMessage()
                    .deserialize(
                            WORLD_LOAD.getText(player.locale()),
                            Placeholder.parsed(
                                    "world", world.getMinecraftWorld().getName()));
            player.sendMessage(message);
        });
    }

    @Subcommand("unload")
    @Syntax("<worldID>")
    public void onUnload(final Player player, final String worldID) {
        final Component message;
        if (this.worldDemoModule.unloadDemoWorld(worldID)) {
            message = MiniMessage.miniMessage()
                    .deserialize(WORLD_UNLOAD.getText(player.locale()), Placeholder.parsed("world", worldID));
        } else {
            message = MiniMessage.miniMessage()
                    .deserialize(WORLD_INVALID_ID.getText(player.locale()), Placeholder.parsed("world", worldID));
        }

        player.sendMessage(message);
    }

    @Subcommand("delete")
    @Syntax("<worldID>")
    @Description("DANGER: Permanently delete the demo world.")
    public void onDelete(final Player player, final String worldID) {
        this.worldDemoModule.unloadDemoWorld(worldID);
        this.worldDemoModule.deleteDemoWorld(worldID).thenAccept(v -> {
            final Component message = MiniMessage.miniMessage()
                    .deserialize(WORLD_DELETE.getText(player.locale()), Placeholder.parsed("world", worldID));
            player.sendMessage(message);
        });
    }
}
