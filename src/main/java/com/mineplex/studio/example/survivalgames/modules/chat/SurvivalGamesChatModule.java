package com.mineplex.studio.example.survivalgames.modules.chat;

import com.mineplex.studio.example.survivalgames.SurvivalGamesPlugin;
import com.mineplex.studio.example.survivalgames.game.stat.SurvivalGamesStats;
import com.mineplex.studio.example.survivalgames.modules.prefix.ChatPrefixModule;
import com.mineplex.studio.sdk.modules.MineplexModule;
import com.mineplex.studio.sdk.modules.MineplexModuleImplementation;
import com.mineplex.studio.sdk.modules.MineplexModuleManager;
import com.mineplex.studio.sdk.modules.chat.BuiltInChatChannel;
import com.mineplex.studio.sdk.modules.chat.ChatModule;
import com.mineplex.studio.sdk.modules.game.MineplexGameModule;
import com.mineplex.studio.sdk.modules.game.PlayerState;
import com.mineplex.studio.sdk.modules.stats.StatsModule;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * The {@link SurvivalGamesChatModule} is responsible
 * for setting up the {@link com.mineplex.studio.example.survivalgames.game.SurvivalGames} chat system,
 * including adding prefixes and handling the rendering of chat messages.
 */
@MineplexModuleImplementation(SurvivalGamesChatModule.class)
public class SurvivalGamesChatModule implements MineplexModule {
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
     * Retrieve the user-specified prefix component for a player.
     *
     * @param player the player for which to retrieve the prefix component
     * @return a Component object representing the user-specified prefix
     */
    private Component getUserSpecifiedPrefixComponent(final Player player) {
        return this.prefixModule
                .getPrefix(player)
                .map(Component::text)
                .map(ChatMessageComponent.PREFIX::apply)
                .orElseGet(() -> Component.text().asComponent());
    }

    /**
     * Retrieve the hovering text for the player's stats.
     *
     * @param source the player for which to retrieve the stats hovering text
     * @return a Component object representing the hovering text for the player's stats
     */
    private Component getStatsHoverText(final Player source) {
        if (SurvivalGamesPlugin.LOCAL_TESTING) {
            return Component.text().asComponent();
        }

        final Map<String, Long> stats = this.statsModule.getPlayerStats(source);
        if (stats.isEmpty()) {
            return Component.text().asComponent();
        }

        final List<Component> hoverComponents = new ArrayList<>(2);
        final BiConsumer<Component, String> statAppender = (displayName, statName) -> {
            final Long stat = stats.get(statName);
            if (stat == null) {
                return;
            }

            hoverComponents.add(ChatMessageComponent.STAT.apply(displayName, stat));
        };

        statAppender.accept(ChatMessageComponent.DEAD_STAT.apply(), SurvivalGamesStats.DEATHS.getStatName());
        statAppender.accept(ChatMessageComponent.KILL_STAT.apply(), SurvivalGamesStats.KILLS.getStatName());

        return Component.join(JoinConfiguration.newlines(), hoverComponents);
    }

    /**
     * Method called to allocate any additional resources this module uses
     */
    @Override
    public void setup() {
        this.statsModule = MineplexModuleManager.getRegisteredModule(StatsModule.class);
        this.prefixModule = MineplexModuleManager.getRegisteredModule(ChatPrefixModule.class);

        final MineplexGameModule gameModule = MineplexModuleManager.getRegisteredModule(MineplexGameModule.class);
        final ChatModule chatModule = MineplexModuleManager.getRegisteredModule(ChatModule.class);
        chatModule.setAudienceFunction(BuiltInChatChannel.GLOBAL, sender -> Set.copyOf(Bukkit.getOnlinePlayers()));
        chatModule.setChatRenderer(BuiltInChatChannel.GLOBAL, (source, sourceDisplayName, message, viewer) -> {
            final List<Component> components = new ArrayList<>(3);

            // Dead player prefix
            gameModule
                    .getCurrentGame()
                    .map(game -> game.getPlayerState(source))
                    .filter(Predicate.not(PlayerState::isAlive))
                    .ifPresent(g -> components.add(ChatMessageComponent.DEAD_PREFIX.apply()));

            // Add stats hover message to player name
            final Component displayName =
                    sourceDisplayName.hoverEvent(HoverEvent.showText(this.getStatsHoverText(source)));
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
