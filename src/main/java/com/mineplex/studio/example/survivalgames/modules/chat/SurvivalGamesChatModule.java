package com.mineplex.studio.example.survivalgames.modules.chat;

import com.mineplex.studio.example.survivalgames.SurvivalGamesI18nText;
import com.mineplex.studio.example.survivalgames.SurvivalGamesPlugin;
import com.mineplex.studio.example.survivalgames.game.stat.SurvivalGamesStats;
import com.mineplex.studio.example.survivalgames.modules.prefix.ChatPrefixModule;
import com.mineplex.studio.sdk.i18n.I18nText;
import com.mineplex.studio.sdk.modules.MineplexModule;
import com.mineplex.studio.sdk.modules.MineplexModuleImplementation;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.chat.BuiltInChatChannel;
import com.mineplex.studio.sdk.modules.chat.ChatModule;
import com.mineplex.studio.sdk.modules.stats.StatsModule;
import java.util.*;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * The {@link SurvivalGamesChatModule} is responsible
 * for setting up the {@link com.mineplex.studio.example.survivalgames.game.SurvivalGames} chat system,
 * including adding prefixes and handling the rendering of chat messages.
 */
@MineplexModuleImplementation(SurvivalGamesChatModule.class)
public class SurvivalGamesChatModule implements MineplexModule {
    // Messages
    /**
     * The {@link I18nText} used as a spectator prefix.
     */
    private static final I18nText DEAD_PREFIX = new SurvivalGamesI18nText("DeadPrefix", "DEAD");
    /**
     * The {@link I18nText} used to represent {@link SurvivalGamesStats#DEATHS}.
     */
    private static final I18nText DEAD_STAT = new SurvivalGamesI18nText("Deaths", "Deaths");
    /**
     * The {@link I18nText} used to represent {@link SurvivalGamesStats#KILLS}.
     */
    private static final I18nText KILL_STAT = new SurvivalGamesI18nText("Kills", "Kills");

    // Modules
    /**
     * The {@link StatsModule} is responsible for saving and accessing {@link Player} stats.
     */
    private StatsModule statsModule;
    /**
     * The {@link ChatPrefixModule} is responsible for saving and accessing {@link Player} prefix.
     */
    private ChatPrefixModule prefixModule;

    /**
     * Create a prefix {@link Component} for dead players.
     *
     * @param locale the locale for which to retrieve the dead prefix component
     * @return a Component object representing the dead prefix
     */
    private Component getDeadPrefixComponent(final Locale locale) {
        return MiniMessage.miniMessage()
                .deserialize(
                        "<white><b><prefix> </b></white>", Placeholder.parsed("prefix", DEAD_PREFIX.getText(locale)));
    }

    /**
     * Retrieve the user-specified prefix component for a player.
     *
     * @param player the player for which to retrieve the prefix component
     * @return a Component object representing the user-specified prefix
     */
    private Component getUserSpecifiedPrefixComponent(final Player player) {
        return this.prefixModule
                .getPrefix(player)
                .map(prefix -> MiniMessage.miniMessage()
                        .deserialize("<green>[<prefix>]</green>", Placeholder.parsed("prefix", prefix)))
                .orElseGet(() -> Component.text().asComponent());
    }

    /**
     * Retrieve the hovering text for the player's stats.
     *
     * @param source the player for which to retrieve the stats hovering text
     * @param locale the locale to use for translating stat names
     * @return a Component object representing the hovering text for the player's stats
     */
    private Component getStatsHoverText(final Player source, final Locale locale) {
        if (SurvivalGamesPlugin.LOCAL_TESTING) {
            return Component.text().asComponent();
        }

        final Map<String, Long> stats = this.statsModule.getPlayerStats(source);
        if (stats.isEmpty()) {
            return Component.text().asComponent();
        }

        final List<Component> hoverComponents = new ArrayList<>(2);
        final BiConsumer<I18nText, String> statAppender = (text, statName) -> {
            final Long stat = stats.get(statName);
            if (stat == null) {
                return;
            }

            hoverComponents.add(MiniMessage.miniMessage()
                    .deserialize(
                            "<yellow><stat-name>: </yellow><white><stat></white>",
                            Placeholder.parsed("stat-name", text.getText(locale)),
                            Placeholder.parsed("stat", String.valueOf(stat))));
        };

        statAppender.accept(DEAD_STAT, SurvivalGamesStats.DEATHS.getStatName());
        statAppender.accept(KILL_STAT, SurvivalGamesStats.KILLS.getStatName());

        return Component.join(JoinConfiguration.newlines(), hoverComponents);
    }

    /**
     * Method called to allocate any additional resources this module uses
     */
    @Override
    public void setup() {
        this.statsModule = MineplexModuleManager.getRegisteredModule(StatsModule.class);
        this.prefixModule = MineplexModuleManager.getRegisteredModule(ChatPrefixModule.class);

        final ChatModule chatModule = MineplexModuleManager.getRegisteredModule(ChatModule.class);
        chatModule.setAudienceFunction(BuiltInChatChannel.GLOBAL, sender -> Set.copyOf(Bukkit.getOnlinePlayers()));
        chatModule.setChatRenderer(BuiltInChatChannel.GLOBAL, (source, sourceDisplayName, message, viewer) -> {
            final Locale locale =
                    viewer instanceof final Player viewerPlayer ? viewerPlayer.locale() : Locale.getDefault();

            final List<Component> components = new ArrayList<>(3);
            // TODO: Migrate to PlayerState system
            // Dead player prefix
            if (source.getGameMode() == GameMode.SPECTATOR) {
                components.add(this.getDeadPrefixComponent(locale));
            }

            // Add stats hover message to player name
            final Component displayName =
                    sourceDisplayName.hoverEvent(HoverEvent.showText(this.getStatsHoverText(source, locale)));
            components.addAll(List.of(
                    this.getUserSpecifiedPrefixComponent(source),
                    Component.translatable("chat.type.text", displayName, message)));

            // Construct new chat message from components
            return Component.join(JoinConfiguration.noSeparators(), components);
        });
    }

    /**
     * Method called to release and cleanup any additional resources this module uses
     */
    @Override
    public void teardown() {
        // TODO: Undo chat render
    }
}
