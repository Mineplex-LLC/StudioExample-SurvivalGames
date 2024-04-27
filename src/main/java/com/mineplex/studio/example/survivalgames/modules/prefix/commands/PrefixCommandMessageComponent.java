package com.mineplex.studio.example.survivalgames.modules.prefix.commands;

import com.mineplex.studio.sdk.modules.i18n.MineplexMessageComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * This class contains all the message components for the Survival Games prefix command.
 */
public class PrefixCommandMessageComponent extends MineplexMessageComponent {
    /**
     * The message for removing a prefix.
     */
    public static final Args0 PREFIX_REMOVE = () -> withCommandPrefix(
            Component.translatable("sg.module.prefix.command.remove").color(NamedTextColor.RED));
    /**
     * The message for setting a prefix.
     */
    public static final Args0 PREFIX_SET = () -> withCommandPrefix(
            Component.translatable("sg.module.prefix.command.set").color(NamedTextColor.GREEN));
}
