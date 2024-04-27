package com.mineplex.studio.example.survivalgames.modules.worlddemo.commands;

import com.mineplex.studio.sdk.modules.i18n.MineplexMessageComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * This class contains all the message components for the Survival Games world demo system.
 */
public class DemoWorldCommandMessageComponent extends MineplexMessageComponent {
    /**
     * The message for missing arguments.
     */
    public static final Args1<Component> MISSING_ARGUMENTS =
            arguments -> withCommandPrefix(Component.translatable("sg.module.worlddemo.command.missing_argument")
                    .color(NamedTextColor.GRAY)
                    .args(arguments.color(NamedTextColor.BLUE)));
    /**
     * The message for an invalid world ID.
     */
    public static final Args1<Component> INVALID_ID =
            id -> withCommandPrefix(Component.translatable("sg.module.worlddemo.command.invalid_id")
                    .color(NamedTextColor.RED)
                    .args(element(id)));
    /**
     * The message for loaded world.
     */
    public static final Args1<Component> WORLD_LOAD =
            world -> withCommandPrefix(Component.translatable("sg.module.worlddemo.command.load")
                    .color(NamedTextColor.GREEN)
                    .arguments(element(world)));
    /**
     * The message for unloaded world.
     */
    public static final Args1<Component> WORLD_UNLOAD =
            world -> withCommandPrefix(Component.translatable("sg.module.worlddemo.command.unload")
                    .color(NamedTextColor.GREEN)
                    .arguments(element(world)));
    /**
     * The message for deleted world.
     */
    public static final Args1<Component> WORLD_DELETE =
            world -> withCommandPrefix(Component.translatable("sg.module.worlddemo.command.delete")
                    .color(NamedTextColor.RED)
                    .arguments(element(world)));
}
