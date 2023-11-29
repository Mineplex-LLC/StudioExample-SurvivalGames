package com.mineplex.studio.example.survivalgames.modules.prefix.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.mineplex.studio.example.survivalgames.SurvivalGamesI18nText;
import com.mineplex.studio.example.survivalgames.modules.prefix.ChatPrefixModule;
import com.mineplex.studio.sdk.i18n.I18nText;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

/**
 * Represents a {@link org.bukkit.command.Command} for setting or removing a {@link Player} prefix.
 */
@RequiredArgsConstructor
@CommandAlias("prefix")
public class PrefixCommand extends BaseCommand {
    // Messages
    /**
     * The {@link I18nText} for a successful removed prefix.
     * This message follows the {@link MiniMessage} format.
     */
    private static final I18nText PREFIX_REMOVE =
            new SurvivalGamesI18nText("COMMAND_PREFIX_REMOVE", "<red>Removed prefix!</red>");
    /**
     * The {@link I18nText} for a successful set prefix.
     * This message follows the {@link MiniMessage} format.
     */
    private static final I18nText PREFIX_SET =
            new SurvivalGamesI18nText("COMMAND_PREFIX_SET", "<green>Set new prefix!</green>");

    // Modules
    /**
     * Prefix module.
     */
    private final ChatPrefixModule module;

    /**
     * Sets the prefix for a {@link Player}.
     *
     * @param player the player for whom the prefix will be set
     * @param args   the arguments passed to the command. If empty, the prefix will be reset.
     */
    @Default
    @Syntax("<prefix> or empty to reset")
    @Description("Set your own prefix.")
    public void onCommand(final Player player, final String[] args) {
        // Reset the prefix
        if (args.length == 0) {
            this.module.removePrefix(player);
            final Component message = MiniMessage.miniMessage().deserialize(PREFIX_REMOVE.getText(player.locale()));
            player.sendMessage(message);
            return;
        }

        // Set new prefix
        final String prefix = String.join(" ", args);
        this.module.updatePrefix(player, prefix);
        final Component message = MiniMessage.miniMessage().deserialize(PREFIX_SET.getText(player.locale()));
        player.sendMessage(message);
    }
}
