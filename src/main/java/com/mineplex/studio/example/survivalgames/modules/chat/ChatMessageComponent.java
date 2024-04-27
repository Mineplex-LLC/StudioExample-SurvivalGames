package com.mineplex.studio.example.survivalgames.modules.chat;

import com.mineplex.studio.sdk.modules.i18n.MineplexMessageComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * This class contains all the message components for the Survival Games chat system.
 */
public class ChatMessageComponent extends MineplexMessageComponent {
    /**
     * The prefix for a dead player.
     */
    public static final Args0 DEAD_PREFIX = () -> Component.join(
                    JoinConfiguration.noSeparators(), Component.translatable("sg.module.chat.dead"), Component.empty())
            .decorate(TextDecoration.BOLD)
            .color(NamedTextColor.WHITE);

    /**
     * The prefix for a player.
     */
    public static final Args1<Component> PREFIX = playerPrefix -> Component.join(
                    JoinConfiguration.noSeparators(), Component.text("["), playerPrefix, Component.text("]"))
            .color(NamedTextColor.GREEN);

    /**
     * The formatted stat message.
     */
    public static final Args2<Component, Long> STAT = (statName, value) -> Component.join(
                    JoinConfiguration.noSeparators(),
                    statName.color(NamedTextColor.YELLOW),
                    Component.text(": ").color(NamedTextColor.YELLOW),
                    Component.text(value).color(NamedTextColor.WHITE))
            .color(NamedTextColor.GRAY);

    /**
     * The the dead stat name.
     */
    public static final Args0 DEAD_STAT = () -> Component.translatable("sg.module.chat.stat.deaths");
    /**
     * The kill stat name.
     */
    public static final Args0 KILL_STAT = () -> Component.translatable("sg.module.chat.stat.kills");
}
